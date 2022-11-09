package com.dse.testdata.object;

import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.VariableNode;

public interface IValueDataNode extends IDataNode {
    /**
     * Check whether the corresponding variable has getter or not
     *
     * @return
     */
    boolean containGetterNode();

    /**
     * Check whether the corresponding variable has setter or not
     *
     * @return
//     */
    boolean containSetterNode();

    IVariableNode getCorrespondingVar();

    void setCorrespondingVar(VariableNode correspondingVar);

    /**
     * Get the getter of element in string. <br/>
     * Ex1: "front[0].N" <br/>
     * Ex2: "front[0]"
     *
     * @return
     */
    String getDotGetterInStr();

    /**
     * Get the setter of element in string. <br/>
     * Ex1: "front[0].N=NULL" <br/>
     * Ex2: "x=2"
     *
     * @param value
     *            the value of variable.
     * @return
     */
    String getDotSetterInStr(String value);

    /**
     * Ex: "front[0].N"
     *
     * @return
     */
    String getGetterInStr();

    /**
     * Ex: front.setName(age);
     *
     * @param nameVar
     *            Ex: age
     * @return the string contains the setter of the current node
     */
    String getSetterInStr(String nameVar);

    /**
     * Check whether the node is array item or not
     *
     * @return true if the current node is array item
     */
    boolean isArrayElement();

    boolean isElementInString();

    boolean isSTLListBaseElement();

    boolean isVoidPointerValue();

    /**
     * Check whether the node is attribute or not (element of
     * class/struct/enum/union/etc.)
     *
     * @return
     */
    boolean isAttribute();

    boolean isStubArgument();

    /**
     * Check whether the node is passing variable or not
     *
     * @return
     */
    boolean isPassingVariable();

    boolean isExternel();

    void setExternel(boolean _externelVariable);

    boolean isInConstructor();

    /**
     * Check whether the node has value in static solution or not. For example, the
     * static solution is "a=1; b=2;". In the variable tree, we have a node named
     * "a". Therefore, this node return true.
     *
     * @return
     */
    boolean isInStaticSolution();

    void setInStaticSolution(boolean isInStaticSolution);

//    /**
//     * Get the string used to put in google test file
//     *
//     * @return
//     * @throws Exception
//     */
//    String getInputForGoogleTest() throws Exception;
//
//    String getAssertionForGoogleTest(String method, String source, String target) throws Exception;

    String getAssertion();

    String getRawType();

    void setRawType(String rawType);

    String getRealType();

    void setRealType(String realType);
}
