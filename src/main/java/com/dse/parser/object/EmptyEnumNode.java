package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEnumerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent enum declaration
 * <p>
 * <p>
 * 
 * <pre>
 * |    enum Color;
 * </pre>
 *
 * @author ducanhnguyen
 */
public class EmptyEnumNode extends EnumNode implements IEmptyStructureNode {
}
