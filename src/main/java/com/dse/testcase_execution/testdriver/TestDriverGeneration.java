package com.dse.testcase_execution.testdriver;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search;
import com.dse.search.Search2;
import com.dse.stub_manager.StubManager;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_manager.*;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.*;

import java.util.*;

import static com.dse.project_init.ProjectClone.wrapInIncludeGuard;

/**
 * Generate test driver for a function
 *
 * @author Vu + D.Anh
 */
public abstract class TestDriverGeneration extends DriverGeneration implements ITestDriverGeneration {

    protected final static AkaLogger logger = AkaLogger.get(TestDriverGeneration.class);

    protected List<String> testScripts;

    protected ITestCase testCase;

    protected String testPathFilePath;

    protected List<String> clonedFilePaths;

    @Override
    public void generate() throws Exception {
        testPathFilePath = testCase.getTestPathFile();

        testScripts = new ArrayList<>();
        clonedFilePaths = new ArrayList<>();

        if (testCase instanceof TestCase) {
            String script = generateTestScript((TestCase) testCase);
            testScripts.add(script);
        } else if (testCase instanceof CompoundTestCase) {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();
            for (TestCaseSlot slot : slots) {
                String name = slot.getTestcaseName();
                TestCase element = TestCaseManager.getBasicTestCaseByName(name);
                assert element != null;
                String testScript = generateTestScript(element);
                testScripts.add(testScript);
            }
        }

        StringBuilder testScriptPart = new StringBuilder();
        for (String item : testScripts) {
            testScriptPart.append(item).append(SpecialCharacter.LINE_BREAK);
        }

        String includedPart = generateIncludePaths();
        String additionalIncludes = generateAdditionalHeaders();

        testDriver = getTestDriverTemplate()
                .replace(TEST_PATH_TAG, Utils.doubleNormalizePath(testPathFilePath))
                .replace(CLONED_SOURCE_FILE_PATH_TAG, includedPart)
                .replace(TEST_SCRIPTS_TAG, testScriptPart.toString())
                .replace(ADDITIONAL_HEADERS_TAG, additionalIncludes)
                .replace(EXEC_TRACE_FILE_TAG, Utils.doubleNormalizePath(testCase.getExecutionResultTrace()))
                .replace(DriverConstant.ADD_TESTS_TAG, generateAddTestStm(testCase));

        if (testCase instanceof CompoundTestCase) {
            TestCaseUserCode userCode = testCase.getTestCaseUserCode();
            testDriver = testDriver
                    .replace(COMPOUND_TEST_CASE_SETUP, userCode.getSetUpContent())
                    .replace(COMPOUND_TEST_CASE_TEARDOWN, userCode.getTearDownContent());
        }
    }

    protected String generateTestScript(TestCase testCase) throws Exception {
        String body = generateBodyScript(testCase);

        String testCaseName = testCase.getName().replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);

        return String.format("void " + AKA_TEST_PREFIX + "%s(void) {\n%s\n}\n", testCaseName, body);
    }

    protected static final String AKA_TEST_PREFIX = "AKA_TEST_";

    private String generateAddTestStm(ITestCase testCase) {
        StringBuilder out = new StringBuilder();

        if (testCase instanceof TestCase) {
            String runStm = generateRunStatement((TestCase) testCase, 1);
            out.append(runStm);
        } else if (testCase instanceof CompoundTestCase) {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();
            for (TestCaseSlot slot : slots) {
                String name = slot.getTestcaseName();
                int iterator = slot.getNumberOfIterations();
                TestCase element = TestCaseManager.getBasicTestCaseByName(name);
                if (element != null) {
                    String runStm = generateRunStatement(element, iterator);
                    out.append(runStm);
                }
            }
        }

        return out.toString();
    }

    private String generateRunStatement(TestCase testCase, int iterator) {
        String testCaseName = testCase.getName();
        String testName = testCaseName.toUpperCase();
        testCaseName = testCaseName.replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);
        String test = AKA_TEST_PREFIX + testCaseName;
        return String.format(RUN_FORMAT, testName, test, iterator);
    }

    private static final String RUN_FORMAT = "\t" + DriverConstant.RUN_TEST + "(\"%s\", &%s, %d);\n";

    private String generateAdditionalHeaders() {
        StringBuilder builder = new StringBuilder();

        if (testCase.getAdditionalHeaders() != null) {
            builder.append(testCase.getAdditionalHeaders()).append(SpecialCharacter.LINE_BREAK);
        }

        List<String> userCodeList = testCase.getAdditionalIncludes();
        for (String item : userCodeList) {
            String stm = String.format("#include \"%s\"\n", item);
            builder.append(stm);
        }

        return builder.toString();
    }
    protected String getCloneSourceCodeFile(TestCase testcase) {
        return testcase.getCloneSourcecodeFilePath();
    }

    protected String generateIncludePaths() {
        StringBuilder includedPart = new StringBuilder();

        if (testCase instanceof TestCase) {
            String path = getCloneSourceCodeFile((TestCase) testCase);
            clonedFilePaths.add(path);

            String includeClonedFile = String.format("#include \"%s\"\n", path);
            includedPart.append(includeClonedFile);

            String instanceDeclaration = generateInstanceDeclaration((TestCase) testCase);
            includedPart.append(instanceDeclaration);

            if (!Environment.getInstance().isC()) {
                ICommonFunctionNode sut = ((TestCase) testCase).getRootDataNode().getFunctionNode();

                if (sut instanceof AbstractFunctionNode) {
                    INode realParent = ((AbstractFunctionNode) sut).getRealParent();
                    if (realParent == null) realParent = sut.getParent();

                    while (!(realParent instanceof SourcecodeFileNode)) {
                        if (realParent instanceof NamespaceNode)
                            break;

                        realParent = realParent.getParent();
                    }

                    while (realParent instanceof NamespaceNode) {
                        includedPart.append(SpecialCharacter.LINE_BREAK);
                        String namespace = Search.getScopeQualifier(realParent);
                        String usingNamespace = String.format("using namespace %s;\n", namespace);
                        includedPart.append(usingNamespace);
                        realParent = realParent.getParent();
                    }
                }
            }

        } else if (testCase instanceof CompoundTestCase) {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();

            for (TestCaseSlot slot : slots) {
                String name = slot.getTestcaseName();
                TestCase element = TestCaseManager.getBasicTestCaseByName(name);

                assert element != null;
                String clonedFilePath = getCloneSourceCodeFile(element);

                if (!clonedFilePaths.contains(clonedFilePath)) {
                    clonedFilePaths.add(clonedFilePath);

                    String path = Utils.doubleNormalizePath(clonedFilePath);
                    String includeClonedFile = String.format("#include \"%s\"\n", path);
                    includedPart.append(includeClonedFile);

                    String instanceDeclaration = generateInstanceDeclaration(element);
                    includedPart.append(instanceDeclaration);
                }
            }
        }

        return includedPart.toString();
    }

    protected String generateBodyScript(TestCase testCase) throws Exception {
        // STEP 1: assign aka test case name
        String testCaseNameAssign = String.format("%s=\"%s\";", StubManager.AKA_TEST_CASE_NAME, testCase.getName());

        // STEP 2: Generate initialization of variables
        String initialization = generateInitialization(testCase);

        // STEP 3: Generate full function call
        String functionCall = generateFunctionCall(testCase);

        // STEP 4: FCALLS++ - Returned from UUT
        String increaseFcall;
        if (testCase.getFunctionNode() instanceof ConstructorNode)
            increaseFcall = SpecialCharacter.EMPTY;
        else
            increaseFcall = SourceConstant.INCREASE_FCALLS + generateReturnMark(testCase);


        // STEP 5: Repeat iterator
        String singleScript = String.format(
                    "{\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                    "}",
                testCaseNameAssign,
                testCase.getTestCaseUserCode().getSetUpContent(),
                initialization,
                functionCall,
                increaseFcall,
                testCase.getTestCaseUserCode().getTearDownContent());

//        StringBuilder script = new StringBuilder();
//        for (int i = 0; i < iterator; i++)
//            script.append(singleScript).append(SpecialCharacter.LINE_BREAK);

        // STEP 6: mark beginning and end of test case
//        script = new StringBuilder(wrapScriptInMark(testCase, script.toString()));
//        script = new StringBuilder(wrapScriptInTryCatch(script.toString()));
//
//        return script.toString();
        singleScript = wrapScriptInTryCatch(singleScript);
        return singleScript;
    }

    protected String generateReturnMark(TestCase testCase) {
        ICommonFunctionNode sut = testCase.getFunctionNode();

        String markStm;

        if (sut instanceof FunctionNode || sut instanceof MacroFunctionNode || sut instanceof LambdaFunctionNode) {
            String relativePath = PathUtils.toRelative(sut.getAbsolutePath());
            markStm = String.format(DriverConstant.MARK + "(\"Return from: %s\");", Utils.doubleNormalizePath(relativePath));
        } else {
            SubprogramNode subprogram = null;

            INode parent = sut.getParent();

            if (sut instanceof IFunctionNode && ((IFunctionNode) sut).getRealParent() != null)
                parent = ((IFunctionNode) sut).getRealParent();

            RootDataNode globalRoot = Search2.findGlobalRoot(testCase.getRootDataNode());

            assert globalRoot != null;
            for (IDataNode globalVar : globalRoot.getChildren()) {
                if (((ValueDataNode) globalVar).getCorrespondingVar() instanceof InstanceVariableNode
                        && ((ValueDataNode) globalVar).getCorrespondingType().equals(parent)
                        && !globalVar.getChildren().isEmpty()
                        && !globalVar.getChildren().get(0).getChildren().isEmpty()) {

                    subprogram = (SubprogramNode) globalVar.getChildren().get(0).getChildren().get(0);
                }
            }

            assert subprogram != null;

            String relativePath = PathUtils.toRelative(sut.getAbsolutePath());

            String functionPath = Utils.doubleNormalizePath(relativePath);
            String subprogramPath = subprogram.getPathFromRoot();
            markStm = String.format(DriverConstant.MARK + "(\"Return from: %s|%s\");", functionPath, subprogramPath);
        }

        return markStm;
    }

    protected abstract String wrapScriptInTryCatch(String script);

//    protected String wrapScriptInMark(TestCase testCase, String script) {
//        String beginMark = generateTestPathMark(MarkPosition.BEGIN, testCase);
//        String endMark = generateTestPathMark(MarkPosition.END, testCase);
//
//        return beginMark + SpecialCharacter.LINE_BREAK + script + endMark;
//    }
//
//    enum MarkPosition {
//        BEGIN,
//        END
//    }
//
//    private String generateTestPathMark(MarkPosition pos, TestCase testCase) {
//        return String.format(DriverConstant.MARK + "(\"%s OF %s\");", pos, testCase.getName().toUpperCase());
//    }

    private String generateInstanceDeclaration(TestCase testCase) {
        if (Environment.getInstance().isC())
            return SpecialCharacter.EMPTY;

        RootDataNode root = testCase.getRootDataNode();
        IDataNode globalRoot = Search2.findGlobalRoot(root);

        // get sut real parent
        ICommonFunctionNode sut = root.getFunctionNode();
        INode realParent = sut.getParent();
        if (sut instanceof AbstractFunctionNode) {
            realParent = ((AbstractFunctionNode) sut).getRealParent();
            if (realParent == null) realParent = sut.getParent();
        }

        StringBuilder init = new StringBuilder();

        if (globalRoot != null) {
            for (IDataNode child : globalRoot.getChildren()) {
                if (child instanceof ValueDataNode) {
                    VariableNode varNode = ((ValueDataNode) child).getCorrespondingVar();
                    INode varTypeNode = varNode.resolveCoreType();

                    if (varNode instanceof InstanceVariableNode
                            && (varTypeNode == realParent
                            || child instanceof StructDataNode
                            || (child instanceof ClassDataNode
                            && !child.getChildren().isEmpty()))
                    ) {
                        String type = varNode.getRawType();
                        String name = child.getVituralName();
                        String instanceDefinition = String.format("%s* %s;", type, name);

                        init.append(wrapInIncludeGuard(SourceConstant.GLOBAL_PREFIX + name, instanceDefinition));
                    }
                }
            }
        }

        return init.toString();
    }

    protected String generateInitialization(TestCase testCase) throws Exception {
        String initialization = "";

        RootDataNode root = testCase.getRootDataNode();
        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(root);
        IDataNode sutRoot = Search2.findSubprogramUnderTest(root);

        if (globalRoot != null) {
//            List <IDataNode> bfsList = new ArrayList<>() ;
//            bfsList.addAll(globalRoot.getChildren());
//            while(!bfsList.isEmpty()){
//                IDataNode child = bfsList.get(0);
//                if (Environment.getInstance().isC()
//                        && child instanceof ValueDataNode
//                        && ((ValueDataNode) child).getCorrespondingVar() instanceof InstanceVariableNode) {
//                    continue;
//                }
//                initialization += child.getInputForGoogleTest();
//                bfsList.remove(bfsList.get(0));
//                bfsList.addAll(child.getChildren());
//            }
            for (IDataNode child : globalRoot.getChildren()) {
                if (Environment.getInstance().isC()
                        && child instanceof ValueDataNode
                        && ((ValueDataNode) child).getCorrespondingVar() instanceof InstanceVariableNode) {
                    continue;
                }

                if (child instanceof ValueDataNode
                        && !((ValueDataNode) child).haveValue()
                        && !globalRoot.isRelatedVariable(((ValueDataNode) child).getCorrespondingVar()))
                    continue;

                initialization += child.getInputForGoogleTest(false);
            }
        }

        if (sutRoot == null)
            initialization = "/* error initialization */";
        else {

            for(IDataNode child : sutRoot.getChildren()){
                initialization += child.getInputForGoogleTest(true);
            }
            initialization += sutRoot.getInputForGoogleTest(false);
        }

        initialization = initialization.replace(DriverConstant.MARK + "(\"<<PRE-CALLING>>\");",
                String.format(DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");", testCase.getName()));

		initialization = initialization.replaceAll("\\bconst\\s+\\b", SpecialCharacter.EMPTY);

        return initialization;
    }

    protected String generateFunctionCall(TestCase testCase) {
        ICommonFunctionNode functionNode = testCase.getFunctionNode();

        String functionCall = SpecialCharacter.EMPTY;

        if (functionNode instanceof ConstructorNode) {
            return SpecialCharacter.EMPTY;
        }

        String returnType = functionNode.getReturnType().trim();
        returnType = VariableTypeUtils.deleteVirtualAndInlineKeyword(returnType);
        returnType = VariableTypeUtils.deleteStorageClassesExceptConst(returnType);

        if (functionNode instanceof MacroFunctionNode || functionNode.isTemplate()) {
            SubprogramNode sut = Search2.findSubprogramUnderTest(testCase.getRootDataNode());

            if (sut != null)
                returnType = sut.getRawType();
        }

        if (functionNode instanceof DestructorNode) {
            functionCall = Utils.getFullFunctionCall(functionNode);

        } else if (!returnType.equals(VariableTypeUtils.VOID_TYPE.VOID)) {
            functionCall = returnType + " " + SourceConstant.ACTUAL_OUTPUT;

            functionCall += "=" + Utils.getFullFunctionCall(functionNode);
        } else
            functionCall = Utils.getFullFunctionCall(functionNode);

        functionCall = functionCall.replaceAll(ProjectClone.MAIN_REGEX, ProjectClone.MAIN_REFACTOR_NAME);

        functionCall = String.format(DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");%s", testCase.getName(), functionCall);

        return functionCall;
    }

    public void setTestCase(ITestCase testCase) {
        this.testCase = testCase;
    }
}
