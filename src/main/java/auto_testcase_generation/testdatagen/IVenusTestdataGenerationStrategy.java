package auto_testcase_generation.testdatagen;

public interface IVenusTestdataGenerationStrategy {
    int SELECTED_PRIOTITIES_TESTPATH = PRIOTITIES_TESTPATHS.IN_DECREASING_ORDER;

    /**
     * [0..number of conditions)
     */
    int CONDITION_ORDER_CHECKER_ID = CONDITION_ORDER.THIRD;

    interface PRIOTITIES_TESTPATHS {
        int IN_DECREASING_ORDER = 0;
        int IN_INCREASING_ORDER = 1;
        int CURRENT_ORDER = 2;
    }

    interface CONDITION_ORDER {
        int NO_CHECK = -1;
        int FIRST = 0;
        int SECOND = 1;
        int THIRD = 2;
        int FOURTH = 3;
    }
}
