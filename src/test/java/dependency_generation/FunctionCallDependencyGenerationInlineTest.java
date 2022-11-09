package dependency_generation;

import com.dse.parser.dependency.FunctionCallDependencyGeneration;
import com.dse.parser.object.IFunctionNode;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class FunctionCallDependencyGenerationInlineTest extends AbstractFunctionCallTest {
    final static Logger logger = Logger.getLogger(FunctionCallDependencyGenerationInlineTest.class);

    @Test
    public void test01() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\inlineFunction.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\inlineFunction.cpp\\cube(int)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test02() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\inlineMethod.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\inlineMethod.cpp\\operation\\product()";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }
}