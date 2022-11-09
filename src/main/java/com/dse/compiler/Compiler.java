package com.dse.compiler;

import com.dse.compiler.message.CompileMessage;
import com.dse.compiler.message.ICompileMessage;
import com.dse.config.AkaConfig;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.INode;
import com.dse.project_init.ProjectClone;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Compiler implements ICompiler {
    private final static AkaLogger logger = AkaLogger.get(Compiler.class);

    private String compileCommand = "compile";
    private String preprocessCommand = "pre-process";
    private String linkCommand = "link";
    private String debugCommand = "debug";

    private String includeFlag = "-I";
    private String defineFlag = "-D";
    private String outputFlag = "-o";
    private String debugFlag = "-ggdb";

    private String outputExtension = ".out";

    private List<String> includePaths = new ArrayList<>();
    private List<String> defines = new ArrayList<>();

    private String name;

    /*
     * TODO: other option
     */

    public Compiler() {

    }

    public Compiler(Class<?> c) throws IllegalAccessException, NoSuchFieldException {
        name = c.getField("NAME").get(null).toString();

        compileCommand = c.getField("COMPILE_CMD").get(null).toString();
        preprocessCommand = c.getField("PRE_PRECESS_CMD").get(null).toString();
        linkCommand = c.getField("LINK_CMD").get(null).toString();
        debugCommand = c.getField("DEBUG_CMD").get(null).toString();

        includeFlag = c.getField("INCLUDE_FLAG").get(null).toString();
        defineFlag = c.getField("DEFINE_FLAG").get(null).toString();
        outputFlag = c.getField("OUTPUT_FLAG").get(null).toString();
        debugFlag = c.getField("DEBUG_FLAG").get(null).toString();

        outputExtension = c.getField("OUTPUT_EXTENSION").get(null).toString();
    }

    @Override
    public ICompileMessage compile(INode file) {
        String filepath = file.getAbsolutePath();
        filepath = PathUtils.toRelative(filepath);

        String outPath = ProjectClone.getClonedFilePath(file.getAbsolutePath()) + outputExtension;
        return compile(filepath, outPath);
    }

    @Override
    public synchronized ICompileMessage compile(String filePath, String outPath) {
        new File(outPath).getParentFile().mkdirs();
        String script = generateCompileCommand(filePath, outPath);
        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();

        ICompileMessage compileMessage = null;

        try {
            String[] command = CompilerUtils.prepareForTerminal(this, script);
            String message = new Terminal(command, directory).get();
            compileMessage = new CompileMessage(message, filePath);
            compileMessage.setCompilationCommand(script);

            logger.debug("Compilation of " + filePath + ": " + script);
        } catch (Exception ex) {
            logger.error("Error " + ex.getMessage() + " when compiling " + filePath);
        }

        return compileMessage;
    }

    @Override
    public synchronized ICompileMessage link(String executableFilePath, String... outputPaths) {
//        executableFilePath = PathUtils.toRelative(executableFilePath);
//        for (int i = 0; i < outputPaths.length; i++) {
//            outputPaths[i] = PathUtils.toRelative(outputPaths[i]);
//        }
//        String script = generateLinkCommand(executableFilePath, outputPaths);
        String script = CompilerUtils.generateLinkCommand(linkCommand, outputFlag, executableFilePath, outputPaths);
        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();

        ICompileMessage compileMessage;

        try {
            String[] command = CompilerUtils.prepareForTerminal(this, script);
            String message = new Terminal(command, directory).get();
            compileMessage = new CompileMessage(message, executableFilePath);
            compileMessage.setLinkingCommand(script);
        } catch (Exception ex) {
            logger.error("Can not linkage in " + executableFilePath + " with command " + script);
            compileMessage = null;
        }

        return compileMessage;
    }

    @Override
    public String generateCompileCommand(String filePath, String outfilePath) {
//        String outfilePath = CompilerUtils.getOutfilePath(filePath, outputExtension);

        StringBuilder builder = new StringBuilder();
        builder.append(compileCommand)
                .append(SpecialCharacter.SPACE)
                .append("\"" + filePath + "\"")
                .append(SpecialCharacter.SPACE);

        if (includePaths != null && includePaths.size() != 0) {
            for (String path : includePaths) {
                builder.append(includeFlag)
                        .append("\"" + path + "\"")
                        .append(SpecialCharacter.SPACE);
            }
        }

        List<String> userCodes = EnvironmentUserCode.getInstance().getAllFilePath();
        for (String userCode : userCodes) {
            builder.append(INCLUDE_FILE_FLAG)
                    .append("\"").append(userCode).append("\"")
                    .append(SpecialCharacter.SPACE);
        }

        if (defines != null && defines.size() != 0) {
            for (String variable : defines) {
                builder.append(defineFlag)
                        .append(variable)
                        .append(SpecialCharacter.SPACE);
            }
        }

        builder.append(outputFlag)
                .append("\"" + outfilePath + "\"");

        return builder.toString();
    }

    @Override
    public String generatePreprocessCommand(String filePath) {
        String fileName = CompilerUtils.getFileName(filePath);

        StringBuilder builder = new StringBuilder();
        builder.append(preprocessCommand)
                .append(SpecialCharacter.SPACE)
                .append("\"" + filePath + "\"")
                .append(SpecialCharacter.SPACE);

        if (includePaths != null && includePaths.size() != 0) {
            for (String path : includePaths) {
                builder.append(includeFlag)
                        .append("\"" + path + "\"")
                        .append(SpecialCharacter.SPACE);
            }
        }

        if (defines != null && defines.size() != 0) {
            for (String variable : defines) {
                builder.append(defineFlag)
                        .append(variable)
                        .append(SpecialCharacter.SPACE);
            }
        }

        return builder.toString();
    }

    @Override
    public String generateLinkCommand(String executableFilePath, String... outputPaths) {
        if (outputPaths == null || outputPaths.length == 0)
            return null;

        StringBuilder builder = new StringBuilder();

        builder.append(linkCommand)
                .append(SpecialCharacter.SPACE);

        for (String output : outputPaths)
            builder.append("\"" + output + "\"")
                    .append(SpecialCharacter.SPACE);

        builder.append(outputFlag)
                .append("\"" + executableFilePath + "\"");

        return builder.toString();
    }

    @Override
    public String preprocess(String inPath, String outPath) {
        String command = generatePreprocessCommand(inPath) + " " + outputFlag + "\"" + outPath + "\"";

        try {
            String[] script = CompilerUtils.prepareForTerminal(this, command);

            String stderr = new Terminal(script).getStderr();

            File outFile = new File(outPath);

            if (outFile.exists())
                return Utils.readFileContent(outPath);
            else
                return stderr;

        } catch (Exception ex) {
            logger.error("Error " + ex.getMessage() + " when preprocess " + inPath);
        }

        return null;
    }

    @Override
    public String getCompileCommand() {
        return compileCommand;
    }

    @Override
    public String getPreprocessCommand() {
        return preprocessCommand;
    }

    @Override
    public String getLinkCommand() {
        return linkCommand;
    }

    @Override
    public String getDebugCommand() {
        return debugCommand;
    }

    @Override
    public List<String> getIncludePaths() {
        return includePaths;
    }

    @Override
    public List<String> getDefines() {
        return defines;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDefineFlag() {
        return defineFlag;
    }

    @Override
    public String getIncludeFlag() {
        return includeFlag;
    }

    @Override
    public String getOutputExtension() {
        return outputExtension;
    }

    @Override
    public String getOutputFlag() {
        return outputFlag;
    }

    @Override
    public String getDebugFlag() {
        return debugFlag;
    }

    public void setCompileCommand(String compileCommand) {
        this.compileCommand = compileCommand;
    }

    public void setDebugCommand(String debugCommand) {
        this.debugCommand = debugCommand;
    }

    public void setLinkCommand(String linkCommand) {
        this.linkCommand = linkCommand;
    }

    public void setPreprocessCommand(String preprocessComand) {
        this.preprocessCommand = preprocessComand;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public void setDefines(List<String> defines) {
        this.defines = defines;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefineFlag(String defineFlag) {
        this.defineFlag = defineFlag;
    }

    public void setIncludeFlag(String includeFlag) {
        this.includeFlag = includeFlag;
    }

    public void setOutputExtension(String outputExtension) {
        this.outputExtension = outputExtension;
    }

    public void setOutputFlag(String outputFlag) {
        this.outputFlag = outputFlag;
    }

    public void setDebugFlag(String debugFlag) {
        this.debugFlag = debugFlag;
    }

    public boolean isGccCommand(){
        return compileCommand.contains("gcc");
    }

    public boolean isGPlusPlusCommand(){
        return compileCommand.contains("g++");
    }
}
