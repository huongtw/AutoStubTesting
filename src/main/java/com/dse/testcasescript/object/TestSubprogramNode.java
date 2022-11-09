package com.dse.testcasescript.object;

import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.LambdaFunctionNode;
import com.dse.testcase_manager.AbstractTestCase;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class TestSubprogramNode extends AbstractTestcaseNode {
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
        StringBuilder output = new StringBuilder(TEST_SUBPROGRAM + " " + PathUtils.toRelative(name));

        for (ITestcaseNode child : getChildren())
            output.append("\n").append(child.exportToFile());

        return output.toString();
    }

    public String getSimpleNameToDisplayInTestcaseView() throws FunctionNodeNotFoundException {
        if (name.contains("akaLambdaFunction")) {
            LambdaFunctionNode functionNode = (LambdaFunctionNode) UIController.searchFunctionNodeByPath(name);
            functionNode.getArguments();
            StringBuilder returnName = new StringBuilder();
            String declarator = functionNode.getOriginalAST().getDeclarator().getRawSignature();
            int startLine = functionNode.getOriginalAST().getFileLocation().getStartingLineNumber();
            returnName.append("[").append(startLine).append("] ").append(declarator);
            return returnName.toString();
        } else {
            // Some subprogram path has "/" in its name, e.g.,
            // "OverloadingOperators.cpp/CVector::operator /(const CVector&)"
            String tmpName = AbstractTestCase.removeSysPathInName(name);
            String simpleName = (new File(tmpName)).getName();

            String[] pathElements= null;
            if (Utils.isWindows())
                pathElements = tmpName.split("\\\\");
            else if (Utils.isUnix()|| Utils.isMac())
                pathElements = tmpName.split(File.separator);


            for (int i = pathElements.length - 2; i >= 0; i--)
                if (!pathElements[i].contains("."))
                    simpleName = pathElements[i] + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + simpleName;
                else
                    break;

            simpleName = AbstractTestCase.redoTheReplacementOfSysPathInName(simpleName);
            return simpleName;
        }
    }

    public List<String> getAllTestCases(){
        List<String> testcases = new ArrayList<>();
        for (ITestcaseNode child: this.getChildren())
            if (child instanceof TestNewNode){
                String name = ((TestNewNode) child).getName();
                testcases.add(name);
            }
        return testcases;
    }
    public static final String COMPOUND_SIGNAL = "<<COMPOUND>>";
    public static final String INIT_SIGNAL = "<<INIT>>";

}
