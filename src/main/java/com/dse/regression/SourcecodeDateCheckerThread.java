package com.dse.regression;

import com.dse.guifx_v3.objects.FXFileView;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.PhysicalTreeExporter;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.util.Date;
import java.util.Map;

@Deprecated
public class SourcecodeDateCheckerThread extends AbstractAkaTask<INode> {

    final static AkaLogger logger = AkaLogger.get(UIController.class);

    @Override
    protected INode call() throws Exception {

        while (true) {
            try {
                logger.debug("Checking the modification date of source code file");
                if (Environment.getInstance().getProjectNode() != null) {

                    AbstractDifferenceDetecter changeDetecter = new SimpleDifferenceDetecter();
                    String elementFolderOfOldVersion = new WorkspaceConfig().fromJson().getElementDirectory();
                    changeDetecter.detectChanges(Environment.getInstance().getProjectNode(), elementFolderOfOldVersion);
                    Map<INode, Date> modifiedSrcFiles = changeDetecter.getModifiedSourcecodeFiles();

                    if (modifiedSrcFiles.size() > 0) {
//                        String changesInStr = "MODIFIED SOURCE CODE FILES:\n";
//                        for (INode key : modifiedSrcFiles.keySet())
//                            changesInStr += key.getAbsolutePath() + "\n";

                        logger.debug("Changed source code file: " + modifiedSrcFiles.keySet());
                        for (INode changedFile : modifiedSrcFiles.keySet())
                            if (changedFile instanceof ISourcecodeFileNode) {

                                Map<Tab, INode> activeSourcecodeTabs = Environment.getInstance().getActiveSourcecodeTabs();
                                for (Tab openingSrcFileTab : Environment.getInstance().getActiveSourcecodeTabs().keySet()) {
                                    ISourcecodeFileNode openingSrcFile = (ISourcecodeFileNode) activeSourcecodeTabs.get(openingSrcFileTab);

                                    if (PathUtils.equals(changedFile.getAbsolutePath(), openingSrcFile.getAbsolutePath())) {
                                        String content = Utils.readFileContent(openingSrcFile);

                                        FXFileView fileView = new FXFileView(changedFile);
                                        AnchorPane acp = fileView.getAnchorPane(true);
                                        openingSrcFileTab.setContent(acp); // fire Not in Java Fx Thread exception

                                        openingSrcFile.setMd5(Utils.computeMd5(content));
                                        openingSrcFile.setLastModifiedDate(modifiedSrcFiles.get(changedFile));
                                        break;
                                    }
                                }
                            }
                        // save physical tree
                        new PhysicalTreeExporter().export(new File(new WorkspaceConfig().fromJson().getPhysicalJsonFile()),
                                Environment.getInstance().getProjectNode());

//                        showYesNoDialog("Found changes", "Some source code files has been changed. Do you want to reload?",
//                                changesInStr, modifiedSrcFiles);

                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }

        return null;
    }

    public static Alert showYesNoDialog(String title,
                                        String headText, String content, Map<INode, Date> modifiedSrcFiles) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headText);
        alert.setContentText(content);
        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, noButton);

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setMinHeight(350);
        textArea.setText(content);

        alert.getDialogPane().setContent(textArea);

        final Button btnOk = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            for (INode changedFile : modifiedSrcFiles.keySet()) {
                Map<Tab, INode> activeSourcecodeTabs = Environment.getInstance().getActiveSourcecodeTabs();
                for (Tab openingSrcFileTab : Environment.getInstance().getActiveSourcecodeTabs().keySet()) {
                    INode openingSrcFile = activeSourcecodeTabs.get(openingSrcFileTab);

                    if (PathUtils.equals(changedFile.getAbsolutePath(), openingSrcFile.getAbsolutePath())) {
                        openingSrcFileTab.setText(Utils.readFileContent(openingSrcFile));
                    }
                }
            }
        });

        alert.showAndWait();
        return alert;
    }
}
