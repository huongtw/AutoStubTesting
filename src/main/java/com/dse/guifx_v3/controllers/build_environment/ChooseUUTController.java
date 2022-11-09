package com.dse.guifx_v3.controllers.build_environment;

import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.*;
import com.dse.guifx_v3.controllers.object.build_environment.ChooseUUTTreeCell;
import com.dse.guifx_v3.controllers.object.build_environment.ChooseUUTTreeItem;
import com.dse.guifx_v3.controllers.object.build_environment.UnitNamesPath;
import com.dse.guifx_v3.controllers.object.unit_node.AbstractUnitNode;
import com.dse.guifx_v3.controllers.object.unit_node.DependencyNode;
import com.dse.guifx_v3.controllers.object.unit_node.UUTRootNode;
import com.dse.guifx_v3.controllers.object.unit_node.UnitUnderTestNode;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.parser.VectorCastProjectLoader;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChooseUUTController extends AbstractCustomController implements Initializable {
    final static AkaLogger logger = AkaLogger.get(ChooseUUTController.class);
    //    @FXML
//    private ToggleGroup tgStubDependencies;
    @FXML
    private ListView<UnitNamesPath> lvUnitNames; // where we put all source code files
    @FXML
    private Button bForwardArrow;
    @FXML
    private Button bBackwardArrow;
    @FXML
    private TextField tfUnitNamesPath;
    @FXML
    private TreeTableView<AbstractUnitNode> ttvUnitsUnderTest; // where we put UUT, SBF, IGNORE, and DO_NOT_STUB
    @FXML
    private TreeTableColumn<AbstractUnitNode, String> ttcUnitName;
    @FXML
    private TreeTableColumn<AbstractUnitNode, String> ttcType;
    @FXML
    private ListView<Label> lvLibraryStub;

//    @FXML
//    private ListView<CheckBox> lvAdditionalStub;
//    @FXML
//    private ListView<CheckBox> lvSuppressedStub;
//    @FXML
//    private ListView<CheckBox> lvSuppressedTestableFunction;
//    @FXML
//    private ListView<CheckBox> lvNotSupportedType;

    private UnitNamesPath currentUnitSelected = null;
    private ChooseUUTTreeItem currentUUTSelected = null;
    private Label currentLibraryStubSelected = null;
    //    private CheckBox currentAdditionalStubSelected = null;
//    private CheckBox currentSuppressedStubSelected = null;
//    private CheckBox currentSuppressedTestableFunctionSelected = null;
//    private CheckBox currentNotSupportedTypeSelected = null;
    private ChooseUUTTreeItem root;
//    private String stubDependenciesType = StubDependenciesType.ALL;

    public static void main(String[] argv) {
        // test getIgnoreFolders()
        List<File> searchDirectories = new ArrayList<>();
        searchDirectories.add(new File("/home/hoannv/IdeaProjects/akautauto/datatest/duc-anh/TSDV_log4cpp/include"));

        File rootDirectory = new File("/home/hoannv/IdeaProjects/akautauto/datatest/duc-anh/TSDV_log4cpp");

//        List<File> ignoreFolders = getIgnoreFolders(rootDirectory, searchDirectories);
        int i = 0;
        for (File file : rootDirectory.listFiles()) {
            System.out.println(file.getAbsolutePath());
            System.out.println(i++);
        }
    }

    private List<String> getLibraries(List<IEnvironmentNode> libs) {
        List<String> libraries = new ArrayList<>();

        libs.forEach(lib -> {
            String path = null;
            if (lib instanceof EnviroLibraryIncludeDirNode)
                path = ((EnviroLibraryIncludeDirNode) lib).getLibraryIncludeDir();
            else if (lib instanceof EnviroTypeHandledSourceDirNode)
                path = ((EnviroTypeHandledSourceDirNode) lib).getTypeHandledSourceDir();

            if (path != null) {
                File[] files = new File(path).listFiles();
                if (files != null) {
                    for (File file : files) {
                        libraries.add(file.getAbsolutePath());
                    }
                }
            }
        });

        return libraries;
    }

//    private static File getRootDirectory(List<IEnvironmentNode> searchListNodes) {
//        if (searchListNodes.size() >= 1) {
//            EnviroSearchListNode cast = (EnviroSearchListNode) searchListNodes.get(0);
//            File file = new File(cast.getSearchList());
//            return file;
//
//        } else if (searchListNodes.size() == 0) {
//            logger.debug("There is no search list. Add at least once!");
//        }
//        return new File("");
//    }

//    private static List<File> getIgnoreFolders(File rootDirectory, List<File> searchListPaths) {
//        List<File> ignoreFolders = new ArrayList<>();
//        for (File file : Objects.requireNonNull(rootDirectory.listFiles(File::isDirectory))) {
//            if (file.isDirectory()) {
//                if (searchListPaths.contains(file)) {
//                    ignoreFolders.addAll(getIgnoreFolders(file, searchListPaths));
//                } else {
//                    ignoreFolders.add(file);
//                }
//            }
//        }
//        return ignoreFolders;
//    }

    public void initialize(URL location, ResourceBundle resources) {
        Image forward = new Image(Object.class.getResourceAsStream("/icons/forward-arrow.png"));
        Image backward = new Image(Object.class.getResourceAsStream("/icons/backward-arrow.png"));
        bForwardArrow.setGraphic(new ImageView(forward));
        Hint.tooltipNode(bForwardArrow, HintContent.EnvBuilder.UUTChooser.SELECT);
        bBackwardArrow.setGraphic(new ImageView(backward));
        Hint.tooltipNode(bBackwardArrow, HintContent.EnvBuilder.UUTChooser.DESELECT);

        lvUnitNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && tfUnitNamesPath != null)
                tfUnitNamesPath.setText(newValue.getAbsolutePath());

            if (newValue != null) {
                currentUnitSelected = newValue;
            }
        });

        lvUnitNames.setCellFactory(new Callback<ListView<UnitNamesPath>, ListCell<UnitNamesPath>>() {
            @Override
            public ListCell<UnitNamesPath> call(ListView<UnitNamesPath> param) {

                // update the icon of source code file if it is added to stub/test
                ListCell<UnitNamesPath> cell = new ListCell<UnitNamesPath>() {
                    @Override
                    protected void updateItem(UnitNamesPath item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName());
                            if (item.isChoosed()) {
                                setGraphic(Factory.getIcon(item));
                            } else {
                                setGraphic(null);
                            }
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                        lvUnitNames.refresh();
                    }
                };

                // save the selected unit
                cell.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        currentUnitSelected = cell.getListView().getSelectionModel().getSelectedItem();
                        if (!currentUnitSelected.isChoosed()) {
                            addUUT();
                            currentUnitSelected.setChoosed(true);
                        }
                    } else if (event.getClickCount() == 1) {
                        currentUnitSelected = cell.getListView().getSelectionModel().getSelectedItem();
                    }
                });
                return cell;
            }
        });

        lvLibraryStub.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentLibraryStubSelected = newValue;
                tfUnitNamesPath.setText("");
            }
        });
//        lvAdditionalStub.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                currentAdditionalStubSelected = newValue;
//            }
//        });
//        lvSuppressedStub.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                currentSuppressedStubSelected = newValue;
//            }
//        });
//        lvSuppressedTestableFunction.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                currentSuppressedTestableFunctionSelected = newValue;
//            }
//        });
//        lvNotSupportedType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                currentNotSupportedTypeSelected = newValue;
//            }
//        });

        root = new ChooseUUTTreeItem(new UUTRootNode("ROOT"));
        ttvUnitsUnderTest.setRoot(root);

        ttcUnitName.setCellValueFactory(param -> {
            if (param.getValue().getValue() == null) {
                return null;
            } else {
                return new SimpleStringProperty(param.getValue().getValue().getName());
            }
        });

        ttcType.setCellValueFactory(param -> {
            if (param.getValue().getValue() == null) {
                return null;
            } else {
                Object obj = param.getValue().getValue();
                if (obj instanceof UnitUnderTestNode) {
                    return new SimpleStringProperty(((UnitUnderTestNode) obj).getStubType());
                } else if (obj instanceof DependencyNode) {
                    return new SimpleStringProperty(((DependencyNode) obj).getType());
                } else {
                    logger.debug("You did not handle the case " + obj.getClass());
                    return new SimpleStringProperty("no name");
                }
            }
        });

        // display the absolute path of the selected unit (Unit under test or Dependency unit)
        ttvUnitsUnderTest.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.getValue() instanceof UnitUnderTestNode) {
                    tfUnitNamesPath.setText(((UnitUnderTestNode) newValue.getValue()).getAbsolutePath());
                } else if (newValue.getValue() instanceof DependencyNode) {
                    tfUnitNamesPath.setText(((DependencyNode) newValue.getValue()).getSourcecodeFileNode().getAbsolutePath());
                }
            } else {
                tfUnitNamesPath.setText("");
            }
            if (newValue instanceof ChooseUUTTreeItem) {
                currentUUTSelected = (ChooseUUTTreeItem) newValue;
            }
        }));

        ttvUnitsUnderTest.setRowFactory(param -> new ChooseUUTTreeCell());

//        updateStubDependencies();

//        tgStubDependencies.
    }

    public void update() {
        logger.debug("Update Choose UUT");

        if (Environment.WindowState.isIsSearchListNodeUpdated() /*there is no change*/
                || lvUnitNames.getItems().isEmpty() /*there is no item*/) {
            // Delete all existing source code files in the unit names section
            clear();

            logger.debug("Deleted the existing unit names in GUI");

            // Add source code files to unit names section
            logger.debug("Adding new unit names to unit names section");
            List<IEnvironmentNode> searchListNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroSearchListNode());
            List<IEnvironmentNode> typeHandledNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroTypeHandledSourceDirNode());
            searchListNodes.addAll(typeHandledNodes);
            List<IEnvironmentNode> libraryNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryIncludeDirNode());
            searchListNodes.addAll(libraryNodes);

            if (searchListNodes.size() >= 1) {
                ProjectNode projectRootNode = (ProjectNode) constructPhysicalTree(searchListNodes);
                String workingDir = projectRootNode.getFile().getParentFile().getAbsoluteFile() + File.separator + WorkspaceConfig.WORKING_SPACE_NAME;
                new AkaConfig().fromJson().setWorkingDirectory(workingDir).exportToJson();

                // Find all source code files and display it
                List<INode> units = Search.searchNodes(projectRootNode, new SourcecodeFileNodeCondition());
                List<String> libraries = getLibraries(libraryNodes);
                units.removeIf(unit -> libraries.contains(unit.getAbsolutePath()));

                for (INode unit : units) {
                    if (isVisibleOnGUI(unit)) {
                        lvUnitNames.getItems().add(new UnitNamesPath((ISourcecodeFileNode) unit));
                    } else {
                        logger.debug("Do not add " + unit.getAbsolutePath() + " to GUI");
                    }
                }

                ttvUnitsUnderTest.getRoot().getChildren()
                        .removeIf(n -> lvUnitNames.getItems().stream()
                                .noneMatch(u -> u.getName().equals(n.getValue().getName())));

                // show selected units
                showSelectedIgnoreSourcecodeFile();
                showSelectedNotStubNode();
                showSelectedSBFs();
                showSelectedUUTs();

                // refresh the lvUitNames to display arrow of chosen unit
                lvUnitNames.refresh();

                // Save the project root
                Environment.getInstance().setProjectNode(projectRootNode);

            } else if (searchListNodes.size() == 0) {
                logger.debug("There is no search list. Add at least once!");
            }
        } else {
            logger.debug("There is no update on Choose UUTs window");
        }

        updateLibraryStubs();
        validate();
    }

    private boolean isVisibleOnGUI(INode node) {
//        if (libraries.contains(node.getAbsolutePath()))
//            return false;

        if (node instanceof CFileNode)
            return true;

        if (node instanceof CppFileNode)
            return true;

        if (node instanceof HeaderNode) {
            return true;
//            List<INode> functions = Search.searchNodes(node, new FunctionNodeCondition());
//            return !functions.isEmpty();
        }

        return false;
    }


    /**
     * Given a list of directories, create a tree containing these directories and its files
     *
     * @param searchListNodes
     * @return
     */
    private INode constructPhysicalTree(List<IEnvironmentNode> searchListNodes) {
        // Get the source code file lists
        List<File> searchListPaths = new ArrayList<>();
        for (IEnvironmentNode searchListNode : searchListNodes) {
            if (searchListNode instanceof EnviroSearchListNode) {
                EnviroSearchListNode cast = (EnviroSearchListNode) searchListNode;
                searchListPaths.add(new File(cast.getSearchList()));
            } else if (searchListNode instanceof EnviroLibraryIncludeDirNode) {
                EnviroLibraryIncludeDirNode cast = (EnviroLibraryIncludeDirNode) searchListNode;
                searchListPaths.add(new File(cast.getLibraryIncludeDir()));
            } else if (searchListNode instanceof EnviroTypeHandledSourceDirNode) {
                EnviroTypeHandledSourceDirNode cast = (EnviroTypeHandledSourceDirNode) searchListNode;
                searchListPaths.add(new File(cast.getTypeHandledSourceDir()));
            }
        }

        // parse the source code file lists
        VectorCastProjectLoader loader = new VectorCastProjectLoader();
        loader.setSourcecodeList(searchListPaths);
        INode projectRootNode = loader.constructPhysicalTree();
        logger.debug("Constructed the physical tree corresponding to the source code files list");
        return projectRootNode;
    }

    private void showSelectedNotStubNode() {
        List<IEnvironmentNode> uutNodes =
                EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(),
                        new EnviroDontStubNode());
        for (UnitNamesPath namesPath : lvUnitNames.getItems()) {
            // check whether uutNode is added to test when creating env before
            boolean isAdded = isAdded(namesPath, uutNodes);
            if (isAdded) {
                UnitUnderTestNode unitUnderTestNode = new UnitUnderTestNode(namesPath);
                unitUnderTestNode.setStubType(UnitUnderTestNode.DONT_STUB);
                unitUnderTestNode.loadDependencies(unitUnderTestNode.getSourcecodeFileNode());
                root.getChildren().add(new ChooseUUTTreeItem(unitUnderTestNode));
                namesPath.setChoosed(true);
            }
        }
    }

    private void showSelectedIgnoreSourcecodeFile() {
        List<IEnvironmentNode> uutNodes =
                EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(),
                        new EnviroIgnoreNode());
        for (UnitNamesPath namesPath : lvUnitNames.getItems()) {
            // check whether uutNode is added to test when creating env before
            boolean isAdded = isAdded(namesPath, uutNodes);
            if (isAdded) {
                UnitUnderTestNode unitUnderTestNode = new UnitUnderTestNode(namesPath);
                unitUnderTestNode.setStubType(UnitUnderTestNode.IGNORE);
                unitUnderTestNode.loadDependencies(unitUnderTestNode.getSourcecodeFileNode());
                root.getChildren().add(new ChooseUUTTreeItem(unitUnderTestNode));
                namesPath.setChoosed(true);
            }
        }
    }

    private boolean isAdded(UnitNamesPath namesPath, List<IEnvironmentNode> uutNodes) {
        // check whether uutNode is added to test when creating env before
        boolean isAdded = false;
        for (IEnvironmentNode uutNode : uutNodes) {
            String normalizedUutPath = null;
            if (uutNode instanceof EnviroIgnoreNode)
                normalizedUutPath = Utils.normalizePath(((EnviroIgnoreNode) uutNode).getName());
            else if (uutNode instanceof EnviroDontStubNode)
                normalizedUutPath = Utils.normalizePath(((EnviroDontStubNode) uutNode).getName());
            else if (uutNode instanceof EnviroSBFNode)
                normalizedUutPath = Utils.normalizePath(((EnviroSBFNode) uutNode).getName());
            else if (uutNode instanceof EnviroUUTNode)
                normalizedUutPath = Utils.normalizePath(((EnviroUUTNode) uutNode).getName());

            if (normalizedUutPath != null) {
                String normalizedNamePath = Utils.normalizePath(namesPath.getAbsolutePath());
                if (normalizedUutPath.endsWith(normalizedNamePath)) {
                    isAdded = true;
                    break;
                }
            }
        }
        return isAdded;
    }

    private void showSelectedSBFs() {
        List<IEnvironmentNode> uutNodes =
                EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(),
                        new EnviroSBFNode());
        for (UnitNamesPath namesPath : lvUnitNames.getItems()) {
            // check whether uutNode is added to test when creating env before
            boolean isAdded = isAdded(namesPath, uutNodes);
            if (isAdded) {
                UnitUnderTestNode unitUnderTestNode = new UnitUnderTestNode(namesPath);
                unitUnderTestNode.setStubType(UnitUnderTestNode.SBF);
                unitUnderTestNode.loadDependencies(unitUnderTestNode.getSourcecodeFileNode());
                root.getChildren().add(new ChooseUUTTreeItem(unitUnderTestNode));
                namesPath.setChoosed(true);
            }
        }
    }

    private void showSelectedUUTs() {
        List<IEnvironmentNode> uutNodes =
                EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(),
                        new EnviroUUTNode());
        for (UnitNamesPath namesPath : lvUnitNames.getItems()) {
            // check whether uutNode is added to test when creating env before
            boolean isAdded = isAdded(namesPath, uutNodes);
            if (isAdded) {
                UnitUnderTestNode unitUnderTestNode = new UnitUnderTestNode(namesPath);
                unitUnderTestNode.setStubType(UnitUnderTestNode.UUT);
                unitUnderTestNode.loadDependencies(unitUnderTestNode.getSourcecodeFileNode());
                root.getChildren().add(new ChooseUUTTreeItem(unitUnderTestNode));
                namesPath.setChoosed(true);
            }
        }
    }

    /**
     * when ChooseUUTController load from environment, it reconstruct physical tree of the project
     * and update added and deleted file on list view UnitNames
     */
    @Override
    public void loadFromEnvironment() {
        if (Environment.getInstance().getProjectNode() == null)
            UIController.showErrorDialog("You must open an environment firstly", "Update Environment", "There is no opening environment");
        else {
            // Delete all existing source code files in the unit names section
            clear();

            // reparse SearchListNodes
            logger.debug("Deleted the existing unit names in GUI");

            // Add source code files to unit names section
            logger.debug("Adding new unit names to unit names section");
            List<IEnvironmentNode> searchListNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroSearchListNode());
            List<IEnvironmentNode> typeHandledNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroTypeHandledSourceDirNode());
            searchListNodes.addAll(typeHandledNodes);
            List<IEnvironmentNode> libraryNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryIncludeDirNode());
            searchListNodes.addAll(libraryNodes);

            if (searchListNodes.size() >= 1) {
                ProjectNode projectRootNode = (ProjectNode) constructPhysicalTree(searchListNodes);
                // Save the project root
                Environment.getInstance().setProjectNode(projectRootNode);

                // Find all source code files and display it
                List<INode> units = Search.searchNodes(projectRootNode, new SourcecodeFileNodeCondition());
                List<String> libraries = getLibraries(libraryNodes);
                units.removeIf(unit -> libraries.contains(unit.getAbsolutePath()));

                // show all unit
                for (INode unit : units)
                    if (isVisibleOnGUI(unit)) {
                        lvUnitNames.getItems().add(new UnitNamesPath((ISourcecodeFileNode) unit));
                    } else {
                        logger.debug("Do not add " + unit.getAbsolutePath() + " to GUI");
                    }

                // show selected units
                showSelectedIgnoreSourcecodeFile();
                showSelectedNotStubNode();
                showSelectedSBFs();
                showSelectedUUTs();

                // show selected library stubs
                showLibraryStub();

                // refresh the lvUitNames to display arrow of chosen unit
                lvUnitNames.refresh();

                validate();

            } else if (searchListNodes.size() == 0) {
                logger.debug("There is no search list. Add at least once!");
            }
        }
    }

    private void showLibraryStub() {
        List<IEnvironmentNode> libraryNode = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroLibraryStubNode());
        if (libraryNode.size() == 1) {
            EnviroLibraryStubNode libraryStubNode = (EnviroLibraryStubNode) libraryNode.get(0);
            for (String key : libraryStubNode.getLibraries().keySet()) {
                String value = libraryStubNode.getLibraries().get(key);

                Label descriptionLibStub = new Label(value + EnviroLibraryStubNode.SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER + key);
//                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> lvLibraryStub.getSelectionModel().select(checkBox));

                lvLibraryStub.getItems().add(descriptionLibStub);
            }
            lvLibraryStub.refresh();
        }
    }

    private void updateLibraryStubs() {
        lvLibraryStub.getItems().clear();
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroLibraryStubNode());
        if (nodes.size() == 1) {
            EnviroLibraryStubNode enviroLibraryStubNode = (EnviroLibraryStubNode) nodes.get(0);
            for (String name : enviroLibraryStubNode.getLibraryNames()) {
                Label descriptionLibStub = new Label(name);
//                CheckBox checkBox = new CheckBox(name);
//                checkBox.setSelected(true);
//                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> lvLibraryStub.getSelectionModel().select(checkBox));
                lvLibraryStub.getItems().add(descriptionLibStub);
            }
        } else if (nodes.size() == 0) {
            logger.debug("There are no EnviroLibraryStubNode");
        } else {
            logger.debug("There are more than one EnviroLibraryStubNode");
        }

    }
//    public void update(Environment env) {
//        logger.debug("Update Choose UUT");
//
//        if (Environment.WindowState.isIsSearchListNodeUpdated() /*there is no change*/
//                || lvUnitNames.getItems().isEmpty() /*there is no item*/) {
//            // Delete all existing source code files in the unit names section
//            logger.debug("Delete the existing unit names");
//            clear();
//
//            // Add source code files to unit names section
//            logger.debug("Add new unit names to unit names section");
//            List<IEnvironmentNode> searchListNodes = EnvironmentSearch.searchNode(env.getRoot(), new EnviroSearchListNode());
//
//            if (searchListNodes.size() >= 1) {
//                for (IEnvironmentNode searchListNode : searchListNodes)
//                    if (searchListNode instanceof EnviroSearchListNode) {
//                        EnviroSearchListNode cast = (EnviroSearchListNode) searchListNode;
//
//                        if (new File(cast.getSearchList()).exists()) {
//                            // Analyze the project
//                            ProjectParser projectParser = new ProjectParser(new File(cast.getSearchList()));
//                            projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
//                            projectParser.setFuncCallDependencyGeneration_enabled(false);
//                            projectParser.setExpandTreeuptoMethodLevel_enabled(true);
//                            projectParser.setParentReconstructor_enabled(false);
//                            projectParser.setGenerateSetterandGetter_enabled(false);
//                            projectParser.setGlobalVarDependencyGeneration_enabled(false);
//                            IProjectNode projectRootNode = projectParser.getRootTree();
//
//                            // just for testing
//                            //ToString treeDisplayer = new DependencyTreeDisplayer(projectRootNode);
//                            //uiLogger.logToTerminal(treeDisplayer.getTreeInString());
//
//                            // Find all source code files and headers in the given source list, then disolay all to the unit names section
//                            List<INode> units = Search.searchNodes(projectRootNode, new SourcecodeFileNodeCondition());
//                            for (INode unit : units)
//                                if (unit instanceof CFileNode || unit instanceof CppFileNode) {
//                                    lvUnitNames.getItems().add(new UnitNamesPath((SourcecodeFileNode) unit));
//                                } else {
//                                    logger.debug("Do not add " + unit.getAbsolutePath() + " to GUI");
//                                }
//
//                            // Save the project root
//                            env.getProjectRoots().add(projectRootNode);
//                        } else {
//                            logger.debug(cast.getSearchList() + " does not exist!");
//                        }
//                    }
//            } else if (searchListNodes.size() == 0) {
//                logger.debug("There is no search list. Add at least once!");
//            }
//        } else {
//            logger.debug("There is no update on Choose UUTs window");
//        }
//
//        // update the library stubs
//        lvLibraryStub.getItems().clear();
//        EnvironmentRootNode root = Environment.getRoot();
//        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroLibraryStubNode());
//        if (nodes.size() == 1) {
//            EnviroLibraryStubNode enviroLibraryStubNode = (EnviroLibraryStubNode) nodes.get(0);
//            for (String name : enviroLibraryStubNode.getLibraryNames()) {
//                CheckBox checkBox = new CheckBox(name);
//                checkBox.setSelected(true);
//                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> lvLibraryStub.getSelectionModel().select(checkBox));
//                lvLibraryStub.getItems().add(checkBox);
//            }
//        } else if (nodes.size() == 0){
//            logger.debug("There are no EnviroLibraryStubNode");
//        } else {
//            logger.debug("There are more than one EnviroLibraryStubNode");
//        }
//
//        validate();
//    }

    public void clear() {
        lvUnitNames.getItems().clear();
        ttvUnitsUnderTest.getRoot().getChildren().clear();
        tfUnitNamesPath.setText("");
        validate();
    }

    public void addUUT() {
        if (currentUnitSelected != null) {
            if (currentUnitSelected.isChoosed()) {
                return;
            } else {
                logger.debug("add unit under test: " + currentUnitSelected.getAbsolutePath());
                UnitUnderTestNode unitUnderTestNode = new UnitUnderTestNode(currentUnitSelected);

//                logger.debug("Load dependencies of " + currentUnitSelected.getAbsolutePath());
//                unitUnderTestNode.loadDependencies(unitUnderTestNode.getSourcecodeFileNode());

                root.getChildren().add(new ChooseUUTTreeItem(unitUnderTestNode));
                currentUnitSelected.setChoosed(true);

                // TODO: Make the selected unit disabled

                // refresh the lvUitNames to display arrow of chosen unit
                lvUnitNames.refresh();
//                updateStubDependencies();
                validate();
            }
        }
    }

    @FXML
    public void chooseAll() {
        if (lvUnitNames.getItems().size() == 0)
            return;

        for (UnitNamesPath namesPath : lvUnitNames.getItems()) {
            if (!namesPath.isChoosed()) {
                logger.debug("add unit under test: " + namesPath.getAbsolutePath());
                UnitUnderTestNode unitUnderTestNode = new UnitUnderTestNode(namesPath);

                logger.debug("Load dependencies of " + namesPath.getAbsolutePath());
                unitUnderTestNode.loadDependencies(unitUnderTestNode.getSourcecodeFileNode());

                root.getChildren().add(new ChooseUUTTreeItem(unitUnderTestNode));
                namesPath.setChoosed(true);
            }
        }

        // refresh the lvUitNames to display arrow of chosen unit
        lvUnitNames.refresh();
//        updateStubDependencies();
        validate();
    }

    @FXML
    public void chooseNone() {
        ttvUnitsUnderTest.getSelectionModel().clearSelection();
        root.getChildren().clear();
        for (UnitNamesPath namesPath : lvUnitNames.getItems()) {
            namesPath.setChoosed(false);
        }

        // refresh the lvUitNames to display arrow of chosen unit
        lvUnitNames.refresh();
//        updateStubDependencies();
        validate();
    }

    public void removeUUT() {
        if (currentUUTSelected == null) {
            return;
        } else if (currentUUTSelected.getValue() instanceof UnitUnderTestNode) {
            ((UnitUnderTestNode) currentUUTSelected.getValue()).getUnitNamesPath().setChoosed(false);
            root.getChildren().remove(currentUUTSelected);
            ttvUnitsUnderTest.getSelectionModel().clearSelection();
            logger.debug(((UnitUnderTestNode) currentUUTSelected.getValue()).getAbsolutePath() + " is removed");
            currentUUTSelected = null;

            // refresh the lvUitNames to display arrow of chosen unit
            lvUnitNames.refresh();

//        updateStubDependencies();
            validate();
        }
    }

    @Override
    public void save() {
        // Step: Remove all stub nodes
        EnvironmentRootNode enviromentRootNode = Environment.getInstance().getEnvironmentRootNode();

        List<IEnvironmentNode> uutNodes = EnvironmentSearch.searchNode(enviromentRootNode, new EnviroUUTNode());
        enviromentRootNode.getChildren().removeAll(uutNodes);

        List<IEnvironmentNode> stubByFunctionNodes = EnvironmentSearch.searchNode(enviromentRootNode, new EnviroSBFNode());
        enviromentRootNode.getChildren().removeAll(stubByFunctionNodes);

        List<IEnvironmentNode> donotStubNodes = EnvironmentSearch.searchNode(enviromentRootNode, new EnviroDontStubNode());
        enviromentRootNode.getChildren().removeAll(donotStubNodes);

        List<IEnvironmentNode> ignoreNodes = EnvironmentSearch.searchNode(enviromentRootNode, new EnviroIgnoreNode());
        enviromentRootNode.getChildren().removeAll(ignoreNodes);

        removeAllStubByImplementationNodeInEnvironment(enviromentRootNode);

        // Step: save
        ChooseUUTTreeItem rootTree = (ChooseUUTTreeItem) ttvUnitsUnderTest.getRoot();
        for (TreeItem<AbstractUnitNode> child : rootTree.getChildren()) {
            saveIntoEnvironment(child.getValue(), enviromentRootNode, ttvUnitsUnderTest);
        }

        saveLibraryStubsToEnvironment();
        logger.debug("Environment configuration: \n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
        validate();

        if (!isValid()) {
            UIController.showErrorDialog("UUTs and Stubs configuration is required", "Error", "Choose UUTs & Stubs");
        }
    }

    private void saveLibraryStubsToEnvironment() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroLibraryStubNode());
        root.getChildren().removeAll(nodes);

        List<String> libraryNames = new ArrayList<>();

        if (lvLibraryStub.getItems().size() > 0) {
            for (Label item : lvLibraryStub.getItems()) {
                libraryNames.add(item.getText());
            }
            EnviroLibraryStubNode enviroLibraryStubNode = new EnviroLibraryStubNode();
            enviroLibraryStubNode.setLibraryNames(libraryNames);
            root.getChildren().add(enviroLibraryStubNode);
        }
    }

    private void saveIntoEnvironment(AbstractUnitNode treeNode, EnvironmentRootNode enviromentRootNode, TreeTableView<AbstractUnitNode> ttvUnitsUnderTest) {
        // Step: Save stub nodes in tree into environment script

        if (treeNode instanceof UnitUnderTestNode) {
            UnitUnderTestNode cast = (UnitUnderTestNode) treeNode;
            String stubType = cast.getStubType();
            String path = ((UnitUnderTestNode) treeNode).getAbsolutePath();
            INode unit = cast.getSourcecodeFileNode();

            if (stubType != null)
                switch (stubType) {
                    case UnitUnderTestNode.SBF: {
                        EnviroSBFNode newNode = new EnviroSBFNode();
                        newNode.setName(path);
                        newNode.setUnit(unit);
                        newNode.setParent(enviromentRootNode);
                        enviromentRootNode.addChild(newNode);
                        break;
                    }
                    case UnitUnderTestNode.DONT_STUB: {
                        EnviroDontStubNode newNode = new EnviroDontStubNode();
                        newNode.setName(path);
                        newNode.setUnit(unit);
                        newNode.setParent(enviromentRootNode);
                        enviromentRootNode.addChild(newNode);
                        break;
                    }
                    case UnitUnderTestNode.IGNORE: {
                        EnviroIgnoreNode newNode = new EnviroIgnoreNode();
                        newNode.setName(path);
                        newNode.setUnit(unit);
                        newNode.setParent(enviromentRootNode);
                        enviromentRootNode.addChild(newNode);
                        break;
                    }
                    case UnitUnderTestNode.UUT: {
                        EnviroUUTNode newNode = new EnviroUUTNode();
                        newNode.setName(path);
                        newNode.setUnit(unit);
                        newNode.setParent(enviromentRootNode);
                        enviromentRootNode.addChild(newNode);
                        break;
                    }
                    default: {
                        logger.error("Does not support " + treeNode.getStubType());
                        break;
                    }
                }

        } else if (treeNode instanceof DependencyNode) {
            DependencyNode cast = (DependencyNode) treeNode;
            String type = cast.getType();
            INode unit = cast.getSourcecodeFileNode();
            String path = unit.getAbsolutePath();

            if (type != null)
                switch (type) {
                    case DependencyNode.DONT_STUB: {
                        EnviroDontStubNode newNode = new EnviroDontStubNode();
                        newNode.setName(path);
                        newNode.setUnit(unit);
                        newNode.setParent(enviromentRootNode);
                        enviromentRootNode.addChild(newNode);
                        break;
                    }
                    case DependencyNode.STUB_BY_IMPLEMENTATION: {
                        EnviroStubNode newNode = new EnviroStubNode();
                        newNode.setStub(path);
                        newNode.setParent(enviromentRootNode);
                        enviromentRootNode.addChild(newNode);
                        break;
                    }
                    case DependencyNode.STUB_BY_PROTOTYPE: {
                        // no thing to do
                        // By default, all nodes are stubbed by prototype
                        break;
                    }
                    default: {
                        logger.error("Does not support " + treeNode.getStubType());
                        break;

                    }
                }
        } else {
            logger.error(treeNode.getStubType() + " does not support");
        }

        // recursive call
        for (AbstractUnitNode child : treeNode.getChildren()) {
            saveIntoEnvironment(child, enviromentRootNode, ttvUnitsUnderTest);
        }
    }

    private void removeAllStubByImplementationNodeInEnvironment(EnvironmentRootNode root) {
        List<IEnvironmentNode> searchNodes = EnvironmentSearch.searchNode(root, new EnviroStubNode());

        for (IEnvironmentNode searchNode : searchNodes)
            if (searchNode instanceof EnviroStubNode) {
                EnviroStubNode cast = (EnviroStubNode) searchNode;

                if (cast.getType() != null && cast.getName() == null) {
                    // ignore. This node is not stubbed by implementation.

                } else if (cast.getType() == null && cast.getName() != null) {
                    // this node is stubbed by implementation
                    root.getChildren().remove(searchNode);
                } else {
                    logger.error("Error on handling " + cast.getClass());
                }
            }
    }

    // update dependencies stub catagory
//    private void updateStubDependencies() {
//        switch (stubDependenciesType) {
//            case StubDependenciesType.ALL:
//                makeAllStubByPrototype();
//                break;
//            case StubDependenciesType.NONE:
//                makeAllDontStub();
//                break;
//            default:
//                break;
//        }
//    }
    public void stubAllasUUT() {
//        stubDependenciesType = StubDependenciesType.ALL;
        for (TreeItem<AbstractUnitNode> item : ttvUnitsUnderTest.getRoot().getChildren()) {
            ((UnitUnderTestNode) item.getValue()).setStubType(UnitUnderTestNode.UUT);
        }

        ttvUnitsUnderTest.refresh();
        validate();
    }

    public void donotStubAll() {
        for (TreeItem<AbstractUnitNode> item : ttvUnitsUnderTest.getRoot().getChildren()) {
            ((UnitUnderTestNode) item.getValue()).setStubType(UnitUnderTestNode.DONT_STUB);
        }

        ttvUnitsUnderTest.refresh();
        validate();
    }

//    public void ignoreAll() {
////        stubDependenciesType = StubDependenciesType.NONE;
//        for (TreeItem<AbstractUnitNode> item : ttvUnitsUnderTest.getRoot().getChildren()) {
//            ((UnitUnderTestNode) item.getValue()).setStubType(UnitUnderTestNode.IGNORE);
//        }
//
//        ttvUnitsUnderTest.refresh();
//        validate();
//    }

    public void stubAllasSBF() {
//        stubDependenciesType = StubDependenciesType.CUSTOM;
        for (TreeItem<AbstractUnitNode> item : ttvUnitsUnderTest.getRoot().getChildren()) {
            ((UnitUnderTestNode) item.getValue()).setStubType(UnitUnderTestNode.SBF);
        }

        ttvUnitsUnderTest.refresh();
        validate();
    }

    public void addLibraryStub() {
        Stage popUpWindow = PopUpWindowController.getWindow(PopUpWindowController.LIBRARY_STUB, lvLibraryStub);

        // block the environment window
        assert popUpWindow != null;
        popUpWindow.initModality(Modality.WINDOW_MODAL);
        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
        popUpWindow.show();
    }

//   public void addAdditionalStub() {
//        Stage popUpWindow = PopUpWindowController.getWindow(PopUpWindowController.ADDITIONAL_STUB, lvAdditionalStub);
//
//        // block the environment window
//        assert popUpWindow != null;
//        popUpWindow.initModality(Modality.WINDOW_MODAL);
//        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
//        popUpWindow.show();
//    }

//   public void addSuppressedStub() {
//        Stage popUpWindow = PopUpWindowController.getWindow(PopUpWindowController.SUPPRESSED_STUB, lvSuppressedStub);
//
//        // block the environment window
//        assert popUpWindow != null;
//        popUpWindow.initModality(Modality.WINDOW_MODAL);
//        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
//        popUpWindow.show();
//    }
//    public void addSuppressedTestableFunction() {
//        Stage popUpWindow = PopUpWindowController.getWindow(PopUpWindowController.SUPPRESSED_TESTABLE_FUNCTIONS, lvSuppressedTestableFunction);
//
//        // block the environment window
//        assert popUpWindow != null;
//        popUpWindow.initModality(Modality.WINDOW_MODAL);
//        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
//        popUpWindow.show();
//    }
//    public void addNotSupportedType() {
//        Stage popUpWindow = PopUpWindowController.getWindow(PopUpWindowController.NOT_SUPPORTED_TYPES, lvNotSupportedType);
//
//        // block the environment window
//        assert popUpWindow != null;
//        popUpWindow.initModality(Modality.WINDOW_MODAL);
//        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
//        popUpWindow.show();
//    }

    public void deleteLibraryStub() {
        if (currentLibraryStubSelected != null) {
            lvLibraryStub.getItems().remove(currentLibraryStubSelected);
            currentLibraryStubSelected = null;
            lvLibraryStub.getSelectionModel().clearSelection();
        }
    }

//    public void deleteAdditionalStub() {
//        if (currentAdditionalStubSelected != null) {
//            lvAdditionalStub.getItems().remove(currentAdditionalStubSelected);
//            currentAdditionalStubSelected = null;
//            lvAdditionalStub.getSelectionModel().clearSelection();
//        }
//    }
//    public void deleteSuppressedStub() {
//        if (currentSuppressedStubSelected != null) {
//            lvSuppressedStub.getItems().remove(currentSuppressedStubSelected);
//            currentSuppressedStubSelected = null;
//            lvSuppressedStub.getSelectionModel().clearSelection();
//        }
//    }
//
//    public void deleteSuppressedTestableFunction() {
//        if (currentSuppressedTestableFunctionSelected != null) {
//            lvSuppressedTestableFunction.getItems().remove(currentSuppressedTestableFunctionSelected);
//            currentSuppressedTestableFunctionSelected = null;
//            lvSuppressedTestableFunction.getSelectionModel().clearSelection();
//        }
//    }
//
//    public void deleteNotSupportedType() {
//        if (currentNotSupportedTypeSelected != null) {
//            lvNotSupportedType.getItems().remove(currentNotSupportedTypeSelected);
//            currentNotSupportedTypeSelected = null;
//            lvNotSupportedType.getSelectionModel().clearSelection();
//        }
//    }

    private boolean validateTTVUnitsUnderTest() {
        boolean atLeastUutOrSbfNode = false;
        for (TreeItem<AbstractUnitNode> child : ttvUnitsUnderTest.getRoot().getChildren())
            if (child.getValue().getStubType().equals(UnitUnderTestNode.UUT)
                    || child.getValue().getStubType().equals(UnitUnderTestNode.SBF)) {
                atLeastUutOrSbfNode = true;
                break;
            }

        if (ttvUnitsUnderTest.getRoot().getChildren().size() == 0 ||
                !atLeastUutOrSbfNode /*do not add any sbf or uut*/
        ) {
            ttvUnitsUnderTest.setStyle("-fx-border-color: red");
            return false;
        } else {
            ttvUnitsUnderTest.setStyle("-fx-border-color: black; -fx-border-width: 0.1");
            return true;
        }
    }

    public void validate() {
        setValid(validateTTVUnitsUnderTest());
        highlightInvalidStep();
    }

//    private static class StubDependenciesType {
//        public final static String ALL = "ALL";
//        public final static String NONE = "NONE";
//        public final static String CUSTOM = "CUSTOM";
//    }
}
