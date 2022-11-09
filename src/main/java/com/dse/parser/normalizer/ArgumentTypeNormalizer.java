package com.dse.parser.normalizer;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.List;

/**
 * Rewrite arguments in the given function. Ex:
 * "Level0MultipleNsTest(X,ns1::X,ns1::ns2::X)"
 * ----------->"Level0MultipleNsTest(::X,ns1::X,ns1::ns2::X)"
 *
 * @author ducanhnguyen
 */
public class ArgumentTypeNormalizer extends AbstractFunctionNormalizer
        implements IFunctionNormalizer {
    final static AkaLogger logger = AkaLogger.get(ArgumentTypeNormalizer.class);

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.CORE_UTILS));
        parser.getIgnoreFolders()
                .add(new File(
                        "/home/ducanhnguyen/Desktop/ava/data-test/ducanh/coreutils-8.24/gnulib-tests"));
        IFunctionNode function = (IFunctionNode) Search.searchNodes(
                parser.getRootTree(), new FunctionNodeCondition(),
                "who.c" + File.separator + "main(int,char**)").get(0);

        System.out.println(function.getAST().getRawSignature());
        ArgumentTypeNormalizer normalizer = new ArgumentTypeNormalizer();
        normalizer.setFunctionNode(function);
        normalizer.normalize();

        System.out.println(normalizer.getTokens());
        System.out.println(normalizer.getNormalizedSourcecode());
    }

    @Override
    public void normalize() {
//		List<IVariableNode> variableNodes = getFunctionNode().getArguments();
//
//		if (variableNodes.size() > 0) {
//			String newVarDeclarations = "";
//			String originalDeclaration = getFunctionNode().getDeclaration();
//			for (IVariableNode variableNode : variableNodes)
//				if (VariableTypes.isStructureOneDimension(variableNode
//						.getRawType())
//						|| VariableTypes.isStructureTwoDimension(variableNode
//								.getRawType())
//						|| VariableTypes.isStructureOneLevel(variableNode
//								.getRawType())
//						|| VariableTypes.isStructureTwoLevel(variableNode
//								.getRawType())) {
//					/*
//					 * Ex: "int PassAsConstRefTest(const BigData& b)" ------->
//					 * "int PassAsConstRefTest(const ::BigData& b)"
//					 */
//					String fullCoreType = variableNode.getFullType();
//					String newType = variableNode.getRawType().replace(
//							variableNode.getCoreType(), fullCoreType);
//					String newVar = "";
//
//					if (newType.indexOf("[") > 0)
//						newVar = newType.substring(0, newType.indexOf("["))
//								+ " " + variableNode.getNewType()
//								+ newType.substring(newType.indexOf("["));
//					else
//						newVar = newType + " " + variableNode.getNewType();
//
//					newVarDeclarations += newVar + ",";
//				} else
//					newVarDeclarations += ((VariableNode) variableNode)
//							.getFullType()
//							+ " "
//							+ ((VariableNode) variableNode).getName() + ",";
//
//			String newFunctionDeclaration = getFunctionNode().getSimpleName()
//					+ "(" + newVarDeclarations + ")";
//			// merge all
//			newFunctionDeclaration = newFunctionDeclaration.replace(",)", ")");
//			normalizeSourcecode = getFunctionNode().getAST().getRawSignature()
//					.replace(originalDeclaration, newFunctionDeclaration);
//		} else
//			normalizeSourcecode = getFunctionNode().getAST().getRawSignature();

        List<IVariableNode> variableNodes = this.getFunctionNode().getArguments();

        if (variableNodes.size() > 0) {
            StringBuilder newVarDeclarations = new StringBuilder();
            String originalDeclaration = this.getFunctionNode().getDeclaration();
            for (IVariableNode variableNode : variableNodes) {
                /*
				 * Ex: "int PassAsConstRefTest(const BigData& b)" ------->
				 * "int PassAsConstRefTest(const ::BigData& b)"
				 */
                String fullCoreType = variableNode.getFullType();
                String newType = variableNode.getRawType().replace(variableNode.getCoreType(), fullCoreType);
                String newVar;

                if (newType.indexOf("[") > 0)
                    newVar = newType.substring(0, newType.indexOf("[")) + " " + variableNode.getName()
                            + newType.substring(newType.indexOf("["));
                else
                    newVar = newType + " " + variableNode.getName();

                newVarDeclarations.append(newVar).append(",");
            }
            String newFunctionDeclaration = this.getFunctionNode().getSimpleName() + "(" + newVarDeclarations + ")";
            // merge all
            newFunctionDeclaration = newFunctionDeclaration.replace(",)", ")");
            this.normalizeSourcecode = this.getFunctionNode().getAST().getRawSignature().replace(originalDeclaration,
                    newFunctionDeclaration);
        } else
            this.normalizeSourcecode = this.getFunctionNode().getAST().getRawSignature();


    }
}
