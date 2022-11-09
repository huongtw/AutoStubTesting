package com.dse.environment.object;

import com.dse.util.SpecialCharacter;

public class EnviroCompilerNode extends AbstractEnvironmentNode {
    private String name;
    private String compileCmd;
    private String preprocessCmd;
    private String linkCmd;
    private String debugCmd;
    private String includeFlag;
    private String defineFlag;
    private String outputFlag;
    private String debugFlag;
    private String outputExt;

    @Override
    public String toString() {
        return super.toString() + ": compiler = " + name;
    }

    @Override
    public String exportToFile() {
        String output = ENVIRO_COMPILER_NEW + SpecialCharacter.LINE_BREAK;

        output += ENVIRO_COMPILER_NAME + " " + name + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_COMPILE_CMD + " " + compileCmd + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_PREPROCESS_CMD + " " + preprocessCmd + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_LINK_CMD + " " + linkCmd + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_DEBUG_CMD + " " + debugCmd + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_INCLUDE_FLAG + " " + includeFlag + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_DEFINE_FLAG + " " + defineFlag + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_OUTPUT_FLAG + " " + outputFlag + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_DEBUG_FLAG + " " + debugFlag + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_COMPILER_OUTPUT_EXT + " " + outputExt + SpecialCharacter.LINE_BREAK;

        output += ENVIRO_COMPILER_END;

        return output;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCompileCmd() {
        return compileCmd;
    }

    public void setCompileCmd(String compileCmd) {
        this.compileCmd = compileCmd;
    }

    public String getPreprocessCmd() {
        return preprocessCmd;
    }

    public void setPreprocessCmd(String preprocessCmd) {
        this.preprocessCmd = preprocessCmd;
    }

    public String getLinkCmd() {
        return linkCmd;
    }

    public void setLinkCmd(String linkCmd) {
        this.linkCmd = linkCmd;
    }

    public String getDebugCmd() {
        return debugCmd;
    }

    public void setDebugCmd(String debugCmd) {
        this.debugCmd = debugCmd;
    }

    public String getIncludeFlag() {
        return includeFlag;
    }

    public void setIncludeFlag(String includeFlag) {
        this.includeFlag = includeFlag;
    }

    public String getDefineFlag() {
        return defineFlag;
    }

    public void setDefineFlag(String defineFlag) {
        this.defineFlag = defineFlag;
    }

    public String getOutputFlag() {
        return outputFlag;
    }

    public void setOutputFlag(String outputFlag) {
        this.outputFlag = outputFlag;
    }

    public String getDebugFlag() {
        return debugFlag;
    }

    public void setDebugFlag(String debugFlag) {
        this.debugFlag = debugFlag;
    }

    public String getOutputExt() {
        return outputExt;
    }

    public void setOutputExt(String outputExt) {
        this.outputExt = outputExt;
    }
}
