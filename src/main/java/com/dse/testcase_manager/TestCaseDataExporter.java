package com.dse.testcase_manager;

import com.dse.parser.object.DefinitionFunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.NumberOfCallNode;
import com.dse.testdata.Iterator;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.*;
import com.dse.testdata.object.GlobalRootDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.stl.*;
import com.dse.user_code.objects.UsedParameterUserCode;
import com.dse.logger.AkaLogger;
import com.dse.util.NodeType;
import com.dse.util.PathUtils;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Export a tree of folders and files to json
 */
public class TestCaseDataExporter {
    private final static AkaLogger logger = AkaLogger.get(TestCaseDataExporter.class);

    public TestCaseDataExporter() {

    }

//    public String export(DataNode node) {
//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder
//                .registerTypeAdapter(DataNode.class, new FunctionDetailTreeSerializer())
//                .setPrettyPrinting().create();
//        return gson.toJson(node, DataNode.class);
//    }
//
//    public JsonElement exportToJsonElement(DataNode node) {
//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder
//                .registerTypeAdapter(DataNode.class, new FunctionDetailTreeSerializer())
//                .setPrettyPrinting().create();
//        return gson.toJsonTree(node, DataNode.class);
//    }
//
//    public void export(File path, DataNode node) {
//        String json = export(node);
//        System.out.println(json);
//        Utils.writeContentToFile(json, path.getAbsolutePath());
//    }

    public JsonElement exportToJsonElement(IDataTestItem testCase) {
        DataNode node = testCase.getRootDataNode();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(DataNode.class, new FunctionDetailTreeSerializer(testCase))
                .setPrettyPrinting().create();
        return gson.toJsonTree(node, DataNode.class);
    }

    static class FunctionDetailTreeSerializer implements JsonSerializer<DataNode> {

        // used for retrieve subprogram under test
        private IDataTestItem testCase;

        public FunctionDetailTreeSerializer(IDataTestItem testCase) {
            this.testCase = testCase;
        }

        @Override
        public JsonElement serialize(DataNode node, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("type", node.getClass().getName());
            json.addProperty("name", node.getName());
            json.addProperty("virtual_name", node.getVituralName());

            if (node instanceof ValueDataNode || node instanceof NumberOfCallNode) {
                ValueDataNode valueNode = (ValueDataNode) node;

                json.addProperty("dataType", valueNode.getRawType());
                json.addProperty("realType", valueNode.getRealType());

                if (!(node instanceof SubprogramNode)) {
                    json.addProperty("external", valueNode.isExternel());
                    json.addProperty("assertMethod", valueNode.getAssertMethod());

                    if (AssertMethod.USER_CODE.equals(valueNode.getAssertMethod())
                            && valueNode.getAssertUserCode() != null) {
                        JsonObject assertUserCode = parseUserCodeToJsonObject(valueNode.getAssertUserCode());
                        json.add("assertUserCode", assertUserCode);
                    }
                }

                // Xử lí trường hợp riêng cho NumberOfCallNode
                if (node instanceof NumberOfCallNode) {
                    ((NumberOfCallNode) node).setRawType("int");
                    ((NumberOfCallNode) node).setRealType("int");
                    json.addProperty("value", ((NumberOfCallNode) node).getValue());
                    json.addProperty("dataType", valueNode.getRawType());
                    json.addProperty("realType", valueNode.getRealType());
                }

                if (((ValueDataNode) node).isUseUserCode()) {
                    if (((ValueDataNode) node).getUserCode() != null) {
                        JsonObject userCode = parseUserCodeToJsonObject(
                                (UsedParameterUserCode) ((ValueDataNode) node).getUserCode());
                        json.add("userCode", userCode);
                    }
                }
            }

            if (node instanceof RootDataNode) {
                RootDataNode root = (RootDataNode) node;

                if (root.getFunctionNode() != null) {
                    String relativePath = PathUtils.toRelative(root.getFunctionNode().getAbsolutePath());
                    json.addProperty("functionNode", relativePath);
                }

                if (root instanceof GlobalRootDataNode
                        && root.getLevel().equals(NodeType.GLOBAL)) {
                    GlobalRootDataNode globalRoot = (GlobalRootDataNode) root;
                    JsonArray eoArray = new JsonArray();
                    if (globalRoot.getGlobalInputExpOutputMap() == null) {
                        logger.debug("GlobalInputExpOutputMap equals null.");
                    } else {
                        for (IDataNode eoDataNode : globalRoot.getGlobalInputExpOutputMap().values()) {
                            eoArray.add(jsonSerializationContext.serialize(eoDataNode, DataNode.class));
                        }
                    }
                    json.add("paramExpectedOuputs", eoArray);
                }

                json.addProperty("level", root.getLevel().name());

            } else if (node instanceof UnitNode) {
                UnitNode unit = (UnitNode) node;
                String relativePath = PathUtils.toRelative(unit.getSourceNode().getAbsolutePath());
                json.addProperty("sourceNode", relativePath);
//
            } else if (node instanceof ConstructorDataNode) {
                INode functionNode = ((ConstructorDataNode) node).getFunctionNode();

                String path = "";

                if (functionNode != null) {
                    if (node.getParent() instanceof SmartPointerDataNode) {
                        if (functionNode instanceof DefinitionFunctionNode) {
                            path = ((DefinitionFunctionNode) functionNode).getAST().getRawSignature();
                            String relativePath = PathUtils.toRelative(((SmartPointerDataNode) node.getParent())
                                    .getCorrespondingVar().getAbsolutePath());
                            json.addProperty("pointer_path", relativePath);
                        }

                    } else {
                        path = functionNode.getAbsolutePath();
                        path = PathUtils.toRelative(path);
                    }
                }

                json.addProperty("functionNode", path);

            } else if (node instanceof SubprogramNode) {
                String path = PathUtils.toRelative(((SubprogramNode) node).getFunctionNode().getAbsolutePath());
                json.addProperty("functionNode", path);

                if (node instanceof TemplateSubprogramDataNode) {
                    JsonArray realTypesJson = new JsonArray();

                    // if the function is template, we should store the defined type of template parameters
                    Map<String, String> realTypeMapping = ((TemplateSubprogramDataNode) node).getRealTypeMapping();
                    for (String key : realTypeMapping.keySet()) {
                        realTypesJson.add(key + "->" + realTypeMapping.get(key));
                    }
                    json.add(TemplateSubprogramDataNode.NAME_TEMPLATE_TYPE, realTypesJson);

                } else if (node instanceof MacroSubprogramDataNode) {
                    JsonArray realTypesJson = new JsonArray();

                    // if the function is template, we should store the defined type of template parameters
                    Map<String, String> realTypeMapping = ((MacroSubprogramDataNode) node).getRealTypeMapping();
                    for (String key : realTypeMapping.keySet()) {
                        realTypesJson.add(key + "->" + realTypeMapping.get(key));
                    }
                    json.add(MacroSubprogramDataNode.NAME_MACRO_TYPE, realTypesJson);
                }

                // if SubprogramNode is subprogram under test
                // then export Expected Output if not existed yet
                ICommonFunctionNode sut = testCase.getFunctionNode();
                if (sut.equals(((SubprogramNode) node).getFunctionNode())) {
                    if (node.getChildren().size() > 0) {
                        JsonArray eoArray = new JsonArray();
                        for (IDataNode eoDataNode : ((SubprogramNode) node).getParamExpectedOuputs()) {
                            eoArray.add(jsonSerializationContext.serialize(eoDataNode, DataNode.class));
                        }
                        json.add("paramExpectedOuputs", eoArray);
                    }
                }

                //Xử lí riêng cho IterationSubprogramNode
                if (node instanceof IterationSubprogramNode) {
                    json.addProperty("index", String.valueOf(((IterationSubprogramNode) node).getIndex()));
                }

            } else if (node instanceof NormalCharacterDataNode
                    || node instanceof NormalNumberDataNode
                    || node instanceof NormalStringDataNode) {
                NormalDataNode dataNode = (NormalDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                if (dataNode.getCorrespondingType() != null) {
                    json.addProperty("correspondingType", dataNode.getRawType());
                }

                if (!((NormalDataNode) node).isUseUserCode()) {
                    String value = "null";
                    if (dataNode.getValue() != null)
                        value = ((NormalDataNode) node).getValue();

                    json.addProperty("value", value);
                }

            } else if (node instanceof PointerDataNode) {
                PointerDataNode dataNode = (PointerDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                if (dataNode.getCorrespondingType() != null) {
                    String relativePath = PathUtils.toRelative(dataNode.getCorrespondingType().getAbsolutePath());
                    json.addProperty("correspondingType", relativePath);
                }

//                if (!((PointerDataNode) node).isUseUserCode()) {
                json.addProperty("level", dataNode.getLevel());

                if (dataNode.isSetSize())
                    json.addProperty("size", dataNode.getAllocatedSize());
//                }

            } else if (node instanceof OneDimensionDataNode) {
                OneDimensionDataNode dataNode = (OneDimensionDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                if (dataNode.getCorrespondingType() != null) {
                    String relativePath = PathUtils.toRelative(dataNode.getCorrespondingType().getAbsolutePath());
                    json.addProperty("correspondingType", relativePath);
                }

                json.addProperty("fixedSize", dataNode.isFixedSize());

                if (((OneDimensionDataNode) node).isSetSize())
                    json.addProperty("size", ((OneDimensionDataNode) node).getSize());

            } else if (node instanceof MultipleDimensionDataNode) {
                MultipleDimensionDataNode dataNode = (MultipleDimensionDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);
                if (dataNode.getCorrespondingType() != null) {
                    String relativePath = PathUtils.toRelative(dataNode.getCorrespondingType().getAbsolutePath());
                    json.addProperty("correspondingType", relativePath);
                }

                json.addProperty("dimensions", dataNode.getDimensions());
                json.addProperty("fixedSize", dataNode.isFixedSize());

//                if (dataNode.isSetSize()) {
                    int[] sizes = dataNode.getSizes();
                    StringBuilder sizesInString = new StringBuilder();
                    for (int i = 0; i < sizes.length - 1; i++)
                        sizesInString.append(sizes[i]).append(", ");
                    sizesInString.append(sizes[sizes.length - 1]);

                    json.addProperty("size", sizesInString.toString());
//                }

            } else if (node instanceof SubClassDataNode) {
//                if (!((SubClassDataNode) node).isUseUserCode()) {
                SubClassDataNode dataNode = (SubClassDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                INode correspondingType = dataNode.getCorrespondingType();

                String typeRelativePath = PathUtils.toRelative(correspondingType.getAbsolutePath());
                json.addProperty("correspondingType", typeRelativePath);

                json.addProperty("rawType", dataNode.getRawType());

                ConstructorDataNode constructorDataNode = dataNode.getConstructorDataNode();
                if (constructorDataNode != null) {
                    String constructor = constructorDataNode.getName();
                    json.addProperty("selectedConstructor", constructor);
                    String variableType = dataNode.getRawType();
                    json.addProperty("variableType", variableType);
                }
//                }

            } else if (node instanceof ClassDataNode) {
//                if (!((ClassDataNode) node).isUseUserCode()) {
                ClassDataNode dataNode = (ClassDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                INode correspondingType = dataNode.getCorrespondingType();
                String typeRelativePath = PathUtils.toRelative(correspondingType.getAbsolutePath());
                json.addProperty("correspondingType", typeRelativePath);
//                }

            } else if (node instanceof EnumDataNode) {
                EnumDataNode dataNode = (EnumDataNode) node;

                if (!((EnumDataNode) node).isUseUserCode()) {
                    json.addProperty("value", dataNode.getValue());
                    json.addProperty("valueIsSet", dataNode.isSetValue());
                }

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                INode correspondingType = dataNode.getCorrespondingType();
                String typeRelativePath = PathUtils.toRelative(correspondingType.getAbsolutePath());
                json.addProperty("correspondingType", typeRelativePath);

            } else if (node instanceof UnionDataNode) {
//                if (!((UnionDataNode) node).isUseUserCode()) {
                UnionDataNode dataNode = (UnionDataNode) node;

                if (dataNode.getSelectedField() != null) {
                    json.addProperty("selectedField", dataNode.getSelectedField());
                }

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                INode correspondingType = dataNode.getCorrespondingType();
                String typeRelativePath = PathUtils.toRelative(correspondingType.getAbsolutePath());
                json.addProperty("correspondingType", typeRelativePath);
//                }

            } else if (node instanceof StructDataNode) {
//                if (!((StructDataNode) node).isUseUserCode()) {
                StructDataNode dataNode = (StructDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                INode correspondingType = dataNode.getCorrespondingType();
                String typeRelativePath = PathUtils.toRelative(correspondingType.getAbsolutePath());
                json.addProperty("correspondingType", typeRelativePath);
//                }

            } else if (node instanceof STLDataNode) {
//                if (!((STLDataNode) node).isUseUserCode()) {
                STLDataNode dataNode = (STLDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                json.addProperty("correspondingType", "STLListBaseType");

                if (dataNode instanceof StdFunctionDataNode) {
                    StdFunctionDataNode lambdaDataNode = (StdFunctionDataNode) dataNode;
                    json.addProperty("templateArg", lambdaDataNode.getTemplateArgument());
//                    JsonObject userCode = parseUserCodeToJsonObject(
//                            (UsedParameterUserCode) lambdaDataNode.getUserCode());
//                    json.add("userCode", userCode);
                } else if (dataNode instanceof ListBaseDataNode) {
                    ListBaseDataNode listBaseDataNode = (ListBaseDataNode) dataNode;
                    json.addProperty("templateArg", listBaseDataNode.getTemplateArgument());

                    if (listBaseDataNode.isSetSize()) {
                        json.addProperty("size", listBaseDataNode.getSize());
                    }

                } else if (dataNode instanceof PairDataNode) {
                    PairDataNode pairDataNode = (PairDataNode) dataNode;

                    JsonObject templateArg = new JsonObject();
                    templateArg.addProperty("first", pairDataNode.getFirstType());
                    templateArg.addProperty("second", pairDataNode.getSecondType());
                    json.add("templateArg", templateArg);

                } else if (dataNode instanceof SmartPointerDataNode) {
                    SmartPointerDataNode smartPointerDataNode = (SmartPointerDataNode) dataNode;
                    json.addProperty("templateArg", smartPointerDataNode.getTemplateArgument());

                } else if (dataNode instanceof DefaultDeleteDataNode) {
                    DefaultDeleteDataNode deleteDataNode = (DefaultDeleteDataNode) dataNode;

                    List<String> arguments = deleteDataNode.getArguments();

                    if (arguments != null && !arguments.isEmpty())
                        json.addProperty("templateArg", arguments.get(0));

                } else if (dataNode instanceof AllocatorDataNode) {
                    AllocatorDataNode allocatorDataNode = (AllocatorDataNode) dataNode;

                    List<String> arguments = allocatorDataNode.getArguments();

                    if (arguments != null && !arguments.isEmpty())
                        json.addProperty("templateArg", arguments.get(0));
                }
//                }

            } else if (node instanceof FunctionPointerDataNode) {
//                if (!((FunctionPointerDataNode) node).isUseUserCode()) {
                FunctionPointerDataNode dataNode = (FunctionPointerDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                INode selectedFunction = dataNode.getSelectedFunction();
                if (selectedFunction != null) {
                    String reference = PathUtils.toRelative(selectedFunction.getAbsolutePath());
                    json.addProperty("reference", reference);
                }
//                }

            } else if (node instanceof VoidPointerDataNode) {
//                if (!((VoidPointerDataNode) node).isUseUserCode()) {
                VoidPointerDataNode dataNode = (VoidPointerDataNode) node;
                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                if (dataNode.getReferenceType() != null) {
                    json.addProperty("referType", dataNode.getReferenceType());
                }

                if (dataNode.getInputMethod() != null)
                    json.addProperty("inputMethod", dataNode.getInputMethod().toString());
//                if (dataNode.getInputMethod() == VoidPointerDataNode.InputMethod.USER_CODE) {
//                    if (dataNode.getUserCode() != null) {
//                        JsonObject userCode = parseUserCodeToJsonObject(
//                                (UsedParameterUserCode) dataNode.getUserCode());
//                        json.add("userCode", userCode);
//                    }
//                } else if (dataNode.getInputMethod() == VoidPointerDataNode.InputMethod.AVAILABLE_TYPES) {
                if (dataNode.getInputMethod() == VoidPointerDataNode.InputMethod.AVAILABLE_TYPES) {
                    List<String> includes = testCase.getAdditionalIncludePathsMap().get(dataNode);
                    if (includes != null && !includes.isEmpty()) {
                        String cloneFilePath = includes.get(0);
                        String relative = PathUtils.toRelative(cloneFilePath);
                        json.addProperty("referTypeInclude", relative);
                    }
                }
//                }

            } else if (node instanceof OtherUnresolvedDataNode) {
//                if (!((OtherUnresolvedDataNode) node).isUseUserCode()) {
                OtherUnresolvedDataNode dataNode = (OtherUnresolvedDataNode) node;

                String varRelativePath = PathUtils.toRelative(dataNode.getCorrespondingVar().getAbsolutePath());
                json.addProperty("correspondingVar", varRelativePath);

                //TODO: Lamnt fix instance of
//                if (dataNode.getUserCode() instanceof UsedParameterUserCode) {
//                    JsonObject userCode = parseUserCodeToJsonObject(
//                            (UsedParameterUserCode) dataNode.getUserCode());
//                    json.add("userCode", userCode);
//                }
//                }

            } else {
                logger.error("Do not support to export the value of node \"" + node.getName() + "\", class = \"" + node.getClass() + "\" to json file in this version");
            }

            // add children
            if (node.getChildren().size() > 0) {
                JsonArray childrenArr = new JsonArray();
                for (IDataNode child : node.getChildren())
                    childrenArr.add(jsonSerializationContext.serialize(child, DataNode.class));
                json.add("children", childrenArr);
            }

            if (node instanceof ValueDataNode && ((ValueDataNode) node).isStubArgument()) {
                ValueDataNode valueNode = (ValueDataNode) node;

                if (valueNode.getIterators().get(0).getDataNode() == valueNode) {
                    JsonArray iterators = new JsonArray();

                    for (Iterator iterator : valueNode.getIterators()) {
                        JsonObject iteratorJsonObj = new JsonObject();
                        iteratorJsonObj.addProperty("start", iterator.getStartIdx());
                        iteratorJsonObj.addProperty("repeat", iterator.getRepeat());

                        if (iterator.getDataNode() != node) {
                            JsonElement dataNodeJsonObj = jsonSerializationContext
                                    .serialize(iterator.getDataNode(), DataNode.class);
                            iteratorJsonObj.add("dataNode", dataNodeJsonObj);
                        }

                        iterators.add(iteratorJsonObj);
                    }

                    json.add("iterators", iterators);
                }
            }

            return json;
        }

        private JsonObject parseUserCodeToJsonObject(UsedParameterUserCode userCode) {
            JsonObject jsonObject = new JsonObject();
            if (userCode.getType().equals(UsedParameterUserCode.TYPE_CODE)) {
                // export type, content, includePaths
                JsonArray includePaths = new JsonArray();
                for (String path : userCode.getIncludePaths()) {
                    includePaths.add(path);
                }
                jsonObject.add("includePaths", includePaths);
                jsonObject.addProperty("content", userCode.getContent());
                jsonObject.addProperty("type", userCode.getType());

            } else if (userCode.getType().equals(UsedParameterUserCode.TYPE_REFERENCE)) {
                // export id of the reference user code
                jsonObject.addProperty("type", userCode.getType());
                jsonObject.addProperty("id", userCode.getId());
            }

            return jsonObject;
        }
    }
}
