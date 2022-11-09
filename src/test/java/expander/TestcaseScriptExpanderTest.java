//package expander;
//package com.dse.testcasescript;
//
//import com.dse.parser.object.FunctionNode;
//import com.dse.testcasescript.AbstractTestcaseScriptUpdater;
//import com.dse.testcasescript.TestcaseScriptExpander;
//import com.dse.testcasescript.object.ITestcaseNode;
//import com.dse.testdata.gen.module.TreeExpander;
//import com.dse.testdata.object.*;
//import org.apache.log4j.Logger;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class TestcaseScriptExpanderTest extends AbstractTestcaseScriptExpanderTest {
//    final static Logger logger = Logger.getLogger(TestcaseScriptExpanderTest.class);
//
//    /**
//     * Case: save numeric number
//     * <p>
//     * Input: void f1(int a){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test0() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "f1(int)");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof NormalNumberDataNode) {
//            final int value = 10000;
//            ((NormalNumberDataNode) n).setValue(value);
//
//            AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//            updater.setRootDataTree(rootDataTree);
//            updater.setRootTestScript(rootTestscript);
//            updater.setNameOfTestcase("xxx.001");
//            updater.updateOnTestcaseScript();
//
//            // check
//            logger.debug(rootTestscript.exportToFile());
//            Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:a=10000"));
//        }
//    }
//
//    /**
//     * Case: save a no-bound array
//     * Input: void f3(int a[]){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test1() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "f3(int[])");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof OneDimensionDataNode) {
//            // set size for the no-bound array
//            final int size = 100;
//            ((OneDimensionDataNode) n).setSize(size);
//
//            AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//            updater.setRootDataTree(rootDataTree);
//            updater.setRootTestScript(rootTestscript);
//            updater.setNameOfTestcase("xxx.001");
//            updater.updateOnTestcaseScript();
//
//            // check
//            logger.debug(rootTestscript.exportToFile());
//            Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:sizeof(a)=100"));
//        }
//    }
//
//    /**
//     * Case: assign an element of no-bound array
//     * <p>
//     * Input:
//     * void f3(int a[]){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test2() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "f3(int[])");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof OneDimensionDataNode) {
//            String nameVariable = n.getCorrespondingVar().getName();
//
//            // set size
//            final int size = 3;
//            ((OneDimensionDataNode) n).setSize(size);
//            TreeExpander expander = new TreeExpander();
//            expander.expandTree((DataNode) n);
//
//            // set value for a element
//            int elementIndex = 0;
//            IDataNode m = n.getChildren().get(elementIndex);
//            if (m instanceof NormalNumberDataNode) {
//                int value = 21;
//                ((NormalNumberDataNode) m).setValue(value);
//
//                AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//                updater.setRootDataTree(rootDataTree);
//                updater.setRootTestScript(rootTestscript);
//                updater.setNameOfTestcase("xxx.001");
//                updater.updateOnTestcaseScript();
//
//                logger.debug(rootTestscript.exportToFile());
//                Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:sizeof(a)=3"));
//                Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:a[0]=21"));
//
//            }
//        }
//    }
//
//    /**
//     * Case: bound array
//     * Input: void f4(int* a){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test3() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "f4(int*)");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof OneLevelDataNode) {
//            String name = n.getCorrespondingVar().getName();
//
//            // set size
//            final int size = 6;
//            ((OneLevelDataNode) n).setAllocatedSize(size);
//            TreeExpander expander = new TreeExpander();
//            expander.expandTree((DataNode) n);
//
//            // set value
//            int index = 0;
//            IDataNode m = n.getChildren().get(index);
//            if (m instanceof NormalNumberDataNode) {
//                int value = 21;
//                ((NormalNumberDataNode) m).setValue(value);
//
//                logger.debug(rootDataTree.generateInputToSavedInFile());
//
//                AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//                updater.setRootDataTree(rootDataTree);
//                updater.setRootTestScript(rootTestscript);
//                updater.setNameOfTestcase("xxx.001");
//                updater.updateOnTestcaseScript();
//
//                // check 1
//                logger.debug(rootTestscript.exportToFile());
//                Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:a[0]=21"));
//                Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:sizeof(a)=6"));
//            }
//        }
//    }
//
//    /**
//     * Test enum
//     * <p>
//     * Input:
//     * enum Color { RED, GREEN, BLUE };
//     * void SimpleTest(Color color){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test4() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "enum.cpp/SimpleTest(Color)");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof EnumDataNode) {
//            ((EnumDataNode) n).setValue("RED");
//            logger.debug(rootDataTree.generateInputToSavedInFile());
//
//            AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//            updater.setRootDataTree(rootDataTree);
//            updater.setRootTestScript(rootTestscript);
//            updater.setNameOfTestcase("xxx.001");
//            updater.updateOnTestcaseScript();
//
//            logger.debug(rootTestscript.exportToFile());
//
//            // check
//            Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:color=RED"));
//        }
//    }
//
//    /**
//     * Case: set value for numeric return value
//     * Input:
//     * int f1(){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test5() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "return.cpp/f1()");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof NormalNumberDataNode) {
//            final int value = 10000;
//            ((NormalNumberDataNode) n).setValue(value);
//            logger.debug(rootDataTree.generateInputToSavedInFile());
//
//            AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//            updater.setRootDataTree(rootDataTree);
//            updater.setRootTestScript(rootTestscript);
//            updater.setNameOfTestcase("xxx.001");
//            updater.updateOnTestcaseScript();
//
//            // check
//            logger.debug(rootTestscript.exportToFile());
//            Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:RETURN=10000"));
//        }
//    }
//
//    /**
//     * Case: Save return value of a pointer
//     * Input:
//     * int* f2(){}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test6() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "return.cpp/f2()");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof OneLevelDataNode) {
//
//            final int size = 5;
//            ((OneLevelDataNode) n).setAllocatedSize(size);
//            TreeExpander expander = new TreeExpander();
//            expander.expandTree((DataNode) n);
//
//            // set value
//            int index = 0;
//            IDataNode m = n.getChildren().get(index);
//            if (m instanceof NormalNumberDataNode) {
//                int value = 21;
//                ((NormalNumberDataNode) m).setValue(value);
//                logger.debug(rootDataTree.generateInputToSavedInFile());
//
//                AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//                updater.setRootDataTree(rootDataTree);
//                updater.setRootTestScript(rootTestscript);
//                updater.setNameOfTestcase("xxx.001");
//                updater.updateOnTestcaseScript();
//                // check
//                logger.debug(rootTestscript.exportToFile());
//                Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:RETURN[0]=21"));
//                Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:sizeof(RETURN)=5"));
//
//
//            }
//        }
//    }
//
//    /**
//     * Return value of a function is an enum
//     * Input:
//     * enum Color { RED, GREEN, BLUE};
//     * Color f3(){return null;}
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test7() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "return.cpp/f3()");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof EnumDataNode) {
//            String name = n.getCorrespondingVar().getName();
//
//            // set value
//            String enumValue = ((EnumDataNode) n).getAllNameEnumItems().get(0);
//            ((EnumDataNode) n).setValue(enumValue);
//            TreeExpander expander = new TreeExpander();
//            expander.expandTree((DataNode) n);
//            logger.debug(rootDataTree.generateInputToSavedInFile());
//
//
//            AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//            updater.setRootDataTree(rootDataTree);
//            updater.setRootTestScript(rootTestscript);
//            updater.setNameOfTestcase("xxx.001");
//            updater.updateOnTestcaseScript();
//
//            logger.debug(rootTestscript.exportToFile());
//
//            // check
//            Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:RETURN=RED"));
//        }
//    }
//
//    /**
//     * Assign sub-class
//     *
//     * @throws Exception
//     */
//    @Test
//    public void test8() throws Exception {
//        FunctionNode function = findAFunction("datatest/duc-anh/TestcaseScriptExpander", "class.cpp/f1(Polygon)");
//
//        ITestcaseNode rootTestscript = getTestScriptTree("datatest/duc-anh/TestcaseScriptExpander/testscript.tst");
//        RootDataNode rootDataTree = createDataTree(function);
//
//        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
//        if (n instanceof ClassDataNode) {
//            String name = n.getCorrespondingVar().getName();
//
//            // set value
//            ((ClassDataNode) n).setSubClass("Triangle");
//            TreeExpander expander = new TreeExpander();
//            expander.expandTree((DataNode) n);
//            logger.debug(rootDataTree.generateInputToSavedInFile());
//
//
//            AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
//            updater.setRootDataTree(rootDataTree);
//            updater.setRootTestScript(rootTestscript);
//            updater.setNameOfTestcase("xxx.001");
//            updater.updateOnTestcaseScript();
//
//            logger.debug(rootTestscript.exportToFile());
//
//            // check
//            Assert.assertEquals(true, rootTestscript.exportToFile().contains("TEST.VALUE:p=::Triangle")
//                    || rootTestscript.exportToFile().contains("TEST.VALUE:p=Triangle"));
//        }
//    }
//}