package com.dse.compiler.message.error_tree;

import com.dse.compiler.AvailableCompiler;
import com.dse.compiler.Compiler;
import com.dse.compiler.message.ICompileMessage;
import com.dse.compiler.message.error_tree.node.*;
import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.resolver.*;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.ClassvsStructvsNamespaceCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompileMessageParser {

    private final String message;

    private final String filePath;

    private ISourcecodeFileNode sourceNode;

    private List<INode> functionNodes;

    private RootErrorNode rootNode;

    private ScopeErrorNode currentScope;

    private final String FUNCTION_SCOPE_TAG;
    private final String MEMBER_FUNCTION_SCOPE_TAG;
    private final String CONSTRUCTOR_SCOPE_TAG;
    private final String DESTRUCTOR_SCOPE_TAG;
    private final String GLOBAL_SCOPE_TAG;

//    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
//        ProjectParser projectParser = new ProjectParser(new File("/home/lamnt/Desktop/"));
//
//        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
//        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
//        projectParser.setExtendedDependencyGeneration_enabled(true);
//        projectParser.setParentReconstructor_enabled(true);
//
//        ProjectNode projectRoot = projectParser.getRootTree();
//        Environment.getInstance().setProjectNode(projectRoot);
//
//        Compiler compiler = new Compiler(AvailableCompiler.CPP_11_GNU_NATIVE.class);
//        Environment.getInstance().setCompiler(compiler);
//
//        INode function = Search.searchNodes(projectRoot, new AbstractFunctionNodeCondition()).get(0);
//
//        if (function instanceof IFunctionNode) {
//            IASTFunctionDefinition ast = ((IFunctionNode) function).getAST();
//
//            IASTStatement statement = ((IASTCompoundStatement) ast.getBody()).getStatements()[1];
//
//            IASTBinaryExpression expression = (IASTBinaryExpression) ((IASTExpressionStatement) statement).getExpression();
//
//            String type = new NewTypeResolver(function, 0).solve(expression.getOperand1());
//        }
//
//
//        ICompileMessage compileMessage = compiler.compile("/mnt/e/akautauto/datatest/duc-anh/Algorithm/main.cpp");
//
//        CompileMessageParser parser = new CompileMessageParser(compileMessage);
//
//        RootErrorNode rootErrorNode = parser.parse();
//
//        IUndeclaredTypeErrorNode undeclaredErrors = (IUndeclaredTypeErrorNode) CompileErrorSearch
//                .searchNodes(rootErrorNode, IUndeclaredTypeErrorNode.class).get(0);
//
//        IUndeclaredResolver resolver = new UndeclaredTypeResolver(undeclaredErrors);
//        resolver.resolve();
//        List<ResolvedSolution> functionNode = resolver.getSolutions();
//
//        System.out.println();
//    }

    public CompileMessageParser(ICompileMessage message) {
        this.filePath = message.getFilePath();
        this.message = message.getMessage();

        FUNCTION_SCOPE_TAG = filePath + ": In function ";
        MEMBER_FUNCTION_SCOPE_TAG = filePath + ": In member function ";
        CONSTRUCTOR_SCOPE_TAG = filePath + ": In constructor ";
        DESTRUCTOR_SCOPE_TAG = filePath + ": In constructor ";
        GLOBAL_SCOPE_TAG = filePath + ": At global scope:";

        findSourceCodeFileNode();
    }

    public CompileMessageParser(String message, String filePath) {
        this.filePath = filePath;
        this.message = message;

        FUNCTION_SCOPE_TAG = filePath + ": In function ";
        MEMBER_FUNCTION_SCOPE_TAG = filePath + ": In member function ";
        CONSTRUCTOR_SCOPE_TAG = filePath + ": In constructor ";
        DESTRUCTOR_SCOPE_TAG = filePath + ": In constructor ";
        GLOBAL_SCOPE_TAG = filePath + ": At global scope:";

        findSourceCodeFileNode();
    }

    private void findSourceCodeFileNode() {
        List<INode> sourceFiles = Search
                .searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition());

        String absolutePath = PathUtils.toAbsolute(filePath);
        sourceNode = (ISourcecodeFileNode) sourceFiles
                .stream()
                .filter(f -> f.getAbsolutePath().endsWith(absolutePath))
                .findFirst()
                .orElse(null);
    }

    public RootErrorNode parse() {
        String[] lines = message.split("\\R");

        rootNode = new RootErrorNode();
        rootNode.setMessage(message);
        rootNode.setSource(sourceNode);

        int index = 0;

        while (index < lines.length) {
            LineTag lineTag = getLineTag(lines[index]);

            switch (lineTag) {
                case GLOBAL_SCOPE: {
                    GlobalScopeErrorNode node = new GlobalScopeErrorNode();
                    node.setScope(sourceNode);

                    node.setParent(rootNode);

                    currentScope = node;
                    index++;

                    break;
                }

                case FUNCTION_SCOPE: {
                    INode functionNode = findFunctionNode(lines[index], lines[index + 1]);

                    FunctionScopeErrorNode node = new FunctionScopeErrorNode();
                    node.setScope(functionNode);

                    node.setParent(rootNode);

                    currentScope = node;
                    index++;

                    break;
                }

                case UNDECLARED_LOCAL_MEMBER_ERROR: {
                    index = generateUndeclaredLocalErrorNode(lines, index);
                    break;
                }

                case UNDECLARED_NAMESPACE_MEMBER_ERROR: {
                    index = generateUndeclaredNamespaceMemberErrorNode(lines, index);
                    break;
                }

                case UNDECLARED_STRUCTURE_MEMBER_ERROR: {
                    index = generateUndeclaredStructureMemberErrorNode(lines, index);
                    break;
                }

                case UNKNOWN_ERROR: {
                    index = generateUnknownErrorNode(lines, index);
                    break;
                }
            }
        }

        return rootNode;
    }

    private INode getCurrentScope() {
        IErrorNode parent = currentScope == null ? rootNode : currentScope;

        INode scope = sourceNode;

        if (parent instanceof ScopeErrorNode) {
            scope = ((ScopeErrorNode) parent).getScope();

            if (scope == null)
                scope = sourceNode;
        }

        return scope;
    }

    private int generateUndeclaredLocalErrorNode(String[] lines, int index) {
        IErrorNode parent = currentScope == null ? rootNode : currentScope;

        String undeclaredMessage = lines[index] + "\n" + lines[index + 1] + "\n" + lines[index + 2];

        try {
            String name = getName(lines[index])[0];
            Integer[] location = getLocation(lines[index]);

            INode scope = getCurrentScope();

            SymbolASTNameFinder finder = new SymbolASTNameFinder(scope, name, location);

            Undeclared type = finder.getType();

            UndeclaredLocalMemberErrorNode node = null;

            switch (type) {
                case VARIABLE: {
                    node = new UndeclaredLocalVariableErrorNode();
                    break;
                }

                case TYPE: {
                    node = new UndeclaredLocalTypeErrorNode();
                    break;
                }

                case FUNCTION: {
                    node = new UndeclaredLocalFunctionErrorNode();
                    break;
                }

                case UNKNOWN: {
                    return generateUnknownErrorNode(lines, index);
                }
            }

            node.setName(name);
            node.setLine(location[0]);
            node.setOffset(location[1]);
            node.setMessage(undeclaredMessage);
            node.setCall(finder.getResult());
            node.setLocation(sourceNode);

            node.setParent(parent);

        } catch (ArrayIndexOutOfBoundsException ignored) {

        }

        return index + 3;
    }

    private int generateUndeclaredNamespaceMemberErrorNode(String[] lines, int index) {
        IErrorNode parent = currentScope == null ? rootNode : currentScope;

        String undeclaredMessage = lines[index] + "\n" + lines[index + 1] + "\n" + lines[index + 2];

        String[] tempNames = getName(lines[index]);

        try {
            String name = tempNames[0];
            String prefix = tempNames[1];

            NamespaceNode namespace = (NamespaceNode) findNamespaceOrStructure(prefix);

            Integer[] location = getLocation(lines[index]);

            INode scope = getCurrentScope();

            SymbolASTNameFinder finder = new SymbolASTNameFinder(scope, name, location);

            Undeclared type = finder.getType();

            UndeclaredNamespaceMemberErrorNode node = null;

            switch (type) {
                case VARIABLE: {
                    node = new UndeclaredNamespaceVariableErrorNode();
                    break;
                }

                case TYPE: {
                    node = new UndeclaredNamespaceTypeErrorNode();
                    break;
                }

                case FUNCTION: {
                    node = new UndeclaredNamespaceFunctionErrorNode();
                    break;
                }

                case UNKNOWN: {
                    return generateUnknownErrorNode(lines, index);
                }
            }

            node.setName(name);
            node.setLine(location[0]);
            node.setOffset(location[1]);
            node.setMessage(undeclaredMessage);
            node.setCall(finder.getResult());

            node.setLocation(namespace);

            node.setParent(parent);
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }

        return index + 3;
    }

    private int generateUndeclaredStructureMemberErrorNode(String[] lines, int index) {
        IErrorNode parent = currentScope == null ? rootNode : currentScope;

        String undeclaredMessage = lines[index] + "\n" + lines[index + 1] + "\n" + lines[index + 2];

        String[] tempNames = getName(lines[index]);

        try {
            String name = tempNames[1];
            String prefix = tempNames[0];

            prefix = VariableTypeUtils.deleteStructKeyword(prefix);
            prefix = VariableTypeUtils.deleteUnionKeyword(prefix);
            prefix = VariableTypeUtils.deleteClassKeyword(prefix);

            StructureNode structure = (StructureNode) findNamespaceOrStructure(prefix);

            Integer[] location = getLocation(lines[index]);

            INode scope = getCurrentScope();

            SymbolASTNameFinder finder = new SymbolASTNameFinder(scope, name, location);

            Undeclared type = finder.getType();

            UndeclaredStructureMemberErrorNode node = null;

            switch (type) {
                case VARIABLE: {
                    node = new UndeclaredStructureVariableErrorNode();
                    break;
                }

                case TYPE: {
                    node = new UndeclaredStructureTypeErrorNode();
                    break;
                }

                case FUNCTION: {
                    node = new UndeclaredStructureFunctionErrorNode();
                    break;
                }

                case UNKNOWN: {
                    return generateUnknownErrorNode(lines, index);
                }
            }

            node.setName(name);
            node.setLine(location[0]);
            node.setOffset(location[1]);
            node.setMessage(undeclaredMessage);
            node.setCall(finder.getResult());

            node.setLocation(structure);

            node.setParent(parent);
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }

        return index + 3;
    }

    private int generateUnknownErrorNode(String[] lines, int index) {
        IErrorNode parent = currentScope == null ? rootNode : currentScope;

        String errorMessage = SpecialCharacter.EMPTY;

        do {
            errorMessage += lines[index] + SpecialCharacter.LINE_BREAK;
            index++;
        } while (index < lines.length
                && !lines[index].startsWith(filePath));

        errorMessage = errorMessage.substring(0, errorMessage.length() - 1);

        UnknownErrorNode node = new UnknownErrorNode();
        node.setMessage(errorMessage);

        node.setParent(parent);

        return index;
    }

    private Integer[] getLocation(String line) {
//        String normalize = line.substring(compileMessage.getFilePath().length() + 1);
        int startIdx = line.indexOf(":");
        String normalize = line.substring(startIdx + 1);
        int endPos = normalize.indexOf(": error:");

        if (endPos > 0) {
            normalize = normalize.substring(0, endPos);

            String[] pos = normalize.split(":");

            try {
                return Arrays.stream(pos)
                        .map(Integer::parseInt)
                        .toArray(Integer[]::new);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return new Integer[] {0, 0};
    }

    private String[] getName(String line) {
        List<String> names = new ArrayList<>();

        String current = line;

        while (true) {
            int startIdx = current.indexOf(OPEN_NAME_QUOTE) + 1;
            int endIdx = current.indexOf(CLOSE_NAME_QUOTE, startIdx);

            if (startIdx > 0 && endIdx >= startIdx) {
                String name = current.substring(startIdx, endIdx);
                names.add(name);
                current = current.substring(endIdx + 1);
            } else {
                break;
            }
        }

        return names.toArray(new String[0]);
    }

    private boolean isSameFunction(final String functionName, IFunctionNode functionNode) {
        String returnType = SpecialCharacter.EMPTY;

        if (functionNode instanceof FunctionNode) {
            returnType = functionNode.getReturnType();
        }

        String normalize = functionName;
        if (functionName.startsWith(returnType)) {
            normalize = normalize
                    .substring(returnType.length())
                    .replaceAll("\\s*", SpecialCharacter.EMPTY);

            String functionNodeName = Search.getScopeQualifier(functionNode);

            return normalize.equals(functionNodeName);
        }

        return false;
    }

    private boolean isInFunctionScope(int line, IFunctionNode functionNode) {
        IASTFileLocation location = functionNode.getAST().getFileLocation();

        int startLineIdx = location.getStartingLineNumber();

        int endLineIdx = location.getEndingLineNumber();

        return line >= startLineIdx && line <= endLineIdx;
    }

    private INode findFunctionNode(String line, String nextLine) {
        if (getName(line).length == 0)
            return null;

        String functionName = getName(line)[0];

        if (functionNodes == null) {
            try {
                INode root = new SourcecodeFileParser().parseSourcecodeFile(sourceNode.getFile());
                functionNodes = Search.searchNodes(root, new AbstractFunctionNodeCondition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        INode result = functionNodes
                .stream()
                .filter(f -> isSameFunction(functionName, (IFunctionNode) f))
                .findFirst()
                .orElse(null);

        if (result == null) {
            Integer[] location = getLocation(nextLine);
            int lineIdx = location[0];

            result = functionNodes
                    .stream()
                    .filter(f -> isInFunctionScope(lineIdx, (IFunctionNode) f))
                    .findFirst()
                    .orElse(null);
        }

        return result;
    }

    private INode findNamespaceOrStructure(String name) {
        String relativePath = name.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

        VariableSearchingSpace searchingSpace = new VariableSearchingSpace(sourceNode);

        List<Level> space = searchingSpace.generateExtendSpaces();

        List<INode> nodes = Search.searchInSpace(space, new ClassvsStructvsNamespaceCondition(), relativePath);

        return nodes.stream().findFirst().orElse(null);
    }

    private LineTag getLineTag(String line) {
        if (line.startsWith(FUNCTION_SCOPE_TAG)
                || line.startsWith(MEMBER_FUNCTION_SCOPE_TAG)
                || line.startsWith(CONSTRUCTOR_SCOPE_TAG)
                || line.startsWith(DESTRUCTOR_SCOPE_TAG))
            return LineTag.FUNCTION_SCOPE;
        else if (line.startsWith(GLOBAL_SCOPE_TAG))
            return LineTag.GLOBAL_SCOPE;
        else if (line.contains(LOCAL_SCOPE_TAG) || line.contains(C_GLOBAL_SCOPE_TAG)
                || line.contains(C_FUNCTION_SCOPE_TAG) || line.contains(C_LOCAL_SCOPE_TAG))
            return LineTag.UNDECLARED_LOCAL_MEMBER_ERROR;
        else if (line.contains(NAMESPACE_SCOPE_TAG))
            return LineTag.UNDECLARED_NAMESPACE_MEMBER_ERROR;
        else if (line.contains(STRUCTURE_SCOPE_TAG))
            return LineTag.UNDECLARED_STRUCTURE_MEMBER_ERROR;
        else
            return LineTag.UNKNOWN_ERROR;
    }

    private static final int OPEN_NAME_QUOTE = Utils.isWindows() ? 8216 : 39;
    private static final int CLOSE_NAME_QUOTE = Utils.isWindows() ? 8217 : 39;

    private static final String LOCAL_SCOPE_TAG = "was not declared in this scope";
    private static final String C_LOCAL_SCOPE_TAG = "warning: implicit declaration of function ";
    private static final String NAMESPACE_SCOPE_TAG = "is not a member of";
    private static final String STRUCTURE_SCOPE_TAG = "has no member named";
    private static final String C_FUNCTION_SCOPE_TAG = "undeclared (first use in this function)";
    private static final String C_GLOBAL_SCOPE_TAG = "undeclared here (not in a function)";

    public enum Undeclared {
        FUNCTION,
        VARIABLE,
        TYPE,
        UNKNOWN
    }

    private enum LineTag {
        FUNCTION_SCOPE,
        GLOBAL_SCOPE,
        UNDECLARED_LOCAL_MEMBER_ERROR,
        UNDECLARED_NAMESPACE_MEMBER_ERROR,
        UNDECLARED_STRUCTURE_MEMBER_ERROR,
        UNKNOWN_ERROR
    }
}
