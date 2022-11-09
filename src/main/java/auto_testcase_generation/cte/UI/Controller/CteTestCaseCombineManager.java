package auto_testcase_generation.cte.UI.Controller;

import auto_testcase_generation.cte.UI.TestcaseTable.CteTestcaseElement;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.gen.module.type.PointerTypeInitiation;
import com.dse.testdata.object.*;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CteTestCaseCombineManager {
    private FunctionNode functionNode;
    private List<CteTestcaseElement> chosenTestCase;

    public CteTestCaseCombineManager()
    {}

    public CteTestCaseCombineManager(FunctionNode _fnode)
    {

        functionNode = _fnode;
        chosenTestCase = new ArrayList<>();
    }

    public TestCase exportTestCase(List<TestCase> testCaseList, String tcName)
    {
        TestCase result = new TestCase(functionNode, tcName);
        result.setCreationDateTime(LocalDateTime.now());
        result.initGlobalInputExpOutputMap();
        result.initParameterExpectedOutputs();
        if(testCaseList.size() != 0) {
            for (int i = 0; i < testCaseList.size(); i++) {
                TestCase tmpTC = testCaseList.get(i);
                try {
                    combineTestCase(result, tmpTC);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public void combineTestCase(TestCase mainTestCase, TestCase auxTestCase) throws Exception {
        GlobalRootDataNode mainGlobNode = findGlobRootDataNode(mainTestCase.getRootDataNode());
        GlobalRootDataNode auxGlobNode = findGlobRootDataNode(auxTestCase.getRootDataNode());

        combineGlobalData(mainGlobNode, auxGlobNode);

        SubprogramNode mainSubNode = findSubProgramNode(mainTestCase.getRootDataNode());
        SubprogramNode auxSubNode = findSubProgramNode(auxTestCase.getRootDataNode());
        combineData(mainSubNode, auxSubNode);
        Map mainTmpMap = mainSubNode.getInputToExpectedOutputMap();
        Map auxTmpMap = auxSubNode.getInputToExpectedOutputMap();
        for(int i = 0;i < auxSubNode.getChildren().size(); i++)
        {
            if(auxTmpMap.get(auxSubNode.getChildren().get(i)) != null)
            {
                IDataNode auxExDataNode = (IDataNode) auxTmpMap.get(auxSubNode.getChildren().get(i));
                IDataNode mainExDataNode = (IDataNode) mainTmpMap.get(mainSubNode.getChildren().get(i));
                combineDataMainCode(mainExDataNode,auxExDataNode );
            }
        }

    }

    private void combineGlobalData(GlobalRootDataNode mainGlobal, GlobalRootDataNode auxGlobal) throws Exception {
        combineData(mainGlobal, auxGlobal);
        Map mainTmpMap = mainGlobal.getGlobalInputExpOutputMap();
        Map auxTmpMap = auxGlobal.getGlobalInputExpOutputMap();
        for(int i = 0;i < auxGlobal.getChildren().size(); i++)
        {
            if(auxTmpMap.get(auxGlobal.getChildren().get(i)) != null)
            {
                IDataNode auxExDataNode = (IDataNode) auxTmpMap.get(auxGlobal.getChildren().get(i));
                IDataNode mainExDataNode = (IDataNode) mainTmpMap.get(mainGlobal.getChildren().get(i));
                combineDataMainCode(mainExDataNode,auxExDataNode );
            }
        }
    }


    private void combineData(IDataNode mainNode, IDataNode auxNode) throws Exception {
        if (mainNode != null && auxNode != null) {
            if(auxNode instanceof SubprogramNode || auxNode instanceof GlobalRootDataNode) {
                for (int i = 0; i < mainNode.getChildren().size(); i++) {
                    IDataNode mainTmpNode = mainNode.getChildren().get(i);
                    IDataNode auxTmpNode = auxNode.getChildren().get(i);
                    combineDataMainCode(mainTmpNode, auxTmpNode);
                }
            }
            else
            {
                combineDataMainCode(mainNode, auxNode);
            }
        }
    }

    public void combineDataMainCode(IDataNode mainTmpNode, IDataNode auxTmpNode ) throws Exception {
        if (auxTmpNode instanceof NormalNumberDataNode) {

            NormalNumberDataNode auxDataNode = (NormalNumberDataNode) auxTmpNode;
            NormalNumberDataNode mainDataNode = (NormalNumberDataNode) mainTmpNode;


            if (auxDataNode.getValue() != null) {
                mainDataNode.setValue(auxDataNode.getValue());
            }

            if (auxDataNode.getAssertMethod() != null) {
                mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
            }

            if(auxDataNode.getVituralName() != null)
            {
                mainDataNode.setVirtualName();
            }

        } else if (auxTmpNode instanceof NormalCharacterDataNode) {
            NormalCharacterDataNode auxDataNode = (NormalCharacterDataNode) auxTmpNode;
            NormalCharacterDataNode mainDataNode = (NormalCharacterDataNode) mainTmpNode;


            if (auxDataNode.getValue() != null) {
                mainDataNode.setValue(auxDataNode.getValue());
            }

            if (auxDataNode.getAssertMethod() != null) {
                mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
            }
        }


        else if(auxTmpNode instanceof OneDimensionDataNode)
        {
            OneDimensionDataNode auxDataNode = (OneDimensionDataNode) auxTmpNode;
            OneDimensionDataNode mainDataNode = (OneDimensionDataNode) mainTmpNode;
            if(auxDataNode.getSize() > 0)
            {
                mainDataNode.setSize(auxDataNode.getSize());
                mainDataNode.setSizeIsSet(true);
                if(auxDataNode.getSize() <= 20) {
                    TreeExpander expander = new TreeExpander();
                    try {
                        expander.expandTree(mainDataNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (auxDataNode.getAssertMethod() != null) {
                    mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
                }

                if(auxDataNode.getChildren().size() >0)
                {
                    for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                    {
                        IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                        if(auxDataNode.getSize() > 20) {
                            String name = auxTmpDataNode.getName();
                            int num = auxTmpDataNode.getName().indexOf('[');
                            String input = auxTmpDataNode.getName().substring(num);
                            List<String> expanded = new TreeExpander()
                                    .expandArrayItemByIndex( mainDataNode, input);
                            IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                            combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                        }
                        IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                        combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                    }
                }
            }
        }
        else if(auxTmpNode instanceof MultipleDimensionDataNode)
        {
            MultipleDimensionDataNode auxDataNode = (MultipleDimensionDataNode) auxTmpNode;
            MultipleDimensionDataNode mainDataNode = (MultipleDimensionDataNode) mainTmpNode;
            int size = -1;
            if(auxDataNode.isSetSize()) {
                mainDataNode.setSizes(auxDataNode.getSizes());
                mainDataNode.setSizeIsSet(true);
                size = 1;
                for(int i = 0; i < auxDataNode.getDimensions(); i++)
                {
                    size *= auxDataNode.getSizeOfDimension(i);
                }
                if(size <= 20) {
                    TreeExpander expander = new TreeExpander();
                    try {
                        expander.expandTree(mainDataNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
                if (auxDataNode.getAssertMethod() != null) {
                    mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
                }

                if(auxDataNode.getChildren().size() >0)
                {
                    for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                    {
                        if(size <= 20) {
                            if(size != -1) {
                                IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                                IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                                combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                            }
                        }
                        else
                        {
                            IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                            String name = auxTmpDataNode.getName();
                            int num = auxTmpDataNode.getName().indexOf('[');
                            String input = auxTmpDataNode.getName().substring(num);
                            List<String> expanded = new TreeExpander()
                                    .expandArrayItemByIndex( mainDataNode, input);
                            IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                            combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                        }
                    }
                }
//            }
        }

        else if(auxTmpNode instanceof  PointerStructureDataNode)
        {
            PointerStructureDataNode auxDataNode = (PointerStructureDataNode) auxTmpNode;
            PointerStructureDataNode mainDataNode = (PointerStructureDataNode) mainTmpNode;
            if(auxDataNode.getAllocatedSize() > 0) {
                mainDataNode.setAllocatedSize(auxDataNode.getAllocatedSize());
                mainDataNode.setSizeIsSet(true);

                TreeExpander expander = new TreeExpander();
                try {
                    expander.expandTree(mainDataNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (auxDataNode.getAssertMethod() != null) {
                    mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
                }

                if (auxDataNode.getChildren().size() > 0) {
                    for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                    {
                        IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                        IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                        combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                    }
                }
            }
            else if(auxDataNode.isSetSize() == true && auxDataNode.getAllocatedSize()  == -1)
            {
                mainDataNode.setAllocatedSize(auxDataNode.getAllocatedSize());
                mainDataNode.setSizeIsSet(true);
            }
        }
        else if(auxTmpNode instanceof  StructDataNode)
        {
            StructDataNode auxDataNode = (StructDataNode) auxTmpNode;
            StructDataNode mainDataNode = (StructDataNode) mainTmpNode;


            if (auxDataNode.getAssertMethod() != null) {
                mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
            }

            if (auxDataNode.getChildren().size() > 0) {
                for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                {
                    IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                    IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                    combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                }
            }
        }
        else if(auxTmpNode instanceof UnionDataNode)
        {
            UnionDataNode auxDataNode = (UnionDataNode) auxTmpNode;
            UnionDataNode mainDataNode = (UnionDataNode) mainTmpNode;

            if(auxDataNode.getAssertMethod() != null)
            {
                mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
            }
            if(auxDataNode.getSelectedField() != null)
            {
                String field = auxDataNode.getSelectedField();
                mainDataNode.setField(field);
                new TreeExpander().expandStructureNodeOnDataTree(mainDataNode, field);
                if(auxDataNode.getChildren().size() > 0 )
                {
                    for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                    {
                        IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                        IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                        combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                    }
                }
            }

        }

        else if(auxTmpNode instanceof VoidPointerDataNode )
        {
            VoidPointerDataNode auxDataNode = (VoidPointerDataNode) auxTmpNode;
            VoidPointerDataNode mainDataNode = (VoidPointerDataNode) mainTmpNode;

            if (auxDataNode.getReferenceType() != null && auxDataNode.getChildren().size() > 0)
            {
                ValueDataNode vtemp = (ValueDataNode) auxDataNode.getChildren().get(0);
                INode typeNode = vtemp.getCorrespondingVar().getCorrespondingNode();
                String referType = new String(auxDataNode.getReferenceType());
               // String typeNode = referType.substring(0, referType.indexOf("*") - 1);
                String stm = new String(referType + " value" );
                VariableNode v = new VariableNode();
                IASTNode ast = Utils.convertToIAST(stm);
                if (ast instanceof IASTDeclarationStatement) {
                    IASTDeclaration declaration = ((IASTDeclarationStatement) ast).getDeclaration();
                    if (declaration instanceof IASTSimpleDeclaration) {
                        v.setAST(declaration);
                        VariableNode parentVar = ((VoidPointerDataNode) mainDataNode).getCorrespondingVar();
                        v.setParent(parentVar);
                        v.setAbsolutePath(parentVar.getAbsolutePath() + File.separator + v.getName());
                    } else {
                        System.out.println("The declaration is not an IASTSimpleDeclaration");
                    }
                } else {
                    System.out.println("The ast is not an IASTDeclarationStatement");
                }

                v.setCorrespondingNode(typeNode);
                ValueDataNode child = new PointerTypeInitiation(v, mainDataNode).execute();

                if (child != null) {
                    child.setName("value");
                    mainDataNode.setReferenceType(referType);
                    mainDataNode.setInputMethod(VoidPointerDataNode.InputMethod.AVAILABLE_TYPES);
                    mainDataNode.setUserCode(null);
                    new TreeExpander().expandTree(child);
                } else {
                    throw new Exception("Not supported type: " + referType);
                }

//                v.setCorrespondingNode();


//                        ValueDataNode child = new PointerTypeInitiation(v, mainDataNode).execute();
//
//                        if (child != null) {
//                           // child.setName(NAME_REFERENCE);
//                            mainDataNode.setReferenceType(auxDataNode.getReferenceType());
//                            mainDataNode.setInputMethod(VoidPointerDataNode.InputMethod.AVAILABLE_TYPES);
//                            mainDataNode.setUserCode(null);
//                            try {
//                                new TreeExpander().expandTree(child);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
            }

            if(auxDataNode.getChildren().size() > 0)
            {
                for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                {
                    IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                    IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                    combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                }
            }
        }

        else if(auxTmpNode instanceof FunctionPointerDataNode)
        {
            FunctionPointerDataNode auxDataNode = (FunctionPointerDataNode) auxTmpNode;
            FunctionPointerDataNode mainDataNode = (FunctionPointerDataNode) mainTmpNode;

            if (auxDataNode.getSelectedFunction() != null)
            {
                mainDataNode.setSelectedFunction((ICommonFunctionNode) auxDataNode.getSelectedFunction());
            }

            if(auxDataNode.getChildren().size() > 0)
            {
                for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                {
                    IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                    IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                    combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                }
            }
        }

        else if(auxTmpNode instanceof PointerDataNode)
        {
            PointerDataNode auxDataNode = (PointerDataNode) auxTmpNode;
            PointerDataNode mainDataNode = (PointerDataNode) mainTmpNode;
            if(auxDataNode.getAllocatedSize() > 0)
            {
                mainDataNode.setAllocatedSize(auxDataNode.getAllocatedSize());
                mainDataNode.setSizeIsSet(true);

                TreeExpander expander = new TreeExpander();
                try {
                    expander.expandTree(mainDataNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (auxDataNode.getAssertMethod() != null) {
                    mainDataNode.setAssertMethod(auxDataNode.getAssertMethod());
                }

                if(auxDataNode.getChildren().size() >0)
                {
                    for(int j = 0; j < auxDataNode.getChildren().size(); j++)
                    {
                        IDataNode mainTmpDataNode = mainDataNode.getChildren().get(j);
                        IDataNode auxTmpDataNode = auxDataNode.getChildren().get(j);
                        combineDataMainCode(mainTmpDataNode, auxTmpDataNode);
                    }
                    //combineData(mainDataNode,auxDataNode);
                }
            }
            else if(auxDataNode.isSetSize() == true && auxDataNode.getAllocatedSize() == -1)
            {
                mainDataNode.setAllocatedSize(auxDataNode.getAllocatedSize());
                mainDataNode.setSizeIsSet(true);
            }
        }
        else if(auxTmpNode instanceof  EnumDataNode)
        {
            EnumDataNode mainTmpEnum = (EnumDataNode) mainTmpNode;
            EnumDataNode auxTmpEnum = (EnumDataNode) auxTmpNode;

            mainTmpEnum.setValue(auxTmpEnum.getValue());
            mainTmpEnum.setValueIsSet(true);

            if (auxTmpEnum.getAssertMethod() != null) {
                mainTmpEnum.setAssertMethod(auxTmpEnum.getAssertMethod());
            }
        }
        else if(auxTmpNode instanceof OtherUnresolvedDataNode)
        {
            OtherUnresolvedDataNode mainTmpUn = (OtherUnresolvedDataNode) mainTmpNode;
            OtherUnresolvedDataNode auxTmpUn = (OtherUnresolvedDataNode) auxTmpNode;
            if(auxTmpUn.getUserCode() != null) {
                mainTmpUn.setUserCode(auxTmpUn.getUserCode());
                mainTmpUn.setUseUserCode(auxTmpUn.isUseUserCode());
            }

            if (auxTmpUn.getAssertMethod() != null) {
                mainTmpUn.setAssertMethod(auxTmpUn.getAssertMethod());
            }
        }


    }


    public FunctionNode getFunctionNode(){
        return functionNode;
    }

    private SubprogramNode findSubProgramNode(IDataNode node)
    {
        if(node.getChildren().size() != 0)
        {
            for(int i = 0; i < node.getChildren().size(); i++)
            {
                IDataNode tmpNode =  node.getChildren().get(i);
                if(tmpNode instanceof SubprogramNode)
                {
                    return (SubprogramNode) tmpNode;
                }
                else
                {
                    SubprogramNode tmpSubNode = findSubProgramNode(tmpNode);
                    if(tmpSubNode == null) continue;
                    else return tmpSubNode;
                }
            }
        }
            return null;

    }

    private GlobalRootDataNode findGlobRootDataNode(IDataNode node)
    {
        if(node.getChildren().size() != 0)
        {
            for(int i = 0; i < node.getChildren().size(); i++)
            {
                IDataNode tmpNode =  node.getChildren().get(i);
                if(tmpNode instanceof GlobalRootDataNode)
                {
                    return (GlobalRootDataNode) tmpNode;
                }
                else
                {
                    GlobalRootDataNode tmpSubNode = findGlobRootDataNode(tmpNode);
                    if(tmpSubNode == null) continue;
                    else return tmpSubNode;
                }
            }
        }
        return null;
    }

    public List<CteTestcaseElement> getChosenTestCase() {
        return chosenTestCase;
    }

    public boolean isMultipleTestcasesInList()
    {
        return chosenTestCase.size() > 1;
    }
}
