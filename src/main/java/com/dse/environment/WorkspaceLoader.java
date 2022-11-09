package com.dse.environment;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.object.*;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.thread.task.BackgroundTaskObjectController;
import com.dse.thread.task.BackgroundTasksMonitorController;
import com.dse.thread.task.CompileSourcecodeFilesTask;
import com.dse.thread.task.ProjectTreeExpandTask;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.TypeDependency;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.regression.AbstractDifferenceDetecter;
import com.dse.regression.ChangesBetweenSourcecodeFiles;
import com.dse.regression.SimpleDifferenceDetecter;
import com.dse.regression.UnresolvedDependency;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.NodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Load the existing workspace including the dependency files, the hierarchical structure, etc.
 */
public class WorkspaceLoader {
    public final static AkaLogger logger = AkaLogger.get(WorkspaceLoader.class);

    private boolean shouldCompileAgain = false;
    private String elementFolderOfOldVersion;
    private File physicalTreePath;

    private final Node root;
    private AtomicBoolean isLoaded = new AtomicBoolean(false);
    private AtomicBoolean isCancel = new AtomicBoolean(false);

    public WorkspaceLoader(Node root) {
        this.root = root;
    }

    public void load(File physicalTreePath) {
        /*
         * Load the physical tree of the original testing project.
         * During the use of aka, the original testing project may be changed by adding some files.
         * When loading the existing environment, we just parse the file in the original testing project
         */
        logger.debug("Construct the initial tree from " + physicalTreePath.getName());
        isLoaded.set(false);
        isCancel.set(false);

        if (shouldCompileAgain) {
            CompileSourcecodeFilesTask task = new CompileSourcecodeFilesTask();
            // add new Background Task Object to BackgroundTasksMonitor
            BackgroundTaskObjectController controller = BackgroundTaskObjectController.getNewInstance();
            if (controller != null) {
                // parent children relationship synchronize stopping
                controller.setParent(MenuBarController.getMenuBarController().getRebuildEnvironmentBGController());
                if (MenuBarController.getMenuBarController().getRebuildEnvironmentBGController() != null)
                MenuBarController.getMenuBarController().getRebuildEnvironmentBGController().getChildren().add(controller);

                controller.setlTitle("Compiling All Source Code Files");
                // set task to controller to cancel as need when processing
                controller.setTask(task);
                controller.setCancelTitle("Stopping Compile Source Code Files");
                controller.getProgressBar().progressProperty().bind(task.progressProperty());
                controller.getProgressIndicator().progressProperty().bind(task.progressProperty());
                Platform.runLater(() -> BackgroundTasksMonitorController.getController().addBackgroundTask(controller));

                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        if (!task.getValue()) {
                            /*
                             * Parse every source code file to construct the structure of the testing project again.
                             * If there is any change to source code, e.g. someone add/modify a function, we can detect this change.
                             */
                            expand(root);

//                            isLoaded = true;
//                            isCancel = false;
                        } else {
                            isLoaded.set(true);
                            isCancel.set(true);
                        }

                        // remove task if done
                        Platform.runLater(() -> BackgroundTasksMonitorController.getController().removeBackgroundTask(controller));
                    }
                });

                task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        isLoaded.set(true);
                        isCancel.set(true);
                    }
                });
                new Thread(task).start();
            }
        } else {
            expand(root);
            isLoaded.set(true);
            isCancel.set(false);
        }
    }

    private void expand(Node root) {
        ProjectTreeExpandTask projectTreeExpandTask = new ProjectTreeExpandTask(root);
        try {
            projectTreeExpandTask.run();
            if (projectTreeExpandTask.isDone()) {
                isLoaded.set(true);
                isCancel.set(false);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

//        ExecutorService es = Executors.newSingleThreadExecutor();
//        es.submit(projectTreeExpandTask);

//        projectTreeExpandTask.setCallback(new Runnable() {
//            @Override
//            public void run() {
//                isLoaded.set(true);
//                isCancel.set(false);
//            }
//        });
//
//        projectTreeExpandTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent event) {
//                isLoaded.set(true);
//                isCancel.set(false);
//            }
//        });

//        logger.debug("Start expanding the initial tree down to method level");
//        List<INode> sourcecodeFiles = Search.searchNodes(root, new SourcecodeFileNodeCondition());
//        int N = sourcecodeFiles.size() - 1;
//
//        for (int idx = N; idx >= 0; idx--) {
//            INode sourcecodeFile = sourcecodeFiles.get(idx);
//
//            logger.debug("\tParsing " + sourcecodeFile.getAbsolutePath());
//            String content = Utils.readFileContent(sourcecodeFile.getAbsolutePath());
//            try {
//                IASTTranslationUnit ast = Utils.getIASTTranslationUnitforCpp(content.toCharArray());
//                ((SourcecodeFileNode) sourcecodeFile).setAST(ast);
//
//                // expand the tree down to method level
//                SourcecodeFileParser cppParser = new SourcecodeFileParser();
//                cppParser.setSourcecodeNode((SourcecodeFileNode) sourcecodeFile);
//                INode newRoot = cppParser.parseSourcecodeFile(new File(sourcecodeFile.getAbsolutePath()));
//                for (Node child : newRoot.getChildren()) {
//                    child.setParent(sourcecodeFile);
//                    sourcecodeFile.getChildren().add(child);
//                }
//                logger.debug("Size of children in " + sourcecodeFile.getAbsolutePath() + ": " + sourcecodeFile.getChildren().size());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        logger.debug("Finish expanding the initial tree down to method level");
//
//        /*
//         * Load stub library physical tree
//         */
//        SystemLibraryRoot sysLibRoot = SystemLibrary.parseFromFile();
//        Environment.getInstance().setSystemLibraryRoot(sysLibRoot);
//        logger.debug("Load stub library from file");
//
//        /*
//         * Load data user code physical tree
//         */
//        INode userCodeRoot = EnvironmentUserCode.getInstance().parseTree();
//        Environment.getInstance().setUserCodeRoot(userCodeRoot);
//        logger.debug("Load data user code physical tree");
//
//        /*
//         * Load dependencies
//         */
//        logger.debug("Load dependencies from file");
//        loadDependenciesFromAllFiles(root, sysLibRoot, userCodeRoot);
//
//        detectChangeInSourcecodeFilesWhenOpeningEnv(root, elementFolderOfOldVersion);
//
//        // load unit under test for enviro node
//        loadUnitTestableState();
//
//        exportVersionComparisonToFile();
    }

    public boolean isLoaded() {
        return isLoaded.get();
    }

    public boolean isCancel() {
        return isCancel.get();
    }

    public Node getRoot() {
        return root;
    }

    public File getPhysicalTreePath() {
        return physicalTreePath;
    }

    public void setElementFolderOfOldVersion(String elementFolderOfOldVersion) {
        this.elementFolderOfOldVersion = elementFolderOfOldVersion;
    }

    public String getElementFolderOfOldVersion() {
        return elementFolderOfOldVersion;
    }

    public void setPhysicalTreePath(File physicalTreePath) {
        this.physicalTreePath = physicalTreePath;
    }

    public void setShouldCompileAgain(boolean shouldCompileAgain) {
        this.shouldCompileAgain = shouldCompileAgain;
    }
}
