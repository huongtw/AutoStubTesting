package com.dse.testcase_manager;

import com.dse.testcasescript.object.TestNewNode;

import java.time.LocalDateTime;
import java.util.List;

public interface ITestItem {
    String getName();

    void setName(String name);

    TestNewNode getTestNewNode();

    void setTestNewNode(TestNewNode testNewNode);

    void setCreationDateTime(LocalDateTime creationDateTime);

    LocalDateTime getCreationDateTime();

    String getCreationDate();

    String getCreationTime();

    String getPath();

    void setPathDefault();

    void setPath(String path);

    boolean isPrototypeTestcase();

    List<String> getAdditionalIncludes();
}
