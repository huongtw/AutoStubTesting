package com.dse.environment;

import com.dse.environment.object.*;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class EnvironmentAnalyzer implements ICommandList {
    final static AkaLogger logger = AkaLogger.get(EnvironmentAnalyzer.class);
    private File environmentFile = null;
    private IEnvironmentNode root = new EnvironmentRootNode();

    public static void main(String[] args) {
        // create tree from script
        EnvironmentAnalyzer analyzer = new EnvironmentAnalyzer();
        analyzer.analyze(new File("datatest/duc-anh/evironment_sample_01/script2"));
        IEnvironmentNode root = analyzer.getRoot();

        // display tree
        ToString converter = new ToString();
        String output = converter.convert(root);
        logger.debug(output);

        // export to file
        String export = analyzer.getRoot().exportToFile();
        logger.debug("Export = " + export);
    }

    public EnvironmentAnalyzer() {
    }

    public void analyze(File environmentFile) {
        this.environmentFile = environmentFile;

        if (root instanceof EnvironmentRootNode)
            ((EnvironmentRootNode) root).setEnvironmentScriptPath(this.environmentFile.getAbsolutePath());

        String currentContent = Utils.readFileContent(this.environmentFile);
        String[] lines = currentContent.split("\n");
        int startLine = 0;
        int numOfLines = lines.length;
        int currentLineIndex = startLine;

        while (currentLineIndex < numOfLines) {
            String currentLineCommand = lines[currentLineIndex].trim();

            if (currentLineCommand.length() == 0){
                // there is no content in this line
                currentLineIndex++;
            } else if (currentLineCommand.equals(ENVIRO_NEW)) {
                initializeNewEnvironment(root);
                currentLineIndex++;

            } else if (currentLineCommand.equals(ENVIRO_END)) {
                closeEnvironment(root);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_NAME)) {
                setNameOfEnvironment(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_TESTING_METHOD)) {
                setTestingMethod(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_COMPILER_NEW)) {
                int endCompilerLineIndex = currentLineIndex;

                while (!lines[endCompilerLineIndex].trim().startsWith(ENVIRO_COMPILER_END))
                    endCompilerLineIndex++;

                String[] compileLines = Arrays.copyOfRange(lines, currentLineIndex, endCompilerLineIndex + 1);

                setCompiler(root, compileLines);

                currentLineIndex += compileLines.length;

            } else if (currentLineCommand.startsWith(ENVIRO_UUT)) {
                createUUT(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_STUB_BY_FUNCTION)
                    || currentLineCommand.startsWith(ENVIRO_SBF)) {
                createStubByFunction(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_LIBRARY_STUBS)) {
                createLibraryStubs(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_DEFINED_VARIABLE)) {
                createDefinedVariable(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_SEARCH_LIST)) {
                createSearchList(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_LIBRARY_INCLUDE_DIR)) {
                createLibraryIncludeDir(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_TYPE_HANDLED_SOURCE_DIR)) {
                createTypeHandledSourceDir(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_TYPE_HANDLED_DIRS_ALLOWED)) {
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_WHITE_BOX)) {
                createWhiteboxSetting(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_COVERAGE_TYPE)) {
                createCoverageType(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_STUB)) {
                createStub(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_DONT_STUB)) {
                createNotStub(root, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_STUB_DEPEND_USER_CODE_FILE)) {
                List<String> block = root.getBlockOfTag(ENVIRO_END_STUB_DEPEND_USER_CODE_FILE, currentLineIndex, lines);
                createStubDependUserCodeFile(root, block);
                currentLineIndex += block.size() + 1;

            } else if (currentLineCommand.startsWith(ENVIRO_STUB_USER_CODE_FILE)) {
                List<String> block = root.getBlockOfTag(ENVIRO_END_STUB_USER_CODE_FILE, currentLineIndex, lines);
                createStubUserCode(root, block);
                currentLineIndex += block.size() + 1;

            } else if (currentLineCommand.startsWith(ENVIRO_USER_CODE_DEPENDENCIES)) {
                List<String> block = root.getBlockOfTag(ENVIRO_END_USER_CODE_DEPENDENCIES, currentLineIndex, lines);
                createUserCodeDependencies(root, block);
                currentLineIndex += block.size() + 1;

            } else if (currentLineCommand.startsWith(COMMENT)) {
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(ENVIRO_IGNORE)) {
                createIgnoreFunction(root, currentLineCommand);
                currentLineIndex++;

            } else {
                logger.error("Do not support " + currentLineCommand);
                currentLineIndex++;
            }
        }
    }

    private void createLibraryStubs(IEnvironmentNode root, String line) {
        String libraries = line.replace(ENVIRO_LIBRARY_STUBS, "").trim();
        if (!libraries.isEmpty()) {
            EnviroLibraryStubNode newNode = new EnviroLibraryStubNode();
            String[] libraryNames = libraries.split(EnviroLibraryStubNode.SEPERATOR_BETWEEN_LIB_STUBS);

            for (String libraryName : libraryNames) {
                String[] library = libraryName.split(EnviroLibraryStubNode.SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER);
                newNode.addLibrary(library[1], library[0]);
            }

            newNode.setParent(root);
            root.addChild(newNode);
//            newNode.setLibraryNames(libraryNames);
        }

    }

    private void createDefinedVariable(IEnvironmentNode root, String line) {
        EnviroDefinedVariableNode newNode = new EnviroDefinedVariableNode();
        String string = line.replace(ENVIRO_DEFINED_VARIABLE, "").trim();

        if (!string.isEmpty()) {
            if (string.contains("=")){
                // example: ABCD=1
                String[] elements = string.split("=");
                if (elements.length == 2) {
                    newNode.setName(elements[0]);
                    newNode.setValue(elements[1]);
                }
            } else {
                // example: ABCD
                newNode.setName(string);
                newNode.setValue("");
            }

            newNode.setParent(root);
            root.addChild(newNode);
        }
    }

    private void createIgnoreFunction(IEnvironmentNode root, String line) {
        EnviroIgnoreNode newNode = new EnviroIgnoreNode();
        String name = PathUtils.toAbsolute(line.replace(ENVIRO_IGNORE, ""));
        newNode.setName(name);
        newNode.setParent(root);
        root.addChild(newNode);
    }

    private void createStubByFunction(IEnvironmentNode root, String line) {
        EnviroSBFNode newNode = new EnviroSBFNode();
        String name = PathUtils.toAbsolute(line.replace(ENVIRO_STUB_BY_FUNCTION, "")
                .replace(ENVIRO_SBF, "").trim());
        newNode.setName(name);
        newNode.setParent(root);
        root.addChild(newNode);
    }

    private void createNotStub(IEnvironmentNode root, String line) {
        EnviroDontStubNode newNode = new EnviroDontStubNode();
        String name = PathUtils.toAbsolute(line.replace(ENVIRO_DONT_STUB, "").trim());
        newNode.setName(name);
        newNode.setParent(root);
        root.addChild(newNode);
    }

    private void createStub(IEnvironmentNode root, String line) {
        EnviroStubNode newNode = new EnviroStubNode();
        String stub = line.replace(ENVIRO_STUB, "").trim();
        stub = PathUtils.toAbsolute(stub);
        newNode.setStub(stub);
        newNode.setParent(root);
        root.addChild(newNode);
    }

    private void setCompiler(IEnvironmentNode root, String[] lines) {
        EnviroCompilerNode newNode = new EnviroCompilerNode();

        for (String line : lines) {
            if (line.startsWith(ENVIRO_COMPILER_NAME))
                newNode.setName(line.replace(ENVIRO_COMPILER_NAME, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_COMPILE_CMD))
                newNode.setCompileCmd(line.replace(ENVIRO_COMPILER_COMPILE_CMD, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_PREPROCESS_CMD))
                newNode.setPreprocessCmd(line.replace(ENVIRO_COMPILER_PREPROCESS_CMD, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_LINK_CMD))
                newNode.setLinkCmd(line.replace(ENVIRO_COMPILER_LINK_CMD, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_DEBUG_CMD))
                newNode.setDebugCmd(line.replace(ENVIRO_COMPILER_DEBUG_CMD, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_INCLUDE_FLAG))
                newNode.setIncludeFlag(line.replace(ENVIRO_COMPILER_INCLUDE_FLAG, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_DEFINE_FLAG))
                newNode.setDefineFlag(line.replace(ENVIRO_COMPILER_DEFINE_FLAG, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_OUTPUT_FLAG))
                newNode.setOutputFlag(line.replace(ENVIRO_COMPILER_OUTPUT_FLAG, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_DEBUG_FLAG))
                newNode.setDebugFlag(line.replace(ENVIRO_COMPILER_DEBUG_FLAG, "").trim());
            else if (line.startsWith(ENVIRO_COMPILER_OUTPUT_EXT))
                newNode.setOutputExt(line.replace(ENVIRO_COMPILER_OUTPUT_EXT, "").trim());
        }

        newNode.setParent(root);
        root.addChild(newNode);
    }

    private void createCoverageType(IEnvironmentNode root, String line) {
        EnviroCoverageTypeNode newNode = new EnviroCoverageTypeNode();
        newNode.setCoverageType(line.replace(ENVIRO_COVERAGE_TYPE, "").trim());
        newNode.setParent(root);
        root.addChild(newNode);
    }

    private void createTypeHandledSourceDir(IEnvironmentNode parent, String line) {
        EnviroTypeHandledSourceDirNode newNode = new EnviroTypeHandledSourceDirNode();
        String dir = line.replace(ENVIRO_TYPE_HANDLED_SOURCE_DIR, "").trim();
        dir = PathUtils.toAbsolute(dir);
        newNode.setTypeHandledSourceDir(dir);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private void createLibraryIncludeDir(IEnvironmentNode parent, String line) {
        EnviroLibraryIncludeDirNode newNode = new EnviroLibraryIncludeDirNode();
        String dir = line.replace(ENVIRO_LIBRARY_INCLUDE_DIR, "").trim();
        dir = PathUtils.toAbsolute(dir);
        newNode.setLibraryIncludeDir(dir);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    /**
     * Example: "ENVIRO.SEARCH_LIST: C:/test"
     *
     * @param parent
     * @param line
     */
    private void createSearchList(IEnvironmentNode parent, String line) {
        EnviroSearchListNode newNode = new EnviroSearchListNode();
        String dir = line.replace(ENVIRO_SEARCH_LIST, "").trim();
        dir = PathUtils.toAbsolute(dir);
        newNode.setSearchList(dir);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private void createUserCodeDependencies(IEnvironmentNode parent, List<String> block) {
        EnviroUserCodeDependenciesNode newNode = new EnviroUserCodeDependenciesNode();
        newNode.setParent(parent);
        parent.addChild(newNode);
        for (int i = 1; i < block.size() - 1; i++)
            // ignore the first and the last element because they are tags.
            newNode.getBlock().add(block.get(i));

    }

    /**
     * Example
     * ENVIRO.STUB_USER_CODE_FILE:
     * <p>
     * BEGINNING_OF_STUB.b.b
     * printf( " Configure Stubs | Beginning of Stub for Unit b, Subprogram b {\n" );
     * END_BEGINNING_OF_STUB.b.b
     * <p>
     * END_OF_STUB.b.b
     * printf( " } Configure Stubs | End of Stub for Unit b, Subprogram b\n\n" );
     * END_END_OF_STUB.b.b
     * <p>
     * BEGINNING_OF_STUB.c.c
     * printf( " Configure Stubs | Beginning of Stub for Unit c, Subprogram c {\n" );
     * END_BEGINNING_OF_STUB.c.c
     * END_OF_STUB.c.c
     * printf( " } Configure Stubs | End of Stub for Unit c, Subprogram c\n\n" );
     * END_END_OF_STUB.c.c
     * <p>
     * ENVIRO.END_STUB_USER_CODE_FILE:
     *
     * @param parent
     * @param block
     */
    private void createStubUserCode(IEnvironmentNode parent, List<String> block) {
        EnviroStubUserCodeFile stubUserCodeNode = new EnviroStubUserCodeFile();
        stubUserCodeNode.setParent(parent);
        parent.addChild(stubUserCodeNode);
        stubUserCodeNode.analyzeBlock(block);
    }

    /**
     * Example:
     * "ENVIRO.STUB_DEPEND_USER_CODE_FILE:
     * BEGIN_Uc:
     * b
     * // Configure Stubs | Stub Dependency, Unit b
     * END_Uc:
     * BEGIN_Uc:
     * c
     * // Configure Stubs | Stub Dependency, Unit c
     * END_Uc:
     * ENVIRO.END_STUB_DEPEND_USER_CODE_FILE:"
     *
     * @param parent
     * @param block
     */
    private void createStubDependUserCodeFile(IEnvironmentNode parent, List<String> block) {
        EnviroStubDependUserCodeFileNode stubDependUserCodeFileNode = new EnviroStubDependUserCodeFileNode();
        stubDependUserCodeFileNode.setParent(parent);
        parent.addChild(stubDependUserCodeFileNode);
        stubDependUserCodeFileNode.analyzeBlock(block);
    }

    private void createWhiteboxSetting(IEnvironmentNode parent, String lineCommand) {
        EnviroWhiteBoxNode newNode = new EnviroWhiteBoxNode();

        if (lineCommand.endsWith("NO"))
            newNode.setActive(false);
        else if (lineCommand.endsWith("YES"))
            newNode.setActive(true);

        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private void createUUT(IEnvironmentNode parent, String lineCommand) {
        EnviroUUTNode newNode = new EnviroUUTNode();
        int pos = lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1;
        String name = lineCommand.substring(pos).trim();
        name = PathUtils.toAbsolute(name);
        newNode.setName(name);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private void initializeNewEnvironment(IEnvironmentNode parent) {
        EnviroNewNode newNode = new EnviroNewNode();
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private void closeEnvironment(IEnvironmentNode root) {
        EnviroEndNode newNode = new EnviroEndNode();
        newNode.setParent(root);
        root.addChild(newNode);
    }

    /**
     * Example: "ENVIRO.NAME: TEST"
     *
     * @param parent
     * @param lineCommand
     */
    private void setNameOfEnvironment(IEnvironmentNode parent, String lineCommand) {
        EnviroNameNode newNode = new EnviroNameNode();

        int pos = lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1;
        String name = lineCommand.substring(pos).trim();

        newNode.setName(name);

        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    /**
     * Example: "ENVIRO.TESTING_METHOD: TRADITIONAL UNIT TESTING"
     *
     * @param parent
     * @param lineCommand
     */
    private void setTestingMethod(IEnvironmentNode parent, String lineCommand) {
        EnviroTestingMethod testingMethod = new EnviroTestingMethod();

        int pos = lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1;
        String method = lineCommand.substring(pos).trim();

        testingMethod.setMethod(method);

        testingMethod.setParent(parent);
        parent.addChild(testingMethod);
    }

    public File getEnvironmentFile() {
        return environmentFile;
    }

    public void setEnvironmentFile(File environmentFile) {
        this.environmentFile = environmentFile;
    }

    public IEnvironmentNode getRoot() {
        return root;
    }

    public void setRoot(IEnvironmentNode root) {
        this.root = root;
    }
}
