package com.dse.parser.normalizer;

import auto_testcase_generation.testdatagen.structuregen.ChangedToken;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.finder.IVariableSearchingSpace;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.VariableNode;
import com.dse.search.Search;
import com.dse.search.condition.ExternVariableNodeCondition;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.VariableNodeCondition;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;

import java.io.File;
import java.util.List;

/**
 * Replace simple value defined in #define preprocessor with its value
 *
 * @author ducanhnguyen
 */
public class DefinePreprocessorNormalizer extends AbstractFunctionNormalizer implements IFunctionNormalizer {

    public DefinePreprocessorNormalizer() {

    }

    public DefinePreprocessorNormalizer(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.TSDV_R1_2));
        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "SimpleMarcoTest(int)").get(0);

        System.out.println(function.getAST().getRawSignature());
        DefinePreprocessorNormalizer normalizer = new DefinePreprocessorNormalizer();
        normalizer.setFunctionNode(function);
        normalizer.normalize();

        System.out.println(normalizer.getTokens());
        System.out.println(normalizer.getNormalizedSourcecode());
    }

    @Override
    public void normalize() {
        normalizeSourcecode = functionNode.getAST().getRawSignature();

        IVariableSearchingSpace space = new VariableSearchingSpace(functionNode);

        for (Level level : space.getSpaces())
            for (INode n : level) {

                List<INode> externsVariables = Search.searchNodes(n, new ExternVariableNodeCondition());

                for (INode externsVariable : externsVariables)
                    if (externsVariable instanceof VariableNode) {
                        /*
                          Convert value of extern variable into corresponding
                          value
                         */
                        String oldName = externsVariable.getNewType();
                        String newName = getValueOfExternalVariable(oldName, Utils.getRoot(n));

                        normalizeSourcecode = normalizeSourcecode.replaceAll("\\b" + oldName + "\\b", newName);

                        tokens.add(new ChangedToken(newName, oldName));
                    }
            }
    }

    /**
     * Get initializer of an extern variable in the current project
     *
     * @param nameExternalVariable
     * @param root
     * @return
     */
    private String getValueOfExternalVariable(String nameExternalVariable, INode root) {
        String value = "";

        List<INode> nodes = Search.searchNodes(root, new VariableNodeCondition());

        for (INode node : nodes)
            if (node instanceof VariableNode) {

                VariableNode n = (VariableNode) node;
                if (!n.isExtern() && n.getNewType().equals(nameExternalVariable)) {
                    IASTInitializer initializer = n.getInitializer();

                    if (initializer != null)
                        return initializer.getChildren()[0].getRawSignature();
                }
            }
        return value;
    }
}
