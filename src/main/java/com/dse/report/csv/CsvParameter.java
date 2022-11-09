package com.dse.report.csv;

import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.StaticVariableNode;
import com.dse.testdata.object.*;
import com.dse.util.UtilsCsv;

import java.util.*;

public class CsvParameter implements Comparable {
    public static final int _INPUT = 0;
    public static final int _OUTPUT = 1;
    private Map<Integer, CsvValueNode> valueMap;
    private Map<Integer, Integer> stubCountMap; // use only for dataNode is stub subprogram
    private CsvParameter parent;
    private List<CsvParameter> children;
    private IValueDataNode dataNode;
    private String name;
    private int type = _INPUT;
    private int id = 0;
    private int currentLevel;
    private String nameFormat;
    private int dummyIndex = 0;
    private List<CsvParameter> iterator;
    private int iteratorIndex;

    private boolean isArgInput = false;
    private boolean isPointerChekNull = false;
    private boolean haveValue = false;
    private boolean isStub =false;
    private boolean isReturn = false;
    private boolean isStatics = false;
    private String pointerStubFormat = "";

    public boolean isStatics() {
        return isStatics;
    }

    public void setStatics(boolean statics) {
        isStatics = statics;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
    }

    public List<CsvParameter> getIterator() {
        return iterator;
    }

    public void setIterator(List<CsvParameter> iterator) {
        this.iterator = iterator;
    }

    public int getIteratorIndex() {
        return iteratorIndex;
    }

    public void setIteratorIndex(int iteratorIndex) {
        this.iteratorIndex = iteratorIndex;
    }

    public boolean isHaveValue() {
        return haveValue;
    }

    public void setHaveValue(boolean haveValue) {
        this.haveValue = haveValue;
    }

    public boolean isPointerChekNull() {
        return isPointerChekNull;
    }

    public void setPointerChekNull(boolean pointerChekNull) {
        isPointerChekNull = pointerChekNull;
    }

    public boolean isStub() {
        return isStub;
    }

    public void setStub(boolean stub) {
        isStub = stub;
    }

    public boolean isArgInput() {
        return isArgInput;
    }

    public void setArgInput(boolean argInput) {
        isArgInput = argInput;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public CsvParameter getParent() {
        return parent;
    }

    public List<CsvParameter> getChildren() {
        return children;
    }

    public void setChildren(List<CsvParameter> children) {
        this.children = children;
    }

    public void setParent(CsvParameter parent) {
        this.parent = parent;
    }

    public Map<Integer, CsvValueNode> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<Integer, CsvValueNode> valueMap) {
        this.valueMap = valueMap;
    }

    public int getDummyIndex() {
        return dummyIndex;
    }

    public void setDummyIndex(int dummyIndex) {
        this.dummyIndex = dummyIndex;
    }

    public void increaseDummyIndex() {
        dummyIndex++;
    }


    public void addStubCount(int testCase, int stubCount) {
        if(dataNode instanceof  SubprogramNode && isStub){
            stubCountMap.put(testCase, stubCount);
        }
    }

    public Map<Integer, Integer> getStubCountMap() {
        return stubCountMap;
    }

    public CsvParameter() {
        valueMap = new HashMap<>();
        children = new ArrayList<>();
        this.stubCountMap =new HashMap<>();
        this.iterator = new ArrayList<>();
    }

    public CsvParameter(CsvParameter parent, IValueDataNode dataNode) {
        this.parent = parent;
        this.dataNode = dataNode;
        this.valueMap = new HashMap<>();
        this.stubCountMap =new HashMap<>();
        this.iterator = new ArrayList<>();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void addCsvParameterChild(CsvParameter parameter) {
        parameter.setParent(this);
        children.add(parameter);
    }

    public IValueDataNode getDataNode() {
        return dataNode;
    }

    public void setDataNode(IValueDataNode dataNode) {
        this.dataNode = dataNode;
        setName(dataNode.getName());
    }

    public String getName() {
        /*if (parent == null) {
            if (dataNode instanceof ArrayDataNode) return "";
            if(dataNode instanceof PointerDataNode&&((PointerDataNode) dataNode).getAllocatedSize()>0) return "";
            return this.name;
        }
        String name = "";
        if(dataNode instanceof PointerDataNode&&((PointerDataNode) dataNode).getAllocatedSize()>0) return this.parent.getName();
        if (!(dataNode instanceof ArrayDataNode)){
            name = this.name;
            if (this.parent.getName().equals("")) return name;
            return this.parent.getName() + "." + name;
        }*/
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addValue(CsvValueNode valueNode) {
        valueMap.put(valueNode.getId(), valueNode);
        valueNode.setParameter(this);
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public void setNameFormat(IFunctionNode functionNode) {
        Map<String,String> standardPrimitive = UtilsCsv.getStandardPrimitive();
        Set <String> primitiveType = standardPrimitive.keySet();

        CsvParameter root = this;
        while (root.getParent() != null) {
            root = root.getParent();
            if (root.isPointerChekNull()) break;
        }
        this.nameFormat = "";


        String virtualName = (UtilsCsv.getVirtualName(dataNode, functionNode));
        if (isArgInput && !(dataNode.getCorrespondingVar() instanceof StaticVariableNode)) this.nameFormat = "@";
        if(root.isPointerChekNull()&& parent!=null) {
            this.nameFormat="";
            String rawType= dataNode.getRawType();
            String realType = dataNode.getRealType();
            rawType = rawType.replaceAll("[&*]*","");
            realType = realType.replaceAll("[&*]*","");
            String behindDummy = "";
            if(standardPrimitive.get(rawType)!=null||standardPrimitive.get(realType)!=null) behindDummy = standardPrimitive.get(rawType);
            else behindDummy = functionNode.getSimpleName();
            this.nameFormat += (root.isReturn()) ? "Dummy_return_" : "Dummy_" + behindDummy + '_';
            if(root.isReturn()) virtualName = virtualName.replace("@@", "");
        }
        this.nameFormat += virtualName;
        if (dataNode instanceof PointerDataNode) {
            if (parent == null){
                if(!isPointerChekNull || this.getType() == _OUTPUT) this.nameFormat = "$" + this.nameFormat;
            } else {
                String partOne = this.nameFormat.substring(0, this.nameFormat.length() - this.dataNode.getName().length());
                String partTwo = this.dataNode.getName();
                if (parent.isReturn()) {
                    partOne = partOne.replace("RETURN", "");
                    partTwo = partTwo.replace("RETURN", "");
                }
                if (!isPointerChekNull) partOne = '$' + partOne;
                this.nameFormat = partOne + partTwo;
            }
        }

        if(dataNode instanceof PointerDataNode && this.isStub()) this.pointerStubFormat = this.nameFormat;
    }

    public String getReformatOutputParameter() {
        if (type == _OUTPUT) {
            String res = nameFormat.replaceAll("[$]", "");
            res = res.replaceAll("@", "");
            return res;
        }
        return getNameFormat();
    }

    public String getPointerStubFormat() {
        return pointerStubFormat;
    }

    public void setPointerStubFormat(String pointerStubFormat) {
        this.pointerStubFormat = pointerStubFormat;
    }

    public CsvParameter getRootParent() {
        if (parent == null) return this;
        return this.getParent().getRootParent();
    }

    public String getNameOnCSV() {
        String nameOnCSV = "";
        if (dataNode instanceof PointerDataNode) {
            if (parent == null){
                nameOnCSV += (isPointerChekNull) ? dataNode.getName() : "$" + dataNode.getName();
                if (isArgInput) nameOnCSV = '@' + nameOnCSV;
            }
            else {
                if (!isPointerChekNull) nameOnCSV = '$' + dataNode.getName();
                else nameOnCSV = dataNode.getName();
                String parentName = parent.getNormalNameOnCSV();
                if (!parentName.equals("")) {
                    nameOnCSV = parentName + "." + nameOnCSV;
                }
            }
        }
        else {
            nameOnCSV += getNormalNameOnCSV();
        }
        nameOnCSV += ',';

        return nameOnCSV;
    }

    public void setNameFormatStub(IFunctionNode functionNode) {
        int id=0;
        int idCall = 0;
        boolean haveAncestorPointerNode = false;
        boolean haveAncestorReturnNode =false;

        for(CsvParameter parameter1 = this;;parameter1=parameter1.getParent()){
            if (parameter1.getDataNode() instanceof SubprogramNode || parameter1.getDataNode().getName().equals("GLOBAL") || parameter1.getDataNode().getName().equals("STATIC")) {
                idCall = parameter1.id;
                break;
            }
            if(parameter1.getParent().getDataNode() instanceof PointerDataNode) haveAncestorPointerNode=true;
            if(parameter1.getParent().getName().contains("RETURN")) haveAncestorReturnNode = true;
            id = parameter1.currentLevel;
        }

        this.nameFormat = UtilsCsv.getVirtualNameStub(dataNode,functionNode,id);
        if(haveAncestorPointerNode) {
            String name = UtilsCsv.getVirtualName(dataNode,functionNode);
            this.nameFormat =  "Dummy_st_"+functionNode.getSimpleName()+"_"+(haveAncestorReturnNode?name.replace(functionNode.getSimpleName()+"@@", "return") : name);

        }

        if(this.getDataNode() instanceof SubprogramNode) this.nameFormat+="";
        else {
           // if(this.getDataNode() instanceof PointerDataNode) this.nameFormat= "$"+this.nameFormat;
            this.nameFormat+="["+idCall+"]";
        }
    }

    private String getNormalNameOnCSV() {
        if (parent == null) {
            if (!(dataNode instanceof PointerDataNode)) return dataNode.getName();
            else return "";
        }
        String name = "";
        if(dataNode instanceof PointerDataNode
                || dataNode instanceof SubprogramNode
                || dataNode instanceof ClassDataNode) return this.parent.getNormalNameOnCSV();
        if (!(dataNode instanceof ArrayDataNode)){
            name = this.name;
            String parentName = parent.getNormalNameOnCSV();
            if (!parentName.equals("")) name = parentName + '.' + name;
        }
        return name;
    }

    public int getMaxIterator(){
        int maxItr= 0;
        Collection<Integer> values = stubCountMap.values();
        for (Integer v : values) {
            maxItr = maxItr<v ?v : maxItr;
        }
        return maxItr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CsvParameter parameter = (CsvParameter) o;
        return Objects.equals(getName(), parameter.getName()) && Objects.equals(this.getParent(), parameter.getParent())
                && Objects.equals(this.getType(), parameter.getType())
                && Objects.equals(this.id, parameter.getId())
                && Objects.equals(this.currentLevel, parameter.getCurrentLevel());
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof CsvParameter)) {
            return -1;
        }
        CsvParameter parameter = (CsvParameter) o;
        if (id == parameter.getId()) {
            if (currentLevel == parameter.getCurrentLevel()) {
                return 0;
            }
            return (currentLevel < parameter.getCurrentLevel()) ? 1 : -1;
        } else if (id > parameter.getId()) {
            return 1;
        }
        return -1;
    }

    public boolean isPointerParameter() {
        return getDataNode() instanceof PointerDataNode;
    }

    public CsvParameter getRootPointerNode() {
        CsvParameter parent = getParent();
        if (parent == null || !parent.isPointerParameter()) {
            return null;
        }

        if (parent == null) {
            return this;
        }
        CsvParameter child = this;
        while (parent != null && parent.isPointerParameter()) {
            child = parent;
            parent = parent.getParent();
        }
        return child;
    }

    /**
     * parent: p : int***
     * this: p[0] -> true
     * else false
     * @return
     */
    public boolean isFirstChildOfPointer() {
        CsvParameter parent = getParent();
        if (parent != null && parent.isPointerParameter()) {
            String parentName = parent.getName();
            String rootPointerParentName = getRootPointerNode().getName();
            return  (parent.getChildren().indexOf(this) == 0 && parentName.equals(rootPointerParentName));
        }
        return false;
    }

    /**
     * this = int*** p
     * @return p[0][0][0]
     */
    public CsvParameter getLeafChildOfPointer() {
        if (!isPointerParameter()) return null;
        for (CsvParameter child : this.getChildren()) {
            if (child.isPointerParameter()) {
                CsvParameter childLeaf = child.getLeafChildOfPointer();
                if (childLeaf != null) return childLeaf;
            }
            else return child;
        }
        return null;
    }

    public String getContentFormatIntoStubCFile() {
        String content = "";
        if (parent == null) content += getDataNode().getRawType() + " " + getName();
        else content = getNameFormat();
        content += "=" + getValueMap().get(0).getValue();
        return content;
    }

}
