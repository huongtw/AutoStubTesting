package com.dse.testcasescript.object;

import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcasescript.TestcaseSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestNewNode extends TestActionNode {
    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(TEST_NEW);

        for (ITestcaseNode child : getChildren())
            output.append("\n").append(child.exportToFile());

        output.append("\n" + TEST_END);
        return output.toString();
    }

    private List<ITestcaseNode> getCloneChildren() {
        //Todo: need to clone all the children
        List<ITestcaseNode> cloneChildren = new ArrayList<>();
        for (ITestcaseNode child : getChildren()) {
            if (child instanceof TestNameNode) {
                TestNameNode newNameNode = new TestNameNode();
                newNameNode.setName(((TestNameNode) child).getName());
                cloneChildren.add(newNameNode);
            } else {
                cloneChildren.add(child);
            }
        }
        return cloneChildren;
    }
    public TestNewNode cloneTestNewNode() {
        TestNewNode clone = new TestNewNode();
        clone.setParent(getParent());
        clone.setChildren(getCloneChildren());

        // change name
        // Todo: need to have a function to generate a name for testcase
        List<ITestcaseNode> namenodes = TestcaseSearch.searchNode(clone, new TestNameNode());
        if (namenodes.size() == 1) {
            TestNameNode testNameNode = (TestNameNode) namenodes.get(0);
            String originName = testNameNode.getName();
            String[] strings = originName.split("\\.");
            String cloneName = strings[0] + "." + new Random().nextInt(100000);
            testNameNode.setName(cloneName);
            return clone;
        }
        return null;
    }

    public String getName() {
        List<ITestcaseNode> namenodes = TestcaseSearch.searchNode(this, new TestNameNode());
        if (namenodes.size() == 1) {
            return ((TestNameNode) namenodes.get(0)).getName();
        }
        return "";
    }

    public boolean isPrototypeTestcase(){
        return getName().startsWith(TestPrototype.PROTOTYPE_SIGNAL);
    }
}
