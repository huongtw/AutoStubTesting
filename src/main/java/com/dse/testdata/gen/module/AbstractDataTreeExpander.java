package com.dse.testdata.gen.module;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.testdata.gen.module.subtree.InitialStubTreeGen;
import com.dse.testdata.gen.module.type.*;
import com.dse.testdata.object.*;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.testdata.object.stl.PairDataNode;
import com.dse.testdata.object.stl.STLDataNode;
import com.dse.testdata.object.stl.SmartPointerDataNode;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.util.*;

public abstract class AbstractDataTreeExpander implements IDataTreeExpander {
    private final static AkaLogger logger = AkaLogger.get(AbstractDataTreeExpander.class);

    private final static int MAX_ARRAY_SIZE = 20;

    // store the template type to real type in template function, e.g, "T"->"int"
    // key: template type
    // value: real type
    private Map<String, String> realTypeMapping;

//    private boolean skipTypeResolver = false;

//    public AbstractDataTreeExpander(boolean skipTypeResolver) {
//        this.skipTypeResolver = skipTypeResolver;
//    }

    public void expandTree(ValueDataNode node) throws Exception {
        VariableNode vParent = node.getCorrespondingVar();
        node.getChildren().clear();

        if (node instanceof ListBaseDataNode) {
            String templateArg = ((ListBaseDataNode) node).getTemplateArgument();
            int size = ((ListBaseDataNode) node).getSize();

            for (int i = 0; i < size /*&& size <= MAX_ARRAY_SIZE*/; i++) {
                String elementName = ((ListBaseDataNode) node).getElementName(i);

                if (TemplateUtils.isTemplateClass(templateArg))
                    generateArrayItemInStructureVariable(elementName, node);
                else if (VariableTypeUtils.isBasic(templateArg))
                    generateArrayItemInBasicVariable(elementName, node);
                else if (VariableTypeUtils.isStructureSimple(templateArg))
                    generateArrayItemInStructureVariable(elementName, node);
                else if (VariableTypeUtils.isPointer(templateArg))
                    generateArrayItemInPointerVariable(elementName, node);
                else if (VariableTypeUtils.isOneDimension(templateArg)
                        || VariableTypeUtils.isMultipleDimension(templateArg)) // VariableTypeUtils.isTwoDimension(templateArg))
                    generateSTLItemInArrayVariable(elementName, node);
                else
                    logger.error("Does not support to expand " + node.getClass());
            }

        } else if (node instanceof PairDataNode) {
            expandPairDataNode((PairDataNode) node);

        } else if (node instanceof StructDataNode) {
            generateStructureItem(vParent, node);

            for (IDataNode child : node.getChildren())
                if (child instanceof StructDataNode)
                    expandTree(((StructDataNode) child));

        } else if (node instanceof UnionDataNode) {
//            generateStructureItem(vParent, node);

        } else if (node instanceof SubClassDataNode) {
            generateConstructor(vParent, node);
            generateStructureItem(vParent, node);

        } else if (node instanceof SmartPointerDataNode) {
            generateConstructor(vParent, node);

        } else if (node instanceof OneDimensionNumberDataNode
                || node instanceof OneDimensionCharacterDataNode
                || node instanceof OneDimensionStringDataNode) {
            int size = ((OneDimensionDataNode) node).getSize();
            for (int i = 0; i < size && size <= MAX_ARRAY_SIZE; i++)
                generateArrayItemInBasicVariable(node.getName() + "[" + i + "]", node);

        } else if (node instanceof OneDimensionStructureDataNode) {
            int size = ((OneDimensionStructureDataNode) node).getSize();
            for (int i = 0; i < size && size <= MAX_ARRAY_SIZE; i++)
                generateArrayItemInStructureVariable(node.getName() + "[" + i + "]", node);

        } else if (node instanceof OneDimensionPointerDataNode) {
            int size = ((OneDimensionPointerDataNode) node).getSize();
            for (int i = 0; i < size && size <= MAX_ARRAY_SIZE; i++)
                generateArrayItemInPointerForMultiLevel(node.getName() + "[" + i + "]", node);

        } else if (node instanceof MultipleDimensionDataNode) {
            if (((MultipleDimensionDataNode) node).isSetSize() || ((MultipleDimensionDataNode) node).isFixedSize()) {
                int[] sizes = ((MultipleDimensionDataNode) node).getSizes();
                int num = 1;
                for (int size : sizes)
                    num *= size;

                if (num <= MAX_ARRAY_SIZE)
                    expandMultipleDimension(node);
            }

        } else if (node instanceof PointerDataNode) {
            int size = ((PointerDataNode) node).getAllocatedSize();
            int level = PointerTypeInitiation.getLevel(getRawType(node));
            ((PointerDataNode) node).setLevel(level);
            if (((PointerDataNode) node).getLevel() > 1) {
                for (int i = 0; i < size /*&& size <= MAX_ARRAY_SIZE*/; i++)
                    generateArrayItemInPointerForMultiLevel(node.getName() + "[" + i + "]", node);
            } else {
                for (int i = 0; i < size /*&& size <= MAX_ARRAY_SIZE*/; i++) {
                    if (node instanceof PointerStructureDataNode)
                        generateArrayItemInStructureVariable(node.getName() + "[" + i + "]", node);
                    else
                        generateArrayItemInBasicVariable(node.getName() + "[" + i + "]", node);
                }
            }
        } else if (node instanceof NormalStringDataNode) {
            long size = ((NormalStringDataNode) node).getAllocatedSize();
            for (long i = 0; i < size /*&& size <= MAX_ARRAY_SIZE*/; i++)
                generateElementInString(node.getName() + "[" + i + "]", node);

        } else if (node instanceof NumberOfCallNode) {
            SubprogramNode parentSubprogram = (SubprogramNode) node.getParent();
            int repeatation = Integer.parseInt(((NumberOfCallNode) node).getValue());
            for (int i = 0; i < repeatation; i++) {
                IterationSubprogramNode iterationSubprogramNodeCall = new IterationSubprogramNode();
                iterationSubprogramNodeCall.setFunctionNode(parentSubprogram.getFunctionNode());
                iterationSubprogramNodeCall.setName(getDisplayName(parentSubprogram.getName()) + "_call" + String.valueOf(i + 1));
                iterationSubprogramNodeCall.setIndex(i + 1);
                node.addChild(iterationSubprogramNodeCall);
                iterationSubprogramNodeCall.setParent(node);
                new InitialStubTreeGen().addSubprogram(iterationSubprogramNodeCall);
            }

        } else {
//            logger.error("Does not support to expand " + node.getClass());
        }
    }
    public String getDisplayName(String name) {
        int check = 0;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '(') {
                check = i;
            }
        }
        name = name.substring(0, check);
        return name;
    }

    protected void expandPairDataNode(PairDataNode node) throws Exception {
        HashMap<String, String> pair = new HashMap<>();
        pair.put("first", node.getFirstType());
        pair.put("second", node.getSecondType());

        for (String elementName : pair.keySet()) {
            VariableNode v = new VariableNode();
            v.setName(elementName);
            v.setParent(node.getCorrespondingVar());

            String rawType = pair.get(elementName);

            v.setRawType(rawType);
            v.setReducedRawType(rawType);

            String coreType = rawType + "";

            if (TemplateUtils.isTemplateClass(rawType))
                coreType = TemplateUtils.getCoreType(rawType);
//            if (VariableTypeUtils.isTemplateClass(rawType))
//                coreType = VariableTypeUtils.getTemplateClassCoreType(rawType);
            else if (VariableTypeUtils.isMultipleDimension(rawType) || VariableTypeUtils.isOneDimension(rawType))
                coreType = rawType.replaceAll(IRegex.ARRAY_INDEX, "");
            else if (VariableTypeUtils.isPointer(rawType))
                coreType = rawType.replaceAll(IRegex.POINTER, "");

            v.setCoreType(coreType);

            new InitialTreeGen().genInitialTree(v, node);
        }
    }

    protected void expandMultipleDimension(ValueDataNode node) throws Exception {
        if (!(node instanceof MultipleDimensionDataNode))
            throw new Exception(node.getName() + " is not multiple dimension array");

        MultipleDimensionDataNode arrayDataNode = (MultipleDimensionDataNode) node;

        int[] sizes = arrayDataNode.getSizes();

        int[] indexes = new int[sizes.length];

        for (int i = 0; i < sizes.length; i++)
            indexes[i] = 0;

        while (indexes[0] < sizes[0])
            recursiveExpandMultipleArrayItemByIndex(node, sizes, indexes);
    }

    public List<String> expandArrayItemByIndex(ValueDataNode node, String input) throws Exception {
        String[] ranges = Utils.parseIndexesInput(node, input);
        String[] indexes = new String[ranges.length];

        for (int i = 0; i < ranges.length; i++) {
            String[] index = ranges[i].split(",");
            indexes[i] = index[0];
        }

        return recursiveExpandArrayItemByIndex(node, ranges, indexes);
    }

    /**
     *
     * @param element Example: "a[1]"
     * @param currentParent array data node need to expand
     * @return normal data node
     */
    protected NormalDataNode generateArrayItemInBasicVariable(String element, ValueDataNode currentParent) throws Exception {
        VariableNode v = new VariableNode();
        v.setName(element);
        v.setParent(currentParent.getCorrespondingVar());

        String rType = getRawType(currentParent);
        rType = rType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY)
                .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);

        HashMap<String, String> template = traverseMapTemplate(currentParent);

        if (template != null) {
            String coreType = template.get(rType);
            if (coreType != null)
                rType = rType.replace(rType, coreType);
        }

        v.setRawType(rType);
        v.setCoreType(rType);
        v.setReducedRawType(rType);

        // STEP 3.
        NormalDataNode child;
        if (VariableTypeUtils.isCh(rType))
            child = new NormalCharacterDataNode();
        else if (VariableTypeUtils.isNum(rType))
            child = new NormalNumberDataNode();
        else if (VariableTypeUtils.isStr(rType))
            child = new NormalStringDataNode();
        else
            throw new Exception("Cant handle " + v.getNewType());

        child.setParent(currentParent);
        child.setCorrespondingVar(v);
        child.setRawType(v.getRawType());
        child.setRealType(v.getRealType());
        child.setName(element);
        currentParent.addChild(child);

        return child;
    }

    /**
     * @param element       Example: "a[1]"
     * @param currentParent array data node need to expand
     * @return normal data node
     */
    protected NormalDataNode generateElementInString(String element, ValueDataNode currentParent) throws Exception {
        VariableNode v = new VariableNode();
        v.setName(element);
        v.setParent(currentParent.getCorrespondingVar());

        String rType = getRawType(currentParent);
        rType = VariableTypeUtils.removeRedundantKeyword(rType);
        rType = VariableTypeUtils.deleteReferenceOperator(rType);
        rType = rType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY)
                .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);

        HashMap<String, String> template = traverseMapTemplate(currentParent);

        if (template != null) {
            String coreType = template.get(rType);
            if (coreType != null)
                rType = rType.replace(rType, coreType);
        }

        // get element type
        // Ex: "u32string" -> "char32_t"
        rType = NormalStringDataNode.getStringToCharacterTypeMap().get(rType);

        if (rType != null) {
            v.setRawType(rType);
            v.setCoreType(rType);
            v.setReducedRawType(rType);

            // STEP 3.
            NormalDataNode child = new NormalCharacterDataNode();

            child.setParent(currentParent);
            child.setCorrespondingVar(v);
            child.setRawType(rType);
            child.setRealType(v.getRealType());
            child.setName(element);
            currentParent.addChild(child);
            return child;
        } else
            logger.error("Can not find the corresponding type of string type " + rType);

        return null;
    }

    private DataNode generateArrayItemInPointerForMultiLevel(String element, ValueDataNode currentParent) {
        VariableNode currentVar = currentParent.getCorrespondingVar();

        // STEP 1.
        VariableNode v = new VariableNode();
        v.setName(element);
        v.setParent(currentVar);
        v.setCorrespondingNode(currentVar.getCorrespondingNode());

        String rType = getRawType(currentParent);
        rType = rType.replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);

        if (currentParent instanceof PointerDataNode)
            //rType = rType.replaceFirst(IRegex.POINTER, SpecialCharacter.EMPTY);
            rType = rType.replaceAll(IRegex.POINTER + "\\s*$", SpecialCharacter.EMPTY);

        String coreType;
        int closeTemplatePos = rType.lastIndexOf(TemplateUtils.CLOSE_TEMPLATE_ARG);
        if (closeTemplatePos > 0)
            coreType = rType.substring(0, closeTemplatePos)
                    + rType.substring(closeTemplatePos).replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY);
        else
            coreType = rType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY);

        v.setRawType(rType);
        v.setCoreType(coreType);
        v.setReducedRawType(rType);

        // STEP 2.
        PointerDataNode child;

        if (VariableTypeUtils.isCh(coreType))
            child = new PointerCharacterDataNode();
        else if (VariableTypeUtils.isNum(coreType))
            child = new PointerNumberDataNode();
        else if (VariableTypeUtils.isStr(coreType))
            child = new PointerStringDataNode();
        else
            child = new PointerStructureDataNode();

        String simpleRawType = TemplateUtils.deleteTemplateParameters(rType);
        int level = (int) simpleRawType.chars().filter(c -> c == VariableTypeUtils.POINTER_CHAR).count();

        child.setParent(currentParent);
//        child.setType(rType);
        child.setRawType(v.getRawType());
        child.setRealType(v.getRealType());
        child.setName(element);
        child.setCorrespondingVar(v);
        child.setAllocatedSize(PointerDataNode.NULL_VALUE);
        child.setLevel(level);

        currentParent.addChild(child);

        return child;
    }

    private DataNode generateArrayItemInPointerVariable(String element, ValueDataNode currentParent) {
        VariableNode currentVar = currentParent.getCorrespondingVar();

        // STEP 1.
        VariableNode v = new VariableNode();
        v.setName(element);
        v.setParent(currentVar);

        String rType = getRawType(currentParent);
        rType = rType.replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);

        v.setRawType(rType);
        v.setCoreType(rType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY));
        v.setReducedRawType(rType);
        v.setCorrespondingNode(currentVar.getCorrespondingNode());

        // STEP 2.
        PointerDataNode child;

        if (VariableTypeUtils.isCh(rType))
            child = new PointerCharacterDataNode();
        else if (VariableTypeUtils.isNum(rType))
            child = new PointerNumberDataNode();
        else if (VariableTypeUtils.isStr(rType))
            child = new PointerStringDataNode();
        else
            child = new PointerStructureDataNode();

        String simpleRawType = TemplateUtils.deleteTemplateParameters(rType);
        int level = (int) simpleRawType.chars().filter(c -> c == VariableTypeUtils.POINTER_CHAR).count();

        child.setParent(currentParent);
        child.setCorrespondingVar(v);
//        child.setType(v.getRawType());
        child.setRawType(v.getRawType());
        child.setRealType(v.getRealType());
        child.setName(element);
        child.setAllocatedSize(PointerDataNode.NULL_VALUE);
        child.setLevel(level);

        currentParent.addChild(child);

        return child;
    }

    private DataNode generateSTLItemInArrayVariable(String element, ValueDataNode currentParent) {
        // STEP 1.
        VariableNode v = new VariableNode();
        v.setName(element);
        v.setParent(currentParent.getCorrespondingVar());

        String rType = VariableTypeUtils.deleteStorageClasses(((STLDataNode) currentParent).getArguments().get(0));

        v.setRawType(rType);
        v.setCoreType(rType);
        v.setReducedRawType(rType);

        // STEP 2.
        DataNode child = null;

        try {
            if (VariableTypeUtils.isMultipleDimension(rType)) {
                child = new MultipleDimensionTypeInitiation(v, currentParent).execute();
            } else if (VariableTypeUtils.isOneDimension(rType)) {
                child = new OneDimensionTypeInitiation(v, currentParent).execute();
            } else
                throw new Exception("cant handle");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return child;
    }

    protected DataNode generateArrayItemInStructureVariable(String element, ValueDataNode currentParent) throws Exception {
        VariableNode currentVar = currentParent.getCorrespondingVar();

        // STEP 1.
        VariableNode v = new VariableNode();
        v.setName(element);
        v.setParent(currentVar);

//        if (skipTypeResolver) {
            v.setCorrespondingNode(currentVar.getCorrespondingNode());
//        }

        String rType = getRawType(currentParent);
        if (TemplateUtils.isTemplate(rType)) {
            int index = rType.lastIndexOf(TemplateUtils.CLOSE_TEMPLATE_ARG) + 1;
            rType = rType.substring(0, index)
                    + rType.substring(index).replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY)
                    .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);
        } else
            rType = rType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY)
                    .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);

        v.setRawType(rType);
        v.setCoreType(TemplateUtils.getCoreType(rType));
//        v.setCoreType(VariableTypeUtils.getTemplateClassCoreType(rType));
        v.setReducedRawType(rType);

        // STEP 2.
        ValueDataNode child;
        child = new InitialArgTreeGen().genInitialTree(v, currentParent);

//        INode type = v.resolveCoreType();
//
//        if (TemplateUtils.isTemplateClass(rType))
//            return new TemplateClassTypeInitiation(v, currentParent).execute();
//
//        // default expand
//        if (type instanceof STLTypeNode) // STL available type
//            return new TemplateClassTypeInitiation(v, currentParent).execute();
//        else if (type instanceof EnumNode)
//            child = new EnumDataNode();
//        else if (type instanceof StructNode)
//            child = new StructDataNode();
//        else if (type instanceof ClassNode)
//            child = new ClassDataNode();
//        else if (type instanceof UnionNode)
//            child = new UnionDataNode();
//        else if (type instanceof AvailableTypeNode) {// pointer case
//            String typeDef = ((AvailableTypeNode) type).getType();
//            if (VariableTypeUtils.isCh(typeDef))
//                child = new PointerCharacterDataNode();
//            else if (VariableTypeUtils.isNum(typeDef))
//                child = new PointerNumberDataNode();
//            else if (VariableTypeUtils.isStr(typeDef))
//                child = new PointerStringDataNode();
//            else if (VariableTypeUtils.isVoidPointer(typeDef)) {
//                child = new VoidPointerDataNode();
//            } else if (VariableTypeUtils.isVoid(typeDef)) {
//                child = new VoidDataNode();
//            } else if (VariableTypeUtils.isStructureMultiLevel(typeDef)) {
//                child = new PointerStructureDataNode();
//            } else {
//                child = new OtherUnresolvedDataNode();
//            }
//
//            child.setRawType(typeDef);
//            child.setRealType(v.getRealType());
//
////            return generateArrayItemInPointerVariable(element, currentParent);
//        } else throw new Exception("Cant handle " + v.getName() + " with type " + type.getName());
//
//        child.setParent(currentParent);
//        child.setCorrespondingVar(v);
//        if (!(child instanceof PointerDataNode || child instanceof UnresolvedDataNode)) {
//            child.setRawType(v.getRawType());
////        child.setType(v.getFullType());
//            child.setRealType(v.getRealType());
//        }
//        child.setName(element);
//
//        currentParent.addChild(child);
//
//        if (type instanceof StructNode)
//            try {
//                expandTree(child);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//        }

        return child;
    }

    private void generateConstructor(VariableNode vParent, DataNode nParent) throws Exception {
        if (nParent instanceof IConstructorExpanableDataNode) {
            IConstructorExpanableDataNode currentClass = (IConstructorExpanableDataNode) nParent;
            ICommonFunctionNode constructorNode = currentClass.getSelectedConstructor();
            if (constructorNode != null) {
                ConstructorDataNode constructor = new ConstructorDataNode(constructorNode);

                List<IVariableNode> variableNodeList = constructorNode.getArguments();
                for (IVariableNode var : variableNodeList) {
                    IVariableNode finalVar = var;

                    if (TemplateUtils.isTemplateClass(vParent.getRawType())) {
                        INode vParentCoreTypeNode = vParent.resolveCoreType();
                        if (vParentCoreTypeNode instanceof ClassNode) {
                            HashMap<String, String> template = mapTemplate(vParent, (ClassNode) vParentCoreTypeNode);
//                                (ClassNode) TemplateClassDataNode.resolveCoreType(vParent));

                            if (template != null) {
                                String coreType = template.get(var.getCoreType());
                                if (coreType != null) {
                                    String rawType = var.getReducedRawType().replace(var.getCoreType(), coreType);
                                    finalVar = (VariableNode) var.clone();
                                    finalVar.setRawType(rawType);
                                    finalVar.setCoreType(coreType);
                                    finalVar.setReducedRawType(rawType);
                                }
                            }
                        }
                    }

                    new InitialArgTreeGen().genInitialTree((VariableNode) finalVar, constructor);
                }

                constructor.setCorrespondingVar(vParent);
                constructor.setName(constructorNode.getName());
//                constructor.setType(vParent.getFullType());
                constructor.setRawType(vParent.getRawType());
                constructor.setRealType(vParent.getRealType());
                constructor.setParent(nParent);
                nParent.addChild(constructor);
            }
        }
    }

    private HashMap<String, String> traverseMapTemplate(DataNode parent) {
        while (parent != null) {
            if (parent instanceof ValueDataNode) {
                if (!(parent instanceof SubprogramNode) && !(parent instanceof NumberOfCallNode)) {
                    String rawType = ((ValueDataNode) parent).getCorrespondingVar().getRawType();
                    if (TemplateUtils.isTemplateClass(rawType) && !VariableTypeUtilsForStd.isSTL(rawType)) {
                        VariableNode variableNode = ((ValueDataNode) parent).getCorrespondingVar();
                        ClassNode templateClassNode = (ClassNode) variableNode.resolveCoreType();

                        return mapTemplate(variableNode, templateClassNode);
                    }
                }
            }

            parent = (DataNode) parent.getParent();
        }

        return null;
    }

    private HashMap<String, String> mapTemplate(VariableNode variableNode, ClassNode templateClassNode) {
        if (templateClassNode != null) {
            HashMap<String, String> template;
            String[] templateParams = TemplateUtils.getTemplateParameters(templateClassNode);
            String[] templateArgs = TemplateUtils.getTemplateVariableArguments(variableNode);

            if (templateArgs != null && templateParams != null
                    && templateArgs.length == templateParams.length) {
                template = new HashMap<>();

                for (int i = 0; i < templateParams.length; i++)
                    template.put(templateParams[i], templateArgs[i]);

                return template;
            }
        }

        return null;
    }

    private void generateStructureItem(VariableNode vParent, DataNode nParent) throws Exception {
        INode correspondingNode = vParent.resolveCoreType();
        VariableNode parentVariable = vParent;

        if (nParent instanceof SubClassDataNode) {
            correspondingNode = ((ClassDataNode) nParent.getParent()).getCorrespondingType();
            parentVariable = ((ClassDataNode) nParent.getParent()).getCorrespondingVar();
        }

        StructOrClassNode childClass = (StructOrClassNode) correspondingNode;

        HashMap<String, String> template = null;

        if (TemplateUtils.isTemplateClass(parentVariable.getRawType()))
            template = mapTemplate(parentVariable, (ClassNode) childClass);
//                    (ClassNode) vParent.resolveCoreType()); //TemplateClassDataNode.resolveCoreType(vParent));

        if (template != null) {
//            INode coreType = vParent.resolveCoreType();//TemplateClassDataNode.resolveCoreType(vParent);
            if (!childClass.getChildren().isEmpty())
                childClass = (ClassNode) childClass.getChildren().get(0);
        }

        if (childClass == null)
            throw new Exception("Cant find structure node of " + vParent.getRawType());

        List<IVariableNode> attributes = childClass.getPublicAttributes();
        for (INode baseNode : childClass.getBaseNodes())
            if (baseNode instanceof StructureNode)
                attributes.addAll(((StructureNode) baseNode).getPublicAttributes());

        for (IVariableNode node : attributes) {
            IVariableNode finalVar = node;
            String coreType;

            if (template != null)
                coreType = template.get(node.getCoreType());
            else
                coreType = node.getCoreType();
                if (coreType != null) {
                    String rawType = node.getReducedRawType().replace(node.getCoreType(), coreType);
                    finalVar = (VariableNode) node.clone();
                    finalVar.getDependencies().clear();
//                    finalVar.setRawType(rawType);
////                    String coreType;
////                    if (VariableTypeUtils.isTemplateClass(rawType))
////                        coreType = VariableTypeUtils.getTemplateClassCoreType(rawType);
////                    else
////                        coreType = VariableTypeUtils.getCoreTypeWithTemplate(rawType);
//                    finalVar.setCoreType(coreType);
//                    finalVar.setReducedRawType(rawType);

                    String virtualDeclaration = rawType + " " + node.getName();
                    IASTNode ast = Utils.convertToIAST(virtualDeclaration);
                    if (ast instanceof IASTDeclarationStatement)
                        ast = ((IASTDeclarationStatement) ast).getDeclaration();
                    if (ast instanceof IASTDeclaration)
                        finalVar.setAST(ast);
                    else {
                        finalVar.setRawType(rawType);
//                    String coreType;
//                    if (VariableTypeUtils.isTemplateClass(rawType))
//                        coreType = VariableTypeUtils.getTemplateClassCoreType(rawType);
//                    else
//                        coreType = VariableTypeUtils.getCoreTypeWithTemplate(rawType);
                        finalVar.setCoreType(coreType);
                        finalVar.setReducedRawType(rawType);
                    }
                }

                new InitialTreeGen().genInitialTree((VariableNode) finalVar, nParent);
//            generateStructureItem((VariableNode) finalVar, vParent + "." + finalVar, nParent);
        }
    }

    protected ValueDataNode generateStructureItem(VariableNode vChild, String element, DataNode currentParent)
            throws Exception {
        ValueDataNode child;

        String rawType = vChild.getRealType();

        // Step: get the tail of the type
        // For example, 'A::B::C" ---> "C"
        while (rawType.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)) {
            int index = rawType.indexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) + 2;
            String temp = rawType.substring(0, index);
            if (temp.contains("<")) {
                break;
            } else {
                rawType = rawType.substring(index);
            }
        }

        // Step: Clean up raw type
        rawType = VariableTypeUtils.deleteStorageClasses(rawType);
        rawType = VariableTypeUtils.deleteStructKeyword(rawType);
        rawType = VariableTypeUtils.deleteUnionKeyword(rawType);
        rawType = VariableTypeUtils.deleteSizeFromArray(rawType);

        // Step: Check the type
        if (VariableTypeUtils.isBasic(rawType)) {
            child = new BasicTypeInitiation(vChild, currentParent).execute();

        } else if (TemplateUtils.isTemplateClass(rawType)) {
            child = new TemplateClassTypeInitiation(vChild, currentParent).execute();

        } else if (VariableTypeUtils.isMultipleDimension(rawType))
            child = new MultipleDimensionTypeInitiation(vChild, currentParent).execute();

        else if (VariableTypeUtils.isPointer(rawType)) {
            child = new PointerTypeInitiation(vChild, currentParent).execute();

        } else if (VariableTypeUtils.isOneDimension(rawType)) {
            child = new OneDimensionTypeInitiation(vChild, currentParent).execute();

        } else if (VariableTypeUtils.isStructureSimple(rawType)) {
            if (VariableTypeUtils.isEnumNode(rawType, Environment.getInstance().getProjectNode()))
                child = new EnumTypeInitiation(vChild, currentParent).execute();
            else
                child = new StructureTypeInitiation(vChild, currentParent).execute();

        } else if (VariableTypeUtils.isFunctionPointer(rawType)) {
            child = new FunctionPointerTypeInitiation(vChild, currentParent).execute();

        } else  {
            logger.error("Do not handle " + vChild.toString() + " with type " + rawType);
            throw new Exception("Do not handle " + element + " in generateStructureItem");
        }

        expandTree(child);

        return child;
    }

    private void checkOutOfBound(ValueDataNode node, String[] indexes) throws Exception {
        int indexesLen = indexes.length;
        int[] sizes = null;

        if (node instanceof PointerDataNode) {
            sizes = new int[] {((PointerDataNode) node).getAllocatedSize()};
        } else if (node instanceof OneDimensionDataNode) {
            sizes = new int[] {((OneDimensionDataNode) node).getSize()};
        } else if (node instanceof ListBaseDataNode) {
            sizes = new int[] {((ListBaseDataNode) node).getSize()};
        } else if (node instanceof MultipleDimensionDataNode) {
            sizes = ((MultipleDimensionDataNode) node).getSizes();
        }

        if (sizes != null && sizes.length == indexesLen) {
            for (int i = 0; i < indexesLen; i++) {
                int index = Integer.parseInt(indexes[i]);
                if (index >= sizes[i])
                    throw new Exception("Indexes " + Arrays.toString(indexes) + " is out of bounds " + Arrays.toString(sizes));
            }
        }
    }

    public DataNode generateArrayItem(String element, ValueDataNode parent) throws Exception {
        if (parent instanceof PointerDataNode) {
            if (((PointerDataNode) parent).getLevel() > 1) {
                return generateArrayItemInPointerForMultiLevel(element, parent);
            } else {
                if (parent instanceof PointerStructureDataNode)
                    return generateArrayItemInStructureVariable(element, parent);
                else
                    return generateArrayItemInBasicVariable(element, parent);
            }
        } else if (parent instanceof ListBaseDataNode) {
            String templateArg = ((ListBaseDataNode) parent).getTemplateArgument();

            if (TemplateUtils.isTemplateClass(templateArg))
                return generateArrayItemInStructureVariable(element, parent);
            else if (VariableTypeUtils.isBasic(templateArg))
                return generateArrayItemInBasicVariable(element, parent);
            else if (VariableTypeUtils.isStructureSimple(templateArg))
                return generateArrayItemInStructureVariable(element, parent);
            else if (VariableTypeUtils.isPointer(templateArg))
                return generateArrayItemInPointerVariable(element, parent);
            else if (VariableTypeUtils.isOneDimension(templateArg)
                    || VariableTypeUtils.isMultipleDimension(templateArg))
                return generateSTLItemInArrayVariable(element, parent);
            else
                throw new Exception("Does not support to expand " + parent.getClass());
        } else if (parent instanceof MultipleDimensionStructureDataNode || parent instanceof OneDimensionStructureDataNode)
            return generateArrayItemInStructureVariable(element, parent);
        else if (parent instanceof MultipleDimensionPointerDataNode || parent instanceof OneDimensionPointerDataNode)
            return generateArrayItemInPointerForMultiLevel(element, parent);
        else
            return generateArrayItemInBasicVariable(element, parent);
    }

    private List<String> recursiveExpandArrayItemByIndex(ValueDataNode node, String[] ranges, String[] indexes) throws Exception {
        List<String> expanded = new ArrayList<>();

        if (ranges.length != indexes.length) {
            throw new Exception("sizes and indexes have difference size");
        } else {
            int length = ranges.length;

            checkOutOfBound(node, indexes);

            StringBuilder indexesInString = new StringBuilder();
            for (String index : indexes)
                indexesInString.append("[").append(index).append("]");

            String element = node.getName() + indexesInString;

            IDataNode existNode = Search2.findNodeByName(element, node);

            if (existNode == null) {
                generateArrayItem(element, node);
            } else {
                node.getChildren().remove(existNode);
                node.getChildren().add(existNode);
            }

            expanded.add(element);

            boolean flag = true;
            for (int i = 0; i < ranges.length; i++) {
                String[] temp = ranges[i].split(",");
                String last = temp[temp.length - 1];

                if (!last.equals(indexes[i])) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                return expanded;

            String[] lastDim = ranges[length - 1].split(",");

            boolean end = false;
            if (lastDim[lastDim.length - 1].equals(indexes[length - 1])) {
                end = true;
                indexes[length - 1] = lastDim[0];
            } else {
                int pos = Arrays.asList(lastDim).indexOf(indexes[length - 1]);
                indexes[length - 1] = lastDim[pos + 1];
            }

            for (int i = indexes.length - 2; i >= 0; i--) {
                if (end) {
                    String[] dim = ranges[i].split(",");
                    int pos = Arrays.asList(dim).indexOf(indexes[i]);

                    if (pos == dim.length - 1) {
                        indexes[i] = dim[0];
                        end = true;
                    } else
                        indexes[i] = dim[pos + 1];
                }
            }

            expanded.addAll(recursiveExpandArrayItemByIndex(node, ranges, indexes));
        }

        return expanded;
    }

    private void recursiveExpandMultipleArrayItemByIndex(ValueDataNode node, int[] sizes, int[] indexes) throws Exception {
        if (sizes.length != indexes.length) {
            throw new Exception("sizes and indexes have difference size");
        } else {
            int length = sizes.length;

            StringBuilder indexesInString = new StringBuilder();
            for (int index : indexes)
                indexesInString.append("[").append(index).append("]");

            String element = node.getName() + indexesInString;

            generateArrayItem(element, node);

            indexes[length - 1]++;

            for (int i = length - 1; i > 0; i--) {
                if (indexes[i] >= sizes[i]) {
                    indexes[i] = 0;
                    indexes[i - 1]++;
                }
            }

        }
    }

    private String getRawType(ValueDataNode dataNode) {
        String rType;
        if (this.realTypeMapping != null && this.realTypeMapping.size() > 0){
            // if the data note belongs to template function
            rType = dataNode.getRealType();
        } else if (dataNode.getCorrespondingVar() != null) {
            rType = VariableTypeUtils.getRealType(dataNode.getCorrespondingVar());
//            rType = dataNode.getCorrespondingVar().getReducedRawType();
        } else
            rType = dataNode.getRealType();

        if (dataNode instanceof STLDataNode)
            rType = ((STLDataNode) dataNode).getArguments().get(0);

//        rType = VariableTypeUtils.deleteUnionKeyword(rType);
//        rType = VariableTypeUtils.deleteStructKeyword(rType);
//        rType = VariableTypeUtils.deleteStorageClasses(rType);
//        rType = VariableTypeUtils.deleteSizeFromArray(rType);

        HashMap<String, String> template = traverseMapTemplate(dataNode);

        if (template != null) {
            String coreType = template.get(rType);
            if (coreType != null)
                rType = rType.replace(rType, coreType);
        }

        return rType;
    }

    public void setRealTypeMapping(Map<String, String> realTypeMapping) {
        this.realTypeMapping = realTypeMapping;
    }

    public Map<String, String> getRealTypeMapping() {
        return realTypeMapping;
    }
}
