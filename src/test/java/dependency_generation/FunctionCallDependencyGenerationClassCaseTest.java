package dependency_generation;

import com.dse.parser.dependency.FunctionCallDependencyGeneration;
import com.dse.parser.object.IFunctionNode;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class FunctionCallDependencyGenerationClassCaseTest extends AbstractFunctionCallTest {
    final static Logger logger = Logger.getLogger(FunctionCallDependencyGenerationClassCaseTest.class);

    @Test
    public void test01() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\cunit\\CUnit",
                "\\Sources\\wxWidget\\wxWidget.cpp\\myOwnTestFuntion(CU_pSuite)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\Sources\\wxWidget\\wxWidget.cpp\\TopFrame::FindSuite(CU_pSuite)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test02() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\cunit\\CUnit",
                "\\Sources\\wxWidget\\wxWidget.cpp\\TopFrame::OnTreeSelectionChanged(wxTreeEvent&)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\Sources\\wxWidget\\wxWidget.cpp\\TopFrame\\DeactivateSuiteInfo(void)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test03() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\class\\classMethod.cpp\\test(int)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\class\\classMethod.cpp\\getSomeWeight(Person*)";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }

    @Test
    public void test04() {
        IFunctionNode functionNode = warm("datatest\\lamnt\\mysample",
                "\\class\\classMethod.cpp\\getSomeWeight(Person*)");

        new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);

        String calledPath = "\\class\\classMethod.cpp\\Person::getWeight()";

        assertEquals(1, findCalledFunctions(functionNode, calledPath).size());
    }
}