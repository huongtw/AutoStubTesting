package com.dse.testdata.object;

import com.dse.parser.object.*;
import com.dse.search.Search2;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MacroSubprogramDataNode extends SubprogramNode {
    public static final String NAME_MACRO_TYPE = "macro_type";

    private DefinitionFunctionNode realFunctionNode;
    private Map<String, String> realTypeMapping = new HashMap<>();
    public MacroSubprogramDataNode() {}

    public MacroSubprogramDataNode(MacroFunctionNode fn) {
        super(fn);
    }

    public void setRealFunctionNode(Map<String, String> typeMap) throws Exception {
        String prototype = generatePrototype(typeMap);

        IASTNode ast = Utils.convertToIAST(prototype);
        if (ast instanceof IASTDeclarationStatement)
            ast = ((IASTDeclarationStatement) ast).getDeclaration();

        if (ast instanceof CPPASTSimpleDeclaration) {
            DefinitionFunctionNode suggestion = new DefinitionFunctionNode();
            suggestion.setAbsolutePath(functionNode.getAbsolutePath());
            suggestion.setAST((CPPASTSimpleDeclaration) ast);
            suggestion.setName(suggestion.getNewType());

            realFunctionNode = suggestion;

            setDefinition(suggestion);

            initInputToExpectedOutputMap(suggestion);
        }
    }

    public String generatePrototype(Map<String, String> typeMap) {
        String functionName = "AKA_MACRO_" + getDisplayNameInParameterTree();

        String returnType = typeMap.get("RETURN");

        String prototype = returnType + " " + functionName + "(";

        for (IVariableNode arg : ((MacroFunctionNode) functionNode).getArguments()) {
            String argName = arg.getName();
            String argDefinition = Utils.generateVariableDeclaration(typeMap.get(argName), argName);
            prototype += argDefinition + ", ";
        }

        prototype += ")";

        prototype = prototype.replace(", )", ")");

        return prototype;
    }

    public void setDefinition(DefinitionFunctionNode definition) throws Exception {
        for (IVariableNode node : definition.getArguments())
            new InitialArgTreeGen().genInitialTree((VariableNode) node, this);

        VariableNode returnVar = Search2.getReturnVarNode(definition);

        String returnType = definition.getReturnType();
        setRawType(returnType);
        setRealType(returnType);

        if (returnVar != null) {
            new InitialArgTreeGen().genInitialTree(returnVar, this);
            setRealType(returnVar.getRealType());
        }
    }

    public void initInputToExpectedOutputMap(DefinitionFunctionNode definition) {
        inputToExpectedOutputMap.clear();
        try {
            ICommonFunctionNode castFunctionNode = (ICommonFunctionNode) functionNode;
            RootDataNode root = new RootDataNode();
            root.setFunctionNode(castFunctionNode);
            InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
            dataTreeGen.generateCompleteTree(root, null);
            for (IDataNode node : root.getChildren()) {
                if (node instanceof SubprogramNode && ((SubprogramNode) node).getFunctionNode() == functionNode) {

                    // generate datanode to use as expected output
                    for (IVariableNode n : definition.getArguments())
                        new InitialArgTreeGen().genInitialTree((VariableNode) n, (DataNode) node);

                    List<ValueDataNode> inputs = getChildren().stream()
                            .filter(n -> n instanceof ValueDataNode)
                            .map(n -> (ValueDataNode) n)
                            .collect(Collectors.toList());

                    List<ValueDataNode> expecteds = node.getChildren().stream()
                            .filter(n -> n instanceof ValueDataNode)
                            .map(n -> (ValueDataNode) n)
                            .collect(Collectors.toList());

                    for (ValueDataNode eo : expecteds) {
                        for (ValueDataNode input : inputs) {
                            if (input.getCorrespondingVar() == eo.getCorrespondingVar()) {
                                inputToExpectedOutputMap.put(input, eo);
                                eo.setParent(input.getParent());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DefinitionFunctionNode getRealFunctionNode() {
        return realFunctionNode;
    }

    public void setRealFunctionNode(DefinitionFunctionNode realFunctionNode) {
        this.realFunctionNode = realFunctionNode;
    }

    public Map<String, String> getRealTypeMapping() {
        return realTypeMapping;
    }

    public void setRealTypeMapping(Map<String, String> realTypeMapping) {
        this.realTypeMapping = realTypeMapping;
    }
}
