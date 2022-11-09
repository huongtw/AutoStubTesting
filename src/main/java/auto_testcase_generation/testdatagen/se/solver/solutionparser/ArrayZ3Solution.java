package auto_testcase_generation.testdatagen.se.solver.solutionparser;

import java.util.ArrayList;
import java.util.List;

public class ArrayZ3Solution extends Z3Solution {
    // Ex: x!0
    private List<String> indexVarName;

    // item = <element name, value>
    private List<String> values = new ArrayList<>();

    public ArrayZ3Solution(String name, List<String> indexVarNames) {
        this.name = name;
        this.indexVarName = indexVarNames;
    }

    public List<String> getIndexVarName() {
        return indexVarName;
    }

    public void setIndexVarName(List<String> indexVarName) {
        this.indexVarName = indexVarName;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }
}
