package auto_testcase_generation.cfg;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;

import java.util.List;

/**
 * This interface is used to generate nested CFG
 *
 * @author lamnt
 */
public interface INestedCFGGeneration extends ICFGGeneration {

	void setExpression(IASTFunctionCallExpression expr);

	void setPreviousCalls(List<IFunctionNode> previousCalls);

	List<IFunctionNode> getPreviousCalls();
}
