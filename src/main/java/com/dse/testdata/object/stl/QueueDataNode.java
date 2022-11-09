package com.dse.testdata.object.stl;

import com.dse.environment.Environment;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

public class QueueDataNode extends ListBaseDataNode {
    @Override
    public String getElementName(int index) {
        if (index == 0)
            return "front";
        else
            return "element #" + index;
    }

    @Override
    public String getPushMethod() {
        return "push";
    }

//    @Override
//    public String getAssertionForGoogleTest(String method, String source, String target) throws Exception {
//        String output = "";
//
//        if (Environment.getInstance().isC())
//            return "";
//
//        if (isSetSize()) {
//            String actualOutputName = getVituralName().replace(source, target);
//
//            output += String.format("%s(%s.size(), %s.size())%s\n", method, getVituralName(),
//                    actualOutputName, SourceConstant.LOG_FUNCTION_CALLS);
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
//                    output += String.format("%s %s = %s.front();\n",
//                            coreType, actualOutputChildName, actualOutputName);
//
//                    output += String.format("%s.pop();\n", actualOutputName);
//
//                    output += dataNode.getAssertionForGoogleTest(method, source, target) + SpecialCharacter.LINE_BREAK;
//                }
//            }
//
//            // add again
//            for (IDataNode child : getChildren()) {
//                String actualOutputChildName = child.getVituralName().replace(source, target);
//
//                output += String.format("%s.%s(%s);\n", actualOutputName, getPushMethod(), actualOutputChildName);
//            }
//        }
//
//        return output;
//    }
}
