package auto_testcase_generation.cte.UI.CteClassificationTree;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand.*;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassificationNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.DataInsert.CteDataInsert;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CteViewManager {
    private CteView cteView;
    private CteSetParentCommand cteSetParentCommand = null;
    private CteDataInsert dataInsert = new CteDataInsert();
    private List<CteUiNode> chosenNodes = new ArrayList<>();
    private CteViewMode currentMode;
    private List<CteUiNode> freeTree = new ArrayList<>();
    private List<CteNode> waitingForPaste = new ArrayList<>();
    private List<CteNode> lastCopyList = new ArrayList<>();
    Stack<CteUndoableCommand> undoStack = new Stack<>();
    Stack<CteUndoableCommand> redoStack = new Stack<>();

    public CteViewManager()
    {
        currentMode = CteViewMode.FREE;

    }


    public CteSetParentCommand getCteSetParentCommand() {
        return cteSetParentCommand;
    }

    public void setCteSetParentCommand(CteSetParentCommand cteSetParentCommand) {
        this.cteSetParentCommand = cteSetParentCommand;
    }

    public void addChosenNode(CteUiNode newNode, boolean multipleSelect, boolean special)
    {
        if(!multipleSelect)
        {
            currentMode = CteViewMode.SINGLE_CHOOSE;
            boolean isChosen = newNode.isChoosing();
            int size = chosenNodes.size();
            removeAllChosenNodes();
            if(!(size == 1 && isChosen))
            {
                chosenNodes.add(newNode);
                newNode.chosenModeActivate();
                dataInsert.ViewDataInsert(newNode.getTestCase(), newNode);
            }
            else
            {
                if (special) {
                    chosenNodes.add(newNode);
                    newNode.chosenModeActivate();
                    dataInsert.ViewDataInsert(newNode.getTestCase(), newNode);
                } else {
                    dataInsert.unviewDataInsert();
                    currentMode = CteViewMode.FREE;
                }

            }
        }
        else
        {
            currentMode = CteViewMode.MULTI_CHOOSE;
            if(newNode.isChoosing())
            {
                if (!special) {
                    removeChosenNode(newNode);
                    dataInsert.unviewDataInsert();
                    if(chosenNodes.size() == 1)
                    {
                        currentMode = CteViewMode.SINGLE_CHOOSE;
                        dataInsert.ViewDataInsert(chosenNodes.get(0).getTestCase(), chosenNodes.get(0));
                    }
                    else if(chosenNodes.isEmpty()) currentMode = CteViewMode.FREE;
                }

            }
            else
            {
                chosenNodes.add(newNode);
                newNode.chosenModeActivate();
                if(chosenNodes.isEmpty()) dataInsert.ViewDataInsert(newNode.getTestCase(), newNode);
                else dataInsert.unviewDataInsert();
            }
        }
    }

    public void removeChosenNode(CteUiNode node)
    {
        chosenNodes.remove(node);
        node.chosenModeDeactivate();
    }

    public void removeAllChosenNodes()
    {
        for (CteUiNode chosenNode : chosenNodes) {
            chosenNode.chosenModeDeactivate();
        }
        dataInsert.unviewDataInsert();
        chosenNodes.clear();
    }

//    public void moveToWaitingForPaste(int index)
//    {
//        if(index < freeTree.size())
//        {
//            waitingForPaste.clear();
//            for(int i = index; i < freeTree.size(); i++)
//            {
//                waitingForPaste.add(freeTree.get(i));
//            }
//
//            for(int i = 0; i < waitingForPaste.size(); i++)
//            {
//                freeTree.remove(waitingForPaste.get(i));
//            }
//        }
//    }

    public void addToWaitingForPaste(List<CteNode> list)
    {
        waitingForPaste.clear();
        waitingForPaste.addAll(list);
    }

    private void addLastCopyList()
    {
        lastCopyList.clear();
        for(int i = 0; i < waitingForPaste.size(); i++)
        {
            lastCopyList.add(waitingForPaste.get(i));
        }
    }

    public void addToFreeTree(CteUiNode node)
    {
        freeTree.add(node);
    }

    public void chooseSubTree(CteUiNode node) {
        currentMode = CteViewMode.MULTI_CHOOSE;
        addSubTreeToChosenNode(node);
    }

    private void addSubTreeToChosenNode(CteUiNode node) {
        removeChosenNode(node);
        addChosenNode(node, true, true);
        for (CteUiNode child: node.getChildren()) {
            addSubTreeToChosenNode(child);
        }
    }

    public void executeCommand(ICteCommand command)
    {
        if(command instanceof CteUndoableCommand)
        {
            undoStack.push((CteUndoableCommand) command);
            if (undoStack.size() > 20)
            {
                if(undoStack.peek() instanceof CteCutCommand || undoStack.peek() instanceof CteDeleteCommand)
                {
                    undoStack.peek().destroy();
                }
                undoStack.remove(0);
            }
        }

        if(command instanceof CteCutCommand || command instanceof CteCopyCommand
                || command instanceof CteDeleteCommand)
        {
            if(chosenNodes.size() != 0) command.execute();
            else
            {
                deleteLastestInUndo((CteUndoableCommand) command);
            }

            if(command instanceof CteCopyCommand)
            {
                addLastCopyList();
            }
            else{
                redoStack.empty();
            }

            removeAllChosenNodes();
        }
        else
        {
            command.execute();
        }


        System.out.println("Executed the " + command.getClass());


    }

    public void undoCommand()
    {
        if(!undoStack.isEmpty())
        {
            CteUndoableCommand tmp = undoStack.pop();
            redoStack.push(tmp);
            tmp.undo();
        }
    }

    public void redoCommand()
    {
        if(!redoStack.isEmpty())
        {
            CteUndoableCommand tmp = redoStack.pop();
            tmp.execute();
            undoStack.push(tmp);
        }
    }

    public List<CteUiNode> getChosenNodes() {
        return chosenNodes;
    }

    public CteViewMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(CteViewMode currentMode) {
        this.currentMode = currentMode;
    }

    public CteDataInsert getDataInsert() {
        return dataInsert;
    }

    public List<CteUiNode> getFreeTree() {
        return freeTree;
    }

    public CteView getCteView() {
        return cteView;
    }

    public void setCteView(CteView cteView) {
        this.cteView = cteView;
    }

    public List<CteNode> getWaitingForPaste() {
        return waitingForPaste;
    }

    public void clearWaitingForPaste()
    {
        waitingForPaste.clear();
    }

    public boolean ValidSetParFunction(CteUiNode node){
        if(currentMode == CteViewMode.SINGLE_CHOOSE || currentMode == CteViewMode.FREE) return true;
        else if(currentMode == CteViewMode.MULTI_CHOOSE)
        {
            for(int i =0; i < chosenNodes.size(); i++)
            {
                if(isValidChild(node, chosenNodes.get(i)) == false) return false;
            }
            return true;
        }
        else return false;
    }

    private boolean isValidChild(CteUiNode parNode, CteUiNode childNode)
    {
        if(parNode instanceof CteClassificationNode)
        {
            if(childNode instanceof CteClassNode) return  true;
            else return false;
        }
        else
        {
            if(childNode instanceof CteClassificationNode || childNode instanceof CteCompositionNode){
                return true;
            }
            else return false;
        }
    }

    public void changeColorForSubTree(Color newColor, CteUiNode node)
    {
        node.changeDecoratedDetailColor(newColor);
        for(CteUiNode child:node.getChildren())
        {
            changeColorForSubTree(newColor , child);
        }
    }

    public void changeColorForNodes(Color newColor, boolean isForSubTree)
    {
        for(CteUiNode node:chosenNodes)
        {
            if(isForSubTree == false)
            {
                node.changeDecoratedDetailColor(newColor);
            }
            else
            {
                changeColorForSubTree(newColor, node);
            }
        }

        removeAllChosenNodes();
    }

    public List<CteNode> getLastCopyList() {
        return lastCopyList;
    }

    public void removeTreeAfterSetParent(CteUiNode removeNode)
    {
        if(freeTree.contains(removeNode))
        {
            removeNode.setBgColorForNodes(CteCoefficent.NodeBgColor.normalCor);
            freeTree.remove(removeNode);

        }
    }

    public boolean hasSetParentCommand()
    {
        if(cteSetParentCommand != null) return  true;
        return false;
    }

    public void deleteLastestInUndo(CteUndoableCommand command)
    {
        if(undoStack.contains(command)) undoStack.remove(command);
    }

    public void reloadDataInsert(CteUiNode node)
    {
        dataInsert.reload(node);
    }
}
