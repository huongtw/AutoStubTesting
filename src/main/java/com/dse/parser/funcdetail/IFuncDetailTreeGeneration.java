package com.dse.parser.funcdetail;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.RootNode;

/**
 * Xay dung cay Function Detail Tree co. Ex:
 * |
 * + GLOBAL
 * |--- g_var
 * |--- g_student
 * |
 * + UUT
 * |--- testSimple(Student stu)
 * |
 * + STUB
 * |--- testStudent(Student, int x)
 * |--- testStubFunc(int x)
 * |
 * + DONT STUB
 * |--- anotherTest(Student stu)
 *
 * @author TungLam
 */
public interface IFuncDetailTreeGeneration {
    /**
     * Sinh ra cay cau truc tuong trung cho ham chuan bi sinh test data.
     *
     * @param root
     * @param fn
     */
    void generateTree(RootNode root, ICommonFunctionNode fn);

    /**
     * Sinh ra nhanh (sub tree) global variables
     * @param root
     * @param fn
     */
    void generateGlobalSubTree(RootNode root, ICommonFunctionNode fn);

    /**
     * Sinh ra nhanh (sub tree) unit under test
     * @param root
     * @param fn
     */
    void generateUUTSubTree(RootNode root, ICommonFunctionNode fn);

    /**
     * Sinh ra nhanh (sub tree) stub & dont stub subprograms
     * @param root
     * @param fn
     */
    void generateStubSubTree(RootNode root, ICommonFunctionNode fn);
}
