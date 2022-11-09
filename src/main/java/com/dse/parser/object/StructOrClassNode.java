package com.dse.parser.object;

import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.ExtendDependency;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;

import java.util.ArrayList;
import java.util.List;

public abstract class StructOrClassNode extends StructureNode {
    protected boolean extendDependencyState = false;

    protected int visibility;

    protected List<String> extendedNames = new ArrayList<>();

    protected ArrayList<ArrayList<INode>> extendPaths = new ArrayList<>();

    private List<String> getExtendNames() {
        List<String> output = new ArrayList<>();

        IASTDeclSpecifier d = AST.getDeclSpecifier();
        ICPPASTDeclSpecifier d1 = (ICPPASTDeclSpecifier) d;

        if (d1 instanceof CPPASTCompositeTypeSpecifier)
            for (ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier b : ((CPPASTCompositeTypeSpecifier) d1).getBaseSpecifiers())
                output.add(b.getNameSpecifier().getRawSignature());

        return output;
    }

    private void getExtendPaths(INode n, ArrayList<INode> path) {
        path.add(n);

        List<INode> extendedNodes = ((StructOrClassNode) n).getExtendNodes();
        if (extendedNodes.size() > 0)
            for (INode child : extendedNodes)
                this.getExtendPaths(child, path);
        else
            extendPaths.add((ArrayList<INode>) path.clone());
        path.remove(path.size() - 1);
    }

    public int getVisibility() {
        if (visibility > 0)
            return visibility;

        INode realParent = getParent();

        if (!(realParent instanceof StructureNode)) {
            visibility = ICPPASTVisibilityLabel.v_public;
        } else {
            IASTSimpleDeclaration thisAst = getAST();

            IASTSimpleDeclaration astStructure = ((StructureNode) realParent).AST;
            IASTDeclSpecifier declSpec = astStructure.getDeclSpecifier();

            visibility = realParent instanceof ClassNode ?
                    ICPPASTVisibilityLabel.v_private : ICPPASTVisibilityLabel.v_public;

            if (declSpec instanceof IASTCompositeTypeSpecifier) {
                IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpec).getDeclarations(true);
                for (IASTDeclaration declaration : declarations) {
                    if (declaration instanceof ICPPASTVisibilityLabel)
                        visibility = ((ICPPASTVisibilityLabel) declaration).getVisibility();
                    else if (declaration instanceof IASTSimpleDeclaration) {
                        if (declaration == thisAst) {
                                return visibility;
                        }
                    }
                }
            }
        }

        return visibility;
    }


    /**
     * Find all functions declared in the structure node <br/>
     * Ex:<br/>
     * class A{<br/>
     * void x(int a){...}<br/>
     * void y(int a){...}<br/>
     * } <br/>
     * <p>
     * With the given name "x", it returns the node of the function "x(int)"
     *
     * @param simpleName the name of the function
     * @return
     */
    public List<FunctionNode> findFunctionsBySimpleName(String simpleName) {
        List<FunctionNode> output = new ArrayList<>();
        for (INode child : getChildren())
            if (child instanceof FunctionNode) {
                FunctionNode n = (FunctionNode) child;
                if (n.getSimpleName().equals(simpleName))
                    output.add(n);
            }
        return output;
    }

    @Deprecated
    public List<INode> getExtendNodes() {
        List<INode> extendedNode = new ArrayList<>();
        for (Dependency d : getDependencies())
            // A extends B: A is start node, B is end node
            if (d instanceof ExtendDependency && d.getEndArrow().equals(this))
                extendedNode.add(d.getStartArrow());
        return extendedNode;
    }

    /**
     * A extend B, B extend C.
     * <p>
     * Consider A, this function return B and C
     *
     * @param derivedNodes
     * @param node
     */
    private void recurGetDerivedNodes(List<INode> derivedNodes, INode node) {
        if (node instanceof StructureNode) {
            derivedNodes.add(node);
            for (Dependency d : node.getDependencies())
                if (d instanceof ExtendDependency && d.getEndArrow().equals(node))
                    recurGetDerivedNodes(derivedNodes, d.getStartArrow());
        }
    }

    /**
     * A extend B, B extend C.
     * <p>
     * Consider C, this function return B and A
     *
     * @param derivedNodes
     * @param node
     */
    private void recurGetBaseNodes(List<INode> derivedNodes, INode node) {
        if (node instanceof StructureNode) {
            derivedNodes.add(node);
            for (Dependency d : node.getDependencies())
                if (d instanceof ExtendDependency && d.getStartArrow().equals(node))
                    recurGetDerivedNodes(derivedNodes, d.getEndArrow());
        }
    }

    public List<INode> getDerivedNodes() {
        List<INode> derivedNodes = new ArrayList<>();
        recurGetDerivedNodes(derivedNodes, this);
        return derivedNodes;
    }

    /**
     * Tìm tất cả các base node của structure node hiện tại
     * Ex: class A extend class B,
     * class B extend class C
     * -----------------------> return "B", "C"
     *
     * @return danh sách các base node
     */
    public List<INode> getBaseNodes() {
        List<INode> baseNodes = new ArrayList<>();
        recurGetBaseNodes(baseNodes, this);
        return baseNodes;
    }

    private void recurGetBaseNodes(List<INode> list, StructureNode node) {
        List<INode> extendNodes = new ArrayList<>();

        for (Dependency d : getDependencies())
            if (d instanceof ExtendDependency && d.getStartArrow().equals(node))
                extendNodes.add(d.getEndArrow());

        for (INode extendNode : extendNodes)
            if (!list.contains(extendNode))
                list.add(extendNode);

        for (INode n : extendNodes) {
            if (n instanceof StructureNode)
                recurGetBaseNodes(list, (StructureNode) n);
        }
    }

    public ArrayList<ArrayList<INode>> getExtendPaths() {
        ArrayList<INode> path = new ArrayList<>();
        this.getExtendPaths(this, path);
        return extendPaths;
    }

    public List<String> getExtendedNames() {
        return extendedNames;
    }

    /**
     * Get all attributes of the given structure node and its base nodes
     * Ex: class A{int a; int b}
     * extend class B{int c; int d}
     * -----------------------> return "a", "b", "c", "d"
     *
     * @return
     */
    public ArrayList<IVariableNode> getAllAttributes() {
        ArrayList<IVariableNode> attributes = getAttributes();

        for (INode node : getBaseNodes())
            if (node instanceof StructureNode)
                attributes.addAll(((StructureNode) node).getAttributes());

        return attributes;
    }


    @Override
    public void setAST(IASTSimpleDeclaration aST) {
        super.setAST(aST);
        extendedNames = getExtendNames();
    }

    public void setExtendDependencyState(boolean extendDependencyState) {
        this.extendDependencyState = extendDependencyState;
    }

    public boolean isExtendDependencyState() {
        return extendDependencyState;
    }

    public boolean isAbstract() {
        for (INode child : getChildren()) {
            if (child instanceof ICommonFunctionNode) {
                String returnType = ((ICommonFunctionNode) child).getReturnType();
                if (returnType.contains("virtual ")) {
                    return true;
                }
            }
        }

        return false;
    }
}
