package com.dse.testdata.comparable;

import com.dse.testdata.object.ValueDataNode;

public class NullableStatementGenerator extends StatementGenerator implements INullableComparable {

    public NullableStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String assertNull(String name) {
        StringBuilder assertion = new StringBuilder();

        String expectedName = getVirtualName();

//        if (Environment.getInstance().isC()) {
//            String cuAssertStm = String.format("%s(%s);", AssertMethod.CU.ASSERT_NULL, name);
//            assertion.append(cuAssertStm);
//
//        } else {
//            String gTestAssertStm = String.format("%s(%s)", AssertMethod.GTest.EXPECT_NULL, name);
//            assertion.append(gTestAssertStm);
//            assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//        }

        String resultExportStm = getExportExePtrResultStm(name, expectedName, "NULL");
        assertion.append(resultExportStm);

        return assertion.toString();
    }

    @Override
    public String assertNotNull(String name) {
        StringBuilder assertion = new StringBuilder();

        String expectedName = getVirtualName();

//        if (Environment.getInstance().isC()) {
//            String cuAssertStm = String.format("%s(%s);", AssertMethod.CU.ASSERT_NOT_NULL, name);
//            assertion.append(cuAssertStm);
//
//        } else {
//            String gTestAssertStm = String.format("%s(%s)", AssertMethod.GTest.EXPECT_NOT_NULL, name);
//            assertion.append(gTestAssertStm);
//            assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//        }

        String resultExportStm = getExportExePtrResultStm(name, expectedName,"1");
        assertion.append(resultExportStm);

        return assertion.toString();
    }
}
