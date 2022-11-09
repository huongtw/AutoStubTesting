package com.dse.guifx_v3.controllers.build_environment;

import com.dse.compiler.Terminal;
import com.dse.coverage.gcov.LcovCmd;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.environment.object.EnviroWhiteBoxNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class BuildOptionsController extends AbstractCustomController implements Initializable {
    final static AkaLogger logger = AkaLogger.get(BuildOptionsController.class);
    @FXML
    private TextField cmplGcdaFlag,
            cmplGncoFlag,
            lkGncoFlag,
            lkGcdaFlag,
            oDgCFlag,
            iDLcFlag,
            genCommand,
            oDLcFlag,
            lcovCommand,
            brchLcFlag,
            gcovCommand,
            brchGenFlag;
    @FXML
    private CheckBox chbWhitebox;
    @FXML
    private ComboBox<String> cbCoverageType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbCoverageType.getItems().addAll(
                EnviroCoverageTypeNode.BASIS_PATH,
                EnviroCoverageTypeNode.BRANCH,
                EnviroCoverageTypeNode.MCDC,
                EnviroCoverageTypeNode.STATEMENT,
                EnviroCoverageTypeNode.STATEMENT_AND_BRANCH,
                EnviroCoverageTypeNode.STATEMENT_AND_MCDC);
        cbCoverageType.setValue(EnviroCoverageTypeNode.STATEMENT);
        cmplGcdaFlag.setText(LcovCmd.COMPILE_GCDA_FLAG);
        cmplGncoFlag.setText(LcovCmd.COMPILE_GCNO_FLAG);
        lkGcdaFlag.setText(LcovCmd.LINKING_GCDA_FLAG);
        lkGncoFlag.setText(LcovCmd.LINKING_GCNO_FLAG);
        oDgCFlag.setText(LcovCmd.GEN_OUTP_DIR_FLAG);
        iDLcFlag.setText(LcovCmd.LCOV_INP_DIR_FLAG);
        genCommand.setText(LcovCmd.GEN_COMMAND);
        oDLcFlag.setText(LcovCmd.LCOV_OUTP_FLAG);
        lcovCommand.setText(LcovCmd.LCOV_COMMAND);
        brchLcFlag.setText(LcovCmd.BLANK);
        brchGenFlag.setText(LcovCmd.BLANK);
        gcovCommand.setText(LcovCmd.GCOV_COMMAND);

        Hint.tooltipNode(chbWhitebox, HintContent.EnvBuilder.BuildOpt.WHITEBOX);
    }

    public void UpdateBranchFlagCorrespondingToCoverageType() {
        if (cbCoverageType.getValue() != null) {
            if (cbCoverageType.getValue().equals(EnviroCoverageTypeNode.BRANCH) || cbCoverageType.getValue().equals(EnviroCoverageTypeNode.STATEMENT_AND_BRANCH)) {
                brchLcFlag.setText(LcovCmd.LCOV_BRANCH_FLAG);
                brchGenFlag.setText(LcovCmd.GEN_BRANCH_FLAG);
            } else {
                brchLcFlag.setText(LcovCmd.BLANK);
                brchGenFlag.setText(LcovCmd.BLANK);
            }
        }
    }

    @Override
    public void validate() {
        // nothing to do
        setValid(true);
    }

    @Override
    public void save() {
        saveWhitebox(chbWhitebox.isSelected());
        saveCoverageType(cbCoverageType.getValue());
        saveLcovCommand();
    }

    private void saveWhitebox(boolean whiteboxEnabled) {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroWhiteBoxNode());

        if (nodes.size() == 1) {
            ((EnviroWhiteBoxNode) nodes.get(0)).setActive(whiteboxEnabled);
            logger.debug("Environment configuration:\n" + root.exportToFile());
        } else if (nodes.size() == 0) {
            EnviroWhiteBoxNode node = new EnviroWhiteBoxNode();
            node.setActive(whiteboxEnabled);
            root.addChild(node);
            logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
        } else {
            logger.error("Error");
        }
    }

    private void saveCoverageType(String coverageType) {
        if (coverageType != null) {
            EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
            List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCoverageTypeNode());

            if (nodes.size() == 1) {
                ((EnviroCoverageTypeNode) nodes.get(0)).setCoverageType(coverageType);

            } else if (nodes.size() == 0) {
                EnviroCoverageTypeNode node = new EnviroCoverageTypeNode();
                node.setCoverageType(coverageType);
                root.addChild(node);

            } else {
                logger.error("Invalid environment configuration. Found more than one coverage type node: " + nodes);
            }
        } else {
            logger.error("Coverage type is not set up");
        }
        logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
    }

    private void saveLcovCommand() {
        LcovCmd.setComplGcdaFlag(cmplGcdaFlag.getText());
        LcovCmd.setComplGcnoFlag(cmplGncoFlag.getText());
        LcovCmd.setLinkGcdaFlag(lkGcdaFlag.getText());
        LcovCmd.setLinkGcnoFlag(lkGncoFlag.getText());
        if (Utils.isWindows()) {
            LcovCmd.setLcovCmd("perl "
                    + System.getenv("LCOV_HOME") + "\\bin\\" + lcovCommand.getText());
        } else {
            LcovCmd.setLcovCmd(lcovCommand.getText());
        }
        LcovCmd.setLcovBranchFlag(brchLcFlag.getText());
        LcovCmd.setLcovIPDFlag(iDLcFlag.getText());
        LcovCmd.setLcovOPDFlag(oDLcFlag.getText());
        if (Utils.isWindows()) {
            LcovCmd.setGenCmd("perl "
                    + System.getenv("LCOV_HOME") + "\\bin\\" + genCommand.getText());
        } else {
            LcovCmd.setGenCmd(genCommand.getText());
        }
        LcovCmd.setGenBranchFlag(brchGenFlag.getText());
        LcovCmd.setGenOPDFlag(oDgCFlag.getText());
        LcovCmd.setGcovCmd(gcovCommand.getText());
        LcovCmd.setCommandDataContent();
    }

    @Override
    public void loadFromEnvironment() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        // load coverage type
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCoverageTypeNode());
        if (nodes.size() == 1) {
            String type = ((EnviroCoverageTypeNode) nodes.get(0)).getCoverageType();
            // TODO: valid the type
            cbCoverageType.setValue(type);
//            if(type.equals(EnviroCoverageTypeNode.BRANCH) || type.equals(EnviroCoverageTypeNode.STATEMENT_AND_BRANCH))
//            {
//                logger.debug("Branch!!");
//                brchLcFlag.setText(GcovInfo.LcovCmd.LCOV_BRANCH_FLAG);
//                brchGenFlag.setText(GcovInfo.LcovCmd.GEN_BRANCH_FLAG);
//            }
//            else logger.debug("Not Branch");
        } else if (nodes.size() == 0) {
            cbCoverageType.setValue(EnviroCoverageTypeNode.STATEMENT);
        } else {
            logger.error("Invalid environment configuration. Found more than one coverage type node: " + nodes);
        }

        // load check box white-box
        nodes = EnvironmentSearch.searchNode(root, new EnviroWhiteBoxNode());
        if (nodes.size() == 1) {
            boolean selected = ((EnviroWhiteBoxNode) nodes.get(0)).isActive();
            chbWhitebox.setSelected(selected);
        } else if (nodes.size() == 0) {
            chbWhitebox.setSelected(false);
        } else {
            logger.error("Invalid environment configuration. Found more than white box node: " + nodes);
        }
    }

    public void runTestCommand(ActionEvent actionEvent) {
        saveLcovCommand();
        String ver = " --version";
        String testGcovCmd = LcovCmd.getGcovCmd() + ver;
        String res = SpecialCharacter.EMPTY;
        boolean success = true;
        try {
            Terminal terGcov = new Terminal(testGcovCmd);
            res += terGcov.get().split("\n")[1] + SpecialCharacter.LINE_BREAK;
        } catch (IOException | InterruptedException e) {
            success = false;
            UIController.showErrorDialog("Can't execute " + testGcovCmd,
                    "Aka automation tool", "Gcov Command error");
        }
        if (Utils.isWindows()){
            String testPerlCmd = "perl" + ver;
            try {
                Terminal terPerl = new Terminal(testPerlCmd);
                String perlRes = terPerl.get().split("\n")[2];
                res += "perl: " + perlRes.substring(perlRes.indexOf('(') + 1, perlRes.indexOf(')'))
                        + perlRes.substring(perlRes.indexOf(')') + 1)
                        + SpecialCharacter.LINE_BREAK;
            } catch (IOException | InterruptedException e) {
                success = false;
                UIController.showErrorDialog("Can't execute: perl --version",
                        "Aka automation tool", "Perl Command error");
            }
        }
        String testLcovCmd = LcovCmd.getLcovCmd() + ver;
        try {
            Terminal terLcov = new Terminal(testLcovCmd);
            if (terLcov.get().contains("version")) {
                res += terLcov.get().split("\n")[1] + SpecialCharacter.LINE_BREAK;
            } else {
                success = false;
                UIController.showErrorDialog(terLcov.get(),
                        "Aka automation tool", "Lcov Command error");
            }
        } catch (IOException | InterruptedException e) {
            success = false;
            UIController.showErrorDialog("Can't execute: " + testLcovCmd,
                    "Aka automation tool", "Lcov Command error");
        }
        String testGenhtmlCmd = LcovCmd.getGenCmd() + ver;
        try {
            Terminal terGenhtml = new Terminal(testGenhtmlCmd);
            if (terGenhtml.get().contains("version")) {
                res += terGenhtml.get().split("\n")[1] + SpecialCharacter.LINE_BREAK;
            } else {
                success = false;
                UIController.showErrorDialog(terGenhtml.get(),
                        "Aka automation tool", "Lcov Command error");
            }
            logger.debug(res);
        } catch (IOException | InterruptedException e) {
            success = false;
            UIController.showErrorDialog("Can't execute: " + testGenhtmlCmd,
                    "Aka automation tool", "Genhtml Command error");
        }
        if (success) {
            UIController.showSuccessDialog(res, "Aka automation tool", "LCOV");
        }
    }
}
