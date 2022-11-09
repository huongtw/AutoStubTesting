package com.dse.parser.normalizer;

import auto_testcase_generation.testdatagen.AbstractAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.structuregen.ChangedTokens;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.Utils;
//import com.ibm.icu.util.Calendar;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Use some normalizers to make the function to be standard
 *
 * @author ducanhnguyen
 */
public class FunctionNormalizer extends AbstractFunctionNormalizer implements IFunctionNormalizer {
	final static AkaLogger logger = AkaLogger.get(FunctionNormalizer.class);
	private List<AbstractFunctionNormalizer> normalizers = new ArrayList<>();

	public FunctionNormalizer() {
	}

	public FunctionNormalizer(IFunctionNode functionNode, List<AbstractFunctionNormalizer> normalizers) {
		this.functionNode = functionNode;
		this.normalizers = normalizers;
	}

	public static void main(String[] args) throws Exception {
		ProjectParser parser = new ProjectParser(new File(Paths.SAMPLE01));
		IFunctionNode function = (IFunctionNode) Search
				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "StackLinkedList::push(Node*)").get(0);
		FunctionNormalizer normalizer = new FunctionNormalizer();
		normalizer.addNormalizer(new ClassvsStructNormalizer(function));
		normalizer.normalize();
		System.out.println(normalizer.getNormalizedSourcecode());
		System.out.println(normalizer.getTokens().toString());
	}

	@Override
	public void normalize() {
		Date startTime = Calendar.getInstance().getTime();

		IFunctionNode clone = (IFunctionNode) functionNode.clone();

		for (AbstractFunctionNormalizer normalizer : normalizers) {
			// logger.debug(normalizer.getClass().getSimpleName() + "...");
			normalizer.setFunctionNode(clone);
			normalizer.normalize();
			String newSourceCode = normalizer.getNormalizedSourcecode();

			try {
				IASTNode node = Utils.getIASTTranslationUnitforCpp(newSourceCode.toCharArray()).getChildren()[0];

				if (node instanceof CPPASTProblemDeclaration)
					node = Utils.getIASTTranslationUnitforC(newSourceCode.toCharArray()).getChildren()[0];

				if (node instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition newAST = (IASTFunctionDefinition) node;
					clone.setAST(newAST);
				} else
					logger.error("Can not convert \n" + newSourceCode + "\n to AST");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		normalizeSourcecode = clone.getAST().getRawSignature();

		Date end = Calendar.getInstance().getTime();
		AbstractAutomatedTestdataGeneration.normalizationTime += end.getTime() - startTime.getTime();
	}

	@Override
	public ChangedTokens getTokens() {
		ChangedTokens mappingVars = new ChangedTokens();
		for (AbstractNormalizer normalizer : normalizers)
			mappingVars.addAll(normalizer.getTokens());
		return mappingVars;
	}

	public List<AbstractFunctionNormalizer> getNormalizers() {
		return normalizers;
	}

	public void setNormalizers(List<AbstractFunctionNormalizer> normalizers) {
		this.normalizers = normalizers;
	}

	public void addNormalizer(AbstractFunctionNormalizer normalizer) {
		normalizers.add(normalizer);
	}

}
