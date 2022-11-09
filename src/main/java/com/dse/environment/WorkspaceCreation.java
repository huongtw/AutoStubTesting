package com.dse.environment;

import com.dse.config.WorkspaceConfig;
import com.dse.parser.ProjectParser;
import com.dse.parser.VectorCastProjectLoader;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Used when the environment is built successfully.
 * <p>
 * The current information of the environment will be saved externally: dependency folder, element folder, etc.
 */
public class WorkspaceCreation {
    final static AkaLogger logger = AkaLogger.get(WorkspaceCreation.class);

    private String workspace;
    private String dependenciesFolder;
    private String elementFolder;
    private String physicalTreePath;
    private Node root;

    public static void main(String[] args){
        // Construct the physical tree
        logger.debug("Construct the initial physical tree of the originally tested project");
        List<File> sourcecodeList = new ArrayList<>();
//        sourcecodeList.add(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm"));
        sourcecodeList.add(new File("datatest/duc-anh/TSDV_log4cpp"));
        sourcecodeList.add(new File("datatest/duc-anh/TSDV_log4cpp/src"));

        List<File> typeHandledDirectories = new ArrayList<>();
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include"));
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include/log4cpp"));
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include/log4cpp/threading"));

        VectorCastProjectLoader loader = new VectorCastProjectLoader();
        loader.setSourcecodeList(sourcecodeList);
        loader.setTypeHandledDirectories(typeHandledDirectories);
        Node rootOfPhysicalTree = (Node) loader.constructPhysicalTree();

        // create the environment
        WorkspaceCreation evc = new WorkspaceCreation();
        evc.setPhysicalTreePath("/Users/ducanhnguyen/Documents/akautauto/local/wd/test/physical_tree.json");
        evc.setDependenciesFolder("/Users/ducanhnguyen/Documents/akautauto/local/wd/test/dependency/");
        evc.setDependenciesFolder("/Users/ducanhnguyen/Documents/akautauto/local/wd/test/element/");
        evc.setRoot(rootOfPhysicalTree);
        evc.create(evc.getRoot(), evc.getElementFolder(), evc.getDependenciesFolder(), evc.getPhysicalTreePath());
    }

    public void create(Node root, String elementFolder, String dependenciesFolder, String physicalTreePath) {
        // export the tree of the project to json
        logger.debug("Export the physical tree to file " + this.getPhysicalTreePath());
        new PhysicalTreeExporter().export(new File(physicalTreePath), root);

        // parse the project down to file level
        logger.debug("Parse the project " + root.getAbsolutePath() + " down to file level");
        ProjectParser parser = new ProjectParser((ProjectNode) root);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setExtendedDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setGenerateSetterandGetter_enabled(false);
        parser.setTypeDependency_enable(true);
        parser.setSizeOfDependencyGeneration_enabled(true);
        parser.getRootTree();

        // save the information about every file externally
        try {
            this.exportSourcecodeFileNodeToWorkingDirectory((ProjectNode) root, elementFolder, dependenciesFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void createWorkspace(String workspace) {
//        if (new File(workspace).exists())
//            new File(workspace).delete();
//    }

    public void exportSourcecodeFileNodeToWorkingDirectory(ProjectNode projectRootNode, String elementFolder, String dependenciesFolder) throws Exception {
        /**
         * parse the project to get dependency
         */
        ProjectParser projectParser = new ProjectParser(projectRootNode);
        projectParser.setSizeOfDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setGenerateSetterandGetter_enabled(true);
        projectParser.setParentReconstructor_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(true);
        projectParser.setGlobalVarDependencyGeneration_enabled(true);
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        projectParser.setTypeDependency_enable(true);
        projectParser.getRootTree();

        /*
          Export dependencies to file
         */
        List<INode> sourcecodeFileNodes = Search.searchNodes(projectRootNode, new SourcecodeFileNodeCondition());
        for (INode sourcodeFileNode : sourcecodeFileNodes)
            if (sourcodeFileNode instanceof ISourcecodeFileNode) {
                String relativePathToRoot = new File(sourcodeFileNode.getAbsolutePath()).getCanonicalPath().replace(new File(projectRootNode.getAbsolutePath()).getCanonicalPath(), "");

                // export dependencies for every source code file and header
                String dependencyFile = dependenciesFolder + File.separator + relativePathToRoot + WorkspaceConfig.AKA_EXTENSION;
                new File(dependencyFile).getParentFile().mkdirs();
                logger.debug("Dependency of file " + sourcodeFileNode.getAbsolutePath() + " is stored at " + dependencyFile);
                new DependencyFileTreeExporter().export(new File(dependencyFile), (Node) sourcodeFileNode);
            }

        /*
          Export structures to file
         */
        for (INode sourcodeFileNode : sourcecodeFileNodes)
            if (sourcodeFileNode instanceof ISourcecodeFileNode) {
                String relativePathToRoot = new File(sourcodeFileNode.getAbsolutePath()).getCanonicalPath().replace(new File(projectRootNode.getAbsolutePath()).getCanonicalPath(), "");
                // Export the element in the original project to file
                /// use to detect changes when source code files are updated
                String elementFile = elementFolder + File.separator + relativePathToRoot + WorkspaceConfig.AKA_EXTENSION;
                new File(elementFile).getParentFile().mkdirs();
                new SourcecodeFileTreeExporterv2().export(new File(elementFile), sourcodeFileNode);
            }
    }

    public String getPhysicalTreePath() {
        return physicalTreePath;
    }

    public void setPhysicalTreePath(String physicalTreePath) {
        this.physicalTreePath = physicalTreePath;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public String getDependenciesFolder() {
        return dependenciesFolder;
    }

    public void setDependenciesFolder(String dependenciesFolder) {
        this.dependenciesFolder = dependenciesFolder;
    }

    public String getElementFolder() {
        return elementFolder;
    }

    public void setElementFolder(String elementFolder) {
        this.elementFolder = elementFolder;
    }
}
