package com.dse.parser.object;

import com.dse.logger.AkaLogger;
import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.util.Utils;
import com.dse.util.tostring.ReducedDependencyTreeDisplayer;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;

import java.io.File;
import java.util.List;

public class ParseToLambdaFunctionNode{

    private static final AkaLogger logger = AkaLogger.get(ParseToLambdaFunctionNode.class);

    public INode parse(INode n) {
        List<INode> list = Search.searchNodes(n, new AbstractFunctionNodeCondition());
        for (INode temp : list) {
            AbstractFunctionNode functionNode = (AbstractFunctionNode) temp;
            IASTNode astNode = functionNode.getAST();

            ASTVisitor visitor = new ASTVisitor() {
                @Override
                public int visit(IASTExpression expression) {
                    if (expression instanceof ICPPASTLambdaExpression){
                        LambdaFunctionNode newNode = new LambdaFunctionNode();
                        newNode.setBody(expression);
                        INode parent;
                        if (functionNode.getRealParent() != null)
                            parent = functionNode.getRealParent();
                        else parent = functionNode.getParent();
                        parent.getChildren().add(newNode);
                        newNode.setParent(parent);
                        newNode.setOriginalAST((ICPPASTLambdaExpression) expression);
                        newNode.setAbsolutePath(parent.getAbsolutePath() + File.separator + newNode.getName());
                        FunctionCallDependency dependency = new FunctionCallDependency(functionNode, newNode);
                        functionNode.getDependencies().add(dependency);
                        newNode.getDependencies().add(dependency);
                    }
                    return PROCESS_CONTINUE;
                }
            };
            visitor.shouldVisitExpressions = true;
            astNode.accept(visitor);
        }
        return n;
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Utils.normalizePath("datatest/chung")));
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode rootTree = parser.getRootTree();
        ParseToLambdaFunctionNode lambdaParser = new ParseToLambdaFunctionNode();
        lambdaParser.parse(rootTree);

        System.out.println(new ReducedDependencyTreeDisplayer(rootTree).getTreeInString());
    }
}
