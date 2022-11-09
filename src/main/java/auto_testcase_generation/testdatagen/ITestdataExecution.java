package auto_testcase_generation.testdatagen;

import auto_testcase_generation.testdata.object.TestpathString_Marker;
import auto_testcase_generation.testdatagen.structuregen.ChangedTokens;
import com.dse.testdata.gen.module.IDataTreeGeneration;

/**
 * Executing function under test data to get test path
 *
 * @author DucAnh
 */
public interface ITestdataExecution {

	String UNDEFINED_SOLUTION = "";
	String UNDEFINED_TESTPATH = "";

	IDataTreeGeneration getDataGen();

	TestpathString_Marker getEncodedTestpath();

	void setEncodedTestpath(TestpathString_Marker testpath);

	String normalizeTestpathFromFile(String testpath);

	String getInitialization();

	void setInitialization(String initialization);

	ChangedTokens getChangedTokens();

	void setChangedTokens(ChangedTokens changedTokens);

}