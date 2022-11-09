package com.dse.parser;

import com.dse.parser.object.INode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;

/**
 * @author ducanh
 */
public interface ISourcecodeFileParser {

    int IS_FUNCTION_AS_VARIABLE_DECLARATION = 8;

    int IS_UNSPECIFIED_DECLARATION = 9;

    int IS_FUNCTION_DECLARATION = 10;

    int IS_STRUCT_DECLARATION = 12;

    int IS_CLASS_DECLARATION = 13;

    int IS_VARIABLE_DECLARATION = 14;

    int IS_PRIMITIVE_TYPEDEF_DECLARATION = 16;

    int IS_TEMPLATE_DECLARATION = 17;

    int IS_PRIVATE_LABEL = 18;

    int IS_PUBLIC_LABEL = 19;

    int IS_PROTECTED_LABEL = 20;

    int IS_CONSTRUCTOR_DECLARATION = 21;

    int IS_DESTRUCTOR_DECLARATION = 22;

    int IS_ENUM = 23;

    int IS_ENUM_TYPEDEF_DECLARATION = 24;

    int IS_UNION = 25;

    int IS_UNION_TYPEDEF_DECLARATION = 26;

    int IS_STRUCT_TYPEDEF_DECLARATION = 27;

    int IS_ALIAS_DECLARATION = 28;

    int IS_LINKAGE_SPECIFICATION = 29; // e.g., extern "C" {...}

    int IS_FUNCTION_POINTER_TYPEDEF_DECLARATION = 30;

    int IS_EMPTY_STRUCT_DECLARATION = 31;

    int IS_EMPTY_ENUM_DECLARATION = 32;

    int IS_EMPTY_UNION_DECLARATION = 33;

    String METHOD_SIGNAL = "(";

    String FUNCTION_BODY_SIGNAL = "{";

    String UNION_SYMBOL = "union";

    String ENUM_SYMBOL = "enum";

    String STRUCT_SYMBOL = "struct";

    String CLASS_SYMBOL = "class";

    String TYPEDEF_SYMBOL = "typedef";

    File getSourcecodeFile();

    void setSourcecodeFile(File sourcecodeFile);

    INode generateTree(boolean isExpandedToMethodLevelState) throws Exception;

    INode generateTree() throws Exception;

    INode parseSourcecodeFile(File filePath) throws Exception;

    IASTTranslationUnit getIASTTranslationUnit(char[] code) throws Exception;
}
