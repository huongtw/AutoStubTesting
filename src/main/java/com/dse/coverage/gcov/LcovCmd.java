package com.dse.coverage.gcov;

import com.dse.util.SpecialCharacter;

public class LcovCmd {
    public static String GCOV_COMMAND = "gcov";
    public static String COMPILE_GCDA_FLAG = "-fprofile-arcs";
    public static String COMPILE_GCNO_FLAG = "-ftest-coverage";
    public static String LINKING_GCDA_FLAG = "-fprofile-arcs";
    public static String LINKING_GCNO_FLAG = "-ftest-coverage";
    public static String LCOV_COMMAND = "lcov";
    public static String LCOV_BRANCH_FLAG = "--rc lcov_branch_coverage=1";
    public static String LCOV_INP_DIR_FLAG = "--capture --directory";
    public static String LCOV_OUTP_FLAG = "--output-file";
    public static String GEN_COMMAND = "genhtml";
    public static String GEN_BRANCH_FLAG = "--branch-coverage";
    public static String GEN_OUTP_DIR_FLAG ="--output-directory";
    public static String BLANK = "";
    public static String COMPILE_GCDA_SYM = "CompileGcda: ";
    public static String COMPILE_GCNO_SYM = "CompileGcno: ";
    public static String LINKING_GCDA_SYM = "LinkingGcda: ";
    public static String LINKING_GCNO_SYM = "LinkingGcno: ";
    public static String LCOV_COMMAND_SYM = "LcovCommand: ";
    public static String LCOV_BRANCH_SYM = "LcovBranch: ";
    public static String LCOV_INP_DIR_SYM = "LcovInputDir: ";
    public static String LCOV_OUTP_SYM = "LcovOutputDir: ";
    public static String GEN_COMMAND_SYM = "GenCommand: ";
    public static String GEN_BRANCH_SYM = "GenBranchCommand: ";
    public static String GEN_OUTP_DIR_SYM ="GenOutputDir: ";
    public static String GCOV_COMMAND_SYM ="GcovCommand: ";

    private static String complGcdaFlag;
    private static String complGcnoFlag;
    private static String linkGcdaFlag;
    private static String linkGcnoFlag;
    private static String lcovCmd;
    private static String lcovBranchFlag;
    private static String lcovIPDFlag;
    private static String lcovOPDFlag;
    private static String genCmd;
    private static String genBranchFlag;
    private static String genOPDFlag;
    private static String gcovCmd;
    private static String commandDataContent;

    public static String getCommandDataContent()
    {
        return commandDataContent;
    }

    public static void setCommandDataContent()
    {
        String content = "";
        content += LcovCmd.COMPILE_GCDA_SYM + LcovCmd.getComplGcdaFlag() + " .endCGA" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.COMPILE_GCNO_SYM + LcovCmd.getComplGcnoFlag() + " .endCGO" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.LINKING_GCDA_SYM + LcovCmd.getLinkGcdaFlag() + " .endLGA" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.LINKING_GCNO_SYM + LcovCmd.getLinkGcnoFlag() + " .endLGO" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.LCOV_COMMAND_SYM + LcovCmd.getLcovCmd() + " .endLC" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.LCOV_BRANCH_SYM + LcovCmd.getLcovBranchFlag() + " .endLB" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.LCOV_INP_DIR_SYM + LcovCmd.getLcovIPDFlag() + " .endLID" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.LCOV_OUTP_SYM + LcovCmd.getLcovOPDFlag() + " .endLOD" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.GEN_COMMAND_SYM + LcovCmd.getGenCmd() + " .endGC" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.GEN_BRANCH_SYM + LcovCmd.getGenBranchFlag() + " .endGB" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.GEN_OUTP_DIR_SYM + LcovCmd.getGenOPDFlag() + " .endGOD" + SpecialCharacter.LINE_BREAK;
        content += LcovCmd.GCOV_COMMAND_SYM + LcovCmd.getGcovCmd() + " .endGVC" + SpecialCharacter.LINE_BREAK;
        content += SpecialCharacter.LINE_BREAK + "//end";

        commandDataContent = content;
    }



    public static String getGcovCmd() {
        return gcovCmd;
    }

    public static void setGcovCmd(String gcovCmd) {
        LcovCmd.gcovCmd = gcovCmd;
    }

    public static String getComplGcdaFlag() {
        return complGcdaFlag;
    }

    public static void setComplGcdaFlag(String complGcdaFlag) {
        LcovCmd.complGcdaFlag = complGcdaFlag;
    }

    public static String getComplGcnoFlag() {
        return complGcnoFlag;
    }

    public static void setComplGcnoFlag(String complGcnoFlag) {
       LcovCmd.complGcnoFlag = complGcnoFlag;
    }

    public static String getLinkGcdaFlag() {
        return linkGcdaFlag;
    }

    public static void setLinkGcdaFlag(String linkGcdaFlag) {
        LcovCmd.linkGcdaFlag = linkGcdaFlag;
    }

    public static String getLinkGcnoFlag() {
        return linkGcnoFlag;
    }

    public static void setLinkGcnoFlag(String linkGcnoFlag) {
        LcovCmd.linkGcnoFlag = linkGcnoFlag;
    }

    public static String getLcovCmd() {
        return lcovCmd;
    }

    public static void setLcovCmd(String lcovCmd) {
        LcovCmd.lcovCmd = lcovCmd;
    }

    public static String getLcovBranchFlag() {
        return lcovBranchFlag;
    }

    public static void setLcovBranchFlag(String lcovBranchFlag) {
        LcovCmd.lcovBranchFlag = lcovBranchFlag;
    }

    public static String getLcovIPDFlag() {
        return lcovIPDFlag;
    }

    public static void setLcovIPDFlag(String lcovIPDFlag) {
       LcovCmd.lcovIPDFlag = lcovIPDFlag;
    }

    public static String getLcovOPDFlag() {
        return lcovOPDFlag;
    }

    public static void setLcovOPDFlag(String lcovOPDFlag) {
        LcovCmd.lcovOPDFlag = lcovOPDFlag;
    }

    public static String getGenCmd() {
        return genCmd;
    }

    public static void setGenCmd(String genCmd) {
       LcovCmd.genCmd = genCmd;
    }

    public static String getGenBranchFlag() {
        return genBranchFlag;
    }

    public static void setGenBranchFlag(String genBranchFlag) {
        LcovCmd.genBranchFlag = genBranchFlag;
    }

    public static String getGenOPDFlag() {
        return genOPDFlag;
    }

    public static void setGenOPDFlag(String genOPDFlag) {
       LcovCmd.genOPDFlag = genOPDFlag;
    }
}
