package com.dse.guifx_v3.helps;

import com.dse.config.FunctionConfig;
import com.dse.config.FunctionConfigDeserializer;
import com.dse.config.FunctionConfigSerializer;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.CoverageManager;
import com.dse.coverage.CoverageDataObject;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.*;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.controllers.object.build_environment.UnitNamesPath;
import com.dse.parser.object.*;
import com.dse.report.FullReport;
import com.dse.report.ReportManager;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcasescript.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import com.dse.boundary.BoundOfDataTypes;
import com.dse.boundary.BoundOfDataTypesDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dse.coverage.gcov.GcovInfo.changeReportContent;

public class Factory {
    private final static AkaLogger logger = AkaLogger.get(Factory.class);

    public static ImageView getIcon(INode node) {
        Image icon = null;
        if (node instanceof CppFileNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/cppfile.png"));
        }
        if (node instanceof CFileNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/cfile.png"));
        }
        if (node instanceof ICommonFunctionNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/subprogram.png"));
        }
        if (node instanceof IProjectNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        }
        if (node instanceof FolderNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        }
        if (node instanceof ClassNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/class.png"));
        }
        if (node instanceof HeaderNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/header.png"));
        }
        return new ImageView(icon);
    }

    public static ImageView getIcon(ITestcaseNode node) {
        Image icon = null;
        if (node instanceof TestcaseRootNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        } else if (node instanceof TestCompoundSubprogramNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        } else if (node instanceof TestUnitNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        } else if (node instanceof TestInitSubprogramNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        } else if (node instanceof TestNormalSubprogramNode) {
            icon = new Image(Factory.class.getResourceAsStream("/icons/subprogram.png"));
        } else if (node instanceof TestNewNode) {
            if (((TestNewNode) node).isPrototypeTestcase())
                icon = new Image(Factory.class.getResourceAsStream("/icons/prototype.png"));
            else
                icon = new Image(Factory.class.getResourceAsStream("/icons/file.png"));

        } else
            icon = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
        return new ImageView(icon);
    }

    public static ImageView getIcon(UnitNamesPath unit) {
        Image icon = new Image(Factory.class.getResourceAsStream("/icons/arrow_chosen_unit.png"));
        return new ImageView(icon);
    }

    public static ImageView getIcon(BoundOfDataTypes boundOfDataTypes) {
        Image icon = new Image(Factory.class.getResourceAsStream("/icons/arrow_chosen_unit.png"));
        return new ImageView(icon);
    }

    static Tab generateCompoundTestCaseTab(CompoundTestCase compoundTestCase) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/CompoundTestCaseTreeTableView.fxml"));
        try {
            Tab tab = new Tab(compoundTestCase.getName());
            AnchorPane treeTable = loader.load();
            CompoundTestCaseTreeTableViewController controller = loader.getController();
            controller.loadContent(compoundTestCase);
            controller.setCompoundTestCase(compoundTestCase);

            // add controller to MDIWindow's map to refresh as need
            MDIWindowController mdiWindowController = MDIWindowController.getMDIWindowController();
            Map<String, CompoundTestCaseTreeTableViewController> controllerMap = mdiWindowController.getCompoundTestCaseControllerMap();
            controllerMap.put(compoundTestCase.getName(), controller);

            tab.setContent(treeTable);
            tab.setOnSelectionChanged(event -> {
                if (tab.isSelected()) {
                    Environment.getInstance().setCurrentTestcompoundController(controller);
                }
            });
            return tab;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    public static void generateTestCasesExecutionTab(ICommonFunctionNode functionNode) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/TestCasesExecutionTab.fxml"));
        try {
            AnchorPane tabContent = loader.load();
            TestCasesExecutionTabController controller = loader.getController();

            // add to TCExecutionDetailLogger's map
            TCExecutionDetailLogger.addTestCasesExecutionTabController((FunctionNode) functionNode, controller);

            Tab tab = new Tab(functionNode.getSingleSimpleName());
            tab.setContent(tabContent);
            controller.setTab(tab);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public static AnchorPane generateTestcaseTab(IDataTestItem testCase) {
        FXMLLoader treeTableLoader = new FXMLLoader(Object.class.getResource("/FXML/TestCaseTreeTableView.fxml"));
        FXMLLoader testCaseTabLoader = new FXMLLoader(Object.class.getResource("/FXML/TestCaseTab.fxml"));
        try {
            AnchorPane treeTable = treeTableLoader.load();
            TestCaseTreeTableController testCaseTreeTableController = treeTableLoader.getController();
            testCaseTreeTableController.loadTestCase(testCase);
            AnchorPane testCaseTab = testCaseTabLoader.load();
            TestCaseTabController testCaseTabController = testCaseTabLoader.getController();
            testCaseTabController.setTestcaseTreeTable(treeTable);
            testCaseTabController.setTestCase(testCase);
            testCaseTabController.loadContent();

            return testCaseTab;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AnchorPane generateDataInsert(IDataTestItem testCase)
    {
        FXMLLoader treeTableLoader = new FXMLLoader(Object.class.getResource("/FXML/TestCaseTreeTableView.fxml"));
       // FXMLLoader testCaseTabLoader = new FXMLLoader(Object.class.getResource("/FXML/TestCaseTab.fxml"));
        try {
            AnchorPane treeTable = treeTableLoader.load();
            TestCaseTreeTableController testCaseTreeTableController = treeTableLoader.getController();
            testCaseTreeTableController.loadTestCase(testCase);
            //AnchorPane testCaseTab = testCaseTabLoader.load();
            //TestCaseTabController testCaseTabController = testCaseTabLoader.getController();
           //testCaseTabController.setTestcaseTreeTable(treeTable);
           // testCaseTabController.setTestCase(testCase);
           // testCaseTabController.loadContent();

            return treeTable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Tab generateCoverageTabView(String tabName, int line, List<CoverageDataObject> coverageDatas, List<CoverageDataObject> funcCovDatas) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/CoverageViewTab.fxml"));
        try {
            AnchorPane view = loader.load();
            CoverageViewTabController controller = loader.getController();
            controller.setLine(line);

            if (coverageDatas.size() == 1) {
                controller.loadContentToCoverageViewInTab1(Environment.getInstance().getTypeofCoverage(), coverageDatas.get(0).getContent());

                float progress = coverageDatas.get(0).getProgress();
                controller.updateProgress(progress);
                controller.updateProgressForFunctionLevel(funcCovDatas.get(0).getProgress());
                controller.updateProgressDetail(coverageDatas.get(0).getVisited() + "/" + coverageDatas.get(0).getTotal());
                controller.updateProgressDetailForFunctionLevel(funcCovDatas.get(0).getVisited() + "/" + funcCovDatas.get(0).getTotal());
            } else if (coverageDatas.size() == 2) {
                controller.loadContentToCoverageViewInTab1(EnviroCoverageTypeNode.STATEMENT, coverageDatas.get(0).getContent());

                float progress = coverageDatas.get(0).getProgress();
                controller.updateProgress(progress);
                controller.updateProgressForFunctionLevel(funcCovDatas.get(0).getProgress());
                controller.updateProgressDetail(coverageDatas.get(0).getVisited() + "/" + coverageDatas.get(0).getTotal());
                controller.updateProgressDetailForFunctionLevel(funcCovDatas.get(0).getVisited() + "/" + funcCovDatas.get(0).getTotal());

                if (Environment.getInstance().getTypeofCoverage().endsWith(EnviroCoverageTypeNode.BRANCH)) {
                    controller.loadContentToCoverageViewInTab2(EnviroCoverageTypeNode.BRANCH, coverageDatas.get(1).getContent());

                } else {
                    controller.loadContentToCoverageViewInTab2(EnviroCoverageTypeNode.MCDC, coverageDatas.get(1).getContent());
                }

                float progressInTab2 = coverageDatas.get(1).getProgress();
                controller.updateProgressInTab2(progressInTab2);
                controller.updateProgressFunctionInTab2(funcCovDatas.get(1).getProgress());
                controller.updateProgressDetailInTab2(coverageDatas.get(1).getVisited() + "/" + coverageDatas.get(1).getTotal());
                controller.updateProgressDetailFunctionInTab2(funcCovDatas.get(1).getVisited() + "/" + funcCovDatas.get(1).getTotal());
            }

            Tab tab = new Tab(tabName);
            tab.setContent(view);
            return tab;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * There are three cases:
     * <p>
     * Case 1: All test cases are in the same function
     * <p>
     * Case 2: Test cases are in the different functions -> can not compute coverage
     *
     * @param tabName
     * @param testCases
     * @return
     */
    @Deprecated
    public static Tab generateCoverageTab(String tabName, List<TestCase> testCases) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/CoverageViewTab.fxml"));
        try {
            AnchorPane view = loader.load();
            CoverageViewTabController controller = loader.getController();

            switch (Environment.getInstance().getTypeofCoverage()) {
                case EnviroCoverageTypeNode.BASIS_PATH: {
                    // compute coverage at file level
                    CoverageDataObject coverageDataObject = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            Environment.getInstance().getTypeofCoverage());
                    if (coverageDataObject != null) {
                        controller.loadContentToCoverageViewInTab1("BasisPath Coverage", coverageDataObject.getContent());

                        float progress = coverageDataObject.getProgress();
                        controller.updateProgress(progress);
                        controller.updateProgressDetail(coverageDataObject.getVisited() + "/" + coverageDataObject.getTotal());
                    }
                    break;
                }

                case EnviroCoverageTypeNode.BRANCH: {
                    // compute coverage at file level
                    CoverageDataObject coverageDataObject = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            Environment.getInstance().getTypeofCoverage());
                    if (coverageDataObject != null) {
                        controller.loadContentToCoverageViewInTab1("Branch Coverage", coverageDataObject.getContent());

                        float progress = coverageDataObject.getProgress();
                        controller.updateProgress(progress);
                        controller.updateProgressDetail(coverageDataObject.getVisited() + "/" + coverageDataObject.getTotal());
                    }
                    break;
                }

                case EnviroCoverageTypeNode.STATEMENT: {
                    // compute coverage at file level
                    CoverageDataObject coverageDataObject = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            Environment.getInstance().getTypeofCoverage());

                    if (coverageDataObject != null) {
                        controller.loadContentToCoverageViewInTab1("Statement Coverage", coverageDataObject.getContent());

                        float progress = coverageDataObject.getProgress();
                        controller.updateProgress(progress);
                        controller.updateProgressDetail(coverageDataObject.getVisited() + "/" + coverageDataObject.getTotal());
                    }
                    break;
                }
                case EnviroCoverageTypeNode.MCDC: {
                    // compute coverage at file level
                    CoverageDataObject coverageDataObject = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            Environment.getInstance().getTypeofCoverage());
                    if (coverageDataObject != null) {
                        controller.loadContentToCoverageViewInTab1("MCDC Coverage", coverageDataObject.getContent());

                        float progress = coverageDataObject.getProgress();
                        controller.updateProgress(progress);
                        controller.updateProgressDetail(coverageDataObject.getVisited() + "/" + coverageDataObject.getTotal());
                    }
                    break;
                }

                case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                    // tab coverage 1
                    CoverageDataObject coverageDataObject = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            EnviroCoverageTypeNode.STATEMENT);
                    if (coverageDataObject != null) {
                        controller.loadContentToCoverageViewInTab1("Statement Coverage", coverageDataObject.getContent());

                        float progress = coverageDataObject.getProgress();
                        controller.updateProgress(progress);
                        controller.updateProgressDetail(coverageDataObject.getVisited() + "/" + coverageDataObject.getTotal());
                    }
                    // tab coverage 2
                    CoverageDataObject coverageDataObjectInTab2 = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            EnviroCoverageTypeNode.BRANCH);
                    if (coverageDataObjectInTab2 != null) {
                        controller.loadContentToCoverageViewInTab2("Branch Coverage", coverageDataObjectInTab2.getContent());

                        float progressInTab2 = coverageDataObjectInTab2.getProgress();
                        controller.updateProgressInTab2(progressInTab2);
                        controller.updateProgressDetailInTab2(coverageDataObjectInTab2.getVisited() + "/" + coverageDataObjectInTab2.getTotal());
                    }
                    break;
                }

                case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                    // tab coverage 1
                    CoverageDataObject coverageDataObject = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            EnviroCoverageTypeNode.STATEMENT);
                    if (coverageDataObject != null) {
                        controller.loadContentToCoverageViewInTab1("Statement Coverage", coverageDataObject.getContent());

                        float progress = coverageDataObject.getProgress();
                        controller.updateProgress(progress);
                        controller.updateProgressDetail(coverageDataObject.getVisited() + "/" + coverageDataObject.getTotal());
                    }

                    // tab coverage 2
                    CoverageDataObject coverageDataObjectInTab2 = CoverageManager.getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases,
                            EnviroCoverageTypeNode.MCDC);
                    if (coverageDataObjectInTab2 != null) {
                        controller.loadContentToCoverageViewInTab2("MCDC Coverage", coverageDataObjectInTab2.getContent());

                        float progressInTab2 = coverageDataObjectInTab2.getProgress();
                        controller.updateProgressInTab2(progressInTab2);
                        controller.updateProgressDetailInTab2(coverageDataObjectInTab2.getVisited() + "/" + coverageDataObjectInTab2.getTotal());
                    }
                    break;
                }
            }

            Tab tab = new Tab(tabName);
            tab.setContent(view);
            return tab;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static Tab generateCoverageTab(TestCase testCase) {
//        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/CoverageViewTab.fxml"));
//        try {
//            AnchorPane view = loader.load();
//            CoverageViewTabController controller = loader.getController();
//
//            String highlight = AbstractCoverageManager.getCoverage(testCase);
//            controller.loadContentToCoverageViewInTab1(highlight);
//
//            float progress = AbstractCoverageManager.getProgress(testCase);
//            controller.updateProgress(progress);
//
//            String progressDetail = AbstractCoverageManager.getDetailProgressCoverage(testCase);
//            controller.updateProgressDetail(progressDetail);
//
//            Tab tab = new Tab(testCase.getName());
//            tab.setContent(view);
//            return tab;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static Tab generateReportTab(String tabName, ITestCase testCase) {
        final WebView reportView = new WebView();
        final WebEngine webEngine = reportView.getEngine();
        // get path of report file
        FullReport report = new FullReport(testCase, LocalDateTime.now());
        String filePath = report.getPath();
        if (new File(filePath).exists()) { // check if the path exists
            Tab tab = new Tab(tabName);
            String content = ReportManager.readContentFromFile(testCase.getName());
            webEngine.loadContent(content);
            tab.setContent(reportView);
            webEngine.setUserStyleSheetLocation(Object.class.getResource("/css/report_style.css").toString());

            return tab;
        }

        return new Tab(tabName);
    }


    public static Tab generateReportTab(String tabName, String content) {
        final WebView reportView = new WebView();
        final WebEngine webEngine = reportView.getEngine();
        webEngine.loadContent(content);

        webEngine.documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
                if (newValue != null) {
                    NodeList nodeList = newValue.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        EventTarget eventTarget = (EventTarget) node;
                        eventTarget.addEventListener("click", new EventListener() {
                            @Override
                            public void handleEvent(Event evt) {
                                EventTarget target = evt.getCurrentTarget();
                                HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                                String href = anchorElement.getHref();
                                if (href.startsWith("http")) {
                                    logger.debug("Open " + href);
                                    Desktop d = Desktop.getDesktop();
                                    try {
                                        URI address = new URI(href);
                                        d.browse(address);
                                    } catch (URISyntaxException | IOException e) {
                                        logger.error("Cant open " + href + ": " + e.getMessage());
                                    }
                                }
                                evt.preventDefault();
                            }
                        }, false);
                    }
                }
            }
        });

        Tab tab = new Tab(tabName);
        tab.setContent(reportView);
        webEngine.setUserStyleSheetLocation(Object.class.getResource("/css/report_style.css").toString());

        return tab;
    }

    public static Tab generateReportTab(String tabName, String content, String CssLink) {
        final WebView reportView = new WebView();
        final WebEngine webEngine = reportView.getEngine();
        webEngine.loadContent(content);
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(Event ev) {
                            String domEventType = ev.getType();
                            //System.err.println("EventType: " + domEventType);
                            if (domEventType.equals("click")) {
                                String href = ((Element) ev.getTarget()).getAttribute("href");
                                System.out.println(href);
                                if (href != null) {
                                    if (href.contains("#")) {
                                        href = href.substring(0, href.indexOf("#"));
                                    }
                                    String content = Utils.readFileContent(href);
                                    String reportDirPath = new File(href).getParent();
                                    content = changeReportContent(content, reportDirPath);
                                    String finalContent = content;
                                    Platform.runLater(() -> {
                                        webEngine.loadContent(finalContent);
                                        webEngine.reload();
                                    });
                                }
                            }
                        }
                    };

                    Document doc = webEngine.getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, false);
                    }
                }
            }
        });

        Tab tab = new Tab(tabName);
        tab.setContent(reportView);
//        File Css = new File(CssLink);
        webEngine.setUserStyleSheetLocation("file:" + CssLink);

        return tab;
    }

    // used for default function config setting
    public static Stage generateDefaultFunctionConfigStage() {
        // default function configuration
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/FunctionConfigTreeTableView.fxml"));
        try {
            AnchorPane treeTable = loader.load();
            FunctionConfigurationController controller = loader.getController();

            // get the default function configure
            FunctionConfig functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
            if (functionConfig != null) {
                controller.loadContent(functionConfig);

                Scene scene = new Scene(treeTable);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Edit Default Function Configuration");
                stage.initModality(Modality.WINDOW_MODAL);

                return stage;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Stage generateBoundOfVariableTypesStage() {
        // default function configuration
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/BoundOfVariableTypeTreeTableView.fxml"));
        try {
            AnchorPane treeTable = loader.load();
            BoundOfVariableTypeConfigurationController controller = loader.getController();

            String boundOfDataTypeDirectory = new WorkspaceConfig().fromJson().getBoundOfDataTypeDirectory();
            BoundOfDataTypes boundOfDataTypes;
            if (new File(boundOfDataTypeDirectory).exists()) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(BoundOfDataTypes.class, new BoundOfDataTypesDeserializer());
                Gson customGson = gsonBuilder.create();
                boundOfDataTypes = customGson.fromJson(Utils.readFileContent(boundOfDataTypeDirectory),
                        BoundOfDataTypes.class);
            } else {
                boundOfDataTypes = new BoundOfDataTypes();
                boundOfDataTypes.setBounds(boundOfDataTypes.createILP32());
                Environment.exportBoundOfDataTypeToFile(boundOfDataTypes);
            }

            // get the default function configure
            controller.loadContent(boundOfDataTypes);

            Scene scene = new Scene(treeTable);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Edit Bound of variable function Configuration");
            stage.initModality(Modality.WINDOW_MODAL);

            return stage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Tab generateFunctionConfigTab(ICommonFunctionNode functionNode) {
        // default function configuration
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/FunctionConfigTreeTableView.fxml"));
        try {
            AnchorPane treeTable = loader.load();
            FunctionConfigurationController controller = loader.getController();

            // search the function config in database
            FunctionConfig functionConfig = null;
            File functionConfigDirectory = new File(new WorkspaceConfig().fromJson().getFunctionConfigDirectory());
            if (functionConfigDirectory.exists()) {
                for (File configFile : functionConfigDirectory.listFiles())
                    if (configFile.getName().equals(functionNode.getNameOfFunctionConfigJson() + ".json")) {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigDeserializer());
                        Gson customGson = gsonBuilder.create();
                        functionConfig = customGson.fromJson(Utils.readFileContent(configFile), FunctionConfig.class);
                        functionNode.setFunctionConfig(functionConfig);
                        logger.debug("Function config of " + functionNode.getAbsolutePath() + ": " + configFile.getAbsolutePath());
                        break;
                    }
            }

            // create new function config
            if (functionConfig == null) {
                Alert alert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Initialize a function config", "Function config initialization",
                        "Do you want to initialize a function config?");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get().getText().toLowerCase().equals("yes")) {
                    functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
                    functionConfig.setFunctionNode(functionNode);
                    functionConfig.createBoundOfArgument(functionConfig, functionNode);
                    functionNode.setFunctionConfig(functionConfig);

                    // save the function config to file
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigSerializer());
                    Gson customGson = gsonBuilder.setPrettyPrinting().create();
                    String json = customGson.toJson(functionConfig, FunctionConfig.class);
                    String jsonFile = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator + functionNode.getNameOfFunctionConfigJson() + ".json";
                    Utils.writeContentToFile(json, jsonFile);
                    logger.debug("Function config of " + functionNode.getAbsolutePath() + ": " + jsonFile);

                    controller.loadContent((FunctionConfig) functionNode.getFunctionConfig());
                    Tab tab = new Tab(functionNode.getNameOfFunctionConfigTab());
                    tab.setContent(treeTable);
                    return tab;
                }
            } else {
                controller.loadContent((FunctionConfig) functionNode.getFunctionConfig());
                Tab tab = new Tab(functionNode.getNameOfFunctionConfigTab());
                tab.setContent(treeTable);
                return tab;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
