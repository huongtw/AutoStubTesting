package com.dse.util;

/**
 * Bieu dien cac node trong Function Detail Tree hoac Test Data Tree
 * Ex: GLOBAL, UUT, STUB, ...
 *
 * @author TungLam
 */
public enum NodeType {
    ROOT,
    GLOBAL,
    UUT,
    STUB,
    DONT_STUB,
    SBF,
    STATIC
}