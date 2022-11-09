package com.dse.parser.dependency;

import com.dse.parser.object.*;

import java.util.List;

public class TypedefDependencyGeneration extends AbstractDependencyGeneration {

    private final List<INode> candidateNodes;

    public TypedefDependencyGeneration(List<INode> candidateNodes) {
        this.candidateNodes = candidateNodes;
    }

    @Override
    public void dependencyGeneration(INode root) {
        String type = root.getName();

        for (INode node : candidateNodes) {
            if (node instanceof ITypedefDeclaration && !(node instanceof AliasDeclaration)) {
                String oldType = ((ITypedefDeclaration) node).getOldType();
                if (oldType.equals(type)) {
                    boolean success = generateDependency(node, root);

                    if (success) {
                        candidateNodes.remove(node);
                        return;
                    }
                }
            } else if (node instanceof StructTypedefNode
                    || node instanceof EnumTypedefNode
                    || node instanceof UnionTypedefNode) {
                String oldType = node.getNewType();
                if (oldType.equals(type)) {
                    boolean success = generateDependency(node, root);

                    if (success) {
                        candidateNodes.remove(node);
                        return;
                    }
                }
            }
        }
    }

    private boolean generateDependency(INode from, INode to) {
        Dependency d = new TypedefDependency(from, to);

        if (!from.getDependencies().contains(d))
            from.getDependencies().add(d);
        else
            return false;

        if (!to.getDependencies().contains(d))
            to.getDependencies().add(d);
        else
            return false;

        return true;
    }
}
