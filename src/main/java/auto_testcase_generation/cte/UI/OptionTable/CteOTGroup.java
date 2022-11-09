package auto_testcase_generation.cte.UI.OptionTable;

import javafx.scene.control.ToggleGroup;

public class CteOTGroup extends ToggleGroup {
    private String groupID;
    private int rowNum;

    public CteOTGroup(){
        super();
        groupID = new String();
    }

    public CteOTGroup(String _id)
    {
        super();
        groupID = _id;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public void delete()
    {

    }
}
