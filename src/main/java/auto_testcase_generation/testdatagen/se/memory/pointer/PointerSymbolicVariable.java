package auto_testcase_generation.testdatagen.se.memory.pointer;

import auto_testcase_generation.testdatagen.se.memory.*;
import auto_testcase_generation.testdatagen.se.memory.array.ArraySymbolicVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent pointer variable
 *
 * @author ducanh
 */
public class PointerSymbolicVariable extends SymbolicVariable {

    public static final int FIRST_INDEX = 0;
    /**
     * Reference of variable
     */
    protected Reference reference;

    /**
     * Size of the variable. Ex: char* s = new char[100];
     */
    private String size;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public PointerSymbolicVariable(String name, String type, int scopeLevel) {
        super(name, type, scopeLevel);
        /*
		 * Automatically initialize the reference of the variable by assuming it
		 * is not NULL.
		 */
        reference = new Reference(new LogicBlock(name));
        reference.setStartIndex(PointerSymbolicVariable.FIRST_INDEX + "");
    }

    @Override
    public String toString() {
        if (reference != null)
            return "name=" + name + "\n\t, reference=" + reference.toString();
        else
            return "name=" + name + ", reference=null";
    }

    @Override
    public boolean isBasicType() {
        return false;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public List<PhysicalCell> getAllPhysicalCells() {
        List<PhysicalCell> physicalCells = new ArrayList<>();

        if (reference != null)
            for (LogicCell logicCell : reference.getBlock())
                physicalCells.add(logicCell.getPhysicalCell());
        return physicalCells;
    }

    public List<LogicCell> getAllLogicCells() {
        List<LogicCell> logicCells = new ArrayList<>();

        if (reference != null)
            logicCells.addAll(reference.getBlock());
        return logicCells;
    }

    @Override
    public boolean assign(ISymbolicVariable other) {
        if (other instanceof PointerSymbolicVariable) {
            /*
             * p = numbers+3; (p: pointer, numbers: one-dimension array)
             */
            Reference r = ((PointerSymbolicVariable) other).getReference();

            if (r != null) {
                reference.setBlock(r.getBlock());
                reference.setStartIndex(r.getStartIndex());
            }

            return true;

        } else if (other instanceof ArraySymbolicVariable) {
            LogicBlock b = ((ArraySymbolicVariable) other).getBlock();

            if (b != null) {
                reference.setBlock(b);
                reference.setStartIndex(Reference.FIRST_START_INDEX);
            }

            return true;
        }

        return false;
    }
}
