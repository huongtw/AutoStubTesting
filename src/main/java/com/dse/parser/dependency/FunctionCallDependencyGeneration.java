package com.dse.parser.dependency;

import com.dse.environment.Environment;
import com.dse.parser.FunctionCallParser;
import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.finder.MethodFinder;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.TemplateUtils;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import java.io.File;
import java.util.List;

public class FunctionCallDependencyGeneration extends AbstractDependencyGeneration {
    final static AkaLogger logger = AkaLogger.get(FunctionCallDependencyGeneration.class);

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File("/mnt/e/akautauto/datatest/lamnt/stuff/"));
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setExtendedDependencyGeneration_enabled(true);

        Environment.getInstance().setProjectNode(parser.getRootTree());

        List<INode> context =  Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "A/foo()");

        List<INode> template = Search.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "myMax(T,T)");


        FunctionCallDependencyGeneration generation = new FunctionCallDependencyGeneration();
        generation.dependencyGeneration(context.get(0));
        List<DefinitionFunctionNode> p = TemplateUtils.getPossibleTemplateArguments((ICommonFunctionNode) template.get(0));

        System.out.println();
    }

    public void dependencyGeneration(INode root) {
        if (root instanceof IFunctionNode) {
            if (!((IFunctionNode) root).isFunctionCallDependencyState()) {
                IASTFunctionDefinition fnAst = ((IFunctionNode) root).getAST();

                FunctionCallParser visitor = new FunctionCallParser();
                fnAst.accept(visitor);

                checkExpectedList(visitor, (IFunctionNode) root);
                checkUnExpectedList(visitor, (IFunctionNode) root);
                ((IFunctionNode) root).setFunctionCallDependencyState(true);
            } else {
                logger.debug(root.getAbsolutePath() + " is analyzed function call dependency before");
            }

        }
    }

    private void checkExpectedList(FunctionCallParser visitor, IFunctionNode owner) {
        List<IASTFunctionCallExpression> expressions = visitor.getExpressions();

        for (IASTFunctionCallExpression expression : expressions) {
//            IASTExpression ex = expression.getFunctionNameExpression();
//            String funcName = getFunctionName(ex, owner);
//            String funcName = null;
//            if (ex instanceof IASTFieldReference) {
//                String type = new TypeResolver(owner).exec(((IASTFieldReference) ex).getFieldOwner());
//                funcName = type + "::" + ((IASTFieldReference) ex).getFieldName().toString();
//
//                VariableSearchingSpace space = new VariableSearchingSpace(owner);
//                INode typeNode = space.search(type, new StructurevsTypedefCondition());
//                if (typeNode instanceof StructureNode) {
//                    INode parent = typeNode.getParent();
//                    // TODO: structure define in another file
//                    while (parent != null) {
//                        // structure define in a namespace
//                        if (parent instanceof NamespaceNode) {
//                            String namespace = parent.getName();
//                            funcName = namespace + "::" + funcName;
//                        }
//                        parent = parent.getParent();
//                    }
//                }
//                IType type = ((IASTFieldReference) ex).getFieldOwner().getExpressionType();
//                funcName = type.toString() + "::" + ((IASTFieldReference) ex).getFieldName().toString();
//
//                if (type instanceof CPPClassType) {
//                    IASTNode parent = ((CPPClassType) type).getDefinition().getParent();
//                    // TODO: structure define in another file
//                    while (parent != null) {
//                        // structure define in a namespace
//                        if (parent instanceof ICPPASTNamespaceDefinition) {
//                            String namespace = ((ICPPASTNamespaceDefinition) parent).getName().getRawSignature();
//                            funcName = namespace + "::" + funcName;
//                        }
//                        parent = parent.getParent();
//                    }
//                }
//            } else if (ex instanceof IASTIdExpression) {
//                funcName = ((IASTIdExpression) ex).getName().toString();
//            }
//            IASTInitializerClause[] args = expression.getArguments();

            MethodFinder finder = new MethodFinder(owner);
            try {
                INode refferedNode = finder.find(expression);

                if (refferedNode != null) {
                    FunctionCallDependency d = new FunctionCallDependency(owner, refferedNode);
                    if (!owner.getDependencies().contains(d)
                            && !refferedNode.getDependencies().contains(d)) {
                        owner.getDependencies().add(d);
                        refferedNode.getDependencies().add(d);
                        logger.debug("Found a function call dependency: " + d.toString());
                    }
                } else {
                    //logger.debug("expression " + expression.getFunctionNameExpression().getRawSignature() + " in " + owner.getAbsolutePath() + " is not a function!");
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

//    private String getFunctionName(IASTExpression ex, IFunctionNode owner) {
//        String funcName = null;
//        if (ex instanceof IASTFieldReference) {
//            String type = new TypeResolver(owner).exec(((IASTFieldReference) ex).getFieldOwner());
//            funcName = type.replaceAll("[ *]", "") + "::" + ((IASTFieldReference) ex).getFieldName().toString();
//
//            VariableSearchingSpace space = new VariableSearchingSpace(owner);
//            INode typeNode = space.search(type, new StructurevsTypedefCondition());
//            if (typeNode instanceof StructureNode) {
//                INode parent = typeNode.getParent();
//                // TODO: structure define in another file
//                while (parent != null) {
//                    // structure define in a namespace
//                    if (parent instanceof NamespaceNode) {
//                        String namespace = parent.getName();
//                        funcName = namespace + "::" + funcName;
//                    }
//                    parent = parent.getParent();
//                }
//            }
////                IType type = ((IASTFieldReference) ex).getFieldOwner().getExpressionType();
////                funcName = type.toString() + "::" + ((IASTFieldReference) ex).getFieldName().toString();
////
////                if (type instanceof CPPClassType) {
////                    IASTNode parent = ((CPPClassType) type).getDefinition().getParent();
////                    // TODO: structure define in another file
////                    while (parent != null) {
////                        // structure define in a namespace
////                        if (parent instanceof ICPPASTNamespaceDefinition) {
////                            String namespace = ((ICPPASTNamespaceDefinition) parent).getName().getRawSignature();
////                            funcName = namespace + "::" + funcName;
////                        }
////                        parent = parent.getParent();
////                    }
////                }
//        } else if (ex instanceof IASTIdExpression) {
//            funcName = ((IASTIdExpression) ex).getName().toString();
//        }
//
//        return funcName;
//    }

    private void checkUnExpectedList(FunctionCallParser visitor, IFunctionNode owner) {
        List<IASTSimpleDeclaration> names = visitor.getUnexpectedCalledFunctions();

        for (IASTSimpleDeclaration name : names) {

            String funcName = name.getChildren()[0].getRawSignature();

            int nParameters = name.getChildren().length - 1; // ignore the first children because it is corresponding to the name of the alled method

            MethodFinder finder = new MethodFinder(owner);
            try {
                List<INode> refferedNodes = finder.find(funcName, nParameters);
                for (INode refferedNode : refferedNodes) {
                    FunctionCallDependency d = new FunctionCallDependency(owner, refferedNode);
                    if (!owner.getDependencies().contains(d)
                            && !refferedNode.getDependencies().contains(d)) {
                        owner.getDependencies().add(d);
                        refferedNode.getDependencies().add(d);
                        logger.debug("Found an extended dependency: " + d.toString());
                    }
                }
            } catch (Exception e) {
                logger.debug("function " + funcName + " in " + owner.getAbsolutePath() + " can't found!");
            }
        }
    }
}

