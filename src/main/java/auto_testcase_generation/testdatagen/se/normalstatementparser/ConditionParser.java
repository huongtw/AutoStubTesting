package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import com.dse.environment.Environment;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.EnumNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.EnumNodeCondition;
import com.dse.util.IRegex;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionParser extends StatementParser {

    private String newConstraint = "";

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);
        newConstraint = ExpressionRewriterUtils.rewrite(table, ast.getRawSignature());

//        //replace enum with normal index of array
//        List<String[]> enumItemList = new ArrayList<>();
//        //find all enum related to containing file
//        List<INode> uutList = Environment.getInstance().getUUTs();
//        List<Level> dependentFileLevel = new ArrayList<>();
//        for (int i = 0; i < uutList.size(); i++){
//            dependentFileLevel.addAll(new VariableSearchingSpace(uutList.get(i)).getSpaces());
//        }
//        for (Level fileLevel : dependentFileLevel) {
//            List<EnumNode> enumlist = Search.searchNodes(fileLevel.get(0), new EnumNodeCondition());
//            for (EnumNode enumNode : enumlist){
//                enumItemList.addAll(enumNode.getAllEnumItems());
//            }
//        }
//        for (String[] item : enumItemList) {
//            if (newConstraint.contains(item[0]))
//                newConstraint = newConstraint.replaceAll("\\b\\s*"+item[0]+"\\s*\\b", item[1]);
//        }
    }

    public String getNewConstraint() {
        return newConstraint;
    }

    private static String removeRedundantBrackets(String constraint) {
        String rewriteStm = constraint;

        String pattern = "\\b" + IRegex.OPENING_PARETHENESS + IRegex.SPACES
                + IRegex.NAME_REGEX + IRegex.SPACES + IRegex.CLOSING_PARETHENESS + "\\b";

        // Create a pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(rewriteStm);

        while (m.find()) {
            String fullExpr = m.group(0);
            String body = fullExpr.replaceAll(IRegex.OPENING_PARETHENESS, SpecialCharacter.EMPTY)
                    .replaceAll(IRegex.CLOSING_PARETHENESS, SpecialCharacter.EMPTY);
            rewriteStm = rewriteStm.replace(fullExpr, body);
            m = r.matcher(rewriteStm);
        }

        rewriteStm = rewriteStm.replaceAll("^" + IRegex.OPENING_PARETHENESS + IRegex.SPACES
                + "(" + IRegex.NAME_REGEX  + ")" + IRegex.SPACES + IRegex.CLOSING_PARETHENESS, "$1").trim();

        return rewriteStm;
    }

    public void setNewConstraint(String newConstraint) {
        this.newConstraint = newConstraint;
    }
}
