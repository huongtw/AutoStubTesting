package com.dse.testdata.object;

import com.dse.parser.object.VariableNode;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

/**
 * Represent variable as one dimension array.
 *
 * @author ducanhnguyen
 */
public abstract class OneDimensionDataNode extends ArrayDataNode {
	/**
	 * The size of array
	 */
	private int size = UNDEFINED_SIZE; // unspecified size

	public boolean canConvertToString() {
		return false;
	}

	public int getSize() {
		return this.size;
	}

	@Override
	public boolean haveValue() {
		if (size <= 0)
			return false;

		return super.haveValue();
	}

	@Override
	public void setCorrespondingVar(VariableNode correspondingVar) {
		super.setCorrespondingVar(correspondingVar);
		if (VariableTypeUtils.isOneDimension(correspondingVar.getRawType()))
			size = correspondingVar.getSizeOfArray();
	}

	public void setSize(int size) {
		if (size >= 0) {
			setSizeIsSet(true);
		}
		this.size = size;
	}

	@Override
	public String generateInputToSavedInFile() {
		StringBuilder output = new StringBuilder();
		for (IDataNode child : getChildren())
			if (child instanceof NormalDataNode)
				if (((NormalDataNode) child).getValue() != null) {
					output.append(child.getName()).append("=").append(((NormalDataNode) child).getValue()).append(SpecialCharacter.LINE_BREAK);
				}
		output.append("sizeof(").append(getName()).append(")=").append(getSize()).append(SpecialCharacter.LINE_BREAK);
		return output.toString();
	}

	/**
	 * Example:
	 * int[3] ---> true
	 *
	 * int[] ---> false
	 * @return
	 */
	public boolean isConstrainedArray(){
		return !getRawType().contains("[]");
	}

	@Override
	public OneDimensionDataNode clone() {
		OneDimensionDataNode clone = (OneDimensionDataNode) super.clone();

		if (isFixedSize()) {
			clone.size = size;
			try {
				new TreeExpander().expandTree(clone);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return clone;
	}
}
