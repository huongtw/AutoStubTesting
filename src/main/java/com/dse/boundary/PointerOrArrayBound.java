package com.dse.boundary;

import com.dse.config.IFunctionConfigBound;

import java.util.ArrayList;
import java.util.List;

/**
 * Bound of pointer/array variable
 *
 * @author ducanh
 */
public class PointerOrArrayBound implements IFunctionConfigBound {

    private List<String> indexes = new ArrayList<>();

    private String type;

    public PointerOrArrayBound(){}

    public PointerOrArrayBound(String[] normalizedIndexes, String type) {
        for (String index : normalizedIndexes)
            indexes.add(index);

        this.type = type;
    }

    public PointerOrArrayBound(List<String> normalizedIndexes, String type) {
        this.indexes = normalizedIndexes;
        this.type = type;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }

    public String showIndexes() {
        String output = "";
        for (String index : indexes)
            output += index + IFunctionConfigBound.INDEX_DELIMITER;
        output = output.substring(0, output.length() - 1);
        return output;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
