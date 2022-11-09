package auto_testcase_generation.cte.UI.DataInsert;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import com.dse.guifx_v3.helps.Factory;
import com.dse.testcase_manager.TestCase;
import javafx.scene.layout.AnchorPane;

public class CteDataInsert extends AnchorPane {
    private TestCase currentTestCase = null;
    private CteUiNode choosingNode = null;

    public CteDataInsert()
    {
        super();
        setUp();
    }

    public void ViewDataInsert(TestCase testCase, CteUiNode node)
    {
        if(testCase != null){
            if(node == choosingNode)
            {
                unviewDataInsert(node);
            }
            else {
                this.getChildren().clear();
                AnchorPane a = Factory.generateDataInsert(testCase);
//              AnchorPane a = Factory.generateTestcaseTab(testCase);
                this.getChildren().add(a);
                AnchorPane.setBottomAnchor(a, 0.0);
                AnchorPane.setLeftAnchor(a, 0.0);
                AnchorPane.setRightAnchor(a, 0.0);
                AnchorPane.setTopAnchor(a, 0.0);
                this.currentTestCase = testCase;
                this.choosingNode = node;
            }
        }
    }

    public void unviewDataInsert(CteUiNode node)
    {
        if(node == choosingNode)
        {
            this.getChildren().clear();
            this.currentTestCase = null;
            choosingNode = null;
        }
    }

    public void reload(CteUiNode node)
    {
        if(node == choosingNode)
        {
            unviewDataInsert();
            ViewDataInsert(node.getTestCase(), node);
        }
    }

    public void unviewDataInsert()
    {
        if(choosingNode != null)
        {
            this.getChildren().clear();
            this.currentTestCase = null;
            choosingNode = null;
        }
    }

    public boolean RemoveTestCaseByNodeID(String id)
    {
        if(currentTestCase != null && currentTestCase.getName().equals(id))
        {
            this.getChildren().clear();
            choosingNode.chosenModeDeactivate();
            this.currentTestCase = null;
            this.choosingNode = null;
            return true;
        }
        return false;
    }

    private void setUp()
    {
        this.setMinWidth(400);
        //this.setMinHeight(800);
    }

}
