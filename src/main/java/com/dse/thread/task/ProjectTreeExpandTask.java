package com.dse.thread.task;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.*;
import com.dse.environment.Environment;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.TypeDependency;
import com.dse.parser.object.*;
import com.dse.regression.AbstractDifferenceDetecter;
import com.dse.regression.ChangesBetweenSourcecodeFiles;
import com.dse.regression.SimpleDifferenceDetecter;
import com.dse.regression.UnresolvedDependency;
import com.dse.search.Search;
import com.dse.search.condition.NodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.stub_manager.SystemLibrary;
import com.dse.thread.AbstractAkaTask;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Until;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectTreeExpandTask extends AbstractAkaTask<INode> {

    private static final AkaLogger logger = AkaLogger.get(ProjectTreeExpandTask.class);

    private final INode root;

    private List<INode> sources;

    private boolean doRemaining = true;

    public ProjectTreeExpandTask(INode root) {
        this.root = root;
    }

    public void setDoRemaining(boolean doRemaining) {
        this.doRemaining = doRemaining;
    }

    @Override
    protected INode call() throws InterruptedException {
        expand();

        return root;
    }

    public void expand() throws InterruptedException {
        logger.debug("Start expanding the initial tree down to method level");

        sources = Search.searchNodes(root, new SourcecodeFileNodeCondition());
        int lastIndex = sources.size() - 1;

        ExecutorService es = Executors.newFixedThreadPool(5);
        List<Callable<INode>> tasks = new ArrayList<>();

        for (int idx = lastIndex; idx >= 0; idx--) {
            INode sourcecodeFile = sources.get(idx);

            ExpandSourceCodeTask task = new ExpandSourceCodeTask(sourcecodeFile);

            tasks.add(task);
        }

        es.invokeAll(tasks);

        logger.debug("Finish expanding the initial tree down to method level");

        if (doRemaining) {
            /*
             * Load stub library physical tree
             */
//        LoadingPopupController.getInstance().setText("Loading system libraries data");
            SystemLibraryRoot sysLibRoot = SystemLibrary.parseFromFile();
            Environment.getInstance().setSystemLibraryRoot(sysLibRoot);
            logger.debug("Load stub library from file");

            /*
             * Load data user code physical tree
             */
//        LoadingPopupController.getInstance().setText("Loading user code data");
            INode userCodeRoot = EnvironmentUserCode.getInstance().parseTree();
            Environment.getInstance().setUserCodeRoot(userCodeRoot);
            logger.debug("Load data user code physical tree");

            /*
             * Load dependencies
             */
            logger.debug("Load dependencies from file");
            loadDependenciesFromAllFiles(root, sysLibRoot, userCodeRoot);

            logger.debug("Load dependencies from file successfully");
//        LoadingPopupController.getInstance().setText("Analysis source code file changes");
            String elementFolderOfOldVersion = new WorkspaceConfig().fromJson().getElementDirectory();
            detectChangeInSourcecodeFilesWhenOpeningEnv(root, elementFolderOfOldVersion);

            // load unit under test for enviro node
            loadUnitTestableState();

            exportVersionComparisonToFile();
        }

        logger.debug("Expand task finish!!");

        if (callback != null)
            callback.run();
    }

    private Runnable callback;

    public ProjectTreeExpandTask setCallback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    public static void loadUnitTestableState(final List<INode> sources) {
        EnvironmentRootNode envRoot = Environment.getInstance().getEnvironmentRootNode();

        List<IEnvironmentNode> uuts = EnvironmentSearch.searchNode(envRoot, new EnviroUUTNode());
        for (IEnvironmentNode uut : uuts) {
            for (INode source : sources) {
                if (PathUtils.equals(((EnviroUUTNode) uut).getName(), source.getAbsolutePath())) {
                    ((EnviroUUTNode) uut).setUnit(source);
                    sources.remove(source);
                    break;
                }
            }
        }

        List<IEnvironmentNode> sbfs = EnvironmentSearch.searchNode(envRoot, new EnviroSBFNode());
        for (IEnvironmentNode sbf : sbfs) {
            for (INode source : sources) {
                if (PathUtils.equals(((EnviroSBFNode) sbf).getName(), source.getAbsolutePath())) {
                    ((EnviroSBFNode) sbf).setUnit(source);
                    sources.remove(source);
                    break;
                }
            }
        }

        List<IEnvironmentNode> dontStubs = EnvironmentSearch.searchNode(envRoot, new EnviroDontStubNode());
        for (IEnvironmentNode dontStub : dontStubs) {
            for (INode source : sources) {
                if (PathUtils.equals(((EnviroDontStubNode) dontStub).getName(), source.getAbsolutePath())) {
                    ((EnviroDontStubNode) dontStub).setUnit(source);
                    sources.remove(source);
                    break;
                }
            }
        }

        List<IEnvironmentNode> ignores = EnvironmentSearch.searchNode(envRoot, new EnviroIgnoreNode());
        for (IEnvironmentNode ignore : ignores) {
            for (INode source : sources) {
                if (PathUtils.equals(((EnviroIgnoreNode) ignore).getName(), PathUtils.toRelative(source.getAbsolutePath()))) {
                    ((EnviroIgnoreNode) ignore).setUnit(source);
                    break;
                }
            }
        }
    }

    private void loadUnitTestableState() {
        List<INode> list = new ArrayList<>(sources);
        loadUnitTestableState(list);
    }

    private void exportVersionComparisonToFile() {
        // export the report of difference to files
        {
            String changeFile = new WorkspaceConfig().fromJson().getFileContainingUnresolvedDependenciesWhenComparingSourcecode();
            Utils.deleteFileOrFolder(new File(changeFile));
            Utils.writeContentToFile(ChangesBetweenSourcecodeFiles.getUnresolvedDepdendenciesInString(), changeFile);
        }

        // export the report of difference to files
        {
            String changeFile = new WorkspaceConfig().fromJson().getFileContainingChangesWhenComparingSourcecode();
            Utils.deleteFileOrFolder(new File(changeFile));
            Utils.writeContentToFile(ChangesBetweenSourcecodeFiles.getReportOfDifferences(), changeFile);
        }

        // export the report of changed source code to file
        {
            String changeFile = new WorkspaceConfig().fromJson().getFileContainingChangedSourcecodeFileWhenComparingSourcecode();
            Utils.deleteFileOrFolder(new File(changeFile));
            Utils.writeContentToFile(ChangesBetweenSourcecodeFiles.getModifiedSourcecodeFilesInString(), changeFile);
        }
    }

    private void detectChangeInSourcecodeFilesWhenOpeningEnv(INode root, String elementFolderOfOldVersion) {
        AbstractDifferenceDetecter changeDetecter = new SimpleDifferenceDetecter();
        changeDetecter.detectChanges(root, elementFolderOfOldVersion);

        ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles = changeDetecter.getModifiedSourcecodeFiles();
        ChangesBetweenSourcecodeFiles.addedNodes = changeDetecter.getAddedNodes();
        ChangesBetweenSourcecodeFiles.deletedPaths = changeDetecter.getDeletedPaths();
        ChangesBetweenSourcecodeFiles.modifiedNodes = changeDetecter.getModifiedNodes();
    }

    /**
     * Load dependencies from all dependency files
     *
     * @param root
     */
    private void loadDependenciesFromAllFiles(INode root, Node libRoot, INode userCodeRoot) {
        File dependenciesFolder = new File(new WorkspaceConfig().fromJson().getDependencyDirectory());
        logger.debug("Load dependency from folder " + dependenciesFolder.getAbsolutePath());

        if (dependenciesFolder.exists()) {
            if (root != null) {
                List<INode> allNodes = Search.searchNodes(root, new NodeCondition());
                allNodes.addAll(Search.searchNodes(libRoot, new NodeCondition()));
                allNodes.addAll(Search.searchNodes(userCodeRoot, new NodeCondition()));

//                ExecutorService es = Executors.newFixedThreadPool(5);
//                List<Callable<Void>> tasks = new ArrayList<>();

                for (String filePath : Utils.getAllFiles(dependenciesFolder.getAbsolutePath())) {
                    if (filePath.endsWith(WorkspaceConfig.AKA_EXTENSION)) {
                        LoadDependencyTask task = new LoadDependencyTask(filePath, allNodes);
//                        tasks.add(task);
                        task.call();
                    } else {
                        logger.debug("Ignore " + filePath + " because it is not dependency file (.aka)");
                    }
                }

//                es.invokeAll(tasks);
            }
        }
    }

    private static class LoadDependencyTask implements Callable<Void> {

        private static final AkaLogger logger = AkaLogger.get(LoadDependencyTask.class);

        private final String filePath;

        private final List<INode> allNodes;

        public LoadDependencyTask(String filePath, List<INode> allNodes) {
            this.filePath = filePath;
            this.allNodes = allNodes;
        }

        @Override
        public Void call() {
            loadDependenciesFromFile();
            return null;
        }

        /**
         * Load dependencies stored in a dependency file
         */
        private void loadDependenciesFromFile() {
            // parse the content of dependency file
            String json = Utils.readFileContent(filePath);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String path = jsonObject.get("path").getAsString();
            JsonArray dependencies = jsonObject.get("dependency").getAsJsonArray();

            if (path != null && dependencies != null) {
                logger.debug("Loading dependency from: " + path);

                // iterate over all dependencies
                for (JsonElement element : dependencies) {
                    JsonObject cast = (JsonObject) element;

                    // get start node of dependency
                    String start = cast.get("start").getAsString();
                    start = PathUtils.toAbsolute(start);
                    String start_md5 = null;
                    if (cast.get("start-md5") != null)
                        start_md5 = cast.get("start-md5").getAsString();

                    // get end node of dependency
                    String end = cast.get("end").getAsString();
                    end = PathUtils.toAbsolute(end);
                    String end_md5 = null;
                    if (cast.get("end-md5") != null)
                        end_md5 = cast.get("end-md5").getAsString();

                    // get type of the dependency
                    String type = cast.get("type").getAsString();

                    logger.debug("Load a dependency from file: [" + type + "]" + start + " -> " + end);

                    // check matching
                    if (start != null && end != null && type != null) {
                        boolean isMatchingSuccess = findMatching(type, start, end, start_md5, end_md5);
                        if (!isMatchingSuccess) {
                            UnresolvedDependency unresolvedDependency = new UnresolvedDependency(start, end, type);
                            if (!ChangesBetweenSourcecodeFiles.unresolvedDependencies.contains(unresolvedDependency))
                                ChangesBetweenSourcecodeFiles.unresolvedDependencies.add(unresolvedDependency);
                        } else{
                            logger.debug("Match successfully [" + type + "] " + start + " -> " + end);
                        }
                    } else {
                        logger.error("The dependency file is invalid: " + filePath);
                        logger.error("Can not get start, end, or type of dependency.");
                    }
                }
            } else {
                logger.error("The dependency file is invalid: " + filePath);
                logger.error("Can not get path or dependencies");
            }
        }

        /**
         * @param type      the type of the dependency
         * @param start     the absolute path of the start node
         * @param end       the absolute path of the end node
         * @param start_md5 the md5 of the content corresponding to the start node (if it has)
         * @param end_md5   the md5 of the content corresponding to the end node (if it has)
         */
        private boolean findMatching(String type, String start, String end,
                                     String start_md5, String end_md5) {
            String typeID = "com.dse.parser.dependency." + type;

            if (start.length() == 0 || end.length() == 0 || allNodes.size() == 0)
                return false;

            // find the starting nod
            INode startNode = findNode(start, start_md5);
            if (startNode == null) {
                logger.error("Can not find the corresponding node having path " + start);
                return false;
            }

            // find the last node
            INode endNode = findNode(end, end_md5);
            if (endNode == null) {
                logger.error("Can not find the corresponding node having path " + end);
                return false;
            }

            // perform matching
            logger.debug("Find the corresponding nodes of path " + start + " and " + end);
            try {
                Constructor c = Class.forName(typeID).getConstructor(INode.class, INode.class);
                Dependency dependency = (Dependency) c.newInstance(startNode, endNode);

                // Save dependency
                if (!startNode.getDependencies().contains(dependency))
                    startNode.getDependencies().add(dependency);

                if (!endNode.getDependencies().contains(dependency))
                    endNode.getDependencies().add(dependency);

                if (type.equals(TypeDependency.class.getSimpleName())) {
                    if (startNode instanceof IVariableNode) {
                        ((IVariableNode) startNode).setCorrespondingNode(endNode);
                        ((IVariableNode) startNode).setTypeDependencyState(true);
                    } else if (endNode instanceof IVariableNode) {
                        ((IVariableNode) endNode).setCorrespondingNode(startNode);
                        ((IVariableNode) endNode).setTypeDependencyState(true);
                    }
                }

                List<Dependency> dependencies = Environment.getInstance().getDependencies();
                if (!dependencies.contains(dependency))
                    dependencies.add(dependency);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private INode findNode(String absoluteOfNode, String correspondingMd5Checksum) {
            INode startNode = null;

            for (INode n : allNodes) {
                if (PathUtils.equals(n.getAbsolutePath(), absoluteOfNode)) {
                    // find a candidate
                    if (correspondingMd5Checksum == null) {
                        startNode = n;
                        break;
                    } else {
                        // compare md5 if we have
                        if (n instanceof CustomASTNode && ((CustomASTNode) n).getAST() != null)
                            if (Utils.computeMd5(((CustomASTNode) n).getAST().getRawSignature())
                                    .equals(correspondingMd5Checksum)) {
                                startNode = n;
                                break;
                            }
                    }
                }
            }

            return startNode;
        }
    }

    public static class ExpandSourceCodeTask implements Callable<INode> {

        private static final AkaLogger logger = AkaLogger.get(ExpandSourceCodeTask.class);

        private final INode sourceNode;

        public ExpandSourceCodeTask(INode node) {
            this.sourceNode = node;
        }

        @Override
        public INode call() {
            logger.debug("Parsing " + sourceNode.getName());

            String filePath = sourceNode.getAbsolutePath();

            try {
//                LoadingPopupController.getInstance().setText("Expand " + sourceNode.getName() + " upto method level");
                SourcecodeFileParser parser = new SourcecodeFileParser();
                INode newRoot = parser.parseSourcecodeFile(new File(filePath));
                IASTTranslationUnit ast = parser.getTranslationUnit();

                if (sourceNode instanceof ISourcecodeFileNode)
                    ((ISourcecodeFileNode) sourceNode).setAST(ast);

                // expand the tree down to method level
                ParseToLambdaFunctionNode lambdaParser = new ParseToLambdaFunctionNode();
                lambdaParser.parse(newRoot);

                for (Node child : newRoot.getChildren()) {
                    child.setParent(sourceNode);
                    sourceNode.getChildren().add(child);
                }
                
                logger.debug("Found " + sourceNode.getChildren().size() + " children in " + sourceNode.getName());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return sourceNode;
        }
    }
}
