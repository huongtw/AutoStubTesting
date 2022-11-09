package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDirective;

import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;

/**
 * Parse "using namespace xxx"
 *
 * @author ducanhnguyen
 */
public class UsingNamespaceParser extends StatementParser {

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        if (ast instanceof CPPASTDeclarationStatement) {
            IASTNode firstChild = ast.getChildren()[0];
            if (firstChild instanceof CPPASTUsingDirective) {
                IASTNode nameSpace = firstChild.getChildren()[0];
                table.setCurrentNameSpace(nameSpace.getRawSignature());
            }
        }
    }

}
