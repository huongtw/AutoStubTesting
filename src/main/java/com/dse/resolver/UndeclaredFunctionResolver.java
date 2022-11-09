package com.dse.resolver;

import com.dse.compiler.message.error_tree.node.IUndeclaredFunctionErrorNode;
import com.dse.compiler.message.error_tree.node.UndeclaredErrorNode;
import com.dse.environment.Environment;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UndeclaredFunctionResolver extends UndeclaredResolver<IASTFunctionCallExpression> {

    public UndeclaredFunctionResolver(IUndeclaredFunctionErrorNode errorNode) {
        this.errorNode = (UndeclaredErrorNode<? extends INode>) errorNode;

        findFunctionCallExpression();

        findScope();
    }

    @Override
    public void resolve() {
        List<FunctionNode> existFunctions = findExistFunction();

        checkStandardLibrary();

        for (FunctionNode functionNode : existFunctions) {
            INode sourceNode = Utils.getSourcecodeFile(scope);
            String source = functionNode.getAST().getRawSignature();
            int beginIdx = source.indexOf("{");
            source = source.substring(0, beginIdx);
            source += SpecialCharacter.END_OF_STATEMENT;

            int offset = 0;

            if (scope instanceof FunctionNode) {
                INode current = scope;

                while (!(current.getParent() instanceof ISourcecodeFileNode)) {
                    current = current.getParent();

                    if (current == null)
                        break;
                }

                if (current instanceof CustomASTNode) {
                    offset = ((CustomASTNode<?>) current).getAST().getFileLocation().getNodeOffset();
                }

            } else {
                IASTNode statement = callExpression;

                while (statement != null) {
                    if (statement instanceof IASTStatement)
                        break;
                    statement = statement.getParent();
                }

                assert statement != null;
                offset = statement.getFileLocation().getNodeOffset();
            }

            appendSolution(source, sourceNode, offset);
        }

        ResolvedFunctionNode functionNode = generateFunction();
        String source = functionNode.getAST().getRawSignature();
//                .replace(SpecialCharacter.END_OF_STATEMENT, SpecialCharacter.EMPTY);
//        source = String.format("%s {}", source);
        INode sourceNode = Utils.getSourcecodeFile(errorNode.getLocation());
        appendSolution(source, sourceNode, calculateOffset());
    }

    private void checkStandardLibrary() {
        String functionName = callExpression.getFunctionNameExpression().getRawSignature();

        String library = new StandardLibraryFinder().findLibrary(functionName);

        if (library != null) {
            INode sourceNode = Utils.getSourcecodeFile(scope);
            String includeStm = String.format("#include <%s>\n", library);
            appendSolution(includeStm, sourceNode, 0);
        }
    }

    private void appendSolution(String source, INode sourceFile, int offset) {
        ResolvedSolution solution = new ResolvedSolution();

        solution.setErrorNode(errorNode);
        solution.setResolvedSourceCode(source);
        solution.setSourcecodeFile(sourceFile.getAbsolutePath());

        solution.setOffset(offset);

        solutions.add(solution);
    }

    public ResolvedFunctionNode generateFunction() {
        ResolvedFunctionNode functionNode = null;

        String returnType = new ReturnTypePrediction(scope).getReturnType(callExpression);

        IASTInitializerClause[] args = callExpression.getArguments();
        MissingFuntionGenerator generator;

        if (returnType != null) {
            generator = new MissingFuntionGenerator(returnType, errorNode.getName(), args);
            generator.setContext(scope);
            functionNode = generator.exec();

            if (functionNode != null) {
                INode parent = errorNode.getLocation();
                functionNode.setAbsolutePath(parent.getAbsolutePath() + File.separator + functionNode.getName());
            }
        }

        return functionNode;
    }

    private List<FunctionNode> findExistFunction() {
        List<FunctionNode> matches = new ArrayList<>();

        if (!(errorNode.getLocation() instanceof StructureNode)) {
            ProjectNode projectNode = Environment.getInstance().getProjectNode();
            List<INode> functions = Search.searchNodes(projectNode, new FunctionNodeCondition());

            INode dataRoot = Environment.getInstance().getUserCodeRoot();
            functions.addAll(Search.searchNodes(dataRoot, new FunctionNodeCondition()));

            IASTInitializerClause[] args = callExpression.getArguments();

            matches = functions.stream()
                    .filter(f -> {
                        if (f instanceof FunctionNode) {
                            String fName = ((FunctionNode) f).getSingleSimpleName();
                            List<IVariableNode> params = ((FunctionNode) f).getArguments();

                            INode realParent = ((FunctionNode) f).getRealParent() == null ? f.getParent() : ((FunctionNode) f).getRealParent();

                            return fName.equals(errorNode.getName()) && !(realParent instanceof ISourcecodeFileNode)
                                    && args.length == params.size();
                        }
                        return false;
                    }).map(f -> (FunctionNode) f)
                    .collect(Collectors.toList());
        }

        return matches;
    }

    private void findFunctionCallExpression() {
        IASTNode call = errorNode.getCall();

        while (call != null) {
            if (call instanceof IASTFunctionCallExpression) {
                callExpression = (IASTFunctionCallExpression) call;
                break;
            } else
                call = call.getParent();
        }
    }

//    @Override
//    protected int calculateOffset() {
//        INode location = errorNode.getLocation();
//
//        int offset = 0;
//
//        if (location instanceof SourcecodeFileNode) {
//            if (scope instanceof IFunctionNode) {
//                offset = ((IFunctionNode) scope).getAST().getFileLocation().getNodeOffset();
//
//            } else {
//                offset = calculateOffsetInFile();
//            }
//
//        } else if (location instanceof NamespaceNode) {
//            ICPPASTNamespaceDefinition definition = ((NamespaceNode) location).getAST();
//            IASTDeclaration[] declarations = definition.getDeclarations();
//
//            if (declarations.length > 0) {
//                IASTDeclaration last = declarations[declarations.length - 1];
//
//                offset = last.getFileLocation().getNodeOffset();
//
//                offset += last.getFileLocation().getNodeLength();
//            }
//
//        } else if (location instanceof StructureNode) {
//            IASTSimpleDeclaration declaration = ((StructureNode) location).getAST();
//
//            IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
//
//            if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
//                IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpecifier).getMembers();
//
//                if (declarations.length > 0) {
//                    IASTDeclaration last = declarations[declarations.length - 1];
//
//                    offset = last.getFileLocation().getNodeOffset();
//
//                    offset += last.getFileLocation().getNodeLength();
//                }
//            }
//        }
//
//        return offset;
//    }
}
