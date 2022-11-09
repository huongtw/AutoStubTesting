package auto_testcase_generation.testdatagen.se.solver.solutionparser;

import auto_testcase_generation.cfg.testpath.IStaticSolutionGeneration;

public interface IZ3SolutionParser {

    String NO_SOLUTION = IStaticSolutionGeneration.NO_SOLUTION;
    String KHAI_BAO = "(define-fun";
    String LET = "(let ";
    String IF_THEN_ELSE = "(ite ";
    String END = ")";
    String MODEL = "model";
    String SAT = "sat";
    String UNSAT = "unsat";
    String UNKNOWN = "unknown";
    String UNSUPPORTED = "unsupported";
    int KHAI_BAO_ID = -4;
    int KHAI_BAO_ID_PRIMITIVE_VAR = -2;
    int KHAI_BAO_ID_ARRAY_VAR = 0;
    int MODEL_ID = -1;
    int IF_THEN_ELSE_ID = 1;
    int VALUE_ID = 2;
    int END_ID = 3;
    int SAT_ID = 4;
    int UNSAT_ID = 5;
    int UNKNOWN_ID = 6;
    int ERROR = 7;
    int LET_ID = 8;
    int UNSUPPORTED_ID = 9;

    String getSolution(String Z3Solution);

}