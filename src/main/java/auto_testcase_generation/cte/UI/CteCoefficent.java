package auto_testcase_generation.cte.UI;

import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import javafx.scene.paint.Color;

public class CteCoefficent {
    public static final int padX = 70;
    public static final int padY = 80;
    public static final int nodeSpace = 5;
    public static final int nodeDeepSpace = 40;


    // Option Table Coeffiticient
    public static final int otStartX = 250;
    public static final int otStartY = 750;
    public static final int otMinWidth = 2100;
    public static final int otMinHeight = 400;

    public static final String defaultSelectingColor = "b7d3ec";

    public class CteNodeTag{
        public static final String Root = "Root";
        public static final String InputZ = "InputZ";
        public static final String InputGlobal = "InputGlobal";
        public static final String InputGlobArg = "IGA";
        public static final String InputGlobVal = "IGV";
        public static final String InputGlobalArgUS = "IGAUS"; // for union and struct
        public static final String InputGlobalArgUSField ="IGAUSF";
        public static final String InputGlobalArgUSFieldVal ="IGAUSFV";
        public static final String InputArg = "InputArg";
        public static final String InputArgC = "IAC";
        public static final String InputArgVal = "IAV";
        public static final String InputArgUS = "IAUS"; // for union and struct
        public static final String InputArgUSField ="IAUSF";
        public static final String InputArgUSFieldVal ="IAUSFV";
        public static final String OutputZ = "OutputZ";
        public static final String OutputGlobal = "OutputGlobal";
        public static final String OutputGlobArg = "OGA";
        public static final String OutputGlobVal = "OGV";
        public static final String OutputGlobalArgUS = "OGAUS"; // for union and struct
        public static final String OutputGlobalArgUSField ="OGAUSF";
        public static final String OutputGlobalArgUSFieldVal ="OGAUSFV";
        public static final String OutputArg = "OutputArg";
        public static final String OutputArgC = "OAC";
        public static final String OutputArgVal = "OAV";
        public static final String OutputArgUS = "OAUS"; // for union and struct
        public static final String OutputArgUSField ="OAUSF";
        public static final String OutputArgUSFieldVal ="OAUSFV";
        public static final String ArgVChild = "AVC";
        public static final String OutputReturn = "Return";
        public static final String ReturnVal = "RV";
        public static final String Undefined = "Undefined";
        public static final String ChildOfClass = "Class' Child";
    }

    public class CteViewMode{
        public static final int NORMAL = 0;
        public static final int RENAMING = 1;
        public static final int RENAMED = 2;
        public static final int ENTERING_RENAME = 3;
    }

    public class CteElementSize{
        public static final double OriginalWidth = 1920.0;
        public static final double OriginalHeight = 1080.0;
    }

    public static class NodeBgColor{
        public static final Color normalCor = Color.TRANSPARENT;
        public static final Color freeNodeCor = Color.valueOf("fde3e6");
        public static final Color setParModeCor = Color.valueOf("fffdaf");

    }

    public static LoadingPopupController getOpenLoading() {
        LoadingPopupController loadingOpenPopUp = LoadingPopupController.newInstance("Opening CTE");
        loadingOpenPopUp.initOwnerStage(UIController.getPrimaryStage());
        return loadingOpenPopUp;
    }

    public static LoadingPopupController getResetLoading() {
        LoadingPopupController loadingResetPopUp = LoadingPopupController.newInstance("Reset CTE");
        loadingResetPopUp.initOwnerStage(UIController.getPrimaryStage());
        return loadingResetPopUp;
    }
}

