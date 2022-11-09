package auto_testcase_generation.testdatagen.se.solver.solutionparser;

import auto_testcase_generation.testdatagen.se.CustomJeval;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import com.dse.logger.AkaLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert solution in SMT-Solver z3 to the readable human format
 * <p>
 * <p>
 * Z3 link:https://github.com/Z3Prover/z3/releases
 *
 * use when use forall in smt-lib
 * @author anhanh
 */
@Deprecated
public class Z3SolutionParser3 implements IZ3SolutionParser {
    final static AkaLogger logger = AkaLogger.get(Z3SolutionParser.class);

    class PairSolution {
        String key;
        String value;

        PairSolution(String key, String element) {
            this.key = key;
            this.value = element;
        }

        @Override
        public String toString() {
            return key + ", " + value + "\n";
        }
    }

    class Elements extends ArrayList<PairSolution> {

    }

    private Elements elementInArrayMap = new Elements();
    private List<String> indexes;
    private List<String> output = new ArrayList<>();

    public Z3SolutionParser3() {
    }

    public static void main(String[] args) {
//		String Z3Solution = "(error \"line 6 column 30: invalid function application, wrong number of arguments\")\nsat\n(model\n\n (define-fun tvwp ((x!1 Int) (x!2 Int)) Int\n (ite (and (= x!1 0) (= x!2 0)) 10\n 10))\n)";

//		String Z3Solution = "sat\n" +
//		"(model \n" +
//		"  (define-fun tvw_n () Int\n" +
//		"    2)\n" +
//		"  (define-fun tvw_a ((x!0 Int)) Int\n" +
//		"    (ite (= x!0 0) 1\n" +
//		"      0))\n" +
//		")";

//		String Z3Solution = "(model \n" +
//				"  (define-fun AKAa ((x!0 Int)) Int\n" +
//				"    (let ((a!1 (ite (= x!0 12) 12 (ite (= x!0 11) 11 (ite (= x!0 130) 130 10)))))\n" +
//				"    (let ((a!2 (ite (= x!0 14) 14 (ite (= x!0 13) 13 a!1))))\n" +
//				"    (let ((a!3 (ite (= a!2 12) 999 (ite (= a!2 11) 99 (ite (= a!2 10) 9 2)))))\n" +
//				"      (ite (= a!2 130) 3423 (ite (= a!2 14) 453 (ite (= a!2 13) 4444 a!3)))))))\n" +
//				")\n";

        String Z3Solution = "  (define-fun AKAa ((x!0 Int)) Int\n" +
                "    (let ((a!1 (ite (= x!0 11) 11 (ite (= x!0 130) 130 (ite (= x!0 5) 5 10)))))\n" +
                "    (let ((a!2 (ite (= x!0 13) 13 (ite (= x!0 1) 1 (ite (= x!0 12) 12 a!1)))))\n" +
                "    (let ((a!3 (ite (= x!0 7) 7 (ite (= x!0 3) 3 (ite (= x!0 14) 14 a!2)))))\n" +
                "    (let ((a!4 (ite (= a!3 12) 999 (ite (= a!3 11) 99 (ite (= a!3 10) 9 16)))))\n" +
                "    (let ((a!5 (ite (= a!3 130) 3423 (ite (= a!3 14) 453 (ite (= a!3 13) 4444 a!4)))))\n" +
                "    (let ((a!6 (ite (= a!3 5) 6 (ite (= a!3 3) 4 (ite (= a!3 1) 2 a!5)))))\n" +
                "      (ite (= a!3 7) 8 a!6))))))))\n";
        Z3SolutionParser solutionParser = new Z3SolutionParser();
        solutionParser.getSolution(Z3Solution);
        logger.debug("Output = " + new Z3SolutionParser().getSolution(Z3Solution));
    }

    @Override
    public String getSolution(String Z3Solution) {
        StringBuilder solution = new StringBuilder();
        if (Z3Solution.equals(ISymbolicExecution.UNSAT_IN_Z3)) {

        } else {
            String[] lineList = Z3Solution.split("\n");
            String name = "";
            String value;
            boolean ignoreEndLine = false;

            for (String line : lineList) {
                logger.debug("");
                logger.debug("Parse \"" + line + "\"");
                switch (getTypeOfLine(line)) {
                    case KHAI_BAO_ID_PRIMITIVE_VAR: {
                        logger.debug("is KHAI_BAO_ID_PRIMITIVE_VAR");
                        if (elementInArrayMap.size() > 0) {
                            output.addAll(analyzeMap(elementInArrayMap, "a", indexes));
                        }
                        elementInArrayMap = new Elements();
                        indexes = new ArrayList<>();

                        ignoreEndLine = false;
                        name = getNameInDefineFun(line);
                        logger.debug("Name = " + name);
                        break;
                    }

                    case KHAI_BAO_ID_ARRAY_VAR: {
                        logger.debug("is KHAI_BAO_ID_ARRAY_VAR");
                        if (elementInArrayMap.size() > 0) {
                            output.addAll(analyzeMap(elementInArrayMap, name, indexes));
                        }
                        elementInArrayMap = new Elements();
                        indexes = new ArrayList<>();

                        ignoreEndLine = false;
                        name = getNameInDefineFun(line);
                        logger.debug("Name = " + name);

                        indexes = getIndexesInDefineFun(line);
                        logger.debug("Indexe names = " + indexes);
                        break;
                    }

                    case VALUE_ID: {
                        logger.debug("is VALUE_ID");
                        if (!ignoreEndLine) {
                            value = getValueOfVariable(line);
                            value = new CustomJeval().evaluate(value);

                            // ! is the signal of array
                            if (indexes.size() > 0)
                                elementInArrayMap.add(new PairSolution(indexes.get(0) + "=other", value));
                            else {
                                solution.append(name + "=" + value).append(",");
                            }
                        }
                        break;
                    }
                    case ERROR: {
                        logger.debug("is ERROR");
                        break;
                    }
                    case UNKNOWN_ID: {
                        logger.debug("is UNKNOWN_ID");
                        break;
                    }

                    case IF_THEN_ELSE_ID: {
                        logger.debug("is IF_THEN_ELSE_ID");
                        List<String> indexes = getIndex(line);
                        logger.debug("indexes = " + indexes);

                        List<String> indexesVar = getIndexVariable(line);
                        logger.debug("indexesVar = " + indexesVar);

                        List<String> values = getValueInIte(line);
                        logger.debug("values = " + values);

                        for (int i = 0; i < indexes.size(); i++) {
                            String key = String.format("%s=%s", indexesVar.get(i), indexes.get(i));
                            //	if (!elementInArrayMap.containsKey(key)) {
                            String v = values.get(i);
                            elementInArrayMap.add(new PairSolution(key, v));
                            logger.debug("Add key " + key + ", value = " + v);
                            //	}
                        }
                        break;
                    }

                    case LET_ID: {
                        logger.debug("is LET_ID or IF_THEN_ELSE_ID");
                        List<String> indexes = getIndex(line);
                        logger.debug("indexes = " + indexes);

                        List<String> indexesVar = getIndexVariable(line);
                        logger.debug("indexesVar = " + indexesVar);

                        String alternativeName = getNameInLetExpression(line);
                        logger.debug("alternativeName = " + alternativeName);

                        List<String> values = getValueInIte(line);
                        logger.debug("values = " + values);

                        for (int i = 0; i < indexes.size(); i++) {
                            String key = String.format("%s=%s", indexesVar.get(i), indexes.get(i));
//							if (!elementInArrayMap.containsKey(key)) {
                            String v = String.format("%s=%s", alternativeName, values.get(i));
                            elementInArrayMap.add(new PairSolution(key, v));
                            logger.debug("Add key " + key + ", value = " + v);
//							}
                        }

                        // Add the last value
                        String valueInFalse = getValueInFalseBranch(line);
                        if (valueInFalse != null && valueInFalse.length() > 0) {
                            logger.debug("valueInFalse = " + valueInFalse);
                            String k = String.format("%s=%s", indexesVar.get(0), "other");
                            String v = String.format("%s=%s", alternativeName, valueInFalse);
                            elementInArrayMap.add(new PairSolution(k, v));
                            logger.debug("Add key " + k + ", value = " + v);
                        }
                        break;
                    }
                }
            }
            if (elementInArrayMap.size() > 0) {
                output.addAll(analyzeMap(elementInArrayMap, name, indexes));
            }

            for (String o : output) {
                solution.append(o).append(",");
            }
            if (solution.lastIndexOf(",") > 0)
                solution = new StringBuilder(solution.substring(0, solution.lastIndexOf(",")));

            // Restore solution to its original format
            solution = new StringBuilder(solution.toString().replace(ISymbolicVariable.PREFIX_SYMBOLIC_VALUE, "")
                    .replace(ISymbolicVariable.SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES, ".")
                    .replace(ISymbolicVariable.ARRAY_CLOSING, "]").replace(ISymbolicVariable.ARRAY_OPENING, "[")
                    .replace(",", ";"));

            if (solution.length() > 0 && !solution.toString().endsWith(";"))
                solution.append(";");
        }

        if (solution != null && solution.toString().startsWith("=;"))
            solution = new StringBuilder(solution.substring(2));
        return solution.toString();
    }

    private List<String> analyzeMap(Elements constraints, String nameVar, List<String> indexes) {
        List<String> output = new ArrayList<>();
        logger.debug("constraints = " + constraints);
        for (PairSolution p1 : constraints) {
            for (String indexVar : indexes)
                if (p1.key.contains(indexVar)) {
                    logger.debug("Parse " + p1.key);
                    String index = p1.key.split("=")[1];
                    String value = p1.value;
                    if (p1.key.equals("x!0=other")) {
                        int a = 0;
                    }

                    value = find(elementInArrayMap, value, p1.key);

                    if (value.contains("=")) {
                        String n2 = value.split("=")[0];
                        for (PairSolution v2 : constraints)
                            if (v2.value.endsWith("=" + n2)) {
                                value = value.replace(n2, v2.value.split("=")[0]);

                                //
                                value = find(elementInArrayMap, value, v2.key);
                            }
                        if (value.contains("="))
                            value = value.split("=")[1];
                    }
                    output.add(nameVar + "[" + index + "]" + " = " + value);
                }
        }
        return output;
    }

    private String find(Elements constraints, String value, String key) {
        boolean found = true;
        while (found) {
            found = false;
            for (PairSolution p2 : constraints)
                if (p2.key.equals(value)) {
                    value = p2.value;
                    found = true;
                    break;
                }
//			else if (p2.key.equals(key) && !p2.value.equals(value)) {
//					String left = p2.value.split("=")[0];
//					String right = p2.value.split("=")[1];
//
//					String l = value.split("=")[0];
//					String r = value.split("=")[1];
//					if (l.equals(left)) {
//						value = right + "=" + r;
//					} else
//						value = left + "=" + r;
//				}
        }
        return value;
    }
    /**
     * " (ite (= x!1 0) 1\r" ---------> 0
     *
     * @param ifThenElse
     * @return
     */
    private ArrayList<String> getIndex(String ifThenElse) {
        ArrayList<String> indexList = new ArrayList<>();
        Matcher m = Pattern.compile("=\\s(\\w+)!(\\d+)\\s([^\\)]+)").matcher(ifThenElse);
        while (m.find())
            indexList.add(m.group(3));
        return indexList;
    }

    /**
     * " (ite (= x!1 0) 1\r" ----> "x!1"
     *
     * @param ifThenElse
     * @return
     */
    private ArrayList<String> getIndexVariable(String ifThenElse) {
        ArrayList<String> indexList = new ArrayList<>();
        Matcher m = Pattern.compile("=\\s(\\w+!\\d+)\\s([^\\)]+)").matcher(ifThenElse);
        while (m.find())
            indexList.add(m.group(1));
        return indexList;
    }

    private ArrayList<String> getValueInIte(String ifThenElse) {
        ArrayList<String> indexList = new ArrayList<>();
        Matcher m = Pattern.compile("\\(\\s*ite\\s*\\(\\s*=\\s*([a-zA-Z]+)!([0-9]+)\\s+([0-9]+)\\)\\s+([0-9]+)").matcher(ifThenElse);
        while (m.find()) {
            String v = m.group(4);
            indexList.add(v);
        }

        return indexList;
    }

    private String getValueInFalseBranch(String ifThenElse) {
        Matcher m = Pattern.compile("((\\s+)[0-9]+)\\s+([0-9]+)").matcher(ifThenElse);
        boolean found = m.find();
        if (found)
            return m.group(3);
        //
        m = Pattern.compile("((\\s+)[0-9]+)\\s+([a-zA-Z]+![0-9]+)").matcher(ifThenElse);
        found = m.find();
        if (found)
            return m.group(3);
        else return "";
    }

    private String getNameInLetExpression(String ifThenElse) {
        ifThenElse = ifThenElse.trim().replaceAll("\\(\\s*let\\s*\\(\\s*\\(", "");
        String name = ifThenElse.substring(0, ifThenElse.indexOf(" "));
        return name;
    }

    private String getNameInDefineFun(String defineFun) {
        StringBuilder nameFunction = new StringBuilder();
        Matcher m = Pattern.compile("\\(define-fun\\s+(\\w+)").matcher(defineFun);
        while (m.find()) {
            nameFunction = new StringBuilder(m.group(1));
            break;
        }

        // case "define-fun tvw_A ((x!1 Int)) Int"
//		m = Pattern.compile("(\\w+!\\d+)").matcher(defineFun);
//		while (m.find()) {
//			nameFunction.append("[").append(m.group(0)).append("]");
//		}

        return nameFunction.toString();
    }

    /**
     * "define-fun tvw_A ((x!1 Int)) Int" -> x!1
     *
     * @param defineFun
     * @return
     */
    private List<String> getIndexesInDefineFun(String defineFun) {
        List<String> indexes = new ArrayList<>();
        Matcher m = Pattern.compile("([a-zA-Z]+)!([0-9]+)").matcher(defineFun);
        while (m.find()) {
            indexes.add(m.group(0));
        }
        return indexes;
    }


    private int getTypeOfLine(String line) {
        line = line.trim();
        if (line.startsWith(IZ3SolutionParser.KHAI_BAO)) {
            if (line.contains("!"))
                return IZ3SolutionParser.KHAI_BAO_ID_ARRAY_VAR;
            else
                return IZ3SolutionParser.KHAI_BAO_ID_PRIMITIVE_VAR;
        } else if (line.startsWith(IZ3SolutionParser.IF_THEN_ELSE))
            return IZ3SolutionParser.IF_THEN_ELSE_ID;

        else if (line.startsWith(IZ3SolutionParser.LET))
            return IZ3SolutionParser.LET_ID;

        else if (line.contains(IZ3SolutionParser.MODEL))
            return IZ3SolutionParser.MODEL_ID;

        else if (line.equals(IZ3SolutionParser.END))
            return IZ3SolutionParser.END_ID;

        else if (line.equals(IZ3SolutionParser.SAT))
            return IZ3SolutionParser.SAT_ID;

        else if (line.equals(IZ3SolutionParser.UNSAT))
            return IZ3SolutionParser.UNSAT_ID;

        else if (line.equals(IZ3SolutionParser.UNKNOWN))
            return IZ3SolutionParser.UNKNOWN_ID;

        else if (line.startsWith("(error") || line.length() == 0)
            return IZ3SolutionParser.ERROR;

        return IZ3SolutionParser.VALUE_ID;
    }

//	private String getValueOfIte(String ifThenElse) {
//		String value = "";
//		Matcher m = Pattern.compile("(\\(=\\s\\w+!\\d+\\s\\d+\\)\\s*)+(.*)").matcher(ifThenElse);
//		while (m.find()) {
//			value = m.group(2).replace(") ", "").replace(" ", "");
//			break;
//		}
//		// Handle "      (ite (= a!1 13) 4444 a!2))))"
//		// value = "4444a!2))))" --> "4444"
//		if (value.contains("!")) {
//			value = value.replaceAll("[a-zA-Z]+!.+", "");
//		}
//		return value;
//	}

    private String getValueOfVariable(String line) {
        String value = "";
        final String DEVIDE = "/";
        final String NEGATIVE = "-";
        /*
         * Ex: (/ 1.0 10000.0))
         */
        if (line.contains(DEVIDE) && !line.contains(NEGATIVE)) {
            int start = line.indexOf("(") + 1;
            int end = line.indexOf(")");
            String reducedLine = line.substring(start, end);

            String[] elements = reducedLine.split(" ");
            if (elements.length >= 3)
                value = elements[1] + elements[0] + elements[2];

        } else
            /*
             * Ex: (- (/ 9981.0 10000.0))
             */
            if (line.contains(DEVIDE) && line.contains(NEGATIVE)) {
                int start = line.lastIndexOf("(") + 1;
                int end = line.indexOf(")");
                String reducedLine = line.substring(start, end);

                String[] elements = reducedLine.split(" ");
                if (elements.length >= 3)
                    value = NEGATIVE + "(" + elements[1] + elements[0] + elements[2] + ")";

            } else {
                line = line.trim();
                Matcher m = Pattern.compile("([0-9\\.]+)[\\)]+").matcher(line);
                while (m.find()) {
                    value = m.group(1).replace(" ", "");
                    break;
                }
            }
        return value;
    }
}
