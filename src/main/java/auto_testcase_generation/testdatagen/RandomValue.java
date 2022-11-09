package auto_testcase_generation.testdatagen;

import com.dse.logger.AkaLogger;

/**
 * Represent a random value in automated test data generation
 */
public class RandomValue {
    final static AkaLogger logger = AkaLogger.get(RandomValue.class);

    // used to expand the data tree
    private String nameUsedInExpansion;

    // used to update value of a node in data tree
    private String nameUsedToUpdateValue;

    private String value;

    public RandomValue(String nameUsedInExpansion, String value) {
        this.nameUsedInExpansion = nameUsedInExpansion;
        this.nameUsedToUpdateValue = convertNameUsedInExpansionToCode(nameUsedInExpansion);
        this.value = value;
//        logger.debug("Added " + this.toString());
    }

    public static String convertNameUsedInExpansionToCode(String nameUsedInExpansion) {
        return nameUsedInExpansion.replace("]", "")
                .replace("[", "")
                .replace(" ", "")
                .replace("->", "_")
                .replace(".","_");
    }

    public String getNameUsedToUpdateValue() {
        return nameUsedToUpdateValue;
    }

    public void setNameUsedToUpdateValue(String nameUsedToUpdateValue) {
        this.nameUsedToUpdateValue = nameUsedToUpdateValue;
    }

    public String getNameUsedInExpansion() {
        return nameUsedInExpansion;
    }

    public void setNameUsedInExpansion(String nameUsedInExpansion) {
        this.nameUsedInExpansion = nameUsedInExpansion;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RandomValue) {
            if (getNameUsedInExpansion().equals(((RandomValue) obj).getNameUsedInExpansion()))
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "nameUsedInExpansion = \"" + getNameUsedInExpansion() + "\"; nameUsedInCode = \"" + nameUsedToUpdateValue + "\", value = \"" + value + "\"\n";
    }
}
