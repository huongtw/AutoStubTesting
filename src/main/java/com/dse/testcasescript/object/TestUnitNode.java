package com.dse.testcasescript.object;

import com.dse.environment.Environment;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.util.PathUtils;

import java.io.File;
import java.util.List;

public class TestUnitNode extends AbstractTestcaseNode {
    private ISourcecodeFileNode srcNode;
    private String name; // absolute path

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + ": name = " + getName();
    }

    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder();
        output.append(TEST_UNIT + " ").append(PathUtils.toRelative(name)).append("\n");

        for (ITestcaseNode child : getChildren())
            output.append(child.exportToFile()).append("\n");

        return output.toString();
    }

    public ITestcaseNode findNormalSubprogramNode(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode) {
            List<ITestcaseNode> nodes = TestcaseSearch.searchNode(this, new TestNormalSubprogramNode());
            for (ITestcaseNode child : nodes) {
                TestNormalSubprogramNode childCast = (TestNormalSubprogramNode) child;
                if (childCast.getName().equals(((TestNormalSubprogramNode) node).getName()))
                    return child;
            }
        }
        return null;
    }

    public String getShortNameToDisplayInTestcaseTree(){
        return new File(name).getAbsolutePath().replace(Environment.getInstance().getProjectNode().getAbsolutePath(),"");
    }

    public void setSrcNode(ISourcecodeFileNode srcNode) {
        this.srcNode = srcNode;
    }

    public ISourcecodeFileNode getSrcNode() {
        return srcNode;
    }
}
