package com.dse.testdata.object;

import com.dse.testdata.comparable.*;
import com.dse.util.SpecialCharacter;

/**
 * Represent variable as array (one dimension, two dimension, etc.)
 *
 * @author ducanhnguyen
 */
public abstract class ArrayDataNode extends ValueDataNode implements INullableComparable {
	public static final int UNDEFINED_SIZE = -1;
	//Hoan
	private boolean sizeIsSet = false;
	private boolean isFixedSize = false;

	public void setSizeIsSet(boolean sizeIsSet) {
		this.sizeIsSet = sizeIsSet;
	}

	public boolean isFixedSize() {
		return isFixedSize;
	}
	public boolean isSetSize() {
		return sizeIsSet;
	}

	public void setFixedSize(boolean fixedSize) {
		isFixedSize = fixedSize;
	}

	@Override
	public String generareSourcecodetoReadInputFromFile() throws Exception {
		return "";
	}

	@Override
	public ArrayDataNode clone() {
		ArrayDataNode clone = (ArrayDataNode) super.clone();
		clone.isFixedSize = isFixedSize;

		if (isFixedSize)
			clone.sizeIsSet = sizeIsSet;

		return clone;
	}

//	@Override
//	public String assertEqual(String source, String target) {
//		return new ValueStatementGenerator(this).assertEqual(source, target);
//	}
//
//	@Override
//	public String assertNotEqual(String source, String target) {
//		return new ValueStatementGenerator(this).assertNotEqual(source, target);
//	}

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
}
