package dependency_generation;

import com.dse.parser.dependency.FunctionCallDependencyGeneration;
import com.dse.parser.object.IFunctionNode;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FunctionCallDependencyGenerationNamespaceCaseTest extends AbstractFunctionCallTest {
    final static Logger logger = Logger.getLogger(FunctionCallDependencyGenerationNamespaceCaseTest.class);

    @Test
    public void test1() {
        IFunctionNode functionNode = warm("datatest\\duc-anh\\NamespaceMergerTest",
                "\\main.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\main.cpp\\A\\C1\\setX(int)";
        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test2() {
        IFunctionNode functionNode = warm("datatest\\duc-anh\\NamespaceMergerTest",
                "\\main.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\main.cpp\\A\\C2\\setX(int)";
        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test3() {
        IFunctionNode functionNode = warm("datatest\\duc-anh\\NamespaceMergerTest",
                "\\main.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\main.cpp\\B\\C1\\getX()";
        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test4() {
        IFunctionNode functionNode = warm("datatest\\duc-anh\\NamespaceMergerTest",
                "\\main.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\main.cpp\\A\\B\\test(int)";
        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test5() {
        IFunctionNode functionNode = warm("datatest\\duc-anh\\NamespaceMergerTest",
                "\\main.cpp\\main()");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\header\\B.h\\B\\test(C1)";
        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test6() {
        IFunctionNode functionNode = warm("datatest\\duc-anh\\NamespaceMergerTest",
                "\\main.cpp\\A\\B\\test2(int)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\main.cpp\\A\\B\\test(int)";
        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }
}