package com.dse.environment;

import com.dse.environment.object.IEnvironmentNode;

import java.io.File;

/**
 * Convert the tree of environment into a string
 */
public class ToString {
    private String output = "";
    private int level = 0;
    public static void main(String[] args) {
        EnvironmentAnalyzer analyzer = new EnvironmentAnalyzer();
        analyzer.analyze(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/evironment_sample_01/script"));

        IEnvironmentNode root  = analyzer.getRoot();
        ToString converter = new ToString();
        String output = converter.convert(root);
        System.out.println(output);

    }


    public String convert(IEnvironmentNode root){
        if (root !=null) {
            output += generateTab(level) + root.toString();
            output += "\n";

            if (root.getChildren().size() >= 0) {
                level++;
                for (IEnvironmentNode child : root.getChildren()) {
                    convert(child);
                    level--;
                }
            }
        }

        return output;
    }

    private String generateTab(int level){
        StringBuilder tab = new StringBuilder();
        for (int i = 0; i < level; i++)
            tab.append("\t");
        return tab.toString();
    }

    public ToString(){

    }


    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
