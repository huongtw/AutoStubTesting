package com.dse.guifx_v3.helps;

import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.testcase_manager.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheHelper {
    /**
     * these 2 maps are used to render testcase on navigator tree, testcase execution tab when auto generate testcase
     */

    private static Map<ICommonFunctionNode, TestCasesTreeItem> functionToTreeItemMap = new HashMap<>();

    /**
     *  treeItemToListTestCasesMap is used to check if a generated testcase was add to navigator tree or not
     *  <TestCasesTreeItem, List<TestCase>> is added to this map each time a TestCasesTreeItem of a TestNormalSubprogramNode(of a function) is created
     *  a TestCase is added to List<TestCase> when execute a generated testcase.
     *  (Note: If the generating is done then this list can be clear)
     */
    private static Map<TestCasesTreeItem, List<TestCase>> treeItemToListTestCasesMap = new HashMap<>();

    public static Map<ICommonFunctionNode, TestCasesTreeItem> getFunctionToTreeItemMap() {
        return functionToTreeItemMap;
    }

    public static Map<TestCasesTreeItem, List<TestCase>> getTreeItemToListTestCasesMap() {
        return treeItemToListTestCasesMap;
    }
}
