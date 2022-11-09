package com.dse.parser.normalizer;

import java.util.ArrayList;
import java.util.List;

import auto_testcase_generation.testdatagen.se.memory.pointer.PointerCharacterSymbolicVariable;
import com.dse.config.IFunctionConfig;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.IVariableNodeTable;

public class StrlenNormalizer extends AbstractStatementNormalizer implements IStatementNormalizer {

    /**
     * Belong header <cstring>
     */
    public static final String STRLEN = "strlen";

    protected IVariableNodeTable tableVar;

    protected IFunctionConfig functionConfig;

    @Override
    public void normalize() {
        normalizeSourcecode = originalSourcecode;
        IASTNode ast = Utils.convertToIAST(originalSourcecode);

        List<IASTFunctionCallExpression> functionCalls = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor() {

            @Override
            public int visit(IASTExpression statement) {
                if (statement instanceof IASTFunctionCallExpression) {
                    IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression) statement;
                    functionCalls.add(functionCall);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        for (IASTFunctionCallExpression functionCall : functionCalls) {
            String functionName = functionCall.getFunctionNameExpression().getRawSignature();
            if (functionName.equals(StrlenNormalizer.STRLEN)) {
                IASTInitializerClause firstArgument = functionCall.getArguments()[0];
                String nameVar = firstArgument.getRawSignature();
                ISymbolicVariable var = tableVar.findorCreateVariableByName(nameVar);

                if (var instanceof PointerCharacterSymbolicVariable) {
                    String size = ((PointerCharacterSymbolicVariable) var).getSize();
                    int level = ((PointerCharacterSymbolicVariable) var).getLevel();
                    if (level == 1)
                        normalizeSourcecode = normalizeSourcecode
                                .replace(functionCall.getRawSignature(), size);
                }
            }
        }
    }

    public IVariableNodeTable getTableVar() {
        return tableVar;
    }

    public void setTableVar(IVariableNodeTable tableVar) {
        this.tableVar = tableVar;
    }

}
