package auto_testcase_generation.cfg.object;

import auto_testcase_generation.cfg.ICFG;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent normal statements (not flag statement, scope statement, etc.)
 *
 * @author ducanh
 */
public abstract class NormalCfgNode extends CfgNode {

	private IASTNode ast;

	private Map<IASTFunctionCallExpression, ICFG> subCFGs;

	public NormalCfgNode(IASTNode node) {
		ast = node;
		setContent(ast.getRawSignature());
		setAstLocation(node.getFileLocation());
		subCFGs = new HashMap<>();
	}

//	@Override
//	public int getId() {
//		return ast.getFileLocation().getNodeOffset() * -1 - 1;
//	}

	public IASTNode getAst() {
		return ast;
	}

	public void setAst(IASTNode ast) {
		if (ast != null) {
			this.ast = ast;
			setContent(ast.getRawSignature());
		}
	}

	@Override
	public String toString() {
		if (ast != null) {
			return ast.getRawSignature();
		} else
			return getContent();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		NormalCfgNode cloneNode = (NormalCfgNode) super.clone();
		cloneNode.setAst(ast);
		return cloneNode;
	}

	public void setSubCFGs(Map<IASTFunctionCallExpression, ICFG> subCFGs) {
		this.subCFGs = subCFGs;
	}

	public Map<IASTFunctionCallExpression, ICFG> getSubCFGs() {
		return subCFGs;
	}
}
