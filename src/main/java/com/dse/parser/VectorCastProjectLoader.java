package com.dse.parser;

import com.dse.parser.object.FolderNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.Node;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.FolderNodeCondition;
import com.dse.util.PathUtils;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.dse.util.tostring.ToString;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorCastProjectLoader {
    final static AkaLogger logger = AkaLogger.get(VectorCastProjectLoader.class);

    private List<File> sourcecodeList = new ArrayList<>();
    private List<File> typeHandledDirectories = new ArrayList<>();
    private List<File> libraryIncludeDirectories = new ArrayList<>();

    public static void main(String[] args) {
        List<File> sourcecodeList = new ArrayList<>();
        sourcecodeList.add(new File("datatest"));
        sourcecodeList.add(new File("datatest/duc-anh/TSDV_log4cpp"));
        sourcecodeList.add(new File("datatest/duc-anh/TSDV_log4cpp/src"));

        List<File> typeHandledDirectories = new ArrayList<>();
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include"));
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include/log4cpp"));
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include/log4cpp/threading"));

        VectorCastProjectLoader loader = new VectorCastProjectLoader();
        loader.setSourcecodeList(sourcecodeList);
        loader.setTypeHandledDirectories(typeHandledDirectories);
        INode root = loader.constructPhysicalTree();

        /*
          display tree of project
         */
        ToString treeDisplayer = new DependencyTreeDisplayer(root);
        logger.debug("Final tree: \n" + treeDisplayer.getTreeInString());
    }

    public INode constructPhysicalTree() {
        List<File> allAnalyzedFiles = new ArrayList<>();
        allAnalyzedFiles.addAll(getSourcecodeList());
        allAnalyzedFiles.addAll(getTypeHandledDirectories());

        // Create independent trees from source code file list
        Map<File, INode> subphysicalTreeMap = analyzeAllSourceCodeList(allAnalyzedFiles);

        // Get the root path
        File rootSourcecodeList = getRootPath(allAnalyzedFiles);

        // set root of the physical tree
        INode rootOfPhysicalTree = new ProjectNode();
        try {
            rootOfPhysicalTree.setAbsolutePath(rootSourcecodeList.getCanonicalPath());
        } catch (IOException e) {
            rootOfPhysicalTree.setAbsolutePath(rootSourcecodeList.getAbsolutePath());
        }
        // create physical tree
        createInitialPhysicalTree(rootOfPhysicalTree);

        // merge
        mergeSubphysicalTrees(rootOfPhysicalTree, subphysicalTreeMap);

        // optimize the tree by removing the redundant nodes
        int MAX_N_OPTIMIZATION = 10;
        for (int i = 0; i < MAX_N_OPTIMIZATION; i++)
            deleteEmptyFolders(rootOfPhysicalTree);

        return rootOfPhysicalTree;
    }

    private void deleteEmptyFolders(INode root) {
        for (int i = root.getChildren().size() - 1; i >= 0; i--) {
            INode child = root.getChildren().get(i);

            if (child instanceof FolderNode) {
                if (child.getChildren().size() == 0) {
                    root.getChildren().remove(i);
                    //logger.debug("Delete " + child.getAbsolutePath());
                } else {
                    deleteEmptyFolders(child);
                }
            }
        }
    }

    private void mergeSubphysicalTrees(INode root, Map<File, INode> subphysicalTreeMap) {
        List<INode> folders = Search.searchNodes(root, new FolderNodeCondition());
        if (new File(root.getAbsolutePath()).isDirectory()){
            folders.add(root);
        }
        for (INode folder : folders)

            if (new File(folder.getAbsolutePath()).exists()) {

                for (File sourceCodeListItem : subphysicalTreeMap.keySet())

                    if (PathUtils.equals(sourceCodeListItem.getAbsolutePath(), folder.getAbsolutePath())) {
                        List<Node> children = subphysicalTreeMap.get(sourceCodeListItem).getChildren();
                        folder.getChildren().addAll(children);

                        for (Node child : children)
                            child.setParent(folder);
                        break;
                    }
            }
    }

    private void createInitialPhysicalTree(INode root) {
        if (new File(root.getAbsolutePath()).isDirectory()) {
            for (File child : new File(root.getAbsolutePath()).listFiles())

                if (child.isDirectory()) {
                    FolderNode newNode = new FolderNode();
                    try {
                        newNode.setAbsolutePath(child.getCanonicalPath());
                    } catch (IOException e) {
                        newNode.setAbsolutePath(child.getAbsolutePath());
                    }
                    newNode.setParent(root);
                    root.getChildren().add(newNode);
                    createInitialPhysicalTree(newNode);
                } else {
                    // nothing to do
                }
        }
    }

    /**
     * Example:
     * - "A/B/C"
     * - "A/B/D"
     *
     * Output: root of the two paths = "A/B"
     * @param sourcecodeList
     * @return
     */
    private File getRootPath(List<File> sourcecodeList) {
        if (sourcecodeList.size() >= 1) {
            File initialSourcecodeFolder = sourcecodeList.get(0);
            if (initialSourcecodeFolder.exists()) {
                File parent = initialSourcecodeFolder;

                // check whether a folder is a root of other folders
                boolean foundRootOfSourcecodeList = false;
                while (!foundRootOfSourcecodeList) {
                    foundRootOfSourcecodeList = true;

                    for (File item : sourcecodeList) {
                        try {
                            if (!item.getCanonicalPath().startsWith(parent.getCanonicalPath())) {
                                foundRootOfSourcecodeList = false;
                                parent = parent.getParentFile();
                                break;
                            }
                        } catch (IOException e) {
                            logger.error("Can not get canonical path of " + item.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                }

                //
                if (foundRootOfSourcecodeList) {
                    return parent;
                }
            }
        }
        return null;
    }

    private Map<File, INode> analyzeAllSourceCodeList(List<File> sourcecodeList) {
        Map<File, INode> sourcecodeListNodes = new HashMap<>();
        for (File sourcecodeFolder : sourcecodeList)
            if (sourcecodeFolder.exists()) {

                IProjectLoader projectLoader = new ProjectLoader();
                projectLoader.setRecursive(false);
                ProjectNode root = projectLoader.load(sourcecodeFolder);
                sourcecodeListNodes.put(sourcecodeFolder, root);
            }
        return sourcecodeListNodes;
    }

    public List<File> getSourcecodeList() {
        return sourcecodeList;
    }

    public void setSourcecodeList(List<File> sourcecodeList) {
        this.sourcecodeList = sourcecodeList;
    }

    public List<File> getLibraryIncludeDirectories() {
        return libraryIncludeDirectories;
    }

    public void setLibraryIncludeDirectories(List<File> libraryIncludeDirectories) {
        this.libraryIncludeDirectories = libraryIncludeDirectories;
    }

    public List<File> getTypeHandledDirectories() {
        return typeHandledDirectories;
    }

    public void setTypeHandledDirectories(List<File> typeHandledDirectories) {
        this.typeHandledDirectories = typeHandledDirectories;
    }
}
