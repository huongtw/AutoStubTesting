package com.dse.testcase_manager;

import auto_testcase_generation.testdatagen.VoidPtrTypeResolver;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.controllers.TestCaseTreeTableController;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.testdata.InputCellHandler;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.*;
import com.dse.user_code.objects.UsedParameterUserCode;
import com.dse.util.*;
import com.google.gson.*;

import java.util.*;

public class SimpleTestDataImporter {
    private final static AkaLogger logger = AkaLogger.get(SimpleTestDataImporter.class);

    private final IDataTestItem testCase;

    private final InputCellHandler inputCellHandler;


    public SimpleTestDataImporter(IDataTestItem testCase) {
        this.testCase = testCase;
        inputCellHandler = new InputCellHandler();
        inputCellHandler.setInAutoGenMode(true);
        Map<String, String> realTypeMapping = new VoidPtrTypeResolver(testCase.getFunctionNode()).getTypeMap();
        inputCellHandler.setRealTypeMapping(realTypeMapping);
        inputCellHandler.setTestCase(testCase);
    }

    public void importNode(IDataNode node, JsonObject jsonObject) {
        ValueDataNode dataNode = null;

        try {
            String rawType = "";
            String realType = "";

            if (node instanceof ValueDataNode) {
                dataNode = (ValueDataNode) node;

                JsonElement tempDataTypeJsonElement = jsonObject.get("rawType");
                if (tempDataTypeJsonElement != null) {
                    rawType = tempDataTypeJsonElement.getAsString();
                }

                JsonElement tempRealTypeJsonElement = jsonObject.get("realType");
                if (tempDataTypeJsonElement != null) {
                    realType = tempRealTypeJsonElement.getAsString();
                }

                if (jsonObject.get("userCode") != null) {
                    JsonObject userCodeJsonObject = (JsonObject) jsonObject.get("userCode");
                    UsedParameterUserCode userCode = getUserCodeFromJsonObject(userCodeJsonObject);
                    dataNode.setUserCode(userCode);
                    dataNode.setUseUserCode(true);
                    testCase.putOrUpdateDataNodeIncludes(dataNode);
                }
            }

            if (dataNode instanceof NumberOfCallNode) {
                String value = jsonObject.get("value").getAsString();
                if (!value.equals("null")) {
                    inputCellHandler.commitEdit(dataNode, value);
                }
            } else if (dataNode instanceof NormalCharacterDataNode
                    || dataNode instanceof NormalStringDataNode
                    || dataNode instanceof NormalNumberDataNode) {

                if (!dataNode.isUseUserCode()) {
                    String value = jsonObject.get("value").getAsString();
                    if (!value.equals("null"))
                        inputCellHandler.commitEdit(dataNode, value);
                }

            } else if (dataNode instanceof PointerDataNode) {
                if (jsonObject.has("size")) {
                    String size = jsonObject.get("size").getAsString();
                    inputCellHandler.commitEdit(dataNode, size);
                }
            } else if (dataNode instanceof OneDimensionDataNode) {
                if (jsonObject.has("size")) {
                    String size = jsonObject.get("size").getAsString();
                    inputCellHandler.commitEdit(dataNode, size);
                }
            } else if (dataNode instanceof MultipleDimensionDataNode) {
                if (jsonObject.get("size") != null) {
                    String sizesInString = jsonObject.get("size").getAsString();
                    inputCellHandler.commitEdit(dataNode, sizesInString);
                }

            } else if (dataNode instanceof SubprogramNode) { // need to set function node for it
                if (dataNode instanceof MacroSubprogramDataNode
                        || dataNode instanceof TemplateSubprogramDataNode) {
                    dataNode.setRawType(rawType);
                    dataNode.setRealType(realType);
                }

                if (((SubprogramNode) dataNode).isStubable()
                        && !(node instanceof IterationSubprogramNode)
                        && jsonObject.has("stub")) {
                    boolean stub = jsonObject.get("stub").getAsBoolean();
                    if (stub) {
                        NumberOfCallNode numberOfCall = new NumberOfCallNode("Number of calls");
                        dataNode.addChild(numberOfCall);
                        numberOfCall.setParent(dataNode);
                    }
                }

                if (dataNode instanceof TemplateSubprogramDataNode) {
                    // load the template parameters of template functions
                    if (jsonObject.get(TemplateSubprogramDataNode.NAME_TEMPLATE_TYPE) != null) {
                        JsonArray template_types = jsonObject.getAsJsonArray(TemplateSubprogramDataNode.NAME_TEMPLATE_TYPE);
                        Map<String, String> realTypeMapping = new HashMap<>();
                        for (JsonElement element : template_types) {
                            realTypeMapping.put(element.getAsString().split("->")[0],
                                    element.getAsString().split("->")[1]);
                        }
                        ((TemplateSubprogramDataNode) dataNode).setRealTypeMapping(realTypeMapping);
                    }
                }

                if (dataNode instanceof MacroSubprogramDataNode) {
                    // load the template parameters of macro functions
                    if (jsonObject.get(MacroSubprogramDataNode.NAME_MACRO_TYPE) != null) {
                        JsonArray macro_types = jsonObject.getAsJsonArray(MacroSubprogramDataNode.NAME_MACRO_TYPE);
                        Map<String, String> realTypeMapping = new HashMap<>();
                        for (JsonElement element : macro_types) {
                            realTypeMapping.put(element.getAsString().split("->")[0],
                                    element.getAsString().split("->")[1]);
                        }
                        ((MacroSubprogramDataNode) dataNode).setRealTypeMapping(realTypeMapping);
                    }
                }

            } else if (dataNode instanceof SubClassDataNode) {
                if (jsonObject.get("selectedConstructor") != null) {
                    String constructorName = jsonObject.get("selectedConstructor").getAsString();
                    if (constructorName != null)
                        inputCellHandler.commitEdit(dataNode, constructorName);
                }
            } else if (dataNode instanceof ClassDataNode) {
                if (!jsonObject.has("subclass")) {
                    String subClass = jsonObject.get("subclass").getAsString();
                    inputCellHandler.commitEdit(dataNode, subClass);
                }
            } else if (dataNode instanceof EnumDataNode) {
                if (jsonObject.get("value") != null) {
                    String value = jsonObject.get("value").getAsString();
                    inputCellHandler.commitEdit(dataNode, value);
                }
            } else if (dataNode instanceof UnionDataNode) {
                if (jsonObject.get("selectedField") != null) {
                    String field = jsonObject.get("selectedField").getAsString();
                    inputCellHandler.commitEdit(dataNode, field);
                }
            } else if (dataNode instanceof FunctionPointerDataNode) {
                if (jsonObject.get("reference") != null) {
                    String reference = jsonObject.get("reference").getAsString();
                    inputCellHandler.commitEdit(dataNode, reference);
                }
            } else if (dataNode instanceof VoidPointerDataNode) {
                JsonElement jsonElement = jsonObject.get("inputMethod");
                if (jsonElement != null) {
                    String im = jsonElement.getAsString();
                    inputCellHandler.commitEdit(dataNode, im);
                }
            }

            // load children
            if (jsonObject.get("children") != null) {
                for (JsonElement child : jsonObject.get("children").getAsJsonArray()) {
                    JsonObject jsonObjectChild = child.getAsJsonObject();
                    String childName = jsonObjectChild.get("name").getAsString();
                    for (IDataNode childNode : node.getChildren()) {
                        if (childNode.getDisplayNameInParameterTree().equals(childName)) {
                            importNode(childNode, jsonObjectChild);
                        }
                    }
                }
            }

            // if dataNode is sut, load expected outputs of parameters
            if (dataNode instanceof SubprogramNode) {
                if (jsonObject.get("paramExpectedOuputs") != null) {
                    for (JsonElement eo : jsonObject.get("paramExpectedOuputs").getAsJsonArray()) {
                        JsonObject jsonObjectChild = eo.getAsJsonObject();
                        String childName = jsonObjectChild.get("name").getAsString();
                        for (IDataNode childNode : ((SubprogramNode) dataNode).getParamExpectedOuputs()) {
                            if (childNode.getDisplayNameInParameterTree().equals(childName)) {
                                importNode(childNode, jsonObjectChild);
                            }
                        }
                    }
                }
            } else if (node instanceof GlobalRootDataNode && ((RootDataNode) node).getLevel().equals(NodeType.GLOBAL)) {
                if (jsonObject.get("paramExpectedOuputs") != null) {
                    for (JsonElement eo : jsonObject.get("paramExpectedOuputs").getAsJsonArray()) {
                        JsonObject jsonObjectChild = eo.getAsJsonObject();
                        String childName = jsonObjectChild.get("name").getAsString();
                        for (IDataNode childNode : ((GlobalRootDataNode) node).getGlobalInputExpOutputMap().values()) {
                            if (childNode.getDisplayNameInParameterTree().equals(childName)) {
                                importNode(childNode, jsonObjectChild);
                            }
                        }
                    }
                }
            }

        } catch (FunctionNodeNotFoundException fe) {
            logger.debug("function node not found: " + fe.getFunctionPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private UsedParameterUserCode getUserCodeFromJsonObject(JsonObject jsonObject) {
        return getUserCodeFromJsonObject(jsonObject, UsedParameterUserCode.class);
    }

    private <T extends UsedParameterUserCode> T getUserCodeFromJsonObject(JsonObject jsonObject, Class<? extends UsedParameterUserCode> clazz) {
        UsedParameterUserCode userCode = null;

        try {
            userCode = clazz.newInstance();

            if (jsonObject.get("type") != null && jsonObject.get("type").getAsString().equals(UsedParameterUserCode.TYPE_CODE)) {
                // type, content, include paths
                userCode.setType(UsedParameterUserCode.TYPE_CODE);
                userCode.setContent(jsonObject.get("content").getAsString());
                JsonArray includePaths = jsonObject.get("includePaths").getAsJsonArray();
                for (JsonElement element : includePaths) {
                    userCode.getIncludePaths().add(element.getAsString());
                }
    //        } else if (jsonObject.get("type").getAsString().equals(UsedDefineArgumentUserCode.TYPE_REFERENCE)) {
            } else { // UsedDefineArgumentUserCode.TYPE_REFERENCE
                // get the user code has correspond id
                int id = jsonObject.get("id").getAsInt();
                userCode.setType(UsedParameterUserCode.TYPE_REFERENCE);
                userCode.setId(id);
            }

        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return (T) userCode;
    }
}
