package com.dse.parser.externalvariable;

import com.dse.logger.AkaLogger;
import com.dse.parser.ProjectParser;
import com.dse.parser.funcdetail.FunctionDetailTree;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.GlobalVariableNodeCondition;
import com.dse.util.NodeType;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Find all external variables of a function
 * <p>
 * Remain: not detected variable through setter and getter yet
 */
public class RelatedExternalVariableDetecter extends ASTVisitor implements IVariableDetecter {

    private final static AkaLogger logger = AkaLogger.get(RelatedExternalVariableDetecter.class);

    /**
     * Represent function
     */
    private IFunctionNode function;

    private final List<IASTName> variableNames = new ArrayList<>();

    private final List<IASTSimpleDeclaration> declarations = new ArrayList<>();

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
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "Tritype(int,int,int)").get(0);


        RelatedExternalVariableDetecter detecter = new RelatedExternalVariableDetecter(function);
        detecter.findVariables();
    }


    public RelatedExternalVariableDetecter(IFunctionNode function) {
        this.function = function;
        this.shouldVisitExpressions = true;
        this.shouldVisitDeclarations = true;
        function.getAST().accept(this);
    }

    @Override
    public List<IVariableNode> findVariables() {
        for (IASTSimpleDeclaration declaration : declarations) {
            for (IASTDeclarator declarator : declaration.getDeclarators()) {
                String name = declarator.getName().getRawSignature();
                variableNames
                        .removeIf(varName ->
                                varName.getRawSignature().equals(name));
            }
        }

        List<IVariableNode> globalVars = getAllGlobalVariables();
        if (globalVars.isEmpty()) {
            logger.debug("Found no global variable in unit");
        } else {
            logger.debug(String.format("Found %d global variables in unit", globalVars.size()));
        }

        List<IVariableNode> relatedVars = globalVars.stream()
                .filter(this::isUsedInFunction)
                .collect(Collectors.toList());

        if (relatedVars.isEmpty()) {
            logger.debug("Found no global variable used in subprogram");
        } else {
            logger.debug(String.format("Found %d global variables used in subprogram", relatedVars.size()));
        }

        return relatedVars;
    }

    private boolean isUsedInFunction(IVariableNode v) {
        final String varName = v.getName();
        return variableNames.stream()
                .anyMatch(name -> name.getRawSignature().equals(varName));
    }

    private List<IVariableNode> getAllGlobalVariables() {
        FunctionDetailTree functionDetailTree = new FunctionDetailTree(function);
        RootNode rootNode = functionDetailTree.getSubTreeRoot(NodeType.GLOBAL);
        return rootNode.getElements().stream()
                .filter(n -> n instanceof IVariableNode)
                .map(n -> (IVariableNode) n).collect(Collectors.toList());
    }

    @Override
    public int visit(IASTExpression expression) {
        if (expression instanceof IASTIdExpression) {
            variableNames.add(((IASTIdExpression) expression).getName());
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTDeclaration declaration) {
        if (declaration instanceof IASTSimpleDeclaration)
            declarations.add((IASTSimpleDeclaration) declaration);
        return PROCESS_CONTINUE;
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
