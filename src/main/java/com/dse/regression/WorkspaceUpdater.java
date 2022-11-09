package com.dse.regression;

import com.dse.parser.dependency.RealParentDependencyGeneration;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.DependencyFileTreeExporter;
import com.dse.environment.PhysicalTreeExporter;
import com.dse.environment.SourcecodeFileTreeExporterv2;
import com.dse.environment.Environment;
import com.dse.parser.dependency.*;
import com.dse.parser.object.*;
import com.dse.project_init.LcovProjectClone;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search;
import com.dse.search.condition.NodeCondition;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a list of changes, we need to update {workspace}.
 */
public class WorkspaceUpdater {
    final static AkaLogger logger = AkaLogger.get(WorkspaceUpdater.class);

    public void update() {
        ProjectClone.cloneEnvironment();
        LcovProjectClone.cloneLcovEnvironment();

        Environment.getInstance().getCfgsForMcdc().clear();
        Environment.getInstance().getCfgsForBranchAndStatement().clear();

        addAddedNodesToTestCaseScript();

        removeDeletedPathFromTestCaseScript();
        removeDependenciesRelatedToDeletedPaths();

        updateDependencies();

        updateConfigFile();
        updateStructureFiles();
        updateDepdendencyFiles();
    }

    private void updateDepdendencyFiles() {
        // update dependency file
        for (INode changedSrcFile : ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.keySet())
            if (changedSrcFile instanceof ISourcecodeFileNode) {
                String dependenciesFolder = new WorkspaceConfig().fromJson().getDependencyDirectory();
                String dependencyFile = dependenciesFolder + File.separator + changedSrcFile.getRelativePathToRoot() + WorkspaceConfig.AKA_EXTENSION;
                new File(dependencyFile).getParentFile().mkdirs();
                new DependencyFileTreeExporter().export(new File(dependencyFile), (Node) changedSrcFile);
            }
    }

    private void updateStructureFiles() {
        // update element tree
        for (INode changedSrcFile : ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.keySet())
            if (changedSrcFile instanceof ISourcecodeFileNode) {
                String elementFolder = new WorkspaceConfig().fromJson().getElementDirectory();
                String elementFile = elementFolder + File.separator + changedSrcFile.getRelativePathToRoot() + WorkspaceConfig.AKA_EXTENSION;
                new File(elementFile).getParentFile().mkdirs();
                new SourcecodeFileTreeExporterv2().export(new File(elementFile), changedSrcFile);
            }
    }

    private void updateConfigFile() {
        // update test case script with the latest changes
        String updatedTestscript = Environment.getInstance().getTestcaseScriptRootNode().exportToFile();
        Utils.deleteFileOrFolder(new File(new WorkspaceConfig().fromJson().getTestscriptFile()));
        Utils.writeContentToFile(updatedTestscript, new WorkspaceConfig().fromJson().getTestscriptFile());

        // update physical tree
        new PhysicalTreeExporter().export(new File(new WorkspaceConfig().fromJson().getPhysicalJsonFile()), Environment.getInstance().getProjectNode());
    }

//    private void reset(INode n) {
//        if (n instanceof AbstractFunctionNode) {
//            ((AbstractFunctionNode) n).setRealParentDependencyState(false);
//            ((AbstractFunctionNode) n).setSizeDependencyState(false);
//            ((AbstractFunctionNode) n).setFunctionCallDependencyState(false);
//            ((AbstractFunctionNode) n).setGlobalVariableDependencyState(false);
//            for (IVariableNode var : ((AbstractFunctionNode) n).getArguments())
//                var.setTypeDependencyState(false);
//
//        } else if (n instanceof StructOrClassNode) {
//            ((ClassNode) n).setExtendDependencyState(false);
//
//        } else if (n instanceof IncludeHeaderNode) {
//            INode srcNode = Utils.getSourcecodeFile(n);
//            if (srcNode instanceof SourcecodeFileNode) {
//                ((SourcecodeFileNode) srcNode).setExpandedToMethodLevelState(false);
//                ((SourcecodeFileNode) srcNode).setIncludeHeaderDependencyState(false);
//            }
//        }
//    }

    private void updateDependencies() {
        List<INode> nodes = new ArrayList<>();
        nodes.addAll(ChangesBetweenSourcecodeFiles.modifiedNodes);
        nodes.addAll(ChangesBetweenSourcecodeFiles.addedNodes);
        nodes.addAll(ChangesBetweenSourcecodeFiles.modifiedNodes);
        for (INode node : nodes)
            if (node instanceof ISourcecodeFileNode) {
                new IncludeHeaderDependencyGeneration().dependencyGeneration(node);
            } else if (node instanceof StructOrClassNode) {
                new ExtendedDependencyGeneration().dependencyGeneration(node);
            } else if (node instanceof AbstractFunctionNode) {
                new FunctionCallDependencyGeneration().dependencyGeneration(node);

                try {
                    new RealParentDependencyGeneration().dependencyGeneration(node);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new SizeOfArrayDepencencyGeneration().dependencyGeneration(node);

                new GlobalVariableDependencyGeneration().dependencyGeneration(node);

                for (IVariableNode var : ((IFunctionNode) node).getArguments())
                    try {
                        CTypeDependencyGeneration gen = new CTypeDependencyGeneration();
                        gen.setAddToTreeAutomatically(true);
                        gen.dependencyGeneration(var);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
    }

    private void addAddedNodesToTestCaseScript() {
        // update the environment with added nodes
        for (INode addedNode : ChangesBetweenSourcecodeFiles.addedNodes) {

            // add new function in the test case script
            TestcaseRootNode testcaseScriptRoot = Environment.getInstance().getTestcaseScriptRootNode();
            List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(testcaseScriptRoot, new TestUnitNode());
            for (ITestcaseNode testcaseNode : testcaseNodes) {
                if (testcaseNode instanceof TestUnitNode) {
                    String unitNodePath = ((TestUnitNode) testcaseNode).getName();
                    String fileNodePath = Utils.getSourcecodeFile(addedNode).getAbsolutePath();
                    if (PathUtils.equals(unitNodePath, fileNodePath)) {
                        // is a function (just to ensure that we do not add wrong node)
                        if (addedNode.getAbsolutePath().endsWith(")") && !(addedNode instanceof DefinitionFunctionNode)) {
                            // found the correspond node
                            TestNormalSubprogramNode subprogramNode = new TestNormalSubprogramNode();
                            subprogramNode.setName(addedNode.getAbsolutePath());
                            subprogramNode.setParent(testcaseNode);
                            testcaseNode.getChildren().add(subprogramNode);
                            logger.debug("Add " + addedNode.getAbsolutePath() + " to test case script");
                            break;
                        }
                    }
                }
            }
        }
    }

    private void removeDependenciesRelatedToDeletedPaths() {
        // update the environment with deleted items
        // Note that the deleted items do not exist in the current tree of project
        // The current tree of project is corresponding to the current source code file
        List<INode> nodes = Search.searchNodes(Environment.getInstance().getProjectNode(), new NodeCondition());

        for (String deletedPath : ChangesBetweenSourcecodeFiles.deletedPaths) {
            // delete dependencies related to the deleted item

            for (INode node : nodes)
                for (int i = node.getDependencies().size() - 1; i >= 0; i--) {
                    Dependency d = node.getDependencies().get(i);
                    if (PathUtils.equals(d.getStartArrow().getAbsolutePath(), deletedPath)
                            || PathUtils.equals(d.getEndArrow().getAbsolutePath(), deletedPath)) {
                        node.getDependencies().remove(i);
                        logger.debug("Remove dependency " + d.toString());
                    }
                }
        }
    }

    private void removeDeletedPathFromTestCaseScript() {
        TestcaseRootNode testcaseScriptRoot = Environment.getInstance().getTestcaseScriptRootNode();
        List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(testcaseScriptRoot, new AbstractTestcaseNode());

        for (String deletedPath : ChangesBetweenSourcecodeFiles.deletedPaths) {
            // delete deleted path in test case script
            // if the deleted path is corresponding to a function, we need to remove this function from the test case script

            for (ITestcaseNode testcaseNode : testcaseNodes) {
                if (testcaseNode instanceof TestNormalSubprogramNode)
                    if (PathUtils.equals(((TestNormalSubprogramNode) testcaseNode).getName(), deletedPath)) {
                        testcaseNode.getParent().getChildren().remove(testcaseNode);
                        logger.debug("remove " + deletedPath + " from test case script ");
                        break;
                    }
            }
        }
    }
}
