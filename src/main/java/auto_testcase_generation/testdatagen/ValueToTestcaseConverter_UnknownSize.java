package auto_testcase_generation.testdatagen;

import auto_testcase_generation.testdatagen.se.CustomJeval;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use when we do not know the size of array/pointer
 */
public class ValueToTestcaseConverter_UnknownSize {
    final static AkaLogger logger = AkaLogger.get(RandomValue.class);

    private Map<String, String> testcases = new HashMap<>();

    public static void main(String[] args) {
        String testcases = "b[9]=5;b[9][4]=3;b=10";
//        String testcases = "a[2]=1;a[<other indexes>]=-1;a[6]=90;"; // means a[0]=a[1]=-1, a[2]=1
//        String testcases = "trie[0].root_node=0;trie[0]=1;trie=1";
//        String testcases = "a[1][1]]=1;a[2][3]=2;a[<other indexes>][<other indexes>]=-100;";
        ValueToTestcaseConverter_UnknownSize converter = new ValueToTestcaseConverter_UnknownSize(testcases);
        List<RandomValue> randomValues = converter.convert();
        System.out.println(randomValues);
    }

    public List<RandomValue> convert() {
        List<RandomValue> output = new ArrayList<>();
        findSizeOfPointerandArray(getTestcases(), output);
        updateDefaultValue(getTestcases(), output);
        findValueOfElement(getTestcases(), output);
        return output;
    }

    public ValueToTestcaseConverter_UnknownSize(String testcases) {
        if (testcases != null && testcases.length() > 0) {
            String[] tc = testcases.split(DELIMITER_BETWEEN_TESTCASES);
            for (String item : tc)
                if (item.contains(DELIMITER_BETWEEN_KEY_AND_VALUE)) {
                    String key = item.split(DELIMITER_BETWEEN_KEY_AND_VALUE)[0];
                    String value = item.split(DELIMITER_BETWEEN_KEY_AND_VALUE)[1];

                    value = new CustomJeval().evaluate(value);
                    this.testcases.put(key, value);
                }
            logger.debug(this.testcases);
        }
    }

    public List<RandomValue> findSizeOfPointerandArray(Map<String, String> testcases, List<RandomValue> output) {
        // key: variable name
        // value: the maximum size of each dimension
        Map<String, List<String>> varToIndexes = new HashMap<>();

        // get the maximum index
        for (String key : testcases.keySet()) {
            if (key.contains("<other indexes>"))
                key = key.replace("<other indexes>", "1");

            List<String> indexes = Utils.getIndexOfArray(key);
            if (indexes.size() > 0) {
                String nameVar = Utils.getNameVariable(key);

                if (!varToIndexes.containsKey(nameVar)) {
                    // initialize size of array/pointer
                    List<String> initialIndexes = new ArrayList<>();
                    for (int i = 0; i < indexes.size(); i++)
                        if (indexes.get(i).equals(""))
                            initialIndexes.add("1");
                        else {
                            try {
                                initialIndexes.add(Long.parseLong(indexes.get(i)) + 1 + "");
                            } catch (NumberFormatException e) {
                                logger.error("Cannot convert " + indexes.get(i) + " to index");
                                initialIndexes.add("1");
                            }
                        }
                    varToIndexes.put(nameVar, initialIndexes);

                } else {
                    // update size of array/pointer
                    List<String> values = varToIndexes.get(nameVar);
                    for (int i = 0; i < indexes.size(); i++) {
                        Long newIndexNum = Long.parseLong(indexes.get(i)) + 1;
                        Long maxIndex;
                        if(values.size()>i) {
                            maxIndex = Long.parseLong(values.get(i));
                            if (newIndexNum > maxIndex) {
                                values.remove(i);
                                values.add(i, newIndexNum + "");

                                varToIndexes.remove(nameVar);
                                varToIndexes.put(nameVar, values);
                            }
                        }else{
                            values.add(i, newIndexNum + "");
                            varToIndexes.put(nameVar, values);
                        }
                    }
                }
        }
        }

        // convert to sizeof
        for (String nameVar : varToIndexes.keySet()) {
            List<String> indexes = varToIndexes.get(nameVar);
            if (indexes.size() == 0)
                continue;

            long firstSize = Long.parseLong(indexes.get(0));
            RandomValue rv = new RandomValueForSizeOf(nameVar, firstSize + "");
            if (!output.contains(rv))
                output.add(rv);


            if (indexes.size() == 1) {
                // create possible elements
                for (int i = 0; i < firstSize; i++) {
                    String nameArrayWithIndex = nameVar + "[" + i + "]";
                    RandomValue elementValue = new RandomValueForAssignment(nameArrayWithIndex, "");
                    if (!output.contains(elementValue))
                        output.add(elementValue);
                }

            } else if (indexes.size() == 2) {
                long secondSize = Long.parseLong(indexes.get(1));

                for (int i = 0; i < firstSize; i++) {
                    String nameArrayWithIndex = nameVar + "[" + i + "]";
                    RandomValue sizeItem = new RandomValueForSizeOf(nameArrayWithIndex, secondSize + "");
                    if (!output.contains(sizeItem))
                        output.add(sizeItem);
                }

                // create possible elements
                for (int i = 0; i < firstSize; i++)
                    for (int j = 0; j < secondSize; j++) {
                        String nameArrayWithIndex = nameVar + "[" + i + "][" + j + "]";
                        RandomValue valueElement = new RandomValueForAssignment(nameArrayWithIndex, "");
                        if (!output.contains(valueElement))
                            output.add(valueElement);
                    }

            } else if (indexes.size() == 3) {
                long secondSize = Long.parseLong(indexes.get(1));
                long thirdSize = Long.parseLong(indexes.get(2));

                for (int i = 0; i < firstSize; i++) {
                    String nameArrayWithIndex = nameVar + "[" + i + "]";
                    RandomValue elementValue = new RandomValueForSizeOf(nameArrayWithIndex, secondSize + "");
                    if (!output.contains(elementValue))
                        output.add(elementValue);
                }

                for (int i = 0; i < firstSize; i++) {
                    for (int j = 0; j < secondSize; j++) {
                        String nameArrayWithIndex = nameVar + "[" + i + "][" + j + "]";
                        RandomValue elementValue = new RandomValueForSizeOf(nameArrayWithIndex, thirdSize + "");
                        if (!output.contains(elementValue))
                            output.add(elementValue);
                    }
                }

                for (int i = 0; i < firstSize; i++)
                    for (int j = 0; j < secondSize; j++)
                        for (int h = 0; h < secondSize; h++) {
                            String nameArrayWithIndex = nameVar + "[" + i + "][" + j + "][" + h + "]";
                            RandomValue elementValue = new RandomValueForAssignment(nameArrayWithIndex, "");
                            if (!output.contains(elementValue))
                                output.add(elementValue);
                        }
            }
        }

        return output;
    }

    private void updateDefaultValue(Map<String, String> testcases, List<RandomValue> randomValues) {
        // find default values
        Map<String, String> defaults = new HashMap<>();
        for (String key : testcases.keySet())
            if (key.contains("<other indexes>")) {
                String key_norm = key.replace("<other indexes>", "25235252");
                List<String> indexes = Utils.getIndexOfArray(key_norm);
                if (indexes.size() >= 1) {
                    String value = testcases.get(key);
                    String name = Utils.getNameVariable(key_norm);
                    defaults.put(name, value);
                }
        }
        logger.debug("Defaults value = " + defaults);

        // update the elements which do not have any value
        for (RandomValue randomValue : randomValues)
            if (randomValue instanceof RandomValueForAssignment) {
                String name = Utils.getNameVariable(randomValue.getNameUsedInExpansion());

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
                RandomValue randomValue = new RandomValueForAssignment(key, testcases.get(key));

                int index = output.indexOf(randomValue);
                if (index > 0) {
                    output.remove(index);
                    output.add(randomValue);
                } else
                    output.add(randomValue);
            }
        }
        return output;
    }

    public Map<String, String> getTestcases() {
        return testcases;
    }

    public void setTestcases(Map<String, String> testcases) {
        this.testcases = testcases;
    }

    public static final String DELIMITER_BETWEEN_TESTCASES = ";";
    public static final String DELIMITER_BETWEEN_KEY_AND_VALUE = "=";
}
