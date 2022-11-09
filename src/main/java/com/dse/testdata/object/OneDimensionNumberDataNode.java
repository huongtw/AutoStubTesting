package com.dse.testdata.object;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OneDimensionNumberDataNode extends OneDimensionDataNode {

	@Override
	public String getInputForDisplay() throws Exception {

		String input;
		String initialization = "";
		String declaration;

		if (this.canConvertToString()) {
			declaration = String.format("%s=", this.getVituralName());

			Map<Integer, String> values = new TreeMap<>();
			for (IDataNode child : this.getChildren()) {
				NormalDataNode nChild = (NormalDataNode) child;

				String index = Utils.getIndexOfArray(nChild.getName()).get(0);
				values.put(Utils.toInt(index), nChild.getValue());
			}

			for (Integer key : values.keySet()) {
				int value = Utils.toInt(values.get(key));
				initialization += value + ",";
			}
			input = declaration + "={" + initialization + "}" + SpecialCharacter.LINE_BREAK;
			input = input.replace(",}", "}");

		} else {
			for (IDataNode child : this.getChildren())
				initialization += child.getInputForGoogleTest(false);
			input = initialization;
		}
		if (this.isAttribute())
			input += this.getDotSetterInStr(this.getVituralName()) + SpecialCharacter.LINE_BREAK;
		return input;
	}

	@Override
	public String getInputForGoogleTest(boolean isDeclared) throws Exception {
		String declaration ="";

		if (isUseUserCode()) {
			if (isPassingVariable() && !isDeclared)
				return SpecialCharacter.EMPTY;
			else
				return getUserCodeContent();
		}

		if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
			declaration = SpecialCharacter.LINE_BREAK;

			String type = VariableTypeUtils
					.deleteStorageClassesExceptConst(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
			String coreType = type.replaceAll("\\[.*\\]", "");
			if (!isExternel()) {
				List<String> indexes = Utils.getIndexOfArray(type);

				if (indexes.size() > 0) {
					String dimension = "";
					for (String index : indexes)
						if (index.length() == 0)
							dimension += Utils.asIndex(this.getSize());
						else
							dimension += Utils.asIndex(index);

					if (this.getParent() instanceof StructureDataNode)
						declaration = "";
					else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
//						String firstIndex = indexes.get(0);
						if (getSize() > 0) {
							declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
									this.getVituralName(), dimension);
						} else {
							declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
									this.getVituralName());
						}
					} else if (isVoidPointerValue()) {
						declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
								this.getVituralName(), dimension);
					} else if (getSize() > 0) {
						declaration = String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
								this.getVituralName(), dimension);
					} else {
						declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
								this.getVituralName());
					}
				} else if (this.getParent() instanceof StructureDataNode) {
					declaration = "";

				} else if (getSize() > 0) {
					declaration = String.format("%s %s[%s]" + SpecialCharacter.END_OF_STATEMENT, coreType,
							this.getVituralName(), this.getSize());
				} else {
					declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
							this.getVituralName());
				}
				declaration += SpecialCharacter.END_OF_STATEMENT;
			} else {
				declaration += "\n/* is global variable */\n";
			}
		}

		return declaration + super.getInputForGoogleTest(isDeclared);
	}

	@Override
	public String generareSourcecodetoReadInputFromFile() throws Exception {
		if (getParent() instanceof RootDataNode) {
			String typeVar = VariableTypeUtils.deleteStorageClasses(this.getRawType())
					.replace(IDataNode.REFERENCE_OPERATOR, "")
					.replace(IDataNode.ONE_LEVEL_POINTER_OPERATOR, "");
			typeVar = typeVar.substring(0, typeVar.indexOf("["));

			String loadValueStm = "data.findOneDimensionOrLevelBasicByName<" + typeVar + ">(\"" + getVituralName()
					+ "\", DEFAULT_VALUE_FOR_NUMBER)";

			String fullStm = typeVar + "* " + this.getVituralName() + "=" + loadValueStm
					+ SpecialCharacter.END_OF_STATEMENT;
			return fullStm;
		} else {
			// belong to structure node
			// Handle later;
			return "";
		}
	}
}
