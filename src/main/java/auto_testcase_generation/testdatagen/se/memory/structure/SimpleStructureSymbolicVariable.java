package auto_testcase_generation.testdatagen.se.memory.structure;

import java.util.ArrayList;
import java.util.List;

import auto_testcase_generation.testdatagen.se.memory.*;
import auto_testcase_generation.testdatagen.se.memory.basic.*;
import auto_testcase_generation.testdatagen.se.memory.pointer.*;
import auto_testcase_generation.testdatagen.se.memory.array.one_dim.*;
import auto_testcase_generation.testdatagen.se.memory.array.multiple_dims.*;
import com.dse.config.IFunctionConfig;
import com.dse.parser.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.VariableTypeUtils;

import auto_testcase_generation.testdatagen.testdatainit.VariableTypes;

public abstract class SimpleStructureSymbolicVariable extends SymbolicVariable {
	final static AkaLogger logger = AkaLogger.get(SimpleStructureSymbolicVariable.class);

	// Represent attributes in the structure variable
	protected List<ISymbolicVariable> attributes = new ArrayList<>();

	public SimpleStructureSymbolicVariable(String name, String type, int scopeLevel) {
		super(name, type, scopeLevel);
	}

	public List<ISymbolicVariable> getAttributes() {
		return attributes;
	}

	@Override
	public void setNode(INode node) {
		super.setNode(node);
		if (node instanceof StructureNode) {
			StructureNode cast = (StructureNode) node;
			for (IVariableNode attribute : cast.getAttributes()) {
				ISymbolicVariable symbolicAttribute = createSymbolicVariableFromAttribute(attribute);
				this.getAttributes().add(symbolicAttribute);
			}
		}
	}

	@Override
	public boolean assign(ISymbolicVariable other) {
		if (!(other.getClass().equals(getClass())))
			return false;

		SimpleStructureSymbolicVariable classVar = (SimpleStructureSymbolicVariable) other;

		if (!classVar.type.equals(type))
			return false;

		attributes.clear();
		attributes.addAll(classVar.attributes);

		return true;
	}

	public void setAttributes(List<ISymbolicVariable> attributes) {
		this.attributes = attributes;
	}

	public void createNewAttributes(String stub_name, Object o) {
		// Create a new list of attributes
		this.attributes = new ArrayList<>();
		// Get types of the attributes
		for (Node child: getNode().getChildren()) {
			if (child instanceof AttributeOfStructureVariableNode) {
				String type = ((AttributeOfStructureVariableNode) child).getCoreType();
				String name = ((AttributeOfStructureVariableNode) child).getName();
				// Create a new attribute
				ISymbolicVariable attr = createSymbolicVariableFromType(type, name, stub_name);
				// Add the new attribute to the list
				this.attributes.add(attr);
			}
		}
	}

	public ISymbolicVariable createSymbolicVariableFromType(String type, String name, String stub_name) {
		if (VariableTypeUtils.isNumBasic(type)) {
			NumberSymbolicVariable attr = new NumberSymbolicVariable(
					name,
					type,
					getScopeLevel(),
					stub_name+"."+name
			);
			return attr;
		} else if (VariableTypeUtils.isChBasic(type)) {
			CharacterSymbolicVariable attr = new CharacterSymbolicVariable(
					name,
					type,
					getScopeLevel(),
					stub_name+"."+name
			);
			return attr;
		}
		return null;
	}

	// Repetitive code, need a better idea to do this
	private ISymbolicVariable clone_attribute(ISymbolicVariable attr) throws Exception{
		if (attr instanceof PointerCharacterSymbolicVariable) {
			PointerCharacterSymbolicVariable new_attr = new PointerCharacterSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof PointerClassSymbolicVariable) {
			PointerClassSymbolicVariable new_attr = new PointerClassSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof PointerEnumSymbolicVariable) {
			PointerEnumSymbolicVariable new_attr = new PointerEnumSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof PointerNumberSymbolicVariable) {
			PointerNumberSymbolicVariable new_attr = new PointerNumberSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof PointerStructSymbolicVariable) {
			PointerStructSymbolicVariable new_attr = new PointerStructSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof PointerUnionSymbolicVariable) {
			PointerUnionSymbolicVariable new_attr = new PointerUnionSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof PointerSymbolicVariable) {
			PointerSymbolicVariable new_attr = new PointerSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof ClassSymbolicVariable) {
			ClassSymbolicVariable new_attr = new ClassSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.copyAttributes(attr);
			return new_attr;
		} else if (attr instanceof EnumSymbolicVariable) {
			EnumSymbolicVariable new_attr = new EnumSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel(),
					((EnumSymbolicVariable) attr).getSymbolicValue()
			);
			return new_attr;
		} else if (attr instanceof UnionSymbolicVariable) {
			UnionSymbolicVariable new_attr = new UnionSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel(),
					((UnionSymbolicVariable) attr).getSymbolicValue()
			);
//			new_attr.copyAttributes(attr);
			return new_attr;
		} else if (attr instanceof StructSymbolicVariable) {
			StructSymbolicVariable new_attr = new StructSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.copyAttributes(attr);
			return new_attr;
		} else if (attr instanceof NumberSymbolicVariable) {
			NumberSymbolicVariable new_attr = new NumberSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel(),
					((NumberSymbolicVariable) attr).getSymbolicValue()
			);
			return new_attr;
		} else if (attr instanceof CharacterSymbolicVariable) {
			CharacterSymbolicVariable new_attr = new CharacterSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel(),
					((CharacterSymbolicVariable) attr).getSymbolicValue()
			);
			return new_attr;
		} else if (attr instanceof OneDimensionCharacterSymbolicVariable) {
			OneDimensionCharacterSymbolicVariable new_attr = new OneDimensionCharacterSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof OneDimensionClassSymbolicVariable) {
			OneDimensionClassSymbolicVariable new_attr = new OneDimensionClassSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof OneDimensionEnumSymbolicVariable) {
			OneDimensionEnumSymbolicVariable new_attr = new OneDimensionEnumSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof OneDimensionNumberSymbolicVariable) {
			OneDimensionNumberSymbolicVariable new_attr = new OneDimensionNumberSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof OneDimensionStructSymbolicVariable) {
			OneDimensionStructSymbolicVariable new_attr = new OneDimensionStructSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof OneDimensionUnionSymbolicVariable) {
			OneDimensionUnionSymbolicVariable new_attr = new OneDimensionUnionSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof OneDimensionZ3ToIntSymbolicVariable) {
			OneDimensionZ3ToIntSymbolicVariable new_attr = new OneDimensionZ3ToIntSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionCharacterSymbolicVariable) {
			MultipleDimensionCharacterSymbolicVariable new_attr = new MultipleDimensionCharacterSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionClassSymbolicVariable) {
			MultipleDimensionClassSymbolicVariable new_attr = new MultipleDimensionClassSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionEnumSymbolicVariable) {
			MultipleDimensionEnumSymbolicVariable new_attr = new MultipleDimensionEnumSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionNumberSymbolicVariable) {
			MultipleDimensionNumberSymbolicVariable new_attr = new MultipleDimensionNumberSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionStructSymbolicVariable) {
			MultipleDimensionStructSymbolicVariable new_attr = new MultipleDimensionStructSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionUnionSymbolicVariable) {
			MultipleDimensionUnionSymbolicVariable new_attr = new MultipleDimensionUnionSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else if (attr instanceof MultipleDimensionZ3ToIntSymbolicVariable) {
			MultipleDimensionZ3ToIntSymbolicVariable new_attr = new MultipleDimensionZ3ToIntSymbolicVariable(
					attr.getName(),
					attr.getType(),
					attr.getScopeLevel()
			);
			new_attr.assign(attr);
			return new_attr;
		} else {
			return null;
		}
	}

	public void copyAttributes(ISymbolicVariable that) throws Exception{
		if (that instanceof SimpleStructureSymbolicVariable) {
			List<ISymbolicVariable> attr_list = new ArrayList<>();
			for (ISymbolicVariable attr: ((SimpleStructureSymbolicVariable)that).attributes) {
				attr_list.add(this.clone_attribute(attr));
			}
			this.attributes = attr_list;
		} else {
			throw new Exception("Can't copy attributes of a non-structure variable!");
		}
	}

	protected ISymbolicVariable createSymbolicVariableFromAttribute(IVariableNode attribute) {
		SymbolicVariable v = null;

		// All passing variables have global access
		VariableNode par = (VariableNode) attribute;
		INode nodeType = par.resolveCoreType();
		String name = par.getName();
		String defaultValue = PREFIX_SYMBOLIC_VALUE + this.getName() + SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES
				+ name;

//        String realType = Utils.getRealType(par.getReducedRawType(), par.getParent());
		String realType = par.getRealType();

		IFunctionConfig functionConfig = function == null ? null : function.getFunctionConfig();

		if (VariableTypes.isAuto(realType))
			logger.error("Does not support type of the passing variable is auto");
		else {
			v = SymbolicVariable.create(name, realType, nodeType, defaultValue, scopeLevel, functionConfig);
		}
//		/*
//		 * ----------------NUMBER----------------------
//		 */
//		if (VariableTypes.isNumBasic(realType))
//			v = new NumberSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE, defaultValue);
//		else if (VariableTypes.isNumOneDimension(realType)) {
//			v = new OneDimensionNumberSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			((OneDimensionSymbolicVariable) v).getBlock().setName(defaultValue);
//
//		} else if (VariableTypeUtils.isNumMultiDimension(realType)) {
//			v = new MultipleDimensionNumberSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			((MultipleDimensionNumberSymbolicVariable) v).getBlock().setName(defaultValue);
//
//		} else if (VariableTypeUtils.isNumPointer(realType)) {
//			v = new PointerNumberSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			((PointerNumberSymbolicVariable) v).getReference().getBlock().setName(defaultValue);
//
//		} else
//		/*
//		 * ----------------CHARACTER----------------------
//		 */
//		if (VariableTypes.isChBasic(realType))
//			v = new CharacterSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE, defaultValue);
//		else if (VariableTypes.isChOneDimension(realType)) {
//			v = new OneDimensionCharacterSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			((OneDimensionSymbolicVariable) v).getBlock().setName(defaultValue);
//
//		} else if (VariableTypeUtils.isChMultiDimension(realType)) {
//			v = new MultipleDimensionCharacterSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			((MultipleDimensionCharacterSymbolicVariable) v).getBlock().setName(defaultValue);
//
//		} else if (VariableTypeUtils.isChPointer(realType)) {
//			v = new PointerCharacterSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			((PointerCharacterSymbolicVariable) v).getReference().getBlock().setName(defaultValue);
//			((PointerCharacterSymbolicVariable) v)
//					.setSize(this.getFunction().getFunctionConfig().getBoundOfArray().getLower() + "");
//
//		} else
//		/*
//		 * ----------------STRUCTURE----------------------
//		 */
//		if (VariableTypes.isStructureSimple(realType)) {
//
//			if (nodeType instanceof UnionNode)
//				v = new UnionSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof StructNode)
//				v = new StructSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof ClassNode)
//				v = new ClassSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof EnumNode)
//				v = new EnumSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//
//		} else if (VariableTypes.isStructureOneDimension(realType)) {
//			if (nodeType instanceof UnionNode)
//				v = new OneDimensionUnionSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof StructNode)
//				v = new OneDimensionStructSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof ClassNode)
//				v = new OneDimensionClassSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof EnumNode)
//				v = new OneDimensionEnumSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//
//			if (v != null)
//				((OneDimensionSymbolicVariable) v).getBlock().setName(defaultValue);
//
//		} else if (VariableTypeUtils.isStructureMultiDimension(realType)) {
//			if (nodeType instanceof UnionNode)
//				v = new MultipleDimensionUnionSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof StructNode)
//				v = new MultipleDimensionStructSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof ClassNode)
//				v = new MultipleDimensionUnionSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof EnumNode)
//				v = new MultipleDimensionEnumSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//
//			if (v != null)
//				((ArraySymbolicVariable) v).getBlock().setName(defaultValue);
//
//		} else if (VariableTypeUtils.isStructurePointerMultiLevel(realType)
//				|| VariableTypeUtils.isStructureOneLevel(realType)) {
//			if (nodeType instanceof UnionNode)
//				v = new PointerUnionSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof StructNode)
//				v = new PointerStructSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof ClassNode)
//				v = new PointerClassSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//			else if (nodeType instanceof EnumNode)
//				v = new PointerEnumSymbolicVariable(name, realType, ISymbolicVariable.GLOBAL_SCOPE);
//
//			if (v != null)
//				((PointerSymbolicVariable) v).getReference().getBlock().setName(defaultValue);
//		}

		return v;
	}
}
