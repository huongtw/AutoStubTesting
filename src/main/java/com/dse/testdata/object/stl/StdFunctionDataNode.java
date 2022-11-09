package com.dse.testdata.object.stl;

import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.IUserCodeNode;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

public class StdFunctionDataNode extends STLDataNode implements IUserCodeNode {

    private AbstractUserCode userCode = null;

    public String getReturnType() {
        if (arguments.size() > 0) {
            String templateArg = arguments.get(0);
            int index = templateArg.indexOf("(");

            if (index > 0) {
                return templateArg.substring(0, index);
            }
        }

        return null;
    }

    public String getTemplateArgument() {
        String arg = null;

        if (getArguments() != null) {
            arg = getArguments().get(0);
        }

        return arg;
    }

    public void setBody(String body) {
        String params = getParameterWrapper();
        userCode.setContent(String.format(CODE_TEMPLATE, params, body));
    }

    public String[] getParameters() {
        if (arguments.size() > 0) {
            String templateArg = arguments.get(0);
            int start = templateArg.indexOf("(");
            int end = templateArg.lastIndexOf(")");

            if (start > 0 && end > 0) {
                String paramsWrapper = templateArg.substring(start + 1, end);
                // TODO: template type <??, ??>
                return paramsWrapper.split(",\\s*");
            }
        }

        return null;
    }

    private String getParameterWrapper() {
        if (arguments.size() > 0) {
            String templateArg = arguments.get(0);
            int start = templateArg.indexOf("(");
            int end = templateArg.lastIndexOf(")");

            if (start > 0 && end > 0) {
                return templateArg.substring(start, end + 1);
            }
        }

        return null;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input = "";
        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            // get type of variable
            String typeVar = getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = VariableTypeUtils.deleteStructKeyword(typeVar);
            typeVar = VariableTypeUtils.deleteUnionKeyword(typeVar);

            if (isExternel())
                typeVar = "";

            String value = userCode == null ? IDataNode.NULL_POINTER_IN_CPP : userCode.getContent();

            // generate the statement
            if (this.isPassingVariable()) {
                input += typeVar + " " + this.getVituralName() + "=" + value + SpecialCharacter.END_OF_STATEMENT;

            } else if (this.isAttribute()) {
                if (userCode != null)
                    input += this.getVituralName() + "=" + value + SpecialCharacter.END_OF_STATEMENT;

            } else if (this.isArrayElement()) {
                if (userCode != null)
                    input += this.getVituralName() + "=" + value + SpecialCharacter.END_OF_STATEMENT;

            } else if (isSTLListBaseElement()) {
                input += typeVar + " " + this.getVituralName() + "=" + value + SpecialCharacter.END_OF_STATEMENT;

            } else if (this.isInConstructor()){
                input += typeVar + " " + this.getVituralName() + "=" + value + SpecialCharacter.END_OF_STATEMENT;

            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            } else {
                input += typeVar + " " + this.getVituralName() + "=" + value + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return  input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }

//    @Override
//    public String getContextPath() {
//        UnitNode unitNode = getUnit();
//        String filePath;
//
//        if (unitNode != null) {
//            filePath = unitNode.getSourceNode().getAbsolutePath();
//        } else {
//            String[] pathItems = getCorrespondingVar().getAbsolutePath().split(File.separator);
//            filePath = pathItems[1];
//        }
//
//        return filePath;
//    }

//    @Override
//    public String getTemporaryPath() {
//        UnitNode unitNode = getUnit();
//        String filePath;
//        String temporaryPath;
//
//        if (unitNode != null) {
//            filePath = unitNode.getSourceNode().getAbsolutePath();
//            temporaryPath = ProjectClone2.getClonedFilePath(filePath);
//        } else {
//            String[] pathItems = getCorrespondingVar().getAbsolutePath().split(File.separator);
//            filePath = pathItems[1];
//            temporaryPath = SystemLibrary.getLibrariesDirectory() + filePath + SystemLibrary.LIBRARY_EXTENSION;
//        }
//
//        int lastSeparator = temporaryPath.lastIndexOf(File.separator) + 1;
//        temporaryPath = temporaryPath.substring(0, lastSeparator) + "temporary.cpp";
//
//        return temporaryPath;
//    }

    @Override
    public String generateInitialUserCode() {
        String returnStm = "";

        String returnType = getReturnType();
        returnType = VariableTypeUtils.removeRedundantKeyword(returnType);

        if (!returnType.equals(VariableTypeUtils.VOID_TYPE.VOID)) {
            returnStm = "return ";
        }

        String params = getParameterWrapper();
        String code = String.format(CODE_TEMPLATE, params, returnStm);

        return code;
    }

    @Override
    public void setUserCode(AbstractUserCode userCode) {
        this.userCode = userCode;
    }

    @Override
    public AbstractUserCode getUserCode() {
        return userCode;
    }

    private static final String CODE_TEMPLATE = "[]%s {%s};";
}
