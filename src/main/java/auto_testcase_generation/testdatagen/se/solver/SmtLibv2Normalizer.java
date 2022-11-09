package auto_testcase_generation.testdatagen.se.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dse.util.IRegex;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;

import com.dse.parser.normalizer.AbstractNormalizer;
import auto_testcase_generation.testdatagen.se.CustomJeval;
import auto_testcase_generation.testdatagen.se.normalization.AbstractPathConstraintNormalizer;
import auto_testcase_generation.testdatagen.se.normalization.IPathConstraintNormalizer;
import auto_testcase_generation.utils.ASTUtils;

/**
 *
 * @author anhanh
 */
@Deprecated
public class SmtLibv2Normalizer extends AbstractPathConstraintNormalizer implements IPathConstraintNormalizer {
	public final String PREFIX_MARK = "tyuio"; // default

	public static void main(String[] args) {
		String[] samples = new String[] { "((((-((-(tvw_a)+(-1)*1+0))+0))+1+0))>0",
				"((tvwb_w)/((tvwhe)*(tvwhe)/10000))<19",
				"!((tvwkey)==tvwarray[(to_int*(((0)+(to_int*((tvwsize)+0)))/2+0))+0])",
				"tvwp[0+0+0][0+0+0]>=(-10)&&tvwp[0+0+0][0+0+0]<=20",
				"(to_int*(16807*((tvwseed)-(to_int*((tvwseed)/127773))*127773)-(to_int*((tvwseed)/127773))*2836))<0" };

		AbstractNormalizer norm = new SmtLibv2Normalizer();
		norm.setOriginalSourcecode(samples[0]);
		norm.normalize();
		System.out.println(norm.getNormalizedSourcecode());
	}

	@Override
	public void normalize() {
		ConvertNotEqual equalNorm = new ConvertNotEqual(originalSourcecode);
		equalNorm.normalize();
		normalizeSourcecode = equalNorm.getNormalizedSourcecode();

		Map<String, String> arrayItemMap = new HashMap<>();
		while (normalizeSourcecode.contains("[")) {
			ArrayList<String> arrayItems = getArrayItemList(normalizeSourcecode);

			for (String arrayItem : arrayItems)

				if (isSimpleArrayItem(arrayItem)) {
					StringBuilder arrayItemSmtLib = new StringBuilder("(" + Utils.getNameVariable(arrayItem) + " ");

					for (String index : Utils.getIndexOfArray(arrayItem)) {
						String shortenIndex = new CustomJeval().evaluate(index);

						if (shortenIndex.matches(IRegex.INTEGER_NUMBER_REGEX))
							arrayItemSmtLib.append(shortenIndex).append(" ");
						else {
							SmtLibNormalizer norm = new SmtLibNormalizer();
							norm.setOriginalSourcecode(index);
							norm.normalize();

							if (norm.getNormalizedSourcecode().startsWith("("))
								arrayItemSmtLib.append(norm.getNormalizedSourcecode()).append(" ");
							else
								arrayItemSmtLib.append("(").append(norm.getNormalizedSourcecode()).append(")").append(" ");
						}

					}

					arrayItemSmtLib.append(") ");
					//
					String newName = PREFIX_MARK + arrayItemMap.size();
					arrayItemMap.put(newName, arrayItemSmtLib.toString());
					normalizeSourcecode = normalizeSourcecode.replace(arrayItem, newName);
				}
		}

		//
		SmtLibNormalizer norm = new SmtLibNormalizer();
		norm.setOriginalSourcecode(normalizeSourcecode);
		norm.normalize();
		normalizeSourcecode = norm.getNormalizedSourcecode();

		//
		for (String arrayItem : arrayItemMap.keySet())
			normalizeSourcecode = normalizeSourcecode.replace(arrayItem, arrayItemMap.get(arrayItem));
	}

	/**
	 * @param arrayItem
	 * @return true if the input does not contains array item inside it.
	 */
	private boolean isSimpleArrayItem(String arrayItem) {
		int numArray = 0;
		for (Character ch : arrayItem.toCharArray()) {
			if (ch == '[')
				numArray++;
			else if (ch == ']')
				numArray--;
			if (numArray == 2)
				return false;
		}
		return true;

	}

	private ArrayList<String> getArrayItemList(String expression) {
		ArrayList<String> arrayItemList = new ArrayList<>();
		List<ICPPASTArraySubscriptExpression> arrayItemASTs = ASTUtils
				.getArraySubscriptExpression(Utils.convertToIAST(expression));

		for (ICPPASTArraySubscriptExpression arrayItemAST : arrayItemASTs)
			if (!arrayItemList.contains(arrayItemAST.getRawSignature()))
				arrayItemList.add(arrayItemAST.getRawSignature());
		return arrayItemList;
	}
}
