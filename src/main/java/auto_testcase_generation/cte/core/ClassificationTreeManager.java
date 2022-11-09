package auto_testcase_generation.cte.core;

import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteComposition;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import auto_testcase_generation.cte.core.cteTable.CteTable;
import com.dse.boundary.BoundOfDataTypes;
import com.dse.boundary.DataSizeModel;
import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfigBound;
import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.externalvariable.RelatedExternalVariableDetecter;
import com.dse.parser.object.*;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseDataImporter;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.Utils;
import com.google.gson.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClassificationTreeManager {

    private ClassificationTree tree;
    private CteTable table;
    public boolean createNew = false;

    public ClassificationTreeManager(IFunctionNode f)
    {

        if (!loadTreeAndTableFromFile((FunctionNode) f)) {
            this.tree = autoGeneration((FunctionNode) f);
            createNew = true;
            this.table = new CteTable(tree);
           // table.addLines();
        }
        tree.SysPrint();
    }

    private void printTree(IASTNode node, int num)
    {
        System.out.print(" (" + num + ") ");
        for(int i = 0; i < num; i++)
        {
            System.out.print(">");
        }

        System.out.print(node.getClass() + ":  ");
        System.out.println(node.toString());
        for(int i = 0; i < node.getChildren().length; i++)
        {
            int pos = num;
            printTree(node.getChildren()[i], pos + 1);
        }
    }

    private void filterArrayNode(List<CPPASTArraySubscriptExpression> list)
    {
        //loai bo phan tu lap trong mang nhieu chieu
        List<CPPASTArraySubscriptExpression> tmp = new ArrayList<>();
        for(int i = 0; i < list.size(); i++)
        {
            boolean hasName = false;
            for(int j = 0; j < list.get(i).getChildren().length; j++)
            {
                if(list.get(i).getChildren()[j] instanceof CPPASTIdExpression || list.get(i).getChildren()[j] instanceof CPPASTFieldReference)
                {
                    hasName = true;
                    break;
                }
            }
            if(hasName == false) tmp.add(list.get(i));
        }

        for(int i = 0; i < tmp.size(); i++){
            list.remove(tmp.get(i));
        }

        tmp.clear();

//        //loai bo phan tu o ben trai dau =
//        for(int i = 0; i < list.size(); i++)
//        {
//            IASTNode tmpParNode = list.get(i).getParent();
//            int pos = -1;
//            for(int j = 0; j < tmpParNode.getChildren().length; j++)
//            {
//                if(tmpParNode.getChildren()[j] instanceof CPPASTArraySubscriptExpression)
//                {
//                    if(pos >= 0 && j > pos)
//                    {
//                        if(!tmp.contains(tmpParNode.getChildren()[pos])) tmp.add((CPPASTArraySubscriptExpression) tmpParNode.getChildren()[pos]);
//                    }
//                    else if (pos < 0) pos=j;
//                }
//            }
//        }
//
//        for(int i = 0; i < tmp.size(); i++)
//        {
//            list.remove(tmp.get(i));
//        }
    }

    private String getArrayElementDetail(CPPASTArraySubscriptExpression node)
    {
        return getArrayElementName(node) + getArrayElementPos(node, "");
    }

    private String getArrayElementName(CPPASTArraySubscriptExpression node)
    {

        for(int i = 0; i < node.getChildren().length; i++)
        {
            if(node.getChildren()[i] instanceof  CPPASTIdExpression)
            {
                return node.getChildren()[i].toString();
            }
            else if(node.getChildren()[i] instanceof  CPPASTFieldReference){
                CPPASTFieldReference tmp = (CPPASTFieldReference) node.getChildren()[i];
                String structName = null, elementName = null;
                for(int j = 0; j < tmp.getChildren().length; j++)
                {
                    if(tmp.getChildren()[j] instanceof CPPASTIdExpression ) structName = tmp.getChildren()[j].toString();
                    else if(tmp.getChildren()[j] instanceof CPPASTName) elementName = tmp.getChildren()[j].toString();
                }
                if(structName != null && elementName != null) return structName + "->" + elementName;
            }
        }
        return null;
    }

    private String getArrayElementPos(CPPASTArraySubscriptExpression node, String string)
    {

        for(int i = 0; i < node.getChildren().length; i++)
        {
            if(node.getChildren()[i] instanceof CPPASTLiteralExpression)
            {
                string += ( "[" + node.getChildren()[i].toString() + "]" );
            }
        }
        if(node.getParent() instanceof CPPASTBinaryExpression)
        {
            return string;
        }
        else if(node.getParent() instanceof CPPASTArraySubscriptExpression)
        {
            CPPASTArraySubscriptExpression par = (CPPASTArraySubscriptExpression) node.getParent();
            return getArrayElementPos(par, string);
        }
        return null;
    }

    public ClassificationTree autoGeneration(FunctionNode f) {
        //xđ bien local, global
        List <IVariableNode> argumentList = f.getArguments();
        RelatedExternalVariableDetecter detector = new RelatedExternalVariableDetecter(f);
        List<IVariableNode>  glbVarList = detector.findVariables();
        FunctionConfig fCongig = (FunctionConfig) f.getFunctionConfig();

        IASTFunctionDefinition a = f.getAST();

//        System.out.println("#####  DAY LA CAY AST CUA HAM " + f.getName() +"  #####");
////        ASTVisitor visitor = new ASTVisitor() {
////        }
//
//        for(int i = 0; i < a.getChildren().length; i++)
//        {
//            printTree(a.getChildren()[i], 0);
//        }

        List<CPPASTArraySubscriptExpression> list = new ArrayList<>();
        ASTVisitor visitor =  new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                if(expression instanceof CPPASTArraySubscriptExpression)
                {
                    CPPASTArraySubscriptExpression tmp = (CPPASTArraySubscriptExpression) expression;
                    list.add(tmp);
                }

                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitExpressions = true;
        //System.out.println("#################################");
        a.accept(visitor);
        filterArrayNode(list);
        List<String> arrayElementDetailList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++)
        {
            arrayElementDetailList.add(getArrayElementDetail(list.get(i)));
        }

        // loai bo phan tu lap vi co the mot phan tu bi goi nhieu lan trong code.

        Collections.sort(arrayElementDetailList);
        for(int i = 1; i < arrayElementDetailList.size(); i++)
        {
            if(arrayElementDetailList.get(i).equals(arrayElementDetailList.get(i-1)))
            {
                arrayElementDetailList.remove(i);
                i--;
            }
        }

        for(int i = 0; i < arrayElementDetailList.size(); i++)
        {
            System.out.println(arrayElementDetailList.get(i));
        }

//        System.out.println("#######################");


        //

        ClassificationTree tree = new ClassificationTree("auto-gen-tree", f);

        //tao Composition function
        CteComposition function = new CteComposition(f.getDeclaration());

        //tao Composition input;
        CteComposition input = new CteComposition("input");
        function.addChild(input);

        //tao Composition global
        if (!glbVarList.isEmpty()) {
            CteComposition global = new CteComposition("global");
            input.addChild(global);

            generateClassificationForInput(glbVarList, global, null, false, arrayElementDetailList);
        }


        //tao Composition argument
        CteComposition argument = new CteComposition("argument");
        input.addChild(argument);

        generateClassificationForInput(argumentList, argument, fCongig, false, arrayElementDetailList);

        //tao Composition output
        CteComposition output = new CteComposition("output");
        function.addChild(output);

        //tao Composition global
        if (!glbVarList.isEmpty()) {
            CteComposition global1 = new CteComposition("global");
            output.addChild(global1);

            generateClassificationForOutput(glbVarList, global1);
        }

        //tao Compostition argument
        CteComposition argument1 = new CteComposition("argument");
        output.addChild(argument1);

        generateClassificationForOutput(argumentList, argument1);

        //tao Classification return
        CteClassification return1 = new CteClassification("return");
        output.addChild(return1);

        tree.setRoot(function);
        tree.setIdForNodes();
        tree.initializeTreeTestcase(tree.getRoot());

        return tree;
    }

    public void generateClassificationForOutput(List<IVariableNode> argumentList,
                                                        CteNode parent) {
        for (IVariableNode argument: argumentList) {
            //tao Classification
            CteClassification classification = new CteClassification(argument.getName());
            parent.addChild(classification);
        }

    }

    private List<String> getNameInList(List<String> stringList, String name)
    {
        List<String> nameList = new ArrayList<>();
        for(String string: stringList)
        {
            if(string.contains(name + "["))  nameList.add(string);
        }
        return nameList;
    }

    public void generateClassificationForInput(List<IVariableNode> argumentList, CteNode parent, FunctionConfig functionConfig,
                                               boolean isStruct, List<String> arrayEleList) {
        for (IVariableNode argument : argumentList) {
            //xđ kieu du lieu
            BoundOfDataTypes boundOfDataTypes = new BoundOfDataTypes();
            DataSizeModel model = boundOfDataTypes.createILP32();
            String rawType = argument.getRawType();
            PrimitiveBound bound = model.get(rawType);
            INode tempNode = argument.resolveCoreType();

            if((tempNode instanceof UnionNode || tempNode instanceof StructTypedefNode) && !rawType.contains("*"))
            {
                CteComposition unionNode = new CteComposition(argument.getName());
                parent.addChild(unionNode);
                List<IVariableNode> list = new ArrayList<>();
                for(int i = 0; i < tempNode.getChildren().size(); i++)
                {
                    if(tempNode.getChildren().get(i) instanceof AttributeOfStructureVariableNode)
                    {
                        AttributeOfStructureVariableNode tmp = (AttributeOfStructureVariableNode) tempNode.getChildren().get(i);
                        list.add(tmp);
                    }
                }
                generateClassificationForInput(list, unionNode, functionConfig, true, arrayEleList);
            }
            else {
                List<String> listElement;
                if (isStruct == true) {
                    String elName = new String(parent.getName() + "->" + argument.getName());
                    listElement = getNameInList(arrayEleList, elName);
                } else {
                    listElement = getNameInList(arrayEleList, argument.getName());
                }

                if (listElement.size() > 0) {
                    CteComposition arrayNode = new CteComposition(argument.getName());
                    parent.addChild(arrayNode);
                    for(int i = 0; i < listElement.size(); i++)
                    {
                        int index = listElement.get(i).indexOf("->");
                        if(index == -1)
                        {
                            createClass(listElement.get(i), arrayNode, functionConfig, rawType, bound, tempNode, isStruct);
                        }
                        else
                        {
                            String name = listElement.get(i).substring(index+2);
                            createClass(name, arrayNode, functionConfig, rawType, bound, tempNode, isStruct);
                        }
                    }
                } else {
                        createClass(argument.getName(), parent, functionConfig, rawType, bound, tempNode, isStruct);
                }
            }
        }
    }

    private void createClass(String NodeName, CteNode parent, FunctionConfig functionConfig,
                                    String rawType, PrimitiveBound bound, INode tempNode, boolean isStruct )
    {
        CteClassification classification = new CteClassification(NodeName, parent);
        parent.addChild(classification);
        // xac dinh value theo FunctionConfig
        List<String> ListValue = new ArrayList<>();
        if (functionConfig != null) {
            IFunctionConfigBound config = functionConfig.getBoundOfArgumentsAndGlobalVariables().get(NodeName);
            if (config != null) {
                if (config instanceof MultiplePrimitiveBound) {
                    MultiplePrimitiveBound tmpConfig = (MultiplePrimitiveBound) config;
                    String lo = tmpConfig.get(0).getLower();
                    String up = tmpConfig.get(0).getUpper();
                    ListValue = getValueListFromBound(lo, up, rawType);
                }
            }
            else {
                if(bound != null)
                {
                    ListValue = getValueListFromBound(bound.getLower(), bound.getUpper(), rawType);
                }
            }
        }

        //tao class
        if (tempNode instanceof EnumNode) {
            for (String item : ((EnumNode) tempNode).getAllNameEnumItems()) {
                classification.addChild(new CteClass(item));
            }
        } else if ( rawType.contains("*")) {  //
            classification.addChild(new CteClass("NULL"));
            classification.addChild(new CteClass("not NULL"));
        } else if (rawType.contains("int") || rawType.contains("long") ) {
            if (ListValue.size() == 0) { //|| rawType.contains("double") || rawType.contains("float")

                if (bound != null) {
                    List<String> NodeValues = getValueListFromBound(bound.getLower(), bound.getUpper(), rawType);
                    for (String nodeValue : NodeValues) {
                        classification.addChild(new CteClass(nodeValue));
                    }
                } else {
                    classification.addChild(new CteClass("-1"));
                    classification.addChild(new CteClass("0"));
                    classification.addChild(new CteClass("1"));
                }
            } else {
                for (String s : ListValue) {
                    classification.addChild(new CteClass(s));
                }
            }

        } else if (rawType.contains("double") || rawType.contains("float")) {
            if (ListValue.size() == 0) {
                if(bound!= null) {
                    List<String> NodeValues = getValueListFromBound(bound.getLower(), bound.getUpper(), rawType );
                    for (String nodeValue : NodeValues) {
                        classification.addChild(new CteClass(nodeValue));
                    }
                }
                else{
                    classification.addChild(new CteClass("-1"));
                    classification.addChild(new CteClass("0"));
                    classification.addChild(new CteClass("1"));
                }
            } else {
                for (String s : ListValue) {
                    classification.addChild(new CteClass(s));
                }
            }

        } else if (rawType.contains("char")) {
            if (ListValue.size() == 0) {
                classification.addChild(new CteClass("a"));
                classification.addChild(new CteClass("z"));
            } else {
                for (String s : ListValue) {
                    classification.addChild(new CteClass(s));
                }
            }

        } else if (rawType.contains("bool")) {
            classification.addChild(new CteClass("true"));
            classification.addChild(new CteClass("false"));

        } else if (rawType.contains("["))
        {
            classification.addChild(new CteClass("NULL"));
            classification.addChild(new CteClass("not NULL"));
        }

    }

    public void exportCteToFile() {

        JsonObject treeJson = new JsonObject();
        treeJson.addProperty("name", tree.getName());
        treeJson.addProperty("numberOfNode", tree.getNumberOfNodesInTree());
        CteDataExporter exporter = new CteDataExporter();
        JsonElement rootNode = exporter.exportNodeToJSonElement(tree.getRoot());
        treeJson.add("rootNode", rootNode);
        if (table != null) {
            JsonElement jsonTable = exporter.exportTableToJsonElement(table);
            treeJson.add("table", jsonTable);
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        String jsonString = gson.toJson(treeJson);


        String cteFile = new WorkspaceConfig().fromJson().getCteFolder()
                 + File.separator  + getfNode().getInformName() + "_cteTree.json";
        Utils.writeContentToFile(jsonString, cteFile);
        UIController.showSuccessDialog("The Classification Tree has been saved", "Saving CTE", "Saved Successfully" );

    }

    public boolean loadTreeAndTableFromFile(FunctionNode f) {
        String ctePath = new WorkspaceConfig().fromJson().getCteFolder()
                + File.separator + f.getInformName() + "_cteTree.json";
        File cteFile = new File(ctePath);

        if (cteFile.exists() && !cteFile.isDirectory()) {
            importCteFromFile(ctePath, f);
            return (tree.getRoot() != null && table != null);
        }
        return false;
    }

    public void importCteFromFile(String path, IFunctionNode f) {

        String json = Utils.readFileContent(path);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        tree = new ClassificationTree(name, f);
        int numberOfNode = jsonObject.get("numberOfNode").getAsInt();
        tree.setNodeId(numberOfNode);

        CteComposition root = null;
        if (jsonObject.get("rootNode") != null) {
            JsonObject jsonRootNode = jsonObject.get("rootNode").getAsJsonObject();
            root = (CteComposition) importRoot(jsonRootNode);
        }
        tree.setRoot(root);

        table = new CteTable(tree);
        if (jsonObject.get("table") != null) {
            JsonObject jsonTable = jsonObject.get("table").getAsJsonObject();
            importLines(jsonTable, table);
        }
    }

    public static CteNode importRoot(JsonObject json) {
        JsonDeserializer<CteNode> deserializer = (json1, typeOfT, context) -> {
            JsonObject jsonObject = json1.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            CteNode node = null;
            try {
                node = (CteNode) Class.forName(type).newInstance();
                String name = jsonObject.get("name").getAsString();
                String id = jsonObject.get("id").getAsString();
                node.setName(name);
                node.setId(id);

                TestCase testcase = new TestCase();
                testcase.setName(id);
                DataNode rootDataNode;
                if (jsonObject.get("testcase") != null) {
                    JsonObject jsonTestcase = jsonObject.get("testcase").getAsJsonObject();
                    TestCaseDataImporter importer = new TestCaseDataImporter();
                    importer.setTestCase(testcase);
                    rootDataNode = importer.importRootDataNode(jsonTestcase);
                    testcase.setRootDataNode((RootDataNode) rootDataNode);
                    assert rootDataNode != null;
                    testcase.setFunctionNode(((RootDataNode) rootDataNode).getFunctionNode());
                    testcase.updateGlobalInputExpOutputAfterInportFromFile();
                }

                node.setTestcase(testcase);

                if (jsonObject.get("children") != null) {
                    for (JsonElement child : jsonObject.get("children").getAsJsonArray()) {
                        CteNode childNode = context.deserialize(child, CteNode.class);
                        if (childNode != null) {
                            node.addChild(childNode);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return node;
        };
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CteNode.class, deserializer);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, CteNode.class);
    }

    public ClassificationTree getTree() {
        return tree;
    }

    public void setTree(ClassificationTree tree) {
        this.tree = tree;
    }

    public FunctionNode getfNode() {
        return tree.getfNode();
    }

    public CteTable getTable() {
        return table;
    }


    public static void importLines(JsonObject json, CteTable table) {
        for (JsonElement line : json.get("lines").getAsJsonArray()) {
            List<CteNode> chosenNode = new ArrayList<>();
            for (JsonElement jsonNode : ((JsonObject) line).get("chosenNode").getAsJsonArray()) {
                chosenNode.add(table.getTree().searchNodeById(jsonNode.getAsString()));
            }
            table.addLines(((JsonObject) line).get("name").getAsString(), chosenNode);
        }
    }

    public boolean isCreateNew() {
        return createNew;
    }

    private List<String> getValueListFromBound(String lo, String up, String rawType)
    {
        List<String> result = new ArrayList<>();
        if (rawType.contains("int") || rawType.contains("long")) {
            long lowValue = Long.parseLong(lo);
            long upValue = Long.parseLong(up);
            result.add(String.valueOf(lowValue));
            if (upValue - lowValue != 0) {

                long midValue = (long) (upValue + lowValue) / 2;

                if (upValue - lowValue == 2) {
                    result.add(String.valueOf(midValue));
                } else if (upValue - lowValue > 2) {
                    long upMid = midValue + 1;
                    long lowMid = midValue - 1;
                    if (lowMid != lowValue) result.add(String.valueOf(lowMid));
                    result.add(String.valueOf(midValue));
                    result.add(String.valueOf(upMid));

                }
                result.add(String.valueOf(upValue));
            }
        } else if (rawType.contains("double") || rawType.contains("float")) {
            double lowValue = Double.parseDouble(lo);
            double upValue = Double.parseDouble(up);
            result.add(String.valueOf(lowValue));
            if (upValue - lowValue != 0) {

                double midValue = (lowValue + upValue) / 2;
                double padValue = 1;

                if (padValue != 0) {
                    double upMid = midValue + padValue;
                    double lowMid = midValue - padValue;
                    if (lowMid != lowValue && lowMid != upValue) {
                        result.add(String.valueOf(lowMid));
                    }
                    result.add(String.valueOf(midValue));
                    if (upMid != lowValue && upMid != upValue) {
                        result.add(String.valueOf(upMid));
                    }
                }
                result.add(String.valueOf(upValue));
            }
        } else if (rawType.contains("char")) {
            long lowValue = Long.parseLong(lo);
            long upValue = Long.parseLong(up);

            if (upValue < 97) {
                if (upValue < 33) {
                    String value2 = String.valueOf(upValue);
                    String value1 = String.valueOf(lowValue);
                    result.add(value1);
                    if (lowValue != upValue) result.add(value2);
                } else {
                    if (lowValue < 33) {
                        String value2 = String.valueOf(lowValue);
                        result.add(value2);
                    } else {
                        char lowC = (char) lowValue;
                        String value2 = Character.toString(lowC);
                        result.add(value2);
                    }

                    char upC = (char) upValue;
                    String value1 = Character.toString(upC);
                    if (lowValue != upValue) result.add(value1);

                }
            } else if (upValue <= 122) {
                char upC = (char) upValue;
                String value2 = Character.toString(upC);
                String value1 = new String("a");
                if (lowValue > 97) {
                    char lowC = (char) lowValue;
                    value1 = Character.toString(lowC);
                }
                result.add(value1);
                if (lowValue != upValue) result.add(value2);
            } else {
                String value1;
                String value2;
                if (lowValue > 122) {
                    if (lowValue == 127) {
                        value1 = String.valueOf(lowValue);
                    } else {
                        char lowC = (char) lowValue;
                        value1 = Character.toString(lowC);
                    }
                } else {
                    value1 = new String("a");
                }

                if (upValue == 127) {
                    value2 = String.valueOf(upValue);
                } else {
                    char upC = (char) upValue;
                    value2 = Character.toString(upC);
                }

                result.add(value1);
                if (lowValue != upValue) result.add(value2);
            }
        }
        return result;
    }


}
