package com.dse.thread.task;

import com.dse.compiler.Compiler;
import com.dse.compiler.message.CompileMessage;
import com.dse.compiler.message.ICompileMessage;
import com.dse.config.CommandConfig;
import com.dse.config.compile.LinkEntry;
import com.dse.environment.Environment;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.HeaderNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;
import com.dse.util.CompilerUtils;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.dse.guifx_v3.controllers.build_environment.BaseController.BUILD_NEW_ENVIRONMENT;

public class CompileProjectTask extends AbstractAkaTask<BuildEnvironmentResult> {

    private static final AkaLogger logger = AkaLogger.get(CompileProjectTask.class);

    private final AtomicInteger index = new AtomicInteger();

    private final INode root;

    private List<INode> sourceCodes;

    private Compiler compiler;

    private final Map<String, String> compilationCommands = new HashMap<>();

    public CompileProjectTask(INode root) {
        this.root = root;
    }

    private final List<IncludeHeaderDependency> includeDependencies = new ArrayList<>();

    @Override
    protected BuildEnvironmentResult call() {
        BuildEnvironmentResult result = new BuildEnvironmentResult();

        if (root != null) {
            sourceCodes = findAllSourceCodeFiles();

            compiler = Environment.getInstance().getCompiler();

            if (sourceCodes.isEmpty()) {
                result.setExitCode(BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION);
            }

            ICompileMessage compileMessage = new CompileMessage();
            ICompileMessage linkMessage = null;
            ICompileMessage message = findErrorSourcecodeFile();
            if (index.get() == sourceCodes.size()) {
                linkMessage = message;
            } else {
                compileMessage = message;
            }

            result.setCompileMessage(compileMessage);
            result.setLinkMessage(linkMessage);

            if (message.getType() == ICompileMessage.MessageType.ERROR) {
                if (linkMessage == null)
                    result.setExitCode(BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION);
                else
                    result.setExitCode(BUILD_NEW_ENVIRONMENT.FAILURE.LINKAGE);
            } else
                result.setExitCode(BUILD_NEW_ENVIRONMENT.SUCCESS.COMPILATION);
        } else
            result.setExitCode(BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION);

        result.setSourceCodes(sourceCodes);
        result.setCompilationCommands(compilationCommands);
        result.setFileIndex(index);

        return result;
    }

    private List<INode> findAllSourceCodeFiles() {
        // search for source code file nodes in source code file lists and compile them
        List<INode> sourceFiles = Search.searchNodes(root, new SourcecodeFileNodeCondition());
        List<INode> ignores = Environment.getInstance().getIgnores();
//        List<INode> uuts = Environment.getInstance().getUUTs();
//        List<INode> sbfs = Environment.getInstance().getSBFs();
        List<String> libraries = ProjectClone.getLibraries();
        sourceFiles.removeIf(f -> ignores.contains(f) || libraries.contains(f.getAbsolutePath())
//                || (f instanceof HeaderNode && (!uuts.contains(f) && !sbfs.contains(f)))
        );
        sourceFiles.forEach(sourceFile -> {
            try {
                String content = Utils.readFileContent(sourceFile.getAbsolutePath());
                IASTTranslationUnit iastNode = new SourcecodeFileParser().getIASTTranslationUnit(content.toCharArray());
                if (sourceFile instanceof ISourcecodeFileNode) {
                    ((ISourcecodeFileNode) sourceFile).setAST(iastNode);
                    fastGenIncludeDependency((ISourcecodeFileNode) sourceFile, sourceFiles);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        sourceFiles.removeIf(this::isIncludedFile);
        return sourceFiles;
    }

    private void fastGenIncludeDependency(ISourcecodeFileNode fileNode, List<INode> sourceCodes) {
        IASTTranslationUnit ast = fileNode.getAST();
        for (IASTPreprocessorIncludeStatement includeStatement : ast.getIncludeDirectives()) {
            String includeFilePath = includeStatement.getName().getRawSignature();

            /*
             * Library function se khong tim duoc trong day
             */
            includeFilePath = Utils.normalizePath(includeFilePath);
            String projectPath = Environment.getInstance().getProjectNode().getAbsolutePath();
            if (!includeFilePath.startsWith(projectPath)) {
                String parentFile = fileNode.getFile().getParentFile().getAbsolutePath();
                includeFilePath = Paths.get(parentFile, includeFilePath).normalize().toString();
            }

            String finalIncludeFilePath = includeFilePath;
            sourceCodes.stream()
                    .filter(f -> f.getAbsolutePath().equals(finalIncludeFilePath))
                    .findFirst()
                    .ifPresent(includedNode -> {
                        IncludeHeaderDependency d = new IncludeHeaderDependency(fileNode, includedNode);
                        includeDependencies.add(d);
                    });
        }
    }

    private ICompileMessage findErrorSourcecodeFile() {
        ICompileMessage message = null;

        if (index.get() >= 0 && index.get() < sourceCodes.size()) {
            do {
                INode fileNode = sourceCodes.get(index.get());
                String filePath = fileNode.getAbsolutePath();
                logger.debug("Compiling " + filePath);
                message = compiler.compile(fileNode);

                // save the compilation commands to file
                String relativePath = PathUtils.toRelative(filePath);
                if (!compilationCommands.containsKey(relativePath)) {
                    compilationCommands.put(relativePath, message.getCompilationCommand());
                    new CommandConfig().fromJson().setCompilationCommands(compilationCommands).exportToJson();
                }

                //
                if (message.getType() == ICompileMessage.MessageType.ERROR) {
                    break;

                } else {
                    // just warning or compile successfully
                    index.incrementAndGet();
                }
            } while (index.get() < sourceCodes.size());
        }

        if (!(message != null && message.getType() == ICompileMessage.MessageType.ERROR)) {
            logger.debug("Linking source code file to create an executable file");
            message = linkSourceFiles();
        }

        return message;
    }

    public static final String LINKAGE_ERR = "Can not create executable file";

    private ICompileMessage linkSourceFiles() {
        String executablePath = getExecutablePath(root);

        List<INode> linkableSourceCodes = sourceCodes.stream()
                .filter(n -> !(n instanceof HeaderNode))
                .collect(Collectors.toList());

        String[] filePaths = getAllOutputFiles(linkableSourceCodes);

        if (compiler != null) {
            ICompileMessage linkeMessage = compiler.link(executablePath, filePaths);

            if (!new File(executablePath).exists() && linkeMessage.getType() != ICompileMessage.MessageType.ERROR)
                linkeMessage = new CompileMessage(LINKAGE_ERR, "");
            else
                Utils.deleteFileOrFolder(new File(executablePath));

            logger.debug("Linking command: " + linkeMessage.getLinkingCommand());

            LinkEntry linkEntry = new LinkEntry();
            linkEntry.setBinFiles(Arrays.asList(filePaths));
            linkEntry.setCommand(compiler.getLinkCommand());
            linkEntry.setOutFlag(compiler.getOutputFlag());
            linkEntry.setExeFile(executablePath);

            new CommandConfig().fromJson()
                    .setLinkEntry(linkEntry)
                    .exportToJson();

            return linkeMessage;
        }

        return null;
    }

    private String[] getAllOutputFiles(List<INode> sourceCodes) {
        return sourceCodes.stream()
                .filter(file -> !isIncludedFile(file))
                .map(f -> CompilerUtils.getOutfilePath(f.getAbsolutePath(), compiler.getOutputExtension()))
                .toArray(String[]::new);
    }

    private String getExecutablePath(INode root) {
        String executablePath = root.getAbsolutePath();
        if (!root.getAbsolutePath().endsWith(File.separator))
            executablePath += File.separator;
        executablePath += "program.exe";
        return executablePath;
    }

    private boolean isIncludedFile(INode node) {
        for (IncludeHeaderDependency d : includeDependencies) {
            if (d.getEndArrow() == node)
                return true;
        }

        return false;
    }
}
