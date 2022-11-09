package auto_testcase_generation.instrument;

import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.LambdaFunctionNode;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;

import java.io.File;
import java.util.List;

public class FunctionInstrumentForLambda extends FunctionInstrumentationForAllCoverages{
    private ICPPASTLambdaExpression astLambda;

    public FunctionInstrumentForLambda(LambdaFunctionNode functionNode) {
        astFunctionNode = functionNode.getAST();
        astLambda = functionNode.getOriginalAST();
        functionPath = functionNode.getAbsolutePath();
//        if (!functionNode.getDependencies().isEmpty())
//            functionPath = functionNode.getDependencies().get(0).getStartArrow().getAbsolutePath();
    }

    public static void main(String[] args) {
        ProjectParser projectParser = new ProjectParser(
                new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/cov"));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        List<INode> nodes = Search.searchNodes(projectRoot, new FunctionNodeCondition(), "test(int,int)");
        FunctionNode foo = (FunctionNode) nodes.get(0);
        System.out.println("function = " + foo.getAST().getRawSignature());
//
        FunctionInstrumentationForAllCoverages fnIns = new FunctionInstrumentationForAllCoverages(foo.getAST(), foo);
        String instrument = fnIns.generateInstrumentedFunction();
        System.out.println("instrument = " + instrument);
    }

    @Override
    public String generateInstrumentedFunction() {
        try {
            String body = parseBlock((IASTCompoundStatement) astFunctionNode.getBody(), null, "");
            String oldBody = astLambda.getBody().getRawSignature();
            return astLambda.getRawSignature().replace(oldBody, body);
        } catch (Exception e){
            e.printStackTrace();
            // return the original function without instrumentation
            return astLambda.getRawSignature();
        }
    }
}
