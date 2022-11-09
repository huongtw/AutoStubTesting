package auto_testcase_generation.testdatagen.se.solver;

import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PointerOrArrayBound;
import com.dse.boundary.PrimitiveBound;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.NewVariableInSe;
import auto_testcase_generation.testdatagen.se.Parameters;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import com.dse.config.IFunctionConfigBound;
import com.dse.environment.Environment;
import com.dse.parser.object.*;
import com.dse.testdata.gen.module.type.PointerTypeInitiation;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generate SMT-Lib file
 *
 * @author anhanh
 */
public class SmtLibGeneration implements ISmtLibGeneration {
    final static AkaLogger logger = AkaLogger.get(SmtLibGeneration.class);

    // List of test cases
    private final List<IVariableNode> variables;
    // List of path constraints
    private final List<PathConstraint> constraints;
    // SMT-Lib content
    private String smtLib = "";

    private Set<NewVariableInSe> newVariableInSes;
    private ICommonFunctionNode functionNode;

    private SmtLibGeneration(List<IVariableNode> variables, List<PathConstraint> constraints) {
        this.variables = variables.stream()
                .filter(v -> !(v instanceof ExternalVariableNode && v.isConst()))
                .collect(Collectors.toList());

        this.constraints = constraints;
    }

    public SmtLibGeneration(Parameters params, List<PathConstraint> constraints, ICommonFunctionNode functionNode,
                            Set<NewVariableInSe> newVariableInSes) {
        this(params, constraints);
        this.newVariableInSes = newVariableInSes;
        this.functionNode = functionNode;
    }

    public SmtLibGeneration(List<IVariableNode> variables, List<PathConstraint> constraints, ICommonFunctionNode functionNode,
                            Set<NewVariableInSe> newVariableInSes) {
        this(variables, constraints);
        this.functionNode = functionNode;
        this.newVariableInSes = newVariableInSes;
    }

    @Override
    public void generate() throws Exception {
        smtLib = ISmtLibGeneration.OPTION_TIMEOUT + SpecialCharacter.LINE_BREAK +
//				ISmtLibGeneration.NULL_VALUE + SpecialCharacter.LINE_BREAK +
                getDeclarationFun(-1, variables, "", functionNode) + SpecialCharacter.LINE_BREAK + SpecialCharacter.LINE_BREAK;

        // Generate body of the smt-lib file
//		if (constraints.size() == 0)
//			smtLib = EMPTY_SMT_LIB_FILE;
//		else {
        for (PathConstraint constraint : constraints) {
            String constraint_string = constraint.getConstraint();
            switch (constraint_string) {
                case ISymbolicExecution.NO_SOLUTION_CONSTRAINT:
                    smtLib = EMPTY_SMT_LIB_FILE;
                    return;
                case ISymbolicExecution.ALWAYS_TRUE_CONSTRAINT:
                    // nothing to do
                    break;
                default:
                    SmtLibv2Normalizer2 normalizer = new SmtLibv2Normalizer2(
                            constraint_string
//                                    .replace(
//                                            ISymbolicVariable.SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES,
//                                            "."
//                                    )
                    );
                    normalizer.normalize();

                    String normalizedSourceCode = normalizer.getNormalizedSourcecode();
                    if (normalizedSourceCode != null && !normalizedSourceCode.isEmpty()) {
                        smtLib += "(assert" + normalizedSourceCode + ")" + SpecialCharacter.LINE_BREAK;
                    } else {
                        // If we can not normalize the constraint, we ignore it
                        // :)
                    }
                    break;
            }

        smtLib += ISmtLibGeneration.SOLVE_COMMAND;
		}
        // UNDER CONSTRUCTION
        // Remove all brackets encapsulated a string
        smtLib = smtLib.replaceAll("\\(\"", "\"").replaceAll("\"\\)", "\"");
    }

    private String getArgDecla(int n) {
        if (n == 0)
            return "Int";

        String argDecla = "";
        for (int i = 0; i < n; i++)
            argDecla += "Int ";
        return argDecla.substring(0, argDecla.length() - 1);
    }

    private static final int MAX_LOOP = 5;
    private int loopCount = 0;
    private INode prevStructureNode;

    private String getDeclarationFunForPointer(int depth, String name) throws Exception {
        String fullName = name;
        String tmp1 = generateTab(depth) + String.format("(declare-fun %s () Int)", fullName);
        String tmp2 = generateTab(depth) + "; for pointer: value = 0 (means NULL),  value = 1 (not NULL)";
        String tmp3 = generateTab(depth) + String.format("(assert (and (>= %s %s) (<= %s %s)))\n", fullName, "0", fullName, "1");
        return tmp1 + "\n" + tmp2 + "\n" + tmp3 + "\n";
    }

    /**
     * Generate "(declare-fun...)"
     *
     * @return
     * @throws Exception
     */
    private String getDeclarationFun(int depth, List<IVariableNode> variables, String prefix, ICommonFunctionNode functionNode) throws Exception {
        depth++;
        logger.debug("getDeclarationFun");
        StringBuilder output = new StringBuilder();
        if (variables.size() > 0) {
            for (IVariableNode var : variables) {
                logger.debug("Analyze " + var.getName());
//				output.append("\n" + generateTab(depth) + "; -----------------------\n");
                String type = var.getRealType().replaceAll("\\s+", " ");

                output.append(/*generateTab(depth) + */"; Variable \"" + var.getName() + "\" , real type = \"" + type + "\", " + var.getClass() + "\n");

                type = VariableTypeUtils.removeRedundantKeyword(type);
                type = VariableTypeUtils.deleteReferenceOperator(type);

                String originalName = var.getName();
                String modifiedName = prefix + ISymbolicVariable.PREFIX_SYMBOLIC_VALUE + var.getName();

                modifiedName = VariableNodeTable.normalizeNameOfVariable(modifiedName);

                // PRIMITIVE TYPES
                if (VariableTypeUtils.isNumBasicFloat(type)) {
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Real)\n", modifiedName));
                    output.append(generateTab(depth) + addBoundInStr(originalName, modifiedName, type, functionNode));
                    loopCount = 0;
                    prevStructureNode = null;

                } else if (VariableTypeUtils.isBoolBasic(type)
                        || VariableTypeUtils.isChBasic(type)
                        || VariableTypeUtils.isNumBasicInteger(type)
                        || VariableTypeUtils.isStdInt(type)
                        || VariableTypeUtils.isEnumNode(type, Environment.getInstance().getProjectNode())) {
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Int)\n", modifiedName));
                    output.append(generateTab(depth) + addBoundInStr(originalName, modifiedName, type, functionNode));
                    loopCount = 0;
                    prevStructureNode = null;

                }

                // PRIMITIVE POINTER
                else if (VariableTypeUtils.isNumIntegerMultiLevel(type)
                        || VariableTypeUtils.isBoolMultiLevel(type)
                        || VariableTypeUtils.isChMultiLevel(type)) {
//                    int level = PointerTypeInitiation.getLevel(type);
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Int)\n", modifiedName));
                    output.append(generateTab(depth) + addBoundInStr(originalName, modifiedName, type, functionNode));
                    loopCount = 0;
                    prevStructureNode = null;

                } else if (VariableTypeUtils.isNumFloatMultiDimension(type)) {
//                    int size = Utils.getIndexOfArray(type).size();
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Real)\n", modifiedName));
                    output.append(generateTab(depth) + addBoundInStr(originalName, modifiedName, type, functionNode));
                    loopCount = 0;
                    prevStructureNode = null;

                }

                // PRIMITIVE ARRAY
                else if (VariableTypeUtils.isNumIntergerMultiDimension(type)
                        || VariableTypeUtils.isBoolMultiDimension(type)
                        || VariableTypeUtils.isChMultiDimension(type)) {
//                    int size = Utils.getIndexOfArray(type).size();
                    if (type.endsWith("]")) {
                        String elementType = type.substring(0, type.indexOf("[")).trim();
                        // check if array size is specified
                        if (type.length() > elementType.length() + 2) {
                            continue;
                        }
                    }
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Int)\n", modifiedName));
                    output.append(generateTab(depth) + addBoundInStr(originalName, modifiedName, type, functionNode));
                    loopCount = 0;
                    prevStructureNode = null;

                } else if (VariableTypeUtils.isNumFloatMultiLevel(type)) {
//                    int level = PointerTypeInitiation.getLevel(type);
                    if (type.endsWith("]")) {
                        String elementType = type.substring(0, type.indexOf("[")).trim();
                        // check if array size is specified
                        if (type.length() > elementType.length() + 2) {
                            continue;
                        }
                    }
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Real)\n", modifiedName));
                    output.append(generateTab(depth) + addBoundInStr(originalName, modifiedName, type, functionNode));
                    loopCount = 0;
                    prevStructureNode = null;

                }

                // STRUCT POINTER
                else if (VariableTypeUtils.isStructureMultiLevel(type)) {
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Int)\n", modifiedName));
                    output.append(generateTab(depth) + String.format("(assert (>= %s %s))", modifiedName, "0"));
//					INode correspondingType = var.resolveCoreType();
//					if (correspondingType == prevStructureNode)
//						loopCount++;
//					else {
//						loopCount = 0;
//						prevStructureNode = null;
//					}
//					prevStructureNode = correspondingType;
//					if (loopCount < MAX_LOOP) {
//						if (correspondingType instanceof StructNode) {
//							List<IVariableNode> allAttributes = ((StructNode) correspondingType).getAttributes();
//
//							PrimitiveBound bound = functionNode.getFunctionConfig().getBoundOfPointer();
//							if (bound != null)
//								for (long i = bound.getLowerAsDouble(); i < bound.getUpperAsDouble(); i++) {
//									output.append(String.format("; Element %s of %s (START)\n", i, modifiedName));
//									//TODO: infinity loop
//									output.append(getDeclarationFun(allAttributes,
//											modifiedName + ISymbolicVariable.ARRAY_OPENING + i + ISymbolicVariable.ARRAY_CLOSING,
//											functionNode));
//									output.append(String.format("; Element %s of %s (END)\n\n", i, modifiedName));
//								}
//
//						} else
//							output.append(String.format("; do not support this type\n"));
//					} else
//						logger.debug("break infinity loop");
                } else if (VariableTypeUtils.isStructureSimple(type)) {
                    output.append(generateTab(depth) + String.format("(declare-fun %s () Int)\n", modifiedName));
                    output.append(generateTab(depth) + String.format("(assert (>= %s %s))", modifiedName, "0"));
                } else if (VariableTypeUtils.isStr(type)) {
                    output.append(generateTab(depth) + String.format("(declare-fun %s () String)\n", modifiedName));
                } else {
                    output.append(generateTab(depth)).append(String.format("; do not support this type\n"));
                }
            }
        }
        depth--;
        return output.toString();
    }

    private String generateTab(int depth) {
        String tab = "";
        for (int i = 0; i < depth; i++)
            tab += "\t";
        return tab;
    }

    public static void main(String[] args) {
        System.out.println(mapConstToZ3("34.3E38"));

    }

    private static String mapConstToZ3(String value) {
        Pattern pattern = Pattern.compile("E(\\d+)");
        Matcher matcher = pattern.matcher(value);


        if (matcher.find()) {
            String group = matcher.group(0);
            int time = Integer.parseInt(matcher.group(1));
            String full = "1";
            for (int i = 0; i < time; i++)
                full += "0";
            String other = value.replace(group, "");
            value = String.format("(* %s %s)", other, full);
        }

        return value;
    }

    private String addBoundInStr(String originalName, String modifiedName, String type, ICommonFunctionNode functionNode) {
        String output = "";
        IFunctionConfigBound b = functionNode.getFunctionConfig().getBoundOfArgumentsAndGlobalVariables().get(originalName);

        if (b == null) {
            b = Environment.getBoundOfDataTypes().getBounds().get(type);
            if (VariableTypeUtils.isNumBasic(type)) {
                b = functionNode.getFunctionConfig().getBoundOfOtherNumberVars();
            } else if (VariableTypeUtils.isChBasic(type)) {
                b = functionNode.getFunctionConfig().getBoundOfOtherCharacterVars();
            }
        }

        if (b != null) {
            if (b instanceof PrimitiveBound) {
                String lower = ((PrimitiveBound) b).getLower();
                lower = mapConstToZ3(lower);
                String upper = ((PrimitiveBound) b).getUpper();
                upper = mapConstToZ3(upper);
                output = String.format("(assert (and (>= %s %s) (<= %s %s)))\n", modifiedName, lower, modifiedName, upper);

            } else if (b instanceof MultiplePrimitiveBound) {
                MultiplePrimitiveBound bounds = (MultiplePrimitiveBound) b;
                for (PrimitiveBound bound : bounds) {
                    if (bounds.indexOf(bound) == 0) {
                        String lower = bound.getLower();
                        lower = mapConstToZ3(lower);
                        String upper = bound.getUpper();
                        upper = mapConstToZ3(upper);
                        output = String.format("(and (>= %s %s) (<= %s %s))", modifiedName, lower, modifiedName, upper);
                    } else {
                        String lower = bound.getLower();
                        lower = mapConstToZ3(lower);
                        String upper = bound.getUpper();
                        upper = mapConstToZ3(upper);
                        String constraint = String.format("(and (>= %s %s) (<= %s %s))", modifiedName, lower, modifiedName, upper);
                        output = String.format("(or %s %s)", constraint, output);
                    }
                }
                output = String.format("(assert %s)\n", output);
            } else if (b instanceof PointerOrArrayBound) {
                // bound of element type
                String elementType = "";
                if (type.endsWith("*")) {
                    // is pointer
                    elementType = type.substring(0, type.indexOf("*")).trim();
                } else if (type.endsWith("]")) {
                    // is array
                    elementType = type.substring(0, type.indexOf("[")).trim();
                }

                IFunctionConfigBound bound = null;
                if (VariableTypeUtils.isNumBasic(elementType)) {
                    bound = functionNode.getFunctionConfig().getBoundOfOtherNumberVars();
                } else if (VariableTypeUtils.isChBasic(elementType)) {
                    bound = functionNode.getFunctionConfig().getBoundOfOtherCharacterVars();
                }

                //
                if (bound instanceof PrimitiveBound) {
                    int size = ((PointerOrArrayBound) b).getIndexes().size();

                    String lower = ((PrimitiveBound) bound).getLower();
                    String upper = ((PrimitiveBound) bound).getUpper();

                    // "MIN", "MAX" ---> specific values
                    IFunctionConfigBound envBound = Environment.getBoundOfDataTypes().getBounds().get(elementType);
                    if (envBound instanceof PrimitiveBound) {
                        lower = lower.replace(IFunctionConfigBound.MIN_VARIABLE_TYPE, ((PrimitiveBound) envBound).getLower())
                                .replace(IFunctionConfigBound.MAX_VARIABLE_TYPE, ((PrimitiveBound) envBound).getUpper());
                        upper = upper.replace(IFunctionConfigBound.MIN_VARIABLE_TYPE, ((PrimitiveBound) envBound).getLower())
                                .replace(IFunctionConfigBound.MAX_VARIABLE_TYPE, ((PrimitiveBound) envBound).getUpper());
                    }

                    lower = mapConstToZ3(lower);
                    upper = mapConstToZ3(upper);

//                    output = addBoundOfElement(modifiedName, elementType, ((PointerOrArrayBound) b).getIndexes(), lower, upper);
                    output = addBoundOfPointerOrArrayElement(modifiedName, elementType, ((PointerOrArrayBound) b).getIndexes());
                }
            }
        } else {

        }
        return output;
    }

    private String addBoundOfPointerOrArrayElement(String name, String type, List<String> indexes) {
        String output = "";
        for (String i : indexes) {
            long lower = 0;
            long upper = 0;
            if (i.contains(IFunctionConfigBound.RANGE_DELIMITER)) {
                lower = Long.parseLong(i.split(IFunctionConfigBound.RANGE_DELIMITER)[0]);
                upper = Long.parseLong(i.split(IFunctionConfigBound.RANGE_DELIMITER)[1]);
            } else {
                lower = Long.parseLong(i);
                upper = Long.parseLong(i);
            }
            output += String.format("(assert(and (>= %s %s) (<= %s %s)))\n", name, lower, name, upper);
        }
        return output;
    }

    private String addBoundOfElement(String name, String type, List<String> size, String lower, String upper) {
        String output = "";

        if (size.size() == 1) {
            long dimen1 = Long.parseLong(size.get(0).contains(IFunctionConfigBound.RANGE_DELIMITER) ?
                    size.get(0).split(IFunctionConfigBound.RANGE_DELIMITER)[1] : size.get(0));
            dimen1 = dimen1 > MAX_DIMENSION_1 ? MAX_DIMENSION_1 : dimen1;

            for (long idx1 = 0; idx1 < dimen1; idx1++) {
                String nameEle = String.format("(%s %s)", name, idx1);
                String andIndex = String.format("(assert(and (>= %s %s) (<= %s %s)))\n",
                        nameEle, lower, nameEle, upper);
                output += andIndex;
            }

        } else if (size.size() == 2) {
            String s0;
            String s1;
            if (size.get(0).contains(":")) {
                s0 = new String(size.get(0).replace("0:", ""));
                s1 = new String(size.get(1).replace("0:", ""));
            } else {
                s0 = size.get(0);
                s1 = size.get(1);
            }
            long dimen1 = Long.parseLong(s0);
            long dimen2 = Long.parseLong(s1);

            dimen1 = dimen1 > MAX_DIMENSION_1 ? MAX_DIMENSION_1 : dimen1;
            dimen2 = dimen1 > MAX_DIMENSION_2 ? MAX_DIMENSION_2 : dimen2;

            for (long idx1 = 0; idx1 < dimen1; idx1++)
                for (long idx2 = 0; idx2 < dimen2; idx2++) {
                    String nameEle = String.format("(%s %s %s)", name, idx1, idx2);
                    String andIndex = String.format("(assert(and (>= %s %s) (<= %s %s)))\n",
                            nameEle, lower, nameEle, upper);
                    output += andIndex;
                }

        } else if (size.size() == 3) {
            long dimen1 = Long.parseLong(size.get(0));
            long dimen2 = Long.parseLong(size.get(1));
            long dimen3 = Long.parseLong(size.get(1));

            dimen1 = dimen1 > MAX_DIMENSION_1 ? MAX_DIMENSION_1 : dimen1;
            dimen2 = dimen1 > MAX_DIMENSION_2 ? MAX_DIMENSION_2 : dimen2;
            dimen3 = dimen3 > MAX_DIMENSION_3 ? MAX_DIMENSION_3 : dimen3;

            for (long idx1 = 0; idx1 < dimen1; idx1++)
                for (long idx2 = 0; idx2 < dimen2; idx2++)
                    for (long idx3 = 0; idx3 < dimen3; idx3++) {
                        String nameEle = String.format("(%s %s %s %s)", name, idx1, idx2, idx3);
                        String andIndex = String.format("(assert(and (>= %s %s) (<= %s %s)))\n",
                                nameEle, lower, nameEle, upper);
                        output += andIndex;
                    }
        }
        return output;
    }

    @Override
    public String getSmtLibContent() {
        return smtLib;
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public void setNewVariableInSes(Set<NewVariableInSe> newVariableInSes) {
        this.newVariableInSes = newVariableInSes;
    }

    public Set<NewVariableInSe> getNewVariableInSes() {
        return newVariableInSes;
    }

    public static final int MAX_DIMENSION_1 = 100; // to avoid too many element of array/pointer in smt-lib
    public static final int MAX_DIMENSION_2 = 10;
    public static final int MAX_DIMENSION_3 = 10;
}
