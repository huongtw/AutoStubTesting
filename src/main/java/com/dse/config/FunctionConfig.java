package com.dse.config;

import auto_testcase_generation.cfg.object.AbstractConditionLoopCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.object.SimpleCfgNode;
import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PointerOrArrayBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.testdata.gen.module.type.PointerTypeInitiation;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import com.dse.util.VariableTypeUtilsForStd;
import com.google.gson.annotations.Expose;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.util.*;

/**
 * Represent configuration of a function
 *
 * @author ducanh
 */
public class FunctionConfig implements IFunctionConfig {

    private final static AkaLogger logger = AkaLogger.get(FunctionConfig.class);

    private Set<String> loopArguments;

    private Set<ASTNode> functionsDeclarationStatements;

    private ICommonFunctionNode functionNode;

    private Map<String, IFunctionConfigBound> boundOfArguments = new HashMap<>();

    @Expose
    protected long theMaximumNumberOfIterations; // >=1, equal to the number of test data

    @Expose
    protected double floatAndDoubleDelta;

    @Expose
    private String solvingStrategy = SUPPORT_SOLVING_STRATEGIES.USER_BOUND_STRATEGY; // by default

    @Expose
    private PrimitiveBound boundOfPointer;

    @Expose
    private PrimitiveBound boundOfArray;

    @Expose
    private PrimitiveBound boundOfOtherCharacterVars;

    @Expose
    private PrimitiveBound boundOfOtherNumberVars;

    @Expose
    private String testdataGenStrategy;

    @Expose
    private String testdataExecStrategy;

    public FunctionConfig() {
        floatAndDoubleDelta = 0.1;
        theMaximumNumberOfIterations = 10;
        boundOfArray = new PrimitiveBound(1, 3);
        boundOfPointer = new PrimitiveBound(0, 3);
        boundOfOtherCharacterVars = new PrimitiveBound(IFunctionConfigBound.MIN_VARIABLE_TYPE, IFunctionConfigBound.MAX_VARIABLE_TYPE);
        boundOfOtherNumberVars = new PrimitiveBound(IFunctionConfigBound.MIN_VARIABLE_TYPE, IFunctionConfigBound.MAX_VARIABLE_TYPE);
        testdataGenStrategy = TEST_DATA_GENERATION_STRATEGIES.BEST_TIME; // by default
        testdataExecStrategy = TEST_DATA_EXECUTION_STRATEGIES.MULTIPLE_COMPILATION;
    }

    public static double getCommonFloatAndDoubleDelta() {
        FunctionConfig functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
        if (functionConfig != null) {
            return functionConfig.getFloatAndDoubleDelta();
        }

        logger.error("Failed to get the default function configuration.");
        logger.error("The common float and double delta is set with 0.1.");
        return 0.1;
    }

    @Override
    public String getSolvingStrategy() {
        return solvingStrategy;
    }

    @Override
    public void setSolvingStrategy(String solvingStrategy) {
        this.solvingStrategy = solvingStrategy;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        FunctionConfig clone = (FunctionConfig) super.clone();
//        clone.setCharacterBound((ParameterBound) characterBound.clone());
//        clone.setIntegerBound((ParameterBound) integerBound.clone());
        return clone();
    }

    public void setTestdataGenStrategy(String testdataGenStrategy) {
        this.testdataGenStrategy = testdataGenStrategy;
    }

    public String getTestdataGenStrategy() {
        return testdataGenStrategy;
    }

    public String getTestdataExecStrategy() {
        return testdataExecStrategy;
    }

    public void setTestdataExecStrategy(String testdataExecStrategy) {
        this.testdataExecStrategy = testdataExecStrategy;
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public long getTheMaximumNumberOfIterations() {
        return theMaximumNumberOfIterations;
    }

    public void setTheMaximumNumberOfIterations(long theMaximumNumberOfIterations) {
        this.theMaximumNumberOfIterations = theMaximumNumberOfIterations;
    }

    public PrimitiveBound getBoundOfOtherNumberVars() {
        return boundOfOtherNumberVars;
    }

    public void setBoundOfOtherNumberVars(PrimitiveBound boundOfOtherNumberVars) {
        this.boundOfOtherNumberVars = boundOfOtherNumberVars;
    }

    public void setBoundOfOtherCharacterVars(PrimitiveBound boundOfOtherCharacterVars) {
        this.boundOfOtherCharacterVars = boundOfOtherCharacterVars;
    }

    public PrimitiveBound getBoundOfOtherCharacterVars() {
        return boundOfOtherCharacterVars;
    }

    public Map<String, IFunctionConfigBound> getBoundOfArgumentsAndGlobalVariables() {
        return boundOfArguments;
    }

    public void setBoundOfArguments(Map<String, IFunctionConfigBound> boundOfArguments) {
        this.boundOfArguments = boundOfArguments;
    }

    private PointerOrArrayBound updateArrayElement(IFunctionConfig functionConfig, String type) {
        // primitive array
        List<String> indexes = Utils.getIndexOfArray(type);
        if (indexes.size() > 0) {
            List<String> normalizedIndexes = new ArrayList<>();
            for (String index : indexes)
                if (index.length() == 0)
                    normalizedIndexes.add(functionConfig.getBoundOfArray().getLower()
                            + IFunctionConfigBound.RANGE_DELIMITER + functionConfig.getBoundOfArray().getUpper());
                else normalizedIndexes.add(index);
            return new PointerOrArrayBound(normalizedIndexes, type);
        }
        return null;
    }

    private PointerOrArrayBound updatePointerElement(IFunctionConfig functionConfig, String type) {
        // primitive pointer
        int level = PointerTypeInitiation.getLevel(type);
        if (level > 0) {
            List<String> normalizedIndexes = new ArrayList<>();
            for (int i = 0; i < level; i++)
                normalizedIndexes.add(functionConfig.getBoundOfPointer().getLower() + IFunctionConfigBound.RANGE_DELIMITER
                        + functionConfig.getBoundOfPointer().getUpper());
            return new PointerOrArrayBound(normalizedIndexes, type);
        }
        return null;
    }

    @Override
    public void createDefaultConfig(ICommonFunctionNode functionNode) {
        FunctionConfig functionConfig = this;

        functionConfig.setTheMaximumNumberOfIterations(1);
        functionConfig.setBoundOfArray(new PrimitiveBound(1, 3));
        functionConfig.setBoundOfPointer(new PrimitiveBound(0, 3));
        functionConfig.setBoundOfOtherNumberVars(new PrimitiveBound(IFunctionConfigBound.MIN_VARIABLE_TYPE, IFunctionConfigBound.MAX_VARIABLE_TYPE));
        functionConfig.setBoundOfOtherCharacterVars(new PrimitiveBound(IFunctionConfigBound.MIN_VARIABLE_TYPE, IFunctionConfigBound.MAX_VARIABLE_TYPE));
        functionConfig.setTestdataGenStrategy(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.RANDOM);
        functionConfig.setTestdataExecStrategy(IFunctionConfig.TEST_DATA_EXECUTION_STRATEGIES.MULTIPLE_COMPILATION);

        Map<String, IFunctionConfigBound> bounds = createBoundOfArgument(functionConfig, functionNode);
        functionConfig.setBoundOfArguments(bounds);
        functionConfig.setFunctionNode(functionNode);
    }



    public Map<String, IFunctionConfigBound> createBoundOfArgument(IFunctionConfig functionConfig, ICommonFunctionNode functionNode) {
        initializeSet();
        getLoopVariableAndStatementInCFG(functionNode);

        // create bound
        Map<String, IFunctionConfigBound> bounds = new HashMap<>();
        findArgInDeclarationStatement(functionNode.getName());
        boolean detectedBound = false;
        for (IVariableNode variableNode : functionNode.getArgumentsAndGlobalVariables()) {

            String realRawType = variableNode.getRealType();

            detectedBound = false;
            String type = VariableTypeUtils.removeRedundantKeyword(realRawType);
            type = VariableTypeUtils.deleteReferenceOperator(type);
            type = VariableTypeUtils.deleteSizeFromArray(type);
            type = type.trim();

            Set<String> keys = Environment.getBoundOfDataTypes().getBounds().keySet();
            for (String definedBound : keys) {
                PrimitiveBound b = Environment.getBoundOfDataTypes().getBounds().get(definedBound);

                if (definedBound.equals(type)) {
                    // just a primitive variable
                    MultiplePrimitiveBound multiplePrimitiveBound = new MultiplePrimitiveBound();
                    PrimitiveBound primitiveBound;
                    if (loopArguments.contains(variableNode.getName())) {
                        primitiveBound = new PrimitiveBound(0, 10);
                    } else {
                        primitiveBound = new PrimitiveBound(b.getLower(), b.getUpper());
                    }
                    multiplePrimitiveBound.add(primitiveBound);

                    bounds.put(variableNode.getName(), multiplePrimitiveBound);
                    detectedBound = true;
                    break;
                }
            }

            if (detectedBound)
                continue;

            // bound of pointer
            if (VariableTypeUtils.isChMultiLevel(realRawType)
                    || VariableTypeUtils.isNumMultiLevel(realRawType)
                    || VariableTypeUtils.isStrMultiLevel(realRawType)
                    || VariableTypeUtils.isBoolMultiLevel(realRawType)
                    || VariableTypeUtils.isStructureMultiLevel(realRawType)
                    // std
                    || VariableTypeUtilsForStd.isStdMapMultiLevel(realRawType)
                    || VariableTypeUtilsForStd.isStdPairMultiLevel(realRawType)
                    || VariableTypeUtilsForStd.isStdListMultiLevel(realRawType)
                    || VariableTypeUtilsForStd.isStdQueueMultiLevel(realRawType)
                    || VariableTypeUtilsForStd.isStdStackMultiLevel(realRawType)
                    || VariableTypeUtilsForStd.isStdVectorMultiLevel(realRawType)
                    || VariableTypeUtilsForStd.isStdSetMultiLevel(realRawType)) {
                PointerOrArrayBound pointerBound = updatePointerElement(functionConfig, realRawType);
                if (pointerBound != null) {
                    bounds.put(variableNode.getName(), pointerBound);
                    detectedBound = true;
                }
            }
            if (detectedBound)
                continue;
            // bound of array
            if (VariableTypeUtils.isBoolMultiDimension(realRawType) ||
                    VariableTypeUtils.isChMultiDimension(realRawType) ||
                    VariableTypeUtils.isNumMultiDimension(realRawType) ||
                    VariableTypeUtils.isStrMultiDimension(realRawType) ||
                    VariableTypeUtils.isStructureMultiDimension(realRawType)
                    // std
                    || VariableTypeUtilsForStd.isStdMapMultiDimension(realRawType)
                    || VariableTypeUtilsForStd.isStdPairMultiDimension(realRawType)
                    || VariableTypeUtilsForStd.isStdListMultiDimension(realRawType)
                    || VariableTypeUtilsForStd.isStdQueueMultiDimension(realRawType)
                    || VariableTypeUtilsForStd.isStdStackMultiDimension(realRawType)
                    || VariableTypeUtilsForStd.isStdVectorMultiDimension(realRawType)
                    || VariableTypeUtilsForStd.isStdSetMultiDimension(realRawType)) {
                PointerOrArrayBound arrayBound = updateArrayElement(functionConfig, realRawType);
                if (arrayBound != null) {
                    bounds.put(variableNode.getName(), arrayBound);
                    detectedBound = true;
                }
            }

            if (detectedBound)
                continue;
            if (VariableTypeUtils.isVoidPointer(realRawType)) {
//                VoidPointerBound voidPointerBound = new VoidPointerBound();
//                bounds.put(variableNode.getName(), new UndefinedBound());
//                //
//                int a = 0;
            }

            if (!detectedBound) {
                bounds.put(variableNode.getName(), new UndefinedBound());
            }
        }

        functionConfig.setBoundOfArguments(bounds);
        clearSet();
        return bounds;
    }

    public void initializeSet() {
        loopArguments = new HashSet<>();
        functionsDeclarationStatements = new HashSet<>();
    }

    public void clearSet() {
        loopArguments.clear();
        functionsDeclarationStatements.clear();
    }

    private void getLoopVariableAndStatementInCFG(ICommonFunctionNode functionNode){
        IFunctionNode fn = (IFunctionNode) functionNode;
        List<ICfgNode> cfgNodes = fn.generateCFG().getAllNodes();
        for (ICfgNode cfgNode : cfgNodes){
            if (cfgNode instanceof AbstractConditionLoopCfgNode){
                IASTNode node = ((AbstractConditionLoopCfgNode) cfgNode).getAst();
                if (node instanceof CPPASTBinaryExpression){
                    setConditionLoopVariable((CPPASTBinaryExpression) node);
                }
            }
        }
        for (ICfgNode cfgNode : cfgNodes){
            if (cfgNode instanceof SimpleCfgNode){
                IASTNode node = ((SimpleCfgNode) cfgNode).getAst();
                if (node instanceof ASTNode){
                    addASTNode((ASTNode) node);
                }
            }
        }
    }

    public Set<String> getConditionLoopArguments() {
        return loopArguments;
    }

    private boolean addArgsInIASTInitializerClause(IASTInitializerClause clause) {
        if (clause instanceof CPPASTIdExpression) {
            return loopArguments.add(clause.getRawSignature());
        } else if (clause instanceof CPPASTBinaryExpression) {
            boolean found = foundConditionLoopVariable(((CPPASTBinaryExpression) clause).getOperand1());
            if (found){
                foundConditionLoopVariable(((CPPASTBinaryExpression) clause).getOperand2());
            } else {
                found = foundConditionLoopVariable(((CPPASTBinaryExpression) clause).getOperand2());
            }
            return found;
        } else if (clause instanceof CPPASTUnaryExpression) {
            return foundConditionLoopVariable(((CPPASTUnaryExpression) clause).getOperand());
        }
        return false;
    }

    private void findArgInDeclarationStatement(String functionNode) {
        Set<ASTNode> astNodes = functionsDeclarationStatements;
        boolean foundArg = true;
        while (foundArg){
            foundArg = false;
            for (ASTNode stm : astNodes) {
                if (stm instanceof CPPASTDeclarationStatement) {
                    if (((CPPASTDeclarationStatement) stm).getDeclaration() instanceof CPPASTSimpleDeclaration) {
                        CPPASTSimpleDeclaration simpleDeclaration
                                = (CPPASTSimpleDeclaration) ((CPPASTDeclarationStatement) stm).getDeclaration();
                        IASTDeclarator[] cppastDeclarators = simpleDeclaration.getDeclarators();
                        for (IASTDeclarator declarator : cppastDeclarators) {
                            if (declarator instanceof CPPASTDeclarator) {
                                String varName = declarator.getName().getRawSignature();
                                if (loopArguments.contains(varName)) {
                                    if (declarator.getInitializer() instanceof CPPASTEqualsInitializer) {
                                        CPPASTEqualsInitializer equalsInitializer = (CPPASTEqualsInitializer) declarator.getInitializer();
                                        IASTInitializerClause clause = equalsInitializer.getInitializerClause();
                                        foundArg = addArgsInIASTInitializerClause(clause);
                                    }
                                }
                            }
                        }
                    }
                } else if (stm instanceof CPPASTExpressionStatement) {
                    IASTExpression expression = ((CPPASTExpressionStatement) stm).getExpression();
                    if (expression instanceof CPPASTBinaryExpression) {
                        if (((CPPASTBinaryExpression) expression).getOperand1() instanceof CPPASTIdExpression) {
                            String varName = ((CPPASTIdExpression) ((CPPASTBinaryExpression) expression).getOperand1())
                                    .getName().getRawSignature();
                            if (loopArguments.contains(varName)) {
                                IASTInitializerClause clause = ((CPPASTBinaryExpression) expression).getOperand2();
                                foundArg = addArgsInIASTInitializerClause(clause);
                            }
                        }
                    }
                }
            }
        }
    }

    public void addASTNode(ASTNode stm) {
        functionsDeclarationStatements.add(stm);
    }

    public void setConditionLoopVariable(CPPASTBinaryExpression conditionLoopCfgNode) {
        IASTExpression operand1 = conditionLoopCfgNode.getOperand1();
        foundConditionLoopVariable(operand1);
        IASTExpression operand2 = conditionLoopCfgNode.getOperand2();
        foundConditionLoopVariable(operand2);
    }

    private boolean foundConditionLoopVariable(IASTExpression operand) {
        if (operand instanceof CPPASTBinaryExpression) {
            boolean found = foundConditionLoopVariable(((CPPASTBinaryExpression) operand).getOperand1());
            if (found){
                foundConditionLoopVariable(((CPPASTBinaryExpression) operand).getOperand2());
            } else {
                found = foundConditionLoopVariable(((CPPASTBinaryExpression) operand).getOperand2());
            }
            return found;
        } else if (operand instanceof CPPASTIdExpression) {
            return loopArguments.add(operand.getRawSignature());
        } else if (operand instanceof CPPASTUnaryExpression) {
            return foundConditionLoopVariable(((CPPASTUnaryExpression) operand).getOperand());
        }
        return false;
    }

    public PrimitiveBound getBoundOfArray() {
        return boundOfArray;
    }

    public void setBoundOfArray(PrimitiveBound boundOfArray) {
        this.boundOfArray = boundOfArray;
    }

    public PrimitiveBound getBoundOfPointer() {
        return boundOfPointer;
    }

    public void setBoundOfPointer(PrimitiveBound boundOfPointer) {
        this.boundOfPointer = boundOfPointer;
    }

    public double getFloatAndDoubleDelta() {
        return floatAndDoubleDelta;
    }

    public void setFloatAndDoubleDelta(double doubleAndFloatDelta) {
        this.floatAndDoubleDelta = doubleAndFloatDelta;
    }
}