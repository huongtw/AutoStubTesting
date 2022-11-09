package com.dse.boundary;

import com.dse.config.WorkspaceConfig;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BoundaryManager {
    private final Map<String, BoundOfDataTypes> nameToBoundaryMap = new HashMap<>();
    private String usingBoundaryName;

    /**
     * Singleton partern
     */
    private static BoundaryManager instance = null;

    public static BoundaryManager getInstance() {
        if (instance == null) {
            instance = new BoundaryManager();
        }
        return instance;
    }

    public void clear() {
        nameToBoundaryMap.clear();
    }

    public void loadExistedBoundaries() {
        String BOUNDARY_FOLDER = new WorkspaceConfig().fromJson().getBoundOfDataTypeDirectory();
        String usingBoundaryPath = BOUNDARY_FOLDER + File.separator + WorkspaceConfig.USING_BOUND_OF_DATA_TYPES;
        if (! (new File(usingBoundaryPath).exists())) {
            Utils.writeContentToFile(BoundOfDataTypes.MODEL_ILP32, usingBoundaryPath);
            usingBoundaryName = BoundOfDataTypes.MODEL_ILP32; // make default ILP32
        } else {
            usingBoundaryName = Utils.readFileContent(usingBoundaryPath).trim();
        }

        BoundOfDataTypes LP32 = new BoundOfDataTypes();
        LP32.setBounds(LP32.createILP32());
        LP32.getBounds().setName(BoundOfDataTypes.MODEL_LP32);
        nameToBoundaryMap.put(LP32.getBounds().getName(), LP32);

        BoundOfDataTypes ILP32 = new BoundOfDataTypes();
        ILP32.setBounds(ILP32.createILP32());
        ILP32.getBounds().setName(BoundOfDataTypes.MODEL_ILP32);
        nameToBoundaryMap.put(ILP32.getBounds().getName(), ILP32);

        BoundOfDataTypes LLP64 = new BoundOfDataTypes();
        LLP64.setBounds(LLP64.createLP64());
        LLP64.getBounds().setName(BoundOfDataTypes.MODEL_LLP64);
        nameToBoundaryMap.put(LLP64.getBounds().getName(), LLP64);

        BoundOfDataTypes LP64 = new BoundOfDataTypes();
        LP64.setBounds(LP64.createLP64());
        LP64.getBounds().setName(BoundOfDataTypes.MODEL_LP64);
        nameToBoundaryMap.put(LP64.getBounds().getName(), LP64);

        File boundaryFolder = new File(BOUNDARY_FOLDER);
        for (File file : Objects.requireNonNull(boundaryFolder.listFiles())) {
            if (file.getName().endsWith(".json")) {
                BoundOfDataTypes boundOfDataTypes;
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(BoundOfDataTypes.class, new BoundOfDataTypesDeserializer());
                Gson customGson = gsonBuilder.create();
                boundOfDataTypes = customGson.fromJson(Utils.readFileContent(file),
                        BoundOfDataTypes.class);
                String name = file.getName().replace(".json", "");
                boundOfDataTypes.getBounds().setName(name);
                nameToBoundaryMap.put(name, boundOfDataTypes);
            }
        }

        // handle when the using Boundary Name is not existed
        if (! nameToBoundaryMap.containsKey(usingBoundaryName)) {
            usingBoundaryName = BoundOfDataTypes.MODEL_ILP32;
            exportUsingBoundaryName();
        }
    }

    public void setUsingBoundaryName(String usingBoundaryName) {
        this.usingBoundaryName = usingBoundaryName;
    }

    public void exportUsingBoundaryName() {
        String BOUNDARY_FOLDER = new WorkspaceConfig().fromJson().getBoundOfDataTypeDirectory();
        String usingBoundaryPath = BOUNDARY_FOLDER + File.separator + WorkspaceConfig.USING_BOUND_OF_DATA_TYPES;
        Utils.writeContentToFile(usingBoundaryName, usingBoundaryPath);
    }

    public String getUsingBoundaryName() {
        return usingBoundaryName;
    }

    public BoundOfDataTypes getUsingBoundOfDataTypes() {
        return nameToBoundaryMap.get(usingBoundaryName);
    }

    public Collection<BoundOfDataTypes> getExistedBoundaries() {
        return nameToBoundaryMap.values();
    }

    public Map<String, BoundOfDataTypes> getNameToBoundaryMap() {
        return nameToBoundaryMap;
    }
}
