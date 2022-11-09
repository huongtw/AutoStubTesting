package com.dse.parser.dependency.finder;


import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.GlobalVariableNodeCondition;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

import java.io.File;
import java.util.List;

/**
 * Find variable by name
 *
 * @author TungLam
 */
public class VariableFinder {

    /**
     * Node in the structure that contains the searched variable
     */
    private IFunctionNode context;

    public VariableFinder(IFunctionNode context) {
        this.context = context;
    }

    public static void main(String[] args) throws Exception {
        ProjectParser parser = new ProjectParser(new File(Paths.SEPARATE_FUNCTION_TEST));
        FunctionNode context = (FunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), File.separator + "test9(int,A)").get(0);

        IVariableNode node = context.getArguments().get(1);
        IASTDeclSpecifier iastDeclSpecifier = node.getASTType();
        VariableFinder finder = new VariableFinder(context);
        VariableNode var = (VariableNode) finder.find("g_var");
    }

    public INode find(String variableName) {
        List<Level> spaces = new VariableSearchingSpace(context).generateExtendSpaces();
        for (Level l : spaces)
            for (INode n : l) {
                List<INode> completeVariables = Search.searchNodes(n, new GlobalVariableNodeCondition());
                for (INode variable : completeVariables)
                    if (variable.getName().equals(variableName)) {
                        return variable;
                    }
            }

        return null;
    }


}
