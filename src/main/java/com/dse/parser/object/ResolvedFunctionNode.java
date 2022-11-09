package com.dse.parser.object;

import java.util.ArrayList;
import java.util.List;

public class ResolvedFunctionNode extends DefinitionFunctionNode {
//    private List<IVariableNode> arguments;
//    private String returnType;
//    private String simpleName;
//
//    @Override
//    public String getNewType() {
//        return getName();
//    }
//
//    public void setArguments(List<IVariableNode> arguments) {
//        List<INode> children = new ArrayList<>(arguments);
//        for (IVariableNode child:arguments)
//            this.getChildren().add((Node) child);
//        this.arguments = arguments;
//    }
//
//    public void setReturnType(String returnType) {
//        this.returnType = returnType;
//    }
//
//    public void setSimpleName(String simpleName) {
//        this.simpleName = simpleName;
//    }
//
//
//
//    public void setName() {
//        StringBuilder result = new StringBuilder();
//        result.append(simpleName).append("(");
//        for (int i = 0; i < arguments.size(); i++) {
//            if (i != 0)
//                result.append(",");
//            result.append(arguments.get(i).getRawType());
//        }
//        result.append(")");
//        setName(result.toString());
//    }
//
//    @Override
//    public List<IVariableNode> getArguments() {
//        return arguments;
//    }
//
//    @Override
//    public String getReturnType() {
//        return returnType;
//    }
//
//    @Override
//    public String getSimpleName() {
//        return simpleName;
//    }
//
//    @Override
//    public String getSingleSimpleName() {
//        return simpleName;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder result = new StringBuilder();
//        result.append(returnType).append(" ").append(simpleName).append("(");
//        for (int i = 0; i < arguments.size(); i++) {
//            if (i != 0)
//                result.append(",");
//            result.append(arguments.get(i).getRawType()).append(" ").append(arguments.get(i));
//        }
//        result.append(")");
//        return result.toString();
//    }
}
