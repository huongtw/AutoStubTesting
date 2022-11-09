package auto_testcase_generation.cte.UI.TestcaseTable;

import auto_testcase_generation.cte.UI.Controller.CteTestCaseCombineManager;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.UI.OptionTable.CteOTChoice;
import auto_testcase_generation.cte.UI.OptionTable.CteOTRow;
import auto_testcase_generation.cte.UI.OptionTable.CteOptionTable;
import auto_testcase_generation.cte.booleanChangeListen.BooleanChangeListener;
import auto_testcase_generation.cte.booleanChangeListen.ListenableBoolean;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import auto_testcase_generation.cte.core.cteTable.CteTableTestcase;
import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.TestCase;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CteTestcaseTable extends Pane {
    private List<CteTestcaseElement> tcList;
    private CteOptionTable opTable;
    private int numOfTest = 0;
    private int nextID = 1;
    private CteTestCaseCombineManager testCaseCombineManager;
    private ListenableBoolean combineTaskWaiting = new ListenableBoolean(false);
    private List<TestCase> combinedTestcase = new ArrayList<>();
    private boolean menuOpened = false;
    private boolean selectedAll = false;
    private boolean multipleSelect = false;
    private String functionName;
   // private VBox vb;
  //  private CteTable cTableManager;


    public CteTestcaseTable(CteOptionTable _opTable, String _functionName) {
        super();
        this.opTable = _opTable;
        this.opTable.setTestcaseTable(this);
        this.testCaseCombineManager = new CteTestCaseCombineManager(opTable.getCteMain().getTreeManager().getfNode());
        tcList = new ArrayList<>();
        functionName = _functionName;
        if (opTable.getcTableManager() != null) {
            for (CteTableTestcase t : opTable.getcTableManager().getLines()) {
                addingTestcase(t);
            }
        }
    }

    public void addingTestcase(CteTableTestcase coreTc)
    {
        String id = "tc" + coreTc.getId();
        String name = coreTc.getName();
        CteTestcaseElement el = new CteTestcaseElement( (numOfTest++)*31, name, id);
        el.widthProperty().bind(this.widthProperty());
        el.setTestcaseTable(this);
        tcList.add(el);
        this.getChildren().addAll(el, el.getTcLabel());
        CteOTRow otRow = opTable.addRow(id);
        setUpChoicesPos(id, el);
        for (CteNode chosenNode : coreTc.getChosenNodes()) {
            otRow.getChoicebyId(chosenNode.getId()).setSelected(true);
        }
    }

    public void addingTestcase() {
        numOfTest += 1;
        CteTableTestcase coreTc = addLineInCore();
        String id = "tc" + coreTc.getId();
        String name = coreTc.getName();
        CteTestcaseElement el = new CteTestcaseElement( (numOfTest-1)*31, name, id);
        el.widthProperty().bind(this.widthProperty());
        el.setTestcaseTable(this);
        tcList.add(el);
        this.getChildren().addAll(el, el.getTcLabel());
        //opTable.getcTableManager().addLines(el.getTcLabel().getText());
        CteOTRow otRow = opTable.addRow(id);
        setUpChoicesPos(id, el);
    }

    public void setUpChoicesPos(String id, CteTestcaseElement element)
    {
        CteOTRow tmpRow = opTable.getRowById(id);
        if(tmpRow != null)
        {
            tmpRow.setPosOfChoices(element);
            tmpRow.getHighLight().yProperty().bind(element.yProperty());
            tmpRow.getHighLight().heightProperty().bind(element.heightProperty());
            tmpRow.setUpChoicesLineYPos();
        }
    }


    private CteTableTestcase addLineInCore() {
        return opTable.getcTableManager().addLines();
    }

    private void deleteTestcase(CteTestcaseElement testCase)
    {
        if(tcList.contains(testCase))
        {
            tcList.remove(testCase);
            this.getChildren().removeAll(testCase, testCase.getTcLabel());
        }
        opTable.deleteRow(testCase.getTestcaseID());
        opTable.getcTableManager().deleteLine(Integer.parseInt(testCase.getTestcaseID().substring(2)));
        numOfTest--;
    }

    public void deleteTestcases()
    {
       // opTable.getcTableManager().clearLines();
        if(!testCaseCombineManager.getChosenTestCase().isEmpty())
        {
            for(int i = 0; i <testCaseCombineManager.getChosenTestCase().size();i++ ){
                deleteTestcase(testCaseCombineManager.getChosenTestCase().get(i));
            }
        }

        testCaseCombineManager.getChosenTestCase().clear();
        if(selectedAll) selectedAll = false;
        rearrangeTestList();
    }

    public void rearrangeTestList()
    {
        if(!tcList.isEmpty())
        {
            int currentTestcaseNum = 1;
            for (CteTestcaseElement cteTestcaseElement : tcList) {
                double pos = (currentTestcaseNum - 1) * 31;
                if (cteTestcaseElement.getY() != pos) {
                    cteTestcaseElement.setPos(pos);
                }
                currentTestcaseNum++;
            }

        }
        else
        {
            numOfTest = 0;
            nextID = 1;
            opTable.getcTableManager().resetLineID();
        }
    }


    public void selectedOrUnselectedAll()
    {
        if(selectedAll || tcList.size() == testCaseCombineManager.getChosenTestCase().size())
        {
            unselectAllTestCaseFromList();
            selectedAll = false;

        }
        else{
            selectAllTestCaseToList();
            selectedAll = true;
        }
    }

    public void genTcWithPairwise()
    {
        if(opTable.getCteMain().hasFreeNode())
        {
            UIController.showErrorDialog("There are Freenode(s) in Classification Tree","Cannot generate pairwise!", "");
        }
        else
        {
        opTable.getcTableManager().loadTable();
        List<CteTableTestcase> tcList = opTable.getcTableManager().genPairwise();
        int index = opTable.getNumOfRound();
        System.out.println("INDEX  " + index);
        for(int i =0; i < tcList.size(); i++)
        {
            addingTestcase(tcList.get(i));
        }
        for(int i = index; i < tcList.size()+index; i++)
        {

            for(int k = 0 ; k < opTable.getRowList().get(i).getButOfRowList().size(); k++) {
            }
            for(int j = 0; j < tcList.get(i-index).getChosenNodes().size(); j++ )
            {
                String id = tcList.get(i-index).getChosenNodes().get(j).getId();
               // System.out.print(id + " ");
                if(opTable.getRowList().get(i).getChoicebyId(id) != null)
                {
                    opTable.getRowList().get(i).getChoicebyId(id).setSelected(true);
                }
            }
        }}
    }

//    public void prefixTestList()
//    {
//        tcList.setCellFactory(lst ->
//            new ListCell<String>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                        setPrefHeight(45.0);
//
//            }
//    });
//    }

    public List<TestCase> getAllTestCaseChoice(CteTestcaseElement element)
    {
        List<TestCase> result = new ArrayList<>();
        List<CteOTChoice> choices = new ArrayList<>();
        String elID = element.getTestcaseID();
        if(!elID.isEmpty())
        {
            CteOTRow tmpRow = opTable.getRowById(elID);
            if(tmpRow != null)
            {
                for(int i = 0; i < tmpRow.getButOfRowList().size(); i++)
                {
                    CteOTChoice tmpChoice = tmpRow.getButOfRowList().get(i);
                    if(tmpChoice.isSelected())
                    {
                        choices.add(tmpChoice);
                    }
                }
                List<CteOTChoice> parentChoices = new ArrayList<>();
                for(int i = 0; i < choices.size(); i++)
                {
                    if(choices.get(i).getParentChoice() != null) parentChoices.add(choices.get(i).getParentChoice());
                }

                for(int i = 0; i < parentChoices.size(); i++)
                {
                    choices.remove(parentChoices.get(i));
                }
                choices.sort(new Comparator<CteOTChoice>() {
                    @Override
                    public int compare(CteOTChoice o1, CteOTChoice o2) {
                        if(o1.getLayoutX() > o2.getLayoutX()) return 1;
                        else if(o1.getLayoutX() > o2.getLayoutX()) return -1;
                        else {
                            if(o1.getLayoutY() > o2.getLayoutY()) return 1;
                            else if(o1.getLayoutY() < o2.getLayoutY() ) return -1;
                        }
                        return 0;

                    }
                });
                for(int i = 0; i <choices.size(); i++)
                {
                    TestCase tmpTestCase = opTable.getCteMain().getTestCaseByID(choices.get(i).getNodeId());
                    if(tmpTestCase!= null)
                    {
                        result.add(tmpTestCase);
                    }
                }
            }

        }

        return result;
    }



    public TestCase combineTestCase(CteTestcaseElement element, String tcName)
    {
        List<TestCase> testCases = getAllTestCaseChoice(element);
        TestCase result = testCaseCombineManager.exportTestCase(testCases, tcName);
        return result;
    }

    public void exportAllTestCases()
    {
        if(!testCaseCombineManager.getChosenTestCase().isEmpty())
        {
            List<CteTestcaseElement> tmpList = testCaseCombineManager.getChosenTestCase();
            tmpList.sort(new Comparator<CteTestcaseElement>() {
                @Override
                public int compare(CteTestcaseElement o1, CteTestcaseElement o2) {

                    if( o1.getY() > o2.getY()) return 1;
                    else if(o1.getY() < o2.getY()) return  -1;
                    else return 0;
                }
            });
            for (int i =0;i < tmpList.size(); i++)
            {
                TestCase testCase = tmpList.get(i).ExportTestcase();
                if(testCase!= null)
                {
                    combinedTestcase.add(testCase);
                }
            }

            for (int i =0;i < tmpList.size(); i++)
            {
                tmpList.get(i).setUnselected();
                i--;
            }
            turnOnCombineTask();
        }
    }

    public void selectTestCaseToList(CteTestcaseElement newElement)
    {
        if(!testCaseCombineManager.getChosenTestCase().contains(newElement))
        {
            testCaseCombineManager.getChosenTestCase().add(newElement);
        }
    }

    public void selectAllTestCaseToList()
    {
        for(int i = 0; i < tcList.size(); i++)
        {
            tcList.get(i).setSelected();
        }
    }

    public void unselectTestCaseFromList(CteTestcaseElement newElement)
    {
        if(testCaseCombineManager.getChosenTestCase().contains(newElement))
        {
            testCaseCombineManager.getChosenTestCase().remove(newElement);
        }
    }

    public void unselectAllTestCaseFromList()
    {
        for(int i = 0; i < tcList.size(); i++)
        {
            tcList.get(i).setUnselected();
        }
    }


    public List<CteTestcaseElement> getTcList() {
        return tcList;
    }

    public ListenableBoolean getCombineTaskWaiting() {
        return combineTaskWaiting;
    }

    public void setCombineTaskListener(BooleanChangeListener listener)
    {
        combineTaskWaiting.addBooleanChangeListener(listener);
    }

    public void turnOnCombineTask()
    {
        combineTaskWaiting.setFlag(true);
    }

    public void turnOffCombineTask()
    {
        combineTaskWaiting.setFlag(false);
        combinedTestcase.clear();
        selectedAll = false;
    }

    public List<TestCase> getCombinedTestcases() {
        return combinedTestcase;
    }

    public boolean isMenuOpened() {
        return menuOpened;
    }

    public void setMenuOpened(boolean _menuOpened) {
        this.menuOpened = _menuOpened;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public boolean isMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }


    public CteOptionTable getOpTable() {
        return opTable;
    }

    public CteTestCaseCombineManager getTestCaseCombineManager() {
        return testCaseCombineManager;
    }

    public void saveCte() {
        for (CteTestcaseElement line : tcList) {
            String elID = line.getTestcaseID();
            if(!elID.isEmpty() && elID.startsWith("tc")) {
                CteTableTestcase tmpCoreLine = getLineFromCore(elID);
                tmpCoreLine.clearChosenNode();
                CteOTRow tmpRow = opTable.getRowById(elID);
                if (tmpRow != null) {
                    for (CteOTChoice tmpChoice : tmpRow.getButOfRowList()) {
                        if (tmpChoice.isSelected()) {
                            tmpCoreLine.chooseNode(getNodeFromCore(tmpChoice.getNodeId()));
                        }
                    }
                }
            }
        }

        opTable.getCteMain().getTreeManager().exportCteToFile();

    }

    private CteNode getNodeFromCore(String id) {
        return opTable.getCteMain().getTreeManager().getTree().searchNodeById(id);
    }

    private CteTableTestcase getLineFromCore(String id) {
        return opTable.getCteMain().getTreeManager().getTable().getLine(Integer.parseInt(id.substring(2)));
    }

    public CteTestcaseElement getTestcaseElementByID(String ID)
    {
        for( CteTestcaseElement element : tcList)
        {
            if(element.getTestcaseID().equals(ID)) return element;
        }
        return null;
    }

    public void sortTcList()
    {
        tcList.sort(new Comparator<CteTestcaseElement>() {
            @Override
            public int compare(CteTestcaseElement o1, CteTestcaseElement o2) {
                String name1 = o1.getTcLabel().getText();
                String name2 = o2.getTcLabel().getText();
                int result = name1.compareTo(name2);
                return 0;
            }
        });
    }
}

