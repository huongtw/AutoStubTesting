package com.dse.guifx_v3.objects;

import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PointerOrArrayBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfig;
import com.dse.config.IFunctionConfigBound;
import com.dse.config.UndefinedBound;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.IVariableNode;
import com.dse.logger.AkaLogger;
import com.dse.boundary.DataSizeModel;
import com.dse.thread.task.GenerateTestdataTask;

import java.util.ArrayList;
import java.util.List;

public class FunctionConfigParameter {
    private final static AkaLogger logger = AkaLogger.get(FunctionConfigParameter.class);
    private FunctionConfig functionConfig;
    private String param;
    private String validTypeRange;
    private String value;
    private boolean editable;

    public FunctionConfigParameter(FunctionConfig functionConfig, String param, String value) {
        if (functionConfig != null) {
            this.functionConfig = functionConfig;
            if (validateParam(param)) {
                this.param = param;
                setValue(value);
            }
        }
        this.editable = true;
    }

    public FunctionConfigParameter(FunctionConfig functionConfig, String param, String value, boolean editable) {
        if (functionConfig != null) {
            this.functionConfig = functionConfig;
            if (validateParam(param)) {
                this.param = param;
                setValue(value);
            }
        }
        this.editable = editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public String getParam() {
        return param;
    }

    public String getValue() {
        return value;
    }


    public boolean setValue(String value) {
        switch (param) {
            case TEST_DATA_GEN_STRATEGY: {
//                if (value.equals(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.DART)
//                        || value.equals(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.DIRECTED_DIJKSTRA)) {
//                    UIController.showErrorDialog("Does not support this option in this version", "Function configuration", "Do not support");
//                    return false;
//                } else {
                functionConfig.setTestdataGenStrategy(value);
                this.value = value;
                return true;
//                }
            }

            case FLOAT_AND_DOUBLE_DELTA: {
                boolean isOK = checkFloatAndDoubleDelta(value);
                if (isOK) {
                    functionConfig.setFloatAndDoubleDelta(Double.parseDouble(value));
                    this.value = String.valueOf(Double.parseDouble(value));
                    return true;
                } else {
                    return false;
                }
            }

            case THE_MAXIMUM_NUMBER_OF_ITERATIONS: {
                boolean isOK = checkNumberOfIteration(param, value, functionConfig);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case CHARACTER_BOUND_LOWER: {
                String upper = functionConfig.getBoundOfOtherCharacterVars().getUpper();
                boolean isOK = checkBoundOfCharacterAndNumber(param, value, upper, functionConfig.getBoundOfOtherCharacterVars(), null);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case CHARACTER_BOUND_UPPER: {
                String lower = functionConfig.getBoundOfOtherCharacterVars().getLower();
                boolean isOK = checkBoundOfCharacterAndNumber(param, lower, value, functionConfig.getBoundOfOtherCharacterVars(), null);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case NUMBER_BOUND_LOWER: {
                String upper = functionConfig.getBoundOfOtherNumberVars().getUpper();
                boolean isOK = checkBoundOfCharacterAndNumber(param, value, upper, functionConfig.getBoundOfOtherNumberVars(), null);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case NUMBER_BOUND_UPPER: {
                String lower = functionConfig.getBoundOfOtherNumberVars().getLower();
                boolean isOK = checkBoundOfCharacterAndNumber(param, lower, value, functionConfig.getBoundOfOtherNumberVars(), null);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case LOWER_BOUND_OF_OTHER_ARRAYS: {
                String upper = functionConfig.getBoundOfArray().getUpper();
                boolean isOK = checkBoundOfArrayorPointer(param, value, upper, functionConfig.getBoundOfArray(), 1);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }
            case UPPER_BOUND_OF_OTHER_ARRAYS: {
                String lower = functionConfig.getBoundOfArray().getLower();
                boolean isOK = checkBoundOfArrayorPointer(param, lower, value, functionConfig.getBoundOfArray(), 1);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case LOWER_BOUND_OF_OTHER_POINTERS: {
                String upper = functionConfig.getBoundOfPointer().getUpper();
                boolean isOK = checkBoundOfArrayorPointer(param, value, upper, functionConfig.getBoundOfPointer(), 0);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            case UPPER_BOUND_OF_OTHER_POINTERS: {
                String lower = functionConfig.getBoundOfPointer().getLower();
                boolean isOK = checkBoundOfArrayorPointer(param, lower, value, functionConfig.getBoundOfPointer(), 0);
                if (isOK) {
                    this.value = value;
                    return true;
                } else
                    return false;
            }

            default: {
                if (param != null && param.length() > 0) {
                    boolean isOK = setValueOfArguments(functionConfig, param, value);
                    if (isOK) {
                        this.value = value;
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private boolean setValueOfArguments(FunctionConfig functionConfig, String param, String value) {
        value = value.trim();
        String nameArg = param.replace(IFunctionConfigBound.ARGUMENT_SIZE, "");

        // Find the corresponding argument
        IVariableNode correspondingVar = null;
        List<IVariableNode> arguments = functionConfig.getFunctionNode().getArgumentsAndGlobalVariables();
        for (IVariableNode argument : arguments)
            if (argument.getName().equals(nameArg)) {
                correspondingVar = argument;
                break;
            }

        //
        IFunctionConfigBound bound = functionConfig.getBoundOfArgumentsAndGlobalVariables().get(nameArg);
        if (bound != null && correspondingVar != null) {
            // CASE 1.
            if (bound instanceof PrimitiveBound) {
                String lower = null, upper = null;
                DataSizeModel dataSizeModel = Environment.getBoundOfDataTypes().getBounds();
                PrimitiveBound dataSize = dataSizeModel.get(correspondingVar.getReducedRawType());

                if (value.contains(IFunctionConfigBound.RANGE_DELIMITER)) {
                    lower = value.split(IFunctionConfigBound.RANGE_DELIMITER)[0].trim();
                    upper = value.split(IFunctionConfigBound.RANGE_DELIMITER)[1].trim();
                    boolean isOK = checkBoundOfCharacterAndNumber(param, lower, upper, (PrimitiveBound) bound, dataSize);
                    return isOK;
                } else {
                    try {
                        Double tmp = Double.parseDouble(value);
                        lower = value;
                        upper = value;

                        boolean isOK = checkBoundOfCharacterAndNumber(param, lower, upper, (PrimitiveBound) bound, dataSize);
                        return isOK;
                    } catch (NumberFormatException e) {
                        UIController.showErrorDialog("The bound of variable " + nameArg + " is not valid"
                                , "Invalid bound", "Wrong bound");
                        return false;
                    }
                }


            } else if (bound instanceof MultiplePrimitiveBound) {
                // get data size model
                // split value to an array of primitive bound
                // todo: check 2 vung trung lap
                DataSizeModel dataSizeModel = Environment.getBoundOfDataTypes().getBounds();
                PrimitiveBound dataSize = dataSizeModel.get(correspondingVar.getReducedRawType());
                List<PrimitiveBound> primitiveBounds = new ArrayList<>();

                String[] boundStrings = value.split(IFunctionConfigBound.INDEX_DELIMITER); // split
                for (int i = 0; i < boundStrings.length; i++) {
                    String boundString = boundStrings[i].trim();
                    String lower = null, upper = null;
                    PrimitiveBound primitiveBound = new PrimitiveBound();

                    if (boundString.contains(IFunctionConfigBound.RANGE_DELIMITER)) {
                        lower = boundString.split(IFunctionConfigBound.RANGE_DELIMITER)[0].trim();
                        upper = boundString.split(IFunctionConfigBound.RANGE_DELIMITER)[1].trim();
                        boolean isOK = checkBoundOfCharacterAndNumber(param, lower, upper, primitiveBound, dataSize);
                        if (!isOK) {
                            return false;
                        }
                    } else {
                        try {
                            Double tmp = Double.parseDouble(boundString);
                            lower = boundString;
                            upper = boundString;

                            boolean isOK = checkBoundOfCharacterAndNumber(param, lower, upper, primitiveBound, dataSize);
                            if (!isOK) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            UIController.showErrorDialog("The bound of variable " + nameArg + " is not valid"
                                    , "Invalid bound", "Wrong bound");
                            return false;
                        }
                    }
                    primitiveBounds.add(primitiveBound);
                }

                // if all bound is valid
                ((MultiplePrimitiveBound) bound).clear();
                ((MultiplePrimitiveBound) bound).addAll(primitiveBounds);
                return true;

            } else if (bound instanceof PointerOrArrayBound) {
                List<String> indexes = new ArrayList<>();
                if (value.contains(IFunctionConfigBound.INDEX_DELIMITER))
                    for (String token : value.split(IFunctionConfigBound.INDEX_DELIMITER)) {
                        indexes.add(token);
                    }
                else indexes.add(value);
                ((PointerOrArrayBound) bound).setIndexes(indexes);
                return true;

            } else if (bound instanceof UndefinedBound) {
                return true;
            }

        }
        return false;
    }

    private boolean checkFloatAndDoubleDelta(String value) {
        try {
            double delta = Double.parseDouble(value);
            if (delta > 1 || delta < 0) { // the delta must lest than 1
                return false;
            }

            String tmp = String.valueOf(delta);
            // the delta must has only one character '1' and other is '0' characters
            if (tmp.indexOf("1") != tmp.lastIndexOf("1")) {
                return false;
            }
            if (Double.parseDouble(tmp.replace('1', '0')) != 0) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkNumberOfIteration(String param, String iteration, FunctionConfig functionConfig) {
        try {
            long iterationNum = Long.parseLong(iteration);
            if (iterationNum <= 0) {
                UIController.showErrorDialog("The number of iterations " + param + " must be a positive integer"
                        , "Invalid value", "Wrong number of iteration");
                return false;
            } else {
                functionConfig.setTheMaximumNumberOfIterations(iterationNum);
                return true;
            }
        } catch (NumberFormatException e) {
            UIController.showErrorDialog("The number of iterations " + param + " must be a positive integer"
                    , "Invalid value", "Wrong number of iteration");
            return false;
        }
    }

    private boolean checkBoundOfArrayorPointer(String param, String lower, String upper, PrimitiveBound bound, int validMin) {
        try {
            long lowerTmp = Long.parseLong(lower);
            long upperTmp = Long.parseLong(upper);

            if (lowerTmp < validMin || upperTmp < validMin) {
                UIController.showErrorDialog(
                        "Reason: The lower value and the upper value must be greater or equal to " + validMin +
                                " (lower value = " + lower + ", upper value = " + upper + ")"
                        , "Invalid value of " + param, "Wrong value of " + param);
                return false;
            } else if (lowerTmp <= upperTmp) {
                bound.setUpper(upper);
                bound.setLower(lower);
                return true;
            } else {
                UIController.showErrorDialog(
                        "Reason: The lower value and the upper value are not matched! " +
                                "(lower value = " + lower + ", upper value = " + upper + ")"
                        , "Invalid value of " + param, "Wrong value of " + param);
                return false;
            }

        } catch (NumberFormatException e) {
            UIController.showErrorDialog("The upper value (" + upper + ") and the lower value (" + lower + ") of array bound must be integer"
                    , "Invalid value of " + param, "Wrong value of " + param);
            return false;
        }
    }

    private boolean checkBoundOfCharacterAndNumber(String param, String lower, String upper, PrimitiveBound bound, PrimitiveBound validRange) {
        if (upper == null || lower == null) {
            UIController.showErrorDialog("Error"
                    , "Invalid value of " + param, "Wrong value of " + param);
            return false;

        }
        // CASE 1
        else if (lower.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
            if (upper.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) || upper.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                bound.setLower(lower);
                bound.setUpper(upper);
                return true;
            } else {
                try {
                    Double upperDouble = Double.parseDouble(upper);
                    // todo: ask Duc Anh to check if upper double is bigger than max value of the invalid range
                    bound.setLower(lower);
                    bound.setUpper(upper);
                    return true;
                } catch (Exception e) {
                    UIController.showErrorDialog(
                            "Reason: The lower value and the upper value are not matched! " +
                                    "(lower value = " + lower + ", upper value = " + upper + ")"
                            , "Invalid value of " + param, "Wrong value of " + param);
                    return false;
                }
            }
        }
        // CASE 2
        else if (lower.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
            if (upper.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                bound.setLower(lower);
                bound.setUpper(upper);
                return true;
            } else {
                UIController.showErrorDialog(
                        "Reason: The lower value and the upper value are not matched! " +
                                "(lower value = " + lower + ", upper value = " + upper + ")"
                        , "Invalid value of " + param, "Wrong value of " + param);
                return false;
            }
        }
        // CASE 3. Lower value is a number or a string
        else {
            try {
                double lowerDouble = Double.parseDouble(lower);
                if (validRange != null && lowerDouble < validRange.getLowerAsDouble()) {
                    UIController.showErrorDialog(
                            "Lower " + lower + " must be >= " + validRange.getLowerAsDouble()
                            , "Invalid value of " + param, "Wrong value of " + param);
                    return false;
                } else
                    try {
                        if (upper.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
                            UIController.showErrorDialog(
                                    "Reason: The lower value and the upper value are not matched! "
                                            + "(lower value = " + lower + ", upper value = " + upper + ")"
                                    , "Invalid value of " + param, "Wrong value of " + param);
                            return false;

                        } else if (upper.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                            bound.setLower(lower);
                            bound.setUpper(upper);
                            return true;

                        } else {
                            double upperDouble = Double.parseDouble(upper);

                            if (validRange != null && upperDouble > validRange.getUpperAsDouble()) {
                                UIController.showErrorDialog(
                                        "Upper " + upper + " must be <=" + validRange.getUpperAsDouble()
                                        , "Invalid value of " + param, "Wrong value of " + param);
                                return false;
                            } else if (lowerDouble <= upperDouble) {
                                bound.setLower(lower);
                                bound.setUpper(upper);
                                return true;
                            } else {
                                UIController.showErrorDialog(
                                        "Reason: The lower value and the upper value are not matched! "
                                                + "(lower value = " + lower + ", upper value = " + upper + ")"
                                        , "Invalid value of " + param, "Wrong value of " + param);
                                return false;
                            }
                        }
                    } catch (Exception e1) {
                        UIController.showErrorDialog("Reason: The value of upper bound " + upper + " is a string"
                                , "Invalid value of " + param, "Wrong value of " + param);
                        return false;
                    }
            } catch (Exception e2) {
                UIController.showErrorDialog("Reason: The value of lower bound " + lower + " is a string"
                        , "Invalid value of " + param, "Wrong value of " + param);
                return false;
            }
        }
    }

    private boolean validateParam(String param) {
        return true;
    }

    static String[] getTestDataExecStrategies() {
        return new String[]{IFunctionConfig.TEST_DATA_EXECUTION_STRATEGIES.MULTIPLE_COMPILATION, IFunctionConfig.TEST_DATA_EXECUTION_STRATEGIES.SINGlE_COMPILATION};
    }

    static String[] getSolvingStrategies() {
        return new String[]{IFunctionConfig.SUPPORT_SOLVING_STRATEGIES.USER_BOUND_STRATEGY, IFunctionConfig.SUPPORT_SOLVING_STRATEGIES.Z3_STRATEGY};
    }

    static String[] getTestDataGenStrategies() {
        return new String[]{
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BEST_COVERAGE,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BEST_TIME,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.RANDOM,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_DIJKSTRA,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BASIS_PATH_TESTING,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.WHITEBOX_BOUNDARY,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.MID_MIN_MAX,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_CFDS,
                IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_DFS,
        };
    }

    public void setValidTypeRange(String validTypeRange) {
        this.validTypeRange = validTypeRange;
    }

    public String getValidTypeRange() {
        return validTypeRange;
    }

    // parameters'name
    public static final String CHARACTER_BOUND_LOWER = "CHARACTER_BOUND_LOWER";
    public static final String CHARACTER_BOUND_UPPER = "CHARACTER_BOUND_UPPER";
    public static final String NUMBER_BOUND_LOWER = "INTEGER_BOUND_LOWER";
    public static final String NUMBER_BOUND_UPPER = "INTEGER_BOUND_UPPER";
    public static final String SOLVING_STRATEGY = "SOLVING_STRATEGY";
    public static final String TEST_DATA_GEN_STRATEGY = "STRATEGY";
    public static final String TEST_DATA_EXEC_STRATEGY = "TEST_DATA_EXEC_STRATEGY";
    public static final String THE_MAXIMUM_NUMBER_OF_ITERATIONS = "THE_MAXIMUM_NUMBER_OF_ITERATIONS";
    public static final String FLOAT_AND_DOUBLE_DELTA = "FLOAT_AND_DOUBLE_DELTA";

    public static final String LOWER_BOUND_OF_OTHER_ARRAYS = "ARRAY_SIZE_LOWER";
    public static final String UPPER_BOUND_OF_OTHER_ARRAYS = "ARRAY_SIZE_UPPER";

    public static final String LOWER_BOUND_OF_OTHER_POINTERS = "POINTER_SIZE_LOWER";
    public static final String UPPER_BOUND_OF_OTHER_POINTERS = "POINTER_SIZE_UPPER";

}
