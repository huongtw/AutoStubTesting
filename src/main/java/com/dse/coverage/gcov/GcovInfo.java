package com.dse.coverage.gcov;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.ibm.icu.lang.UCharacter;

import java.io.File;
import java.util.logging.Logger;

public class GcovInfo {
    private String directory; // the root directory of testcasse (lcov folder)
    private String infoPath;
    private String reportPath; // the path to report ( html)
    private String filePath;
    private String reportContent;
    private String cssPath;
    private String rootFilePath;
    private String lvovCPath; // path for file .lcov.c
    private String reportDirPath;
    private String cmdDataPath; // path to file data to read command
    private String dataFileName; // name of data file

    private String compileGcdaFlag;
    private String compileGcnoFlag;
    private String linkingGcdaFlag;
    private String linkingGcnoFlag;
    private String lcovCommand;
    private String lcovBranch;
    private String lcovInputDirFlag;
    private String lcovOutputDirFlag;
    private String generateCommand;
    private String generateBranchFlag;
    private String generateOutputDirFlag;
    private String gcovCommand;


    public GcovInfo() {
    }

    public static String changeReportContent(String reportContent, String dirPath) {
        String changedPath1 = "src=\"file:" + dirPath + File.separator;
        String newContent = reportContent.replace("src=\"", changedPath1);

        String changedPath2 = "<a href=\"" + dirPath + File.separator; //"\"coverFile\"><a href=\""
        newContent = newContent.replace("<a href=\"", changedPath2);
        return newContent;
    }

    public String getReportDirPath() {
        return reportDirPath;
    }

    public void setReportDirPath(String reportDirPath) {
        this.reportDirPath = reportDirPath;
    }

    public String getLcovCPath() {
        return lvovCPath;
    }

    public void setLcovCPath(String lcovCPath) {
        this.lvovCPath = lcovCPath;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String _reportContent) {
        reportContent = _reportContent;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getRootFilePath() {
        return rootFilePath;
    }

    public void setRootFilePath(String rootFilePath) {
        this.rootFilePath = rootFilePath;
    }

    public String getInfoPath() {
        return infoPath;
    }

    public void setInfoPath(String infoPath) {
        this.infoPath = infoPath;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public String getCssPath() {
        return cssPath;
    }

    public void setCssPath(String cssPath) {
        this.cssPath = cssPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCmdDataPath() {
        return cmdDataPath;
    }

    public void setCmdDataPath(String cmdDataPath) {
        this.cmdDataPath = cmdDataPath;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getCompileGcdaFlag() {
        return compileGcdaFlag;
    }

    public void setCompileGcdaFlag(String compileGcdaFlag) {
        this.compileGcdaFlag = compileGcdaFlag;
    }

    public String getCompileGcnoFlag() {
        return compileGcnoFlag;
    }

    public void setCompileGcnoFlag(String compileGcnoFlag) {
        this.compileGcnoFlag = compileGcnoFlag;
    }

    public String getLinkingGcdaFlag() {
        return linkingGcdaFlag;
    }

    public void setLinkingGcdaFlag(String linkingGcdaFlag) {
        this.linkingGcdaFlag = linkingGcdaFlag;
    }

    public String getLinkingGcnoFlag() {
        return linkingGcnoFlag;
    }

    public void setLinkingGcnoFlag(String linkingGcnoFlag) {
        this.linkingGcnoFlag = linkingGcnoFlag;
    }

    public String getLcovCommand() {
        return lcovCommand;
    }

    public void setLcovCommand(String lcovCommand) {
        this.lcovCommand = lcovCommand;
    }

    public String getLcovBranch() {
        return lcovBranch;
    }

    public void setLcovBranch(String lcovBranch) {
        this.lcovBranch = lcovBranch;
    }

    public String getLcovInputDirFlag() {
        return lcovInputDirFlag;
    }

    public void setLcovInputDirFlag(String lcovInputDirFlag) {
        this.lcovInputDirFlag = lcovInputDirFlag;
    }

    public String getLcovOutputDirFlag() {
        return lcovOutputDirFlag;
    }

    public void setLcovOutputDirFlag(String lcovOutputDirFlag) {
        this.lcovOutputDirFlag = lcovOutputDirFlag;
    }

    public String getGenerateCommand() {
        return generateCommand;
    }

    public void setGenerateCommand(String generateCommand) {
        this.generateCommand = generateCommand;
    }

    public String getGenerateBranchFlag() {
        return generateBranchFlag;
    }

    public void setGenerateBranchFlag(String generateBranchFlag) {
        this.generateBranchFlag = generateBranchFlag;
    }

    public String getGenerateOutputDirFlag() {
        return generateOutputDirFlag;
    }

    public void setGenerateOutputDirFlag(String generateOutputDirFlag) {
        this.generateOutputDirFlag = generateOutputDirFlag;
    }

    public String getGcovCommand() {
        return gcovCommand;
    }

    public void setGcovCommand(String gcovCommand) {
        this.gcovCommand = gcovCommand;
    }


/*    private void createData(){
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

        Utils.writeContentToFile(content, this.getCmdDataPath());
    }*/

    public void setUpCommand() {
        String content = Utils.readFileContent(this.getCmdDataPath());
        this.setCompileGcdaFlag(content.substring(content.indexOf(LcovCmd.COMPILE_GCDA_SYM) + LcovCmd.COMPILE_GCNO_SYM.length(), content.indexOf(" .endCGA")));
        this.setCompileGcnoFlag(content.substring(content.indexOf(LcovCmd.COMPILE_GCNO_SYM) + LcovCmd.COMPILE_GCNO_SYM.length(), content.indexOf(" .endCGO")));
        this.setLinkingGcdaFlag(content.substring(content.indexOf(LcovCmd.LINKING_GCDA_SYM) + LcovCmd.LINKING_GCDA_SYM.length(), content.indexOf(" .endLGA")));
        this.setLinkingGcnoFlag(content.substring(content.indexOf(LcovCmd.LINKING_GCNO_SYM) + LcovCmd.LINKING_GCNO_SYM.length(), content.indexOf(" .endLGO")));
        this.setLcovCommand(content.substring(content.indexOf(LcovCmd.LCOV_COMMAND_SYM) + LcovCmd.LCOV_COMMAND_SYM.length(), content.indexOf(" .endLC")));
        this.setLcovBranch(content.substring(content.indexOf(LcovCmd.LCOV_BRANCH_SYM) + LcovCmd.LCOV_BRANCH_SYM.length(), content.indexOf(" .endLB")));
        this.setLcovInputDirFlag(content.substring(content.indexOf(LcovCmd.LCOV_INP_DIR_SYM) + LcovCmd.LCOV_INP_DIR_SYM.length(), content.indexOf(" .endLID")));
        this.setLcovOutputDirFlag(content.substring(content.indexOf(LcovCmd.LCOV_OUTP_SYM) + LcovCmd.LCOV_OUTP_SYM.length(), content.indexOf(" .endLOD")));
        this.setGenerateCommand(content.substring(content.indexOf(LcovCmd.GEN_COMMAND_SYM) + LcovCmd.GEN_COMMAND_SYM.length(), content.indexOf(" .endGC")));
        this.setGenerateBranchFlag(content.substring(content.indexOf(LcovCmd.GEN_BRANCH_SYM) + LcovCmd.GEN_BRANCH_SYM.length(), content.indexOf(" .endGB")));
        this.setGenerateOutputDirFlag(content.substring(content.indexOf(LcovCmd.GEN_OUTP_DIR_SYM) + LcovCmd.GEN_OUTP_DIR_SYM.length(), content.indexOf(" .endGOD")));
        this.setGcovCommand(content.substring(content.indexOf(LcovCmd.GCOV_COMMAND_SYM) + LcovCmd.GCOV_COMMAND_SYM.length(), content.indexOf(" .endGVC")));

    }

}
