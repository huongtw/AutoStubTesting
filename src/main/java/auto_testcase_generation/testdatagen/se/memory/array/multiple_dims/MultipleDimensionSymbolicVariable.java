package auto_testcase_generation.testdatagen.se.memory.array.multiple_dims;

import auto_testcase_generation.testdatagen.se.memory.array.ArraySymbolicVariable;

/**
 * Represent one dimension pointer
 *
 * @author ducanh
 */
public abstract class MultipleDimensionSymbolicVariable extends ArraySymbolicVariable {

    public MultipleDimensionSymbolicVariable(String name, String type, int scopeLevel) {
        super(name, type, scopeLevel);
    }

}
