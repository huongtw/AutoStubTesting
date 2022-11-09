package com.dse.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dse.environment.Environment;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

import com.dse.parser.normalizer.AbstractPreprocessorParser;

/**
 * Get all preprocessors in the current files and the included files
 *
 * @author ducanhnguyen
 */
public class PreprocessorParser extends AbstractPreprocessorParser implements IPreprocessorParser {

    public PreprocessorParser(INode ast) {
        if (ast instanceof IFunctionNode)
            functionNode = (IFunctionNode) ast;
    }

    public static void main(String[] args) throws Exception {
        ProjectParser projectParser = new ProjectParser(new File("/mnt/e/akautauto/datatest/lamnt/macro/"));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        INode functionNode = Search
                .searchNodes(projectRoot, new FunctionNodeCondition(), "main()").get(0);
        PreprocessorParser p = new PreprocessorParser(functionNode);

        ASTVisitor visitor = new ASTVisitor() {
            public List<IASTName> names = new ArrayList<>();
            public List<IASTExpression> expressions = new ArrayList<>();
            public List<IASTStatement> statements = new ArrayList<>();

            @Override
            public int visit(IASTName name) {
                names.add(name);
                return super.visit(name);
            }

            @Override
            public int visit(IASTExpression expression) {
                expressions.add(expression);
                return super.visit(expression);
            }

            @Override
            public int visit(IASTStatement statement) {
                statements.add(statement);
                return super.visit(statement);
            }
        };

        visitor.shouldVisitExpressions = true;
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitNames = true;

        ((FunctionNode) functionNode).getAST().accept(visitor);

        System.out.println(p.getAllPreprocessors());
    }

    /**
     * Get all preprocessor macro definitions nodes of the given unit including
     * the included headers
     *
     * @return
     * @throws Exception
     */
    public List<PreprocessorMacroDefinitionNode> getAllPreprocessors() throws Exception {
        List<PreprocessorMacroDefinitionNode> macros = new ArrayList<>();
        macros.addAll(getMacrosInIncludedFiles(functionNode));
        macros.addAll(getMacrosInCurrentFile(functionNode));
        return macros;
    }

    private List<PreprocessorMacroDefinitionNode> getMacrosInIncludedFiles(INode functionNode) throws Exception {
        List<PreprocessorMacroDefinitionNode> macros = new ArrayList<>();
        List<Dependency> dependencies = Utils.getSourcecodeFile(functionNode).getDependencies();

        for (Dependency d : dependencies)
            if (d instanceof IncludeHeaderDependency) {
                INode included = d.getEndArrow();
                if (included instanceof ISourcecodeFileNode) {

                    IASTTranslationUnit unit = ((ISourcecodeFileNode) included).getAST();
                    int functionLocation = ((IFunctionNode) functionNode).getAST().getFileLocation().getNodeOffset();
                    macros.addAll(getPreprocessors(unit, functionLocation));
                }
            }

        return macros;
    }

    private List<PreprocessorMacroDefinitionNode> getMacrosInCurrentFile(INode functionNode) throws Exception {
        INode currentFile = Utils.getSourcecodeFile(functionNode);
        IASTTranslationUnit unit = ((ISourcecodeFileNode) currentFile).getAST();
        return getPreprocessors(unit, ((IFunctionNode) functionNode).getAST().getFileLocation().getNodeOffset()
                + ((IFunctionNode) functionNode).getAST().getFileLocation().getNodeLength());
    }

    /**
     * Get all preprocessor macro definitions nodes of the given unit that
     * defined in this unit
     *
     * @param unit
     * @return
     * @throws Exception
     */
    private List<PreprocessorMacroDefinitionNode> getPreprocessors(IASTTranslationUnit unit, int functionLocation)
            throws Exception {
        List<PreprocessorMacroDefinitionNode> macros = new ArrayList<>();
        if (unit != null) {
            IASTPreprocessorMacroDefinition[] press = unit.getMacroDefinitions();
            for (IASTPreprocessorMacroDefinition pres : press)
                if (pres instanceof IASTPreprocessorObjectStyleMacroDefinition) {
                    IASTFileLocation location = pres.getExpansionLocation();

                    if (location.getNodeOffset() < functionLocation) {
                        PreprocessorMacroDefinitionNode macroNode = null;

                        if (pres.getClass().getSimpleName().equals("ASTMacroDefinition"))
                            macroNode = new MacroDefinitionNode();
                        else if (pres.getClass().getSimpleName().equals("ASTFunctionStyleMacroDefinition"))
                            macroNode = new FunctionStyleMacroDefinitionNode();

                        if (macroNode != null) {
                            macroNode.setAST(pres);
                            macros.add(macroNode);
                        }
                    }

                } else
                    throw new Exception("Dont support " + pres.getRawSignature());
        }

        return macros;
    }
}
