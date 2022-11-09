package com.dse.testdata.object.stl;

import com.dse.environment.Environment;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ListBaseDataNode extends STLDataNode {
    private int size = -1;

    private boolean sizeIsSet = false;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isSetSize() {
        return sizeIsSet;
    }

    public void setSizeIsSet(boolean sizeIsSet) {
        this.sizeIsSet = sizeIsSet;
    }

    public abstract String getElementName(int index);

    public String getTemplateArgument() {
        String arg = null;

        if (getArguments() != null) {
            arg = getArguments().get(0);
        }

        return arg;
    }

    public abstract String getPushMethod();

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {

        String declaration = "";
        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            String type = VariableTypeUtils
                    .deleteStorageClassesExceptConst(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));

            String type2 = VariableTypeUtils.getFullRawType(getCorrespondingVar());

//        if (!type.startsWith(SpecialCharacter.STD_NAMESPACE))
//            type = SpecialCharacter.STD_NAMESPACE + type;

            if (isExternel())
                type = "";

            if (!isAttribute() && !isArrayElement()) {
                declaration += type + SpecialCharacter.SPACE + getVituralName()
                        + SpecialCharacter.END_OF_STATEMENT + SpecialCharacter.LINE_BREAK;
            }

            List<IDataNode> children = new ArrayList<>(getChildren());

            if (this instanceof StackDataNode)
                Collections.reverse(children);

            for (IDataNode child : children) {
//            declaration += "//" + child.getClass().getSimpleName() + SpecialCharacter.SPACE
//                    + child.getName() + SpecialCharacter.LINE_BREAK;

                declaration += child.getInputForGoogleTest(isDeclared) + SpecialCharacter.LINE_BREAK;
                declaration += getVituralName() + SpecialCharacter.DOT + this.getPushMethod()
                        + "(" + child.getVituralName() + ")"
                        + SpecialCharacter.END_OF_STATEMENT + SpecialCharacter.LINE_BREAK;
            }
        }

        return declaration;
    }

//    @Override
//    public String getAssertionForGoogleTest(String method, String source, String target) throws Exception {
//        String output = "";
//
//        if (Environment.getInstance().isC())
//            return "";
//
//        if (sizeIsSet) {
//            String actualOutputName = getVituralName().replace(source, target);
//
//            output += String.format("%s(%s.size(), %s.size())%s\n", method, getVituralName(),
//                    actualOutputName, SourceConstant.LOG_FUNCTION_CALLS);
//
//            String type = getRawType();
//
//            // init iterator
//            output += String.format("%s::iterator AKA_TEMP_ITERATOR = %s.begin();\n", type, actualOutputName);
//
//            for (IDataNode child : getChildren()) {
//                if (child instanceof ValueDataNode) {
//                    ValueDataNode dataNode = (ValueDataNode) child;
//
//                    String actualOutputChildName = dataNode.getVituralName().replace(source, target);
//
//                    String coreType = VariableTypeUtils
//                            .deleteStorageClasses(dataNode.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//
//                    output += String.format("%s %s = *AKA_TEMP_ITERATOR;\n",
//                            coreType, actualOutputChildName);
//
//                    output += dataNode.getAssertionForGoogleTest(method, source, target) + SpecialCharacter.LINE_BREAK;
//
//                    // increase iterator
//                    output += "AKA_TEMP_ITERATOR++;\n";
//                }
//            }
//        }
//
//        return output;
//    }
}
