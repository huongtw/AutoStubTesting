package auto_testcase_generation.testdatagen.se;

import java.util.Objects;

/**
 * path constraint: "trie[0].root_node != NULL" ---> new variable: "trie[0].root_node",  "trie", "trie[0]"
 */
public class NewVariableInSe {
    private String originalName; // come from the constraint
    private String normalizedName; // used in smt-lib
    private boolean isInteger = true;

    public NewVariableInSe(String originalName, String normalizedName){
        this.originalName = originalName;
        this.normalizedName = normalizedName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    @Override
    public String toString() {
        return String.format("original: \"%s\"; modified: \"%s\"\n", originalName, normalizedName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewVariableInSe that = (NewVariableInSe) o;
        return Objects.equals(originalName, that.originalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalName);
    }
}
