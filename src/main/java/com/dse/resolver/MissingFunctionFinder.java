package com.dse.resolver;

import com.dse.parser.FunctionCallParser;
import com.dse.parser.dependency.finder.MethodFinder;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;

import java.util.ArrayList;
import java.util.List;

public class MissingFunctionFinder implements IMissingFinder {
    private final static AkaLogger logger = AkaLogger.get(MissingFunctionFinder.class);
    private INode root;

    public MissingFunctionFinder(INode root) {
        if (root instanceof ISourcecodeFileNode)
            this.root = root;
        else
            logger.error(root.getName() + "is not a source code file");
    }

    public List<ICommonFunctionNode> getMissingFuntion() {
        List<ICommonFunctionNode> resolved = new ArrayList<>();
        List<INode> completeFunctions = Search.searchNodes(root, new AbstractFunctionNodeCondition());

        int prevSize;
        do {
            prevSize = resolved.size();

            for (INode functionNode : completeFunctions)
                resolved.addAll(getMissingFunctionIn((IFunctionNode) functionNode));
        } while (prevSize != resolved.size());

        return resolved;
    }

    public List<ICommonFunctionNode> getMissingFunctionIn(IFunctionNode owner) {
        IASTFunctionDefinition fnAst = owner.getAST();
        FunctionCallParser visitor = new FunctionCallParser();
        fnAst.accept(visitor);

        List<ICommonFunctionNode> resolvedFunctions = new ArrayList<>();

        for (IASTFunctionCallExpression expression : visitor.getExpressions()) {

            MethodFinder finder = new MethodFinder(owner);
            finder.ignoreArgsType = true;

            INode referredNode = finder.find(expression);
            if (referredNode == null) {
                logger.debug(expression.getFunctionNameExpression().getRawSignature() + " is missing.");
                String funcName = finder.getFunctionSimpleName(expression);
                String returnType = new ReturnTypePrediction(owner).getReturnType(expression);
                IASTInitializerClause[] args = expression.getArguments();
                MissingFuntionGenerator generator;
                ICommonFunctionNode functionNode;
                if (returnType != null && funcName != null) {
                    generator = new MissingFuntionGenerator(returnType, funcName, args);
                    generator.setContext(owner);
                    functionNode = generator.exec();
                } else
                    functionNode = null;

                if (functionNode != null) {
                    // Generate function call dependency
//                    FunctionCallDependency d = new FunctionCallDependency(owner, functionNode);
//                    if (!owner.getDependencies().contains(d)
//                            && !functionNode.getDependencies().contains(d)) {
//                        owner.getDependencies().add(d);
//                        functionNode.getDependencies().add(d);
//                    }
                    resolvedFunctions.add(functionNode);
                    logger.debug("resolve function " + functionNode);
                }
            }
        }

        return resolvedFunctions;
    }

}
