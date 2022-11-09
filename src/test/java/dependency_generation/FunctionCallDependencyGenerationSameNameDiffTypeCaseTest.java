package dependency_generation;

import com.dse.parser.dependency.FunctionCallDependencyGeneration;
import com.dse.parser.object.IFunctionNode;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class FunctionCallDependencyGenerationSameNameDiffTypeCaseTest extends AbstractFunctionCallTest {
    final static Logger logger = Logger.getLogger(FunctionCallDependencyGenerationSameNameDiffTypeCaseTest.class);

    @Test
    public void testCallClass() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\callClass()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\calledFunc(Student)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void testCallInt() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\callInt()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\calledFunc(int)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void testCallPointer() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\callPointer()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\calledFunc(int*)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void testCallCastingType() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\callFloat()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\calledFunc(int)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void testCall2Dim() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\call2Dim()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\calledFunc(int[][1])";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }
}