package auto_testcase_generation.cfg;

import auto_testcase_generation.cfg.object.*;
import com.dse.parser.normalizer.FunctionNormalizer;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;

/**
 * Generate control flow graph from source code for sub-condition coverage
 *
 * @author DucAnh
 */
public class CFGGenerationforSubConditionCoverage implements ICFGGeneration {
	final static AkaLogger logger = AkaLogger.get(CFGGenerationforSubConditionCoverage.class);

	private IFunctionNode functionNode;

	public CFGGenerationforSubConditionCoverage(IFunctionNode fn) {
		this.functionNode = fn;
	}

	public static void main(String[] args) throws Exception {
		ProjectParser parser = new ProjectParser(new File(Paths.SYMBOLIC_EXECUTION_TEST));
		parser.setExpandTreeuptoMethodLevel_enabled(true);
		INode function = Search
				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "Merge2(int[],int[],int[],int,int)")
				.get(0);

		System.out.println(((IFunctionNode) function).getAST().getRawSignature());
		FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
		String normalizedCoverage = fnNorm.getNormalizedSourcecode();
		IFunctionNode clone = (IFunctionNode) function.clone();
		clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));

		CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);
		ICFG cfg = cfgGen.generateCFG();
		cfg.setIdforAllNodes();
		System.out.println(cfg.toString());
	}

	@Override
	public ICFG generateCFG() throws Exception {
		return parse(functionNode);
	}

	private ICFG parse(IFunctionNode functionNode) throws Exception {
		ICFG cfg = new CFGGenerationforBranchvsStatementvsBasispathCoverage(functionNode).generateCFG();
		cfg.setIdforAllNodes();

		for (ICfgNode cfgNode : cfg.getAllNodes()) {
			if (cfgNode instanceof ConditionCfgNode && !(cfgNode instanceof IConditionElementCfgNode)) {
				cfgNode.setAstLocation(((ConditionCfgNode) cfgNode).getAst().getFileLocation());
			}
		}

		boolean containComplexConditions = true;
		while (containComplexConditions) {
			containComplexConditions = false;

			for (ICfgNode cfgNode : cfg.getAllNodes())
				if (cfgNode instanceof ConditionCfgNode && isComplexCondition((ConditionCfgNode) cfgNode)) {
					createGraphForComplexCondition(functionNode, (ConditionCfgNode) cfgNode, cfg);
					containComplexConditions = true;
					break;
				}
		}

		return cfg;
	}

	private void createGraphForComplexCondition(IFunctionNode fn, ConditionCfgNode complexConditionNode, ICFG cfg) {
		IASTNode ast = Utils.shortenAstNode(complexConditionNode.getAst());
		if (ast instanceof IASTBinaryExpression) {
			IASTBinaryExpression astBin = (IASTBinaryExpression) ast;

			IASTExpression left = astBin.getOperand1();
			IASTExpression right = astBin.getOperand2();

			ConditionCfgNode leftNode = null;
			ConditionCfgNode rightNode = null;

			switch (astBin.getOperator()) {
			case IASTBinaryExpression.op_logicalAnd:
			case IASTBinaryExpression.op_logicalOr:
				if (complexConditionNode instanceof ConditionDoCfgNode) {
					leftNode = new ConditionElementDoCfgNode(Utils.shortenAstNode(left));
					rightNode = new ConditionElementDoCfgNode(Utils.shortenAstNode(right));
//					leftNode.setAstLocation(left.getFileLocation());
//					rightNode.setAstLocation(right.getFileLocation());

				} else if (complexConditionNode instanceof ConditionIfCfgNode) {
					leftNode = new ConditionElementIfCfgNode(Utils.shortenAstNode(left));
					rightNode = new ConditionElementIfCfgNode(Utils.shortenAstNode(right));
//					leftNode.setAstLocation(left.getFileLocation());
//					rightNode.setAstLocation(right.getFileLocation());

				} else if (complexConditionNode instanceof ConditionForCfgNode) {
					leftNode = new ConditionElementForCfgNode(Utils.shortenAstNode(left));
					rightNode = new ConditionElementForCfgNode(Utils.shortenAstNode(right));
//					leftNode.setAstLocation(left.getFileLocation());
//					rightNode.setAstLocation(right.getFileLocation());

				} else if (complexConditionNode instanceof ConditionWhileCfgNode) {
					leftNode = new ConditionElementWhileCfgNode(Utils.shortenAstNode(left));
					rightNode = new ConditionElementWhileCfgNode(Utils.shortenAstNode(right));
//					leftNode.setAstLocation(left.getFileLocation());
//					rightNode.setAstLocation(right.getFileLocation());
				}
			}

			// Create new node
			if (leftNode != null && rightNode != null) {
				ICfgNode parent = complexConditionNode.getParent();
				ICfgNode falseBranch = complexConditionNode.getFalseNode();
				ICfgNode trueBranch = complexConditionNode.getTrueNode();

				switch (astBin.getOperator()) {
				case IASTBinaryExpression.op_logicalAnd:
					leftNode.setParent(parent);
					leftNode.setTrue(rightNode);
					leftNode.setFalse(falseBranch);

					rightNode.setParent(leftNode);
					rightNode.setTrue(trueBranch);
					rightNode.setFalse(falseBranch);
					break;

				case IASTBinaryExpression.op_logicalOr:
					leftNode.setParent(parent);
					leftNode.setTrue(trueBranch);
					leftNode.setFalse(rightNode);

					rightNode.setParent(leftNode);
					rightNode.setTrue(trueBranch);
					rightNode.setFalse(falseBranch);
					break;
				}

				// Update all nodes
				leftNode.setId(cfg.getMaxId() + 1);
				rightNode.setId(cfg.getMaxId() + 2);
				cfg.getAllNodes().add(leftNode);
				cfg.getAllNodes().add(rightNode);
				cfg.getAllNodes().remove(complexConditionNode);

				for (ICfgNode node : cfg.getAllNodes())
					if (node instanceof ScopeCfgNode) {
						ScopeCfgNode castNode = (ScopeCfgNode) node;
						if (castNode.getFalseNode().equals(complexConditionNode)
								|| castNode.getTrueNode().equals(complexConditionNode))
							node.setBranch(leftNode);

					} else if (node instanceof NormalCfgNode) {

						if (node instanceof ConditionCfgNode) {
							ConditionCfgNode castNode = (ConditionCfgNode) node;

							if (castNode.getTrueNode().equals(complexConditionNode))
								castNode.setTrue(leftNode);
							else if (castNode.getFalseNode().equals(complexConditionNode))
								castNode.setFalse(leftNode);

						} else if (node instanceof SimpleCfgNode) {
							SimpleCfgNode castNode = (SimpleCfgNode) node;
							if (castNode.getFalseNode().equals(complexConditionNode)
									|| castNode.getTrueNode().equals(complexConditionNode))
								node.setBranch(leftNode);
						}
					}
			}
		}
	}

	private boolean isComplexCondition(ConditionCfgNode cfgNode) {
		boolean isComplexCon = false;

		IASTNode ast = cfgNode.getAst();
		if (ast != null) {
			ast = Utils.shortenAstNode(ast);
			if (ast instanceof IASTBinaryExpression) {
				IASTBinaryExpression astBinary = (IASTBinaryExpression) ast;
				switch (astBinary.getOperator()) {
				case IASTBinaryExpression.op_logicalAnd:
				case IASTBinaryExpression.op_logicalOr:
					isComplexCon = true;
					break;
				default:
					break;
				}
			}
		}
		return isComplexCon;
	}

	@Override
	public IFunctionNode getFunctionNode() {
		return functionNode;
	}

	@Override
	public void setFunctionNode(IFunctionNode functionNode) {
		this.functionNode = functionNode;
	}
}
