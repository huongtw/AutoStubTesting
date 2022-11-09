package com.dse.testdata.gen.module;

import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;

public class SimpleTreeDisplayer {

    String treeInString = "";

//    public static void main(String[] args) throws Exception {
//        ProjectParser parser = new ProjectParser(new File(Paths.DATA_GEN_TEST));
//
//        String name = "test(int,int*,int[],int[2],char,char*,char[],char[10],SinhVien*,SinhVien,SinhVien[])";
//        IFunctionNode function = (IFunctionNode) Search
//                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), name).get(0);
//        FunctionConfig functionConfig = new FunctionConfig();
//        functionConfig.setCharacterBound(new Bound(32, 36));
//        functionConfig.setIntegerBound(new Bound(45, 50));
//        functionConfig.setSizeOfArray(3);
//        function.setFunctionConfig(functionConfig);
//        /*
//         *
//         */
//        Map<String, Object> staticSolution = new HashMap<>();
//        staticSolution.put("sv.age", 1);
//        staticSolution.put("sv.name[2]", 97);
//
//        IDataTreeGeneration dataTreeGen = new DataTreeGeneration();
//        dataTreeGen.setFunctionNode(function);
//        dataTreeGen.setStaticSolution(staticSolution);
//        dataTreeGen.getInputforGoogleTest();
//
//        System.out.println(dataTreeGen.getFunctionCall());
//    }

    private void displayTree(IDataNode node, int level){
        if (node == null) {

        } else {
            if (node instanceof RootDataNode)
                treeInString += genTab(level) + "[" + node.getName() + "]" + "\n";
            else if (node instanceof SubprogramNode)
                treeInString += genTab(level) + "[" + node.getClass().getSimpleName() + "] real name: " + node.getName() + "\n";
            else {
                DataNode n = (DataNode) node;

                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] real name: " + n.getName() + "\n";
                treeInString += genTab(level + 1) + "virtual name: " + n.getVituralName() + "\n";

                if (n instanceof NormalDataNode)
                    treeInString += genTab(level + 1) + "value: " + ((NormalDataNode) n).getValue() + "\n";
                else if (n instanceof EnumDataNode)
                    treeInString += genTab(level + 1) + "value: " + ((EnumDataNode) n).getValue() + "\n";
                else if (n instanceof PointerDataNode)
                    treeInString += genTab(level + 1) + "size (-1 = NULL): " + ((PointerDataNode) n).getAllocatedSize()
                            + "\n";
                else if (n instanceof OneDimensionDataNode)
                    treeInString += genTab(level + 1) + "size: " + ((OneDimensionDataNode) n).getSize() + "\n";

                if (n.getParent() != null)
                    treeInString += genTab(level + 1) + "virtual name of parent: " + n.getParent().getVituralName() + "\n";

                if (n instanceof ValueDataNode) {
                    treeInString += genTab(level + 1) + "is external variable : " + ((ValueDataNode) n).isExternel() + "\n";
                    treeInString += genTab(level + 1) + "type: " + ((ValueDataNode) n).getRawType() + "\n";
                    if (((ValueDataNode) n).getCorrespondingVar() != null)
                        treeInString += genTab(level + 1) + "corresponding variable node: "
                                + ((ValueDataNode) n).getCorrespondingVar().getAbsolutePath() + "(" + ((ValueDataNode) n).getCorrespondingVar().getClass().getSimpleName() + ")" + "\n";
                    try {
                        treeInString += genTab(level + 1) + "script: " + n.getInputForGoogleTest(false).replace("\n", "") + "\n";
                    } catch (Exception e) {
                        treeInString += genTab(level + 1) + "script: " + "" + "\n";
                    }
                    treeInString += genTab(level + 1) + "is array element: " + ((ValueDataNode) n).isArrayElement() + "\n";
                    treeInString += genTab(level + 1) + "is passing variable: " + ((ValueDataNode) n).isPassingVariable() + "\n";
                    treeInString += genTab(level + 1) + "is attribute: " + ((ValueDataNode) n).isAttribute() + "\n";
                    treeInString += genTab(level + 1) + "is in constructor: " + ((ValueDataNode) n).isInConstructor() + "\n";
                }
                // treeInString += genTab(level + 1) + "dot getter:" + n.getDotGetterInStr() +
                // "\n";
                // treeInString += genTab(level + 1) + "dot setter:" + n.getDotSetterInStr("aa")
                // + "\n";

                // if (n.getGetterInStr().length() > 0)
                // treeInString += genTab(level + 1) + "getter:" + n.getGetterInStr() + "\n";
                // if (n.getSetterInStr("aa").length() > 0)
                // treeInString += genTab(level + 1) + "setter:" + n.getSetterInStr("aa") +
                // "\n";
            }

            if (node.getChildren() != null) {
                for (IDataNode child : node.getChildren()) {
                    displayTree(child, ++level);
                    level--;
                }
            }
        }
    }

    protected String genTab(int level) {
        StringBuilder tab = new StringBuilder();
        for (int i = 0; i < level; i++)
            tab.append("     ");
        return tab.toString();
    }

    public String toString(IDataNode n) {
        displayTree(n, 0);
        return treeInString;
    }
}
