package com.dse.config;

import com.dse.boundary.PrimitiveBound;
import com.dse.parser.object.ICommonFunctionNode;

import java.util.Map;

/**
 * Represent configuration of a function
 *
 * @author DucAnh
 */
public interface IFunctionConfig {

    interface SUPPORT_SOLVING_STRATEGIES {
        String Z3_STRATEGY = "Z3_STRATEGY";
        String USER_BOUND_STRATEGY = "USER_BOUND_STRATEGY";
    }

    interface TEST_DATA_GENERATION_STRATEGIES {
        String BEST_COVERAGE = "BEST COVERAGE";
        String BEST_TIME = "BEST TIME";
        String CONCOLIC_TESTING_DIJKSTRA = "DIJKSTRA";
        String RANDOM = "RANDOM";
        String NORMAL_BOUND = "NORMAL BOUND";
        String BVA = "BVA";
        String BVA_BOUNDARYCONDITION = "BVA/BOUNDARY CONDITION";
        String ROBUSTNESS = "ROBUSTNESS";
        String WHITEBOX_BOUNDARY = "WHITEBOX BOUNDARY";
//        String DEEP_BOUNDARY = "DEEP BOUNDARY";
        String MID_MIN_MAX = "MID/MIN/MAX";
        String CONCOLIC_TESTING_CFDS = "CONCOLIC (CFDS)";
        String CONCOLIC_TESTING_DFS = "CONCOLIC (DFS)";
        //        String CLASSIC_WHITEBOX_BOUNDARY = "CLASSIC WHITEBOX BOUNDARY";
        String BASIS_PATH_TESTING = "BASIS PATH";
    }

    interface TEST_DATA_EXECUTION_STRATEGIES {
        String SINGlE_COMPILATION = "SINGLE_COMPILATION";
        String MULTIPLE_COMPILATION = "MULTIPLE_COMPILATION";
    }

    String getSolvingStrategy();

    void setSolvingStrategy(String solvingStrategy);

    void setTestdataGenStrategy(String testdataGenStrategy);

    String getTestdataGenStrategy();

    void setTestdataExecStrategy(String testdataExecStrategy);

    ICommonFunctionNode getFunctionNode();

    void setFunctionNode(ICommonFunctionNode functionNode);

    long getTheMaximumNumberOfIterations();

    void setTheMaximumNumberOfIterations(long theMaximumNumberOfIterations);

    Map<String, IFunctionConfigBound> getBoundOfArgumentsAndGlobalVariables();

    void setBoundOfArguments(Map<String, IFunctionConfigBound> boundOfArguments);

    PrimitiveBound getBoundOfOtherNumberVars();

    void setBoundOfOtherNumberVars(PrimitiveBound boundOfOtherNumberVars);

    void setBoundOfOtherCharacterVars(PrimitiveBound boundOfOtherCharacterVars);

    PrimitiveBound getBoundOfOtherCharacterVars();

    PrimitiveBound getBoundOfArray();

    void setBoundOfArray(PrimitiveBound boundOfArray);

    PrimitiveBound getBoundOfPointer();

    void setBoundOfPointer(PrimitiveBound boundOfPointer);

    void createDefaultConfig(ICommonFunctionNode functionNode);
}