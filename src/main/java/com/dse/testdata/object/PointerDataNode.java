package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.testdata.comparable.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;

/**
 * Represent variable as pointer (one level, two level, etc.)
 *
 * @author ducanhnguyen
 */
public abstract class PointerDataNode extends ValueDataNode implements INullableComparable {
	public static final int NULL_VALUE = -1;

	protected int level;

	/**
	 * The allocated size, including '\0'.
	 *
	 * Ex1: node="xyz" ---> allocatedSize = 4 <br/>
	 * Ex2: node="" ---> allocatedSize = 1
	 */
	private int allocatedSize;

	private boolean sizeIsSet = false;

	public boolean isSetSize() {
		return sizeIsSet;
	}

	public void setSizeIsSet(boolean sizeIsSet) {
		this.sizeIsSet = sizeIsSet;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAllocatedSize() {
		return this.allocatedSize;
	}

	public void setAllocatedSize(int allocatedSize) {
		this.allocatedSize = allocatedSize;
	}

	public boolean isNotNull() {
		return this.allocatedSize >= 1;
	}

	@Override
	public String generareSourcecodetoReadInputFromFile() throws Exception {
		StringBuilder output = new StringBuilder();
		for (IDataNode child : this.getChildren())
			output.append(child.generareSourcecodetoReadInputFromFile());
		return output.toString();
	}

	@Override
	public String getInputForGoogleTest(boolean isDeclared) throws Exception {
		if (isUseUserCode()) {
			if (isPassingVariable() && !isDeclared)
				return SpecialCharacter.EMPTY;
			else
				return getUserCodeContent();
		}

		if (Environment.getInstance().isC())
			return getCInput(isDeclared);
		else
			return getCppInput(isDeclared);
	}

	private String getCInput(boolean isDeclared) throws Exception {
		String input = "";
		if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
			String type = VariableTypeUtils
					.deleteStorageClassesExceptConst(getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));

			String coreType = "";
			if (getChildren() != null && !getChildren().isEmpty())
				coreType = ((ValueDataNode) getChildren().get(0)).getRawType();
			else {
				int index = type.lastIndexOf('*');
				if (index < 0) {
					String realType = getRealType();
					index = realType.lastIndexOf('*');
					if (index >= 0) {
						coreType = realType.substring(0, index).trim();
					}
				} else {
					coreType = type.substring(0, index).trim();
				}
			}

			if (this instanceof PointerStructureDataNode) {
				type = type.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);
				coreType = coreType.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);
			}

			if (isExternel())
				type = "";

			if (isPassingVariable() || isSTLListBaseElement() || isInConstructor() || isGlobalExpectedValue() || isSutExpectedArgument()) {
				String allocation = "";

				if (this.isNotNull())
					allocation = String.format("%s %s = malloc(%d * sizeof(%s))" + SpecialCharacter.END_OF_STATEMENT,
							type, this.getVituralName(), this.getAllocatedSize(), coreType);
				else {
					allocation = String.format("%s %s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT,
							type, this.getVituralName());
				}
				input += allocation;
			} else if (isArrayElement() || isAttribute()) {
				String allocation;

				if (this.isNotNull())
					allocation = String.format("%s = malloc(%d * sizeof(%s))" + SpecialCharacter.END_OF_STATEMENT,
							this.getVituralName(), this.getAllocatedSize(), coreType);
				else
					allocation = String.format("%s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT
							, this.getVituralName());
				input += allocation;
			} else if (isVoidPointerValue()) {
				String allocation = "";

				if (this.isNotNull())
					allocation = String.format("%s %s = malloc(%d * sizeof(%s))" + SpecialCharacter.END_OF_STATEMENT,
							type, this.getVituralName(), this.getAllocatedSize(), coreType);
				else {
					allocation = String.format("%s %s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT,
							type, this.getVituralName());
				}
				input += allocation;
			} else {
				if (this.isNotNull())
					input = String.format("%s = malloc(%d * sizeof(%s))" + SpecialCharacter.END_OF_STATEMENT,
							this.getVituralName(), this.getAllocatedSize(), coreType);
				else
					input += String.format("%s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT
							, this.getVituralName());
			}
		}

		return input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
	}

	private String getCppInput(boolean isDeclared) throws Exception {
		String input = "";
		if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
			String type = VariableTypeUtils
					.deleteStorageClassesExceptConst(getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));

			String coreType = "";
			if (getChildren() != null && !getChildren().isEmpty())
				coreType = ((ValueDataNode) getChildren().get(0)).getRawType();
			else
				coreType = type.substring(0, type.lastIndexOf('*'));

			if (isExternel())
				type = "";

			String name = getVituralName();
			int size = getAllocatedSize();

			if (isPassingVariable() || isSTLListBaseElement() || isInConstructor() || isGlobalExpectedValue() || isSutExpectedArgument()) {
				String allocation;

				if (this.isNotNull()) {
					allocation = String.format("%s %s = (%s) malloc(%d * sizeof(%s));", type, name, type, size, coreType);
				} else {
					allocation = String.format("%s %s = "
									+ IDataNode.NULL_POINTER_IN_CPP
									+ SpecialCharacter.END_OF_STATEMENT
							, type, name);
				}
				input += allocation;
			} else if (isArrayElement() || isAttribute()) {
				String allocation;

				if (this.isNotNull())
					allocation = String.format("%s = (%s) malloc(%d * sizeof(%s));", name, type, size, coreType);
				else
					allocation = String.format("%s = "
									+ IDataNode.NULL_POINTER_IN_CPP
									+ SpecialCharacter.END_OF_STATEMENT
							, name);
				input += allocation;
			} else {
				if (this.isNotNull())
					input += String.format("%s = (%s) malloc(%d * sizeof(%s));", name, type, size, coreType);
				else
					input += name + " = " + IDataNode.NULL_POINTER_IN_CPP + SpecialCharacter.END_OF_STATEMENT;
			}
		}

		return input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
	}

	@Override
	public String assertNull(String name) {
		return new NullableStatementGenerator(this).assertNull(name);
	}

	@Override
	public String assertNotNull(String name) {
		return new NullableStatementGenerator(this).assertNotNull(name);
	}

	@Override
	public String getAssertion() {
		if (isVoidPointerValue()) {
			return SpecialCharacter.EMPTY;
		}

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
	public PointerDataNode clone() {
		PointerDataNode clone = (PointerDataNode) super.clone();
		clone.level = level;
		return clone;
	}

}
