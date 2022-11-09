package com.dse.testcase_manager;

import com.dse.config.WorkspaceConfig;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a compound test case
 */
public class CompoundTestCase extends AbstractTestCase {
    private final static AkaLogger logger = AkaLogger.get(CompoundTestCase.class);

    private final List<TestCaseSlot> slots = new ArrayList<>();

    // this constructor is used for development
    public CompoundTestCase() {
    }

    public CompoundTestCase(String name) {
        setName(name);
    }

    public void setName(String name) {
        super.setName(name);
        if (getPath() == null)
            setPathDefault();
        updateTestNewNode(name);
    }

    @Override
    public void setPathDefault() {
        String testcasePath = new WorkspaceConfig().fromJson().getCompoundTestcaseDirectory() + File.separator + getName() + ".json";
        setPath(testcasePath);
    }

    public List<TestCaseSlot> getSlots() {
        return slots;
    }

    // for development
    public void setNameAndPath(String name, String path) {
        super.setName(name);
        setPath(path);
    }

    public void changeOrder(int source, int target) {
        TestCaseSlot slotSource = getSlotBySlotNumber(source);
        if (slotSource != null) {
            if (source < target) { // move down
                for (TestCaseSlot slot : getSlots()) {
                    int num = slot.getSlotNum();
                    if (num > source && num <= target) {
                        slot.setSlotNum(num - 1);
                    }
                    slotSource.setSlotNum(target);
                }
            } else if (source > target) { // move up
                for (TestCaseSlot slot : getSlots()) {
                    int num = slot.getSlotNum();
                    if (num < source && num >= target) {
                        slot.setSlotNum(num + 1);
                    }
                    slotSource.setSlotNum(target);
                }
            }
        }
    }

    private TestCaseSlot getSlotBySlotNumber(int num) {
        for (TestCaseSlot slot: slots) {
            if (slot.getSlotNum() == num) {
                return slot;
            }
        }
        logger.error("slot not found.");
        return null;
    }

    private void deleteSlot(TestCaseSlot slot) {
        changeOrder(slot.getSlotNum(), slots.size() - 1);
        slots.remove(slot);
    }

    public void validateSlots() {
        List<TestCaseSlot> shouldBeDelete = new ArrayList<>();
        for (TestCaseSlot slot : slots) {
            if (! slot.validate()) {
                shouldBeDelete.add(slot);
            }
        }

        for (TestCaseSlot slot : shouldBeDelete) {
            deleteSlot(slot);
        }
    }

    @Override
    public String generateDefinitionCompileCmd() {
        StringBuilder output = new StringBuilder();

        for (TestCaseSlot slot : slots) {
            String testCaseName = slot.getTestcaseName();

            String defineName = testCaseName.toUpperCase()
                    .replace(SpecialCharacter.DOT, SpecialCharacter.UNDERSCORE_CHAR);

            String define = String.format("-DAKA_TC_%s", defineName);

            if (slots.indexOf(slot) != 0)
                output.append(SpecialCharacter.SPACE);

            output.append(define);
        }

        return output.toString();
    }

    @Override
    protected List<INode> getAllRelatedFileToLink() {
        List<INode> sourceNodes = new ArrayList<>();

        for (TestCaseSlot slot : slots) {
            String testCaseName = slot.getTestcaseName();
            TestCase testCase = TestCaseManager.getBasicTestCaseByName(testCaseName);
            ICommonFunctionNode functionNode = testCase.getFunctionNode();

            INode sourceNode = Utils.getSourcecodeFile(functionNode);

            if (!sourceNodes.contains(sourceNode))
                sourceNodes.add(sourceNode);

            sourceNode.getDependencies().stream()
                    .filter(d -> d instanceof IncludeHeaderDependency && d.getEndArrow().equals(sourceNode))
                    .forEach(d -> {
                        INode start = d.getStartArrow();
                        if (!sourceNodes.contains(start))
                            sourceNodes.add(start);
                    });
        }

        return sourceNodes;
    }

    @Override
    public List<String> getAdditionalIncludes() {
        List<String> list = new ArrayList<>();

        for (TestCaseSlot slot : slots) {
            String testCaseName = slot.getTestcaseName();
            TestCase testCase = TestCaseManager.getBasicTestCaseByName(testCaseName);

            if (testCase != null) {
                for (String include : testCase.getAdditionalIncludes()) {
                    if (!list.contains(include))
                        list.add(include);
                }
            }
        }

        return list;
    }
}
