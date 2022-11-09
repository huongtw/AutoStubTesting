package com.dse.util;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.FunctionCallParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.MethodFinder;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.resolver.NewTypeResolver;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.ClassNodeCondition;
import com.dse.search.condition.DefinitionFunctionNodeCondition;
import com.dse.search.condition.FunctionNodeCondition;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.io.File;
import java.util.*;

public class TemplateUtils {
    private final static AkaLogger logger = AkaLogger.get(TemplateUtils.class);
    public static final String OPEN_TEMPLATE_ARG = "<";
    public static final String CLOSE_TEMPLATE_ARG = ">";
    public static final String PARAMETER_SEPARATOR = ", ";

    /**
     * Check whether a type is a template type
     * @param rawType
     * @param fn
     * @return
     */
    public static boolean isTemplateTypeDefinedByUser(String rawType, ICommonFunctionNode fn) {
        if (fn instanceof MacroFunctionNode)
            return false;
        IFunctionNode functionNode = (IFunctionNode) fn;
        rawType = rawType.replace("&","").trim();
        rawType = VariableTypeUtils.deleteStorageClasses(rawType);

        if (functionNode.isTemplate()) {
            // Ex: "template <typename T>
            //T myMax(T x, T y)
            //{
            //   return (x > y)? x: y;
            //}
            CPPASTTemplateDeclaration templateDeclaration = null;
            if (functionNode.getAST() instanceof CPPASTTemplateDeclaration) {
                templateDeclaration = (CPPASTTemplateDeclaration) functionNode.getAST();
            } else if (functionNode.getAST().getParent() instanceof CPPASTTemplateDeclaration) {
                templateDeclaration = (CPPASTTemplateDeclaration) functionNode.getAST().getParent();
            }

            if (templateDeclaration != null){
                ICPPASTTemplateParameter[] parameters = templateDeclaration.getTemplateParameters();
                for (ICPPASTTemplateParameter parameter: parameters) {
                    // get typename"T"
                    String typeName = ((CPPASTSimpleTypeTemplateParameter) parameter).getName().getRawSignature();
//                    if (typeName.equals(rawType)){
                    // Ex: rawtype = "T[]", type name = "T"
                    if (rawType.matches(".*\\b" + typeName + "\\b.*")){
                        return true;
                    }
                }
            }
        }
            return false;

    }

    public static boolean isTemplateClass(String rawType) {
        rawType = VariableTypeUtils.deleteUnionKeyword(rawType);
        rawType = VariableTypeUtils.deleteStructKeyword(rawType);
        rawType = VariableTypeUtils.deleteStorageClasses(rawType);
        rawType = rawType.trim();
        return rawType.contains(OPEN_TEMPLATE_ARG)
                && rawType.endsWith(CLOSE_TEMPLATE_ARG);
    }

    public static boolean isTemplate(String rawType) {
        return rawType.contains(OPEN_TEMPLATE_ARG)
                && rawType.contains(CLOSE_TEMPLATE_ARG);
    }

    public static boolean isChildOfTemplateClass(IVariableNode variableNode) {
        INode parent = variableNode.getParent();

        while (parent != null) {
            if (parent instanceof ClassNode && ((ClassNode) parent).isTemplate())
                return true;
            else
                parent = parent.getParent();
        }

        return false;
    }

    public static String getCoreType(String rawType) {
        String coreType = getTemplateClass(rawType);

        if (isTemplateClass(rawType)) {
            int begin = rawType.indexOf(OPEN_TEMPLATE_ARG) + 1;
            int end = rawType.lastIndexOf(CLOSE_TEMPLATE_ARG);

            coreType = coreType.substring(begin, end);
        }

        return coreType;
    }

    public static String getTemplateClass(String rawType) {
        int index = rawType.lastIndexOf(CLOSE_TEMPLATE_ARG) + 1;

        String coreType = rawType.substring(0, index) + rawType.substring(index)
                .replaceAll(IRegex.POINTER, "")
                .replaceAll(IRegex.ADDRESS, "")
                .replaceAll(IRegex.ARRAY_INDEX, "");

        return coreType;
    }


    public static String getTemplateFullRawType(VariableNode variableNode) {
        String rawType = variableNode.getRawType();

        String variablePath = variableNode.getAbsolutePath();

        int closeBracketPos = variablePath.lastIndexOf(')') + 1;
        String functionPath = variablePath.substring(0, closeBracketPos);

        List<SearchCondition> conditions = Arrays.asList(
                new FunctionNodeCondition(),
                new DefinitionFunctionNodeCondition()
        );

        List<INode> functions = Search
                .searchNodes(Environment.getInstance().getProjectNode(), conditions);

        for (INode function : functions) {
            if (PathUtils.equals(function.getAbsolutePath(), functionPath)) {
                List<Level> space = new VariableSearchingSpace(function)
                        .generateExtendSpaces();
                rawType = recursiveGetFullRawType(rawType, space);
                break;
            }
        }

        rawType = VariableTypeUtils.deleteStorageClasses(rawType);
        rawType = VariableTypeUtils.deleteVirtualAndInlineKeyword(rawType);
        rawType = VariableTypeUtils.deleteReferenceOperator(rawType);
        rawType = rawType.replace("<::", "<")
                .replace(", ::", ", ");

        return rawType;
    }

    public static String recursiveGetFullRawType(String rawType, List<Level> space) {
        // Get Core Type
        String coreType = rawType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY)
                .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);

        if (isTemplate(rawType))
            coreType = getTemplateClass(rawType);

        // Get type operator
        String typeOperator = rawType.substring(coreType.length());
        if (isTemplateClass(rawType)) typeOperator = SpecialCharacter.EMPTY;

        String fullType = coreType;

        IASTNode astNode = Utils.convertToIAST(coreType);

        if (astNode instanceof IASTIdExpression) {
            IASTName fName = ((IASTIdExpression) astNode).getName();

            String className = "";

            if (fName instanceof ICPPASTQualifiedName) {
                ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) fName).getQualifier();
                for (ICPPASTNameSpecifier specifier : qualifier)
                    className += specifier.getRawSignature()
                            + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS;

                fName = fName.getLastName();
            }

            if (fName instanceof ICPPASTTemplateId) {
                String templateName = ((ICPPASTTemplateId) fName)
                        .getTemplateName().getRawSignature();
                className += templateName;
            } else if (fName != null) {
                className += fName.getRawSignature();
            }

            String relativePath = className.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

            List<INode> possibleNodes = Search.searchInSpace(space, new ClassNodeCondition(), relativePath);

            if (possibleNodes.size() == 1) {
                fullType = Search.getScopeQualifier(possibleNodes.get(0));
            } else {
                fullType = className;
            }

            if (fName instanceof ICPPASTTemplateId) {
                fullType += OPEN_TEMPLATE_ARG;

                IASTNode[] templateArguments = ((ICPPASTTemplateId) fName).getTemplateArguments();
                for (IASTNode templateArgument : templateArguments) {
                    String templateArgRawType = templateArgument.getRawSignature();

                    fullType += recursiveGetFullRawType(templateArgRawType, space);
                    fullType += PARAMETER_SEPARATOR;
                }

                fullType += CLOSE_TEMPLATE_ARG;

                fullType = fullType.replace(PARAMETER_SEPARATOR + CLOSE_TEMPLATE_ARG, CLOSE_TEMPLATE_ARG);
            }
        }

        return fullType + typeOperator;
    }

    /**
     * Get all template arguments in raw type
     *
     * Eg: vector <int, float> -> {"int", "float"}
     *     vector<pair<int, float>> -> {"pair<int, float>"}
     *     pair<pair<int, pair<float, string>>, float> -> {"pair<int, pair<float, string>>", "float"}
     *
     * @param rawType of variable
     * @return list of template arguments
     */
    public static String deleteTemplateParameters(String rawType) {
        String rawTypeWithoutParams = rawType + "";

        int openPos = rawType.indexOf(OPEN_TEMPLATE_ARG);
        int closePos = rawType.lastIndexOf(CLOSE_TEMPLATE_ARG);

        if (openPos > 0 && closePos > 0)
            rawTypeWithoutParams = rawType.substring(0, openPos) + rawType.substring(closePos + 1);

        return rawTypeWithoutParams.trim();
    }

    public static String[] getTemplateParameters(INode node) {
        IASTNode ast = null;

        if (node instanceof DefinitionFunctionNode)
            ast = ((DefinitionFunctionNode) node).getAST();
        else if (node instanceof AbstractFunctionNode)
            ast = ((AbstractFunctionNode) node).getAST();
        else if (node instanceof ClassNode)
            ast = ((ClassNode) node).getAST();

        if (ast != null && ast.getParent() instanceof ICPPASTTemplateDeclaration) {
            ICPPASTTemplateDeclaration declaration = (ICPPASTTemplateDeclaration) ast.getParent();
            ICPPASTTemplateParameter[] astParameters = declaration.getTemplateParameters();

            int length = astParameters.length;

            String[] parameters = new String[length];

            for (int i = 0; i < length; i++) {
                if (astParameters[i] instanceof ICPPASTSimpleTypeTemplateParameter)
                    parameters[i] = ((ICPPASTSimpleTypeTemplateParameter) astParameters[i]).getName().getRawSignature();
                else if (astParameters[i] instanceof ICPPASTTemplatedTypeTemplateParameter)
                    parameters[i] = ((ICPPASTTemplatedTypeTemplateParameter) astParameters[i]).getName().getRawSignature();
                else
                    return null;
            }

            return parameters;
        }

        return null;
    }

    public static String[] getTemplateVariableArguments(VariableNode variableNode) {
        String[] templateArgs = null;

        IASTNode astVariable = variableNode.getAST();

        if (astVariable != null) {
            IASTDeclSpecifier declSpecifier = null;

            if (astVariable instanceof IASTParameterDeclaration)
                declSpecifier = ((IASTParameterDeclaration) astVariable).getDeclSpecifier();

            else if (astVariable instanceof IASTSimpleDeclaration)
                declSpecifier = ((IASTSimpleDeclaration) astVariable).getDeclSpecifier();

            if (declSpecifier instanceof IASTNamedTypeSpecifier) {
                IASTName name = ((IASTNamedTypeSpecifier) declSpecifier).getName();
                if (name instanceof ICPPASTQualifiedName)
                    name = name.getLastName();
                if (name instanceof ICPPASTTemplateId) {
                    IASTNode[] templateArgASTNodes = ((ICPPASTTemplateId) name).getTemplateArguments();

                    templateArgs = Arrays.stream(templateArgASTNodes)
                            .filter(Objects::nonNull)
                            .map(IASTNode::getRawSignature)
                            .toArray(String[]::new);

                    return templateArgs;
                }
            }
        } else {
            String type = variableNode.getRawType();
            templateArgs = TemplateUtils.complexGetTemplateArguments(type);
        }

        return templateArgs;
    }

    /**
     *
     * Ex:
     <pre>
     ...
     template1<int>(3, 7);
     int a = 0; ...
     </pre>
     return "template1<int>"

     * @param ast
     * @return all template id, ex: "mypair<int>", v.v.
     */
    public static List<CPPASTTemplateId> getAllTemplateId(IASTNode ast){
        // get all template ids
        List<CPPASTTemplateId> templateIds = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor() {

            @Override
            public int visit(IASTExpression name) {
                // Handle case 'template1<int>(3, 7);'
                if (name.getChildren().length > 0 && name.getChildren()[0] instanceof CPPASTTemplateId)
                    templateIds.add((CPPASTTemplateId) name.getChildren()[0]);
                return ASTVisitor.PROCESS_CONTINUE;
            }

            @Override
            public int visit(IASTDeclaration name) {
                // Handle case 'mypair<int> myobject (115, 36);: '
                if (name instanceof CPPASTSimpleDeclaration && name.getChildren().length > 0) {
                    IASTNode firstChild = name.getChildren()[0];
                    if (firstChild instanceof CPPASTNamedTypeSpecifier) {
                        if (firstChild.getChildren().length > 0 && firstChild.getChildren()[0] instanceof CPPASTTemplateId)
                            templateIds.add((CPPASTTemplateId) firstChild.getChildren()[0]);
                    }
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitDeclarations = true;
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitExpressions = true;
        ast.accept(visitor);
        return templateIds;
    }

    @Deprecated
    public static List<DefinitionFunctionNode> getPossibleTemplateArgumentsv2(IFunctionNode functionNode) {
        List<DefinitionFunctionNode> possibleTemplates = new ArrayList<>();

        if (!(functionNode.isTemplate()))
            return possibleTemplates;

        // find template types
        List<String> templateTypes = getTypesInTemplate(functionNode);
        logger.debug("Found template types: " + templateTypes);
        if (templateTypes.size() == 0)
            return possibleTemplates;
        List<CPPASTTemplateId> declarations = getAllTemplateId(Utils.getSourcecodeFile(functionNode).getAST());

        //
        for (CPPASTTemplateId templateId : declarations) {
            // Ex: "mypair<int>"
            IASTName name = templateId.getTemplateName(); //mypair

            if (name.getRawSignature().equals(functionNode.getSimpleName())) {
                logger.debug("Found " + templateId.getRawSignature());
                IASTNode[] arguments = templateId.getTemplateArguments();

                Map<String, String> mappingFromTemplateTypeToRealType = new HashMap<>();
                for (int i = 0; i < arguments.length; i++) {
                    IASTNode argument = arguments[i];
                    mappingFromTemplateTypeToRealType.put(templateTypes.get(i), argument.getRawSignature());
                }
                logger.debug("mappingFromTemplateTypeToRealType = " + mappingFromTemplateTypeToRealType);

                String prototype = functionNode.getReturnType()+" " + functionNode.getAST().getDeclarator().getRawSignature();
                logger.debug("original prototype = " + prototype);

                String newPrototype = prototype;
                for (String key : mappingFromTemplateTypeToRealType.keySet())
                    newPrototype = newPrototype.replaceAll("\\b" + Utils.toRegex(key) + "\\b", mappingFromTemplateTypeToRealType.get(key));
                newPrototype += ";";
                newPrototype = newPrototype.replaceAll("\\[\\s*[0-9]*\\s*\\]\\s*([a-zA-Z_]+)"," $1[]");
                logger.debug("newPrototype = " + newPrototype);
                // create
                if (newPrototype != null) {
                    IASTNode ast = Utils.convertToIAST(newPrototype);
                    if (ast instanceof IASTProblemHolder) {
                        ast = Utils.convertToIAST("void* " + newPrototype);
                    }
                    if (ast instanceof IASTDeclarationStatement)
                        ast = ((IASTDeclarationStatement) ast).getDeclaration();

                    if (ast instanceof IASTSimpleDeclaration) {
                        DefinitionFunctionNode suggestion = new DefinitionFunctionNode();
                        suggestion.setAbsolutePath(functionNode.getAbsolutePath());
                        suggestion.setAST((CPPASTSimpleDeclaration) ast);
                        suggestion.setName(suggestion.getNewType());

                        possibleTemplates.add(suggestion);
                    }

                }
            }

        }

        return possibleTemplates;
    }

    @Deprecated
    // Note: I tried with some test cases and it fails.
    public static List<DefinitionFunctionNode> getPossibleTemplateArguments(ICommonFunctionNode functionNode) {
        List<DefinitionFunctionNode> functionNodes = new ArrayList<>();

        for (Dependency d : functionNode.getDependencies()) {
            if (d instanceof FunctionCallDependency && d.getEndArrow().equals(functionNode)) {
                if (d.getStartArrow() instanceof IFunctionNode) {
                    IFunctionNode callee = (IFunctionNode) d.getStartArrow();

                    IASTFunctionDefinition fnAst = callee.getAST();

                    FunctionCallParser visitor = new FunctionCallParser();
                    fnAst.accept(visitor);

                    for (IASTFunctionCallExpression expression : visitor.getExpressions()) {
                        MethodFinder finder = new MethodFinder(callee);
                        INode called = finder.find(expression);
                        if (called != null && called.equals(functionNode)) {
                            String name = expression.getFunctionNameExpression().getRawSignature();
                            String prototype = null;

                            if (isTemplate(name)) {
                                IASTName astName = ((IASTIdExpression) expression.getFunctionNameExpression()).getName();

                                if (astName instanceof ICPPASTQualifiedName)
                                    astName = astName.getLastName();

                                List<String> templateArgs = new ArrayList<>();
                                if (astName instanceof ICPPASTTemplateId) {
                                    for (IASTNode arg : ((ICPPASTTemplateId) astName).getTemplateArguments())
                                        if (arg != null)
                                            templateArgs.add(arg.getRawSignature());
                                }

                                String[] templateParams = getTemplateParameters(functionNode);
                                prototype = functionNode.toString();

                                assert templateParams != null;
                                if (templateArgs.size() == templateParams.length) {
                                    for (int i = 0; i < templateArgs.size(); i++) {
                                        prototype = prototype.replaceAll("\\b" + templateParams[i] + "\\b", templateArgs.get(i));
                                    }
                                }
                            } else {
                                prototype = "";

                                List<IVariableNode> parameters = functionNode.getArguments();
                                IASTInitializerClause[] arguments = expression.getArguments();
                                Map<String, String> templateMapType = new HashMap<>();

                                for (int i = 0; i < arguments.length; i++) {
                                    if (arguments[i] instanceof IASTExpression) {
                                        String paramName = parameters.get(i).getName();

                                        String paramType = new NewTypeResolver(callee, 0).exec((IASTExpression) arguments[i]);
                                        paramType = VariableTypeUtils.deleteStorageClasses(paramType);
                                        paramType = paramType.replaceAll(" \\*", "*")
                                                .replaceAll(" \\[", "[");

                                        String parameterDeclaration;

                                        List<String> indexes = Utils.getIndexOfArray(deleteTemplateParameters(paramType));

//                                        List<String> indexes = Utils.getIndexOfArray(deleteTemplateParameters(parameters.get(i).getRawType()));

                                        if (indexes.size() > 0) {
                                            int idx = paramType.length() - 1;
                                            while (paramType.charAt(idx) == SpecialCharacter.CLOSE_SQUARE_BRACE
                                                    || paramType.charAt(idx) == SpecialCharacter.OPEN_SQUARE_BRACE
                                                    || Character.isDigit(paramType.charAt(idx)))
                                                idx--;
                                            parameterDeclaration = paramType.substring(0, idx + 1) + " " + paramName;
                                            for (String index : indexes)
                                                parameterDeclaration += "[" + index + "]";

                                        } else {
                                            parameterDeclaration = paramType + " " + paramName;
                                        }


                                        prototype += parameterDeclaration + ", ";
                                    } else {
                                        prototype = null;
                                        break;
                                    }
                                }

                                String returnType = new NewTypeResolver(callee, 0).exec(expression);

                                if (returnType != null) {

                                    returnType = VariableTypeUtils.deleteSizeFromArray(returnType);
                                    returnType = returnType.replaceAll(" \\*", "*")
                                            .replaceAll(" \\[]", "*")
                                            .replaceAll("\\[]", "*");

                                    prototype = String.format("%s %s(%s)", returnType, functionNode.getSimpleName(), prototype);
                                    prototype = prototype.replace(", )", ")");
                                } else prototype = null;
                            }

                            if (prototype != null) {
                                IASTNode ast = Utils.convertToIAST(prototype);
                                if (ast instanceof IASTDeclarationStatement)
                                    ast = ((IASTDeclarationStatement) ast).getDeclaration();

                                if (ast instanceof IASTSimpleDeclaration) {
                                    DefinitionFunctionNode suggestion = new DefinitionFunctionNode();
                                    suggestion.setAbsolutePath(functionNode.getAbsolutePath());
                                    suggestion.setAST((CPPASTSimpleDeclaration) ast);
                                    suggestion.setName(suggestion.getNewType());

                                    functionNodes.add(suggestion);
                                }
                            }
                        }
                    }
                }
            }
        }

        return functionNodes;
    }
//
//    private static String getTemplatePrototypeUsingArgumentsCase(IASTFunctionCallExpression expression, ICommonFunctionNode function) {
//        expression.getFunctionNameExpression()
//    }

    public static String getTemplateSimpleName(String rawType) {
        String rawName = getTemplateRawName(rawType);

        String[] element = rawName.split(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS);

        return element[element.length - 1];
    }

    /**
     * <pre>
     * template <class T>
     * class mypair {
     * public:
     * mypair (T first, T second){...}
     * };
     * </pre>
     * <p>
     * Consider constructor "mypair (T first, T second)", we found this constructor is used in a template class with a template parameter "T"
     *
     * @param functionNode a function which is a template function, or in a template class
     * @return the name of template parameter, e.g., "T"
     */
    public static List<String> getTypesInTemplate(IFunctionNode functionNode) {
        List<String> types = new ArrayList<>();

        List<IASTNode> searchNodes = new ArrayList<>();
        searchNodes.add(functionNode.getAST());
        if (functionNode.getRealParent() != null && functionNode.getRealParent() instanceof ClassNode)
            searchNodes.add(((ClassNode) functionNode.getRealParent()).getAST());

        for (IASTNode searchNode : searchNodes) {
            // find template declaration
            int limit = 4; // to avoid infinite loop
            IASTNode ast = searchNode;
            while (ast != null && !(ast instanceof CPPASTTemplateDeclaration) && limit > 0) {
                ast = ast.getParent();
                limit--;
            }

            if (ast != null && ast instanceof CPPASTTemplateDeclaration) {
                ICPPASTTemplateParameter[] templateParameters = ((CPPASTTemplateDeclaration) ast).getTemplateParameters();
                for (ICPPASTTemplateParameter templateParameter : templateParameters)
                    if (templateParameter instanceof CPPASTSimpleTypeTemplateParameter) {
                        String templateType = ((CPPASTSimpleTypeTemplateParameter) templateParameter).getName().getRawSignature();
                        if (!types.contains(templateType))
                            types.add(templateType);
                    }
            }
        }
        return types;
    }

    public static String getTemplateRawName(String rawType) {
        String simpleRawType = deleteTemplateParameters(rawType);
        simpleRawType = VariableTypeUtils.deleteStorageClasses(simpleRawType);
        simpleRawType = VariableTypeUtils.deleteVirtualAndInlineKeyword(simpleRawType);

        String coreType = simpleRawType
                .replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY)
                .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY)
                .replaceAll(IRegex.ADDRESS, SpecialCharacter.EMPTY);

        return coreType;
    }

    public static String getTemplateFullName(VariableNode variable) {
        String rawType = variable.getRawType();
        String fullName = getTemplateRawName(rawType);;

        String variablePath = variable.getAbsolutePath();

        int closeBracketPos = variablePath.lastIndexOf(')') + 1;
        String functionPath = variablePath.substring(0, closeBracketPos);

        List<INode> functions = Search
                .searchNodes(Environment.getInstance().getProjectNode(), new FunctionNodeCondition(), functionPath);

        if (functions.isEmpty())
            functions = Search
                    .searchNodes(Environment.getInstance().getProjectNode(), new DefinitionFunctionNodeCondition(), functionPath);

        if (!functions.isEmpty()) {
            String classRelativePath = fullName.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

            List<Level> space = new VariableSearchingSpace(functions.get(0)).generateExtendSpaces();
            List<INode> possibleNodes = Search.searchInSpace(space, new ClassNodeCondition(), classRelativePath);


            if (!possibleNodes.isEmpty()) {
                if (possibleNodes.size() == 1)
                    fullName = Search.getScopeQualifier(possibleNodes.get(0));
            }
        }

        return fullName;
    }

//    public static void main(String[] args) {
//        String[] arguments = getTemplateArguments("List<Pair<int*, vector<float>>>");
//        System.out.println();
//    }

    public static String[] getTemplateArguments(String rawType) {
        IASTNode astNode = Utils.convertToIAST(rawType);

        if (astNode instanceof IASTIdExpression) {
            IASTName fName = ((IASTIdExpression) astNode).getName();

            if (fName instanceof ICPPASTQualifiedName)
                fName = fName.getLastName();

            if (fName instanceof ICPPASTTemplateId) {
                IASTNode[] templateArguments = ((ICPPASTTemplateId) fName).getTemplateArguments();

                String[] result = new String[templateArguments.length];

                for (int i = 0; i < templateArguments.length; i++)
                    result[i] = templateArguments[i].getRawSignature();

                return result;
            }
        }

        return complexGetTemplateArguments(rawType);
    }

    public static String[] complexGetTemplateArguments(String rawType) {
        List<String> templateArguments = new ArrayList<>();

        // Step 2: remove template type name and < > container
        String rType = rawType.substring(rawType.indexOf(OPEN_TEMPLATE_ARG) + 1,
                            rawType.lastIndexOf(CLOSE_TEMPLATE_ARG));

        String[] tempTemplateArgs = rType.split(",");

        String tempCompleteArg = "";

        for (String argument : tempTemplateArgs) {
            // trim the string
            argument = argument.trim();

            // case float, pair<float>, vector<vector<int>>
            if ((!argument.contains(OPEN_TEMPLATE_ARG) && !argument.contains(CLOSE_TEMPLATE_ARG))
                    || (argument.contains(OPEN_TEMPLATE_ARG) && argument.contains(CLOSE_TEMPLATE_ARG)
                    && Utils.countCharIn(argument, '<') == Utils.countCharIn(argument, '>'))) {
                templateArguments.add(argument);
                tempCompleteArg = "";
            } else {
                if (tempCompleteArg.isEmpty())
                    tempCompleteArg += argument;
                else
                    tempCompleteArg += PARAMETER_SEPARATOR + argument;

                if (Utils.countCharIn(tempCompleteArg, '<') == Utils.countCharIn(tempCompleteArg, '>')) {
                    templateArguments.add(tempCompleteArg + "");
                    tempCompleteArg = "";
                }
            }
        }

        return templateArguments.toArray(new String[templateArguments.size()]);
    }

    public static boolean isValidClass(ClassNode base, ClassNode derived, String[] baseArguments) {
        String baseName = base.getName();

        IASTCompositeTypeSpecifier declSpec = derived.getSpecifiedAST();

        if (declSpec instanceof CPPASTCompositeTypeSpecifier) {
            ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[] baseSpecs =
                    ((CPPASTCompositeTypeSpecifier) declSpec).getBaseSpecifiers();

            for (ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec : baseSpecs) {
                ICPPASTNameSpecifier nameSpec = baseSpec.getNameSpecifier();
                if (nameSpec instanceof ICPPASTTemplateId) {
                    String derivedName = ((ICPPASTTemplateId) nameSpec).getTemplateName().getRawSignature();

                    if (derivedName.equals(baseName)) {
                        List<String> templateParameters = Arrays.asList(
                                Objects.requireNonNull(getTemplateParameters(derived)));

                        IASTNode[] templateArguments = ((ICPPASTTemplateId) nameSpec).getTemplateArguments();

                        if (templateArguments.length != baseArguments.length)
                            return false;

                        for (int i = 0; i < templateArguments.length; i++) {
                            String templateArgType = templateArguments[i].getRawSignature();

//                            if (templateParameters.contains(templateArgType) {
//
//                            } else {
//                                String baseType =
//                                if (VariableTypeUtils.getSimpleRawType(te))
//                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public static String recursiveGetBaseClass(String type,  ClassNode derived, final List<INode> bases) {
        if (bases == null || bases.isEmpty() || bases.get(0).equals(derived))
            return type;

        IASTCompositeTypeSpecifier declSpec = derived.getSpecifiedAST();

        if (declSpec instanceof CPPASTCompositeTypeSpecifier) {
            ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[] baseSpecs =
                    ((CPPASTCompositeTypeSpecifier) declSpec).getBaseSpecifiers();

            for (ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec : baseSpecs) {
                ICPPASTNameSpecifier nameSpec = baseSpec.getNameSpecifier();
                if (nameSpec instanceof ICPPASTTemplateId) {
                    ICPPASTTemplateId template = ((ICPPASTTemplateId) nameSpec);
                    String baseName = template.getTemplateName().getRawSignature();

                    // delete namespace scope
                    baseName = VariableTypeUtils.getSimpleRealType(baseName);

                    for (INode base : bases) {
                        if (base.getName().equals(baseName)) {

                            if (!derived.isTemplate()) {
                                return recursiveGetBaseClass(template.getRawSignature(), (ClassNode) base, bases);

                            } else {
                                String[] derivedParams = getTemplateParameters(derived);
                                String[] derivedArguments = getTemplateArguments(type);
                                type = template.getRawSignature();

                                for (int i = 0; i < derivedParams.length; i++) {
                                    type = type.replaceAll("\\b" + derivedParams[i] + "\\b", derivedArguments[i]);
                                }

                                return recursiveGetBaseClass(type, (ClassNode) base, bases);
                            }
                        }
                    }
                }
            }
        }

        return type;
    }
}
