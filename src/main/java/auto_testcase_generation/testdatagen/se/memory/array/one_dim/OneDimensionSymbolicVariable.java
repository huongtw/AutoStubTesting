package auto_testcase_generation.testdatagen.se.memory.array.one_dim;

import auto_testcase_generation.testdatagen.se.memory.array.ArraySymbolicVariable;

/**
 * Represent one dimension pointer
 *
 * @author ducanh
 */
public abstract class OneDimensionSymbolicVariable extends ArraySymbolicVariable {

    public OneDimensionSymbolicVariable(String name, String type, int scopeLevel) {
        super(name, type, scopeLevel);
    }

}
