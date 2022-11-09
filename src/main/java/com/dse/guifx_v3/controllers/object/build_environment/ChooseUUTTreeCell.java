package com.dse.guifx_v3.controllers.object.build_environment;

import com.dse.exception.OpenFileException;
import com.dse.guifx_v3.controllers.object.unit_node.AbstractUnitNode;
import com.dse.guifx_v3.controllers.object.unit_node.DependencyNode;
import com.dse.guifx_v3.controllers.object.unit_node.UnitUnderTestNode;
import com.dse.guifx_v3.helps.UIController;
import com.dse.util.Utils;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import com.dse.logger.AkaLogger;

public class ChooseUUTTreeCell extends TreeTableRow<AbstractUnitNode> {
    private final static AkaLogger logger = AkaLogger.get(ChooseUUTTreeCell.class);
    private AbstractUnitNode node = null;

    @Override
    public void updateItem(AbstractUnitNode item, boolean empty) {
        super.updateItem(item, empty);

        if (getTreeItem() != null) {
            node = getItem();
        }

        if (node != null) {
            setContextMenu(new ContextMenu());

            if (node instanceof  DependencyNode) {
                addContextmenuForDependencyNode(node);
            }

            if (node instanceof  UnitUnderTestNode)
                addContextmenuForUnitUnderTestNode(node);
        }
    }

    private void addContextmenuForDependencyNode(AbstractUnitNode node) {
        if (node instanceof DependencyNode) {
            MenuItem iStubByImplement = setStubByImplementationOnRightClick(node);
            MenuItem iStubByPrototype = setStubByPrototypeOnRightClick(node);
            MenuItem iDontStub = setDoNotStubOnRightClick(node);
            MenuItem iViewSourcecodeFile = setViewSourcecodeFileOnRightClick(node);

            if (((DependencyNode) node).isCanSetStubType()) {
                getContextMenu().getItems().addAll(iStubByImplement, iStubByPrototype, iDontStub);
            }
            getContextMenu().getItems().add(iViewSourcecodeFile);
        }
    }

    private void addContextmenuForUnitUnderTestNode(AbstractUnitNode node) {
        if (node instanceof UnitUnderTestNode) {
            MenuItem itemSBF = setSbfOnRightClick(node);
            MenuItem itemUUT = setUutOnRightClick(node);
            MenuItem itemIgnore = setIgnoreOnRightClick(node);
            MenuItem itemNotStub = setNotStubOnRightClick(node);
            MenuItem itemViewSourcecodeFile = setViewSourcecodeFileOnRightClick(node);
            getContextMenu().getItems().addAll(itemSBF, itemUUT, itemIgnore, itemNotStub, itemViewSourcecodeFile);
        }
    }

    private MenuItem setSbfOnRightClick(AbstractUnitNode node) {
        MenuItem itemSBF = new MenuItem(UnitUnderTestNode.SBF);
        itemSBF.setOnAction(event -> {
            if (node != null) {
                ((UnitUnderTestNode) node).setStubType(UnitUnderTestNode.SBF);
                getTreeTableView().refresh();
            }
        });
        return itemSBF;
    }

    private MenuItem setNotStubOnRightClick(AbstractUnitNode node) {
        MenuItem itemNotStub = new MenuItem(UnitUnderTestNode.DONT_STUB);
        itemNotStub.setOnAction(event -> {
            if (node != null) {
                ((UnitUnderTestNode) node).setStubType(UnitUnderTestNode.DONT_STUB);
                getTreeTableView().refresh();
            }
        });
        return itemNotStub;
    }

    private MenuItem setIgnoreOnRightClick(AbstractUnitNode node) {
        MenuItem itemIgnore = new MenuItem(UnitUnderTestNode.IGNORE);
        itemIgnore.setOnAction(event -> {
            if (node != null) {
                ((UnitUnderTestNode) node).setStubType(UnitUnderTestNode.IGNORE);
                getTreeTableView().refresh();
            }
        });
        return itemIgnore;
    }

    private MenuItem setUutOnRightClick(AbstractUnitNode node) {
        MenuItem itemUUT = new MenuItem(UnitUnderTestNode.UUT);
        itemUUT.setOnAction(event -> {
            if (node != null) {
                ((UnitUnderTestNode) node).setStubType(UnitUnderTestNode.UUT);
                getTreeTableView().refresh();
            }
        });
        return itemUUT;
    }

    private MenuItem setStubByImplementationOnRightClick(AbstractUnitNode node) {
        MenuItem iStubByImplement = new MenuItem(DependencyNode.STUB_BY_IMPLEMENTATION);
        iStubByImplement.setOnAction(event -> {
            if (node != null) {
                logger.debug("set STUB_BY_IMPLEMENTATION on " + ((DependencyNode) node).getSourcecodeFileNode().getAbsolutePath());
                ((DependencyNode) node).setType(DependencyNode.STUB_BY_IMPLEMENTATION);
                getTreeTableView().refresh();
            }
        });
        return iStubByImplement;
    }

    private MenuItem setStubByPrototypeOnRightClick(AbstractUnitNode node) {
        MenuItem iStubByPrototype = new MenuItem(DependencyNode.STUB_BY_PROTOTYPE);
        iStubByPrototype.setOnAction(event -> {
            if (node != null) {
                logger.debug("set STUB_BY_PROTOTYPE on " + ((DependencyNode) node).getSourcecodeFileNode().getAbsolutePath());
                ((DependencyNode) node).setType(DependencyNode.STUB_BY_PROTOTYPE);
                getTreeTableView().refresh();
            }
        });
        return iStubByPrototype;
    }

    private MenuItem setDoNotStubOnRightClick(AbstractUnitNode node) {
        MenuItem iDontStub = new MenuItem(DependencyNode.DONT_STUB);
        iDontStub.setOnAction(event -> {
            if (node != null) {
                logger.debug("set DONT_STUB on " + ((DependencyNode) node).getSourcecodeFileNode().getAbsolutePath());
                ((DependencyNode) node).setType(DependencyNode.DONT_STUB);
                getTreeTableView().refresh();
            }
        });
        return iDontStub;
    }

    private MenuItem setViewSourcecodeFileOnRightClick(AbstractUnitNode node) {
        MenuItem iViewSourcecodeFile = new MenuItem("View source code file on explorer");
        iViewSourcecodeFile.setOnAction(event -> {
            if (node != null) {
                if (node instanceof DependencyNode) {
                    String path = ((DependencyNode) node).getSourcecodeFileNode().getAbsolutePath();
                    try {
                        Utils.openFolderorFileOnExplorer(path);
                    } catch (OpenFileException e) {
                        UIController.showErrorDialog(e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    } catch (Exception e) {
                        UIController.showErrorDialog("Error code " + e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    }
                } else if (node instanceof UnitUnderTestNode) {
                    String path = ((UnitUnderTestNode) node).getSourcecodeFileNode().getAbsolutePath();
                    try {
                        Utils.openFolderorFileOnExplorer(path);
                    } catch (OpenFileException e) {
                        UIController.showErrorDialog(e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    } catch (Exception e) {
                        UIController.showErrorDialog("Error code " + e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    }
                }
            }
        });
        return iViewSourcecodeFile;
    }
}
