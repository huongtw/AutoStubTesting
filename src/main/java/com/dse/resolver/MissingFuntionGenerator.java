package com.dse.resolver;

import com.dse.parser.object.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissingFuntionGenerator implements IMissingGenerator {
    private String returnType;
    private String functionName;
    private IASTInitializerClause[] args;
    private INode context;

//    private final static AkaLogger logger = AkaLogger.get(MissingFuntionGenerator.class);

    public MissingFuntionGenerator(String returnType, String functionName, IASTInitializerClause[] args) {
        this.args = args;
        this.functionName = functionName;
        this.returnType = returnType;
    }

//    public DefinitionFunctionNode gen() {
//        DefinitionFunctionNode functionNode = new DefinitionFunctionNode();
//        IASTSimpleDeclaration declaration = createFunctionDeclaration();
//        functionNode.setAST((CPPASTSimpleDeclaration) declaration);
//
//        INode parent = getParent();
//        if (parent != null) {
//            functionNode.setAbsolutePath(parent.getAbsolutePath() + File.separator + functionNode.getNewType());
//            functionNode.setParent(parent);
//            parent.getChildren().add(functionNode);
//        }
//
//        return functionNode;
//    }

    public ResolvedFunctionNode exec() {
        ResolvedFunctionNode functionNode = null;
        List<IVariableNode> children = new ArrayList<>();

//        if (returnType.contains(TypeResolver.UNRESOLVED_TYPE_FLAG)
//                || returnType.contains(TypeResolver.PROBLEM_TYPE_FLAG)) {
//            logger.debug("cant resolve type of " + functionName);
//            return null;
//        }
//
//        if (functionName.contains(TypeResolver.UNRESOLVED_TYPE_FLAG)
//                || functionName.contains(TypeResolver.PROBLEM_TYPE_FLAG)) {
//            logger.debug("cant resolve namespace scope/class scope of " + functionName);
//            return null;
//        }

        if (returnType == null || functionName == null) {
            return null;
        }

//        for (int i = 0; i < args.length; i++) {
//            String type = new TypeResolver(context).exec((IASTExpression) args[i]);
////            if (type.contains(TypeResolver.UNRESOLVED_TYPE_FLAG) || type.contains(TypeResolver.PROBLEM_TYPE_FLAG)){
////                logger.debug("cant resolve arg " + args[i].getRawSignature() + "of " + functionName);
////                return null;
////            }
//            if (type == null) {
//                return null;
//            }
//            IVariableNode variableNode = new VariableNode();
//            variableNode.setRawType(type);
//            variableNode.setName("arg" + i);
//            variableNode.setParent(functionNode);
//            variableNode.setCoreType(type);
//            variableNode.setReducedRawType(type);
//            children.add(variableNode);
//        }
//
//        functionNode.setArguments(children);
//        functionNode.setSimpleName(getFuntionSimpleSimpleName());
//        functionNode.setName();
//        functionNode.setReturnType(returnType);
//        INode parent = getParent();
//        if (parent == null) {
//            functionNode.setSimpleName(functionName);
//            functionNode.setName();
//            parent = context.getParent();
//        }
//        functionNode.setParent(parent);
//        parent.getChildren().add(functionNode);
//        functionNode.setAbsolutePath(parent.getAbsolutePath() + File.separator + functionNode.getNewType());

        try {
            String prototype = generatePrototype();

            IASTNode ast = Utils.convertToIAST(prototype);

            if (ast instanceof IASTDeclarationStatement)
                ast = ((IASTDeclarationStatement) ast).getDeclaration();

            if (ast instanceof IASTSimpleDeclaration) {
                functionNode = new ResolvedFunctionNode();
                functionNode.setAST((CPPASTSimpleDeclaration) ast);
                functionNode.setName(functionNode.getNewType());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return functionNode;
    }

    private String generatePrototype() throws Exception {
        String prototype = returnType + " " + functionName + "(";

        for (int i = 0; i < args.length; i++) {
            String type = new NewTypeResolver(context, 0).exec((IASTExpression) args[i]);

            if (type == null) {
                throw new Exception("Cant resolve arg type");
            }

            String name = "arg" + i;
            if (args[i] instanceof IASTIdExpression) {
                name = ((IASTIdExpression) args[i]).getName()
                        .getRawSignature()
                        .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);
            }

            prototype += type + " " + name + ", ";
        }

        prototype += ")";

        prototype = prototype.replace(", )", ")");

        return prototype;
    }

//    private IASTDeclarator createDeclarator() {
//        CPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
//
//        // Create name of Function
//        String simpleName = getFuntionSimpleSimpleName();
//        IASTName name = new CPPASTName(simpleName.toCharArray());
//        name.setParent(declarator);
//        declarator.setName(name);
//
//        // Create parameters
//        ICPPASTParameterDeclaration[] parameters = new CPPASTParameterDeclaration[args.length];
//        for (int i = 0; i < args.length; i++) {
//            parameters[i] = new CPPASTParameterDeclaration();
//
//            // Create fDeclSpec
//            String paramType = new TypeResolver(context).exec((IASTExpression) args[i]);
//            VariableSearchingSpace space = new VariableSearchingSpace(context);
//            DeclSpecSearcher searcher = new DeclSpecSearcher(paramType, space.getSpaces());
//
//            IASTDeclSpecifier fDeclSpecClone = searcher.getFirstDeclSpec();
//            IASTDeclSpecifier fDeclSpec = fDeclSpecClone.copy();
//            fDeclSpec.getFileLocation();
//            fDeclSpec.setParent(parameters[i]);
//
//            // Create fDeclarator
//            IASTDeclarator fDeclarator = new CPPASTDeclarator();
//            IASTName parameterName = new CPPASTName(("arg" + i).toCharArray());
//            parameterName.setParent(fDeclarator);
//            fDeclarator.setName(parameterName);
//            fDeclarator.setParent(parameters[i]);
//
//            // Configurate the parameter declaration
//            parameters[i].setDeclSpecifier(fDeclSpec);
//            parameters[i].setDeclarator(fDeclarator);
//            parameters[i].setParent(declarator);
//
//            declarator.addParameterDeclaration(parameters[i]);
//        }
//
//        return declarator;
//    }
//
//    private IASTDeclSpecifier createFunctionDeclSpec() {
//        VariableSearchingSpace space = new VariableSearchingSpace(context);
//        DeclSpecSearcher searcher = new DeclSpecSearcher(returnType, space.getSpaces());
//        IASTDeclSpecifier declSpecifierClone = searcher.getFirstDeclSpec();
//        IASTDeclSpecifier declSpecifier = declSpecifierClone.copy();
//        declSpecifier.getFileLocation();
//
//        return declSpecifier;
//    }
//
//    private IASTNode getASTParentNode() {
//        INode parent = getParent();
//        if (parent instanceof CustomASTNode)
//            return ((CustomASTNode) parent).getAST();
//        else if (parent instanceof SourcecodeFileNode)
//            return ((SourcecodeFileNode) parent).getAST();
//
//        return null;
//    }
//
//    private IASTSimpleDeclaration createFunctionDeclaration() {
//        IASTSimpleDeclaration declaration = new CPPASTSimpleDeclaration();
//
//        IASTDeclarator declarator = createDeclarator();
//        declarator.setParent(declaration);
//        declaration.addDeclarator(declarator);
//
//        IASTDeclSpecifier declSpecifier = createFunctionDeclSpec();
//        declSpecifier.setParent(declaration);
//        declaration.setDeclSpecifier(declSpecifier);
//
//        IASTNode astParentNode = getASTParentNode();
//        declaration.setParent(astParentNode);
//
//        if (astParentNode instanceof IASTTranslationUnit)
//            ((IASTTranslationUnit) astParentNode).addDeclaration(declaration);
//
//        return declaration;
//    }

    private String getFuntionSimpleSimpleName() {
        List<String> elements =
                new ArrayList<>(Arrays.asList(functionName.split(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)));

        int lastIdx = elements.size() - 1;

        return elements.get(lastIdx);
    }

//    private INode getParent() {
//        List<String> elements =
//                new ArrayList<>(Arrays.asList(functionName.split(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)));
//
//        int lastIdx = elements.size() - 1;
//
//        if (lastIdx == 0) {
//            INode parent = context.getParent();
//
//            if (context.getRealParent() != null)
//                parent = context.getRealParent();
//
//            return parent;
//        }
//
//        elements.remove(lastIdx);
//        String path = String.join(File.separator, elements);
//
//        List<INode> possibleParent = new VariableSearchingSpace(context)
//                .search(path, new ClassvsStructvsNamespaceCondition());
//
//        if (possibleParent.isEmpty())
//            return null;
//
//        return possibleParent.get(0);
//    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public IASTInitializerClause[] getArgs() {
        return args;
    }

    public void setArgs(IASTInitializerClause[] args) {
        this.args = args;
    }

    public INode getContext() {
        return context;
    }

    public void setContext(INode context) {
        this.context = context;
    }
}
