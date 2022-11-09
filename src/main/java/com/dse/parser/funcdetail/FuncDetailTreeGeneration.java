package com.dse.parser.funcdetail;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.dependency.TypeDependency;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.ClassNodeCondition;
import com.dse.search.condition.GlobalVariableNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.search.condition.StructNodeCondition;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dse.util.Utils.getMacrosValue;

public class FuncDetailTreeGeneration implements IFuncDetailTreeGeneration {
    private final static AkaLogger logger = AkaLogger.get(FuncDetailTreeGeneration.class);

    public FuncDetailTreeGeneration(RootNode root, ICommonFunctionNode fn) {
        generateTree(root, fn);
    }

    @Override
    public void generateTree(RootNode root, ICommonFunctionNode fn) {
        logger.debug("generateGlobalSubTree");
        generateGlobalSubTree(root, fn);

        logger.debug("generateUUTSubTree");
        generateUUTSubTree(root, fn);

        logger.debug("generateStubSubTree");
        generateStubSubTree(root, fn);
    }

    List<INode> includeNodes = new ArrayList<>();

    private boolean isSystemUnit(INode unit) {
        List<INode> sources = Search
                .searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition());

        return !sources.contains(unit);
    }

    private List<Node> getAllIncludedNodes(INode n) {
        List<Node> output = new ArrayList<>();

        if (n != null) {
            try {
                for (Dependency child : n.getDependencies()) {
                    if (child instanceof IncludeHeaderDependency) {
                        if (child.getStartArrow().equals(n)) {
                            includeNodes.add(n);

                            INode end = child.getEndArrow();
                            if (!includeNodes.contains(end) && !isSystemUnit(end)) {
                                output.add((Node) end);
                                /*
                                 * In case recursive include
                                 */
                                output.addAll(getAllIncludedNodes(end));
                            }
                        }
                    }
                }
            } catch (StackOverflowError e) {
                e.printStackTrace();
            }
        }

        return output;
    }


    @Override
    public void generateGlobalSubTree(RootNode root, ICommonFunctionNode fn) {
        RootNode globalRoot = new RootNode(NodeType.GLOBAL);

        /*
         * Them cac bien global co trong unit
         */
//        Map<String, String> macrosValue = Utils.getMacrosValue((IFunctionNode) fn);
//        SourcecodeFileParser parser = new SourcecodeFileParser();
//        parser.appendMacroDefinition(macrosValue);
//        INode unit = null;
//        if (fn.getParent() instanceof ISourcecodeFileNode) {
//            parser.setSourcecodeNode((ISourcecodeFileNode) fn.getParent());
//            try {
//                unit = parser.generateTree();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            unit = Utils.getSourcecodeFile(fn);
//        }
        INode unit = Utils.getSourcecodeFile(fn);
        List<INode> globalVariables = Search.searchNodes(unit, new GlobalVariableNodeCondition());

        List<Node> includedNodes = getAllIncludedNodes(unit);
        for (Node node : includedNodes) {
            List<INode> includedGlobalVariables = Search.searchNodes(node, new GlobalVariableNodeCondition());
            includedGlobalVariables.forEach(global -> {
                if (!globalVariables.contains(global))
                    globalVariables.add(global);
            });
        }

        for (INode node : globalVariables) {
            if ((node instanceof ExternalVariableNode)) {
                boolean isConst = ((ExternalVariableNode) node).getRawType().contains("const ");
                if (!isConst)
                    globalRoot.addElement(node);
            }
        }

        /*
         * Them instance trong truong hop test method cua class
         */
        if (!Environment.getInstance().isC()) {
            List<INode> instances = searchAllInstances(unit);
            if (fn instanceof IFunctionNode) {
                INode realParent = ((IFunctionNode) fn).getRealParent();
                if (realParent instanceof StructOrClassNode && !instances.contains(realParent)) {
                    instances.add(realParent);
                }
            }

            for (INode instance : instances) {
                InstanceVariableNode instanceVarNode = generateInstance(instance);
                globalRoot.addElement(instanceVarNode);
            }
        }

        root.addElement(globalRoot);
    }

    private InstanceVariableNode generateInstance(INode correspondingType) {
        InstanceVariableNode instance = new InstanceVariableNode();
        String type = Search.getScopeQualifier(correspondingType);

        if (correspondingType instanceof ClassNode && ((ClassNode) correspondingType).isTemplate()) {
            String[] templateParams = TemplateUtils.getTemplateParameters(correspondingType);
            if (templateParams != null) {
                type += TemplateUtils.OPEN_TEMPLATE_ARG;

                for (String param : templateParams)
                    type += param + ", ";

                type += TemplateUtils.CLOSE_TEMPLATE_ARG;
                type = type.replace(", >", ">");
            }
        }

        instance.setCoreType(type);
        instance.setRawType(type);
        instance.setReducedRawType(type);

        String instanceVarName = type.replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);
        instanceVarName = SourceConstant.INSTANCE_VARIABLE + SpecialCharacter.UNDERSCORE + instanceVarName;
        instance.setName(instanceVarName);

        instance.setParent(correspondingType);

        instance.setCorrespondingNode(correspondingType);
        TypeDependency d = new TypeDependency(instance, correspondingType);
        instance.getDependencies().add(d);
        correspondingType.getDependencies().add(d);

        return instance;
    }

    private List<INode> searchAllInstances(INode unit) {
        List<INode> instances;

        List<SearchCondition> conditions = new ArrayList<>();
        conditions.add(new StructNodeCondition());
        conditions.add(new ClassNodeCondition());
        instances = Search.searchNodes(unit, conditions);

        instances.removeIf(instance -> {
            if (((StructOrClassNode) instance).getVisibility() != ICPPASTVisibilityLabel.v_public)
                return true;

            if (instance instanceof IEmptyStructureNode)
                return true;

            return false;
        });

        return instances;
    }

    @Override
    public void generateUUTSubTree(RootNode root, ICommonFunctionNode fn) {
        RootNode uutRoot = new RootNode(NodeType.UUT);
        uutRoot.addElement(fn);
        root.addElement(uutRoot);
    }

    @Override
    public void generateStubSubTree(RootNode root, ICommonFunctionNode fn) {
        RootNode dontStubRoot = new RootNode(NodeType.DONT_STUB);
        RootNode stubRoot = new RootNode(NodeType.STUB);

        for (Dependency d : fn.getDependencies()) {
            if (d instanceof FunctionCallDependency && ((FunctionCallDependency) d).fromNode(fn)) {
                INode referNode = d.getEndArrow();
                stubRoot.addElement(referNode);
            }
        }

        root.addElement(dontStubRoot);
        root.addElement(stubRoot);
    }
}
