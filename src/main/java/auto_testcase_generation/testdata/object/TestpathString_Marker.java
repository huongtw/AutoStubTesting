package auto_testcase_generation.testdata.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.instrument.FunctionInstrumentationForStatementvsBranch_Marker;
import auto_testcase_generation.instrument.IFunctionInstrumentationGeneration;

/**
 * Represent a test path generated from the instrumented function by using
 * #{FunctionInstrumentation_Marker.java}
 *
 * @author Duc Anh Nguyen
 *
 */
public class TestpathString_Marker implements ITestpathGeneratedFromExecutingFunction {
    // not in human readable format
    private String[] encodedTestpath;
    private float increasingCoveragePercentage = 0.0f;

    private List<StatementInTestpath_Mark> standardTestpathSet;

    public static void main(String[] args) {
        // In this example;
        // "###": delimiter between properties
        // "=>" : delimiter between nodes in a test path
        // "@#$": delimiter between a property and its value
        String stm1 = "line-in-function@#$1###offset@#$11###statement@#$int a = 0;";
        String stm2 = "line-in-function@#$4###offset@#$51###statement@#$a=b";
        String[] testpath = new String[] {stm1, stm2};
        TestpathString_Marker normalize = new TestpathString_Marker();
        normalize.setEncodedTestpath(testpath);
        System.out.println(
                normalize.getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Marker.STATEMENT));
        System.out.println(normalize.getStandardTestpathByProperty(
                FunctionInstrumentationForStatementvsBranch_Marker.LINE_NUMBER_IN_FUNCTION));
        System.out.println(normalize.getStandardTestpathByAllProperties());
    }

    @Override
    public String[] getEncodedTestpath() {
        return encodedTestpath;
    }

    @Override
    public void setEncodedTestpath(String[] encodedTestpath) {
        this.encodedTestpath = encodedTestpath;
    }

//    public void setEncodedTestpath(String[] encodedTestpath) {
//        if (encodedTestpath.length >= 1) {
//            for (int i = 0; i < encodedTestpath.length - 1; i++)
//                this.encodedTestpath += encodedTestpath[i] + ITestpathInCFG.SEPARATE_BETWEEN_NODES;
//            this.encodedTestpath += encodedTestpath[encodedTestpath.length - 1];
//        }
//    }

    /**
     * Get a standard test path by removing redundant information
     *
     * See properties in #{FunctionInstrumentation_Marker}
     *
     * @param nameProperty
     *            the name of property which you want to extract its information
     * @return
     */
    public ArrayList<String> getStandardTestpathByProperty(String nameProperty) {
        ArrayList<String> standardTestpath = new ArrayList<>();

        for (StatementInTestpath_Mark properties : getStandardTestpathSetByAllProperties()) {
            if (properties != null && properties.getPropertyByName(nameProperty) != null)
                standardTestpath.add(properties.getPropertyByName(nameProperty).getValue());
        }
        
        return standardTestpath;
    }

    public ArrayList<StatementInTestpath_Mark> getStandardTestpathByAllProperties() {
        ArrayList<StatementInTestpath_Mark> standardTestpath = new ArrayList<>();
        String[] testpathItems = encodedTestpath;
        for (String testpathItem : testpathItems)
            standardTestpath.add(lineExtractor(testpathItem));

        return standardTestpath;
    }

    public List<StatementInTestpath_Mark> getStandardTestpathSetByAllProperties() {
        if (standardTestpathSet == null) {
            standardTestpathSet = new ArrayList<>();
            String[] testpathItems = encodedTestpath;
            for (String testpathItem : testpathItems) {
                StatementInTestpath_Mark line = lineExtractor(testpathItem);
                standardTestpathSet.add(line);
            }
        }

        return standardTestpathSet;
    }

    /**
     * Parse a node in test path (corresponding to the marker of a statement)
     *
     * @param line
     * @return
     */
    public static StatementInTestpath_Mark lineExtractor(String line) {
        StatementInTestpath_Mark properties = new StatementInTestpath_Mark();
        String[] tokens = line.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES);

        for (String token : tokens)
            if (token.contains(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)) {
                String nameProperty = token.substring(0, token.indexOf(
                        IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE));
                String valueProperty = token.substring(token.indexOf(
                        IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)
                        + IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE
                        .length());
                properties.getProperties().put(nameProperty, valueProperty);
            }

        return properties;
    }

    @Override
    public String toString() {
        return getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Marker.STATEMENT).toString();
    }

//    public void appendAStatement(StatementInTestpath_Mark propertiesInNode) {
//        if (encodedTestpath.length() > 0)
//            encodedTestpath += ITestpathInCFG.SEPARATE_BETWEEN_NODES;
//
//        for (Property_Marker property : propertiesInNode.getProperties())
//            encodedTestpath += property.getKey()
//                    + FunctionInstrumentationForStatementvsBranch_Marker.DELIMITER_BETWEEN_PROPERTY_AND_VALUE
//                    + property.getValue()
//                    + FunctionInstrumentationForStatementvsBranch_Marker.DELIMITER_BETWEEN_PROPERTIES;
//
//        encodedTestpath = encodedTestpath.substring(0,
//                encodedTestpath.length() - ITestpathInCFG.SEPARATE_BETWEEN_NODES.length() - 1);
//    }

    public float getIncreasingCoveragePercentage() {
        return increasingCoveragePercentage;
    }

    public void setIncreasingCoveragePercentage(float increasingCoveragePercentage) {
        this.increasingCoveragePercentage = increasingCoveragePercentage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestpathString_Marker) {
            TestpathString_Marker cast = (TestpathString_Marker) obj;
            return cast.getEncodedTestpath().equals(this.getEncodedTestpath());
        } else
            return false;
    }
}
