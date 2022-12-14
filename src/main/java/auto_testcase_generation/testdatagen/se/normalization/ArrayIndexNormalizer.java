package auto_testcase_generation.testdatagen.se.normalization;

import java.util.List;

import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;

import com.dse.parser.normalizer.AbstractStatementNormalizer;
import com.dse.parser.normalizer.IStatementNormalizer;
import auto_testcase_generation.utils.ASTUtils;

/**
 * Add some characters to the expression to make the expression parse easily.
 * Ex: a[1]---->a[1+0]
 *
 * @author DucAnh
 */
public class ArrayIndexNormalizer extends AbstractStatementNormalizer
        implements IPathConstraintNormalizer, IStatementNormalizer {

    public static void main(String[] args) {
        String[] tests = new String[]{"a+b[1]", "a+b[1+1]"};

        for (String test : tests) {
            ArrayIndexNormalizer norm = new ArrayIndexNormalizer();
            norm.setOriginalSourcecode(test);
            norm.normalize();
            System.out.println(norm.getNormalizedSourcecode());
        }
    }

    @Override
    public void normalize() {
        if (originalSourcecode != null && originalSourcecode.length() > 0)
            normalizeSourcecode = addCharacters(originalSourcecode);
        else
            normalizeSourcecode = originalSourcecode;
    }

    /**
     * Add some characters to the expression to make the expression parse
     * easily. Ex: a[1]---->a[1+0]
     *
     * @param statement
     * @return
     */
    private String addCharacters(String statement) {
        String newStatement = statement;
        List<ICPPASTArraySubscriptExpression> arrayItems = ASTUtils
                .getArraySubscriptExpression(Utils.convertToIAST(statement));

        for (ICPPASTArraySubscriptExpression arrayItem : arrayItems) {
            String str = arrayItem.getRawSignature();
            /*
			 * Ex: a[1]---->a[1+0]
			 */
            String newStr = str.replace("]", "+0]");
            newStatement = newStatement.replace(str, newStr);
        }

        return newStatement;
    }
}
