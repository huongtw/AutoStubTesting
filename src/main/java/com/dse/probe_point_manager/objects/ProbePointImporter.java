package com.dse.probe_point_manager.objects;

import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.SourcecodeFileNode;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.logger.AkaLogger;
import com.google.gson.*;

public class ProbePointImporter {

    private final static AkaLogger logger = AkaLogger.get(ProbePointImporter.class);

    public ProbePoint importProbePoint(JsonObject jsonObject) {
        JsonDeserializer<ProbePoint> deserializer = ((jsonElement, type, jsonDeserializationContext) -> {
            ProbePoint probePoint = new ProbePoint();

            try {
                String name = jsonObject.get("name").getAsString();
                String before = jsonObject.get("before").getAsString();
                String content = jsonObject.get("content").getAsString();
                String after = jsonObject.get("after").getAsString();
                String sourceCodeFileNodePath = jsonObject.get("sourceCodeFileNode").getAsString();
                String lineInSourceCodeFile = jsonObject.get("lineInSourceCodeFile").getAsString();
                String functionNodePath = jsonObject.get("functionNode").getAsString();
                String path = jsonObject.get("path").getAsString();
                String lineInFunction = jsonObject.get("lineInFunction").getAsString();
                JsonArray testCaseNames = jsonObject.get("testCases").getAsJsonArray();
                boolean isValid = jsonObject.get("isValid").getAsBoolean();

                probePoint.setName(name);
                probePoint.setBefore(before);
                probePoint.setContent(content);
                probePoint.setAfter(after);
                probePoint.setPath(path);
                probePoint.setLineInSourceCodeFile(Integer.parseInt(lineInSourceCodeFile));
                probePoint.setLineInFunctionNode(Integer.parseInt(lineInFunction));
                probePoint.setValid(isValid);

                for (JsonElement tcName : testCaseNames) {
                    TestCase testCase = TestCaseManager.getBasicTestCaseByName(tcName.getAsString());
                    if (testCase != null) probePoint.getTestCases().add(testCase);
                }

                IFunctionNode iFunctionNode = (IFunctionNode) UIController.searchFunctionNodeByPath(functionNodePath);
                probePoint.setFunctionNode(iFunctionNode);
                ISourcecodeFileNode sourcecodeFileNode = searchSourceCodeFileNodeByPath(sourceCodeFileNodePath);
                if (sourcecodeFileNode != null) {
                    probePoint.setSourcecodeFileNode(sourcecodeFileNode);
                } else {
                    logger.debug("Can not search SourceCodeFileNode when import ProbePoint");
                    probePoint.setValid(false);
                    return probePoint;
                }

            } catch (FunctionNodeNotFoundException fe) {
                UIController.showInformationDialog("Can not find function "
                        + fe.getFunctionPath() + " when load probePoint " + probePoint.getName(),
                        "Function Node not found",
                        "Function Node not found");
                logger.debug("Function Node Not Found: " + fe.getFunctionPath());
                probePoint.setValid(false);
                return probePoint;
            } catch (NullPointerException ne) {
                logger.debug("Null pointer exception");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return probePoint;
        });

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ProbePoint.class, deserializer);
        Gson customGson = gsonBuilder.create();
        return customGson.fromJson(jsonObject, ProbePoint.class);
    }

    private ISourcecodeFileNode searchSourceCodeFileNodeByPath(String path) {
        return UIController.searchSourceCodeFileNodeByPath(path);
    }
}
