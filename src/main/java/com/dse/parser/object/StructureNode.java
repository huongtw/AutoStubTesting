package com.dse.parser.object;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.TypedefDependency;
import com.dse.search.Search;
import com.dse.search.condition.ClassNodeCondition;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.IRegex;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent Class, Struct, union, enum
 *
 * @author DucAnh
 */
public abstract class StructureNode extends CustomASTNode<IASTSimpleDeclaration> implements ISourceNavigable {
	final static AkaLogger logger = AkaLogger.get(StructureNode.class);

	public static void main(String[] args) {
		ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));
		List<INode> structureNode = Search
				.searchNodes(parser.getRootTree(), new ClassNodeCondition());
		List<INode> classNode = Search
				.searchNodes(parser.getRootTree(), new FunctionNodeCondition());

		INode root = parser.getRootTree();
		System.out.println(root.toString());
	}

	public boolean haveTypedef() {
		if (this instanceof StructTypedefNode || this instanceof SpecialStructTypedefNode
				|| this instanceof EnumTypedefNode || this instanceof SpecialEnumTypedefNode
				|| this instanceof UnionTypedefNode || this instanceof SpecialUnionTypedefNode)
			return true;

		boolean checkTypedefDependency = getDependencies().stream().anyMatch(d -> d instanceof TypedefDependency);
		return checkTypedefDependency;
	}

	/**
	 * Get all attributes of the given structure node Ex: <br/>
	 * class A{int a; int b;...} <br/>
	 * -----------------------> return "a", "b"
	 * 
	 * @return
	 */
	public ArrayList<IVariableNode> getAttributes() {
		ArrayList<IVariableNode> attributes = getPrivateAttributes();
		attributes.addAll(getPublicAttributes());
		return attributes;
	}

	public ArrayList<ICommonFunctionNode> getConstructors() {
		ArrayList<ICommonFunctionNode> methods = new ArrayList<>();
		for (INode node : getChildren())
			if (node instanceof ConstructorNode) {
				ConstructorNode f = (ConstructorNode) node;
				IASTFunctionDefinition ast = f.getAST();

				String decl = ast.getDeclSpecifier().getRawSignature();
				decl = VariableTypeUtils.deleteStorageClasses(decl);
				IASTFunctionDeclarator declarator = ast.getDeclarator();
				IASTNode firstChildOfDeclarator = declarator.getChildren()[0];

				/*
				  if it is constructor/destructor class/structure
				 */
				if (decl.equals("") && firstChildOfDeclarator.getRawSignature().equals(getNewType()))
					methods.add(f);
			} else if (node instanceof ICommonFunctionNode) {
				String functionName = ((ICommonFunctionNode) node).getSingleSimpleName();
				String structureName = getName();
				if (functionName.equals(structureName)) {
					methods.add((ICommonFunctionNode) node);
				}
			}

		if (methods.isEmpty())
			methods.add(generateDefaultConstructor());

		return methods;
	}

	public static class DefaultConstructor extends ConstructorNode {

		private String structureName;
		private String constructorName;

		public DefaultConstructor(StructureNode structureNode) {
			structureName = structureNode.getName();
			constructorName = String.format("%s()", structureName);

			genAST();

			setName(constructorName);
			setParent(structureNode);

			String constructorPath = getAbsolutePath() + File.separator + constructorName;
			setAbsolutePath(constructorPath);
		}

		private void genAST() {
			IASTFunctionDefinition definition = new CPPASTFunctionDefinition();
			IASTSimpleDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();
			definition.setDeclSpecifier(declSpec);
			IASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator(new CPPASTName(structureName.toCharArray()));
			definition.setDeclarator(declarator);
			setAST(definition);
		}

		@Override
		public String getReturnType() {
			return structureName;
		}

		@Override
		public String getName() {
			return constructorName;
		}

		@Override
		public String toString() {
			return constructorName;
		}
	}

	private ICommonFunctionNode generateDefaultConstructor() {
		DefaultConstructor defaultConstructor = new DefaultConstructor(this);
		getChildren().add(defaultConstructor);
		return defaultConstructor;
	}

	public ArrayList<IVariableNode> getPrivateAttributes() {
		ArrayList<IVariableNode> attributes = new ArrayList<>();
		for (INode node : getChildren())
			if (node instanceof IVariableNode && ((VariableNode) node).isPrivate)
				attributes.add((VariableNode) node);
		return attributes;
	}

	public ArrayList<IVariableNode> getPublicAttributes() {
		ArrayList<IVariableNode> attributes = new ArrayList<>();
		for (INode node : getChildren())
			if (node instanceof IVariableNode && !((VariableNode) node).isPrivate)
				attributes.add((IVariableNode) node);
		return attributes;
	}

	public List<INode> getPublicAnonymous() {
		List<INode> anonymous = new ArrayList<>();

		for (INode child : getChildren()) {
			if (child instanceof StructureNode && isAnonymousChild((StructureNode) child)
					&& getAnonymousVisibility((StructureNode) child) == ICPPASTVisibilityLabel.v_public)
				anonymous.add(child);
		}

		return anonymous;
	}

	private boolean isAnonymousChild(StructureNode child) {
		return child.getName().isEmpty();
	}

	private int getAnonymousVisibility(StructureNode anonymous) {
		IASTSimpleDeclaration astStructure = AST;
		IASTDeclSpecifier declSpec = astStructure.getDeclSpecifier();

		int visibility = this instanceof ClassNode ?
				ICPPASTVisibilityLabel.v_private : ICPPASTVisibilityLabel.v_public;

		if (declSpec instanceof IASTCompositeTypeSpecifier) {
			IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpec).getDeclarations(true);
			for (IASTDeclaration declaration : declarations) {
				if (declaration instanceof ICPPASTVisibilityLabel)
					visibility = ((ICPPASTVisibilityLabel) declaration).getVisibility();
				else if (declaration instanceof IASTSimpleDeclaration) {
					if (declaration.getRawSignature().equals(anonymous.getAST().getRawSignature())) {
						return visibility;
					}
				}
			}
		}

		return visibility;
	}

	@Override
	public void setAST(IASTSimpleDeclaration aST) {
		super.setAST(aST);
	}

	/**
	 * Two cases might happen. <br/>
	 * <ol>
	 * <li>Case 1. <b>name</b> is an attribute, it therefore returns the node
	 * corresponding to this attribute <br/>
	 * Ex:<i> class A{B* x;} class B{C* y;}<br/>
	 * Consider class A, if name = "x.y" ----------output---------> attribute "C*
	 * y"</i></li>
	 * 
	 * 
	 * <li>Case 2. <b>name</b> is an element of pointer/array attribute, <br/>
	 * - If the kind of the attribute is basic (e.g., int*, int[], char*, v.v.), it
	 * return an instance of @see{AvailableTypeNode.class} <br/>
	 * Ex: <i>class A{int* x;}<br/>
	 * Consider class A, if name = "x[0]" ----------output---------> "int" </i><br/>
	 * <br/>
	 * - Otherwise, it returns the definition of attribute element.<br/>
	 * Ex: <i>class A{B* x;}class B{C* y;}<br/>
	 * Consider class A, if name = "x.y[1]" ----------output---------> definition of
	 * class C</i></li>
	 * </ol>
	 * 
	 * @param name
	 *            Ex1: name. Ex2: name[0]. Ex3: sv.other[0].name
	 * @return attribute have the given name
	 */
	public INode findAttributeByName(String name) {
		if (name == null || name.length() == 0)
			return null;
		IASTNode astName = Utils.convertToIAST(name);

		/*
		  STEP 1: Get all elements in the given name

		  Ex: "other[0].name" --------------> 3 elements: "other, [0], name"
		 */
		List<String> elements = new ArrayList<>();
		boolean stop = false;
		while (!stop) {
			logger.debug(name);
			logger.debug(".");
			// Ex: "other[0]"
			if (astName instanceof ICPPASTArraySubscriptExpression) {
				// Add "[0]" to the list
				elements.add(0, "[" + astName.getChildren()[astName.getChildren().length - 1].getRawSignature() + "]");
				astName = astName.getChildren()[0];
			} else
			// Ex: "other"
			if (astName instanceof IASTIdExpression) {
				elements.add(0, astName.getRawSignature());
				stop = true;
			} else
			// Ex: "other[0].name"
			if (astName instanceof IASTFieldReference) {
				// Add "name" to the list
				elements.add(0, astName.getChildren()[1].getRawSignature());

				astName = astName.getChildren()[0];
			}
		}
		// STEP 2. Parse elements
		INode currentNode = this;
		stop = false;
		final String ONE_LEVEL_POINTER = "*";
		while (elements.size() > 0 && !stop) {
			String item = elements.get(0);
			elements.remove(0);

			// Case 2: Access the element of an attribute
			if (item.startsWith("[")) {

				if (currentNode instanceof IVariableNode) {
					IVariableNode castCurrentNode = (IVariableNode) currentNode;
					currentNode = castCurrentNode.resolveCoreType();

					if (currentNode instanceof AvailableTypeNode) {
						String type = ((AvailableTypeNode) currentNode).getType()
								.replaceFirst(IRegex.POINTER, SpecialCharacter.EMPTY);
						currentNode = new AvailableTypeNode();
						((AvailableTypeNode) currentNode).setType(type);
					} else if (currentNode instanceof StructureNode) {
						String type = castCurrentNode.getRealType()
								.replaceFirst(IRegex.POINTER, SpecialCharacter.EMPTY);
						currentNode = new VariableNode();
					}

				} else if (currentNode instanceof AvailableTypeNode) {
					String type = ((AvailableTypeNode) currentNode).getType();
					currentNode = new AvailableTypeNode();
					((AvailableTypeNode) currentNode).setType(type.replace(ONE_LEVEL_POINTER, ""));
				}

			} else
			// Case 1
			if (currentNode instanceof StructureNode) {
				StructureNode castCurrentNode = (StructureNode) currentNode;
				for (IVariableNode attribute : castCurrentNode.getAttributes())
					if (attribute.getName().equals(item)) {
						currentNode = attribute;
						break;
					}
			} else
				stop = true;
		}
		// STEP 3.
		// If all elements has been analyzed
		if (elements.size() == 0)
			return currentNode;
		else
			return null;
	}
}
