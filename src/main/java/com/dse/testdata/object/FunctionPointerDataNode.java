package com.dse.testdata.object;

import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.testdata.comparable.*;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;

import java.util.List;

public class FunctionPointerDataNode extends OtherUnresolvedDataNode implements INullableComparable {

    private List<INode> possibleFunctions;

    private ICommonFunctionNode selectedFunction;

    public List<INode> getPossibleFunctions() {
        IFunctionPointerTypeNode typeNode = getCorrespondingType();
        if (possibleFunctions == null && typeNode != null) {
            possibleFunctions = Search.searchAllMatchFunctions(typeNode);
        }

        return possibleFunctions;
    }

    @Override
    public boolean haveValue() {
        return selectedFunction != null;
    }

    public void setSelectedFunction(ICommonFunctionNode selectedFunction) {
        this.selectedFunction = selectedFunction;
    }

    public INode getSelectedFunction() {
        return selectedFunction;
    }

    @Override
    public String getDisplayNameInParameterTree() {
        if (name.isEmpty()) {
            INode typeNode = getCorrespondingType();

            if (typeNode instanceof FunctionPointerTypeNode) {
                return ((FunctionPointerTypeNode) typeNode).getFunctionName();
            }
        }

        return super.getDisplayNameInParameterTree();
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) {
        String input = SpecialCharacter.EMPTY;

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){

            String typeVar = getRawType();

            if (isExternel())
                typeVar = "";

            if (getName().isEmpty()) {
                String functionName = getCorrespondingType().getFunctionName();
                String newFunctionName = getVituralName() + functionName;

                if (selectedFunction != null) {

                    String valueVar = String.format("&%s;", selectedFunction.getSimpleName());

                    if (this.isPassingVariable()) {
                        String first = typeVar.replaceFirst("\\b" + functionName + "\\b", newFunctionName);
                        input += first + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isAttribute()) {
                        input += functionName + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isArrayElement()) {
                        input += functionName + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (isSTLListBaseElement()) {
                        String first = typeVar.replaceFirst("\\b" + functionName + "\\b", newFunctionName);
                        input += first + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isInConstructor()) {
                        String first = typeVar.replaceFirst("\\b" + functionName + "\\b", newFunctionName);
                        input += first + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else {
                        String first = typeVar.replaceFirst("\\b" + functionName + "\\b", newFunctionName);
                        input += first + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;
                    }
                } else if (isPassingVariable()) {
                    String first = typeVar.replaceFirst("\\b" + functionName + "\\b", newFunctionName);
                    input += first + SpecialCharacter.END_OF_STATEMENT;
                }
            } else {
                if (selectedFunction != null) {

                    String valueVar = String.format("&%s;", selectedFunction.getSimpleName());

                    if (this.isPassingVariable()) {
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isAttribute()) {
                        input += this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isArrayElement()) {
                        input += this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (isSTLListBaseElement()) {
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isInConstructor()){
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else {
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;
                    }
                } else if (isPassingVariable() || isInConstructor()) {
                    input += typeVar + " " + getVituralName() + SpecialCharacter.END_OF_STATEMENT;
                }
            }
        }

        return input;
    }



    @Override
    public IFunctionPointerTypeNode getCorrespondingType() {
        INode typeNode = super.getCorrespondingType();
        if (typeNode instanceof IFunctionPointerTypeNode)
            return (IFunctionPointerTypeNode) typeNode;
        return null;
    }

    @Override
    public String getAssertion() {
        String actualName = getActualName();

        String output = SpecialCharacter.EMPTY;

        String assertMethod = getAssertMethod();
        if (assertMethod != null) {
            switch (assertMethod) {
                case AssertMethod.ASSERT_NULL:
                    output = assertNull(actualName);
                    break;

                case AssertMethod.ASSERT_NOT_NULL:
                    output = assertNotNull(actualName);
                    break;

                case AssertMethod.USER_CODE:
                    output = getAssertUserCode().normalize();
                    break;
            }
        }

        return output + super.getAssertion();
    }

    @Override
    public String assertNull(String name) {
        return new NullableStatementGenerator(this).assertNull(name);
    }

    @Override
    public String assertNotNull(String name) {
        return new NullableStatementGenerator(this).assertNotNull(name);
    }

    public String getActualName() {
        IFunctionPointerTypeNode typeNode = getCorrespondingType();
        String actualName;
        if (typeNode instanceof FunctionPointerTypeNode) {
            actualName = typeNode.getFunctionName();

            String expectedOutputRegex = "\\Q" + SourceConstant.EXPECTED_OUTPUT + "\\E";
            actualName = actualName.replaceFirst(expectedOutputRegex, SourceConstant.ACTUAL_OUTPUT);

            String expectedPrefixRegex = "\\Q" + SourceConstant.EXPECTED_PREFIX + "\\E";
            actualName = actualName.replaceFirst(expectedPrefixRegex, SpecialCharacter.EMPTY);

            String stubPrefixRegex = "\\Q" + SourceConstant.STUB_PREFIX + "\\E";
            actualName = actualName.replaceFirst(stubPrefixRegex, SpecialCharacter.EMPTY);

            String globalRegex = "\\Q" + SourceConstant.GLOBAL_PREFIX + "\\E";
            actualName = actualName.replaceFirst(globalRegex, SpecialCharacter.EMPTY);
        } else {
            actualName = super.getActualName();
        }

        return actualName;
    }
}
