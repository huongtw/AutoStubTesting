package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import auto_testcase_generation.testdatagen.se.memory.SymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import auto_testcase_generation.testdatagen.testdatainit.VariableTypes;
import com.dse.parser.object.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;

/**
 * Ex1: "int a = 2;" <br/>
 * Ex2: "int a = y+z;" <br/>
 *
 * @author ducanhnguyen
 */
public class DeclarationParser extends StatementParser {
    private IFunctionNode function;
    /**
     * The current scope of the statement. The value of global scope is equivalent
     * to zero.
     */
    private int scopeLevel = 0;

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);
        if (ast instanceof IASTSimpleDeclaration && function != null)
            parseDeclaration((IASTSimpleDeclaration) ast, table, callTable, scopeLevel, function);
    }

    public void parseDeclaration(IASTSimpleDeclaration stm, VariableNodeTable table, FunctionCallTable callTable,
                                 int scopeLevel, ICommonFunctionNode functionNode) throws Exception {
        // TODO: ignore
        IASTSimpleDeclaration normalize = stm;
        String rawType = stm.getDeclSpecifier().getRawSignature();
        if (normalize.getDeclarators().length > 1) {
            String tempDeclare = rawType + SpecialCharacter.SPACE + stm.getDeclarators()[0].getName().getRawSignature();
            IASTNode tempAST = Utils.convertToIAST(tempDeclare);
            if (tempAST instanceof IASTDeclarationStatement)
                tempAST = ((IASTDeclarationStatement) tempAST).getDeclaration();
            if (tempAST instanceof IASTSimpleDeclaration)
                normalize = (IASTSimpleDeclaration) tempAST;
        }
        VariableNode var = new InternalVariableNode();
        var.setAST(normalize);
        var.setParent(functionNode);
        INode correspondingNode = var.resolveCoreType();

        String realType = var.getRealType();

        if (correspondingNode == null)
            return;

        for (IASTDeclarator declarator : stm.getDeclarators()) {
            String name = declarator.getName().getRawSignature();

            String defaultValue = VariableTypeUtils.isNumBasic(realType) || VariableTypeUtils.isChBasic(realType) || correspondingNode instanceof EnumNode ? "0" : name;

            SymbolicVariable v = SymbolicVariable.create(name, realType, correspondingNode,
                    defaultValue, scopeLevel, function.getFunctionConfig());

//			if (VariableTypes.isNumBasic(realType)) {
//				v = new NumberSymbolicVariable(name, realType, scopeLevel, defaultValue);
//
//			} else if (VariableTypes.isChBasic(realType)) {
//				v = new CharacterSymbolicVariable(name, realType, scopeLevel, defaultValue);
//
//			} else if (VariableTypes.isNumOneDimension(realType))
//				v = new OneDimensionNumberSymbolicVariable(name, realType, scopeLevel);
//
//			else if (VariableTypes.isChOneDimension(realType))
//				v = new OneDimensionCharacterSymbolicVariable(name, realType, scopeLevel);
//
//			else if (VariableTypeUtils.isNumPointer(realType))
//				v = new PointerNumberSymbolicVariable(name, realType, scopeLevel);
//
//			else if (VariableTypeUtils.isChPointer(realType))
//				v = new PointerCharacterSymbolicVariable(name, realType, scopeLevel);
//
//			else if (VariableTypes.isStructureSimple(realType)) {
//				if (correspondingNode instanceof StructNode)
//					v = new StructSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//				else if (correspondingNode instanceof ClassNode)
//					v = new ClassSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//				else if (correspondingNode instanceof EnumNode) {
//					v = new EnumSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//				} else if (correspondingNode instanceof UnionNode)
//					v = new UnionSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//			}  else if (VariableTypeUtils.isStructureMultiLevel(realType)
//					|| VariableTypeUtils.isStructureOneLevel(realType)) {
//				if (correspondingNode instanceof StructNode)
//					v = new PointerStructSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//				else if (correspondingNode instanceof ClassNode)
//					v = new PointerClassSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//				else if (correspondingNode instanceof EnumNode) {
//					v = new PointerEnumSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//				} else if (correspondingNode instanceof UnionNode)
//					v = new PointerUnionSymbolicVariable(name,
//							table.getCurrentNameSpace() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + realType,
//							scopeLevel);
//
//				if (v != null) {
//					((PointerSymbolicVariable) v)
//							.setSize(function.getFunctionConfig().getBoundOfArray().getLower() + "");
//				}
//			} else {
//				// dont support this realType
//			}

            if (v != null) {
                table.add(v);

                IASTInitializer initialization = declarator.getInitializer();

                if (initialization != null) {
                    String ini = v.getName() + initialization.getRawSignature();

                    IASTNode ast = Utils.convertToIAST(ini);
                    ast = Utils.shortenAstNode(ast);

                    new BinaryAssignmentParser().parse(ast, table, callTable);
                }
            }
        }
    }

    /**
     * Get type of variable. If the type of variable is <b>auto</b>, we replace this
     * type by corresponding type
     *
     * @param stm3
     *            Represent the declaration
     * @param declarator
     *            Represent the current declarator
     * @return
     */
    private String getType(IASTSimpleDeclaration stm3, IASTDeclarator declarator) {
        String decl = stm3.getDeclSpecifier().getRawSignature();
        String type;

        if (VariableTypes.isAuto(decl)) {
            String initialization = declarator.getInitializer().getChildren()[0].getRawSignature();
            /*
             * Predict the type of variable based on its initialization
             */
            type = VariableTypes.getTypeOfAutoVariable(initialization);

        } else {
            type = decl;
            /*
             * Check the variable is pointer or not
             *
             * The first child is corresponding to the left side. For example, considering
             * "int a = z*y", we parse the first child (its content: "int a")
             */
            IASTNode firstChild = declarator.getChildren()[0];
            if (firstChild instanceof CPPASTPointer)
                type += "*";

            if (declarator.getChildren().length >= 2) {
                IASTNode secondChild = declarator.getChildren()[1];
                if (secondChild instanceof CPPASTArrayModifier)
                    type += "[]";
            }
        }
        return type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public void setScopeLevel(int scopeLevel) {
        this.scopeLevel = scopeLevel;
    }

    public IFunctionNode getFunction() {
        return function;
    }

    public void setFunction(IFunctionNode function) {
        this.function = function;
    }
}
