package com.dse.util;

import com.dse.testcase_execution.DriverConstant;

public interface SourceConstant {

    String EXPECTED_OUTPUT = "AKA_EXPECTED_OUTPUT";

    String ACTUAL_OUTPUT = "AKA_ACTUAL_OUTPUT";

    String INSTANCE_VARIABLE = "AKA_INSTANCE";

    String INSTANCE_VARIABLE_POINTER = "AKA_INSTANCE_PTR";

    String MARK_STM = DriverConstant.MARK;

    String INCREASE_FCALLS = DriverConstant.CALL_COUNTER + "++;";

    String STUB_PREFIX = "AKA_STUB_";

    String SRC_PREFIX = "AKA_SRC_";

    String EXPECTED_PREFIX = "EXPECTED_";

    String INCLUDE_PREFIX = "AKA_INCLUDE_";

    String GLOBAL_PREFIX = "AKA_GLOBAL_";

    String TEST = "TEST";

    String EXPECT_EQ = "EXPECT_EQ";

    // Assert integer value
    String ASSERT_EQ = "EXPECT_EQ";

    // Assert decimal value
    String ASSERT_NEAR = "ASSERT_NEAR";

    String PASSED_FLAG = "[  PASSED  ]";

    String FAILED_FLAG = "[  FAILED  ]";

//    String LOG_FUNCTION_CALLS = " << \"Aka function calls: \" << " + DriverConstant.CALL_COUNTER + " << \"\\n\";";
//
//    // unix, macosx
//    // -w: disable warning
//    String COMPILE_FLAG_FOR_GOOGLETEST = " -lgtest_main  -lgtest -lpthread -lstdc++ -w";
//    String COMPILE_FLAG_FOR_CUNIT = " -lcunit -w";
//
//    // windows
//    // -w: disable warning
//    String COMPILE_FLAG_WINDOWS_FOR_GOOGLETEST = " -lgtest_main  -lgtest -w";
//    String COMPILE_FLAG_WINDOWS_FOR_CUNIT = " -lcunit -w";
//
//    static String getGTestCommand(String origin, boolean useGTest) {
//        if (Utils.isWindows()){
////            if (useGTest) {
////                // include gtest library
////                if (!origin.contains(COMPILE_FLAG_WINDOWS_FOR_GOOGLETEST))
////                    return origin + COMPILE_FLAG_WINDOWS_FOR_GOOGLETEST;
////                else
////                    return origin;
////            } else {
////                // the new compilation is modified from a version which contains google test flag
////                return origin.replace(COMPILE_FLAG_WINDOWS_FOR_GOOGLETEST, SpecialCharacter.EMPTY);
////            }
//
//            if (Environment.getInstance().getCompiler().isGPlusPlusCommand()) {
//                if (!origin.contains(COMPILE_FLAG_WINDOWS_FOR_GOOGLETEST))
//                    return origin + COMPILE_FLAG_WINDOWS_FOR_GOOGLETEST;
//                else
//                    return origin;
//            } else if (Environment.getInstance().getCompiler().isGccCommand()){
//                if (!origin.contains(COMPILE_FLAG_FOR_CUNIT))
//                    return origin + COMPILE_FLAG_FOR_CUNIT;
//                else
//                    return origin;
//            }
//        } else {
////            if (useGTest) {
//                // include gtest library
//            if (Environment.getInstance().getCompiler().isGPlusPlusCommand()) {
//                if (!origin.contains(COMPILE_FLAG_FOR_GOOGLETEST))
//                    return origin + COMPILE_FLAG_FOR_GOOGLETEST;
//                else
//                    return origin;
//            } else if (Environment.getInstance().getCompiler().isGccCommand()){
//                if (!origin.contains(COMPILE_FLAG_FOR_CUNIT))
//                    return origin + COMPILE_FLAG_FOR_CUNIT;
//                else
//                    return origin;
//            }
////            } else {
////                // the new compilation is modified from a version which contains google test flag
////                return origin.replace(COMPILE_FLAG, SpecialCharacter.EMPTY);
////            }
//        }
//        return ""; // cause error
//    }
}
