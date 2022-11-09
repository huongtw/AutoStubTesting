package com.dse.testcasescript.object;

import com.dse.logger.AkaLogger;

import java.util.List;

/**
 * Test.NOTES
 * ...
 * TEST.END_NOTES
 */
public class TestNotesNode extends AbstractTestcaseNode {
    final static AkaLogger logger = AkaLogger.get(TestNotesNode.class);
    private String notes = "";

    public void analyzeBlock(List<String> block) {
        if (block.size() >= 3) {
            // ignore the beginning flag and the terminate flag
            for (int i = 1; i < block.size() - 2; i++)
                notes += block.get(i) + "\n";
            notes += block.get(block.size() - 2);
        } else {
            logger.error("The size of block in " + this.getClass() + " < 2");
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return super.toString() + ": notes=" + getNotes();
    }

    @Override
    public String exportToFile() {
        if (notes.length() > 0)
            return TEST_NOTES + "\n" + notes + "\n" + TEST_END_NOTES;
        else
            return TEST_NOTES + "\n" + TEST_END_NOTES;
    }
}
