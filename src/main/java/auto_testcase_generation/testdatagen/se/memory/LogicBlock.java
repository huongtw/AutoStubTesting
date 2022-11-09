package auto_testcase_generation.testdatagen.se.memory;

import com.dse.util.Utils;

import java.util.ArrayList;

public class LogicBlock extends ArrayList<LogicCell> {
	public static final String FIRST_CELL = "0";
	/**
	 * The name of block.
	 */
	protected String nameBlock;

	public LogicBlock(String name) {
		nameBlock = name;
	}

	/**
	 * @param cell PhysicalCell
	 * @param index
	 *            index of element in block. Ex: 1, 2, [1], [1][2]
	 *
	 * @return true/false
	 */
	public boolean addLogicalCell(PhysicalCell cell, String index) {
		return this.add(new LogicCell(cell, index));
	}

	/**
	 * Find a cell in block by its id
	 *
	 * @param normalizedIndex
	 *            represent index of cell. This index is normalized by shorten
	 *            (e.g., [1+1]---->[2]); does not contains redundant spaces. Ex: 0,
	 *            [1], [2], [1+x], [2][3]
	 * @return LogicCell
	 */
	public LogicCell findCellByIndex(String normalizedIndex) {
		for (LogicCell cell : this)
			if (cell.getIndex().equals(normalizedIndex) || cell.getIndex().equals(Utils.asIndex(normalizedIndex))
					|| Utils.asIndex(cell.getIndex()).equals(normalizedIndex)
					|| Utils.asIndex(cell.getIndex()).equals(Utils.asIndex(normalizedIndex)))
				return cell;
		return null;
	}

	@Override
	public String toString() {
		StringBuilder output;
		if (size() == 0)
			output = new StringBuilder("<block name=" + getName() + "></block>");
		else {
			output = new StringBuilder("<block name=" + getName() + ">");
			for (LogicCell c : this)
				output.append(c.toString()).append(" | ");
			output.append("</block>");
		}

		return output.toString();
	}

	public boolean updateCellByIndex(String index, String newValue) {
		LogicCell updatedCell = findCellByIndex(index);
		if (updatedCell != null) {
			updatedCell.getPhysicalCell().setValue(newValue);
			return true;
		} else
			return false;
	}

	public String getName() {
		return nameBlock;
	}

	public void setName(String name) {
		nameBlock = name;
	}
}
