package auto_testcase_generation.testdatagen;

import com.dse.parser.ProjectParser;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidPtrTypeResolver {

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File("datatest/official_tested_projects/c-algorithms/src"));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setSizeOfDependencyGeneration_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setTypeDependency_enable(true);

        List<IFunctionNode> functions = Search.searchNodes(parser.getRootTree(), new FunctionNodeCondition());
        functions.forEach(f -> {
            Map<String, String> typeMap = new VoidPtrTypeResolver(f).getTypeMap();
            if (!typeMap.isEmpty()) {
                System.out.println("Function " + f.getName());
                System.out.println(typeMap);
            }
        });

    }

    private IASTFunctionDefinition astFunction;
    private final ICommonFunctionNode functionNode;
    private final Map<String, String> typeMap = new HashMap<>();

    public VoidPtrTypeResolver(ICommonFunctionNode fn) {
        functionNode = fn;

        if (fn instanceof MacroFunctionNode)
            astFunction = ((MacroFunctionNode) fn).getCorrespondingFunctionNode().getAST();
        else if (fn instanceof AbstractFunctionNode)
            astFunction = ((AbstractFunctionNode) fn).getAST();

        if (astFunction != null) {
            solve();
        }
    }

    private IVariableNode getParameter(IASTIdExpression operand) {
        String name = operand.getName().getRawSignature();
        return functionNode.getArguments().stream()
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    protected void solve() {
        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                if (expression instanceof IASTCastExpression) {
                    IASTCastExpression castExpr = (IASTCastExpression) expression;
                    IASTExpression operand = castExpr.getOperand();
                    if (operand instanceof IASTIdExpression) {
                        IVariableNode parameter = getParameter((IASTIdExpression) operand);
                        if (parameter != null && VariableTypeUtils.isVoidPointer(parameter.getRealType())) {
                            String type = castExpr.getTypeId().getRawSignature();
                            typeMap.put(parameter.getName(), type);
                        }
                    }
                }

                return PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitExpressions = true;

        astFunction.accept(visitor);
    }

    public Map<String, String> getTypeMap() {
        return typeMap;
    }
}
