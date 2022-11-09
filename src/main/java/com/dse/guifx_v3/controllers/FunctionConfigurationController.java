package com.dse.guifx_v3.controllers;

import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PointerOrArrayBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.config.*;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.ConfigFunctionValueColumnCellFactory;
import com.dse.guifx_v3.objects.FunctionConfigParameter;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;

public class FunctionConfigurationController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(FunctionConfigurationController.class);

    @FXML
    private TreeTableView<FunctionConfigParameter> ttvFunctionConfiguration;
    @FXML
    private TreeTableColumn<FunctionConfigParameter, String> colParameter;
    @FXML
    private TreeTableColumn<FunctionConfigParameter, String> colValue;
    @FXML
    private Label lFunctionName;
    @FXML
    private Button bApply;

    private TreeItem<FunctionConfigParameter> root = new TreeItem<>();
    private Stage stage;
    private boolean isChanged = false;
    private FunctionConfig functionConfig;
    private boolean isDefaultConfig = false;

    public void initialize(URL location, ResourceBundle resources) {
        ttvFunctionConfiguration.setRoot(root);
        setChanged(false);

        colParameter.setCellValueFactory(param -> {
            if (param.getValue().getValue() != null) {
                String name = param.getValue().getValue().getParam();
                if (param.getValue().getValue().getValidTypeRange() != null) {
                    name += "    [" + param.getValue().getValue().getValidTypeRange() + "]";
                }
                return new SimpleStringProperty(name);
            } else return new SimpleStringProperty();
        });

    }

    public static FunctionConfigurationController getInstanceForDefaultConfiguration() {
        FunctionConfigurationController controller = getInstance();
        if (controller != null) {
            // get the default function configure
            FunctionConfig functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
            if (functionConfig != null) {
                controller.getStage().setTitle("Edit Default Function Configuration");
                controller.setFunctionName("Default for all functions");
                controller.setDefaultConfig(true); // must setDefaultConfig before loadContent
                controller.loadContent(functionConfig);
            }
        }

        return controller;
    }

    public static FunctionConfigurationController getInstance(ICommonFunctionNode functionNode) {
        FunctionConfigurationController controller = getInstance();
        if (controller != null) {
            controller.getStage().setTitle("Function Configuration");
            controller.setFunctionName(functionNode.getNameOfFunctionConfigTab());
            controller.setDefaultConfig(false);

            FunctionConfig functionConfig = loadOrInitFunctionConfig(functionNode);
            if (functionConfig == null) {
                return null;
            } else {
                controller.loadContent(functionConfig);
            }
        }

        return controller;
    }

    public static FunctionConfigurationController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class
                .getResource("/FXML/FunctionConfigTreeTableView.fxml"));
        try {
            Parent parent = loader.load();
            FunctionConfigurationController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            scene.getStylesheets().add("/css/treetable.css");

            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void showStage() {
        stage.show();
    }

    @FXML
    public void ok() {
        apply();
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void apply() {
        exportFunctionConfigToJson(functionConfig);
        setChanged(false);
    }

    @FXML
    public void cancel() {
        // reset function config to old value
        if (functionConfig.getFunctionNode() != null) {
            functionConfig = loadOrInitFunctionConfig(functionConfig.getFunctionNode()); // reload from database
        } else {
            functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
        }

        // close stage
        if (stage != null) {
            stage.close();
        }
    }

    private static FunctionConfig loadOrInitFunctionConfig(ICommonFunctionNode functionNode) {
        // search the function config in database
        FunctionConfig functionConfig = null;
        File functionConfigDirectory = new File(new WorkspaceConfig().fromJson().getFunctionConfigDirectory());
        if (functionConfigDirectory.exists()) {
            for (File configFile : Objects.requireNonNull(functionConfigDirectory.listFiles()))
                if (configFile.getName().equals(functionNode.getNameOfFunctionConfigJson() + ".json")) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigDeserializer());
                    Gson customGson = gsonBuilder.create();
                    functionConfig = customGson.fromJson(Utils.readFileContent(configFile), FunctionConfig.class);
                    functionNode.setFunctionConfig(functionConfig);
                    logger.debug("Function config of " + functionNode.getAbsolutePath() + ": " + configFile.getAbsolutePath());
                    break;
                }
        }

        // create new function config
        if (functionConfig == null) {
            Alert alert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Initialize a function config", "Function config initialization",
                    "Do you want to initialize a function config?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get().getText().toLowerCase().equals("yes")) {
                functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
                functionConfig.setFunctionNode(functionNode);
                functionConfig.createBoundOfArgument(functionConfig, functionNode);
                functionNode.setFunctionConfig(functionConfig);

                // save the function config to file
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigSerializer());
                Gson customGson = gsonBuilder.setPrettyPrinting().create();
                String json = customGson.toJson(functionConfig, FunctionConfig.class);
                String jsonFile = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator + functionNode.getNameOfFunctionConfigJson() + ".json";
                Utils.writeContentToFile(json, jsonFile);
                logger.debug("Function config of " + functionNode.getAbsolutePath() + ": " + jsonFile);

                return (FunctionConfig) functionNode.getFunctionConfig();
            }
        } else {
            return (FunctionConfig) functionNode.getFunctionConfig();
        }

        return null;
    }
    private String getValidTypeRange(String varName, ICommonFunctionNode functionNode) {
        for (IVariableNode variableNode : functionNode.getArgumentsAndGlobalVariables()) {
            if (variableNode.getName().equals(varName)) {
                String realRawType = variableNode.getRealType();
                String type = VariableTypeUtils.removeRedundantKeyword(realRawType);
                type = VariableTypeUtils.deleteReferenceOperator(type);
                type = VariableTypeUtils.deleteSizeFromArray(type);
                type = type.trim();

                Set<String> keys = Environment.getBoundOfDataTypes().getBounds().keySet();
                if (keys.contains(type)) {
                    PrimitiveBound b = Environment.getBoundOfDataTypes().getBounds().get(type);
                    return b.getLower() + IFunctionConfigBound.RANGE_DELIMITER + b.getUpper();
                }
            }
        }

        return "not found";
    }

    public void loadContent(FunctionConfig functionConfig) {
        setFunctionConfig(functionConfig); // for exporting as need

        ConfigFunctionValueColumnCellFactory cellFactory = new ConfigFunctionValueColumnCellFactory(functionConfig);
        cellFactory.setDefaultConfig(isDefaultConfig);
        cellFactory.setController(this);
        colValue.setCellFactory(cellFactory);

        List<FunctionConfigParameter> parameters = new ArrayList<>();
        double delta = FunctionConfig.getCommonFloatAndDoubleDelta(); // the delta is set for all environment
        FunctionConfigParameter deltaParam = new FunctionConfigParameter(functionConfig,
                FunctionConfigParameter.FLOAT_AND_DOUBLE_DELTA, String.valueOf(delta));
        parameters.add(deltaParam);

        // bound
        Map<String, IFunctionConfigBound> bounds = functionConfig.getBoundOfArgumentsAndGlobalVariables();
        for (String key : bounds.keySet()) {
            IFunctionConfigBound b = bounds.get(key);
            if (b instanceof PrimitiveBound) {
                parameters.add(new FunctionConfigParameter(functionConfig, key,
                        ((PrimitiveBound) b).getLower() + IFunctionConfigBound.RANGE_DELIMITER + ((PrimitiveBound) b).getUpper()));
            } else if (b instanceof MultiplePrimitiveBound) {
                StringBuilder builder = new StringBuilder();
                List<PrimitiveBound> primitiveBoundList = ((MultiplePrimitiveBound) b);
                for (int i = 0; i < primitiveBoundList.size(); i++) {
                    PrimitiveBound primitiveBound = primitiveBoundList.get(i);
                    builder.append(primitiveBound.getLower()).append(IFunctionConfigBound.RANGE_DELIMITER);
                    builder.append(primitiveBound.getUpper());
                    if (i < primitiveBoundList.size() - 1) {
                        builder.append(',');
                    }
                }

                FunctionConfigParameter parameter = new FunctionConfigParameter(functionConfig, key, builder.toString());
                String validTypeRange = getValidTypeRange(key, functionConfig.getFunctionNode());
                parameter.setValidTypeRange(validTypeRange);
                parameters.add(parameter);

            } else if (b instanceof PointerOrArrayBound) {
                boolean editable = true;
                String type = ((PointerOrArrayBound) b).getType();
                // Check if the array's size is determined in the source code
                if (VariableTypeUtils.deleteSizeFromArray(type).length() < type.length()) {
                    // The boundary for the array can't be edited in this case
                    editable = false;
                }
                parameters.add(
                    new FunctionConfigParameter(
                        functionConfig,
                        key + IFunctionConfigBound.ARGUMENT_SIZE,
                        ((PointerOrArrayBound) b).showIndexes(),
                        editable
                    )
                );
            } else if (b instanceof UndefinedBound)
                parameters.add(new FunctionConfigParameter(functionConfig, key, ((UndefinedBound) b).show()));
        }

        // others
        parameters.add(new FunctionConfigParameter(functionConfig, "", ""));

        String testDataGenStrategy = functionConfig.getTestdataGenStrategy();
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.TEST_DATA_GEN_STRATEGY, testDataGenStrategy));

        String theMaximumNumerOfIterations = String.valueOf(functionConfig.getTheMaximumNumberOfIterations());
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.THE_MAXIMUM_NUMBER_OF_ITERATIONS, theMaximumNumerOfIterations));

        parameters.add(new FunctionConfigParameter(functionConfig, "", ""));
        String characterBoundLower = String.valueOf(functionConfig.getBoundOfOtherCharacterVars().getLower());
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.CHARACTER_BOUND_LOWER, characterBoundLower));

        String characterBoundUpper = String.valueOf(functionConfig.getBoundOfOtherCharacterVars().getUpper());
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.CHARACTER_BOUND_UPPER, characterBoundUpper));

        parameters.add(new FunctionConfigParameter(functionConfig, "", ""));
        String numberBoundLower = String.valueOf(functionConfig.getBoundOfOtherNumberVars().getLower());
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.NUMBER_BOUND_LOWER, numberBoundLower));

        String numberBoundUpper = String.valueOf(functionConfig.getBoundOfOtherNumberVars().getUpper());
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.NUMBER_BOUND_UPPER, numberBoundUpper));

        parameters.add(new FunctionConfigParameter(functionConfig, "", ""));
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.LOWER_BOUND_OF_OTHER_ARRAYS,
                String.valueOf(functionConfig.getBoundOfArray().getLower())));

        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.UPPER_BOUND_OF_OTHER_ARRAYS,
                String.valueOf(functionConfig.getBoundOfArray().getUpper())));

        parameters.add(new FunctionConfigParameter(functionConfig, "", ""));
        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.LOWER_BOUND_OF_OTHER_POINTERS,
                String.valueOf(functionConfig.getBoundOfPointer().getLower())));

        parameters.add(new FunctionConfigParameter(functionConfig, FunctionConfigParameter.UPPER_BOUND_OF_OTHER_POINTERS,
                String.valueOf(functionConfig.getBoundOfPointer().getUpper())));
        //
        for (FunctionConfigParameter param : parameters) {
            TreeItem<FunctionConfigParameter> item = new TreeItem<>(param);
            root.getChildren().add(item);
        }
    }

    private void exportFunctionConfigToJson(FunctionConfig functionConfig) {
        if (functionConfig.getFunctionNode() == null){
            // set up config for workspace level
            new WorkspaceConfig().fromJson().setDefaultFunctionConfig(functionConfig).exportToJson();

        } else {
            // set up config for a function
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigSerializer());
            Gson gson = builder.setPrettyPrinting().create();
            String json = gson.toJson(functionConfig, FunctionConfig.class);

            String functionConfigFile = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator +
                    functionConfig.getFunctionNode().getNameOfFunctionConfigJson() + ".json";
            logger.debug("Export the config of function " + functionConfig.getFunctionNode().getAbsolutePath() + " to " + functionConfigFile);
            Utils.writeContentToFile(json, functionConfigFile);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.initOwner(UIController.getPrimaryStage());
        this.stage.initModality(Modality.WINDOW_MODAL);
    }

    public Stage getStage() {
        return stage;
    }

    private void setFunctionName(String name) {
        lFunctionName.setText(name);
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
        bApply.setDisable(!isChanged);
    }

    public void setFunctionConfig(FunctionConfig functionConfig) {
        this.functionConfig = functionConfig;
    }

    public void setDefaultConfig(boolean defaultConfig) {
        isDefaultConfig = defaultConfig;
    }
}
