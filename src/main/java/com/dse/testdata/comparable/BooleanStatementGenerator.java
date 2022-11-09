package com.dse.testdata.comparable;

import com.dse.testdata.object.ValueDataNode;

public class BooleanStatementGenerator extends StatementGenerator implements IBooleanComparable {

    public BooleanStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String assertTrue(String name) {
        StringBuilder assertion = new StringBuilder();

        String expectedName = getVirtualName();
//        if (Environment.getInstance().isC()) {
//            String cuAssertStm = String.format("%s(%s);", AssertMethod.CU.ASSERT_TRUE, name);
//            assertion.append(cuAssertStm);
//
//        } else {
//            String gTestAssertStm = String.format("%s(%s)", AssertMethod.GTest.EXPECT_TRUE, name);
//            assertion.append(gTestAssertStm);
//
//            assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//        }

        String resultExportStm = getExportExeResultStm(name, expectedName,"false");
        assertion.append(resultExportStm);

        return assertion.toString();
    }

    @Override
    public String assertFalse(String name) {
        StringBuilder assertion = new StringBuilder();

        String expectedName = getVirtualName();

//        if (Environment.getInstance().isC()) {
//            String cuAssertStm = String.format("%s(%s);", AssertMethod.CU.ASSERT_FALSE, name);
//            assertion.append(cuAssertStm);
//
//        } else {
//            String gTestAssertStm = String.format("%s(%s)", AssertMethod.GTest.EXPECT_FALSE, name);
//            assertion.append(gTestAssertStm);
//            assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//        }

        String resultExportStm = getExportExeResultStm(name, expectedName,"false");
        assertion.append(resultExportStm);

        return assertion.toString();
    }
}
