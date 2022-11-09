package com.dse.guifx_v3.helps;

import auto_testcase_generation.cte.UI.Controller.CTEWindowController;
import auto_testcase_generation.cte.UI.Controller.CteController;
import auto_testcase_generation.cte.UI.OptionTable.CteOptionTable;
import auto_testcase_generation.cte.UI.TestcaseTable.CteTestcaseTable;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.core.ClassificationTreeManager;
import com.dse.compiler.message.ICompileMessage;
import com.dse.config.AkaConfig;
import com.dse.config.IProjectType;
import com.dse.config.Paths;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.exception.OpenFileException;
import com.dse.guifx_v3.controllers.CompoundTestCaseTreeTableViewController;
import com.dse.guifx_v3.controllers.build_environment.BaseController;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.main_view.LeftPaneController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.objects.FXFileView;
import com.dse.guifx_v3.objects.ParameterColumnCellFactory;
import com.dse.guifx_v3.objects.SourceCodeViewTab;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.probe_point_manager.ProbePointUtils;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.project_init.ProjectClone;
import com.dse.project_init.ProjectCloneMap;
import com.dse.regression.RegressionScriptManager;
import com.dse.search.LambdaFunctionNodeCondition;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.DefinitionFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNormalSubprogramNode;
import com.dse.testcasescript.object.TestUnitNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.MacroSubprogramDataNode;
import com.dse.testdata.object.TemplateSubprogramDataNode;
import com.dse.testdata.object.UnitNode;
import com.dse.thread.AkaThreadManager;
import com.dse.util.CompilerUtils;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UIController {
    public final static String MODE_VIEW = "MODE_VIEW";
    public final static String MODE_EDIT = "MODE_EDIT";
    public final static String TITLE_BUILD_ENV = "Build Environment";
    public final static String TITLE_UPDATE_ENV = "Update Environment";
    final static AkaLogger logger = AkaLogger.get(UIController.class);
    private static Stage primaryStage = null;
    private static Stage environmentBuilderStage = null;
    private static String mode = UIController.MODE_VIEW;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getEnvironmentBuilderStage() {
        return environmentBuilderStage;
    }

    public static void setEnvironmentBuilderStage(Stage stage) {
        environmentBuilderStage = stage;
    }

    public static void shutdown() {
        // Shut down all automated test data generation threads
        new AkaConfig().fromJson().removeOpeningWorkspaces().exportToJson();
        AkaThreadManager.stopAutomatedTestdataGenerationForAll(Environment.getInstance().getProjectNode());
        System.exit(0);
    }

    /**
     * Create clone project
     *
     * @return
     * @throws IOException
     */
    public static File createCloneProject(String path) {
        String cloneProjectPath;
        /*
         * Ten hien tai dang /tmp/cloneProject_0.
         */
        try {
            String property = "java.io.tmpdir";
            cloneProjectPath = System.getProperty(property);

            // Create unique name for clone project
            cloneProjectPath = FilenameUtils.concat(cloneProjectPath, Paths.CURRENT_PROJECT.CLONE_PROJECT_NAME);

            int cloneProjectId = 0;
            cloneProjectPath += "_";
            while (new File(cloneProjectPath + cloneProjectId).exists())
                cloneProjectId++;

            cloneProjectPath += Integer.toString(cloneProjectId);

            // Clone project
            Utils.copy(new File(Paths.CURRENT_PROJECT.ORIGINAL_PROJECT_PATH), new File(cloneProjectPath));

            // Set "chmod 777" for the clone project
            new File(Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH).setExecutable(true);
            new File(Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH).setReadable(true);
            new File(Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH).setWritable(true);
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }

        return new File(cloneProjectPath);
    }

    /**
     * @return true if we need to parse the original project; false otherwise
     * @throws IOException
     */
    public static boolean shouldParseTheOriginalProject(int typeofProject) {
        switch (typeofProject) {
            case IProjectType.PROJECT_ECLIPSE: {
                return true;
            }
            case IProjectType.PROJECT_DEV_CPP:
            case IProjectType.PROJECT_UNKNOWN_TYPE:
            case IProjectType.PROJECT_CUSTOMMAKEFILE:
            case IProjectType.PROJECT_VISUALSTUDIO:
            case IProjectType.PROJECT_CODEBLOCK:
            default: {
                return false;
            }
        }
    }

    public static void viewPrototype(TestPrototype prototype) {
        AnchorPane tab = Factory.generateTestcaseTab(prototype);
        MDIWindowController.getMDIWindowController().viewPrototype(tab, prototype.getName());
    }

    public static void viewTestCase(TestCase testCase) {
        AnchorPane tab = Factory.generateTestcaseTab(testCase);
        MDIWindowController.getMDIWindowController().viewTestCase(tab, testCase.getName());
    }

    public static void viewTestCase(CompoundTestCase compoundTestCase) {
        Tab tab = Factory.generateCompoundTestCaseTab(compoundTestCase);
        MDIWindowController.getMDIWindowController().viewCompoundTestCase(tab, compoundTestCase.getName());
    }

    public static ISourcecodeFileNode searchSourceCodeFileNodeByPath(TestUnitNode unitNode, ProjectNode projectNode) {
        if (unitNode.getSrcNode() == null) {
            List<INode> sourcecodeNodes = Search.searchNodes(projectNode, new SourcecodeFileNodeCondition());
            INode dataUserCode = Environment.getInstance().getUserCodeRoot();
            sourcecodeNodes.addAll(Search.searchNodes(dataUserCode, new SourcecodeFileNodeCondition()));
            for (INode sourcecodeNode : sourcecodeNodes) {
                if (sourcecodeNode instanceof ISourcecodeFileNode) {
                    if (PathUtils.equals(sourcecodeNode.getAbsolutePath(), unitNode.getName())) {
                        unitNode.setSrcNode((ISourcecodeFileNode) sourcecodeNode);
                        break;
                    }
                }
            }
        }
        return unitNode.getSrcNode();
    }

    public static ICommonFunctionNode searchFunctionNodeByPathInBackupEnvironment(String path) throws FunctionNodeNotFoundException {
        ICommonFunctionNode matchedFunctionNode;

        // create conditions to search (both complete function & prototype function).
        List<INode> functionNodes = new ArrayList<>();
        functionNodes.addAll(Search.searchNodes(Environment.getBackupEnvironment().getProjectNode(), new AbstractFunctionNodeCondition()));
        functionNodes.addAll(Search.searchNodes(Environment.getBackupEnvironment().getProjectNode(), new DefinitionFunctionNodeCondition()));
        functionNodes.addAll(Search.searchNodes(Environment.getBackupEnvironment().getProjectNode(), new MacroFunctionNodeCondition()));

        SystemLibraryRoot libraryRoot = Environment.getBackupEnvironment().getSystemLibraryRoot();
        if (libraryRoot != null)
            functionNodes.addAll(Search.searchNodes(libraryRoot, new AbstractFunctionNodeCondition()));

        INode dataRoot = Environment.getBackupEnvironment().getUserCodeRoot();
        if (dataRoot != null)
            functionNodes.addAll(Search.searchNodes(dataRoot, new AbstractFunctionNodeCondition()));

        for (INode functionNode : functionNodes) {
            if (functionNode instanceof ICommonFunctionNode) {
                if (PathUtils.equals(functionNode.getAbsolutePath(), path)) {
                    matchedFunctionNode = (ICommonFunctionNode) functionNode;
                    return matchedFunctionNode;
                }
            }
        }

        throw new FunctionNodeNotFoundException(path);
    }

    public static ICommonFunctionNode searchFunctionNodeByPath(String path) throws FunctionNodeNotFoundException {
        ICommonFunctionNode matchedFunctionNode;

        // create conditions to search (both complete function & prototype function).
        List<INode> functionNodes = new ArrayList<>();
        functionNodes.addAll(Search.searchNodes(Environment.getInstance().getProjectNode(), new AbstractFunctionNodeCondition()));
        functionNodes.addAll(Search.searchNodes(Environment.getInstance().getProjectNode(), new DefinitionFunctionNodeCondition()));
        functionNodes.addAll(Search.searchNodes(Environment.getInstance().getProjectNode(), new MacroFunctionNodeCondition()));
        functionNodes.addAll(Search.searchNodes(Environment.getInstance().getProjectNode(), new LambdaFunctionNodeCondition()));

        SystemLibraryRoot libraryRoot = Environment.getInstance().getSystemLibraryRoot();
        if (libraryRoot != null)
            functionNodes.addAll(Search.searchNodes(libraryRoot, new AbstractFunctionNodeCondition()));

        INode dataRoot = Environment.getInstance().getUserCodeRoot();
        if (dataRoot != null)
            functionNodes.addAll(Search.searchNodes(dataRoot, new AbstractFunctionNodeCondition()));

        for (INode functionNode : functionNodes) {
            if (functionNode instanceof ICommonFunctionNode) {
                if (PathUtils.equals(functionNode.getAbsolutePath(), path)) {
                    matchedFunctionNode = (ICommonFunctionNode) functionNode;
                    return matchedFunctionNode;
                }
            }
        }


//        int lastFileSeparator = path.lastIndexOf(File.separatorChar);
//        if (lastFileSeparator > 0) {
//            String prototype = path.substring(lastFileSeparator + 1);
//
//            IASTNode astNode = Utils.convertToIAST(prototype);
//
//            if (astNode instanceof IASTDeclarationStatement) {
//                astNode = ((IASTDeclarationStatement) astNode).getDeclaration();
//            }
//
//            if (astNode instanceof CPPASTSimpleDeclaration) {
//                DefinitionFunctionNode functionNode = new DefinitionFunctionNode();
//                functionNode.setAbsolutePath(path);
//                functionNode.setAST((CPPASTSimpleDeclaration) astNode);
//                functionNode.setName(functionNode.getNewType());
//                return functionNode;
//            }
//        }

        throw new FunctionNodeNotFoundException(path);
    }

    public static void refreshCompoundTestcaseViews() {
        MDIWindowController mdiWindowController = MDIWindowController.getMDIWindowController();
        Map<String, CompoundTestCaseTreeTableViewController> map = mdiWindowController.getCompoundTestCaseControllerMap();
        for (CompoundTestCaseTreeTableViewController controller : map.values()) {
            controller.refresh();
        }
    }

    public static void viewCoverageOfMultipleTestcases(String functionName, List<TestCase> testCases) {
        MDIWindowController.getMDIWindowController().viewCoverageOfMultipleTestcase(functionName, testCases);
    }

    public static void viewCoverageOfMultipleTestcasesFromAnotherThread(String functionName, List<TestCase> testCases) {
        Platform.runLater(() -> viewCoverageOfMultipleTestcases(functionName, testCases));
    }

    public static void viewCoverageOfATestcase(TestCase testCase) {
        String tabName = testCase.getName();
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(testCase);
        MDIWindowController.getMDIWindowController().viewCoverageOfMultipleTestcase(tabName, testCases);
    }

    public static void viewSourceCode(INode node) {
        // Get the source code file node
        INode sourcecodeFileNode = node;

        while (!(sourcecodeFileNode instanceof ISourcecodeFileNode) && sourcecodeFileNode != null) {
            sourcecodeFileNode = sourcecodeFileNode.getParent();
        }

        if (sourcecodeFileNode != null) {
            // Set the option of viewing source code
            FXFileView fileView = new FXFileView(node);
            MDIWindowController.getMDIWindowController().viewSourceCode(sourcecodeFileNode, fileView);
        }
    }

//    public static void viewCte(INode node, TestNormalSubprogramNode testNormalSubprogramNode) throws Exception {
//        INode sourcecodeFileNode = node;
//        IFunctionNode Fnode = (IFunctionNode) node;
//
//        while (!(sourcecodeFileNode instanceof ISourcecodeFileNode) && sourcecodeFileNode != null) {
//            sourcecodeFileNode = sourcecodeFileNode.getParent();
//        }
//        if (sourcecodeFileNode != null) {
//            // Set the option of viewing source code
//            CteController cteController = new CteController(Fnode, testNormalSubprogramNode);
//            MDIWindowController.getMDIWindowController().viewClassificationTree(Fnode, cteController);
//        }
//    }

    public static void viewCte(IFunctionNode Fnode, CteController cteController) throws Exception {

        MDIWindowController.getMDIWindowController().viewClassificationTree(Fnode, cteController);

    }

    public static ISourcecodeFileNode searchSourceCodeFileNodeByPath(String path) {
        ISourcecodeFileNode matchedNode;

        // create conditions to search (both complete function & prototype function).
        List<INode> nodes = new ArrayList<>(Search.searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition()));

        for (INode node : nodes) {
            if (node instanceof ISourcecodeFileNode) {
                if (PathUtils.equals(node.getAbsolutePath(), path)) {
                    matchedNode = (ISourcecodeFileNode) node;
                    return matchedNode;
                }
            }
        }

        return null;
    }

//    public static void debugAndGetLog(ITestCase testCase) {
//        SE se = new SE();
//        se.setExe(testCase.getExecutableFile());
//        se.setTestdriver(testCase.getSourceCodeFile());
//        se.setSrc2("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm/gb.Utils.akaignore.cpi");
//        try {
//            se.compile();
//            se.debug(se.getExe());
//        } catch (InterruptedException | IOException | NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }

    public static void openTheLocation(INode node) throws OpenFileException {
        assert (node != null);

        if (node instanceof FolderNode || node instanceof ISourcecodeFileNode || node instanceof ProjectNode || node instanceof UnknowObjectNode) {
            String path = node.getAbsolutePath();
            Utils.openFolderorFileOnExplorer(path);
        }
    }

    public static void loadTestCasesNavigator(File testcasesScript) {
        // load test cases script to Environment
        Environment.getInstance().loadTestCasesScript(testcasesScript);
        ITestcaseNode root = Environment.getInstance().getTestcaseScriptRootNode();
        LeftPaneController.getLeftPaneController().renderNavigator(root);
    }

    public static void loadProjectStructureTree(IProjectNode root) {
        LeftPaneController.getLeftPaneController().renderProjectTree(root);
    }

    public static void clear() {
        BaseSceneController.getBaseSceneController().clear();
        UILogger.reinitializeUiLogger();
    }

    public static void executeTestCaseWithDebugMode(ITestCase testCase) {
        MDIWindowController.getMDIWindowController().viewDebug(testCase);
    }

    public static void newCCPPEnvironment() {
        Environment.createNewEnvironment();
        showEnvironmentBuilderWindow(TITLE_BUILD_ENV);
    }

    public static void updateCCPPEnvironment() {
        // clone the current environment to modify for updating
        Environment.backupEnvironment();
        BaseController.resetStatesInAllWindows();
        showEnvironmentBuilderWindow(TITLE_UPDATE_ENV);
    }

    public static void loadProbePoints() {
        ProbePointManager.getInstance().clear();
        ProbePointManager.getInstance().loadProbePoints();
        MDIWindowController.getMDIWindowController().updateLVProbePoints();
    }

    public static void loadRegressionScripts() {
        RegressionScriptManager.getInstance().clear();
        RegressionScriptManager.getInstance().loadRegressionScripts();
    }

    private static void showEnvironmentBuilderWindow(String title) {
        Scene scene = BaseController.getBaseScene();
        environmentBuilderStage = new Stage();
        environmentBuilderStage.setTitle(title);
        environmentBuilderStage.setResizable(false);
        environmentBuilderStage.setScene(scene);
        environmentBuilderStage.setOnCloseRequest(event -> {
            if (title.equals(TITLE_BUILD_ENV)) {
                Environment.WindowState.isSearchListNodeUpdated(false);
                Environment.saveTempEnvironment();
            } else {
                BaseController.resetStatesInAllWindows();
            }
            Environment.restoreEnvironment();
        });

        // block the primary window
        environmentBuilderStage.initModality(Modality.WINDOW_MODAL);
        environmentBuilderStage.initOwner(getPrimaryStage().getScene().getWindow());

        environmentBuilderStage.show();
    }

    public static void showErrorDialog(String content, String title, String headText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
            Label label = new Label(content);
            label.setWrapText(true);
            alert.getDialogPane().setContent(label);
            alert.setTitle(title);
            alert.setHeaderText(headText);
            alert.initOwner(getPrimaryStage());
            alert.showAndWait();
        });
    }

    public static void showDetailDialog(Alert.AlertType type, String title, String headText, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(headText);

            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setMinHeight(350);
            textArea.setText(content);

            alert.getDialogPane().setContent(textArea);
            alert.initOwner(getPrimaryStage());

            alert.showAndWait();
        });
    }

    public static void showDetailDialogInMainThread(Alert.AlertType type, String title, String headText, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headText);

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setMinHeight(350);
        textArea.setText(content);

        alert.getDialogPane().setContent(textArea);

        alert.showAndWait();
    }

    public static void showSuccessDialog(String content, String title, String headText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(headText);
            alert.initOwner(getPrimaryStage());
            alert.showAndWait();
        });
    }

    public static void showInformationDialog(String content, String title, String headText) {
        showSuccessDialog(content, title, headText);
    }

    public static Alert showYesNoDialog(Alert.AlertType type, String title, String headText, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headText);

        double width = alert.getDialogPane().getWidth();
        double padding = 10;
        Text text = new Text(content);
        text.setWrappingWidth(width - padding * 2);
        alert.getDialogPane().setPadding(new Insets(padding));
        alert.getDialogPane().setContent(text);

        ButtonType okButton = ButtonType.YES;
        ButtonType noButton = ButtonType.NO;
        alert.getButtonTypes().setAll(okButton, noButton);

//        alert.getDialogPane().getScene().getWindow().sizeToScene();

        alert.initOwner(getPrimaryStage());

        return alert;
    }

    public static void showMacroTypeDefinitionDialog(MacroSubprogramDataNode macroSubprogram,
                                                     ParameterColumnCellFactory.ParameterColumnCell cell) {

        MacroFunctionNode functionNode = (MacroFunctionNode) macroSubprogram.getFunctionNode();

        Alert alert = new TypeDeclarationDialog(functionNode) {
            @Override
            public void onOkClick(Map<String, String> typeMap) throws Exception {
                macroSubprogram.getChildren().clear();
                macroSubprogram.setRealFunctionNode(typeMap);
                macroSubprogram.setRealTypeMapping(typeMap);
                cell.refresh();
            }

            @Override
            public ICompileMessage onTestCompile(Map<String, String> typeMap) {
                UnitNode unitNode = macroSubprogram.getUnit();
                INode context = unitNode.getSourceNode();

                String prototype = macroSubprogram.generatePrototype(typeMap);

                logger.debug("Macro Prototype: " + prototype);

                ICompileMessage compileMessage = CompilerUtils.testCompile(typeMap.get("RETURN"), prototype, functionNode, context);
                if (compileMessage.getType() == ICompileMessage.MessageType.ERROR)
                    UIController.showDetailDialog(AlertType.ERROR, "Compilation message",
                            "Fail", compileMessage.getMessage());
                else
                    UIController.showSuccessDialog("The source code file "
                                    + Utils.getSourcecodeFile(functionNode).getAbsolutePath() + " is compile successfully with the given types",
                            "Compilation message", "Success");
                return compileMessage;
            }
        };

        Platform.runLater(alert::showAndWait);
    }

    public static void showTemplateTypeDefinitionDialog(TemplateSubprogramDataNode templateSubprogram,
                                                        ParameterColumnCellFactory.ParameterColumnCell cell) {

        ICommonFunctionNode functionNode = (ICommonFunctionNode) templateSubprogram.getFunctionNode();

//        final String[] prototype = {SpecialCharacter.EMPTY};

        Alert alert = new TypeDeclarationDialog(functionNode) {
//            Map<String, String> typeMap;
//            private String prototype = SpecialCharacter.EMPTY;

            @Override
            public void onOkClick(Map<String, String> typeMap) throws Exception {
                templateSubprogram.getChildren().clear();
                String prototype = generatePrototype(typeMap);
                templateSubprogram.setRealFunctionNode(prototype);
                templateSubprogram.setRealTypeMapping(typeMap);
                cell.refresh();
            }

            private String generatePrototype(Map<String, String> typeMap) {
                StringBuilder builder = new StringBuilder();
                String returnType = functionNode.getReturnType();
                builder.append(returnType)
                        .append(SpecialCharacter.SPACE)
                        .append("AKA_TEMPLATE_")
                        .append(functionNode.getSingleSimpleName())
                        .append("(");

                for (IVariableNode arg : functionNode.getArguments()) {
                    String varType = arg.getRawType();
                    String varName = arg.getName();
                    String variableDefinition = Utils.generateVariableDeclaration(varType, varName);
                    builder.append(variableDefinition).append(", ");
                }

                builder.append(")");
                String prototype = builder.toString();
                prototype = prototype.replace(", )", ")");

                for (Map.Entry<String, String> entry : typeMap.entrySet()) {
                    prototype = prototype.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
                }

                return prototype;
            }

            @Override
            public ICompileMessage onTestCompile(Map<String, String> typeMap) {
//                this.typeMap = typeMap;
                String returnType = functionNode.getReturnType();
                String prototype = generatePrototype(typeMap);

                UnitNode unitNode = templateSubprogram.getUnit();
                INode context = unitNode.getSourceNode();

                ICompileMessage compileMessage = CompilerUtils.testCompile(returnType, prototype, functionNode, context);
                logger.debug("Compile Message: " + compileMessage);
                if (compileMessage.getType() == ICompileMessage.MessageType.ERROR)
                    UIController.showDetailDialog(AlertType.ERROR, "Compilation message",
                            "Fail", compileMessage.getMessage());
                else
                    UIController.showSuccessDialog("The source code file "
                                    + Utils.getSourcecodeFile(functionNode).getAbsolutePath() + " is compile successfully with the given types",
                            "Compilation message", "Success");
                return compileMessage;
            }
        };

        Platform.runLater(alert::showAndWait);
    }

    public static void showArrayExpanderDialog(ParameterColumnCellFactory.ParameterColumnCell cell) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle("Expand Array Item");
            DataNode dataNode = cell.getTreeTableRow().getTreeItem().getValue();
            alert.setHeaderText("Expand children of " + dataNode.getName() + " by index");

            TextField textField = new TextField();
            Hint.tooltipNode(textField, "Example: [0], [1][2] or [0..1][2]");
            alert.getDialogPane().setContent(textField);

            final Button btnOk = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            btnOk.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    cell.expandArrayItems(textField.getText());
                } catch (Exception e) {
                    showErrorDialog("Error: " + e.getMessage(), "Invalid input", "Something wrong with input");
//                    e.printStackTrace();/**/
                }
            });

            alert.initOwner(getPrimaryStage());
            alert.showAndWait();
        });
    }

    public static List<ITestcaseNode> filterSubprograms(List<ITestcaseNode> subprograms) {
        subprograms.removeIf(subprogram -> {
            ICommonFunctionNode function;

            try {
                function = UIController
                        .searchFunctionNodeByPath(((TestNormalSubprogramNode) subprogram).getName());

                if (!Environment.getInstance().isOnWhiteBoxMode()) {
                    if (function.getVisibility() != ICPPASTVisibilityLabel.v_public)
                        return true;

                    if (function instanceof AbstractFunctionNode) {
                        INode realParent = ((AbstractFunctionNode) function).getRealParent();
                        if (realParent == null) realParent = function.getParent();

                        if (realParent instanceof StructOrClassNode) {
                            if (((StructOrClassNode) realParent).getVisibility() != ICPPASTVisibilityLabel.v_public)
                                return true;
                        }
                    }
                }

                return function.getParent() instanceof ICommonFunctionNode;

            } catch (FunctionNodeNotFoundException e) {
                showErrorDialog("Do not found function " + ((TestNormalSubprogramNode) subprogram).getName(), "Function Not Found", "FunctionNodeNotFoundException");
                return true;
            }
        });

        return subprograms;
    }

    public static void insertIncludesForProbePoints(ISourcecodeFileNode sourcecodeFileNode) {
        List<ProbePoint> probePoints = ProbePointManager.getInstance()
                .searchProbePoints(sourcecodeFileNode);
        // only need to insert include for probe point that be applied to a test case
        probePoints.removeIf(pp -> pp.getTestCases().size() == 0);

        Collections.sort(probePoints);
        String filePath = ProjectClone.getClonedFilePath(sourcecodeFileNode.getAbsolutePath());
        List<String> content = ProbePointUtils.readData(filePath);

        for (int pos = 0; pos < probePoints.size(); pos++) {
            ProbePoint probePoint = probePoints.get(pos);
            AbstractFunctionNode functionNode = (AbstractFunctionNode) probePoint.getFunctionNode();

            int lineInIgnore = new ProjectCloneMap(filePath).getLineInFunction(
                    functionNode, probePoint.getLineInSourceCodeFile());
            int preEqualNum = 0;
            int lineInSource = probePoint.getLineInSourceCodeFile();
            for (int i = pos - 1; i >= 0; i--) {
                ProbePoint temp = probePoints.get(i);
                if (temp.getLineInSourceCodeFile() == lineInSource) {
                    preEqualNum++;
                } else if (temp.getLineInSourceCodeFile() < lineInSource)
                    break;
            }
            int before = lineInIgnore + 2 * pos - preEqualNum;
            int after = before + preEqualNum + 2;

            String includeBefore = "#include " + "\"" + probePoint.getBefore() + "\"";
            String includeAfter = "#include " + "\"" + probePoint.getAfter() + "\"";
            content.add(before, includeBefore);
            content.add(after, includeAfter);

            System.out.println(probePoint.getName() + "(liignore: " + lineInIgnore + ")"
                    + " - b: " + before + " - a: " + after);
        }

        String newContent = String.join("\n", content);
        Utils.writeContentToFile(newContent, filePath);
    }

    public static void insertIncludesForProbePoints() {
        for (String sourcePath : ProbePointManager.getInstance().getProbePointMap().keySet()) {
            ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath(sourcePath);
            if (sourcecodeFileNode != null) {
                UIController.insertIncludesForProbePoints(sourcecodeFileNode);
            }
        }
    }

    public static String getMode() {
        return mode;
    }

    public static void setMode(String mode) {
        UIController.mode = mode;
    }

    public static void updateActiveSourceCodeTabs() {
        Environment.getInstance().getActiveSourcecodeTabs().clear();
        Map<Tab, INode> activeSourcecodeTabs = Environment.getBackupEnvironment().getActiveSourcecodeTabs();
        for (Tab tab : activeSourcecodeTabs.keySet()) {
            ISourcecodeFileNode fileNode = UIController.searchSourceCodeFileNodeByPath(
                    activeSourcecodeTabs.get(tab).getAbsolutePath());
            ((SourceCodeViewTab) tab).setSourceCodeFileNode(fileNode);
            Environment.getInstance().getActiveSourcecodeTabs().put(tab, fileNode);
        }
    }

    public static String chooseDirectoryOfNewFile(String extension) {
        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().addAll(//
//                new FileChooser.ExtensionFilter("All Files", "*.*"),
//                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
//                new FileChooser.ExtensionFilter("PNG", "*.png"));
        fileChooser.setTitle("File Chooser");
        String fileType = extension.replace(SpecialCharacter.DOT_IN_STRUCT, SpecialCharacter.EMPTY);
        String desc = String.format("%s files (*%s)", fileType, extension);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, "*" + extension));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(desc, "*" + extension));
//        fileChooser.setInitialFileName(fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
        Stage stage = new Stage();
        File saveFile = fileChooser.showSaveDialog(stage);
        if (saveFile == null) {
            return null;
        }

        String path = saveFile.getAbsolutePath();

        if (!path.endsWith(extension)) {
            path += extension;
        }

        return path;
    }

    public static void addOnFocusListener(Stage stage) {
//        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent mouseEvent) {
//                if (Environment.getInstance() != null && Environment.getInstance().getEnvironmentRootNode() != null) {
//                    String environmentFilePath = Environment.getInstance().getEnvironmentRootNode().getEnvironmentScriptPath();
//
//                    if (environmentFilePath != null) {
//                        AkaConfig localConfig = new AkaConfig().fromJson();
//                        String localWorkspace = localConfig.getOpeningWorkspaceDirectory();
//                        logger.debug("local workspace: " + localWorkspace);
//
//                        String workspace = environmentFilePath.replace(WorkspaceConfig.ENV_EXTENSION, SpecialCharacter.EMPTY);
//                        logger.debug("current workspace: " + workspace);
//
//                        if (!localWorkspace.equals(workspace)) {
//                            resetAkaConfig();
//                        }
//                    }
//                }
//            }
//        });
        stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!oldValue && newValue) {
                    resetAkaConfig();
                }
            }
        });
    }

    private static void resetAkaConfig() {
        if (Environment.getInstance() != null && Environment.getInstance().getEnvironmentRootNode() != null) {
            String environmentFilePath = Environment.getInstance().getEnvironmentRootNode().getEnvironmentScriptPath();

            if (environmentFilePath != null) {
                AkaConfig akaConfig = new AkaConfig().fromJson();
                String prevOpeningWorkspace = akaConfig.getOpeningWorkspaceDirectory();
                String workspace = environmentFilePath.replace(WorkspaceConfig.ENV_EXTENSION, SpecialCharacter.EMPTY);

                if (!workspace.equals(prevOpeningWorkspace)) {
                    File environmentFile = new File(environmentFilePath);
                    String newWorkingDirectory = environmentFile.getParentFile().getAbsolutePath();

                    akaConfig.setWorkingDirectory(newWorkingDirectory);
                    logger.debug("Setup the working directory at " + newWorkingDirectory);

                    akaConfig.setOpeningWorkspaceDirectory(workspace);
                    akaConfig.addOpeningWorkspaces(workspace);
                    logger.debug("Setup the workspace directory at " + workspace);

                    String workspaceConfig = workspace + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;
                    akaConfig.setOpenWorkspaceConfig(workspaceConfig);
                    logger.debug("Setup the workspace config at " + workspaceConfig);

                    akaConfig.exportToJson();
                }
            }
        }
    }
}
