package com.dse.project_init;

import auto_testcase_generation.instrument.AbstractFunctionInstrumentation;
import auto_testcase_generation.instrument.FunctionInstrumentationForAllCoverages;
import auto_testcase_generation.instrument.FunctionInstrumentationForMacro;
import com.dse.compiler.Compiler;
import com.dse.compiler.Terminal;
import com.dse.config.CommandConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroLibraryIncludeDirNode;
import com.dse.environment.object.EnviroLibraryStubNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.logger.AkaLogger;
import com.dse.parser.IProjectLoader;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.dependency.IncludeHeaderDependencyGeneration;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.*;
import com.dse.stub_manager.StubManager;
import com.dse.stub_manager.SystemLibrary;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_manager.ITestCase;
import com.dse.thread.AbstractAkaTask;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.dse.config.WorkspaceConfig.WORKING_SPACE_NAME;

public class ProjectClone {

    public static final String CLONED_FILE_EXTENSION = ".akaignore";
    public static final String MAIN_REFACTOR_NAME = "AKA_MAIN(";
    public static final String MAIN_REGEX = "\\bmain\\b\\s*\\(";
    private static final AkaLogger logger = AkaLogger.get(ProjectClone.class);
    protected static List<String> sLibraries;
    protected final Map<String, String> refactors = new HashMap<>();
    List<String> globalDeclarations = new ArrayList<>();
    protected boolean whiteBoxEnable;
    protected List<String> libraries;
    protected List<IEnvironmentNode> stubLibraries;
    protected boolean canStub;

    /**
     * Clone all source code file with extension in project directories.
     */
    public static void cloneEnvironment() {
        List<IEnvironmentNode> stubLibraries = EnvironmentSearch
                .searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryStubNode());

        List<INode> stubUnits = Environment.getInstance().getStubs();
        List<INode> sbfUnits = Environment.getInstance().getSBFs();
        List<INode> uutUnits = Environment.getInstance().getUUTs();

        List<String> libraries = getLibraries();

        boolean onWhiteBoxMode = Environment.getInstance().isOnWhiteBoxMode();

        ProjectNode projectRoot = Environment.getInstance().getProjectNode();
        List<ISourcecodeFileNode> sources = Search.searchNodes(projectRoot, new SourcecodeFileNodeCondition());
        sources.removeIf(source -> libraries.contains(source.getAbsolutePath()));

        for (ISourcecodeFileNode sourceCode : sources) {
            try {
                String cloneFilePath = getClonedFilePath(sourceCode.getAbsolutePath());

                ProjectClone clone = new ProjectClone();

                clone.libraries = libraries;
                clone.whiteBoxEnable = (uutUnits.contains(sourceCode) || sbfUnits.contains(sourceCode))
                        && onWhiteBoxMode;
                clone.canStub = stubUnits.contains(sourceCode) || sbfUnits.contains(sourceCode);
                clone.stubLibraries = stubLibraries;

                Utils.copy(sourceCode.getFile(), new File(cloneFilePath));

                String newContent = clone.generateFileContent(sourceCode);
                logger.debug("Generate instrument file of " + sourceCode.getName() + " successfully");
                Utils.writeContentToFile(newContent, cloneFilePath);

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        StubManager.exportListToFile();
    }

    public static void cloneASourceCodeFile(ISourcecodeFileNode sourceCode) {
        if (sourceCode == null)
            return;

        List<String> libraries = getLibraries();
        List<INode> stubUnits = Environment.getInstance().getStubs();
        List<INode> sbfUnits = Environment.getInstance().getSBFs();
        List<INode> uutUnits = Environment.getInstance().getUUTs();
        List<IEnvironmentNode> stubLibraries = EnvironmentSearch
                .searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryStubNode());
        boolean onWhiteBoxMode = Environment.getInstance().isOnWhiteBoxMode();

        try {
            String cloneFilePath = getClonedFilePath(sourceCode.getAbsolutePath());

            ProjectClone clone = new ProjectClone();

            clone.libraries = libraries;
            clone.whiteBoxEnable = (uutUnits.contains(sourceCode) || sbfUnits.contains(sourceCode)) && onWhiteBoxMode;
            clone.canStub = stubUnits.contains(sourceCode) || sbfUnits.contains(sourceCode);
            clone.stubLibraries = stubLibraries;

            Utils.copy(sourceCode.getFile(), new File(cloneFilePath));

            String newContent = clone.generateFileContent(sourceCode);
            logger.debug("Generate instrument file of " + sourceCode.getName() + " successfully");
            Utils.writeContentToFile(newContent, cloneFilePath);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove all cloned file with extension ".akaignore" in project directories.
     */
    public static void removeClonedFile() {
        ProjectNode projectRoot = Environment.getInstance().getProjectNode();
        List<INode> sources = Search.searchNodes(projectRoot, new SourcecodeFileNodeCondition());

        for (INode sourceCode : sources) {
            String clonedFilePath = getClonedFilePath(sourceCode.getAbsolutePath());
            File clonedFile = new File(clonedFilePath);
            if (clonedFile.exists())
                Utils.deleteFileOrFolder(clonedFile);
        }
    }

    /**
     * remove all mark function in source code
     */
    public static String simplify(String source) {
        String simplify = source.replaceAll("\\b" + DriverConstant.MARK + "\\(\"[^\"]+\"\\);", SpecialCharacter.EMPTY);
        simplify = simplify.replaceAll("\\b" + DriverConstant.MARK + "\\(\"[^\"]+\"\\)\\s*&&\\s*",
                SpecialCharacter.EMPTY);
        simplify = simplify.replaceAll("#include \".+probepoints", "<~ \"");
        simplify = simplify.replace(SourceConstant.INCREASE_FCALLS, SpecialCharacter.EMPTY);
        return simplify;
    }

    public static int getStartLineNumber(ICommonFunctionNode functionNode) {
        INode sourceNode = Utils.getSourcecodeFile(functionNode);

        String clonedFilePath = getClonedFilePath(sourceNode.getAbsolutePath());

        String clonedFile = Utils.readFileContent(clonedFilePath);

        if (functionNode instanceof IFunctionNode) {
            IASTFunctionDefinition functionDefinition = ((IFunctionNode) functionNode).getAST();

            String rawSource = functionDefinition.getRawSignature();

            int openIndex = rawSource.indexOf('{') + 1;

            String declaration = rawSource.substring(0, openIndex)
                    .replaceAll(MAIN_REGEX, MAIN_REFACTOR_NAME)
                    .replaceAll("\\s+\\{", "{");

            int offsetInClonedFile = clonedFile.indexOf(declaration);
            if (offsetInClonedFile < 0) {
                declaration = rawSource.substring(0, openIndex)
                        .replaceAll(MAIN_REGEX, MAIN_REFACTOR_NAME);
                offsetInClonedFile = clonedFile.indexOf(declaration);
            }
            if (offsetInClonedFile < 0)
                return 0;

            return (int) clonedFile
                    .substring(0, offsetInClonedFile)
                    .chars()
                    .filter(c -> c == '\n')
                    .count();
        } else if (functionNode instanceof MacroFunctionNode) {
            IASTNode ast = ((MacroFunctionNode) functionNode).getAST();
            return ast.getFileLocation().getStartingLineNumber();

        } else if (functionNode instanceof DefinitionFunctionNode) {
            IASTNode ast = ((DefinitionFunctionNode) functionNode).getAST();
            return ast.getFileLocation().getStartingLineNumber();

        }

        return 0;
    }

    /**
     * Ex: Utils.cpp -> Utils.akaignore.cpp
     *
     * @param origin file path
     * @return cloned file path
     */
    public static String getClonedFilePath(String origin) {
        Path rootPath = Paths.get(Environment.getInstance().getProjectNode().getAbsolutePath());
        Path originPath = Paths.get(PathUtils.toAbsolute(origin));
        String relativePath = rootPath.relativize(originPath).toString();
        int lastDotPos = relativePath.lastIndexOf(SpecialCharacter.DOT);
        String clonedName = relativePath.substring(0, lastDotPos) + CLONED_FILE_EXTENSION
                + relativePath.substring(lastDotPos);
        return new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + clonedName;
    }

    public static String getOriginFilePath(String clone) {
        String originPath = clone;
        String relativePath = PathUtils.toRelative(clone);
        relativePath = Utils.normalizePath(relativePath);
        String subPath = "." + File.separator + WORKING_SPACE_NAME
                + File.separator + Environment.getInstance().getName()
                + File.separator +
                WorkspaceConfig.INSTRUMENT_SOURCE_FOLDER_NAME;
        subPath = Utils.normalizePath(subPath);
        if (relativePath.startsWith(subPath)) {
            subPath = relativePath.substring(subPath.length());
            originPath = Environment.getInstance().getProjectNode().getAbsolutePath() + subPath;
            originPath = Utils.normalizePath(originPath);
        }
        return originPath
                .replaceFirst("\\Q" + Environment.getInstance().getName() + "\\E\\.", SpecialCharacter.EMPTY)
                .replace(CLONED_FILE_EXTENSION, SpecialCharacter.EMPTY);
    }

    /**
     * Ex: int x;
     * To #ifdef AKA_GLOBAL_X
     * #define AKA_GLOBAL_X
     * int x;
     * #endif
     *
     * @param name    define name
     * @param content needed to be guard
     * @return new source code
     */
    public static String wrapInIncludeGuard(String name, String content) {
        return String.format("/** Guard statement to avoid multiple declaration */\n" +
                "#ifndef %s\n#define %s\n%s\n#endif\n", name, name, content);
    }

    public static List<String> getLibraries() {
        if (sLibraries == null) {
            sLibraries = new ArrayList<>();

            List<IEnvironmentNode> libs = EnvironmentSearch
                    .searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryIncludeDirNode());
            libs.forEach(lib -> {
                String path = ((EnviroLibraryIncludeDirNode) lib).getLibraryIncludeDir();
                File[] files = new File(path).listFiles();
                if (files != null) {
                    for (File file : files) {
                        sLibraries.add(file.getAbsolutePath());
                    }
                }
            });
            // List<IEnvironmentNode> types =
            // EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(),
            // new EnviroTypeHandledSourceDirNode());
            // types.forEach(lib -> {
            // String path = ((EnviroTypeHandledSourceDirNode)
            // lib).getTypeHandledSourceDir();
            // File[] files = new File(path).listFiles();
            // if (files != null) {
            // for (File file : files) {
            // sLibraries.add(file.getAbsolutePath());
            // }
            // }
            // });
        }

        return sLibraries;
    }

    public static String generateCallingMark(String content) {
        content = PathUtils.toRelative(content);
        content = Utils.doubleNormalizePath(content);
        return String.format(DriverConstant.MARK + "(\"Calling: %s\");" + SourceConstant.INCREASE_FCALLS, content);
    }

    /**
     * Generate clone file content
     *
     * @param sourceCode origin source code file content
     * @return cloned file content
     */
    protected String generateFileContent(INode sourceCode) throws InterruptedException {
        String filePath = sourceCode.getAbsolutePath();
        String oldContent = Utils.readFileContent(filePath);

        List<SearchCondition> conditions = new ArrayList<>();
        conditions.add(new IncludeHeaderNodeCondition());
        conditions.add(new GlobalVariableNodeCondition());
        conditions.add(new AbstractFunctionNodeCondition());
        conditions.add(new MacroFunctionNodeCondition());
        // conditions.add(new DefinitionFunctionNodeCondition());

        List<INode> redefines = Search.searchNodes(sourceCode, conditions);
        redefines.removeIf(this::isIgnore);

        int size = redefines.size();

        ExecutorService es = Executors.newFixedThreadPool(5);
        AtomicInteger counter = new AtomicInteger();
        List<Callable<Void>> tasks = new ArrayList<>();
        for (INode child : redefines) {
            Callable<Void> c = () -> {
                // int index = redefines.indexOf(child);

                logger.debug(String.format("[%d/%d] Clone & instrument %s", counter.incrementAndGet(), size,
                        child.getName()));

                if (child instanceof IncludeHeaderNode) {
                    refactorInclude((IncludeHeaderNode) child, sourceCode);

                } else if (child instanceof ExternalVariableNode)
                    guardGlobalDeclaration(child);

                else if (child instanceof ICommonFunctionNode) {
                    ICommonFunctionNode function = (ICommonFunctionNode) child;
                    refactorFunction(function);
                }

                return null;
            };
            tasks.add(c);
        }

        es.invokeAll(tasks);

        oldContent = addMissingLibraryToStub(oldContent);

        for (Map.Entry<String, String> entry : refactors.entrySet()) {
            String prev = entry.getKey();
            String newC = entry.getValue();
            logger.debug("New content: " + newC);
            // String regex = "\\W" + Pattern.quote(prev);
            // oldContent = oldContent.replaceAll(regex, newC);
            oldContent = oldContent.replace(prev, newC);
        }

        for (String globalDeclaration : globalDeclarations)
            oldContent = oldContent.replace("#endif\n\n" + globalDeclaration, "#endif");

        if (whiteBoxEnable)
            oldContent = refactorWhiteBox(oldContent);

        oldContent = oldContent
                // .replaceAll(MAIN_REGEX, MAIN_REFACTOR_NAME)
                .replaceAll("\\bconstexpr\\b", SpecialCharacter.EMPTY);

        String additionDeclaration = "extern int strcmp(const char * str1, const char * str2);\n" +
                "extern int AKA_mark(char* str);\n" +
                "extern void AKA_assert(char* actualName, int actualVal, char* expectedName, int expectedVal);\n" +
                "extern int AKA_assert_double(char* actualName, double actualVal, char* expectedName, double expectedVal);\n"
                +
                "extern int AKA_assert_ptr(char* actualName, void* actualVal, char* expectedName, void* expectedVal);\n"
                +
                "extern int AKA_fCall;\n" +
                "extern char* AKA_test_case_name;\n\n\n\n";
        if (!filePath.endsWith(IProjectLoader.C_FILE_SYMBOL)) {
            if (!Environment.getInstance().isC()) {
                additionDeclaration = "#include <string>\n" +
                        "extern int strcmp(const char * str1, const char * str2);\n" +
                        "extern int AKA_mark(std::string append);\n" +
                        "extern void AKA_assert(std::string actualName, int actualVal, std::string expectedName, int expectedVal);\n"
                        +
                        "extern int AKA_assert_double(std::string actualName, double actualVal, std::string expectedName, double expectedVal);\n"
                        +
                        "extern int AKA_assert_ptr(std::string actualName, void* actualVal, std::string expectedName, void* expectedVal);\n"
                        +
                        "extern int AKA_fCall;\n" +
                        "extern char* AKA_test_case_name;\n\n\n\n";
            }
        }

        oldContent = additionDeclaration + oldContent;

        String defineSourceCodeName = SourceConstant.SRC_PREFIX + sourceCode.getAbsolutePath().toUpperCase()
                .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);

        return wrapInIncludeGuard(defineSourceCodeName, oldContent);
    }

    /**
     * Add missing library header in file content if it's using a function from a
     * missing library
     *
     * @param oldContent
     * @return
     */
    private String addMissingLibraryToStub(String oldContent) {
        for (IEnvironmentNode stubLibrary : stubLibraries) {
            Map<String, String> libs = ((EnviroLibraryStubNode) stubLibrary).getLibraries();
            for (Map.Entry<String, String> entry : libs.entrySet()) {
                if (oldContent.contains(entry.getKey())) {
                    String inclStr = "#include " + SpecialCharacter.DOUBLE_QUOTES
                            + SystemLibrary.getLibrariesDirectory() + entry.getValue()
                            + SystemLibrary.LIBRARY_EXTENSION + SpecialCharacter.DOUBLE_QUOTES;
                    String inclGuardName = SourceConstant.INCLUDE_PREFIX
                            + entry.getValue().replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE)
                            .toUpperCase();
                    if (!oldContent.contains(entry.getValue())) {
                        oldContent = wrapInIncludeGuard(inclGuardName, inclStr) + "\n" + oldContent;
                    }
                }
            }
        }
        return oldContent;
    }

    private boolean isIgnore(INode node) {
        if (node instanceof ICommonFunctionNode && node.getParent() instanceof ICommonFunctionNode)
            return true;

        if (node instanceof AbstractFunctionNode) {
            IASTStatement body = ((AbstractFunctionNode) node).getAST().getBody();
            return body == null;
        }

        return false;
    }

    protected String getCorrespondingClonePath(String path) {
        return getClonedFilePath(path);
    }

    /**
     * Refactor include to clone akaignore file and stub libraries source files.
     *
     * @param includeHeader node
     * @param sourceCode    node
     * @return new source code file content
     */
    private void refactorInclude(IncludeHeaderNode includeHeader, INode sourceCode) {

        // Guard all include statement in source code
        String guardIncludeStm = guardIncludeHeader(includeHeader);

        // find header node from include header statement
        List<INode> headerNodes = new IncludeHeaderDependencyGeneration().findIncludeNodes(includeHeader, sourceCode);
        headerNodes.removeIf(headerNode -> libraries.contains(headerNode.getAbsolutePath()));

        // get prev include statement
        String oldIncludeStatement = includeHeader.getAST().getRawSignature();

        // Modify include dependency to clone akaignore file
        if (!headerNodes.isEmpty() && !includeHeader.getAST().isSystemInclude()) {
            INode headerNode = headerNodes.get(0);
            IncludeHeaderDependency d = new IncludeHeaderDependency(sourceCode, headerNode);
            if (sourceCode.getDependencies().contains(d)) {
                String clonedHeaderPath = getCorrespondingClonePath(headerNode.getAbsolutePath());
                String clonedFilePath = getCorrespondingClonePath(sourceCode.getAbsolutePath());
                Path parentDirPath = Paths.get(clonedFilePath).getParent();
                Path clonedPath = Paths.get(clonedHeaderPath);
                Path relativePath = parentDirPath.relativize(clonedPath);
                String newIncludeStatement = oldIncludeStatement.replace(includeHeader.getNewType(),
                        relativePath.toString());
                guardIncludeStm = guardIncludeStm.replace(oldIncludeStatement, newIncludeStatement);
            }
        }
        // STUB LIBRARY case
        else {
            String library = includeHeader.getNewType();
            boolean isSystem = includeHeader.getAST().isSystemInclude();
            boolean isStub = false;

            for (IEnvironmentNode stubLibrary : stubLibraries) {
                if (((EnviroLibraryStubNode) stubLibrary).getLibraries().containsValue(library)) {
                    String clonedFilePath = SystemLibrary.getLibrariesDirectory() + library
                            + SystemLibrary.LIBRARY_EXTENSION;
                    String newIncludeStatement = oldIncludeStatement.replace(includeHeader.getNewType(),
                            clonedFilePath);
                    guardIncludeStm = guardIncludeStm.replace(oldIncludeStatement, newIncludeStatement)
                            .replace(TemplateUtils.OPEN_TEMPLATE_ARG, SpecialCharacter.DOUBLE_QUOTES)
                            .replace(TemplateUtils.CLOSE_TEMPLATE_ARG, SpecialCharacter.DOUBLE_QUOTES);
                    isStub = true;
                    break;
                }
            }

            if (!isStub && isSystem) {
                guardIncludeStm = oldIncludeStatement;
            }
        }

        refactors.put(oldIncludeStatement, guardIncludeStm);
    }

    /**
     * Refactor function content
     *
     * @param function node
     * @return new source code file content
     */
    protected void refactorFunction(ICommonFunctionNode function) {
        if (function instanceof AbstractFunctionNode || function instanceof MacroFunctionNode) {
            String oldFunctionCode;

            if (function instanceof AbstractFunctionNode) {
                IASTFunctionDefinition functionAST = ((AbstractFunctionNode) function).getAST();
                oldFunctionCode = functionAST.getRawSignature();
            } else {
                oldFunctionCode = ((MacroFunctionNode) function).getAST().getRawSignature();
            }

            // generate instrumented function content
            String newFunctionCode;
            if (function instanceof MacroFunctionNode)
                newFunctionCode = generateInstrumentedFunction((MacroFunctionNode) function);
            else
                newFunctionCode = generateInstrumentedFunction((IFunctionNode) function);

            // include stub code in function scope
            if (function instanceof FunctionNode && canStub) {
                newFunctionCode = includeStubFile(newFunctionCode, (FunctionNode) function);
                StubManager.initializeStubCode((FunctionNode) function);
            }

            // modify all function call expression to stub libraries
            for (IEnvironmentNode stubLibrary : stubLibraries) {
                if (stubLibrary instanceof EnviroLibraryStubNode) {
                    Map<String, String> libraries = ((EnviroLibraryStubNode) stubLibrary).getLibraries();

                    for (String libraryName : libraries.keySet()) {
                        if (!libraryName.matches("[a-zA-Z0-9_]+") && libraryName.indexOf('(') != -1) {
                            libraryName = libraryName.substring(0, libraryName.indexOf('(')).trim();
                            libraryName = libraryName.substring(libraryName.lastIndexOf(' ') + 1);
                        }
                        if (SystemLibrary.isUseLibrary(libraryName, function)) {
                            String newLibraryCall = SourceConstant.STUB_PREFIX + libraryName;

                            newFunctionCode = newFunctionCode
                                    .replaceAll(
                                            "\\b" + Pattern.quote(VariableTypeUtils.STD_SCOPE + libraryName) + "\\b",
                                            newLibraryCall);

                            newFunctionCode = newFunctionCode
                                    .replaceAll("\\b" + Pattern.quote(libraryName) + "\\b", newLibraryCall);

                            // update function call dependency
                            if (newFunctionCode.contains(newLibraryCall)) {
                                List<ICommonFunctionNode> possibles = Search.searchNodes(
                                        Environment.getInstance().getSystemLibraryRoot(),
                                        new FunctionNodeCondition());

                                possibles.removeIf(f -> !f.getSimpleName().equals(newLibraryCall));

                                if (!possibles.isEmpty()) {
                                    ICommonFunctionNode libraryFunction = possibles.get(0);
                                    Dependency d = new FunctionCallDependency(function, libraryFunction);

                                    if (!function.getDependencies().contains(d)
                                            && !libraryFunction.getDependencies().contains(d)) {
                                        function.getDependencies().add(d);
                                        libraryFunction.getDependencies().add(d);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // change main function to AKA_MAIN
            // if (function.getSingleSimpleName().equals("main")) {
            // newFunctionCode = refactorMain(newFunctionCode);
            // }

            // change constexpr function to normal function
            newFunctionCode = newFunctionCode.replaceFirst("constexpr\\s+", " ");

            refactors.put(oldFunctionCode, newFunctionCode);
        }
    }

    // /**
    // * Ex: int main() -> int AKA_MAIN()
    // *
    // * @param oldMain function node
    // * @return new source code file
    // */
    // private String refactorMain(String oldMain) {
    // return oldMain.replaceAll(MAIN_REGEX, MAIN_REFACTOR_NAME);
    // }

    /**
     * Ex: int foo() {
     * int x, y;
     * ...
     * return x + y;
     * }
     * To int foo() {
     * #include "Utils.foo.23.324.stub"
     * int x, y;
     * ...
     * return x + y;
     * }
     *
     * @param oldContent of function source code
     * @param function   needed to insert stub code
     * @return new function source code
     */
    protected String includeStubFile(String oldContent, FunctionNode function) {
        String markFunctionStm;
        if (this instanceof LcovProjectClone) {
            markFunctionStm = SourceConstant.INCREASE_FCALLS;
        } else {
            markFunctionStm = generateCallingMark(function.getAbsolutePath());
        }
        int bodyBeginPos = oldContent.indexOf(SpecialCharacter.OPEN_BRACE) + markFunctionStm.length() + 1;

        String stubDirectory = new WorkspaceConfig().fromJson().getStubCodeDirectory();
        String stubFilePath = stubDirectory + (stubDirectory.endsWith(File.separator) ? "" : File.separator)
                + StubManager.getStubCodeFileName(function);

        String originPath = Utils.getSourcecodeFile(function).getAbsolutePath();
        String clonePath = ProjectClone.getClonedFilePath(originPath);
        Path parentDirPath = Paths.get(clonePath).getParent();
        Path stubPath = Paths.get(stubFilePath);

        String relativePath = parentDirPath.relativize(stubPath).toString();

        StubManager.addStubFile(function.getAbsolutePath(), stubFilePath);

        return oldContent.substring(0, bodyBeginPos)
                + String.format("\n\t/** Include stub source code */\n\t#include \"%s\"\n", relativePath)
                + oldContent.substring(bodyBeginPos);
    }

    /**
     * Ex: #include "class.h"
     * To #ifdef AKA_INCLUDE_CLASS_H
     * #define AKA_INCLUDE_CLASS_H
     * #include "class.h"
     * #endif
     *
     * @param child corresponding external variable node
     * @return new guarded include statement
     */
    private String guardIncludeHeader(INode child) {
        if (child instanceof IncludeHeaderNode) {
            String oldIncludeHeader = ((IncludeHeaderNode) child).getAST().getRawSignature();

            String header = child.getName().replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE).toUpperCase();

            return wrapInIncludeGuard(SourceConstant.INCLUDE_PREFIX + header, oldIncludeHeader);
        }

        return null;
    }

    /**
     * Ex: int x;
     * To #ifdef AKA_GLOBAL_X
     * #define AKA_GLOBAL_X
     * int x;
     * #endif
     *
     * @param child corresponding external variable node
     * @return new declaration of external variable node
     */
    private void guardGlobalDeclaration(INode child) {
        IASTNodeLocation[] tempAstLocations = ((ExternalVariableNode) child).getASTType().getNodeLocations();
        if (tempAstLocations.length > 0) {
            IASTNodeLocation astNodeLocation = tempAstLocations[0];
            if (astNodeLocation instanceof IASTCopyLocation) {
                IASTNode declaration = ((IASTCopyLocation) astNodeLocation).getOriginalNode().getParent();
                if (declaration instanceof IASTDeclaration) {
                    String originDeclaration = declaration.getRawSignature();

                    if (!globalDeclarations.contains(originDeclaration))
                        globalDeclarations.add(originDeclaration);

                    String oldDeclaration = ((ExternalVariableNode) child).getASTType().getRawSignature() + " "
                            + ((ExternalVariableNode) child).getASTDecName().getRawSignature() + ";";

                    String header = child.getAbsolutePath();

                    if (header.startsWith(File.separator))
                        header = header.substring(1);

                    header = header.replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE).toUpperCase();

                    String newDeclaration = wrapInIncludeGuard(SourceConstant.GLOBAL_PREFIX + header, oldDeclaration);

                    refactors.put(originDeclaration, newDeclaration + "\n" + originDeclaration);
                }
            }
        }
    }

    private String generateInstrumentedFunction(MacroFunctionNode functionNode) {
        final String success = String.format("/** Instrumented function %s */\n", functionNode.getName());
        final String fail = String.format("/** Can not instrument function %s */\n", functionNode.getName());

        String instrumentedSourceCode;

        FunctionInstrumentationForMacro fnInstrumentation = new FunctionInstrumentationForMacro(functionNode.getAST());

        fnInstrumentation.setFunctionPath(functionNode.getAbsolutePath());
        fnInstrumentation.setMacroFunctionNode(functionNode);
        String instrument = fnInstrumentation.generateInstrumentedFunction(); // just for release
        if (instrument == null || instrument.length() == 0) {
            // can not instrument
            instrumentedSourceCode = fail + functionNode.getAST().getRawSignature();
        } else {
            instrumentedSourceCode = success + instrument;
        }

        return instrumentedSourceCode;
    }

    /**
     * Change all private and protected labels in source code to public
     */
    private String refactorWhiteBox(String oldContent) {
        oldContent = oldContent.replaceAll("\\bprivate\\b", "public");
        oldContent = oldContent.replaceAll("\\bprotected\\b", "public");
        oldContent = oldContent.replaceAll("\\bstatic \\b", SpecialCharacter.EMPTY);

        return oldContent;
    }

    /**
     * Perform on instrumentation on the original function
     */
    private String generateInstrumentedFunction(IFunctionNode functionNode) {
        final String success = String.format("/** Instrumented function %s */\n", functionNode.getName());
        final String fail = String.format("/** Can not instrument function %s */\n", functionNode.getName());

        String instrumentedSourceCode;

        IASTFunctionDefinition astInstrumentedFunction = functionNode.getAST();

        AbstractFunctionInstrumentation fnInstrumentation = new FunctionInstrumentationForAllCoverages(
                astInstrumentedFunction, functionNode);

        fnInstrumentation.setFunctionPath(functionNode.getAbsolutePath());

        String instrument = fnInstrumentation.generateInstrumentedFunction();
        if (instrument == null || instrument.length() == 0) {
            // can not instrument
            instrumentedSourceCode = fail + functionNode.getAST().getRawSignature();
        } else {
            instrumentedSourceCode = success + instrument;
            int bodyIdx = instrumentedSourceCode.indexOf(SpecialCharacter.OPEN_BRACE) + 1;

            instrumentedSourceCode = instrumentedSourceCode.substring(0, bodyIdx)
                    + generateCallingMark(functionNode.getAbsolutePath()) // insert mark start function
                    + instrumentedSourceCode.substring(bodyIdx);
        }

        return instrumentedSourceCode;
    }

    public static void compileIgnoreFiles(String directory, ITestCase testCase) {
        CommandConfig rootCommand = new CommandConfig().fromJson();
        for (Map.Entry<String, String> entry : rootCommand.getCompilationCommands().entrySet()) {
            String originPath = entry.getKey();
            String script = entry.getValue();
            String newPath = getClonedFilePath(originPath);
            newPath = Utils.doubleNormalizePath(newPath);
            script = script.replaceFirst("\"[^\"]+\"", "\"" + newPath + "\"");
            script += SpecialCharacter.SPACE + testCase.generateDefinitionCompileCmd();
            script += " -DASSERT_ENABLE";

            Compiler compiler = Environment.getInstance().getCompiler();

            try {
                String[] command = CompilerUtils.prepareForTerminal(compiler, script);
                String message = new Terminal(command, directory).get();
                logger.debug("Compilation of " + newPath + ": " + script);
            } catch (Exception ex) {
                logger.error("Error " + ex.getMessage() + " when compiling " + newPath);
            }

        }
    }

    public static void main(String[] args) {
        String script = "gcc -c \"dfdsfds.c\" -o\"dfdsfds.out\"";
        String newPath = "dfdsfds.akaignore.c";
        script = script.replaceFirst("\"[^\"]+\"", "\"" + newPath + "\"");
        System.out.println(script);
    }

    public static class CloneThread extends AbstractAkaTask<Void> {
        @Override
        protected Void call() {
            // stub system libraries
            SystemLibrary.generateStubCode();
            SystemLibraryRoot libraryRoot = SystemLibrary.parseFromFile();
            Environment.getInstance().setSystemLibraryRoot(libraryRoot);

            INode root = EnvironmentUserCode.getInstance().parseTree();
            Environment.getInstance().setUserCodeRoot(root);

            // clone tested source code
            ProjectClone.cloneEnvironment();
            LcovProjectClone.cloneLcovEnvironment();
            StubManager.exportListToFile();

            return null;
        }
    }
}
