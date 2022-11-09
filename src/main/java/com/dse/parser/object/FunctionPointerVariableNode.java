package com.dse.parser.object;

/**
 * Ex:
 * "list_sort(ListCompareFunc compare_func)",
 *
 * where compare_func is a function pointer:
 * "typedef int (*ListCompareFunc)(ListValue value1, ListValue value2);"
 */
public class FunctionPointerVariableNode extends VariableNode {
}
