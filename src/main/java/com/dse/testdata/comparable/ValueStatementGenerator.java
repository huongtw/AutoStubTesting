package com.dse.testdata.comparable;

import com.dse.testdata.object.ValueDataNode;
import com.dse.util.VariableTypeUtils;

public class ValueStatementGenerator extends EqualityStatementGenerator implements IValueComparable {

    public ValueStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String assertLower(String source, String target) {
        StringBuilder assertion = new StringBuilder();

        if (haveValue()) {
            String expectedName = getVirtualName();
            String actualName = expectedName.replace(source, target);
//            if (Environment.getInstance().isC()) {
//                String cuAssertStm = String.format("%s(%s, %s);", AssertMethod.CU.ASSERT_LOWER, actualName, expectedName);
//                assertion.append(cuAssertStm);
//
//            } else {
//                String gTestAssertStm = String.format("%s(%s, %s)", AssertMethod.GTest.EXPECT_LT, actualName, expectedName);
//                assertion.append(gTestAssertStm);
//
//                assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//            }

            String resultExportStm = getExportExeResultStmByType(getRealType(), actualName, expectedName);
            assertion.append(resultExportStm);
        }

        return assertion.toString();
    }

    @Override
    public String assertGreater(String source, String target) {
        StringBuilder assertion = new StringBuilder();

        if (haveValue()) {
            String expectedName = getVirtualName();
            String actualName = expectedName.replace(source, target);
//            if (Environment.getInstance().isC()) {
//                String cuAssertStm = String.format("%s(%s, %s);", AssertMethod.CU.ASSERT_GREATER, actualName, expectedName);
//                assertion.append(cuAssertStm);
//
//            } else {
//                String gTestAssertStm = String.format("%s(%s, %s)", AssertMethod.GTest.EXPECT_GT, actualName, expectedName);
//                assertion.append(gTestAssertStm);
//
//                assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//            }

            String resultExportStm = getExportExeResultStmByType(getRealType(), actualName, expectedName);
            assertion.append(resultExportStm);
        }

        return assertion.toString();
    }

    @Override
    public String assertLowerOrEqual(String source, String target) {
        StringBuilder assertion = new StringBuilder();

        if (haveValue()) {
            String expectedName = getVirtualName();
            String actualName = expectedName.replace(source, target);
//            if (Environment.getInstance().isC()) {
//                String cuAssertStm = String.format("%s(%s, %s);", AssertMethod.CU.ASSERT_LOWER_OR_EQUAL, actualName, expectedName);
//                assertion.append(cuAssertStm);
//
//            } else {
//                String gTestAssertStm = String.format("%s(%s, %s)", AssertMethod.GTest.EXPECT_LE, actualName, expectedName);
//                assertion.append(gTestAssertStm);
//
//                assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//            }

            String resultExportStm = getExportExeResultStmByType(getRealType(), actualName, expectedName);
            assertion.append(resultExportStm);
        }

        return assertion.toString();
    }


    @Override
    public String assertGreaterOrEqual(String source, String target) {
        StringBuilder assertion = new StringBuilder();

        if (haveValue()) {
            String expectedName = getVirtualName();
            String actualName = expectedName.replace(source, target);
//            if (Environment.getInstance().isC()) {
//                String cuAssertStm = String.format("%s(%s, %s);", AssertMethod.CU.ASSERT_GREATER_OR_EQUAL, actualName, expectedName);
//                assertion.append(cuAssertStm);
//
//            } else {
//                String gTestAssertStm = String.format("%s(%s, %s)", AssertMethod.GTest.EXPECT_GE, actualName, expectedName);
//                assertion.append(gTestAssertStm);
//
//                assertion.append(IGTestConstant.LOG_FUNCTION_CALLS);
//
//            }

            String resultExportStm = getExportExeResultStmByType(getRealType(), actualName, expectedName);
            assertion.append(resultExportStm);
        }

        return assertion.toString();
    }

    public static String getExportExeResultStmByType(String realType, String actualName, String expectedName) {
        String statement;

        if (VariableTypeUtils.isNumBasicFloat(realType))
            statement = getExportExeDoubleResultStm(actualName, expectedName, expectedName);
        else if (VariableTypeUtils.isChOneLevel(realType)
                || VariableTypeUtils.isChOneDimension(realType))
            statement = getExportExeResultStm(actualName, expectedName, expectedName);
        else if (VariableTypeUtils.isOneDimension(realType)
                || VariableTypeUtils.isMultipleDimension(realType)
                || VariableTypeUtils.isPointer(realType))
            statement = getExportExePtrResultStm(actualName, expectedName, expectedName);
        else
            statement = getExportExeResultStm(actualName, expectedName, expectedName);

        return statement;
    }
}
