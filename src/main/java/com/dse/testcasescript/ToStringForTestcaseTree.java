package com.dse.testcasescript;

import com.dse.testcasescript.object.ITestcaseNode;

import java.io.File;

/**
 * Convert the tree of test cases into a string
 */
public class ToStringForTestcaseTree {
    private String output = "";
    private int level = 0;

    public static void main(String[] args) {
        TestcaseAnalyzer analyzer = new TestcaseAnalyzer();
        ITestcaseNode root = analyzer.analyze(new File("datatest/duc-anh/testcase_sample/script07"));

        // export to tree
        ToStringForTestcaseTree converter = new ToStringForTestcaseTree();
        String output = converter.convert(root);
        System.out.println(output);
    }

    public String convert(ITestcaseNode root) {
        if (root != null) {
            output += generateTab(level) + root.toString();
            output += "\n";

            if (root.getChildren().size() >= 0) {
                level++;
                for (ITestcaseNode child : root.getChildren()) {
                    convert(child);
                    level--;
                }
            }
        }

        return output;
    }

    private String generateTab(int level) {
        StringBuilder tab = new StringBuilder();
        for (int i = 0; i < level; i++)
            tab.append("\t");
        return tab.toString();
    }

    public ToStringForTestcaseTree() {

    }


    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
