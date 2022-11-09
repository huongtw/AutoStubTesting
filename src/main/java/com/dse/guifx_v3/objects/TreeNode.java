package com.dse.guifx_v3.objects;

import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.parser.object.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import com.dse.logger.AkaLogger;

import java.util.List;

public class TreeNode extends TreeItem<String> {
    final static UILogger uiLogger = UILogger.getUiLogger();
    final static AkaLogger logger = AkaLogger.get(TreeNode.class);
    private INode item;

    public TreeNode() {
    }

    public TreeNode(INode item) {
        super();

        this.item = item;

        // set name of node
        setValue(createNameOfNode(this.item));

        // set Icon
        setGraphic(Factory.getIcon(this.item));

        // If it's not a leaf node, treat it as a parent node
        if (canBeExpanded(this.getItem())) {
            getChildren().add(new TreeNode());
        }
        expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //System.out.println("newValue = " + newValue);
                BooleanProperty bb = (BooleanProperty) observable;
                // System.out.println("bb.getBean() = " + bb.getBean());
                TreeNode node = (TreeNode) bb.getBean();

                if (node.canBeExpanded(node.getItem())) {
                    if (isExpanded()) {
//                        uiLogger.log("Expanding node " + node.getItem().getAbsolutePath());
                        logger.debug("Expanding node " + node.getItem().getAbsolutePath());
                        node.loadChildren(node.getItem());
                    } else {
//                        uiLogger.log("Collapsing node " + node.getItem().getAbsolutePath());
                        logger.debug("Collapsing node " + node.getItem().getAbsolutePath());
                        node.getChildren().clear();
                        node.getChildren().add(new TreeNode());
                    }
                }
            }
        });

    }

    public INode getItem() {
        return this.item;
    }

//    public boolean isPlaceHolderNode() {// node cha chưa được load các node con
//        TreeNode node = (TreeNode) this.getChildren().get(0);
//        return getChildren().size() == 1 && ! node.hasItem();
//    }

    /**
     * Check if a node can be expanded in the project structure
     *
     * @return
     */
    private boolean canBeExpanded(INode node) {
        if (node != null) {
            if (this.getItem() instanceof ProjectNode)
                return true;
            else if (this.getItem() instanceof NamespaceNode || this.getItem() instanceof StructNode
                    || this.getItem() instanceof ClassNode
                    || this.getItem() instanceof ISourcecodeFileNode || this.getItem() instanceof FolderNode) {
                for (INode child : this.getItem().getChildren()) {
                    if (shouldDisplayInTree(child)) {
                        return true;
                    }
                }
                return false; // there is no child that should be displayed, hence can not be expanded
            } else
                return false;
        } else
            return false;
    }

    /**
     * Load Children TreeNodes
     */
    private void loadChildren(INode node) {
        if (node != null && canBeExpanded(node)) {
            this.getChildren().clear();

            List<Node> itemChildren = this.item.getChildren();
            for (INode child : itemChildren)
                if (shouldDisplayInTree(child)) {
                    this.getChildren().add(new TreeNode(child));
                }
        }
    }

    /**
     * Check if a node should be displayed in tree
     *
     * @return
     */
    private boolean shouldDisplayInTree(INode node) {
        if (node != null) {
            //                     || node instanceof ExternalVariableNode
            //                    || node instanceof UnknowObjectNode)
            return node instanceof ProjectNode || node instanceof StructureNode || node instanceof NamespaceNode
                    || node instanceof ICommonFunctionNode || node instanceof FolderNode || node instanceof ISourcecodeFileNode;
        } else
            return false;
    }

    private String createNameOfNode(INode node) {
//        if (node instanceof NamespaceNode)
//            return "namespace " + node.getName();
//        else if (node instanceof ClassNode)
//            return "class " + node.getName();
//        else if (node instanceof StructNode)
//            return "struct " + node.getName();
//        else if (node instanceof EnumNode)
//            return "enum " + node.getName();
//        else if (node instanceof ICommonFunctionNode)
//            return node.getName() + " " + ((ICommonFunctionNode) node).getReturnType();
//        else if (node instanceof  ExternalVariableNode)
//            return node.getName() + " " + ((ExternalVariableNode) node).getRawType();
//        else
        // comments above are okay, but I prefer this style.
            return "["+node.getClass().getSimpleName()+"] " + node.getName();
    }

//    // ! Chưa kiểm nghiệm
//    public int compare(INode i1, INode i2) {
//
//        // Các thư mục sẽ được hiển thị trước các tập tin
//        if (i1 instanceof IHasFileNode && i2 instanceof IHasFileNode) {
//            boolean d1 = ((IHasFileNode) i1).getFile().isDirectory(), d2 = ((IHasFileNode) i2)
//                    .getFile().isDirectory();
//            return d1 ^ d2 ? d1 ? -1 : 1 : 0;
//        }
//
//        return 0;
//    }


//    public void sortChild(Comparator<TreeNode> c) {
//        if (getChildren() != null)
//            super.sortChild(c);
//    }

}
