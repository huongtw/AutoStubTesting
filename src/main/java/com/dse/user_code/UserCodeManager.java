package com.dse.user_code;

import com.dse.guifx_v3.objects.FXFileView;
import com.dse.config.WorkspaceConfig;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.time.Duration;
import java.util.*;

public class UserCodeManager {
    private final Map<Integer, ParameterUserCode> idToParamUserCodeMap = new HashMap<>();
    private final Map<Integer, TestCaseUserCode> idToTCUserCodeMap = new HashMap<>();

    /**
     * Singleton pattern
     */
    private static UserCodeManager instance = null;

    public static UserCodeManager getInstance() {
        if (instance == null) {
            instance = new UserCodeManager();
        }
        return instance;
    }

    public void clear() {
        idToParamUserCodeMap.clear();
        idToTCUserCodeMap.clear();
    }

    public AbstractUserCode importUserCode(File file) {
        String content = Utils.readFileContent(file);
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        return new UserCodeImporter().importUserCode(jsonObject);
    }

    public void loadExistedUserCodes() {
        String paramDir = PathUtils.toAbsolute(getParamUCDirectory());
        String testCaseDir = PathUtils.toAbsolute(getTestCaseUCDirectory());
        loadExistedUserCodes(paramDir);
        loadExistedUserCodes(testCaseDir);
    }

    public void loadExistedUserCodes(String dirPath) {
        File codeUserDirectory = new File(dirPath);
        for (File file : Objects.requireNonNull(codeUserDirectory.listFiles())) {
            if (file.getName().endsWith(".json")) {
                AbstractUserCode userCode = importUserCode(file);
                if (userCode instanceof ParameterUserCode) {
                    idToParamUserCodeMap.put(userCode.getId(), (ParameterUserCode) userCode);
                } else if (userCode instanceof TestCaseUserCode) {
                    idToTCUserCodeMap.put(userCode.getId(), (TestCaseUserCode) userCode);
                }
            }
        }
    }

    public List<AbstractUserCode> getAllExistedUserCode() {
        List<AbstractUserCode> userCodes = new ArrayList<>();
        userCodes.addAll(idToParamUserCodeMap.values());
        userCodes.addAll(idToTCUserCodeMap.values());

        return userCodes;
    }

    public static CodeArea formatCodeArea(String content, boolean lineNumber, boolean editable) {
        CodeArea codeArea = new CodeArea();

        if (lineNumber)
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.multiPlainChanges().successionEnds(Duration.ofMillis(100))
                .subscribe(ignore -> codeArea
                        .setStyleSpans(0, FXFileView.computeHighlighting(codeArea.getText())));

        codeArea.replaceText(content);
        codeArea.setEditable(editable);
        codeArea.setWrapText(true);

        codeArea.getStylesheets().add(Object.class.getResource("/css/keywords.css").toExternalForm());

        return codeArea;
    }

    public void putParamUserCode(ParameterUserCode userCode) {
        if (userCode != null && !idToParamUserCodeMap.containsKey(userCode.getId())) {
            idToParamUserCodeMap.put(userCode.getId(), userCode);
        }
    }

    public void putTCUserCode(TestCaseUserCode userCode) {
        if (userCode != null && !idToTCUserCodeMap.containsKey(userCode.getId())) {
            idToTCUserCodeMap.put(userCode.getId(), userCode);
        }
    }

    public void putUserCode(AbstractUserCode userCode) {
        if (userCode instanceof ParameterUserCode)
            putParamUserCode((ParameterUserCode) userCode);
        else if (userCode instanceof TestCaseUserCode) {
            putTCUserCode((TestCaseUserCode) userCode);
        }
    }

    public ParameterUserCode getParamUserCodeById(int id) {
        return idToParamUserCodeMap.get(id);
    }

    public AbstractUserCode getTCUserCodeById(int id) {
        return idToTCUserCodeMap.get(id);
    }

    public List<ParameterUserCode> getAllParameterUserCodes() {
        return new ArrayList<>(idToParamUserCodeMap.values());
    }

    public List<TestCaseUserCode> getAllTestCaseUserCodes() {
        return new ArrayList<>(idToTCUserCodeMap.values());
    }

    public void removeParamUserCode(ParameterUserCode userCode) {
        idToParamUserCodeMap.remove(userCode.getId());
    }

    public void removeTCUserCode(TestCaseUserCode userCode) {
        idToTCUserCodeMap.remove(userCode.getId());
    }

    private String getParamUCDirectory() {
        return new WorkspaceConfig().fromJson().getParamUserCodeDirectory();
    }

    private String getTestCaseUCDirectory() {
        return new WorkspaceConfig().fromJson().getTestCaseUserCodeDirectory();
    }

    private String getPathOfUserCode(AbstractUserCode userCode) {
        if (userCode instanceof ParameterUserCode) {
            return getParamUCDirectory() + File.separator + "USER_CODE."
                    + userCode.getId() + ".json";
//        } else if (userCode instanceof TestCaseUserCode) {
        } else { // Test Case User Code
            return getTestCaseUCDirectory() + File.separator + "USER_CODE."
                    + userCode.getId() + ".json";
        }
    }

    public String getAbsolutePathOfUserCode(AbstractUserCode userCode) {
        return PathUtils.toAbsolute(getPathOfUserCode(userCode));
    }

    public void exportUserCodeToFile(AbstractUserCode userCode) {
        String content = new UserCodeExporter().export(userCode);
        Utils.writeContentToFile(content, getAbsolutePathOfUserCode(userCode));
    }

    // user code type
    public final static String USER_CODE_TYPE_ALL = "ALL";
    public final static String USER_CODE_TYPE_PARAM = "PARAMETER USER CODE";
    public final static String USER_CODE_TYPE_TEST_CASE = "TEST CASE USER CODE";
    // user code folder path
    public final static String PARAM_FOLDER_NAME = "parameter";
    public final static String TEST_CASE_FOLDER_NAME = "testcase";
}
