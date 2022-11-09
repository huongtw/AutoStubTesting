package com.dse.environment;

import com.dse.environment.object.*;

import java.util.List;

public class example {
    public static void main(String[] args) {
        IEnvironmentNode root = new EnvironmentRootNode();

        // add begin node of environent
        root.addChild(new EnviroNewNode());// ENVIRO.NEW

        // create a compiler node
        EnviroCompilerNode n = new EnviroCompilerNode();//ENVIRO.COMPILER
        n.setName("C++11");
        root.addChild(n);

        // create a UUT
        EnviroUUTNode nn = new EnviroUUTNode();
        nn.setName("A.cpp");
        root.addChild(nn);

        // create defined variables
        EnviroDefinedVariableNode definedVariableNode = new EnviroDefinedVariableNode();
        definedVariableNode.setName("ahaa");
        definedVariableNode.setValue("valueahaa");
        EnviroDefinedVariableNode node2 = new EnviroDefinedVariableNode();
        node2.setName("node2");
        node2.setValue("node2");
        EnviroDefinedVariableNode node3 = new EnviroDefinedVariableNode();
        node3.setName("node3");
        node3.setValue("node3");
        root.addChild(definedVariableNode);
        root.addChild(node2);
        root.addChild(node3);

        // turn on whitebox testing
        EnviroWhiteBoxNode nnn = new EnviroWhiteBoxNode();
        nnn.setActive(true);
        root.addChild(nnn);
        // add end node of environment
        root.addChild(new EnviroEndNode());

        // export to file
        String content = root.exportToFile();
        System.out.println(content);

        // Search example
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroWhiteBoxNode());
        System.out.println(nodes);
    }
}
