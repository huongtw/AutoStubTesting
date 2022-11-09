package com.dse.util;

import com.dse.parser.object.*;
import com.dse.report.csv.CsvParameter;
import com.dse.search.Search2;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.*;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class UtilsCsv {


   /*  public static  Map<String, String> readFile (String filePath){
        Map<String, String> standardTypePrimitive = new HashMap<>();
        try {
            File file = new File(filePath);
            Scanner myReader = new Scanner(file);
            myReader.nextLine();
            while (myReader.hasNextLine()){
                String data = myReader.nextLine();
                String [] data_ = data.split(",");
                standardTypePrimitive.put(data_[0],data_[1]);
                for (String s: data_) {
                    System.out.println(s);

                }
            }
        } catch (FileNotFoundException e){
            System.out.println("Error in read file");
            e.printStackTrace();
        }
        return standardTypePrimitive;
    }*/


    private static File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = UtilsCsv.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }
    private static InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = UtilsCsv.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }


    // print input stream
    private static void printInputStream(InputStream is) {

        try (InputStreamReader streamReader =
                     new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // print a file
    private static void printFile(File file) {

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            lines.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static Map<String, String>getStandardPrimitive() {
        //String path = UtilsCsv.class.getResource("standardPrimitive.txt").toString();
        // return readFile(path);

        //String fileName = "database.properties";
        Map<String, String> standard = new HashMap<>();
        String fileName = "standardPrimitive.txt";
        InputStream is = getFileFromResourceAsStream(fileName);
        try (InputStreamReader streamReader =
                     new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String [] strings = line.split(",");
                standard.put(strings[0],strings[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return standard;

    }


    static public List<String> getSubProgramParameters(TestCase currentTestcase, IFunctionNode functionNode) {
        List<String> list = new ArrayList<>();
        for (IVariableNode variableNode : functionNode.getArguments()) {
            list.add(variableNode.getName());
        }
        IDataNode staticNode = Search2.findStaticRoot((RootDataNode) currentTestcase.getRootDataNode());
        for (IDataNode node : staticNode.getChildren()) {
            list.add(node.getName());
        }
        return list;
    }

    static public List<String> getArgumentInSubProgram(IFunctionNode functionNode) {
        List<String> list = new ArrayList<>();
        for (IVariableNode variableNode : functionNode.getArguments()) {
            list.add(variableNode.getName());
        }
        return list;
    }

    static public List<String> getGlobalParameter(TestCase currentTestCase) {
        GlobalRootDataNode globalRootDataNode = Search2.findGlobalRoot(currentTestCase.getRootDataNode());
        List<String> list = new ArrayList<>();

        List<IVariableNode> staticGlobal = new ArrayList<>();
        List<IVariableNode> otherGlobal = new ArrayList<>();
        for (IVariableNode child : globalRootDataNode.getRelatedVariables()) {
            if (child.getASTType() != null && child.getASTType().getStorageClass() == IASTDeclSpecifier.sc_static) {
                staticGlobal.add(child);
            } else otherGlobal.add(child);

        }

        for (IVariableNode globalNode : staticGlobal) {
            list.add(globalNode.getName());
        }

        for (IVariableNode globalNode : otherGlobal) {
            list.add(globalNode.getName());
        }
        return list;
    }

    static public List<String> getPossibleOutput(TestCase currentTestcase, IFunctionNode functionNode) {
        List<String> outputs = new ArrayList<>();

        // return var
        outputs.add(functionNode.getSimpleName() + "@@");

        // arguments
        for (IVariableNode variableNode : functionNode.getArguments()) {
            String realType = variableNode.getRealType().trim();
            if (realType.endsWith("&") || VariableTypeUtils.isPointer(realType)
                    || VariableTypeUtils.isOneDimension(realType)
                    || VariableTypeUtils.isMultipleDimension(realType)) {
                outputs.add(variableNode.getName());
            }
        }

        // static var
        RootDataNode staticRoot = Search2.findStaticRoot(currentTestcase.getRootDataNode());
        for (IDataNode staticNode : staticRoot.getChildren()) {
            outputs.add(staticNode.getName());
        }

        // global var: static global first
        GlobalRootDataNode globalRootDataNode = Search2.findGlobalRoot(currentTestcase.getRootDataNode());
        List<IVariableNode> staticGlobal = new ArrayList<>();
        List<IVariableNode> otherGlobal = new ArrayList<>();
        for (IVariableNode child : globalRootDataNode.getRelatedVariables()) {
            if (child.getASTType() != null && child.getASTType().getStorageClass() == IASTDeclSpecifier.sc_static) {
                staticGlobal.add(child);
            } else otherGlobal.add(child);

        }

        for (IVariableNode globalNode : staticGlobal) {
            outputs.add(globalNode.getName());
        }

        for (IVariableNode globalNode : otherGlobal) {
            outputs.add(globalNode.getName());
        }
        return outputs;
    }

    public static List<String> getPointerArgCheckNullDirect(IASTNode iastNode) {
        if (iastNode == null) return null;
        List<String> listPointerCheckNullName = new ArrayList<>();
        if (iastNode instanceof CPPASTBinaryExpression) {
            CPPASTBinaryExpression expression = (CPPASTBinaryExpression) iastNode;
            IASTNode var1 = expression.getChildren()[0];

            IASTNode var2 = expression.getChildren()[1];

            if (var1.getRawSignature().equals("NULL")) {
                if (!listPointerCheckNullName.contains(var2.getRawSignature()))
                    listPointerCheckNullName.add(var2.getRawSignature());
            } else if (var2.getRawSignature().equals("NULL")) {
                if (!listPointerCheckNullName.contains(var1.getRawSignature()))
                    listPointerCheckNullName.add(var1.getRawSignature());
            }
        }

        for (IASTNode child : iastNode.getChildren()) {
            for (String s : getPointerArgCheckNullDirect(child))
                if (!listPointerCheckNullName.contains(s)) listPointerCheckNullName.add(s);
        }

        return listPointerCheckNullName;
    }


    public static String getVirtualName(IDataNode dataNode, IFunctionNode functionNode) {
        Stack<IDataNode> dataNodes = new Stack<>();
        String virtualName = "";
        if (dataNode instanceof ClassDataNode || dataNode instanceof ConstructorDataNode) return "";
        for (IDataNode iDataNode = dataNode; ; iDataNode = iDataNode.getParent()) {
            if (iDataNode instanceof SubprogramNode
                    || iDataNode.getName().equals("GLOBAL")
                    || iDataNode.getName().equals("STATIC")
                    || iDataNode instanceof ClassDataNode) {
                if(iDataNode.getName().equals("GLOBAL")) {
                    if (iDataNode instanceof GlobalRootDataNode) {
                        for(IVariableNode node: ((GlobalRootDataNode) iDataNode).getRelatedVariables()){
                            if (node.getASTType() != null && node.getASTType().getStorageClass() == IASTDeclSpecifier.sc_static){
                                virtualName += functionNode.getParent().getName() + "/";
                                break;
                            }
                        }
                    }
                }
                else if (iDataNode.getName().equals("STATIC")) virtualName += functionNode.getParent().getName() + "/";
                else if (dataNode instanceof ValueDataNode) {
                    if (((ValueDataNode) dataNode).getCorrespondingVar() instanceof StaticVariableNode) {
                        virtualName += functionNode.getParent().getName() + "/";
                   }
                }
                break;
            }
            dataNodes.push(iDataNode);
        }
        while (!dataNodes.isEmpty()) {
            IDataNode current = dataNodes.pop();
            if ((current instanceof ArrayDataNode || current instanceof PointerDataNode) && !dataNodes.isEmpty()) {
                virtualName += "";
            } else {
                virtualName += ((((ValueDataNode) current).getCorrespondingVar() instanceof ReturnVariableNode || current.getName().contains("RETURN")) ?
                        current.getName().replace("RETURN", functionNode.getSimpleName() + "@@") : current.getName()) + ".";
            }
        }
        return virtualName.substring(0, virtualName.length() - 1);
    }

    public static String getVirtualNameStub(IDataNode dataNode, IFunctionNode functionNode, int idRoot) {

        String AMSTB = "AMSTB_";
        String prefix = AMSTB + functionNode.getParent().getName() + "/" + AMSTB + functionNode.getSimpleName() + "@";

        Stack<IDataNode> dataNodes = new Stack<>();
        String virtualName = "";
        for (IDataNode iDataNode = dataNode; ; iDataNode = iDataNode.getParent()) {
            if (iDataNode instanceof SubprogramNode || iDataNode.getName().equals("GLOBAL") || iDataNode.getName().equals("STATIC")) {
                if (dataNode instanceof SubprogramNode) return prefix + "STBCNT_" + functionNode.getSimpleName();
                break;
            }
            dataNodes.push(iDataNode);
        }
        IDataNode firstNode = dataNodes.peek();
        while (!dataNodes.isEmpty()) {
            IDataNode current = dataNodes.pop();
            if (current == firstNode) {
                if (((ValueDataNode) current).getCorrespondingVar() instanceof ReturnVariableNode || current.getName().contains("RETURN")) {
                    prefix += "STUBRET_" + functionNode.getSimpleName() + ".";
                    virtualName += prefix;
                } else {
                    prefix += "STUBARG_" + functionNode.getSimpleName() + "_";
                    String[] split = current.getName().split("\\[", 2);
                    virtualName += prefix + "ARG" + Integer.toString(idRoot + 1) + (split.length < 2 ?
                            "" : ("[" + split[1])) + "_";
                }
                continue;
            }

            if ((current instanceof ArrayDataNode || current instanceof PointerDataNode) && !dataNodes.isEmpty()) {
                virtualName += "";
            } else {
                VariableNode valueDataNode = ((ValueDataNode) current).getCorrespondingVar();

                if (current.getParent().getParent() instanceof SubprogramNode) {
                    if (current.getParent() instanceof ArrayDataNode)
                        virtualName = virtualName.substring(0, virtualName.length() - 1) +
                                "[" + current.getName().split("\\[", 2)[1] + "]";
                    continue;
                }
                virtualName += ((valueDataNode instanceof ReturnVariableNode || current.getName().contains("RETURN")) ?
                        current.getName().replace("RETURN", functionNode.getSimpleName()) : current.getName()) + ".";
            }
        }
        return virtualName.substring(0, virtualName.length() - 1);

    }

    public static int getStubCount(SubprogramNode stubSubprogram) {
        int stubCnt = 0;
        for (IDataNode child : stubSubprogram.getChildren()) {
            if (child instanceof ValueDataNode) {
                ValueDataNode valueNode = (ValueDataNode) child;
                int maxIterCnt = 0;
                for (Iterator iterator : valueNode.getIterators()) {
                    int iterCnt = iterator.getStartIdx();
                    if (iterator.getRepeat() != Iterator.FILL_ALL) {
                        iterCnt += iterator.getRepeat() - 1;
                    }
                    if (iterCnt > maxIterCnt)
                        maxIterCnt = iterCnt;
                }

                if (maxIterCnt > stubCnt)
                    stubCnt = maxIterCnt;
            }
        }
        return stubCnt;
    }

    public static List<CsvParameter> getChildInPointerParameter(Queue<CsvParameter> queue) {
        List<CsvParameter> list = new ArrayList<>();
        while (!queue.isEmpty()) {
            CsvParameter currentPop = queue.poll();
            Stack<CsvParameter> stack = new Stack<>();
            stack.push(currentPop);
            while (!stack.isEmpty()) {
                CsvParameter currentStack = stack.pop();
                if (currentStack.isHaveValue() && !currentStack.equals(currentPop)) list.add(currentStack);
                if (queue.contains(currentStack)) continue;
                Stack<CsvParameter> reverse = new Stack<>();
                for (int i = currentStack.getChildren().size() - 1; i >= 0; i--) {
                    if (currentStack.getChildren().get(i).getDataNode() instanceof PointerDataNode) {
                        reverse.push(currentStack.getChildren().get(i));
                    }
                    stack.push(currentStack.getChildren().get(i));
                }
                Collections.reverse(reverse);
                queue.addAll(reverse);
            }
        }
        return list;
    }

    public static List<IASTNode> getExpressionNode(IASTNode iastNode) {
        List<IASTNode> list = new ArrayList<>();
        if (iastNode instanceof CPPASTIdExpression
                || iastNode instanceof CPPASTArraySubscriptExpression
                || iastNode instanceof CPPASTFieldReference
                || iastNode instanceof CPPASTName) list.add(iastNode);
        for (IASTNode child : iastNode.getChildren()) {
            list.addAll(getExpressionNode(child));
        }
        return list;
    }

    public static void main(String[] args) {
       // String path = UtilsCsv.class.getResource("/standardPrimitive.txt").toString();
       // System.out.println(path);
       // readFile(UtilsCsv.class.getResource("/standardPrimitive.txt").toString());
        System.out.println(getStandardPrimitive());
        String a = "***rt&*";
        System.out.println(a.replaceAll("[*&]*",""));
    }
}
