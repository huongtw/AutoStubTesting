package auto_testcase_generation.testdatagen.se.memory;

import java.util.List;

import auto_testcase_generation.testdatagen.se.memory.array.multiple_dims.*;
import auto_testcase_generation.testdatagen.se.memory.array.one_dim.*;
import auto_testcase_generation.testdatagen.se.memory.basic.CharacterSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.basic.NumberSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.pointer.*;
import auto_testcase_generation.testdatagen.se.memory.structure.ClassSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.structure.EnumSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.structure.StructSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.structure.UnionSymbolicVariable;
import auto_testcase_generation.testdatagen.testdatainit.VariableTypes;
import auto_testcase_generation.utils.ASTUtils;
import com.dse.config.IFunctionConfig;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;

/**
 * Represent a symbolic variable
 *
 * @author ducanh
 */
public abstract class SymbolicVariable implements ISymbolicVariable {
	protected ICommonFunctionNode function;
	/**
	 * The AST node corresponding to variable
	 */
	protected INode node;

	/**
	 * Name of variable
	 */
	protected String name;

	/**
	 * The type of variable
	 */
	protected String type;

	/**
	 * The scope of variable
	 */
	protected int scopeLevel;

//	/**
//	 * The function node contains this variable
//	 */
//	protected INode context;

	public SymbolicVariable(String name, String type, int scopeLevel) {
		this.name = name;
		this.type = type;
		this.scopeLevel = scopeLevel;
	}

//	public INode getContext() {
//		return context;
//	}
//
//	public void setContext(INode context) {
//		this.context = context;
//	}

	@Override
	public boolean isBasicType() {
		return VariableTypeUtils.isBasic(type);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int getScopeLevel() {
		return scopeLevel;
	}

	@Override
	public void setScopeLevel(int scopeLevel) {
		this.scopeLevel = scopeLevel;
	}

	@Override
	public INode getNode() {
		return node;
	}

	@Override
	public void setNode(INode node) {
		this.node = node;
	}

	@Override
	public ICommonFunctionNode getFunction() {
		return function;
	}

	@Override
	public void setFunction(ICommonFunctionNode function) {
		this.function = function;
	}

	@Override
	public abstract List<PhysicalCell> getAllPhysicalCells();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ISymbolicVariable) {
			ISymbolicVariable var = (ISymbolicVariable) obj;
            return var.getName().equals(this.getName()) && var.getScopeLevel() == getScopeLevel();
		} else
			return false;
	}

	private static final AkaLogger logger = AkaLogger.get(SymbolicVariable.class);

	public static SymbolicVariable create(String name, String realType, INode nodeType, String defaultValue,
                                          int scopeLevel, IFunctionConfig functionConfig) {
		SymbolicVariable v = null;

		if (VariableTypes.isAuto(realType)) {
			logger.debug("Does not support type of the passing variable is auto");
		}
		else {
			/*
			 * ----------------NUMBER----------------------
			 */
			if (VariableTypeUtils.isNumBasic(realType)) {
				v = new NumberSymbolicVariable(name, realType, scopeLevel, defaultValue);

			} else if (VariableTypeUtils.isNumOneDimension(realType)
					|| VariableTypeUtils.isStdintOneDimension(realType)) {
				v = new OneDimensionNumberSymbolicVariable(name, realType, scopeLevel);
				((OneDimensionSymbolicVariable) v).getBlock().setName(defaultValue);
			} else if (VariableTypeUtils.isNumMultiDimension(realType)
					|| VariableTypeUtils.isStdintMultiDimension(realType)) {
				v = new MultipleDimensionNumberSymbolicVariable(name, realType, scopeLevel);
				((MultipleDimensionNumberSymbolicVariable) v).getBlock().setName(defaultValue);

			} else if (VariableTypeUtils.isNumPointer(realType) || VariableTypeUtils.isStdintPointer(realType)) {
				v = new PointerNumberSymbolicVariable(name, realType, scopeLevel);
				((PointerNumberSymbolicVariable) v).getReference().getBlock().setName(defaultValue);

			} else {
				/*
				 * ----------------CHARACTER----------------------
				 */
				if (VariableTypeUtils.isChBasic(realType))
					v = new CharacterSymbolicVariable(name, realType, scopeLevel, defaultValue);

				else if (VariableTypeUtils.isChOneDimension(realType)) {
					v = new OneDimensionCharacterSymbolicVariable(name, realType, scopeLevel);
					((OneDimensionSymbolicVariable) v).getBlock().setName(defaultValue);

				} else if (VariableTypeUtils.isChMultiDimension(realType)) {
					v = new MultipleDimensionCharacterSymbolicVariable(name, realType, scopeLevel);
					((MultipleDimensionCharacterSymbolicVariable) v).getBlock().setName(defaultValue);

				} else if (VariableTypeUtils.isChPointer(realType)) {
					v = new PointerCharacterSymbolicVariable(name, realType, scopeLevel);
					((PointerCharacterSymbolicVariable) v).getReference().getBlock().setName(defaultValue);
					if (functionConfig != null) {
						((PointerCharacterSymbolicVariable) v)
								.setSize(functionConfig.getBoundOfArray().getLower() + "");
					}

				} else {
					/*
					 * ----------------STRUCTURE----------------------
					 */
					if (VariableTypeUtils.isStructureSimple(realType)) {

						if (nodeType instanceof UnionNode)
							v = new UnionSymbolicVariable(name, realType, scopeLevel, defaultValue);
						else if (nodeType instanceof StructNode)
							v = new StructSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof ClassNode)
							v = new ClassSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof EnumNode)
							v = new EnumSymbolicVariable(name, realType, scopeLevel, defaultValue);
						else
							logger.debug("Do not support symbolic execution for " + realType);

					} else if (VariableTypeUtils.isStructureOneDimension(realType)) {
						if (nodeType instanceof UnionNode)
							v = new OneDimensionUnionSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof StructNode)
							v = new OneDimensionStructSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof ClassNode)
							v = new OneDimensionClassSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof EnumNode)
							v = new OneDimensionEnumSymbolicVariable(name, realType, scopeLevel);
						else
							logger.debug("Do not support symbolic execution for " + realType);

						if (v != null)
							((OneDimensionSymbolicVariable) v).getBlock().setName(defaultValue);

					} else if (VariableTypeUtils.isStructureMultiDimension(realType)) {
						if (nodeType instanceof UnionNode)
							v = new MultipleDimensionUnionSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof StructNode)
							v = new MultipleDimensionStructSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof ClassNode)
							v = new MultipleDimensionUnionSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof EnumNode)
							v = new MultipleDimensionEnumSymbolicVariable(name, realType, scopeLevel);
						else
							logger.debug("Do not support symbolic execution for " + realType);

						if (v != null)
							((MultipleDimensionSymbolicVariable) v).getBlock().setName(defaultValue);

					} else if (VariableTypeUtils.isStructurePointerMultiLevel(realType)
							|| VariableTypeUtils.isStructureOneLevel(realType)) {
						if (nodeType instanceof UnionNode)
							v = new PointerUnionSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof StructNode)
							v = new PointerStructSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof ClassNode)
							v = new PointerClassSymbolicVariable(name, realType, scopeLevel);
						else if (nodeType instanceof EnumNode)
							v = new PointerEnumSymbolicVariable(name, realType, scopeLevel);
						else
							logger.debug("Do not support symbolic execution for " + realType);

						if (v != null) {
							PointerSymbolicVariable pointerVar = (PointerSymbolicVariable) v;
							pointerVar.getReference().getBlock().setName(defaultValue);

							if (functionConfig != null)
								pointerVar.setSize(functionConfig.getBoundOfArray().getLower() + "");

							IASTDeclarationStatement tempDeclare = ASTUtils.generateDeclarationStatement(realType);
							VariableNode tempVar = new VariableNode();
							tempVar.setAST(tempDeclare.getDeclaration());

							((PointerStructureSymbolicVariable) v).setLevel(tempVar.getLevelOfPointer());
						}

					} else {
						logger.debug(String.format("The variable %s with type %s is not supported now", name, realType));
					}
				}
			}
		}
		if (v != null) {
			v.setNode(nodeType);
		}
		return v;
	}

	@Override
	public int compareTo(ISymbolicVariable o) {
		return Integer.compare(scopeLevel, o.getScopeLevel());
	}
}
