package com.dse.environment;

import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.dse.environment.SourcecodeFileTreeExporterv2.NAME_SOURCE_COLDE_FILE_TAG;

public class SourcecodeTreeImporterv2 {
    private File sourcecodeJsonFile;

    public Map<String, String> load(File sourcecodeJsonFile) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson customGson = gsonBuilder.create();
        String json = Utils.readFileContent(sourcecodeJsonFile);
        Map<String, String> obj = customGson.fromJson(json, HashMap.class);

        // TODO: test lại nhưng không biết khi nào hàm này đc gọi? - Lamnt
        Map<String, String> refactor = new HashMap<>();

        for (Map.Entry<String, String> entry : obj.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equals(NAME_SOURCE_COLDE_FILE_TAG)) {
                value = PathUtils.toAbsolute(value);
                value = Utils.normalizePath(value);
                refactor.put(key, value);
            } else {
                key = PathUtils.toAbsolute(key);
                key = Utils.normalizePath(key);
                refactor.put(key, value);
            }
        }

        return refactor;
    }

    public File getSourcecodeJsonFile() {
        return sourcecodeJsonFile;
    }

    public void setSourcecodeJsonFile(File sourcecodeJsonFile) {
        this.sourcecodeJsonFile = sourcecodeJsonFile;
    }
}
