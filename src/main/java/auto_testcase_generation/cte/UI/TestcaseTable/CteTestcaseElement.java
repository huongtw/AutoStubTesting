package auto_testcase_generation.cte.UI.TestcaseTable;

import auto_testcase_generation.cte.UI.OptionTable.CteOTChoice;
import auto_testcase_generation.cte.UI.OptionTable.CteOTRow;
import com.dse.testcase_manager.TestCase;
import com.dse.util.SpecialCharacter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;

public class CteTestcaseElement extends Rectangle {
    private Text tcLabel;
    private TextField tcTextField;
    private double paddingX = 7.5, paddingY = 8;
    private double alphaX = 8, alphaY = 10;
    private String testcaseID;
    private CteTestcaseTable testcaseTable = null;
    private ContextMenu menu;
    private int exportTime = 0;
    private boolean selected = false;

    public CteTestcaseElement(double yPos, String _name, String id)
    {
        super();
        this.setX(0);
        this.setY(yPos);
        this.setFill(Color.TRANSPARENT);
        this.setStroke(Color.TRANSPARENT);
       // this.setStrokeWidth(2);
        testcaseID = id;
        tcLabel = new Text(_name);
        tcTextField = new TextField();

        tcLabel.setX(15);
        tcLabel.setLayoutY(15);
        tcLabel.setY(yPos + (31 - tcLabel.getLayoutBounds().getHeight())/2);

        this.setHeight(31);
        this.setWidth(tcLabel.getLayoutBounds().getWidth());
        menu = new ContextMenu();
        setUpSelected();
        setUpContextMenu();


    }

    private void setUpSelected()
    {
        this.setOnMouseEntered((t) ->{
            if(!selected) beHover();
        });
        this.setOnMouseExited((t) ->{
            beUnhover();
        });
        tcLabel.setOnMouseEntered((t) ->{
            if(!selected)  beHover();
        });
        tcLabel.setOnMouseExited((t) -> {
            beUnhover();
        });
        tcLabel.setOnMouseClicked((t) -> {
//            if(t.getButton().equals(MouseButton.SECONDARY))
//            {
//                OpenMenu(t.getScreenX(), t.getScreenY());
//            }
            if(t.isShiftDown() || t.isControlDown())
            {
                setSelectedOrUnselectedInMultipleMode();
            }
            else setSelectedOrUnselectedInSingleMode();
        });

        this.setOnMouseClicked((t) -> {
//            if(t.getButton().equals(MouseButton.SECONDARY))
//            {
//                OpenMenu(t.getScreenX(), t.getScreenY());
//            }
            if(t.isShiftDown() || t.isControlDown())
            {
                setSelectedOrUnselectedInMultipleMode();
            }
            else setSelectedOrUnselectedInSingleMode();
        });

        
    }

//    public void OpenMenu(double posX, double posY)
//    {
//        if (!testcaseTable.isMenuOpened()) {
//            menu.show(this, posX, posY);
//            testcaseTable.setMenuOpened(true);
//        }
//    }

    public TestCase ExportTestcase()
    {
        if(testcaseTable != null)
        {
            String newTestcaseName = setUpNameForExportTestCase();
           TestCase a = testcaseTable.combineTestCase(this, newTestcaseName);
//           testcaseTable.turnOnCombineTask(a);
           increaseExportTime();
           return a;
        }

        return null;
    }

    public void beHover()
    {
        this.setFill(Color.LIGHTGRAY);
    }

    public void beUnhover()
    {
        if(!selected) {
            this.setFill(Color.TRANSPARENT);
        }
    }

    public void setSelectedOrUnselectedInMultipleMode()
    {
        if(selected == false) {
            setSelected();
        }
        else
        {
            setUnselected();
        }
    }

    public void setSelectedOrUnselectedInSingleMode()
    {
        if(testcaseTable.getTestCaseCombineManager().isMultipleTestcasesInList() || !this.selected)
        {
            testcaseTable.unselectAllTestCaseFromList();
            this.setSelected();
        }
        else
        {
            this.setUnselected();
        }
    }

    public void setSelected()
    {
        if(selected == false) {
            changeSelected(true);
            testcaseTable.selectTestCaseToList(this);
            this.setFill(Color.STEELBLUE);
            tcLabel.setFill(Color.WHITE);
            testcaseTable.getOpTable().getRowById(testcaseID).activateHighlight();

        }
    }

    public void setUnselected()
    {
        if(selected == true)
        {
            changeSelected(false);
            testcaseTable.unselectTestCaseFromList(this);
            this.setFill(Color.TRANSPARENT);
            tcLabel.setFill(Color.BLACK);
            testcaseTable.getOpTable().getRowById(testcaseID).deactivateHighlight();

        }
    }

    public void changeSelected(boolean newVal)
    {
        selected = newVal;
        if(newVal == true)
        {
            menu.getItems().get(0).setDisable(true);
            menu.getItems().get(1).setDisable(false);
        }
        else
        {
            menu.getItems().get(0).setDisable(false);
            menu.getItems().get(1).setDisable(true);
        }
    }

    public Text getTcLabel() {
        return tcLabel;
    }

    public void setTcLabel(Text tcLabel) {
        this.tcLabel = tcLabel;
    }

    public TextField getTcTextField() {
        return tcTextField;
    }

    public void setTcTextField(TextField tcTextField) {
        this.tcTextField = tcTextField;
    }

    public CteTestcaseTable getTestcaseTable() {
        return testcaseTable;
    }

    public void setTestcaseTable(CteTestcaseTable testcaseTable) {
        this.testcaseTable = testcaseTable;
    }

    public String getTestcaseID() {
        return testcaseID;
    }

    public void setTestcaseID(String testcaseID) {
        this.testcaseID = testcaseID;
    }

    private void setUpContextMenu()
    {
        MenuItem item1 = new MenuItem("Select this Testcase");
        item1.setOnAction((t) -> {
            setSelected();
        });

        MenuItem item2 = new MenuItem("Unselect this Testcase");
        item2.setOnAction((t) -> {
            setUnselected();
        });

        item2.setDisable(true);

        menu.getItems().addAll(item1, item2);

        menu.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == false) {
                    testcaseTable.setMenuOpened(false);
                }
            }
        });
    }


    private String setUpNameForExportTestCase()
    {
        String result = getNameOfTestCase();

            result += "_" + exportTime  + "_C";

        return result;
    }

    private void increaseExportTime()
    {
        exportTime++;
    }

    public ContextMenu getMenu() {
        return menu;
    }

    private String getNameOfTestCase() {
        String name = new String(testcaseTable.getFunctionName() +"_"+tcLabel.getText());
        return name
                .replace("+", "plus")
                .replace("-", "minus")
                .replace("*", "mul")
                .replace("/", "div")
                .replace("%", "mod")
                .replace("=", "equal")
                .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE)
                .replaceAll("[_]+", SpecialCharacter.UNDERSCORE);
    }

    public void setPos(double yPos)
    {
        this.setY(yPos);
        tcLabel.setY(yPos + (31 - tcLabel.getLayoutBounds().getHeight())/2);
    }




    public boolean isSelected() {
        return selected;
    }
}
