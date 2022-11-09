package com.dse.resolver;

import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.*;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VariableTypePrediction {
    private INode context;

    private String name;

    private List<IASTNode> occurs;

    private boolean isArray;

    private boolean isPointer;

    private int[] level;

    private List<MethodHolder> methods = new ArrayList<>();

    private List<String> attributes = new ArrayList<>();

    private String[] results = new String[0];

    public VariableTypePrediction(INode context, String name) {
        this.context = context;
        this.name = name;
        analysis();
    }

    private void analysis() {
        occurs = getAllOccurs();

        level = new int[occurs.size()];

        findAllMethods();

        if (!methods.isEmpty()) {
            List<StructureNode> matchedStructures = findAllMatches();

            results = matchedStructures
                    .stream()
                    .map(this::getFinalType)
                    .toArray(String[]::new);
        }
    }

    public String[] getResults() {
        return results;
    }

    private String getFinalType(INode node) {
        String rawType = Search.getScopeQualifier(node);

        for (int i = 0; level.length > 0 && i < level[0]; i++)
            rawType += VariableTypeUtils.POINTER_CHAR;

        return rawType;
    }

    private void findAllMethods() {
        for (IASTNode occur : occurs) {
            IASTNode parent = occur.getParent();

            while (parent.getParent() != null) {
                boolean isArrayIndex = parent instanceof IASTArraySubscriptExpression;
                boolean isStarUnary = parent instanceof IASTUnaryExpression
                        && ((IASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_star;

                if (isArrayIndex || isStarUnary) {
                    level[occurs.indexOf(occur)]++;
                }

                if (parent instanceof IASTFieldReference) {
                    IASTName fieldName = ((IASTFieldReference) parent).getFieldName();

                    if (fieldName != occur)
                        break;
                }

                parent = parent.getParent();
            }

            if (parent instanceof IASTFieldReference) {
                IASTFieldReference fieldReference = (IASTFieldReference) parent;

                if (fieldReference.isPointerDereference())
                    level[occurs.indexOf(occur)]++;

                String fieldName = fieldReference.getFieldName().getRawSignature();

                if (fieldReference.getParent() instanceof IASTFunctionCallExpression) {
                    IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression) fieldReference.getParent();

                    String returnType = new NewTypeResolver(context).exec(functionCall);

                    IASTInitializerClause[] args = functionCall.getArguments();

                    int arguments = args.length;

                    String[] argTypes = new String[arguments];

                    for (int i = 0; i < arguments; i++) {
                        argTypes[i] = new NewTypeResolver(context).exec((IASTExpression) args[i]);
                    }

                    MethodHolder methodHolder = new MethodHolder();
                    methodHolder.setArguments(arguments);
                    methodHolder.setName(fieldName);
                    methodHolder.setArgumentTypes(argTypes);
                    methodHolder.setReturnType(returnType);

                    methods.add(methodHolder);
                } else {
                    String attrName = fieldReference.getRawSignature().split("\\.")[1];
                    attributes.add(attrName);
                }
            }
        }
    }

    public List<StructureNode> findAllMatches() {
        VariableSearchingSpace searchingSpace = new VariableSearchingSpace(context);

        List<Level> space = searchingSpace.getSpaces();

        List<INode> matches = new ArrayList<>();

        List<SearchCondition> conditions = Arrays.asList(new StructNodeCondition(), new ClassNodeCondition());

        for (Level level : space) {
            for (INode node : level) {
                List<INode> nodes = Search.searchNodes(node, conditions);

                nodes.removeIf(n -> !isMatch(n));

                matches.addAll(nodes);
            }
        }

        return matches.stream().map(s -> (StructureNode) s).collect(Collectors.toList());
    }



    private boolean isMatch(INode node) {
        if (!(node instanceof StructureNode))
            return false;

        StructureNode structureNode = (StructureNode) node;

        List<INode> methods = Search.searchNodes(structureNode, new AbstractFunctionNodeCondition());
        methods.addAll(Search.searchNodes(structureNode, new DefinitionFunctionNodeCondition()));

        List<IVariableNode> attributes = structureNode.getAttributes();

        int methodCount = 0;
        int attributeCount = 0;

        for (MethodHolder holder : this.methods) {
            String functionName = holder.getName();
            int arguments = holder.getArguments();

            for (INode method : methods) {
                ICommonFunctionNode functionNode = (ICommonFunctionNode) method;
                if (functionNode.getSingleSimpleName().equals(functionName)
                        && functionNode.getArguments().size() == arguments) {
                    methodCount++;
                    break;
                }
            }
        }

        for (String attributeName : this.attributes) {
            for (IVariableNode attribute : attributes) {
                if (attribute.getName().equals(attributeName)) {
                    attributeCount++;
                    break;
                }
            }
        }

        return (methodCount == this.methods.size() && attributeCount == this.attributes.size());
    }

    public INode getContext() {
        return context;
    }

    public void setContext(IFunctionNode context) {
        this.context = context;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IASTNode> getOccurs() {
        return occurs;
    }

    public void setOccurs(List<IASTNode> occurs) {
        this.occurs = occurs;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public void setPointer(boolean pointer) {
        isPointer = pointer;
    }

    public List<MethodHolder> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodHolder> methods) {
        this.methods = methods;
    }

    public List<IASTNode> getAllOccurs() {
        IASTNode ast = null;

        if (context instanceof IFunctionNode)
            ast = ((IFunctionNode) context).getAST();
        else if (context instanceof ISourcecodeFileNode)
            ast = ((ISourcecodeFileNode) context).getAST();

        VariableOccursionDetector detector = new VariableOccursionDetector();
        detector.setVariableName(name);

        if (ast != null)
            ast.accept(detector);

        return detector.getOccurs();
    }

    public static class MethodHolder {
        private String name;
        private int arguments;
        private String[] argumentTypes;
        private String returnType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getArguments() {
            return arguments;
        }

        public void setArguments(int arguments) {
            this.arguments = arguments;
        }

        public String[] getArgumentTypes() {
            return argumentTypes;
        }

        public void setArgumentTypes(String[] argumentTypes) {
            this.argumentTypes = argumentTypes;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }
    }

    private enum OccurCase {
        RETURN,
        METHOD_CALL
    }
}
