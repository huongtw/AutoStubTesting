package auto_testcase_generation.testdatagen.se.memory.pointer;

/**
 * Represent one level pointer
 *
 * @author lamnt
 */
public abstract class PointerStructureSymbolicVariable extends PointerSymbolicVariable {
    public PointerStructureSymbolicVariable(String name, String type, int scopeLevel) {
        super(name, type, scopeLevel);
    }
}
