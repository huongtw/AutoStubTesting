package com.dse.report.csv;

import com.dse.testdata.object.*;

public class CsvValueNode {
    private Integer id;
    private IValueDataNode data;
    private CsvParameter parameter;

    public CsvParameter getParameter() {
        return parameter;
    }

    public void setParameter(CsvParameter parameter) {
        this.parameter = parameter;
    }

    public CsvValueNode(Integer id, IValueDataNode data) {
        this.id = id;
        this.data = data;
    }

    public CsvValueNode() {
    }

    public CsvValueNode(CsvParameter parameter, IValueDataNode data) {
        this.data = data;
        this.parameter = parameter;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public IValueDataNode getData() {
        return data;
    }

    public void setData(IValueDataNode data) {
        this.data = data;
    }

    public String getValue() {
        if (data instanceof NormalDataNode) {
            if (data == null || data.getName().equals("null")) {
                return "";
            }
            String value = ((NormalDataNode) data).getValue();
            return (value == null) ? "" : value;
        } else if(data instanceof EnumDataNode){
            String value = ((EnumDataNode) data).getValue();
            return (value == null) ? "" : value;
        } else if(data instanceof PointerDataNode){

            if(parameter.isStub()){
                if(((PointerDataNode) data).getAllocatedSize()<=0||parameter.getChildren().isEmpty()) return "0";

                String val = parameter.getChildren().get(0).getNameFormat();
                return val;
            }

            if(((PointerDataNode) data).getAllocatedSize()<=0||parameter.getChildren().isEmpty()) return "0";
            if(!parameter.isPointerChekNull()) return Integer.toString(((PointerDataNode) data).getAllocatedSize());
            if (parameter.isFirstChildOfPointer()) {
                return parameter.getLeafChildOfPointer().getNameFormat();
            }
            String val = parameter.getChildren().get(0).getNameFormat();
            return val.replace("$", "");
//            String partOne = val.substring(0, val.length() - parameter.getChildren().get(0).getName().length());
//            String partTwo = this.data.getName();
//            return (partOne + partTwo).replace("$", "");
        }

        return "";
    }

    @Deprecated
    public String getDefaultValue() {
        CsvParameter parameter = this.getParameter();
        IValueDataNode data = this.getData();
//        CsvValueNode longestSizeValueNode = null;
        for (CsvValueNode valueNode : parameter.getValueMap().values()) {
            IValueDataNode dataNode = valueNode.getData();
            if (dataNode != null) {
                if (dataNode instanceof NormalDataNode ) return ((NormalDataNode) dataNode).getValue();
                else if (dataNode instanceof EnumDataNode) return ((EnumDataNode) dataNode).getValue();
//                else if (dataNode instanceof PointerDataNode
//                        && dataNode.getChildren().size() > data.getChildren().size()) {
//                    data = dataNode;
//                }
            }
        }
//        if (data instanceof PointerDataNode) return data.
        return "";
    }

    @Override
    public String toString() {
        return "CsvValueNode{" +
                "data=" + data +
                '}';
    }
}
