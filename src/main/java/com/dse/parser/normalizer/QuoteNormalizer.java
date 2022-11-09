package com.dse.parser.normalizer;

import auto_testcase_generation.testdatagen.structuregen.ChangedToken;
import auto_testcase_generation.utils.ASTUtils;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;

import java.io.File;
import java.util.List;

/**
 * cout << "xxxxxxxxxxxxxxxxxxxxx" ----------------> cout << "@1"
 *
 * @author ducanhnguyen
 */
public class QuoteNormalizer extends AbstractFunctionNormalizer implements IFunctionNormalizer {

    public static final String PREFIX_REPLACEMENT = "@#$";
    public static int DEFAULT_ID_TOKEN = 672;
    public static int ID_TOKEN = QuoteNormalizer.DEFAULT_ID_TOKEN;

    public QuoteNormalizer() {
    }

    public QuoteNormalizer(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.SAMPLE01));
        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "disp(Node*)").get(0);

        System.out.println(function.getAST().getRawSignature());
        QuoteNormalizer normalizer = new QuoteNormalizer();
        normalizer.setFunctionNode(function);
        normalizer.normalize();

        System.out.println(normalizer.getTokens());
        System.out.println(normalizer.getNormalizedSourcecode());
    }

    @Override
    public void normalize() {
        QuoteNormalizer.ID_TOKEN = QuoteNormalizer.DEFAULT_ID_TOKEN;

        normalizeSourcecode = functionNode.getAST().getRawSignature();

        List<ICPPASTLiteralExpression> literalExpressions = ASTUtils.getLiteralExpressions(functionNode.getAST());

        for (ICPPASTLiteralExpression literalExpr : literalExpressions) {
            String oldContent = literalExpr.getRawSignature();
            String newContent = "\"" + QuoteNormalizer.PREFIX_REPLACEMENT + QuoteNormalizer.ID_TOKEN++ + "\"";
            normalizeSourcecode = normalizeSourcecode.replace(oldContent, newContent);

            tokens.add(new ChangedToken(oldContent, newContent));
        }
    }

}
