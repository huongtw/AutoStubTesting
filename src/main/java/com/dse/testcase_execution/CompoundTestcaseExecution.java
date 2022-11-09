package com.dse.testcase_execution;

import auto_testcase_generation.testdata.object.TestpathString_Marker;
import com.dse.config.CommandConfig;
import com.dse.testcase_execution.testdriver.*;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.stub_manager.StubManager;
import com.dse.testcase_manager.*;
import com.dse.util.TestPathUtils;
import com.dse.util.Utils;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Execute a test case
 */
public class CompoundTestcaseExecution extends AbstractTestcaseExecution {
    private List<TestCase> elements = new ArrayList<>();

    @Override
    public void execute() throws Exception {
        if (!(getTestCase() instanceof CompoundTestCase)) {
            logger.error(getTestCase().getName() + " is not a compound test case");
            return;
        }

        CompoundTestCase testCase = (CompoundTestCase) getTestCase();

        for (ITestCase element : elements)
            initializeConfigurationOfTestcase(element);
        initializeConfigurationOfTestcase(testCase);
        
        testDriverGen = generateTestDriver(testCase);

        if (testDriverGen != null) {
            for (TestCase element : elements)
                StubManager.generateStubCode(element);

            CommandConfig testCaseCommandConfig = initializeCommandConfigToRunTestCase(testCase);

            IDriverGenMessage compileAndLinkMessage = compileAndLink(testCaseCommandConfig);

            if (!compileAndLinkMessage.isEmpty())
                logger.debug(String.format("Compile & Link Message:\n%s\n", compileAndLinkMessage));

            // Run the executable file
            if (new File(testCase.getExecutableFile()).exists()) {
                logger.debug("Execute " + testCase.getExecutableFile());

                String message = runExecutableFile(testCaseCommandConfig);
                testCase.setExecuteLog(message);

                logger.debug("Execute done");

                cloneTestPathForElements();
                cloneExecutionResultForElements();

                if (getMode() == IN_DEBUG_MODE) {
                    // nothing to do
                } else {
                    if (new File(testCase.getTestPathFile()).exists()) {
                        // Read hard disk until the test path is written into file completely
                        TestpathString_Marker encodedTestpath = readTestpathFromFile(testCase);

                        // shorten test path if it is too long
                        encodedTestpath = shortenTestpath(encodedTestpath);

                        if (encodedTestpath.getEncodedTestpath().length > 0) {
                            UILogger.getUiLogger().logToBothUIAndTerminal("Retrieve the test path file successfully.");
                            UILogger.getUiLogger()
                                    .logToBothUIAndTerminal("Generate test paths for " + testCase.getName() + " successfully");

                        } else {
                            String msg = "The content of test path file is empty after execution";
                            UILogger.getUiLogger().logToBothUIAndTerminal(msg);
                            if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                                    ||*/ getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                                UIController.showErrorDialog(msg, "Test case execution", "Fail");
                                testCase.setStatus(TestCase.STATUS_FAILED);
                                return;
                            }
                        }
                    } else {
                        String msg = "Does not found the test path file when executing " + testCase.getExecutableFile();
                        logger.error(msg);
                        if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                                ||*/ getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                            UIController.showErrorDialog(msg, "Test case execution", "Fail");
                            testCase.setStatus(TestCase.STATUS_FAILED);
                            return;
                        }
                    }
                }
            } else {
                String msg = "Can not generate executable file " + testCase.getExecutableFile();
                UILogger.getUiLogger().logToBothUIAndTerminal(msg);
                if (getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                    final String title = "Executable file generation";
                    String headText, content;
                    if (!compileAndLinkMessage.isCompileSuccess()) {
                        headText = "Failed to compile test driver";
                        content = compileAndLinkMessage.getCompileMessage();
                    } else {
                        headText = "Failed to link test driver";
                        content = compileAndLinkMessage.getLinkMessage();
                    }

                    UIController.showDetailDialog(Alert.AlertType.ERROR, title, headText, content);
                    testCase.setStatus(TestCase.STATUS_FAILED);

                    return;
                }
            }
        }
        testCase.setStatus(TestCase.STATUS_SUCCESS);
    }

    @Override
    public TestDriverGeneration generateTestDriver(ITestCase testCase) throws Exception {
        TestDriverGeneration testDriver = null;

        // create the right version of test driver generation
        switch (getMode()) {
            case IN_AUTOMATED_TESTDATA_GENERATION_MODE:

            case IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE:
                /*case IN_EXECUTION_WITHOUT_GTEST_MODE: */{
                initializeCommandConfigToRunTestCase(testCase);
                if (Environment.getInstance().isC()){
                    testDriver = new TestDriverGenerationForC();

                } else {
                    testDriver = new TestDriverGenerationForCpp();
                }
                break;
            }

            case IN_DEBUG_MODE: {
                initializeCommandConfigToRunTestCase(testCase);
                if (Environment.getInstance().isC()){
                    testDriver = new TestDriverGenerationForCDebugger();
                } else {
                    testDriver = new TestDriverGenerationForCppDebugger();
                }
                break;
            }
        }

        if (testDriver != null) {
            // generate test driver
            testDriver.setTestCase(testCase);
            testDriver.generate();

            Utils.writeContentToFile(testDriver.getTestDriver(), testCase.getSourceCodeFile());
            logger.debug("Add test driver to " + testCase.getSourceCodeFile() + " done");
        }

        return testDriver;
    }

    @Override
    public void setTestCase(ITestCase testcase) {
        super.setTestCase(testcase);

        if (testcase instanceof CompoundTestCase) {
            for (TestCaseSlot slot : ((CompoundTestCase) testcase).getSlots()) {
                TestCase element = TestCaseManager.getBasicTestCaseByName(slot.getTestcaseName());
                if (element != null)
                    elements.add(element);
            }
        }
    }

    public List<TestCase> getElements() {
        return elements;
    }

    public void setElements(List<TestCase> elements) {
        this.elements = elements;
    }

    private void cloneExecutionResultForElements() {
        refactorResultTrace(getTestCase());

//        File compoundResultFile = new File(getTestCase().getExecutionResultFile());
        File compoundTraceFile = new File(getTestCase().getExecutionResultTrace());

        for (TestCase element : elements) {
            try {
//                File elementResultFile = new File(element.getExecutionResultFile());
//                Utils.copyFolder(compoundResultFile, elementResultFile);

                File elementTraceFile = new File(element.getExecutionResultTrace());
                Utils.copy(compoundTraceFile, elementTraceFile);
            } catch (Exception ex) {
                logger.error("Cant clone result file because " + ex.getMessage());
            }
        }
    }

    private void cloneTestPathForElements() {
        String compoundTestPath = Utils.readFileContent(getTestCase().getTestPathFile());

        for (int i = 0; i < elements.size(); i++)
            cloneTestPathFor(i, compoundTestPath);
    }

    private void cloneTestPathFor(int index, String compoundTestPath) {
        TestCase current = elements.get(index);

        String beginTag = TestPathUtils.BEGIN_TAG + current.getName().toUpperCase();
        int beginPos = compoundTestPath.indexOf(beginTag);

        int endPos = compoundTestPath.length();

        if (index < elements.size() - 1) {
            String endTag = TestPathUtils.BEGIN_TAG + elements.get(index + 1).getName().toUpperCase();
            endPos = compoundTestPath.indexOf(endTag);
        }

        String elementTestPath = compoundTestPath.substring(beginPos, endPos);
        String[] prevEvents = compoundTestPath.substring(0, beginPos).split("\\R");

        int skip = 0;

        for (String prevEvent : prevEvents)
            if (prevEvent.startsWith(TestPathUtils.CALLING_TAG) || prevEvent.startsWith(TestPathUtils.RETURN_TAG))
                skip++;

        if (skip > 0)
            elementTestPath = String.format("%s%d\n", TestPathUtils.SKIP_TAG, skip) + elementTestPath;

        Utils.writeContentToFile(elementTestPath, current.getTestPathFile());
    }
}