package auto_testcase_generation.testdatagen.se.memory.structure;

import auto_testcase_generation.testdatagen.se.memory.PhysicalCell;
import auto_testcase_generation.testdatagen.se.memory.basic.BasicSymbolicVariable;

import java.util.ArrayList;
import java.util.List;

public class UnionSymbolicVariable extends BasicSymbolicVariable {

	public UnionSymbolicVariable(String name, String type, int scopeLevel, String value) {
		super(name, type, scopeLevel, value);
	}

	@Override
	public List<PhysicalCell> getAllPhysicalCells() {
		// TODO: Get all physical cells of this symbolic variable
		return new ArrayList<>();
	}
}
