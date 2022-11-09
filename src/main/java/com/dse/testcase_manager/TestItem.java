package com.dse.testcase_manager;

import com.dse.testcasescript.object.TestNameNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.util.DateTimeUtils;
import com.dse.util.SpecialCharacter;

import java.time.LocalDateTime;

public abstract class TestItem implements ITestItem {
    // name of test case
    private String name;

    private TestNewNode testNewNode;

    // the path of the test case
    private String path;

    private LocalDateTime creationDateTime;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = removeSpecialCharacter(name);

    }

    @Override
    public TestNewNode getTestNewNode() {
        return testNewNode;
    }

    @Override
    public void setTestNewNode(TestNewNode testNewNode) {
        this.testNewNode = testNewNode;
    }

    @Override
    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    @Override
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    @Override
    public String getCreationDate() {
        return DateTimeUtils.getDate(creationDateTime);
    }

    @Override
    public String getCreationTime() {
        return DateTimeUtils.getTime(creationDateTime);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = removeSysPathInName(path);
    }

    public static String removeSysPathInName(String path) {
        // name could not have File.separator
        return path.replaceAll("operator\\s*/", "operator_division");
    }

    public static String removeSpecialCharacter(String name) {
        return name
                .replace("+", "plus")
                .replace("-", "minus")
                .replace("*", "mul")
                .replace("/", "div")
                .replace("%", "mod")
                .replace("=", "equal")
                .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE)
                .replaceAll("[_]+", SpecialCharacter.UNDERSCORE);
    }

    protected void updateTestNewNode(String name) {
        testNewNode = new TestNewNode();
        TestNameNode testNameNode = new TestNameNode();
        testNameNode.setName(name);
        testNewNode.getChildren().add(testNameNode);
        testNameNode.setParent(testNewNode);
    }
}
