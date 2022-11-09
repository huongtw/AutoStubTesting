package com.dse.environment;

import com.dse.environment.object.*;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentSearch {
    final static AkaLogger logger = AkaLogger.get(EnvironmentSearch.class);

    public static void main(String[] args) {
        // create tree from script
        EnvironmentAnalyzer analyzer = new EnvironmentAnalyzer();
        analyzer.analyze(new File("datatest/duc-anh/evironment_sample_01/script"));
        IEnvironmentNode root = analyzer.getRoot();

        // display tree
        ToString converter = new ToString();
        String output = converter.convert(root);
        logger.debug(output);

        // search
        logger.debug(EnvironmentSearch.searchNode(root, new EnviroStubNode()));
    }

    public static List<IEnvironmentNode> searchNode(IEnvironmentNode searchRoot, AbstractEnvironmentNode condition) {
        List<IEnvironmentNode> output = new ArrayList<>();

        for (IEnvironmentNode child : searchRoot.getChildren()) {
            if (condition.getClass().isInstance(child)) // check whether child is an sub-class of condition
                output.add(child);
            output.addAll(searchNode(child, condition));
        }
        return output;
    }
}
