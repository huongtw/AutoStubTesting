package com.dse.guifx_v3.controllers.build_environment;

import com.dse.compiler.ICompiler;
import com.dse.compiler.message.CompileMessage;
import com.dse.compiler.message.ICompileMessage;
import com.dse.compiler.message.error_tree.CompileErrorSearch;
import com.dse.compiler.message.error_tree.CompileMessageParser;
import com.dse.compiler.message.error_tree.node.*;
import com.dse.config.AkaConfig;
import com.dse.config.CommandConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.config.compile.LinkEntry;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.HeaderNode;
import com.dse.parser.object.INode;
import com.dse.resolver.*;
import com.dse.logger.AkaLogger;
import com.dse.util.CompilerUtils;
import com.dse.util.PathUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SrcResolverController implements Initializable {

    private final static AkaLogger logger = AkaLogger.get(SrcResolverController.class);

    private String directory;
    private List<INode> sourceFiles;
    private AtomicInteger startIdx;
    private ICompiler compiler;
    private Stage stage;
    private Map<String, String> compilationCommands = new HashMap<>();

    @FXML
    private Text txtFilePath;
    @FXML
    private TextArea txtCompileMessage;
    @FXML
    private Button btnOpenFile;
    @FXML
    private Button btnHelp;
    @FXML
    private Button btnAbort;
    @FXML
    private Button btnIgnoreAll;
    @FXML
    private Button btnIgnore;
    @FXML
    private ListView<IErrorNode> lvMissing;
    @FXML
    public ListView<ResolvedSolution> lvResolved;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtCompileMessage.setWrapText(true);
        txtCompileMessage.setEditable(false);

        lvMissing.setOnMouseClicked(event -> {
            IErrorNode errorNode = lvMissing.getSelectionModel().getSelectedItem();
            displayResolveMessage(errorNode);
        });

        lvResolved.setCellFactory(lv -> new ResolvedSolutionCell());
        setHint();
    }

    public void setHint() {
        Hint.tooltipNode(btnOpenFile, HintContent.EnvBuilder.SrcResolver.OPEN_FILE);
        Hint.tooltipNode(btnHelp, HintContent.EnvBuilder.SrcResolver.OPEN_HELP);
        Hint.tooltipNode(btnAbort, HintContent.EnvBuilder.SrcResolver.ABORT);
        Hint.tooltipNode(btnIgnoreAll, HintContent.EnvBuilder.SrcResolver.IGNORE_ALL);
        Hint.tooltipNode(btnIgnore, HintContent.EnvBuilder.SrcResolver.IGNORE);
    }

    public class ResolvedSolutionCell extends ListCell<ResolvedSolution> {
        private final ContextMenu contextMenu = new ContextMenu();

        private void setupContextMenu() {
            MenuItem addToSourceItem = new MenuItem("Add to source");

            addToSourceItem.setOnAction(event -> {
                ResolvedSolution solution = getItem();
                useSolution(solution);
            });

            contextMenu.getItems().clear();
            contextMenu.getItems().add(addToSourceItem);
            setContextMenu(contextMenu);
        }

        private void useSolution(ResolvedSolution solution) {
            SolutionManager.getInstance().use(solution);

            // re-parse
            BaseController.addAnalysisInformationToWorkspace(
                    new AkaConfig().fromJson().getOpeningWorkspaceDirectory(),
                    new WorkspaceConfig().fromJson().getDependencyDirectory(),
                    new WorkspaceConfig().fromJson().getPhysicalJsonFile(),
                    new WorkspaceConfig().fromJson().getElementDirectory());

            // re-compile
            ICompileMessage message = findErrorSourcecodeFile(compiler, sourceFiles, startIdx);

            if (message.getType() != ICompileMessage.MessageType.ERROR) {
                getStage().close();
                createSucessAlertWhenBuilding();
                exportInformationExternally();
            }
        }

        @Override
        protected void updateItem(ResolvedSolution item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                ResolvedSolution solution = getItem();

                if (solution != null) {
                    setText(solution.getResolvedSourceCode());
                    setupContextMenu();
                }
            }
        }
    }

//    public int findProblemInTestedProject(Node projectRootNode) {
//        if (projectRootNode != null) {
//            AtomicInteger startIdx = new AtomicInteger(0);
//            setStartIdx(startIdx);
//
//            List<INode> sourceFiles = findAllSourceCodeFiles(projectRootNode);
//            setSourceFiles(sourceFiles);
//
//            ICompiler compiler = updateCompiler();
//            setCompiler(compiler);
//
//            if (sourceFiles.isEmpty())
//                return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION;
//
//            ICompileMessage message = findErrorSourcecodeFile(compiler, sourceFiles, startIdx);
//            if (message.getType() == ICompileMessage.MessageType.ERROR)
//                return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION;
//            else return BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.COMPILATION;
//        }
//        else
//            return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION;
//    }
//
//    private ICompiler updateCompiler() {
//        // set compiler
//        return Environment.getInstance().getCompiler();
//    }
//
//    private List<INode> findAllSourceCodeFiles(INode projectRootNode) {
//        // search for source code file nodes in source code file lists and compile them
//        List<INode> sourceFiles = Search.searchNodes(projectRootNode, new SourcecodeFileNodeCondition());
//        List<INode> ignores = Environment.getInstance().getIgnores();
//        List<INode> uuts = Environment.getInstance().getUUTs();
//        List<INode> sbfs = Environment.getInstance().getSBFs();
//        List<String> libraries = ProjectClone.getLibraries();
//        sourceFiles.removeIf(f -> ignores.contains(f) || libraries.contains(f.getAbsolutePath())
//                || (f instanceof HeaderNode && (!uuts.contains(f) && !sbfs.contains(f))));
//        sourceFiles.forEach(sourceFile -> {
//            try {
//                String content = Utils.readFileContent(sourceFile.getAbsolutePath());
//                IASTTranslationUnit iastNode = new SourcecodeFileParser().getIASTTranslationUnit(content.toCharArray());
//                if (sourceFile instanceof SourcecodeFileNode) {
//                    ((SourcecodeFileNode) sourceFile).setAST(iastNode);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        return sourceFiles;
//    }

    private boolean isIncludedFile(INode node) {
        for (Dependency d : node.getDependencies()) {
            if (d instanceof IncludeHeaderDependency) {
                if (d.getEndArrow() == node)
                    return true;
            }
        }

        return false;
    }

    @FXML
    private void onBtnIgnoreAllClick() {
        if (compiler != null && sourceFiles != null && startIdx.get() >= 0) {

            if (startIdx.get() < sourceFiles.size() - 1) {
                // move to the next source code file
                startIdx.set(startIdx.get() + 1);

                // compile all source code files and do not care whether they has problems or not
                while (startIdx.get() < sourceFiles.size()) {
                    findErrorSourcecodeFile(compiler, sourceFiles, startIdx);
                    startIdx.set(startIdx.get() + 1);
                }
            }

            logger.debug("Linking source code file to create an executable file");
            boolean linkageSuccessed = linkSourceFiles();
            if (linkageSuccessed) {
                getStage().close();
                createSucessAlertWhenBuilding();
                exportInformationExternally();
            } else {
                createFailureAlertWhenBuilding(new CompileMessage("Can not create executable file from object files", ""));

                // reset the index. Users may modify the source code file and build again.
                startIdx.set(0);
            }
        }
    }

    private String[] getAllOutfilePaths(List<INode> nodes) {
        String[] filePaths = nodes.stream()
                .filter(file -> !isIncludedFile(file))
                .map(f -> CompilerUtils.getOutfilePath(f.getAbsolutePath(), compiler.getOutputExtension()))
                .toArray(String[]::new);

        return filePaths;
    }

    @FXML
    private void onBtnIgnoreClick() {
        if (startIdx.get() >= 0 && startIdx.get() < sourceFiles.size() - 1) {
            // move to the next source code file
            startIdx.set(startIdx.get() + 1);

            // just compile the current source code file and do not do anything
            findErrorSourcecodeFile(compiler, sourceFiles, startIdx);
        }
        else
//        if (startIdx.get() >= sourceFiles.size())
        {
            // all source code files are compiled. Now we need to create executable files
            logger.debug("Linking source code file to create an executable file");
            boolean linkageSuccessed = linkSourceFiles();

            if (linkageSuccessed) {
                getStage().close();
                createSucessAlertWhenBuilding();
                exportInformationExternally();
            } else {
                createFailureAlertWhenBuilding(new CompileMessage("Can not create executable file", ""));

                // reset the index. Users may modify the source code file and build again.
                startIdx.set(0);
            }
        }
    }

    private void exportInformationExternally() {
        // TODO:
    }
    @FXML
    private void onBtnAbortClick() {
        getStage().close();
    }

    @FXML
    private void onBtnHelpClick(ActionEvent event) {
        MenuBarController.getMenuBarController().openUserManual(event);
    }

    @FXML
    private void onBtnOpenFileClick() {
        if (sourceFiles != null) {
            INode currentFile = sourceFiles.get(startIdx.get());
            try {
                UIController.openTheLocation(currentFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean linkSourceFiles() {
        String executablePath = directory + (directory.endsWith(File.separator) ? "" : File.separator) + "program.exe";

        List<INode> linkableSourceCodes = sourceFiles.stream()
                .filter(n -> !(n instanceof HeaderNode))
                .collect(Collectors.toList());

        String[] filePaths = getAllOutfilePaths(linkableSourceCodes);

        if (compiler != null) {
            ICompileMessage linkeMessage = compiler.link(executablePath, filePaths);

            if (linkeMessage == null)
                linkeMessage = new CompileMessage("Can not create executable file", "");

            logger.debug("Linking command: " + linkeMessage.getLinkingCommand());
            LinkEntry linkEntry = new LinkEntry();
            linkEntry.setBinFiles(Arrays.asList(filePaths));
            linkEntry.setCommand(compiler.getLinkCommand());
            linkEntry.setOutFlag(compiler.getOutputFlag());
            linkEntry.setExeFile(executablePath);
//            new CommandConfig().fromJson().setLinkingCommand(linkeMessage.getLinkingCommand()).exportToJson();
            new CommandConfig().fromJson().setLinkEntry(linkEntry).exportToJson();

            return (linkeMessage.getType() == ICompileMessage.MessageType.EMPTY)
                    && new File(executablePath).exists();
        } else{
            logger.error("Can not linkage because the directory is not set up");
            return false;
        }
    }

    private Alert createSucessAlertWhenBuilding() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Build Environment");

        alert.setHeaderText(null);
        alert.setContentText("Build environment successfully");
        alert.showAndWait();
        return alert;
    }

    private Alert createFailureAlertWhenBuilding(ICompileMessage message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Build Environment");

        alert.setHeaderText("Can not build environment");

        if (message.getMessage() != null && message.getMessage().length() > 0) {
            TextArea textArea = new TextArea();
            textArea.setText(message.getMessage());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            alert.getDialogPane().setContent(textArea);
        }

        alert.showAndWait();
        return alert;
    }

    private ICompileMessage findErrorSourcecodeFile(ICompiler compiler, List<INode> sourceFiles, AtomicInteger startIdx) {
        ICompileMessage message = null;

        if (startIdx.get() >= 0 && startIdx.get() < sourceFiles.size()) {
            do {
                INode fileNode = sourceFiles.get(startIdx.get());
                String filePath = fileNode.getAbsolutePath();
                logger.debug("Compiling " + filePath);
                message = compiler.compile(fileNode);

                // save the compilation commands to file
                String relativePath = PathUtils.toRelative(filePath);
                if (!compilationCommands.containsKey(relativePath)) {
                    compilationCommands.put(relativePath, message.getCompilationCommand());
                    new CommandConfig().fromJson().setCompilationCommands(compilationCommands).exportToJson();
                }

                String outputPath = CompilerUtils.getOutfilePath(filePath, compiler.getOutputExtension());
                File outputFile = new File(outputPath);

                // check if compile successfully
                if (message.getType() == ICompileMessage.MessageType.ERROR || !outputFile.exists()) {
                    displayProblemOnScreen(filePath, message);
                    break;
                } else {
                    // just warning or compile successfully
                    startIdx.set(startIdx.get() + 1);
                }
            } while (startIdx.get() < sourceFiles.size());
        }

        logger.debug("Linking source code file to create an executable file");
        boolean linkageSuccessed = linkSourceFiles();
        if (linkageSuccessed) {
            getStage().close();
            createSucessAlertWhenBuilding();
            exportInformationExternally();
        } else {
            //createFailureAlertWhenBuilding(new CompileMessage("Can not create executable file", ""));
        }
        return message;
    }



    private List<IErrorNode> getAllUndeclaredErrors(ICompileMessage compileMessage) {
        CompileMessageParser parser = new CompileMessageParser(compileMessage);

        RootErrorNode rootErrorNode = parser.parse();

        return CompileErrorSearch.searchNodes(rootErrorNode, UndeclaredErrorNode.class);
    }

    public void displayProblemOnScreen(String title, ICompileMessage message) {
        txtFilePath.setText(title);
        txtCompileMessage.setText(message.getMessage());

        List<IErrorNode> undeclaredErrors = getAllUndeclaredErrors(message);

        lvMissing.getItems().clear();
        lvMissing.getItems().addAll(undeclaredErrors);

        lvResolved.getItems().clear();
        lvResolved.refresh();
    }

    private void displayResolveMessage(IErrorNode errorNode) {
        if (errorNode instanceof UndeclaredErrorNode) {
            IUndeclaredResolver resolver = null;

            if (errorNode instanceof IUndeclaredFunctionErrorNode) {
                resolver = new UndeclaredFunctionResolver((IUndeclaredFunctionErrorNode) errorNode);
            } else if (errorNode instanceof IUndeclaredVariableErrorNode) {
                resolver = new UndeclaredVariableResolver((IUndeclaredVariableErrorNode) errorNode);
            } else if (errorNode instanceof IUndeclaredTypeErrorNode) {
                resolver = new UndeclaredTypeResolver((IUndeclaredTypeErrorNode) errorNode);
            }

            if (resolver != null) {
                try {
                    resolver.resolve();
                    List<ResolvedSolution> solutions = resolver.getSolutions();
                    lvResolved.getItems().clear();
                    lvResolved.getItems().addAll(solutions);
                } catch (Exception ex) {
                    lvResolved.getItems().clear();
                    logger.debug(ex);
                } finally {
                    lvResolved.refresh();

                }
            }
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setSourceFiles(List<INode> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public void setCompiler(ICompiler compiler) {
        this.compiler = compiler;
    }

    public ICompiler getCompiler() {
        return compiler;
    }

    public void setStartIdx(AtomicInteger startIdx) {
        this.startIdx = startIdx;
    }

    public Map<String, String> getCompilationCommands() {
        return compilationCommands;
    }

    public void setCompilationCommands(Map<String, String> compilationCommands) {
        this.compilationCommands = compilationCommands;
    }
}
