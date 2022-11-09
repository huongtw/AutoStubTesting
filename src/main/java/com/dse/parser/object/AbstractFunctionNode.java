package com.dse.parser.object;

import auto_testcase_generation.cfg.CFGGenerationforBranchvsStatementvsBasispathCoverage;
import auto_testcase_generation.cfg.CFGGenerationforSubConditionCoverage;
import auto_testcase_generation.cfg.ICFG;
import com.dse.parser.externalvariable.StaticVariableDetecter;
import com.dse.parser.normalizer.*;
import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.parser.externalvariable.ReducedExternalVariableDetecter;
import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.*;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.search.Search;
import com.dse.search.condition.GlobalVariableNodeCondition;
import com.dse.testcase_manager.AbstractTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractFunctionNode extends CustomASTNode<IASTFunctionDefinition> implements IFunctionNode {
	final static AkaLogger logger = AkaLogger.get(AbstractFunctionNode.class);

	private FunctionNormalizer fnNormalizeFunctionToDisplayCFG;
	private FunctionNormalizer fnNormalizeFunctionToFindStaticTestcase;
	private FunctionNormalizer fnNormalizeFunctionToExecute;
	private FunctionConfig functionConfig = null;
	private MacroNormalizer2 fnMacroNormalizer = null;			// true if the function node is analyzed global variable dependency generation before
	private FunctionNormalizer generalNormalizationFunction = null;

	// true if the variable node is analyzed size dependency generation before
	// size dependency show relationship between two arguments of a function: an argument is pointer/array, the other one is an
	// integer number representing the size of that pointer/array
	protected boolean sizeDependencyState = false;

	// true if the function node is analyzed function call dependency generation before
	private boolean functionCallDependencyState = false;

	// true if the function node is analyzed global variable dependency generation before
	private boolean globalVariableDependencyState = false;

	// true if the function node is analyzed global variable dependency generation before
	private boolean realParentDependencyState = false;

//	/**
//	 * Represent the real parent of function. Ex: if function in class is defined
//	 * outside it, then its real parent is this class
//	 */
//	private INode realParent;

	private int visibility;


	protected List<IVariableNode> arguments = new ArrayList<>();
	protected List<StaticVariableNode> staticVariables;

	public AbstractFunctionNode() {
		super();
		try {
			Icon ICON_FUNCTION = new ImageIcon(Node.class.getResource("/image/node/FunctionNode.png"));
			setIcon(ICON_FUNCTION);
		} catch (Exception e) {
		}
	}

	@Override
	public void setRealParent() {
		//TODO: implement later
	}

	public void setArguments(List<IVariableNode> arguments) {
		this.arguments = arguments;
	}

	@Override
	public List<IVariableNode> getArguments() {
		if (this.arguments == null || this.arguments.size() == 0) {
			this.arguments = new ArrayList<>();

			for (INode child : getChildren())
				if (child instanceof IVariableNode && !(child instanceof StaticVariableNode)) {
					this.arguments.add((IVariableNode) child);
				}
		}
		return this.arguments;
	}

	public List<StaticVariableNode> getStaticVariables() {
		if (this.staticVariables == null) {
			this.staticVariables = new ArrayList<>();

			for (INode child : getChildren())
				if (child instanceof StaticVariableNode) {
					this.staticVariables.add((StaticVariableNode) child);
				}
		}
		return this.staticVariables;
	}

	@Override
	public String getDeclaration() {
		String simpleName = getSimpleName();
		String declator = getAST().getDeclarator().getRawSignature();
		int index = declator.indexOf(simpleName);

		return declator.substring(index);
	}

	@Override
	public List<IVariableNode> getReducedExternalVariables() {
		List<IVariableNode> externalVars = new ReducedExternalVariableDetecter(this).findVariables();
		for (Dependency d : getDependencies()) {
			if (d instanceof GlobalVariableDependency)
				if (!externalVars.contains(d.getEndArrow()))
					externalVars.add((IVariableNode) d.getEndArrow());
		}

		return externalVars;
	}

	@Override
	public String getFullName() {
		StringBuilder fullName = new StringBuilder(getSingleSimpleName() + "(");
		for (INode var : getArguments())
			fullName.append(var.getNewType()).append(",");
		fullName.append(")");
		fullName = new StringBuilder(fullName.toString().replace(",)", ")"));

		/*
		 * Add prefix of the current name
		 */
		String logicPath = getLogicPathFromTopLevel();
		if (logicPath.length() > 0)
			return logicPath + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + fullName;
		else
			return fullName.toString();
	}

	/**
	 * Get the name of the function and the types of variables pass into it Ex:
	 * "test(int,int)"
	 */
	@Override
	public String getNewType() {
		StringBuilder output = new StringBuilder(getAST().getDeclarator().getName().getRawSignature());
		output.append("(");
		for (IVariableNode parameter : getArguments()) {
			output.append(parameter.getRawType());

			String defaultVal = parameter.getDefaultValue();
			if (defaultVal != null)
				output.append("= ").append(defaultVal);

			output.append(",");
		}
		output.append(")");
		output = new StringBuilder(output.toString().replace(",)", ")").replaceAll("\\s*\\)", "\\)"));
		return output.toString();
	}

	@Override
	public IASTFileLocation getNodeLocation() {
		return getAST().getDeclarator().getFileLocation();
	}

	@Override
	public INode getRealParent() {
		for (Dependency d:this.getDependencies())
			if (d instanceof RealParentDependency && d.getStartArrow().equals(this))
			{
				return d.getEndArrow();
			}
		return null;
	}

	@Override
	public String getSimpleName() {
		CPPASTFunctionDeclarator declarator = (CPPASTFunctionDeclarator) getAST().getDeclarator();
//		IASTNode[] children = declarator.getChildren();
//		IASTNode selectedChild = children[0];
//		if (selectedChild instanceof CPPASTPointer)
//			selectedChild = children[1];
		IASTName selectedChild = declarator.getName();

		String simpleName = selectedChild.getRawSignature();
		simpleName = simpleName.replace(" ", "");

		return simpleName;
	}


	@Override
	public File getSourceFile() {
		INode sourceCodeFileNode = Utils.getSourcecodeFile(this);
		if (sourceCodeFileNode != null)
			return new File(sourceCodeFileNode.getAbsolutePath());
		else
			return null;
	}

	@Override
	public INode isGetter() {
		for (Dependency d : getDependencies())
			if (d instanceof GetterDependency)
				if (d.getStartArrow() instanceof VariableNode)
					return d.getStartArrow();
				else if (d.getEndArrow() instanceof VariableNode)
					return d.getEndArrow();
		return null;
	}

	@Override
	public boolean isNoType() {
		return getAST().getDeclSpecifier().getRawSignature().isEmpty();
	}

	@Override
	public INode isSetter() {
		for (Dependency d : getDependencies())
			if (d instanceof SetterDependency)
				if (d.getStartArrow() instanceof VariableNode)
					return d.getStartArrow();
				else if (d.getEndArrow() instanceof VariableNode)
					return d.getEndArrow();
		return null;
	}

	@Override
	public void setAST(IASTFunctionDefinition aST) {
		super.setAST(aST);

		// remove all existing variables node
		for (int i = getChildren().size() - 1; i >= 0; i--)
			if (getChildren().get(i) instanceof IVariableNode){
				getChildren().remove(i);
			}

		IASTFunctionDeclarator declarator = getAST().getDeclarator();

		// find arguments
		for (IASTNode child : declarator.getChildren()) {
			if (child instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration astArgument = (IASTParameterDeclaration) child;

				VariableNode argumentNode = new InternalVariableNode();
				argumentNode.setAST(astArgument);
				argumentNode.setParent(this);

				if (!VariableTypeUtils.isVoid(argumentNode.getRealType())) {
					if (!(getChildren().contains(argumentNode)))
						getChildren().add(argumentNode);
				}
			}
		}

		StaticVariableDetecter staticDetecter = new StaticVariableDetecter(this);
		List<IVariableNode> staticVars = staticDetecter.findVariables();
		staticVars.forEach(v -> {
			if (!(getChildren().contains(v)))
				getChildren().add((Node) v);
		});
	}

	@Override
	public void setParent(INode parent) {
		super.setParent(parent);
//		realParent = parent;
	}

	@Override
	public String toString() {
		return getAST().getDeclSpecifier().toString() + " " + getAST().getDeclarator().getRawSignature();
	}

	@Override
	public boolean isMethod() {
		INode realParent = getRealParent() == null ? getParent() : getRealParent();
		return realParent instanceof StructureNode;
	}

	@Override
	public boolean isTemplate() {
		INode realParent = getRealParent() == null ? getParent() : getRealParent();

		if (realParent instanceof ClassNode) {
			if (((ClassNode) realParent).isTemplate()) {
				String[] templateParams = TemplateUtils.getTemplateParameters(realParent);

				for (String templateParam : templateParams) {

					if (isUseTemplate(getReturnType(), templateParam))
						return true;

					for (IVariableNode argument : getArguments()) {
						if (isUseTemplate(argument.getRawType(), templateParam))
							return true;
					}
				}
			}
		}

		return AST.getParent() instanceof ICPPASTTemplateDeclaration;
	}

	private boolean isUseTemplate(String type, String templateParam) {
		String simpleType = type.replaceAll("[^\\w]", " ");

		if (simpleType.equals(templateParam))
			return true;

		String[] extended = new String[] {templateParam + " ", " " + templateParam, " " + templateParam + " "};
		for (String extend : extended) {
			if (type.contains(extend))
				return true;
		}

		return false;
	}

	@Override
	public String getSingleSimpleName() {
		String singleSimpleName = getSimpleName();
		if (!singleSimpleName.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
			return singleSimpleName;
		else
			return singleSimpleName
					.substring(singleSimpleName.lastIndexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) + 2);
	}

	@Override
	public IVariableNode getCorrespondingVariable() {
		IVariableNode output = null;
		INode structureParent = Utils.getStructureParent(this);

		if (structureParent instanceof StructureNode) {
			StructureNode p = (StructureNode) structureParent;

			for (IVariableNode child : p.getAttributes())
				if (child.getSetterNode() != null && child.getSetterNode().equals(this)
						|| child.getGetterNode() != null && child.getGetterNode().equals(this)) {
					output = child;
					break;
				}
		}
		return output;
	}

	public VariableNode getReturnVariableNode() {
		for (INode child : getChildren()) {
			if (child instanceof ReturnVariableNode)
				return (VariableNode) child;
		}

		String returnType = getReturnType();

		returnType = VariableTypeUtils.deleteVirtualAndInlineKeyword(returnType);

		VariableNode returnVar = new ReturnVariableNode();
		returnVar.setName(INameRule.RETURN_VARIABLE_NAME_PREFIX);
		returnVar.setRawType(returnType);

		String coreType = returnType.replace(SpecialCharacter.POINTER, "");
		coreType = VariableTypeUtils.deleteStorageClasses(coreType);
		coreType = VariableTypeUtils.deleteStructKeyword(coreType);
		coreType = VariableTypeUtils.deleteUnionKeyword(coreType);
		returnVar.setCoreType(coreType);

		returnVar.setReducedRawType(returnType);

		if (getChildren().isEmpty())
			returnVar.setParent(this);
		else {
			if (!(getChildren().get(getChildren().size() - 1) instanceof ReturnVariableNode))
				returnVar.setParent(this);
		}

		return returnVar;
	}


	@Override
	public List<IVariableNode> getExpectedNodeTypes() {
		List<IVariableNode> expectedNodeTypes = getReducedExternalVariables();
		String returnType = getReturnType();

		if (!isNoType()) {
			// add a variable representing return value
//			VariableNode returnVar = new ReturnVariableNode();
//			returnVar.setName(INameRule.RETURN_VARIABLE_NAME_PREFIX);
//			returnVar.setRawType(returnType);
//			String coreType = returnType.replace(SpecialCharacter.POINTER, "");
//			coreType = VariableTypeUtils.deleteStorageClasses(coreType);
//			coreType = VariableTypeUtils.deleteStructKeyword(coreType);
//			coreType = VariableTypeUtils.deleteUnionKeyword(coreType);
//			returnVar.setCoreType(coreType);
//			returnVar.setReducedRawType(returnType);
//			returnVar.setParent(this);
			VariableNode returnVar = getReturnVariableNode();

			expectedNodeTypes.add(returnVar);
		}

		/*
		 * Add throw variable
		 */
		VariableNode returnVar = new ThrowVariableNode();
		returnVar.setName(INameRule.THROW_VARIABLE_NAME_PREFIX);
		returnVar.setRawType(VariableTypeUtils.THROW);
		returnVar.setCoreType(VariableTypeUtils.THROW);
		returnVar.setReducedRawType(VariableTypeUtils.THROW);
		returnVar.setParent(this);

		expectedNodeTypes.add(returnVar);

		/*
		 * Add all variables as passing variable
		 */
		for (IVariableNode argument : getPassingVariables())
			if (!expectedNodeTypes.contains(argument))
				expectedNodeTypes.add(argument);

		return expectedNodeTypes;
	}

	@Override
	public List<IVariableNode> getPassingVariables() {
		List<IVariableNode> passingVariables = new ArrayList<>();

		passingVariables.addAll(getArguments());

		passingVariables.addAll(getReducedExternalVariables());

		return passingVariables;
	}

	@Override
	public String getReturnType() {
		IASTFunctionDefinition funDef = getAST();

		IASTDeclSpecifier declSpecifier = getAST().getDeclSpecifier();
		String returnType = declSpecifier.getRawSignature();
		if (declSpecifier instanceof IASTNamedTypeSpecifier) {
			returnType = String.valueOf(((IASTNamedTypeSpecifier) declSpecifier).getName().toCharArray());
		} else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			returnType = declSpecifier.toString();
		}

		/*
		 * Name of function may contain * character. Ex: SinhVien* StrDel2(char s[],int
		 * k,int h){...} ==> * StrDel2(char s[],int k,int h)
		 */
		boolean isReturnReference = false;
		CPPASTFunctionDeclarator functionDeclarator = (CPPASTFunctionDeclarator) funDef.getChildren()[1];
//		IASTNode firstChild = functionDeclarator.getChildren()[0];
//		if (firstChild instanceof CPPASTPointer)
//			isReturnReference = true;
//		/*
//		 *
//		 */
//		returnType += isReturnReference ? "*" : "";

		for (IASTNode operator : functionDeclarator.getPointerOperators())
			if (operator.getRawSignature().equals("*"))
				returnType += "*";

		return returnType;
	}

	@Override
	public IASTFunctionDefinition getNormalizedASTtoDisplayinCFG() throws Exception {
		IASTFunctionDefinition normalizedAST;
		SwitchCaseNormalizer switchCaseNormalizer = new SwitchCaseNormalizer(this);
		switchCaseNormalizer.setFunctionNode(this);
		switchCaseNormalizer.normalize();
		String sc = switchCaseNormalizer.getNormalizedSourcecode();

		normalizedAST = Utils.getFunctionsinAST(sc.toCharArray()).get(0);

		return normalizedAST;
	}

	@Override
	public String getInstrumentedofNormalizedSource() {
		return null;
	}

	@Override
	public String getLogicPathFromTopLevel() {
		INode namespace = Utils.getTopLevelClassvsStructvsNamesapceNodeParent(this);

		if (namespace == null)
			return "";
		else {
			String path = "";
			INode parent = this;
			while (!parent.equals(namespace)) {
				if (parent instanceof IFunctionNode)
					parent = ((IFunctionNode) parent).getRealParent();
				else
					parent = parent.getParent();

				if (parent instanceof NamespaceNode || parent instanceof StructureNode)
					path = parent.getNewType() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + path;
				else
					break;
			}
			if (path.length() >= 2)
				path = path.substring(0, path.length() - 2);
			return path;
		}
	}

	@Override
	public Boolean isChildrenOfUnnameNamespace() {
		INode namespace = Utils.getTopLevelClassvsStructvsNamesapceNodeParent(this);

		if (namespace == null)
			return false;
		else {
			return namespace.getNewType().equals("");
		}

	}

	@Override
	public FunctionNormalizer getNormalizeFunctionToExecute() throws Exception {
		// logger.debug("Normalize function to execute");
		if (fnNormalizeFunctionToExecute== null) {
			FunctionNormalizer fnNormalizer = new FunctionNormalizer();
			fnNormalizer.setFunctionNode(this);

//			if (Settingv2.getValue(ISettingv2.IN_TEST_MODE).equals("false")
//					|| (Settingv2.getValue(ISettingv2.IN_TEST_MODE).equals("true")
////							&& AbstractJUnitTest.ENABLE_MACRO_NORMALIZATION
//					)) {
//				fnNormalizer.addNormalizer(new MacroNormalizer2());
//			}

			fnNormalizer.addNormalizer(new FunctionNameNormalizer());
			fnNormalizer.addNormalizer(new ArgumentTypeNormalizer());
			fnNormalizer.addNormalizer(new TernaryConvertNormalizer());
			fnNormalizer.addNormalizer(new ConditionCovertNormalizer());
			fnNormalizer.addNormalizer(new ThrowNormalizer());
			fnNormalizer.addNormalizer(new SwitchCaseNormalizer());
//			fnNormalizer.addNormalizer(new FunctionCallNormalizer());

			fnNormalizer.normalize();
			fnNormalizeFunctionToExecute = fnNormalizer;
		}
		return fnNormalizeFunctionToExecute;
	}

	@Override
	@Deprecated
	public FunctionNormalizer normalizeFunctionToFindStaticTestcase() throws Exception {
		logger.debug("Normalize function to find static test case");
		if (fnNormalizeFunctionToFindStaticTestcase == null) {
			FunctionNormalizer fnNormalizer = new FunctionNormalizer();
			fnNormalizer.setFunctionNode(this);

//			if (Settingv2.getValue(ISettingv2.IN_TEST_MODE).equals("false")
//					|| (Settingv2.getValue(ISettingv2.IN_TEST_MODE).equals("true")
////							&& AbstractJUnitTest.ENABLE_MACRO_NORMALIZATION
//				)) {
//				fnNormalizer.addNormalizer(new MacroNormalizer2());
//			}

			fnNormalizer.addNormalizer(new ArgumentTypeNormalizer());
			fnNormalizer.addNormalizer(new TernaryConvertNormalizer());
			fnNormalizer.addNormalizer(new ConditionCovertNormalizer());
			fnNormalizer.addNormalizer(new ClassvsStructNormalizer());
			fnNormalizer.addNormalizer(new EnumNormalizer());
			fnNormalizer.addNormalizer(new ExternNormalizer());
			fnNormalizer.addNormalizer(new NullPtrNormalizer());
			fnNormalizer.addNormalizer(new ThrowNormalizer());
			fnNormalizer.addNormalizer(new SwitchCaseNormalizer());
			fnNormalizer.addNormalizer(new EndStringNormalizer());
			// fnNormalizer.addNormalizer(new ConstantNormalizer());

			fnNormalizer.normalize();
			fnNormalizeFunctionToFindStaticTestcase = fnNormalizer;
		}
		return fnNormalizeFunctionToFindStaticTestcase;
	}

	@Override
	public FunctionNormalizer normalizeFunctionToDisplayCFG() throws Exception {
		logger.debug("Normalization function to display in CFG");
		if (fnNormalizeFunctionToDisplayCFG == null) {
			FunctionNormalizer fnNormalizer = new FunctionNormalizer();
			fnNormalizer.setFunctionNode(this);
			fnNormalizer.addNormalizer(new TernaryConvertNormalizer());
			fnNormalizer.addNormalizer(new ConditionCovertNormalizer());
			fnNormalizer.addNormalizer(new SwitchCaseNormalizer());
			fnNormalizer.normalize();
			fnNormalizeFunctionToDisplayCFG = fnNormalizer;
		}
		return fnNormalizeFunctionToDisplayCFG;
	}

	@Override
	public FunctionNormalizer normalizedAST() throws Exception {
		if (generalNormalizationFunction == null) {
			FunctionNormalizer fnNormalizer = new FunctionNormalizer();
			fnNormalizer.setFunctionNode(this);

			// only for test
//			if (AbstractSetting.getValue(ISettingv2.IN_TEST_MODE).equals("false")
//					|| (AbstractSetting.getValue(ISettingv2.IN_TEST_MODE).equals("true")
//					&& AbstractJUnitTest.ENABLE_MACRO_NORMALIZATION)) {
//				fnNormalizer.addNormalizer(new MacroNormalizer2());
//			}
			// end test
			fnNormalizer.addNormalizer(new FunctionNameNormalizer());
			fnNormalizer.addNormalizer(new ArgumentTypeNormalizer());
			fnNormalizer.addNormalizer(new TernaryConvertNormalizer());
			fnNormalizer.addNormalizer(new ConditionCovertNormalizer());
			fnNormalizer.addNormalizer(new SwitchCaseNormalizer());
//			fnNormalizer.addNormalizer(new FunctionCallNormalizer());

			fnNormalizer.normalize();
			generalNormalizationFunction = fnNormalizer;

		}
		return generalNormalizationFunction;
	}

	@Override
	public SetterandGetterFunctionNormalizer performSettervsGetterTransformer() {
		SetterandGetterFunctionNormalizer performer = new SetterandGetterFunctionNormalizer();
		performer.setFunctionNode(this);
		performer.normalize();
		return performer;
	}

	@Override
	public ICFG generateCFG() {
		logger.debug("Generate CFG to find static solution");
		String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
		ICFG cfg = null;
		try {
			cfg = CFGUtils.createCFG(this, typeOfCoverage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfg;
	}

	@Override
	public ICFG generateCFGofExecutionSourcecode() {
		logger.debug("Generate CFG of execution source code");
		ICFG cfg = null;
		String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
		try {
			if (typeOfCoverage.equals(EnviroCoverageTypeNode.BRANCH)
					|| typeOfCoverage.equals(EnviroCoverageTypeNode.STATEMENT)
					|| typeOfCoverage.equals(EnviroCoverageTypeNode.BASIS_PATH)) {
				cfg = new CFGGenerationforBranchvsStatementvsBasispathCoverage(
						this.getGeneralNormalizationFunction().getFunctionNode()).generateCFG();
				cfg.setFunctionNode(this);

			} else if (typeOfCoverage.equals(EnviroCoverageTypeNode.MCDC)) {
				cfg = new CFGGenerationforSubConditionCoverage(this.getGeneralNormalizationFunction().getFunctionNode())
						.generateCFG();
				cfg.setFunctionNode(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfg;
	}

	@Override
	public MacroNormalizer2 getFnMacroNormalizer() {
		return fnMacroNormalizer;
	}

	@Override
	public void setFnMacroNormalizer(MacroNormalizer2 fnMacroNormalizer) {
		this.fnMacroNormalizer = fnMacroNormalizer;
	}

	@Override
	public String generateCompleteFunction() {
//		String completeFunction;
//
//		// STEP 1: normalize the arguments of the current function
//		ArgumentTypeNormalizer varialeTypeNormalizer = new ArgumentTypeNormalizer();
//		varialeTypeNormalizer.setFunctionNode(this);
//		varialeTypeNormalizer.normalize();
//		completeFunction = varialeTypeNormalizer.getNormalizedSourcecode();
//
//		// STEP 2: normalize the name of function
//		String prefixPathofFunction = getLogicPathFromTopLevel();
//		if (prefixPathofFunction.length() > 0)
//			if (prefixPathofFunction.startsWith(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
//				completeFunction = completeFunction.replace(getSimpleName(),
//						getLogicPathFromTopLevel() + getSingleSimpleName());
//			else
//				completeFunction = completeFunction.replace(getSimpleName(),
//						getLogicPathFromTopLevel() + SpecialCharacter.FILE_SCOPE_ACCESS + getSingleSimpleName());
//
//		return completeFunction;
		return getAST().getRawSignature();
	}

	@Override
	public INode clone() {
		IFunctionNode clone = new FunctionNode();
		clone.setAbsolutePath(getAbsolutePath());
		clone.setChildren(getChildren());
		clone.setDependencies(getDependencies());
		clone.setId(getId());
		clone.setName(getNewType());
		clone.setParent(getParent());
		clone.setFunctionConfig((FunctionConfig) functionConfig);
		clone.setAST(getAST());
		clone.setArguments(getArguments());

		clone.setFnMacroNormalizer(getFnMacroNormalizer());
		clone.setGeneralNormalizationFunction(getGeneralNormalizationFunction());
		return clone;
	}

	@Override
	public FunctionNormalizer getFnNormalizeFunctionToFindStaticTestcase() {
		return fnNormalizeFunctionToFindStaticTestcase;
	}

	@Override
	public void setFnNormalizeFunctionToFindStaticTestcase(FunctionNormalizer fnNormalizeFunctionToFindStaticTestcase) {
		this.fnNormalizeFunctionToFindStaticTestcase = fnNormalizeFunctionToFindStaticTestcase;
	}

	@Override
	public FunctionNormalizer getFnNormalizeFunctionToExecute() {
		return fnNormalizeFunctionToExecute;
	}

	@Override
	public void setFnNormalizeFunctionToExecute(FunctionNormalizer fnNormalizeFunctionToExecute) {
		this.fnNormalizeFunctionToExecute = fnNormalizeFunctionToExecute;
	}

	@Override
	public FunctionNormalizer getFnNormalizeFunctionToDisplayCFG() {
		return fnNormalizeFunctionToDisplayCFG;
	}

	@Override
	public void setFnNormalizeFunctionToDisplayCFG(FunctionNormalizer fnNormalizeFunctionToDisplayCFG) {
		this.fnNormalizeFunctionToDisplayCFG = fnNormalizeFunctionToDisplayCFG;
	}

	@Override
	public boolean isStaticFunction() {
		if (getRealParent() == null) {
			return getReturnType().contains("static");
		} else {
			for (INode node : getRealParent().getChildren()) {
				if (node instanceof DefinitionFunctionNode) {
					if (((DefinitionFunctionNode) node).getReturnType().contains("static")
							&& ((DefinitionFunctionNode) node).getSimpleName().equals(getSingleSimpleName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public int getVisibility() {
		if (visibility > 0)
			return visibility;

		INode realParent = getRealParent() == null ? getParent() : getRealParent();

		if (!(realParent instanceof StructureNode)) {
			visibility = ICPPASTVisibilityLabel.v_public;
		} else {
			String name = getSingleSimpleName();

			IASTSimpleDeclaration astStructure = ((StructureNode) realParent).AST;
			IASTDeclSpecifier declSpec = astStructure.getDeclSpecifier();

			visibility = realParent instanceof ClassNode ?
					ICPPASTVisibilityLabel.v_private : ICPPASTVisibilityLabel.v_public;

			if (declSpec instanceof IASTCompositeTypeSpecifier) {
				IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpec).getDeclarations(true);
				for (IASTDeclaration declaration : declarations) {
					if (declaration instanceof ICPPASTVisibilityLabel)
						visibility = ((ICPPASTVisibilityLabel) declaration).getVisibility();
					else if (declaration instanceof IASTSimpleDeclaration) {
						if (((IASTSimpleDeclaration) declaration).getDeclarators().length > 0) {

							IASTDeclarator declarator = ((IASTSimpleDeclaration) declaration).getDeclarators()[0];

							if (declarator instanceof ICPPASTFunctionDeclarator) {
								String functionName = declarator.getName().getLastName().getRawSignature();
								int parameters = ((ICPPASTFunctionDeclarator) declarator).getParameters().length;

								if (functionName.equals(name) && getArguments().size() == parameters)
									return visibility;
							}
						}
					} else if (declaration instanceof IASTFunctionDefinition) {
						IASTDeclarator declarator = ((IASTFunctionDefinition) declaration).getDeclarator();

						if (declarator instanceof ICPPASTFunctionDeclarator) {
							String functionName = declarator.getName().getLastName().getRawSignature();
							int parameters = ((ICPPASTFunctionDeclarator) declarator).getParameters().length;

							if (functionName.equals(name) && getArguments().size() == parameters)
								return visibility;
						}
					}
				}
			}
		}

		return visibility;
	}

	//Hoannv

	@Override
	public IFunctionConfig getFunctionConfig() {
		return functionConfig;
	}

	@Override
	public void setFunctionConfig(FunctionConfig functionConfig) {
		this.functionConfig = functionConfig;
		if (this.functionConfig != null)
			this.functionConfig.setFunctionNode(this);
	}

	public boolean isFunctionCallDependencyState() {
		return functionCallDependencyState;
	}

	public void setFunctionCallDependencyState(boolean functionCallDependencyState) {
		this.functionCallDependencyState = functionCallDependencyState;
	}

	public boolean isGlobalVariableDependencyState() {
		return globalVariableDependencyState;
	}

	public void setGlobalVariableDependencyState(boolean globalVariableDependencyState) {
		this.globalVariableDependencyState = globalVariableDependencyState;
	}
	@Override
	public FunctionNormalizer getGeneralNormalizationFunction() {
		return generalNormalizationFunction;
	}

	@Override
	public void setGeneralNormalizationFunction(FunctionNormalizer generalNormalizationFunction) {
		this.generalNormalizationFunction = generalNormalizationFunction;
	}


	@Override
	public String getNameOfFunctionConfigTab() {
		String name = this.getFullName();
		INode current = this.getParent();
		while (current != null && !(current instanceof ProjectNode)) {
			name = current.getName() + File.separator + name;
			current = current.getParent();
		}
		return name;
	}

	@Override
	public String getNameOfFunctionConfigJson() {
		String name = this.getSimpleName() + getFullName().length();
		INode current = this.getParent();
		while (current != null && !(current instanceof ProjectNode)) {
			name = current.getName() + "_"+ name;
			current = current.getParent();
		}
		return name;
	}

	public boolean isSizeDependencyState() {
		return sizeDependencyState;
	}

	public void setSizeDependencyState(boolean sizeDependencyState) {
		this.sizeDependencyState = sizeDependencyState;
	}

	public boolean isRealParentDependencyState() {
		return realParentDependencyState;
	}

	public void setRealParentDependencyState(boolean realParentDependencyState) {
		this.realParentDependencyState = realParentDependencyState;
	}

	@Override
	public String getHighlightedFunctionPathForBranchCoverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.BRANCH_COVERAGE_FILE_EXTENSION;
	}

	@Override
	public String getProgressCoveragePathForBranchCoverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.BRANCH_PROGRESS_FILE_EXTENSION;
	}

	@Override
	public String getHighlightedFunctionPathForMcdcCoverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.MCDC_COVERAGE_FILE_EXTENSION;
	}

	@Override
	public String getProgressCoveragePathForMcdcCOverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.MCDC_PROGRESS_FILE_EXTENSION;
	}

	@Override
	public String getHighlightedFunctionPathForStmCoverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.STATEMENT_COVERAGE_FILE_EXTENSION;
	}

	@Override
	public String getProgressCoveragePathForStmCOverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.STATEMENT_PROGRESS_FILE_EXTENSION;
	}

	@Override
	public String getProgressCoveragePathForBasisPathCoverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.BASIS_PATH_PROGRESS_FILE_EXTENSION;
	}

	@Override
	public String getHighlightedFunctionPathForBasisPathCoverage() {
		return new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + AbstractTestCase.BASIS_PATH_COVERAGE_FILE_EXTENSION;
	}

	public String getHighlightedFunctionPath(String typeOfCoverage){
		switch (typeOfCoverage){
			case EnviroCoverageTypeNode.STATEMENT:{
				return this.getHighlightedFunctionPathForStmCoverage();
			}

			case EnviroCoverageTypeNode.BRANCH:{
				return this.getHighlightedFunctionPathForBranchCoverage();
			}

			case EnviroCoverageTypeNode.BASIS_PATH:{
				return this.getHighlightedFunctionPathForBranchCoverage();
			}
			case EnviroCoverageTypeNode.MCDC:{
				return this.getHighlightedFunctionPathForMcdcCoverage();
			}
			default:
				return "";
		}
	}

	public String getProgressCoveragePath(String typeOfCoverage){
		switch (typeOfCoverage){
			case EnviroCoverageTypeNode.STATEMENT:{
				return this.getProgressCoveragePathForStmCOverage();
			}

			case EnviroCoverageTypeNode.BRANCH:{
				return this.getProgressCoveragePathForBranchCoverage();
			}

			case EnviroCoverageTypeNode.BASIS_PATH:{
				return this.getProgressCoveragePathForBasisPathCoverage();
			}
			case EnviroCoverageTypeNode.MCDC:{
				return this.getHighlightedFunctionPathForMcdcCoverage();
			}
			default:
				return "";
		}
	}

	@Override
	public String getTemplateFilePath() {
		return new WorkspaceConfig().fromJson().getTemplateFunctionDirectory() + File.separator + getSimpleName() +
				ITestCase.AKA_SIGNAL + Utils.computeMd5(getAbsolutePath()) + ".json";
	}

	@Override
	public String getName() {
		String name = new File(AbstractTestCase.removeSysPathInName(getAbsolutePath())).getName();
		name = AbstractTestCase.redoTheReplacementOfSysPathInName(name);
		return name;
	}

	public String getInformName()
	{
		String iName = getName();
		iName = iName.replace("*", "Pt");
		return iName;
	}

	@Override
	public List<IVariableNode> getExternalVariables() {
		List<IVariableNode> globalVariables = new ArrayList<>();

		INode unit = Utils.getSourcecodeFile(this);
		List<INode> nodes = Search.searchNodes(unit, new GlobalVariableNodeCondition());
		for (INode n : nodes)
			if (n instanceof ExternalVariableNode && !globalVariables.contains(n))
				globalVariables.add((IVariableNode) n);

		// get all global variables in header files
		List<Node> includedNodes = VariableSearchingSpace.getAllIncludedNodes(unit);
		VariableSearchingSpace.includeNodes = new ArrayList<>();
		for (Node node : includedNodes) {
			List<INode> includedGlobalVariables = Search.searchNodes(node, new GlobalVariableNodeCondition());

			includedGlobalVariables.forEach(global -> {

				if (global instanceof ExternalVariableNode && !globalVariables.contains(global))
					globalVariables.add((IVariableNode) global);
			});
		}

		return globalVariables;
	}


	@Override
	public List<IVariableNode> getArgumentsAndGlobalVariables() {
		List<IVariableNode> output = getExternalVariables();
		output.addAll(getArguments());
		return output;
	}

	@Override
	public boolean hasVoidPointerArgument() {
		for (IVariableNode arg: this.getArguments())
			if (VariableTypeUtils.isVoidPointer(arg.getRealType())){
				return true;
			}
		return false;
	}

	@Override
	public boolean hasFunctionPointerArgument() {
		for (IVariableNode arg: this.getArguments())
			if (VariableTypeUtils.isFunctionPointer(arg.getRealType())){
				return true;
			}
		return false;
	}
}
