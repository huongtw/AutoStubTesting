package auto_testcase_generation.testdatagen;

import com.dse.boundary.PointerOrArrayBound;
import auto_testcase_generation.testdatagen.se.CustomJeval;
import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfig;
import com.dse.config.IFunctionConfigBound;
import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.VariableTypeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use when we know the size of array/pointer
 */
public class ValueToTestcaseConverter_DefinedSize {
    final static AkaLogger logger = AkaLogger.get(RandomValue.class);

    private Map<String, String> testcases = new HashMap<>();
    private ICommonFunctionNode fn;

    public static void main(String[] args) {
        ProjectParser projectParser = new ProjectParser(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm"));
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        ProjectNode projectRoot = projectParser.getRootTree();
        ICommonFunctionNode fn = (ICommonFunctionNode) Search.searchNodes(projectRoot,
                new FunctionNodeCondition(), "uninit_var(int[3],int[3])").get(0);

        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.getBoundOfArgumentsAndGlobalVariables().put("a", new PointerOrArrayBound(new String[]{"3:3"}, "int[3]"));
        functionConfig.getBoundOfArgumentsAndGlobalVariables().put("b", new PointerOrArrayBound(new String[]{"3:3"}, "int[3]"));
        functionConfig.setFunctionNode(fn);
        fn.setFunctionConfig(functionConfig);

        String testcases = "a[<other indexes>]=-1;dp[<other indexes>]=-1;b[<other indexes>]=1;";
        ValueToTestcaseConverter_DefinedSize converter = new ValueToTestcaseConverter_DefinedSize(testcases, fn);
        List<RandomValue> randomValues = converter.convert();
        System.out.println(randomValues);
    }

    public List<RandomValue> convert() {
        List<RandomValue> output = new ArrayList<>();
        findSizeOfPointerandArray(fn.getFunctionConfig(), output);
        updateDefaultValue(getTestcases(), output);
        findValueOfElement(getTestcases(), output);
        addPrimitiveValue(getTestcases(), output);
        return output;
    }

    public ValueToTestcaseConverter_DefinedSize(String testcases, ICommonFunctionNode fn) {
        if (testcases != null && testcases.length() > 0) {
            String[] tc = testcases.split(DELIMITER_BETWEEN_TESTCASES);
            for (String item : tc)
                if (item.contains(DELIMITER_BETWEEN_KEY_AND_VALUE)) {
                    String key = item.split(DELIMITER_BETWEEN_KEY_AND_VALUE)[0];
                    String value = item.split(DELIMITER_BETWEEN_KEY_AND_VALUE)[1];

                    value = new CustomJeval().evaluate(value);
                    this.testcases.put(key, value);
                }
        }
        this.fn = fn;
    }

    public void addPrimitiveValue(Map<String, String> testcases, List<RandomValue> output) {
        for (String key : testcases.keySet()) {
            if (!isArray(key) && !key.contains("<other indexes>")) {
                String value = testcases.get(key);
                RandomValue rv = new RandomValue(key, value);
                if (!output.contains(rv))
                    output.add(rv);
            }
        }

    }

    /**
     * Based on function config, create expressions to: (1) save the size of pointer/array, and (2) save the value of elements
     * @param functionConfig
     * @param output
     */
    public void findSizeOfPointerandArray(IFunctionConfig functionConfig, List<RandomValue> output) {
        // key: name of variable; value: a string array, where each element represents the value of each dimension
        Map<String, List<String>> varToIndexes = new HashMap<>();

        Map<String, String> nameToType = new HashMap<>();
        for (String key : functionConfig.getBoundOfArgumentsAndGlobalVariables().keySet()) {
            IFunctionConfigBound bound = functionConfig.getBoundOfArgumentsAndGlobalVariables().get(key);

            if (bound instanceof PointerOrArrayBound) {

                // get indexes of pointer/array
                List<String> indexes = new ArrayList<>();
                List<String> boundIndexes = ((PointerOrArrayBound) bound).getIndexes();
                for (String boundIndex : boundIndexes)
                    if (boundIndex.contains(PointerOrArrayBound.RANGE_DELIMITER)) {
                        indexes.add(boundIndex.split(PointerOrArrayBound.RANGE_DELIMITER)[1]); //just get the maximum index
                    } else {
                        indexes.add(boundIndex);
                    }

                String nameVar = key;
                String type = ((PointerOrArrayBound) bound).getType();
                nameToType.put(key, type);

                if (!varToIndexes.containsKey(nameVar)) {
                    // initialize max size of array/pointer
                    List<String> initialIndexes = new ArrayList<>();
                    for (int i = 0; i < indexes.size(); i++) {
                        initialIndexes.add("0"); // default size
                    }
                    varToIndexes.put(nameVar, initialIndexes);
                }

                List<String> values = varToIndexes.get(nameVar);
                for (int i = 0; i < indexes.size(); i++) {
                    Long newIndexNum = Long.parseLong(indexes.get(i)) + 1;

                    Long maxIndex = Long.parseLong(values.get(i));
                    if (newIndexNum > maxIndex) {
                        values.remove(i);
                        values.add(i, newIndexNum + "");

                        varToIndexes.remove(nameVar);
                        varToIndexes.put(nameVar, values);
                    }
                }
            }
        }

        // convert to sizeof
        for (String nameVar : varToIndexes.keySet()) {
            String type = nameToType.get(nameVar);
            List<String> indexes = varToIndexes.get(nameVar);

            if (indexes.size() == 1) {
                long m = Long.parseLong(indexes.get(0));

                m = m > MAX ? MAX : m;

                output.add(new RandomValue(nameVar, String.format("sizeof(%s)=%s", nameVar, m)));


                // create possible elements
                for (int i = 0; i < m; i++) {
                    String nameArrayWithIndex = nameVar + "[" + i + "]";
                    RandomValue rv = new RandomValue(nameArrayWithIndex, getDefaultValue(type, functionConfig));
                    if (!output.contains(rv))
                        output.add(rv);
                }

            } else if (indexes.size() == 2) {
                long m = Long.parseLong(indexes.get(0));
                long n = Long.parseLong(indexes.get(1));

                if (m * n > MAX) {
                    // to avoid processing too long
                    m = m > MAX_DIMENSION ? MAX_DIMENSION : m;
                    n = n > MAX_DIMENSION ? MAX_DIMENSION : n;
                }

                for (int i = 0; i < m; i++) {
                    String nameArrayWithIndex = nameVar + "[" + i + "]";
                    RandomValue rv = new RandomValue(nameVar,
                            String.format("sizeof(%s)=%s", nameArrayWithIndex, n));
                    if (!output.contains(rv))
                        output.add(rv);
                }

                // create possible elements
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < n; j++) {
                        String nameArrayWithIndex = nameVar + "[" + i + "][" + j + "]";
                        RandomValue rv = new RandomValue(nameArrayWithIndex, getDefaultValue(type, functionConfig));
                        if (!output.contains(rv))
                            output.add(rv);
                    }

            } else if (indexes.size() == 3) {
                long m = Long.parseLong(indexes.get(0));
                long n = Long.parseLong(indexes.get(1));
                long k = Long.parseLong(indexes.get(2));

                if (m * n > MAX) {
                    // to avoid processing too long
                    m = m > MAX_DIMENSION ? MAX_DIMENSION : m;
                    n = n > MAX_DIMENSION ? MAX_DIMENSION : n;
                    k = k > MAX_DIMENSION ? MAX_DIMENSION : k;
                }

                output.add(new RandomValue(nameVar, String.format("sizeof(%s)=%s", nameVar, m)));

                for (int i = 0; i < m; i++) {
                    String nameArrayWithIndex = nameVar + "[" + i + "]";
                    RandomValue rv = new RandomValue(nameVar,
                            String.format("sizeof(%s)=%s", nameArrayWithIndex, n));
                    if (!output.contains(rv))
                        output.add(rv);
                }

                // create possible elements
                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < n; j++) {
                        String nameArrayWithIndex = nameVar + "[" + i + "][" + j + "]";
                        RandomValue rv = new RandomValue(nameVar,
                                String.format("sizeof(%s)=%s", nameArrayWithIndex, k));
                        if (!output.contains(rv))
                            output.add(rv);
                    }
                }

                for (int i = 0; i < m; i++)
                    for (int j = 0; j < n; j++)
                        for (int h = 0; h < n; h++) {
                            String nameArrayWithIndex = nameVar + "[" + i + "][" + j + "][" + h + "]";
                            RandomValue rv = new RandomValue(nameArrayWithIndex, getDefaultValue(type, functionConfig));
                            if (!output.contains(rv))
                                output.add(rv);
                        }
            }
        }
    }

    private String getDefaultValue(String type, IFunctionConfig functionConfig) {
        String v = "";
        if (VariableTypeUtils.isNumMultiDimension(type)
                || VariableTypeUtils.isBoolMultiDimension(type)) {
            v = functionConfig.getBoundOfOtherNumberVars().getLower();
            type = type.substring(0, type.indexOf("["));

            if (v.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getLower();
            } else if (v.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getUpper();
            }

        } else if (VariableTypeUtils.isNumMultiLevel(type)
                || VariableTypeUtils.isBoolMultiLevel(type)) {
            v = functionConfig.getBoundOfOtherNumberVars().getLower();
            type = type.substring(0, type.indexOf("*"));

            if (v.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getLower();
            } else if (v.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getUpper();
            }

        } else if (VariableTypeUtils.isChMultiLevel(type)) {
            v = functionConfig.getBoundOfOtherCharacterVars().getLower();
            type = type.substring(0, type.indexOf("*"));

            if (v.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getLower();
            } else if (v.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getUpper();
            }
        } else if (VariableTypeUtils.isChMultiDimension(type)) {
            v = functionConfig.getBoundOfOtherCharacterVars().getLower();
            type = type.substring(0, type.indexOf("["));

            if (v.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getLower();
            } else if (v.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
                v = Environment.getBoundOfDataTypes().getBounds().get(type).getUpper();
            }
        }
        return v;
    }

    private void updateDefaultValue(Map<String, String> testcases, List<RandomValue> randomValues) {
        // find default values
        Map<String, String> defaults = new HashMap<>();
        for (String key : testcases.keySet()) {
            if (isArray(key) && key.contains("<other indexes>")) {
                String value = testcases.get(key);
                defaults.put(key.substring(0, key.indexOf("[")), value);
            }
        }
        logger.debug("Defaults value = " + defaults);

        // update the elements which do not have any value
        for (RandomValue randomValue : randomValues)
            if (isArray(randomValue.getNameUsedInExpansion())) {
                String name = randomValue.getNameUsedInExpansion()
                        .substring(0, randomValue.getNameUsedInExpansion().indexOf("["));

                String v = defaults.get(name);
                if (v != null)
                    randomValue.setValue(v);
            }
    }

    public List<RandomValue> findValueOfElement(Map<String, String> testcases, List<RandomValue> output) {
        for (String key : testcases.keySet()) {
            if (key.contains("<other indexes>")) {
                // ignore
            } else {
                RandomValue randomValue = new RandomValue(key, testcases.get(key));

                int index = output.indexOf(randomValue);
                if (index >= 0) {
                    output.remove(index);
                    output.add(randomValue);
                }
            }
        }
        return output;
    }

    private boolean isArray(String v) {
        return v.contains("[");
    }

    public Map<String, String> getTestcases() {
        return testcases;
    }

    public void setTestcases(Map<String, String> testcases) {
        this.testcases = testcases;
    }

    public ICommonFunctionNode getFn() {
        return fn;
    }

    public void setFn(ICommonFunctionNode fn) {
        this.fn = fn;
    }

    public static final long MAX = 1000;
    public static final long MAX_DIMENSION = 20;
    public static final String DELIMITER_BETWEEN_TESTCASES = ";";
    public static final String DELIMITER_BETWEEN_KEY_AND_VALUE = "=";
}
