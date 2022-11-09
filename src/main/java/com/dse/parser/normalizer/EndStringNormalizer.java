package com.dse.parser.normalizer;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;

import java.io.File;

/**
 * Ex:"while (a[c] != '\0'){"--------------->"while (a[c] != 0){"
 *
 * @author DucAnh
 */
public class EndStringNormalizer extends AbstractFunctionNormalizer implements IFunctionNormalizer {
    public EndStringNormalizer() {

    }

    public EndStringNormalizer(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.SYMBOLIC_EXECUTION_TEST));
        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "check_anagram(char[],char[])").get(0);

        System.out.println(function.getAST().getRawSignature());
        EndStringNormalizer normalizer = new EndStringNormalizer();
        normalizer.setFunctionNode(function);
        normalizer.normalize();

        System.out.println(normalizer.getTokens());
        System.out.println(normalizer.getNormalizedSourcecode());
    }

    @Override
    public void normalize() {
        String content = functionNode.getAST().getRawSignature();
        normalizeSourcecode = content.replace("'\\0'", "0");
    }
}
