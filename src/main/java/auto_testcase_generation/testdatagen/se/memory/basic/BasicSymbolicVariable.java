package auto_testcase_generation.testdatagen.se.memory.basic;

import java.util.ArrayList;
import java.util.List;

import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.PhysicalCell;
import auto_testcase_generation.testdatagen.se.memory.SymbolicVariable;
import auto_testcase_generation.testdatagen.testdatainit.VariableTypes;
import com.dse.util.IRegex;

/**
 * Represent basic variable (number and character)
 *
 * @author ducanh
 */
public abstract class BasicSymbolicVariable extends SymbolicVariable {

    protected PhysicalCell physicalCell;

    public BasicSymbolicVariable(String name, String type, int scopeLevel, String value) {
        super(name, type, scopeLevel);
        physicalCell = new PhysicalCell(value);
    }

    @Override
    public String toString() {
        return "name=" + name + ", value=" + getSymbolicValue();
    }

    @Override
    public boolean isBasicType() {
        return true;
    }

    public String getSymbolicValue() {
        return physicalCell.getValue();
    }

    public void setValue(String value) {
        if (!VariableTypes.isNumFloat(type)) {
            if (value.matches(IRegex.INTEGER_NUMBER_REGEX))
                physicalCell.setValue(value);
            else
                // physicalCell.setValue(ISymbolicExecution.TO_INT_Z3 + "*(" +
                // value + "+0)");
                physicalCell.setValue("(" + value + "+0)");
        } else
            physicalCell.setValue(value);
    }

    public PhysicalCell getCell() {
        return physicalCell;
    }

    public void setCell(PhysicalCell cell) {
        physicalCell = cell;
    }

    @Override
    public boolean assign(ISymbolicVariable other) {
        if (!(other instanceof BasicSymbolicVariable))
            return false;

        BasicSymbolicVariable basicVar = (BasicSymbolicVariable) other;
        setValue(basicVar.getSymbolicValue());

        return true;
    }

    @Override
    public List<PhysicalCell> getAllPhysicalCells() {
        List<PhysicalCell> physicalCells = new ArrayList<>();
        physicalCells.add(physicalCell);
        return physicalCells;
    }
}
