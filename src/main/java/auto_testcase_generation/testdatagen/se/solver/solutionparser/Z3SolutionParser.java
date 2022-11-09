package auto_testcase_generation.testdatagen.se.solver.solutionparser;

import auto_testcase_generation.testdatagen.se.CustomJeval;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import com.dse.logger.AkaLogger;
import org.apache.log4j.Level;

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
 * @author anhanh
 */
public class Z3SolutionParser implements IZ3SolutionParser {
	final static AkaLogger logger = AkaLogger.get(Z3SolutionParser.class);

	private List<Z3Solution> z3Solutions = new ArrayList<>();
	public Z3SolutionParser() {
	}

	public static void main(String[] args) {
//		String Z3Solution = "(error \"line 6 column 30: invalid function application, wrong number of arguments\")\nsat\n(model\n\n (define-fun tvwp ((x!1 Int) (x!2 Int)) Int\n (ite (and (= x!1 0) (= x!2 0)) 10\n 10))\n)";
//		String Z3Solution = "sat\n" +
//		"(model \n" +
//				"  (define-fun tvw_a ((x!1 Int)) Int\n" +
//				"    (ite (= x!1 0) 22\n" +
//				"      110))\n" +
//		")";

//		String Z3Solution = "sat\n" +
//				"(model \n" +
//				"  (define-fun ____aka____a ((x!0 Int)) Int\n" +
//				"    1)\n" +
//				"  (define-fun ____aka____b ((x!0 Int)) Int\n" +
//				"    0)\n" +
//				")\n";

//		String Z3Solution  = "sat\n" +
//				"(model \n" +
//				"  (define-fun ____aka____a ((x!0 Int)) Int\n" +
//				"    (ite (= x!0 2) 0\n" +
//				"      (- 1)))\n" +
//				"  (define-fun ____aka____b ((x!0 Int)) Int\n" +
//				"    0)\n" +
//				")";

		String Z3Solution = "sat\n" +
				"(model \n" +
				"  (define-fun x () Bool\n" +
				"    true)\n" +
				")";
		System.out.println(new Z3SolutionParser().getSolution(Z3Solution));
	}

	@Override
	public String getSolution(String Z3Solution) {
		logger.setLevel(Level.OFF);
		List<String> solution = new ArrayList<>();
		if (Z3Solution.contains(ISymbolicExecution.UNSAT_IN_Z3)) {
			return NO_SOLUTION;

		} else {
			String[] lines = Z3Solution.replace("\r", "\n").split("\n");

			int count = 0;
			while (count < lines.length) {
				String line = lines[count].trim();

				logger.debug("");
				logger.debug("Parse \"" + line + "\"");

				switch (getTypeOfLine(line)) {

					case KHAI_BAO_ID_PRIMITIVE_VAR: {
						logger.debug("is KHAI_BAO_ID_PRIMITIVE_VAR");
						String name = getNameInDefineFun(line);
						z3Solutions.add(new PrimitiveZ3Solution(name));
						logger.debug("Name = " + name);
						break;
					}

					case KHAI_BAO_ID_ARRAY_VAR: {
						logger.debug("is KHAI_BAO_ID_ARRAY_VAR");

						String name = getNameInDefineFun(line);
						logger.debug("Name = " + name);

						List<String> indexVarNames = getIndexesInDefineFun(line);
						logger.debug("indexVarNames = " + indexVarNames);

						z3Solutions.add(new ArrayZ3Solution(name, indexVarNames));
						break;
					}

					case IF_THEN_ELSE_ID: {
						logger.debug("is IF_THEN_ELSE_ID");
						// merge with below lines
						line = merge(count, lines, line);
						logger.debug("Merge with below line: line = " + line);
						parseIte(line, solution, getLatestZ3Solution(z3Solutions));
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

					case UNSUPPORTED_ID: {
						logger.debug("is UNSUPPORTED_ID");
						break;
					}

					case VALUE_ID: {
						logger.debug("is VALUE_ID");
						String value = getValueOfVariable(line);
						value = new CustomJeval().evaluate(value);
						logger.debug("value = " + value);
						// Check if value exists
						if(value.isEmpty()) {
							logger.debug("No value for line: " + line);
							break;
						}

						if (!z3Solutions.isEmpty()) {
							Z3Solution latestZ3Solution = getLatestZ3Solution(z3Solutions);
							String name = latestZ3Solution.getName();
							if (latestZ3Solution instanceof PrimitiveZ3Solution) {
								solution.add(String.format("%s=%s", name, value));
							} else if (latestZ3Solution instanceof ArrayZ3Solution) {
//							if (((ArrayZ3Solution) latestZ3Solution).getValues().size() == 0)
								solution.add(String.format("%s[<other indexes>]=%s", name, value));
							}
						}
						break;
					}

					default: {
						logger.debug("Ignore");
						break;
					}
				}
				count++;
			}

			return normalizeSolution(solution);
		}
	}

	public void parseIte(String line, List<String> solution, Z3Solution latestZ3Solution) {
		if (!(latestZ3Solution instanceof ArrayZ3Solution))
			return;
		String currentVarName = latestZ3Solution.getName();

		// set value of specific elements
		String valueIfTrue = getValueOfIte(line);
		logger.debug("valueIfTrue = " + valueIfTrue);
		if (valueIfTrue != null && valueIfTrue.length() > 0) {
			ArrayList<String> indexList = getIndex(line);
			logger.debug("indexes = " + indexList);

			ArrayList<String> indexVariableList = getIndexVariable(line);
			logger.debug("index vars =  " + indexVariableList);

			for (int i = 0; i < indexList.size(); i++) {
				String newValue = String.format("%s=%s", currentVarName + "[" + indexList.get(i) + "]", valueIfTrue);
				solution.add(newValue);
				((ArrayZ3Solution) latestZ3Solution).getValues().add(newValue);
			}
		}

		// set value of other elements
		String valueIfFalse = getValueInFalseBranch(line);
		logger.debug("valueIfFalse = " + valueIfFalse);
		if (valueIfFalse != null && valueIfFalse.length() > 0) {
			String newValue = String.format("%s[<other indexes>]=%s", currentVarName, valueIfFalse);
			solution.add(newValue);
			((ArrayZ3Solution) latestZ3Solution).getValues().add(newValue);
		}
	}

	private Z3Solution getLatestZ3Solution(List<Z3Solution> z3Solutions) {
		return z3Solutions.get(z3Solutions.size() - 1);
	}

	private String merge(int count, String[] lineList, String line) {
		while (count + 1 <= lineList.length - 1) {
			String nextLine = lineList[count + 1];
			if (getTypeOfLine(nextLine) == VALUE_ID) {
				line = line + " " + nextLine.trim() + " ";
				count += 1;
				continue;
			} else
				break;
		}
		return line.trim();
	}

	private String normalizeSolution(List<String> solution) {
		String solutionInStr = "";
		for (String item : solution) {
			// Restore solution to its original format
			item = item.replace(ISymbolicVariable.PREFIX_SYMBOLIC_VALUE, "")
					.replace(ISymbolicVariable.SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES, ".")
					.replace(ISymbolicVariable.ARRAY_CLOSING, "]").replace(ISymbolicVariable.ARRAY_OPENING, "[");
			solutionInStr += item + ";";
		}
		return solutionInStr;
	}

	private String getNameInDefineFun(String defineFun) {
		StringBuilder nameFunction = new StringBuilder();
		Matcher m = Pattern.compile("\\(define-fun\\s+(\\w+)").matcher(defineFun);
		while (m.find()) {
			nameFunction = new StringBuilder(m.group(1));
			break;
		}
		return nameFunction.toString();
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

	private String getName(String defineFun) {
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

		else if (line.equals(IZ3SolutionParser.UNSUPPORTED))
			return IZ3SolutionParser.UNSUPPORTED_ID;

		return IZ3SolutionParser.VALUE_ID;
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
//	private String getValueOfIte(String ifThenElse) {
//		String value = "";
//		Matcher m = Pattern.compile("(\\(=\\s\\w+!\\d+\\s\\d+\\)\\s*)+(.*)").matcher(ifThenElse);
//		while (m.find()) {
//			value = m.group(2).replace(") ", "").replace(" ", "");
//			break;
//		}
//		return value;
//	}

	private String getValueInFalseBranch(String ifThenElse) {
		Matcher m = Pattern.compile("((\\s+)[0-9\\.]+)\\s+([0-9\\.]+)").matcher(ifThenElse);
		boolean found = m.find();
		if (found)
			return m.group(3);
		//
		m = Pattern.compile("((\\s+)[0-9\\.]+)\\s+([a-zA-Z]+![0-9\\.]+)").matcher(ifThenElse);
		found = m.find();
		if (found)
			return m.group(3);

		// negative
		m = Pattern.compile("\\s+(-\\s+[0-9\\.]+)\\)\\)").matcher(ifThenElse);
		found = m.find();
		if (found)
			return m.group(1).replace("- ", "");
		else return "";
	}

	private String getValueOfIte(String ifThenElse) {
		Matcher m = Pattern.compile("\\(\\s*ite\\s*\\(\\s*=\\s*([a-zA-Z]+)!([0-9]+)\\s+([0-9]+)\\)\\s+([0-9]+)").matcher(ifThenElse);
		if (m.find()) {
			return m.group(4);
		}
		// negative
		m = Pattern.compile("\\(\\s*ite\\s*\\(\\s*=\\s*([a-zA-Z]+)!([0-9]+)\\s+([0-9]+)\\)\\s+\\((-\\s*[0-9]+)").matcher(ifThenElse);
		if (m.find()) {
			return m.group(4).replace("- ", "-");
		}
		return "";
	}
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
			Matcher m = Pattern.compile("-*\\s*[0-9\\.E]+").matcher(line);
			while (m.find()) {
				value = m.group(0).replace(" ", "");
				break;
			}
		}

		if (value.isEmpty() && line.endsWith(")") && !line.startsWith("(")) {
			int end = line.indexOf(")");
			value = line.substring(0, end);
		}

		return value;
	}
}
