package auto_testcase_generation.cfg;

import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.testdatagen.DirectedAutomatedTestdataGeneration;
import com.dse.logger.AkaLogger;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate (called/nested) control flow graph
 * from source code for statement/branch coverage
 *
 * @author Lamnt
 */
public class NestedCFGGenerationforBranchvsStatementvsBasispathCoverage extends CFGGenerationforBranchvsStatementvsBasispathCoverage
        implements INestedCFGGeneration {

    private IASTFunctionCallExpression expr;
    private List<IFunctionNode> previousCalls = new ArrayList<>();

    public NestedCFGGenerationforBranchvsStatementvsBasispathCoverage(IFunctionNode normalizedFunction) {
        super(normalizedFunction);
    }

    public void setPreviousCalls(List<IFunctionNode> previousCalls) {
        this.previousCalls = previousCalls;
    }

    public List<IFunctionNode> getPreviousCalls() {
        return previousCalls;
    }

    @Override
    protected void preprocessor(IFunctionNode fn) {
        BeginFunctionCallFlagCfgNode BEGIN = new BeginFunctionCallFlagCfgNode();
        BEGIN.setFunction(fn);
        BEGIN.setExpr(expr);

        EndFunctionCallFlagCfgNode END = new EndFunctionCallFlagCfgNode();
        END.setFunction(fn);
        END.setBeginNode(BEGIN);
        END.setExpr(expr);

        this.END = END;
        this.BEGIN = BEGIN;
    }

    @Override
    protected void attachSubCFG(IFunctionNode called, IASTFunctionCallExpression expr,
                                NormalCfgNode cfgNode) throws Exception {
        if (!previousCalls.contains(called)) {
            INestedCFGGeneration cfgGen =
                    new NestedCFGGenerationforBranchvsStatementvsBasispathCoverage(called);

            cfgGen.getPreviousCalls().addAll(previousCalls);
            cfgGen.getPreviousCalls().add(functionNode);
            cfgGen.setExpression(expr);

            ICFG cfg = cfgGen.generateCFG();

            cfgNode.getSubCFGs().put(expr, cfg);
        }
    }

    @Override
    public void setExpression(IASTFunctionCallExpression expr) {
        this.expr = expr;
    }

}
