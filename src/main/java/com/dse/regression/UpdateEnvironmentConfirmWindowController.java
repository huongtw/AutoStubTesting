package com.dse.regression;

import com.dse.compiler.message.ICompileMessage;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.PhysicalTreeExporter;
import com.dse.environment.WorkspaceCreation;
import com.dse.environment.object.EnviroSBFNode;
import com.dse.environment.object.EnviroUUTNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.controllers.build_environment.AbstractCustomController;
import com.dse.guifx_v3.controllers.build_environment.BaseController;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.object.*;
import com.dse.search.LambdaFunctionNodeCondition;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.thread.AkaThread;
import com.dse.thread.task.BuildEnvironmentResult;
import com.dse.thread.task.RunInAnotherThreadTask;
import com.dse.thread.task.UpdateEnvironmentTask;
import com.dse.user_code.UserCodeManager;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.dse.guifx_v3.controllers.build_environment.BaseController.showResolver;
import static com.dse.guifx_v3.helps.UIController.getPrimaryStage;

public class UpdateEnvironmentConfirmWindowController extends AbstractApplyChangesConfirm {

    final static AkaLogger logger = AkaLogger.get(UpdateEnvironmentConfirmWindowController.class);

    public static UpdateEnvironmentConfirmWindowController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/UpdateEnvironmentConfirmWindow.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Update Environment Confirmation");

            UpdateEnvironmentConfirmWindowController controller = loader.getController();
            controller.setStage(stage);

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void ok() {
        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Build Environment");
        loadingPopup.initOwnerStage(stage);
        loadingPopup.show();

        // STEP 1: handle change: delete TCs, restate TCs, make probe points invalid
        RunInAnotherThreadTask handleChangeTask = new RunInAnotherThreadTask();
        handleChangeTask.setCallBack(this::applyChange);

        new AkaThread(handleChangeTask).start();

        // STEP 1: reparse project
        handleChangeTask.setOnSucceeded(event -> {
            UpdateEnvironmentTask updateEnvTask = new UpdateEnvironmentTask();
            new AkaThread(updateEnvTask).start();

            updateEnvTask.setOnSucceeded(event1 -> {
                loadingPopup.close();

                int success = BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.OTHER;

                BuildEnvironmentResult result = null;
                try {
                    result = updateEnvTask.get();
                    success = result.getExitCode();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (success != BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.CREATE_ENVIRONMENT) {
                    switch (success) {
                        case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION: {
                            Stage stage = showResolver(result);
                            stage.setOnHiding(new EventHandler<WindowEvent>() {
                                @Override
                                public void handle(WindowEvent event) {
                                    UpdateEnvironmentConfirmWindowController.this.getStage().close();
                                }
                            });
                            break;
                        }

                        case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.LINKAGE:
                            showLinkError(result.getLinkMessage());
                            break;

                        case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_ENV_FILE:
                        case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_TST_FILE: {
                            UIController.showErrorDialog("Could not rebuild the environment because the name of the new environment exists", "Rebuild environment", "Fail");
                            break;
                        }

                        default: {
                            UIController.showErrorDialog("Could not rebuild the environment due to unexpected error", "Rebuild environment", "Fail");
                            break;
                        }
                    }
                } else {
                    updateOthers();
                }
            });
        });
    }

    private void showLinkError(ICompileMessage linkMessage) {
        Platform.runLater(() -> {
            ButtonType ignore = new ButtonType("Ignore", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alert = new Alert(Alert.AlertType.WARNING, SpecialCharacter.EMPTY, ignore, cancel);
            alert.setTitle("Build Environment Error");
            alert.setHeaderText("Failed to link this project");

            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setMinHeight(350);
            textArea.setText(linkMessage.getMessage());

            alert.getDialogPane().setContent(textArea);
            alert.initOwner(getPrimaryStage());

            Optional<ButtonType> result = alert.showAndWait();

            if (result.orElse(ignore) == cancel) {
                alert.close();
            } else {
                try {
                    updateOthers();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateTestCaseNavigator() {
//        Environment.getInstance().refreshTestCaseScriptRootNode();
        TestcaseRootNode root = Environment.getInstance().getTestcaseScriptRootNode();

        // <<SBF>> and <<UUT>>
        IEnvironmentNode envRoot = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> uutNodes = EnvironmentSearch.searchNode(envRoot, new EnviroUUTNode());
        List<IEnvironmentNode> allSourceCodeNodes = new ArrayList<>(uutNodes);
        List<IEnvironmentNode> sbfNodes = EnvironmentSearch.searchNode(envRoot, new EnviroSBFNode());
        allSourceCodeNodes.addAll(sbfNodes);

        List<ITestcaseNode> unitNodes = TestcaseSearch.searchNode(root, new TestUnitNode());
        for (ITestcaseNode node : unitNodes) {
            TestUnitNode unitNode = (TestUnitNode) node;
            IEnvironmentNode uutNode = null;
            for (IEnvironmentNode n : allSourceCodeNodes) {
                if (n instanceof EnviroUUTNode &&  ((EnviroUUTNode) n).getName().equals(unitNode.getName())) {
                    uutNode = n;
                    break;
                } else if (n instanceof EnviroSBFNode && ((EnviroSBFNode) n).getName().equals(unitNode.getName())) {
                    uutNode = n;
                    break;
                }
            }
            ISourcecodeFileNode fileNode = UIController.searchSourceCodeFileNodeByPath(unitNode.getName());
            if (fileNode != null && uutNode != null) {
//                ParseToLambdaFunctionNode newParser = new ParseToLambdaFunctionNode();
//                newParser.parse(fileNode);
                allSourceCodeNodes.remove(uutNode);

                List<INode> children = Search.searchNodes(fileNode, new AbstractFunctionNodeCondition());
                children.addAll(Search.searchNodes(fileNode, new MacroFunctionNodeCondition()));
                children.addAll(Search.searchNodes(fileNode, new LambdaFunctionNodeCondition()));
                children = children.stream().distinct().collect(Collectors.toList());

                List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());
                for (ITestcaseNode node1 : subprogramNodes) {
                    TestNormalSubprogramNode normalSubNode = (TestNormalSubprogramNode) node1;
                    try {
                        ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(normalSubNode.getName());
                        children.remove(functionNode);
//                        ICommonFunctionNode prevFunctionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(normalSubNode.getName());
//                        List<TestCase> testCases = TestCaseManager.getTestCasesByFunction(prevFunctionNode);
//
//                        for (TestCase testCase : testCases) {
//                            node.addChild(testCase.getTestNewNode());
//                            testCase.getTestNewNode().setParent(node);
//                        }
                    } catch (FunctionNodeNotFoundException fe) {
                        unitNode.getChildren().remove(normalSubNode);
                    }
                }

                for (INode function : children) {
                    if (function instanceof ICommonFunctionNode) {
                        TestNormalSubprogramNode newSubprogram = new TestNormalSubprogramNode();
                        newSubprogram.setName(function.getAbsolutePath());
                        unitNode.addChild(newSubprogram);
                    }
                }
            } else {
                root.getChildren().remove(unitNode);
            }
        }

        for (IEnvironmentNode scNode : allSourceCodeNodes) {
            TestUnitNode unitNode = new TestUnitNode();

            if (scNode instanceof EnviroUUTNode) {
                unitNode.setName(((EnviroUUTNode) scNode).getName());
            } else if (scNode instanceof EnviroSBFNode) {
                unitNode.setName(((EnviroSBFNode) scNode).getName());
            }

            root.addChild(unitNode);

            ISourcecodeFileNode matchedSourcecodeNode = UIController.searchSourceCodeFileNodeByPath(unitNode.getName());

            // add subprograms
            if (matchedSourcecodeNode != null) {
                ParseToLambdaFunctionNode newParser = new ParseToLambdaFunctionNode();
                newParser.parse(matchedSourcecodeNode);

                List<INode> children = Search.searchNodes(matchedSourcecodeNode, new AbstractFunctionNodeCondition());
                children.addAll(Search.searchNodes(matchedSourcecodeNode, new MacroFunctionNodeCondition()));
                children.addAll(Search.searchNodes(matchedSourcecodeNode, new LambdaFunctionNodeCondition()));
                children = children.stream().distinct().collect(Collectors.toList());

                for (INode function : children) {
                    if (function instanceof ICommonFunctionNode) {
                        TestNormalSubprogramNode newSubprogram = new TestNormalSubprogramNode();
                        newSubprogram.setName(function.getAbsolutePath());
                        unitNode.addChild(newSubprogram);
                    }
                }
            }
        }
//        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestNormalSubprogramNode());
//
//        try {
//            for (ITestcaseNode node : nodes) {
//                TestNormalSubprogramNode normalSubNode = (TestNormalSubprogramNode) node;
//                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(normalSubNode.getName());
//                List<TestCase> testCases = TestCaseManager.getTestCasesByFunction(functionNode);
//
//                for (TestCase testCase : testCases) {
//                    node.addChild(testCase.getTestNewNode());
//                    testCase.getTestNewNode().setParent(node);
//                }
//            }
//        } catch (FunctionNodeNotFoundException fe) {
//            logger.debug(fe.getMessage());
//        }

        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestCompoundSubprogramNode());
        List<CompoundTestCase> compoundTestCases = new ArrayList<>();
        List<String> compTCNames = new ArrayList<>(TestCaseManager.getNameToCompoundTestCaseMap().keySet());
        for (String name : compTCNames) {
            compoundTestCases.add(TestCaseManager.getCompoundTestCaseByName(name));
        }

        for (CompoundTestCase testCase : compoundTestCases) {
            nodes.get(0).addChild(testCase.getTestNewNode());
            testCase.getTestNewNode().setParent(nodes.get(0));
        }

        Environment.getInstance().saveTestcasesScriptToFile();
    }

    private void updateOthers() {
        // clear every thing (MDIWindows, message windows)
        UIController.clear();
        BaseController.resetStatesInAllWindows();

        updateTestCaseNavigator();

        File testcasesScriptFile = new File(new WorkspaceConfig().fromJson().getTestscriptFile());
        UIController.loadTestCasesNavigator(testcasesScriptFile);

        TestCaseManager.clearMaps();
        TestCaseManager.initializeMaps();

        // update information displaying on bottom of screen
        BaseSceneController.getBaseSceneController().updateInformation();

        loadProjectStructureTree();

        File envFile = new File(new WorkspaceConfig().fromJson().getEnvironmentFile());
        MenuBarController.addEnvironmentToHistory(envFile);

        // load user codes
        UserCodeManager.getInstance().clear();
        UserCodeManager.getInstance().loadExistedUserCodes();

        // update probe points - step 2:
        UIController.loadProbePoints();
        // update probe points - step 3:
        UIController.insertIncludesForProbePoints();

        UIController.loadRegressionScripts();

        // reanalyze dependencies
        try {
            ProjectNode projectRootNode = Environment.getInstance().getProjectNode();

            WorkspaceConfig wkConfig = new WorkspaceConfig().fromJson();

            String physicalTreePath = wkConfig.getPhysicalJsonFile();
            File physicalTreeFile = new File(physicalTreePath);
            new PhysicalTreeExporter().export(physicalTreeFile, projectRootNode);

            Utils.deleteFileOrFolder(new File(wkConfig.getDependencyDirectory()));
            Utils.deleteFileOrFolder(new File(wkConfig.getElementDirectory()));
            new WorkspaceCreation().exportSourcecodeFileNodeToWorkingDirectory(projectRootNode,
                    wkConfig.getElementDirectory(),
                    wkConfig.getDependencyDirectory());

            UIController.showSuccessDialog("Dependency analyzer successes", "Dependency analyzer", "Success");
        } catch (Exception e) {
            e.printStackTrace();
            UIController.showErrorDialog("Dependency analyzer caught an unexpected error", "Dependency analyzer", "Fail");
        }

        if (stage != null) {
            stage.close();
        }
        UIController.getEnvironmentBuilderStage().close();

        UIController.showSuccessDialog("Rebuild the environment successfully", "Rebuild environment", "Success");
    }

    /**
     * Export a environment tree to file
     *
     * @return true if success, false if failed
     */
    private int exportEnvironmentToFile(File environmentFile, EnvironmentRootNode root) {
        logger.debug("Exporting the environment script to file.");

        // if we are creating new environment, but the environment exists before
        if (AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_BLANK_GUI
                || AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_OPENING_GUI)
            if (environmentFile.exists()) {
                logger.error("The environment file " + environmentFile.getAbsolutePath() + " exists! Check it again.");
                return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_ENV_FILE;
            }

        // export the environment script to file in the working directory
        String envFileRelative = PathUtils.toRelative(environmentFile.getAbsolutePath());
        new WorkspaceConfig().fromJson().setEnvironmentFile(envFileRelative).exportToJson();
        try {
            FileWriter writer = new FileWriter(environmentFile);
            writer.write(root.exportToFile());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("The environment has been exported. Path = " + environmentFile.getAbsolutePath());
        return BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_ENV_FILE;
    }

    private int updateEnvFileWhenRebuilding(File environmentFile, EnvironmentRootNode root) {
        return exportEnvironmentToFile(environmentFile, root);
    }

//    /**
//     * @param envNodeName
//     * @param uutNodesInUpdatedTst
//     * @param tstRoot
//     * @return true if envNode is not in tst tree, and we can add it to tst tree
//     */
//    private boolean handledAddedSourcecodeFileWhenUpdatingEnv(String envNodeName,
//                                                              List<ITestcaseNode> uutNodesInUpdatedTst,
//                                                              TestcaseRootNode tstRoot) {
//        // find a tst node corresponding to the env node
//        ITestcaseNode matchingUut = null;
//        for (ITestcaseNode uutNodeInTst : uutNodesInUpdatedTst)
//            if (uutNodeInTst instanceof TestUnitNode)
//                if (((TestUnitNode) uutNodeInTst).getName().equals(envNodeName)) {
//                    matchingUut = uutNodeInTst;
//                    break;
//                }
//
//        if (matchingUut == null) {
//            // if we can not found a corresponding node in tst tree,
//            // it means that we add new uut/sbf when updating env.
//            // We need to add this node to tst tree.
//            SourcecodeFileParser srcParser = new SourcecodeFileParser();
//            try {
//                INode rootSrc = srcParser.parseSourcecodeFile(new File(envNodeName));
//
//                // add source code file to tst tree
//                TestUnitNode unitNode = new TestUnitNode();
//                unitNode.setName(envNodeName);
//                unitNode.setParent(tstRoot);
//                tstRoot.getChildren().add(unitNode);
//
//                // add all functions to tst tree
//                List<INode> children = Search.searchNodes(rootSrc, new AbstractFunctionNodeCondition());
//                children.addAll(Search.searchNodes(rootSrc, new MacroFunctionNodeCondition()));
//
//                for (INode child : children)
//                    if (child instanceof ICommonFunctionNode) {
//                        TestNormalSubprogramNode subprogramNode = new TestNormalSubprogramNode();
//                        subprogramNode.setName(child.getAbsolutePath());
//                        subprogramNode.setParent(unitNode);
//                        unitNode.getChildren().add(subprogramNode);
//                    }
//                return true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//        return false;
//    }
//
//    private boolean handledDeletedSourcecodeFileWhenUpdatingEnv(String tstNodeName,
//                                                                List<IEnvironmentNode> envNodes,
//                                                                ITestcaseNode tstNode) {
//        boolean existInEnvTree = false;
//        for (IEnvironmentNode envNode : envNodes) {
//            String envName = "";
//            if (envNode instanceof EnviroUUTNode)
//                envName = ((EnviroUUTNode) envNode).getName();
//            else if (envNode instanceof EnviroSBFNode)
//                envName = ((EnviroSBFNode) envNode).getName();
//            if (envName.equals(tstNodeName)) {
//                existInEnvTree = true;
//                break;
//            }
//        }
//
//        if (!existInEnvTree) {
//            // we need to remove this node in tst tree
//            ITestcaseNode root = tstNode.getParent();
//            root.getChildren().remove(tstNode);
//
//            for (ITestcaseNode child : TestcaseSearch.searchNode(tstNode, new TestNameNode()))
//                if (child instanceof TestNameNode) {
//                    logger.debug("Deleted: " + ((TestNameNode) child).getName() + " in " + tstNodeName);
//                }
//        }
//
//        return false;
//    }

//    private int updateTestcaseScriptWhenRebuilding(TestcaseRootNode tstRoot, EnvironmentRootNode updatedEnvRoot,
//                                                   String tstFile) {
//        // get all uuts and stubs (after being updated)
//        List<IEnvironmentNode> sbfNodes = EnvironmentSearch.searchNode(updatedEnvRoot, new EnviroSBFNode());
//        List<IEnvironmentNode> uutNodes = EnvironmentSearch.searchNode(updatedEnvRoot, new EnviroUUTNode());
//        sbfNodes.addAll(uutNodes);
//
//        // get all unit nodes in tst
//        List<ITestcaseNode> uutNodesInUpdatedTst = TestcaseSearch.searchNode(tstRoot, new TestUnitNode());
//
//        Map<String, String> deletedTestcases = new HashMap<>();
//
//        // Case 1: exist in environment tree, but not exist in tst
//        for (IEnvironmentNode envNode : sbfNodes) {
//            // get name of environment node
//            String envNodeName = "";
//            if (envNode instanceof EnviroSBFNode)
//                envNodeName = ((EnviroSBFNode) envNode).getName();
//            else if (envNode instanceof EnviroUUTNode)
//                envNodeName = ((EnviroUUTNode) envNode).getName();
//
//            if (envNodeName.equals(""))
//                continue;
//            handledAddedSourcecodeFileWhenUpdatingEnv(envNodeName, uutNodesInUpdatedTst, tstRoot);
//
//        }
//
//        // Case 2: deleted source code file when updating environment
//        for (ITestcaseNode tstNode : uutNodesInUpdatedTst)
//            if (tstNode instanceof TestUnitNode) {
//                String tstName = ((TestUnitNode) tstNode).getName();
//                handledDeletedSourcecodeFileWhenUpdatingEnv(tstName, sbfNodes, tstNode);
//            }
//
//        // save in file
//        Utils.writeContentToFile(tstRoot.exportToFile(), new WorkspaceConfig().fromJson().getTestscriptFile());
//
//        // save deleted node
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String json = gson.toJson(deletedTestcases);
//        Utils.writeContentToFile(json, new WorkspaceConfig().fromJson().getDeletedTestcaseWhenUpdatingEnv());
//        return BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_TST_FILE;
//    }
//
//    private int rebuildExistingEnvironment(ActionEvent actionEvent) {
//        if (Environment.getInstance().getProjectNode() == null)
//            return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.OTHER;
//
//        // STEP: delete all related folders when environment is updated
//        // Then export physical_tree.json, dependencies, elements, etc. to initialized working space
//        WorkspaceConfig wkConfig = new WorkspaceConfig().fromJson();
//        Utils.deleteFileOrFolder(new File(wkConfig.getDependencyDirectory()));
//        Utils.deleteFileOrFolder(new File(wkConfig.getPhysicalJsonFile()));
//        Utils.deleteFileOrFolder(new File(wkConfig.getElementDirectory()));
//        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
//        BaseController.addAnalysisInformationToWorkspace(workspace,
//                wkConfig.getDependencyDirectory(),
//                wkConfig.getPhysicalJsonFile(),
//                wkConfig.getElementDirectory());
//
//        // STEP: update env file
//        File envFile = new File(new WorkspaceConfig().fromJson().getEnvironmentFile());
//        EnvironmentRootNode envRoot = Environment.getInstance().getEnvironmentRootNode();
//        int exportedEnvironmentDone = updateEnvFileWhenRebuilding(envFile, envRoot);
//        if (exportedEnvironmentDone != BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_ENV_FILE) {
//            return exportedEnvironmentDone;
//        }
//
//        // STEP: update tst file
//        TestcaseRootNode updatedTstRoot = Environment.getBackupEnvironment().getTestcaseScriptRootNode();
//        String tstFile = new WorkspaceConfig().fromJson().getTestscriptFile();
//        int exportTestscriptDone = updateTestcaseScriptWhenRebuilding(updatedTstRoot, envRoot, tstFile);
//        if (exportTestscriptDone != BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_TST_FILE) {
//            return exportTestscriptDone;
//        }
//
////        // STEP: update name of the environment
////        String newName = Environment.getInstance().getNameOfEnvironment();
////        String oldName = Environment.getBackupEnvironment().getNameOfEnvironment();
////        if (!newName.equals(oldName)){
////            changeTheNameOfWorkspace(Environment.getInstance().getNameOfEnvironment());
////        }
//
//        // OTHERS
//        updateRemaining(envFile, actionEvent);
//
//        return BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.CREATE_ENVIRONMENT;
//    }

    @FXML
    public void cancel() {
        if (stage != null) {
            stage.close();
        }
    }

//    private void updateRemaining(File envFile, ActionEvent actionEvent) {
////        setIgnoredFoldersInProjectTree();
//
//        // Clone project
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//        executorService.submit(new ProjectClone.CloneThread());
////        executorService.submit(new SystemHeaderParser.InitThread());
//
//        executorService.shutdown();
//        try {
//            executorService.awaitTermination(10, TimeUnit.MINUTES);
//
//            // create workspace
//            WorkspaceCreation wk = new WorkspaceCreation();
//            wk.setWorkspace(new AkaConfig().fromJson().getOpeningWorkspaceDirectory());
//            wk.setDependenciesFolder(new WorkspaceConfig().fromJson().getDependencyDirectory());
//            wk.setElementFolder(new WorkspaceConfig().fromJson().getElementDirectory());
//            wk.setPhysicalTreePath(new WorkspaceConfig().fromJson().getPhysicalJsonFile());
//            wk.setRoot(Environment.getInstance().getProjectNode());
//            wk.create(wk.getRoot(), wk.getElementFolder(), wk.getDependenciesFolder(), wk.getPhysicalTreePath());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // clear every thing (MDIWindows, messange windows)
//        UIController.clear();
//        BaseController.resetStatesInAllWindows();
//
//        File testcasesScriptFile = new File(new WorkspaceConfig().fromJson().getTestscriptFile());
//        UIController.loadTestCasesNavigator(testcasesScriptFile);
//
//        // update information displaying on bottom of screen
//        BaseSceneController.getBaseSceneController().updateInformation();
//
//        loadProjectStructureTree();
//
//        MenuBarController.addEnvironmentToHistory(envFile);
//    }

    private void loadProjectStructureTree() {
        IProjectNode root = Environment.getInstance().getProjectNode();
        root.setName(Environment.getInstance().getName());
        UIController.loadProjectStructureTree(root);
        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
        new PhysicalTreeExporter().export(new File(physicalTreePath), (Node) root);
    }
}
