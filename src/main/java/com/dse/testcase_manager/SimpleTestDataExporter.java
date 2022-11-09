package com.dse.testcase_manager;

import auto_testcase_generation.testdatagen.RandomInputGeneration;
import com.dse.guifx_v3.controllers.TestCaseTreeTableController;
import com.dse.guifx_v3.objects.AbstractTableCell;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.NumberOfCallNode;
import com.dse.testdata.object.*;
import com.dse.user_code.objects.UsedParameterUserCode;
import com.dse.util.IRegex;
import com.dse.util.NodeType;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Export a tree of folders and files to json
 */
public class SimpleTestDataExporter {

    private final static AkaLogger logger = AkaLogger.get(SimpleTestDataExporter.class);

    public JsonElement exportToJsonElement(IDataTestItem testCase) {
        DataNode node = testCase.getRootDataNode();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(DataNode.class, new TestDataTreeSerializer(testCase))
                .setPrettyPrinting().create();
        return gson.toJsonTree(node, DataNode.class);
    }

    static class TestDataTreeSerializer implements JsonSerializer<DataNode> {

        // used for retrieve subprogram under test
        private final IDataTestItem testCase;

        public TestDataTreeSerializer(IDataTestItem testCase) {
            this.testCase = testCase;
        }

        @Override
        public JsonElement serialize(DataNode node, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("clazz", node.getClass().getSimpleName());
            json.addProperty("name", node.getDisplayNameInParameterTree());

            if (node instanceof ValueDataNode && !(node instanceof NumberOfCallNode)) {
                ValueDataNode valueNode = (ValueDataNode) node;

                json.addProperty("rawType", valueNode.getRawType());
                json.addProperty("realType", valueNode.getRealType());

                if (valueNode.isUseUserCode()) {
                    if (((ValueDataNode) node).getUserCode() != null) {
                        JsonObject userCode = parseUserCodeToJsonObject(
                                (UsedParameterUserCode) ((ValueDataNode) node).getUserCode());
                        json.add("userCode", userCode);
                    }
                }
            }

            if (node instanceof RootDataNode) {
                RootDataNode root = (RootDataNode) node;

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

            } else if (node instanceof NumberOfCallNode) {
                json.addProperty("value", ((NumberOfCallNode) node).getValue());

            } else if (node instanceof SubprogramNode) {
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

                if (((SubprogramNode) node).isStubable() && ((SubprogramNode) node).isStub() && !(node instanceof IterationSubprogramNode)) {
                    json.addProperty("stub", true);
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
            } else if (node instanceof NormalCharacterDataNode
                    || node instanceof NormalNumberDataNode
                    || node instanceof NormalStringDataNode) {
                NormalDataNode dataNode = (NormalDataNode) node;

                if (!(dataNode.isUseUserCode())) {
                    String value = "null";
                    if (dataNode.getValue() != null)
                        value = dataNode.getValue();
                    json.addProperty("value", value);
                }

            } else if (node instanceof PointerDataNode) {
                PointerDataNode dataNode = (PointerDataNode) node;

                if (dataNode.isSetSize())
                    json.addProperty("size", dataNode.getAllocatedSize());

            } else if (node instanceof OneDimensionDataNode) {
                OneDimensionDataNode dataNode = (OneDimensionDataNode) node;

                if (dataNode.isSetSize())
                    json.addProperty("size", dataNode.getSize());

            } else if (node instanceof MultipleDimensionDataNode) {
                MultipleDimensionDataNode dataNode = (MultipleDimensionDataNode) node;
                json.addProperty("size", dataNode.getSizes()[0]);

            } else if (node instanceof SubClassDataNode) {
                SubClassDataNode dataNode = (SubClassDataNode) node;
                ConstructorDataNode constructorDataNode = dataNode.getConstructorDataNode();
                if (constructorDataNode != null) {
                    String constructor = constructorDataNode.getName();
                    json.addProperty("selectedConstructor", constructor);
                }

            } else if (node instanceof ClassDataNode) {
                ClassDataNode dataNode = (ClassDataNode) node;
                json.addProperty("subclass", dataNode.getSubClass().getName());

            } else if (node instanceof EnumDataNode) {
                EnumDataNode dataNode = (EnumDataNode) node;

                if (!dataNode.isUseUserCode()) {
                    json.addProperty("value", dataNode.getValue());
                }

            } else if (node instanceof UnionDataNode) {
                UnionDataNode dataNode = (UnionDataNode) node;

                if (dataNode.getSelectedField() != null) {
                    json.addProperty("selectedField", dataNode.getSelectedField());
                }

            } else if (node instanceof FunctionPointerDataNode) {
                FunctionPointerDataNode dataNode = (FunctionPointerDataNode) node;

                INode selectedFunction = dataNode.getSelectedFunction();
                if (selectedFunction != null) {
                    String reference = selectedFunction.getName();
                    json.addProperty("reference", reference);
                }

            } else if (node instanceof VoidPointerDataNode) {
                VoidPointerDataNode dataNode = (VoidPointerDataNode) node;

                String refType = dataNode.getReferenceType();
                if (refType != null) {
                    String selectedCoreType = refType.replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY);
                    String category = VariableTypeUtils.isBasic(selectedCoreType)
                            ? AbstractTableCell.OPTION_VOID_POINTER_PRIMITIVE_TYPES
                            : AbstractTableCell.OPTION_VOID_POINTER_STRUCTURE_TYPES;
                    long level = refType.chars().filter(c -> c == '*').count();
                    String value = String.format("%s=%s,%s=%s,%s=%s",
                            RandomInputGeneration.VOID_POINTER____SELECTED_CORE_TYPE, selectedCoreType,
                            RandomInputGeneration.VOID_POINTER____SELECTED_CATEGORY, category,
                            RandomInputGeneration.VOID_POINTER____POINTER_LEVEL, level);
                    json.addProperty("inputMethod", value);
                }

            }

            // add children
            if (node.getChildren().size() > 0) {
                JsonArray childrenArr = new JsonArray();
                for (IDataNode child : node.getChildren())
                    childrenArr.add(jsonSerializationContext.serialize(child, DataNode.class));
                json.add("children", childrenArr);
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
