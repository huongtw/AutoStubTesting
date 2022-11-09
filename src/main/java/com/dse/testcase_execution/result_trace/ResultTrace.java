package com.dse.testcase_execution.result_trace;

import com.dse.testcase_execution.AbstractTestcaseExecution;
import com.dse.testcase_manager.ITestCase;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResultTrace extends AbstractResultTrace {

    private String message;

    private String userCode;

    private String actualName, expectedName;

    private String actualVal, expectedVal;

    private String tag;

    @Override
    public String getExpected() {
        return expectedVal;
    }

    @Override
    public String getExpectedName() {
        return expectedName;
    }

    @Override
    public String getActualName() {
        return actualName;
    }

    @Override
    public String getActual() {
        return actualVal;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String getUserCode() {
        return userCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static List<IResultTrace> load(ITestCase testCase) {
        List<IResultTrace> list = new ArrayList<>();

        String path = testCase.getExecutionResultTrace();

        if (!new File(path).exists())
            return null;

        String content = Utils.readFileContent(path);

        if (!content.trim().isEmpty()) {
            JsonArray jsonArray;
            try {
                jsonArray = JsonParser.parseString(content).getAsJsonArray();
            } catch (JsonSyntaxException e) {
                content = AbstractTestcaseExecution.refactorResultTrace(testCase);
                jsonArray = JsonParser.parseString(content).getAsJsonArray();
            }

            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                ResultTrace failure = new ResultTrace();

                String tag = jsonObject.get("tag").getAsString();
                failure.setTag(tag);

                String actualName = jsonObject.get("actualName").getAsString();
                failure.setActualName(actualName);

                String actualVal = jsonObject.get("actualVal").getAsString();
                failure.setActualVal(actualVal);

                String expectedName = jsonObject.get("expectedName").getAsString();
                failure.setExpectedName(expectedName);

                String expectedVal = jsonObject.get("expectedVal").getAsString();
                failure.setExpectedVal(expectedVal);

                if (jsonObject.get("userCode") != null) {
                    String userCode = jsonObject.get("userCode").getAsString();
                    failure.setUserCode(userCode);
                }

                failure.setMessage(jsonObject.toString());
                if (!list.contains(failure))
                    list.add(failure);
            }
        }

        return list;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public void setActualName(String actualName) {
        this.actualName = actualName;
    }

    public void setExpectedName(String expectedName) {
        this.expectedName = expectedName;
    }

    public void setActualVal(String actualVal) {
        this.actualVal = actualVal;
    }

    public void setExpectedVal(String expectedVal) {
        this.expectedVal = expectedVal;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultTrace failure = (ResultTrace) o;

        return Objects.equals(message, failure.message);
    }
}
