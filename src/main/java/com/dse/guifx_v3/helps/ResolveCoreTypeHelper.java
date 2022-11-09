package com.dse.guifx_v3.helps;

import com.dse.environment.Environment;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.TypeDependency;
import com.dse.parser.object.INode;
import com.dse.parser.object.VariableNode;
import com.dse.search.Search;
import com.dse.search.condition.StructurevsTypedefCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;

import java.util.List;

public class ResolveCoreTypeHelper {

    private final static AkaLogger logger = AkaLogger.get(ResolveCoreTypeHelper.class);

    public static INode resolve(String vPath) {
        List<Dependency> dependencies = Environment.getInstance().getDependencies();
        for (Dependency dependency : dependencies) {
            if (dependency instanceof TypeDependency) {
                if (PathUtils.equals(dependency.getStartArrow().getAbsolutePath(), vPath)) {
                    return dependency.getEndArrow();
                }
            }
        }

        logger.error("Can not resolve " + vPath);
        return null;
    }

    public static INode resolve(VariableNode variableNode) {
        INode coreType = resolve(variableNode.getAbsolutePath());
        if (coreType == null) {
            logger.error("Can not resolve core type for the variableNode: " + variableNode.getAbsolutePath());
        }
        return coreType;
    }

    public static INode getType(String absolutePath) {
        // Search Level 1
        List<Dependency> dependencies = Environment.getInstance().getDependencies();
        for (Dependency dependency : dependencies) {
            if (dependency instanceof TypeDependency) {
                if (PathUtils.equals(dependency.getEndArrow().getAbsolutePath(), absolutePath)) {
                    return dependency.getEndArrow();
                }
            }
        }

        // Search Level 2
        List<INode> structureNodes = Search
                .searchNodes(Environment.getInstance().getProjectNode(), new StructurevsTypedefCondition());
        for (INode structureNode : structureNodes) {
            if (PathUtils.equals(structureNode.getAbsolutePath(), absolutePath)) {
                return structureNode;
            }
        }

        // Search Level 3
        structureNodes = Search.searchNodes(Environment.getInstance().getUserCodeRoot(), new StructurevsTypedefCondition());
        for (INode structureNode : structureNodes) {
            if (PathUtils.equals(structureNode.getAbsolutePath(), absolutePath)) {
                return structureNode;
            }
        }

        return null;
    }
}
