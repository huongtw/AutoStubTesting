package auto_testcase_generation.cte.UI.Controller;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.OptionTable.CteOptionTable;
import auto_testcase_generation.cte.UI.TestcaseTable.CteTestcaseTable;
import auto_testcase_generation.cte.booleanChangeListen.BooleanChangeListener;
import auto_testcase_generation.cte.core.ClassificationTreeManager;
import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.testcase_manager.TestCase;
import com.dse.testcasescript.object.TestNormalSubprogramNode;
import com.dse.thread.AkaThread;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.util.List;

public class CteController  {

    private CteView CTView;
    //  private CteDataInsert DIView;
    private CteOptionTable OTView;
    private CteTestcaseTable TTView;
    private ClassificationTreeManager coreManager;
    private CTEWindowController cteWindowController;
    private AnchorPane cteWindow;
    private TestNormalSubprogramNode navigatorNode;
    private final IFunctionNode functionNode;

//    private AnchorPane cteWindow;


    public CteController(IFunctionNode _functionNode, TestNormalSubprogramNode _naviNode)
    {
        functionNode = _functionNode;
        navigatorNode = _naviNode;
        setUp(functionNode);
    }

    public void setUp(IFunctionNode functionNode)
    {

        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/cte/CTEWindow.fxml"));
        try {
            Parent parent = loader.load();
            cteWindowController = loader.getController();
            cteWindow = (AnchorPane) parent;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        setUpView(functionNode);

//        LoadingPopupController popupController = CteCoefficent.getOpenLoading();

        coreManager = new ClassificationTreeManager(functionNode);

        CTView = new CteView(coreManager);
        CTView.setCteController(this);

        OTView = new CteOptionTable(CTView);

        String fName = functionNode.getSimpleName();
        TTView = new CteTestcaseTable(OTView, fName);

        OTView.minWidthProperty().bind(CTView.widthProperty());
        OTView.minHeightProperty().bind(TTView.heightProperty());
        cteWindowController.setUpView(CTView, OTView, TTView);
        CTView.setUpScroll();
        setUpTCBtns();
    }

    public void resetCTE() {
        deleteCteJsonFile();

        coreManager = new ClassificationTreeManager(functionNode);
        CTView = new CteView(coreManager);
        CTView.setCteController(this);

        OTView = new CteOptionTable(CTView);

        String fName = functionNode.getSimpleName();
        TTView = new CteTestcaseTable(OTView, fName);

        OTView.minWidthProperty().bind(CTView.widthProperty());
        OTView.minHeightProperty().bind(TTView.heightProperty());
        cteWindowController.setUpView(CTView, OTView, TTView);
        CTView.setUpScroll();
        setUpTCBtns();

    }


    private void deleteCteJsonFile() {
        String ctePath = new WorkspaceConfig().fromJson().getCteFolder()
                + File.separator + ((FunctionNode) functionNode).getInformName() + "_cteTree.json";
        File cteFile = new File(ctePath);
        cteFile.delete();
    }

    public AnchorPane getCteWindow() {
        return cteWindow;
    }

    public CTEWindowController getCteWindowController() {
        return cteWindowController;
    }



    public void setUpPos(TabPane pane)
    {
//        cteWindowController.getCTPane().maxWidthProperty().bind(cteWindowController.getOTnCT().widthProperty().subtract(1));
//        cteWindowController.getOTPane().maxWidthProperty().bind(cteWindowController.getOTnCT().widthProperty().subtract(1));
//        cteWindowController.getCTPane().minWidthProperty().bind(cteWindowController.getOTnCT().widthProperty().subtract(1));
//        cteWindowController.getOTPane().minWidthProperty().bind(cteWindowController.getOTnCT().widthProperty().subtract(1));
        cteWindowController.getCTPane().maxWidthProperty()
                .bind(cteWindowController.getDInTR().getDividers().get(0).positionProperty().multiply(pane.widthProperty()));
        cteWindowController.getCTPane().maxWidthProperty()
                .bind(cteWindowController.getDInTR().getDividers().get(0).positionProperty().multiply(pane.widthProperty()));
    }

    private void setUpTCBtns()
    {
        cteWindowController.getAddingTC().setOnAction((t) -> {
            TTView.addingTestcase();
        });

        cteWindowController.getGenPairwise().setOnAction((t) -> {
            TTView.genTcWithPairwise();
        });

        cteWindowController.getSelectAll().setOnAction((t) -> {
            TTView.selectedOrUnselectedAll();
        });

        cteWindowController.getDeleteTC().setOnAction((t) -> {
            TTView.deleteTestcases();
        });

        cteWindowController.getExportTC().setOnAction((t) -> {
            TTView.exportAllTestCases();
            System.out.println("Export Testcase");
        });
        cteWindowController.getSaveCTE().setOnAction((t) -> {
            TTView.saveCte();
        });
        cteWindowController.getArrangeTree().setOnAction((t) -> {
            CTView.reArrange();
        });
        cteWindowController.getResetCTE().setOnAction((t) -> {
            LoadingPopupController resetLoading = CteCoefficent.getResetLoading();
            resetLoading.show();
            resetCTE();
            resetLoading.close();
        });
    }

    public TestNormalSubprogramNode getNavigatorNode() {
        return navigatorNode;
    }

    public void setNavigatorNode(TestNormalSubprogramNode navigatorNode) {
        this.navigatorNode = navigatorNode;
    }

    public CteTestcaseTable getTTView() {
        return TTView;
    }

    public void addingListener(BooleanChangeListener listener)
    {
        TTView.setCombineTaskListener(listener);
    }

    public void turnOffCombineTask()
    {
        TTView.turnOffCombineTask();
    }

    public List<TestCase> getCombinedTestCases()
    {
        return TTView.getCombinedTestcases();
    }
}
