package com.dse.parser.externalvariable;

import com.dse.logger.AkaLogger;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Find all static variables of a function
 * <p>
 * Remain: not detected variable through setter and getter yet
 */
public class StaticVariableDetecter implements IVariableDetecter {

    private final static AkaLogger logger = AkaLogger.get(StaticVariableDetecter.class);
    /**
     * Represent function
     */
    private IFunctionNode function;

    public StaticVariableDetecter(IFunctionNode function) {
        this.function = function;
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File("datatest/duc-anh/Algorithm"));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setSizeOfDependencyGeneration_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setTypeDependency_enable(true);

        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "print_floyd(int)").get(0);

        StaticVariableDetecter detecter = new StaticVariableDetecter(function);
        List<IVariableNode> vars = detecter.findVariables();
        INode type = vars.get(0).resolveCoreType();
        System.out.println();
    }

    @Override
    public List<IVariableNode> findVariables() {
        List<IVariableNode> variableNodes = new ArrayList<>();

        if (function != null) {
            IASTFunctionDefinition ast = function.getAST();

            ASTVisitor visitor = new ASTVisitor() {
                @Override
                public int visit(IASTDeclaration declaration) {
                    if (declaration instanceof IASTSimpleDeclaration) {
                        IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
                        IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
                        if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_static && !declSpec.isConst()) {
                            List<StaticVariableNode> vars = generateVariable(simpleDeclaration);
                            variableNodes.addAll(vars);
                        }
                    }

                    return PROCESS_CONTINUE;
                }
            };

            visitor.shouldVisitDeclarations = true;

            ast.accept(visitor);
        }

        return variableNodes;
    }

    private List<StaticVariableNode> generateVariable(IASTSimpleDeclaration decList) {
        List<StaticVariableNode> staticVariables = new ArrayList<>();

        for (IASTDeclarator dec : decList.getDeclarators()) {
            String content = decList.getDeclSpecifier().getRawSignature()+ " " + dec.getRawSignature();
            IASTNode decItem = Utils.convertToIAST(content);
            if (decItem instanceof IASTDeclarationStatement)
                decItem = decItem.getChildren()[0];
            else if (!(decItem instanceof IASTSimpleDeclaration)) {
                decItem = decList.copy(IASTNode.CopyStyle.withLocations);
                int decLength = ((IASTSimpleDeclaration) decItem).getDeclarators().length;
                if (decLength == 0) {
                    ((IASTSimpleDeclaration) decItem).addDeclarator(dec);
                } else {
                    ((IASTSimpleDeclaration) decItem).getDeclarators()[0] = dec;
                    for (int i = 1; i < decLength; i++) {
                        ((IASTSimpleDeclaration) decItem).getDeclarators()[i] = null;
                    }
                }
            }

            StaticVariableNode v = new StaticVariableNode();

            v.setAST(decItem);
            v.setParent(function);
            v.setContext(function);

            staticVariables.add(v);
        }

        return staticVariables;
    }

    @Override
    public IFunctionNode getFunction() {
        return function;
    }

    @Override
    public void setFunction(IFunctionNode function) {
        this.function = function;
    }

}
