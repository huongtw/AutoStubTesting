package com.dse.testcase_manager;

//import auto_testcase_generation.testdatagen.FullBoundedTestGen;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.CacheHelper;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.funcdetail.FunctionDetailTree;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search2;
import com.dse.testdata.DataTree;
import com.dse.testdata.object.*;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.UsedParameterUserCode;
import com.dse.util.NodeType;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represent a single test case
 */
public class TestCase extends AbstractTestCase implements IDataTestItem {
    private final static AkaLogger logger = AkaLogger.get(TestCase.class);

    private RootDataNode rootDataNode;
    private ICommonFunctionNode functionNode;
    // map input to expected output of global variables
    private Map<ValueDataNode, ValueDataNode> globalInputExpOutputMap = new HashMap<>();
    // map data node to include paths of the data node
    private final Map<DataNode, List<String>> additionalIncludePathsMap = new HashMap<>();

    public TestCase(ICommonFunctionNode functionNode, String name) {
        setName(name);
        FunctionDetailTree functionDetailTree = new FunctionDetailTree(functionNode);
        DataTree dataTree = new DataTree(functionDetailTree);
        rootDataNode = dataTree.getRoot();
        setFunctionNode(functionNode);
    }

    public TestCase() {
    }

    @Override
    public void setPathDefault() {
        String testcasePath = new WorkspaceConfig().fromJson().getTestcaseDirectory() + File.separator + getName() + ".json";
        setPath(testcasePath);
    }

    public Map<ValueDataNode, ValueDataNode> getGlobalInputExpOutputMap() {
        return globalInputExpOutputMap;
    }

    // is only called when create new testcase
    public void initGlobalInputExpOutputMap() {
        try {
            globalInputExpOutputMap.clear();
            FunctionDetailTree functionDetailTree = new FunctionDetailTree(functionNode);
            DataTree dataTree = new DataTree(functionDetailTree);
            RootDataNode root = dataTree.getRoot();

//            RootDataNode root = getRootDataNode();

            // children of the global data node of the root are used to be expected output values
            RootDataNode newGlobalDataNode = getGlobalDataNode(root);
            GlobalRootDataNode globalDataNode = getGlobalDataNode(rootDataNode);
            if (newGlobalDataNode != null && globalDataNode != null) {
                mapGlobalInputToExpOutput(globalDataNode.getChildren(), newGlobalDataNode.getChildren());
                globalDataNode.setGlobalInputExpOutputMap(globalInputExpOutputMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private GlobalRootDataNode getGlobalDataNode(RootDataNode root) {
        for (IDataNode uut : root.getChildren()) {
            if (uut instanceof UnitUnderTestNode) {
                for (IDataNode dataNode : uut.getChildren()) {
                    if (dataNode instanceof GlobalRootDataNode
                            && ((RootDataNode) dataNode).getLevel().equals(NodeType.GLOBAL)) {
                        return (GlobalRootDataNode) dataNode;
                    }
                }
            }
        }
        return null;
    }

    private void mapGlobalInputToExpOutput(List<IDataNode> inputs, List<IDataNode> expOutputs) {
        if (inputs != null) {
            for (IDataNode inputValue : inputs) {
                for (IDataNode expectedOutput : expOutputs) {
                    if (expectedOutput.getName().equals(inputValue.getName())) {
                        ((ValueDataNode) expectedOutput).setExternel(false);
                        expectedOutput.setParent(inputValue.getParent());
                        globalInputExpOutputMap.put((ValueDataNode) inputValue, (ValueDataNode) expectedOutput);
                    }
                }
            }
        }
    }

    public void updateGlobalInputExpOutputAfterInportFromFile() {
        GlobalRootDataNode globalRootDataNode = getGlobalDataNode(rootDataNode);
        if (globalRootDataNode != null)
            globalInputExpOutputMap = globalRootDataNode.getGlobalInputExpOutputMap();
    }

    public RootDataNode getRootDataNode() {
        return rootDataNode;
    }

    public void setRootDataNode(RootDataNode rootDataNode) {
        this.rootDataNode = rootDataNode;
    }

    public void setName(String name) {
        super.setName(name);
//        if (getPath() == null) {
        setPathDefault();
        setBreakpointPathDefault();
        setCurrentCoverageDefault();
        setCurrentProgressDefault();
//        }
        updateTestNewNode(name);
    }

    public boolean initParameterExpectedOutputs() {
        if (rootDataNode != null) {
            SubprogramNode sut = Search2.findSubprogramUnderTest(rootDataNode);
            if (sut != null) {
                // init parameter expected output datanodes
                sut.initInputToExpectedOutputMap();
                return true;
            }

//            UnitUnderTestNode unitUnderTestNode = null;
//            for (IDataNode dataNode : rootDataNode.getChildren()) {
//                if (dataNode instanceof UnitUnderTestNode) {
//                    unitUnderTestNode = (UnitUnderTestNode) dataNode;
//                    break;
//                }
//            }
//
//            if (unitUnderTestNode != null) {
//                SubprogramNode subprogramNode = null;
//                for (IDataNode dataNode1 : unitUnderTestNode.getChildren()) {
//                    if (dataNode1 instanceof SubprogramNode) {
//                        subprogramNode = (SubprogramNode) dataNode1;
//                        break;
//                    }
//                }
//
//                if (subprogramNode != null) {
//                    // init parameter expected output datanodes
//                    subprogramNode.initInputToExpectedOutputMap();
//                    return true;
//                }
//            }
        }

        return false;
    }

    public boolean initParameterExpectedOutputsWithPrototype(String testCaseDirectory) {
        if (rootDataNode != null) {
            SubprogramNode sut = Search2.findSubprogramUnderTest(rootDataNode);
            if (sut != null) {
//                 init parameter expected output datanodes
                sut.initInputToExpectedOutputMapWithProtoType(testCaseDirectory);

                return true;
            }

        }

        return false;
    }

    public String getCloneSourcecodeFilePath() {
        // find the source code file containing the tested function
        INode sourcecodeFile = Utils.getSourcecodeFile(functionNode);

        // set up path for the cloned source code file
//        int lastExtensionPos = sourcecodeFile.getAbsolutePath().lastIndexOf(".");
//        return sourcecodeFile.getAbsolutePath()
//                .substring(0, lastExtensionPos) + ITestCase.AKA_SIGNAL + getName() + sourcecodeFile.getAbsolutePath()
//                .substring(lastExtensionPos);
        return ProjectClone.getClonedFilePath(sourcecodeFile.getAbsolutePath());
    }

    /**
     * put or update data node and is include paths (used by user code) to map
     *
     * @param dataNode data node that use user code
     */
    public void putOrUpdateDataNodeIncludes(DataNode dataNode) {
        if (dataNode instanceof IUserCodeNode) {
            AbstractUserCode uc = ((IUserCodeNode) dataNode).getUserCode();
            if (uc instanceof UsedParameterUserCode) {
                UsedParameterUserCode userCode = (UsedParameterUserCode) uc;
                List<String> includePaths = new ArrayList<>();
                if (userCode.getType().equals(UsedParameterUserCode.TYPE_CODE)) {
                    includePaths.addAll(userCode.getIncludePaths());
                } else if (userCode.getType().equals(UsedParameterUserCode.TYPE_REFERENCE)) {
                    ParameterUserCode reference = UserCodeManager.getInstance()
                            .getParamUserCodeById(userCode.getId());
                    includePaths.addAll(reference.getIncludePaths());
                }

                if (additionalIncludePathsMap.containsKey(dataNode)) {
                    List<String> paths = additionalIncludePathsMap.get(dataNode);
                    paths.clear();
                    paths.addAll(includePaths);
                } else {
                    additionalIncludePathsMap.put(dataNode, includePaths);
                }
            } else {
                additionalIncludePathsMap.remove(dataNode);
            }
        }
    }

    public void putOrUpdateDataNodeIncludes(DataNode dataNode, String includePath) {
        if (dataNode instanceof IUserCodeNode) {
            additionalIncludePathsMap.remove(dataNode);
            List<String> paths = new ArrayList<>();
            paths.add(includePath);
            additionalIncludePathsMap.put(dataNode, paths);
        }
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    @Override
    public void setStatus(String status) {
        super.setStatus(status);
        if (status.equals(STATUS_NA)) {
            deleteOldDataExceptValue();
        }
    }

    public void updateToTestCasesNavigatorTree() {
        ICommonFunctionNode functionNode = this.getRootDataNode().getFunctionNode();
        TestCasesTreeItem treeItem = CacheHelper.getFunctionToTreeItemMap().get(functionNode);
        treeItem.addTestNewNode(treeItem, this);

        Environment.getInstance().saveTestcasesScriptToFile();
        logger.debug("Append the name of testcase " + this.getName() + " to " + Environment.getInstance().getTestcaseScriptRootNode().getAbsolutePath());
    }

    /**
     * get all additional include headers used by user codes of test case
     */
    public List<String> getAdditionalIncludes() {
        // add from additionalIncludePathsMap
        List<String> allPaths = new ArrayList<>();
        for (Collection<String> collection : additionalIncludePathsMap.values()) {
            allPaths.addAll(collection);
        }

        // add from test case user code include paths
        allPaths.addAll(getTestCaseUserCode().getIncludePaths());

        return allPaths.stream().distinct().collect(Collectors.toList());
    }

    public Map<DataNode, List<String>> getAdditionalIncludePathsMap() {
        return additionalIncludePathsMap;
    }


    @Override
    protected List<INode> getAllRelatedFileToLink() {
        List<INode> sourceNodes = new ArrayList<>();

        INode sourceNode = Utils.getSourcecodeFile(functionNode);
        sourceNodes.add(sourceNode);

        sourceNode.getDependencies().stream()
                .filter(d -> d instanceof IncludeHeaderDependency && d.getStartArrow().equals(sourceNode))
                .forEach(d -> {
                    INode start = d.getEndArrow();
                    if (!sourceNodes.contains(start))
                        sourceNodes.add(start);
                });

        return sourceNodes;
    }

    @Override
    public String generateDefinitionCompileCmd() {
        String defineName = getName().toUpperCase()
                .replace(SpecialCharacter.DOT, SpecialCharacter.UNDERSCORE_CHAR);

        return String.format("-DAKA_TC_%s", defineName);
    }

    public void combineWithTestCase(TestCase tc)
    {

    }
}
