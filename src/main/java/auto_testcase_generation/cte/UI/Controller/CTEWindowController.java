package auto_testcase_generation.cte.UI.Controller;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.OptionTable.CteOptionTable;
import auto_testcase_generation.cte.UI.TestcaseTable.CteTestcaseTable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.net.URL;
import java.util.ResourceBundle;

public class CTEWindowController implements Initializable {

//    /**
//     * Singleton pattern like
//     */
//    private static AnchorPane cteWindow = null;
//    private static CTEWindowController cteWindowController = null;
//
//    private static void prepare() {
//        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/cte/CTEWindow.fxml"));
//        try {
//            Parent parent = loader.load();
//            cteWindow = (AnchorPane) parent;
//            cteWindowController = loader.getController();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static AnchorPane getCTEWindow() {
//        if (cteWindow == null) {
//            prepare();
//        }
//        return cteWindow;
//    }
//
//    public static CTEWindowController getMDIWindowController() {
//        if (cteWindowController == null) {
//            prepare();
//        }
//        return cteWindowController;
//    }


    @FXML
    private SplitPane DInTR; // split the Data Insert and the rest
    @FXML
    private SplitPane OTnCT; // split the Option Table and Classification Tree
    @FXML
    private AnchorPane CTnMN; // for Classification Tree and the Menu
    @FXML
    private SplitPane splitCTnMN;
    @FXML
    private ScrollPane CTPane;
//    @FXML
//    private AnchorPane CT;
    @FXML
    private AnchorPane MNnTCBtn;
    @FXML
    private AnchorPane MNPane;
//    @FXML
//    private ToolBar TCBtns;

    @FXML
    private AnchorPane OTnTT; // for Option table and Testcases Table
    @FXML
    private SplitPane splitOTnTT;
    @FXML
    private ScrollPane OTPane;
//    @FXML
//    private AnchorPane OT;
    @FXML
    private ScrollPane TTPane;
//    @FXML
//    private AnchorPane TT;
    @FXML
    private AnchorPane DIPane;

    @FXML
    private Button exportTC;
    @FXML
    private Button selectAll;
    @FXML
    private Button genPairwise;
    @FXML
    private Button addingTC;
    @FXML
    private Button deleteTC;
    @FXML
    private Button saveCTE;
    @FXML
    private Button arrangeTree;
    @FXML
    private Button resetCTE;



    public void setUpView(CteView CTView, CteOptionTable OTView, CteTestcaseTable TTView)
    {
        setUpSize();
        Group CTGroup = new Group(CTView);
        CTPane.setContent(new VBox(CTGroup));
        Group OTGroup = new Group(OTView);
        OTPane.setContent(OTGroup);
        CTView.minWidthProperty().bind(OTView.widthProperty());
        OTView.minWidthProperty().bind(CTView.widthProperty());
       // CTPane.minWidthProperty().bindBidirectional(OTPane.minWidthProperty());
//        CT.getChildren().addAll(CTView);
//        OT.getChildren().addAll(OTView);


//        CTPane.hvalueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if(CTView.getMode() == CteCoefficent.CteViewMode.RENAMED)
//                {
//                    CTView.setMode(CteCoefficent.CteViewMode.NORMAL);
//                    CTPane.setHvalue(oldValue.doubleValue());
//                }
//            }
//        });



        TTPane.setContent(TTView);
        TTView.minWidthProperty().bind(TTPane.widthProperty());
        TTView.minHeightProperty().bind(TTPane.heightProperty());
        OTView.minHeightProperty().bind(TTView.minHeightProperty());

        OTPane.hvalueProperty().bindBidirectional(CTPane.hvalueProperty());
        TTPane.vvalueProperty().bindBidirectional(OTPane.vvalueProperty());

        DIPane.getChildren().add(CTView.getDInsert());
        AnchorPane.setBottomAnchor(CTView.getDInsert(), 0.0);
        AnchorPane.setTopAnchor(CTView.getDInsert(), 0.0);
        AnchorPane.setRightAnchor(CTView.getDInsert(), 0.0);
        AnchorPane.setLeftAnchor(CTView.getDInsert(), 0.0);

        CTView.getDInsert().minHeightProperty().bind(DIPane.heightProperty());

        CTPane.minWidthProperty().bind(OTPane.widthProperty());

        CTPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        OTPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        OTPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        CTPane.maxWidthProperty().bind(CTView.widthProperty());
        CTPane.maxHeightProperty().bind(CTView.heightProperty());

        OTPane.maxWidthProperty().bind(CTView.widthProperty());

        CTView.minHeightProperty().bind(CTPane.heightProperty());
        splitCTnMN.getDividers().get(0).positionProperty().bindBidirectional(splitOTnTT.getDividers().get(0).positionProperty());

        CTPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                CTView.requestFocus();
            }
        });
//        CTPane.setOnScroll(new EventHandler<ScrollEvent>() {
//            @Override
//            public void handle(ScrollEvent event) {
////                if (event.isControlDown())
//                CTView.requestFocus();
//            }
//        });
    }

    public void setUpSize()
    {
        double trueWidth = Screen.getPrimary().getBounds().getWidth();
        double trueHeight = Screen.getPrimary().getBounds().getHeight();
        double ratioWidth = trueWidth/ CteCoefficent.CteElementSize.OriginalWidth;
        double ratioHeight = trueHeight/CteCoefficent.CteElementSize.OriginalHeight;
        if(ratioWidth != 1 && ratioHeight != 1)
        {
            double prefHForMNnTCBtn = MNnTCBtn.getPrefHeight()*ratioHeight;
            MNnTCBtn.setPrefHeight(prefHForMNnTCBtn);
            double prefWForMNnTCBtn =  MNnTCBtn.getPrefWidth()*ratioWidth;
            MNnTCBtn.setPrefWidth(prefWForMNnTCBtn);

            CTPane.setLayoutX(prefWForMNnTCBtn);
            CTPane.setPrefHeight(prefHForMNnTCBtn);

//            TCBtns.setLayoutY(prefHForMNnTCBtn-30);
//            TCBtns.setPrefWidth(prefWForMNnTCBtn);

            MNPane.setPrefHeight(prefHForMNnTCBtn - 30);
            MNPane.setPrefWidth(prefWForMNnTCBtn);

            TTPane.setPrefWidth(prefWForMNnTCBtn);

            OTPane.setLayoutX(prefWForMNnTCBtn);

            DIPane.setPrefHeight(DIPane.getPrefHeight()*ratioHeight);


        }
    }





    public AnchorPane getMNnTCBtn() {
        return MNnTCBtn;
    }

//    public ToolBar getTCBtns() {
//        return TCBtns;
//    }

    public Button getExportTC() {
        return exportTC;
    }

    public Button getSelectAll() {
        return selectAll;
    }

    public Button getGenPairwise() {
        return genPairwise;
    }

    public Button getAddingTC() {
        return addingTC;
    }

    public Button getDeleteTC() {
        return deleteTC;
    }

    public Button getSaveCTE() {return saveCTE;}

    public Button getArrangeTree() {return arrangeTree;}

    public Button getResetCTE() {
        return resetCTE;
    }

    //    public AnchorPane getCteWindow() {
//        return cteWindow;
//    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public SplitPane getDInTR() {
        return DInTR;
    }

    public SplitPane getOTnCT() {
        return OTnCT;
    }

    public AnchorPane getCTnMN() {
        return CTnMN;
    }

    public ScrollPane getCTPane() {
        return CTPane;
    }

    public AnchorPane getMNPane() {
        return MNPane;
    }

    public AnchorPane getOTnTT() {
        return OTnTT;
    }

    public ScrollPane getOTPane() {
        return OTPane;
    }

    public ScrollPane getTTPane() {
        return TTPane;
    }

    public AnchorPane getDIPane() {
        return DIPane;
    }


}
