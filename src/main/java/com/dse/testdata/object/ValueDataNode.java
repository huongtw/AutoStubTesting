package com.dse.testdata.object;

import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search2;
import com.dse.stub_manager.SystemLibrary;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testdata.Iterator;
import com.dse.testdata.comparable.*;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.testdata.object.stl.PairDataNode;
import com.dse.testdata.object.stl.STLArrayDataNode;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.UsedParameterUserCode;
import com.dse.user_code.objects.AssertUserCode;
import com.dse.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a variable node in the <b>variable tree</b>, example: class,
 * struct, array item, etc.
 *
 * @author DucAnh
 */
public abstract class ValueDataNode extends DataNode implements IValueDataNode, IUserCodeNode {

    protected AbstractUserCode userCode = null;
    protected boolean useUserCode = false;

    /**
     * The type of variable. Ex: const int&
     */
    private String rawType = SpecialCharacter.EMPTY;

    private String realType = SpecialCharacter.EMPTY;

    /**
     * The node contains the definition of type's variable. For example, the type of
     * variable is "Student". This instance returns the node that defines "Student"
     * (class Student{char* name; ...}).
     */
    private VariableNode correspondingVar = null;

    /**
     * global variable
     */
    private boolean externel = false;

    private boolean isInStaticSolution = false;

    private List<Iterator> iterators;

    private String assertMethod;

    private AssertUserCode assertUserCode;

    public ValueDataNode() {
        iterators = new ArrayList<>();
        iterators.add(new Iterator(this));
    }

    public AssertUserCode getAssertUserCode() {
        return assertUserCode;
    }

    public void setAssertUserCode(AssertUserCode assertUserCode) {
        this.assertUserCode = assertUserCode;
    }

    public INode getCorrespondingType() {
        VariableNode correspondingVar = getCorrespondingVar();

        if (correspondingVar == null)
            return null;

        return getCorrespondingVar().resolveCoreType();
    }

    public boolean haveValue() {
        for (IDataNode child : getChildren()) {
            if (child instanceof ValueDataNode && ((ValueDataNode) child).haveValue())
                return true;
        }

        return false;
    }

    public void setAssertMethod(String assertMethod) {
        this.assertMethod = assertMethod;
    }

    public String getAssertMethod() {
        return assertMethod;
    }

    @Override
    public boolean containGetterNode() {
        return this.getCorrespondingVar() != null && this.getCorrespondingVar().getGetterNode() != null;
    }

    @Override
    public boolean containSetterNode() {
        return this.getCorrespondingVar() != null && this.getCorrespondingVar().getSetterNode() != null;
    }

    @Override
    public VariableNode getCorrespondingVar() {
        return this.correspondingVar;
    }

    @Override
    public void setCorrespondingVar(VariableNode correspondingVar) {
        this.correspondingVar = correspondingVar;
    }

    @Override
    public String getDotGetterInStr() {
        StringBuilder dotAccess = new StringBuilder();
        List<IDataNode> chain = this.getNodesChainFromRoot(this);

        for (IDataNode node : chain)
            if (node instanceof ValueDataNode) {
                ValueDataNode item = (ValueDataNode) node;

                if (item.isArrayElement() || item.isPassingVariable())
                    dotAccess.append(item.getName());
                else
                    dotAccess.append(IDataNode.DOT_ACCESS).append(item.getName());
            }

        return dotAccess.toString();
    }

    @Override
    public String getDotSetterInStr(String value) {
        return this.getDotGetterInStr() + "=" + value;
    }

    @Override
    public String getGetterInStr() {
        final String METHOD = "()";

        StringBuilder getter = new StringBuilder();
        List<IDataNode> chain = this.getNodesChainFromRoot(this);

        for (IDataNode node : chain)
            if (node instanceof ValueDataNode) {
                ValueDataNode item = (ValueDataNode) node;

                if (item.isArrayElement() || item.isPassingVariable())
                    getter.append(item.getName());
                else if (item.containGetterNode())
                    getter.append(IDataNode.DOT_ACCESS)
                            .append(item.getCorrespondingVar().getGetterNode().getSingleSimpleName())
                            .append(METHOD);
                else if (!item.containGetterNode())
                    if (item.getCorrespondingVar().isPrivate())
                        getter.append(IDataNode.GETTER_METHOD)
                                .append(Utils.toUpperFirstCharacter(item.getName() + METHOD));
                    else
                        getter.append(IDataNode.DOT_ACCESS).append(item.getName());
            }

        return getter.toString();
    }

    protected String getExportExeResultStm(String actualName, String expectedName) {
        return String.format(DriverConstant.ASSERT + "(\"%s\", %s, \"%s\", %s);",
                actualName, actualName, expectedName, expectedName);
    }

    @Override
    public String getAssertion() {
        StringBuilder assertion = new StringBuilder();

        for (IDataNode child : this.getChildren()) {
            if (child instanceof ValueDataNode) {
                String childAssertion = ((ValueDataNode) child).getAssertion() + SpecialCharacter.LINE_BREAK;
                assertion.append(childAssertion);
            }
        }

        return assertion.toString();
    }

    protected String getActualName() {
        String actualName = getVituralName();

        String expectedOutputRegex = "\\Q" + SourceConstant.EXPECTED_OUTPUT + "\\E";
        actualName = actualName.replaceFirst(expectedOutputRegex, SourceConstant.ACTUAL_OUTPUT);

        String expectedPrefixRegex = "\\Q" + SourceConstant.EXPECTED_PREFIX + "\\E";
        actualName = actualName.replaceFirst(expectedPrefixRegex, SpecialCharacter.EMPTY);

        String stubPrefixRegex = "\\Q" + SourceConstant.STUB_PREFIX + "\\E";
        actualName = actualName.replaceFirst(stubPrefixRegex, SpecialCharacter.EMPTY);

        String globalRegex = "\\Q" + SourceConstant.GLOBAL_PREFIX + "\\E";
        actualName = actualName.replaceFirst(globalRegex, SpecialCharacter.EMPTY);

        return actualName;
    }

//    @Override
//    public String getAssertionForGoogleTest(String method, String source, String target) throws Exception {
////		if (!getVituralName().startsWith(IGTestConstant.EXPECTED_OUTPUT))
////			throw new Exception("Only expected output value can assert");
//
//        String output = "";
//
//        if (this instanceof ConstructorDataNode)
//            return output;
//
//        for (IDataNode child : this.getChildren()) {
//            if (child instanceof ValueDataNode)
//                output += ((ValueDataNode) child).getAssertionForGoogleTest(method, source, target)
//                        + SpecialCharacter.LINE_BREAK;
//        }
//
//        output = output.replace(SpecialCharacter.LINE_BREAK + SpecialCharacter.LINE_BREAK, SpecialCharacter.LINE_BREAK);
//        output = output.replace(";;", ";");
//
//        return output + SpecialCharacter.LINE_BREAK;
//    }

    @Override
    public String getSetterInStr(String nameVar) {
        String setter = "";
        List<IDataNode> chain = this.getNodesChainFromRoot(this);

        /*
         * Get the getter of the previous variable.For example, we have "front[0]" and
         * we need to get the setter of this variable.
         *
         * The first step, we get the getter of variable "front".
         */
        String getterOfPreviousNode = "";

        final int MIN_ELEMENTS_IN_CHAIN = 2;

        if (chain.size() >= MIN_ELEMENTS_IN_CHAIN) {
            /*
             * If the variable belongs is array item, belongs to class/struct/namespace,
             * etc., the the size of chain is greater than 2
             */
            IDataNode previousNode = chain.get(chain.size() - 2);
            if (previousNode instanceof ValueDataNode)
                getterOfPreviousNode = ((ValueDataNode) previousNode).getGetterInStr();
        } else {
            // nothing to do
        }
        /*
         *
         */
        IDataNode currentNode = chain.get(chain.size() - 1);
        if (currentNode instanceof ValueDataNode) {
            ValueDataNode dataNode = (ValueDataNode) currentNode;

            if (dataNode.isArrayElement())
                return this.getParent().getVituralName() + dataNode.getName() + "=" + nameVar;
            else if (dataNode.isPassingVariable())
                setter = getterOfPreviousNode + dataNode.getName() + "=" + nameVar;
            else if (dataNode.containSetterNode())
                setter = getterOfPreviousNode + IDataNode.DOT_ACCESS
                        + dataNode.getCorrespondingVar().getSetterNode().getSingleSimpleName() + "(" + nameVar + ")";
            else if (!dataNode.containSetterNode())
                if (dataNode.getCorrespondingVar().isPrivate())
                    setter = getterOfPreviousNode + IDataNode.SETTER_METHOD
                            + Utils.toUpperFirstCharacter(dataNode.getName() + "(" + nameVar + ")");
                else if (dataNode instanceof OneDimensionCharacterDataNode) {
                    String name = getterOfPreviousNode + IDataNode.DOT_ACCESS + dataNode.getName();
                    setter = "strcpy(" + name + "," + nameVar + ")";

                } else
                    setter = getterOfPreviousNode + IDataNode.DOT_ACCESS + dataNode.getName() + "=" + nameVar;
        }

        return setter;
    }

    @Override
    public String getRawType() {
        return this.rawType;
    }

    @Override
    public void setRawType(String rawType) {
        rawType = VariableTypeUtils.deleteStorageClassesExceptConst(rawType);
        this.rawType = rawType;
    }

    @Override
    public String getRealType() {
        return realType;
    }

    @Override
    public void setRealType(String realType) {
        realType = VariableTypeUtils.deleteStorageClassesExceptConst(realType);
        this.realType = realType;
    }

    public boolean isExternel() {
        return externel;
    }

    public void setExternel(boolean _externelVariable) {
        externel = _externelVariable;
    }

    @Override
    public boolean isArrayElement() {
        IDataNode parent = getParent();

        if (!(parent instanceof ValueDataNode))
            return false;

        if (parent instanceof ArrayDataNode || parent instanceof PointerDataNode || parent instanceof STLArrayDataNode)
            return true;

        if (this instanceof SubClassDataNode && parent instanceof ClassDataNode)
            return ((ClassDataNode) parent).isArrayElement();

        return false;
    }

    @Override
    public boolean isElementInString() {
        IDataNode parent = getParent();

        if (!(parent instanceof ValueDataNode))
            return false;

        if (parent instanceof NormalStringDataNode)
            return true;

        return false;
    }

    @Override
    public boolean isSTLListBaseElement() {
        return this.getParent() != null && this.getParent() instanceof ListBaseDataNode;
    }

    @Override
    public boolean isAttribute() {
        if (this instanceof SubClassDataNode) {
            return getParent().getParent() instanceof StructureDataNode || getParent().getParent() instanceof PairDataNode;
        } else if (this instanceof SubprogramNode) {
            if (this instanceof ConstructorDataNode)
                return getParent().getParent().getParent() instanceof StructureDataNode
                        || getParent().getParent().getParent() instanceof PairDataNode;
            else
                return false;
        } else
            return this.getParent() instanceof StructureDataNode || getParent() instanceof PairDataNode;
    }

    public boolean isVoidPointerValue() {
        return getParent() instanceof VoidPointerDataNode;
    }

    public boolean isExpected() {
        IDataNode parent = getParent();

        if (parent == null)
            return false;

        if (!(parent instanceof ValueDataNode))
            return false;

        if (this instanceof SubprogramNode && !(this instanceof ConstructorDataNode))
            return false;

        if (parent instanceof SubprogramNode && !(parent instanceof ConstructorDataNode)) {
            IDataNode grandParent = parent.getParent();

            boolean isReturnVar = getName().equals("RETURN");

            // case test function in sbf unit (<<SBF>>) or stub subprogram (<<STUB>>)
//            if (grandParent instanceof RootDataNode)
//                return !isReturnVar;
            if (parent instanceof IterationSubprogramNode) {
                return !isReturnVar;
            }
            if (grandParent instanceof UnitNode) {
                boolean isStubUnit = grandParent instanceof StubUnitNode;
                return ((isStubUnit && !isReturnVar) || (!isStubUnit && isReturnVar));
//                        && Search2.getExpectedValue(this) != null;
            }
        }

        return ((ValueDataNode) parent).isExpected();
    }

    @Override
    public boolean isInConstructor() {
        return this.getParent() instanceof ConstructorDataNode;
    }

    @Override
    public boolean isInStaticSolution() {
        return this.isInStaticSolution;
    }

    @Override
    public void setInStaticSolution(boolean isInStaticSolution) {
        this.isInStaticSolution = isInStaticSolution;
    }

    @Override
    public boolean isPassingVariable() {
        return this.getParent() != null && !isSutExpectedValue()
                && (/*this.getParent() instanceof RootDataNode || */this.getParent() instanceof SubprogramNode)
                && !(this.getParent() instanceof ConstructorDataNode);
    }

    public boolean isStubArgument() {
        if (this instanceof SubprogramNode)
            return false;

        IDataNode ancestor = parent.getParent();
        ancestor = ancestor.getParent();
        ancestor = ancestor.getParent();

        if (ancestor instanceof StubUnitNode)
            return true;

        if (ancestor instanceof RootDataNode) {
            NodeType rootType = ((RootDataNode) ancestor).getLevel();

            if (rootType == NodeType.STUB || rootType == NodeType.SBF)
                return true;
        }

        return false;
    }

    public boolean isInstance() {
        if (isGlobalExpectedValue())
            return false;

        if (this instanceof ClassDataNode || this instanceof StructDataNode)
            if (correspondingVar instanceof InstanceVariableNode)
                return true;

        if (this instanceof SubClassDataNode) {
            return ((ClassDataNode) getParent()).isInstance();
        }

        return false;
    }

    protected String getUserCodeContent() {
        if (userCode instanceof UsedParameterUserCode) {
            UsedParameterUserCode usedUserCode = (UsedParameterUserCode) userCode;
            if (usedUserCode.getType().equals(UsedParameterUserCode.TYPE_REFERENCE)) {
                ParameterUserCode reference = UserCodeManager.getInstance().getParamUserCodeById(userCode.getId());
                return reference.getContent() + SpecialCharacter.LINE_BREAK;
            }
        }

        return userCode.getContent() + SpecialCharacter.LINE_BREAK;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        return super.getInputForGoogleTest(isDeclared);
    }

    public boolean isSupportUserCode() {
        if (this instanceof SubprogramNode)
            return false;
        return true;
    }

    public boolean isHaveExpectedValue() {
        // subprogram under test case
        if (this instanceof SubprogramNode && !(this instanceof ConstructorDataNode)) {
            IDataNode parent = getParent();
            IDataNode grandParent = parent.getParent();

            return parent instanceof StubUnitNode
                    && grandParent instanceof RootDataNode && ((RootDataNode) grandParent).getLevel() == NodeType.ROOT;
        }

        if (getCorrespondingVar() instanceof ReturnVariableNode || !(getParent() instanceof ValueDataNode))
            return false;

        return ((ValueDataNode) getParent()).isHaveExpectedValue();
    }

    public void setVirtualName() {
        if (this.virtualName != null)
            return;

        String virtualName;
        IDataNode parent = getParent();

        if (isExternel()) {
            IVariableNode variable = getCorrespondingVar();
            if (variable instanceof StaticVariableNode)
                virtualName = ((StaticVariableNode) variable).getInstrument();
            else if (variable instanceof InstanceVariableNode)
                virtualName = getName();
            else
                virtualName = getDisplayNameInParameterTree();
        }
        // parameter case
        else if (isPassingVariable()) {
            virtualName = getName();
        }
        // subprogram case
        else if (this instanceof SubprogramNode) {
            virtualName = parent.getVituralName();
        }
        // subclass data node
        else if (this instanceof SubClassDataNode) {
            virtualName = parent.getVituralName();
        }
        else if (this instanceof NumberOfCallNode) {
            virtualName = "NONE_VALUE";
        }
        // virtual name depend on parent's virtual name
        else if (isArrayElement() || isElementInString()) {
            String elementIndex = VariableTypeUtils.getElementIndex(getName());

            String parentVirtualName = parent.getVituralName();

            if (parent instanceof ValueDataNode && ((ValueDataNode) parent).isArrayElement())
                parentVirtualName = parentVirtualName
                        .substring(0, parentVirtualName.lastIndexOf(SpecialCharacter.OPEN_SQUARE_BRACE));

            virtualName = parentVirtualName + elementIndex;
        } else if (isAttribute()) {
            virtualName = parent.getVituralName() + SpecialCharacter.DOT + getName();

            if (parent.getVituralName().startsWith(SourceConstant.INSTANCE_VARIABLE))
                virtualName = parent.getVituralName() + SpecialCharacter.POINT_TO + getName();
        }
//		else if (getParent() instanceof VoidPointerDataNode) {
//            String parentPrefix = parent.getVituralName();
//            virtualName = parentPrefix + SpecialCharacter.UNDERSCORE_CHAR + getName();
//        }
        // other data node
        else {
            String parentPrefix = parent.getVituralName();
            virtualName = getName();

            if (!parentPrefix.equals(NON_VALUE))
                virtualName = parentPrefix + SpecialCharacter.UNDERSCORE_CHAR + virtualName;

            virtualName = virtualName.replace(SpecialCharacter.DOT, SpecialCharacter.UNDERSCORE_CHAR);
            virtualName = virtualName.replaceAll("[^\\w_]", SpecialCharacter.EMPTY);
        }

        // expected output
        if (name.equals("RETURN")) {
            UnitNode unit = (UnitNode) getUnit();
            if (unit != null && !(unit instanceof StubUnitNode))
                virtualName = SourceConstant.EXPECTED_OUTPUT;
        }

        if (isStubArgument()) {
//            String normalizeSubprogramName = parent.getDisplayNameInParameterTree()
//                    .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);
            virtualName = SourceConstant.STUB_PREFIX /*+ normalizeSubprogramName*/ + virtualName;
        }

        if (isSutExpectedArgument()) {
            VariableNode v = getCorrespondingVar();
            if (v instanceof StaticVariableNode) {
                StaticVariableNode staticVar = (StaticVariableNode) v;
                virtualName = SourceConstant.EXPECTED_PREFIX + staticVar.getInstrument();
            } else
                virtualName = SourceConstant.EXPECTED_PREFIX + virtualName;
        } else if (isGlobalExpectedValue()) {
            virtualName = SourceConstant.EXPECTED_PREFIX + SourceConstant.GLOBAL_PREFIX + virtualName;
        }

        if (this instanceof NullPointerDataNode)
            virtualName = "nullptr_t";

        setVituralName(virtualName);
    }

    public boolean isGlobalExpectedValue() {
        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(getTestCaseRoot());

        assert globalRoot != null;
        return (globalRoot.getGlobalInputExpOutputMap().containsValue(this));
    }

    public boolean isSutExpectedValue() {
        IDataNode parent = getParent();

        if (!(parent instanceof ValueDataNode))
            return false;

        if (parent instanceof SubprogramNode) {
            SubprogramNode sut = Search2
                    .findSubprogramUnderTest(((SubprogramNode) parent).getTestCaseRoot());

            if (sut == parent) {
                if (sut.getParamExpectedOuputs().contains(this))
                    return true;
            }
        }

        return ((ValueDataNode) parent).isSutExpectedValue();
    }

    public boolean isSutExpectedArgument() {
//        IDataNode parent = getParent();
//
//        if (!(parent instanceof ValueDataNode))
//            return false;
//
//        if (parent instanceof SubprogramNode) {
            SubprogramNode sut = Search2.findSubprogramUnderTest(getTestCaseRoot());
//
            if (sut != null) {
                if (sut.getParamExpectedOuputs().contains(this))
                    return true;
            }
//        }

        return false;
    }

    public String getDisplayNameInParameterTree() {
        String prefixPath = null;

        Iterator firstIterator = iterators.get(0);

        if (getName().startsWith(SourceConstant.INSTANCE_VARIABLE))
            return getRawType() + " Instance";

        prefixPath = getName() + "";

        INode originalVar = getCorrespondingVar();

        if (originalVar instanceof ReturnVariableNode) {
            prefixPath = "return";
        }

        if (originalVar instanceof ExternalVariableNode) {
            INode currentVar = originalVar.getParent();

            while ((currentVar instanceof StructureNode || currentVar instanceof NamespaceNode)) {
                prefixPath = currentVar.getNewType() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + prefixPath;
                currentVar = currentVar.getParent();
            }
        }

        for (int i = 0; i < iterators.size(); i++) {
            Iterator iterator = iterators.get(i);

            if (iterator.getDataNode() == this) {
                if (i != 0 || iterator.getRepeat() != Iterator.FILL_ALL) {
                    prefixPath += String.format(" [%s]", iterator.getDisplayName());
                }

                break;
            }
        }

        return prefixPath;
    }

    public List<Iterator> getIterators() {
        return iterators;
    }

    public void setIterators(List<Iterator> iterators) {
        this.iterators = iterators;
    }

    // public Iterator getCorrespondingIterator() {
    //     if (isStubArgument()) {
    //         return iterators.stream().filter(i -> i.getDataNode() == this).findFirst().orElse(null);
    //     } else {
    //         if (parent instanceof ValueDataNode) {
    //             return ((ValueDataNode) parent).getCorrespondingIterator();
    //         } else
    //             return null;
    //     }
    // }

    public String[] getAllSupportedAssertMethod() {
        List<String> supportedMethod = new ArrayList<>();
        supportedMethod.add(SpecialCharacter.EMPTY);
        if (this instanceof IEqualityComparable) {
            supportedMethod.add(AssertMethod.ASSERT_EQUAL);
            supportedMethod.add(AssertMethod.ASSERT_NOT_EQUAL);
        }
        if (this instanceof IValueComparable) {
            supportedMethod.add(AssertMethod.ASSERT_LOWER);
            supportedMethod.add(AssertMethod.ASSERT_GREATER);
            supportedMethod.add(AssertMethod.ASSERT_LOWER_OR_EQUAL);
            supportedMethod.add(AssertMethod.ASSERT_GREATER_OR_EQUAL);
        }
        if (this instanceof IBooleanComparable) {
            supportedMethod.add(AssertMethod.ASSERT_TRUE);
            supportedMethod.add(AssertMethod.ASSERT_FALSE);
        }
        if (this instanceof INullableComparable) {
            supportedMethod.add(AssertMethod.ASSERT_NULL);
            supportedMethod.add(AssertMethod.ASSERT_NOT_NULL);
        }

        supportedMethod.add(AssertMethod.USER_CODE);

        return supportedMethod.toArray(new String[0]);
    }

    @Override
    public ValueDataNode clone() {
        ValueDataNode clone = null;

        try {
            clone = getClass().newInstance();
            clone.setName(getName() + "");
            clone.setParent(getParent());
            clone.setRawType(getRawType() + "");
            clone.setRealType(getRealType() + "");
            clone.setCorrespondingVar(getCorrespondingVar());
            clone.setExternel(isExternel());
            clone.setInStaticSolution(isInStaticSolution());
            clone.setIterators(iterators);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return clone;
    }

    // tmp implement IUserCodeNode
    public String getContextPath() {
        UnitNode unitNode = getUnit();
        String filePath;

        if (unitNode != null) {
            filePath = unitNode.getSourceNode().getAbsolutePath();
        } else {
            String[] pathItems = getCorrespondingVar().getAbsolutePath().split(File.separator);
            filePath = pathItems[1];
        }

        return filePath;
    }

    /**
     * @return temporary file path where archive user code file
     */
    public String getTemporaryPath() {
        String filePath;
        String temporaryPath;

        UnitNode unitNode = getUnit();
        if (unitNode != null) {
            filePath = unitNode.getSourceNode().getAbsolutePath();
            temporaryPath = ProjectClone.getClonedFilePath(filePath);
        } else {
            String[] pathItems = getCorrespondingVar().getAbsolutePath().split(File.separator);
            filePath = pathItems[1];
            temporaryPath = SystemLibrary.getLibrariesDirectory() + filePath + SystemLibrary.LIBRARY_EXTENSION;
        }

        int lastSeparator = temporaryPath.lastIndexOf(File.separator) + 1;
        temporaryPath = temporaryPath.substring(0, lastSeparator) + "temporary";

        INode srcNode = Utils.getSourcecodeFile(getTestCaseRoot().getFunctionNode());
        String fileName = srcNode.getName();
        String ext = fileName.substring(fileName.lastIndexOf(SpecialCharacter.DOT));
        temporaryPath += ext;

        return temporaryPath;
    }

    /**
     * @return initial user code with only declaration
     */
    public String generateInitialUserCode() {
        String input = "";

        String typeVar = getRawType();

        if (isExternel())
            typeVar = "";

        // generate the statement
        if (this.isPassingVariable()) {
            input += typeVar + " " + getVituralName() + " = ";

        } else if (this.isAttribute()) {
            input += getVituralName() + " = ";

        } else if (this.isArrayElement()) {
            input += getVituralName() + " = ";

        } else if (isSTLListBaseElement()) {
            input += typeVar + " " + getVituralName() + " = ";

        } else if (this.isInConstructor()) {
            input += typeVar + " " + getVituralName() + " = ";

        } else if (this.isSutExpectedValue()) {
            input += typeVar + " " + getVituralName() + " = ";

        } else if (this.isVoidPointerValue()) {
            input += typeVar + " " + getVituralName() + " = ";

        }

        return input;
    }

    public void setUserCode(AbstractUserCode userCode) {
        this.userCode = userCode;
    }

    public AbstractUserCode getUserCode() {
        if (userCode == null) {
            userCode = new UsedParameterUserCode();
            ((UsedParameterUserCode) userCode).setType(UsedParameterUserCode.TYPE_CODE);
            userCode.setContent(generateInitialUserCode() + DEFAULT_USER_CODE);
        }

        return userCode;
    }

    public void setUseUserCode(boolean useUserCode) {
        this.useUserCode = useUserCode;
        if (!useUserCode) {
            userCode = null;
        }
    }

    public boolean isUseUserCode() {
        return useUserCode;
    }

    public String getUserCodeDisplayText() {
        if (isUseUserCode()) return "USER CODE";
        return "";
    }
    // tmp implement IUserCodeNode
}
