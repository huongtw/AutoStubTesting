package com.dse.parser.object;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;

import java.io.File;

public class FunctionNode extends AbstractFunctionNode {

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.SAMPLE01));
        FunctionNode functionNode = (FunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(),"StackLinkedList::push(Node*)").get(0);

        String name = functionNode.getSimpleName();
//        IASTFunctionDefinition fnAst = functionNode.getAST();
        IASTNode fnAst = functionNode.getAST();

        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                if (expression instanceof IASTFunctionCallExpression) {
                    String name = ((IASTFunctionCallExpression) expression).getFunctionNameExpression().toString();
                    IASTInitializerClause[] arguments = ((IASTFunctionCallExpression) expression).getArguments();
                    for (IASTInitializerClause argument : arguments) {
                        if (argument instanceof IASTExpression) {
                            String typeArg = ((IASTExpression) argument).getExpressionType().toString();
                        }
                    }
                }
                return PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;

        ASTVisitor visitor1 = new ASTVisitor(true) {
            @Override
            public int visit(IASTName name) {
                IBinding binding = name.resolveBinding();
                if (binding instanceof IVariable) {
                    IType type = ((IVariable) binding).getType();
                }
                return PROCESS_CONTINUE;
            }
        };

        fnAst.accept(visitor);
//        System.out.println();
    }
}
