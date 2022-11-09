package com.dse.guifx_v3.objects.bound;

import com.dse.boundary.PrimitiveBound;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.boundary.BoundOfDataTypes;
import javafx.scene.control.Alert;

public class BoundOfVariableTypeConfiguration {
    private final static AkaLogger logger = AkaLogger.get(BoundOfVariableTypeConfiguration.class);
    private BoundOfDataTypes boundOfDataTypes;
    private String variableType;
    private String lower;
    private String upper;

    public BoundOfVariableTypeConfiguration(BoundOfDataTypes boundOfDataTypes, String variableType, String lower, String upper) {
        this.variableType = variableType;
        this.lower = lower;
        this.upper = upper;
        this.boundOfDataTypes = boundOfDataTypes;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getVariableType() {
        return variableType;
    }

    public boolean setLower(String lower) {
        try {
            if (Double.parseDouble(lower) <= Double.parseDouble(upper)) {
                this.lower = lower;
                ((PrimitiveBound) boundOfDataTypes.getBounds().get(getVariableType())).setLower(lower);
                return true;
            } else {
                UIController.showDetailDialog(Alert.AlertType.ERROR, "Wrong lower bound of " + variableType,
                        "Wrong lower value. Reason: " + lower + " > " + upper,
                        "You must not set up the value of \nlower bound greater than the value of upper bound" +
                                "\n\nTwo rules when configuring the lower bound:\n" +
                                "- The value of lower bound must be less than or equal to the value of upper bound" +
                                "\n- The value of lower bound must be a number");
                return false;
            }
        } catch (Exception e) {
            UIController.showDetailDialog(Alert.AlertType.ERROR, "Wrong lower bound of " + variableType,
                    "Wrong lower value. Reason: '" + lower + "'" + " is a string",
                    "You must not set up the value \nof lower bound as a string" +
                            "\n\nTwo rules when configuring the lower bound:\n" +
                            "- The value of lower bound must be less than or equal to the value of upper bound" +
                            "\n- The value of lower bound must be a number");
//            e.printStackTrace();
            return false;
        }
    }

    public String getLower() {
        return lower;
    }

    public boolean setUpper(String upper) {
        try {
            if (Double.parseDouble(this.lower) <= Double.parseDouble(upper)) {
                this.upper = upper;
                ((PrimitiveBound) boundOfDataTypes.getBounds().get(getVariableType())).setUpper(upper);
                return true;
            } else {
                UIController.showDetailDialog(Alert.AlertType.ERROR, "Wrong upper bound of " + variableType,
                        "Wrong upper value. Reason: " + lower + " > " + upper,
                        "You must not set up the value of \nupper bound smaller than the value of lower bound" +
                                "\n\nTwo rules when configuring the upper bound:\n" +
                                "- The value of upper bound must be greater than or equal to the value of lower bound" +
                                "\n- The value of upper bound must be a number");
                return false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            UIController.showDetailDialog(Alert.AlertType.ERROR, "Wrong upper bound of " + variableType,
                    "Wrong upper value. Reason: '" + upper + "'" + " is a string",
                    "You must not set up the value \nof upper bound as a string" +
                            "\n\nTwo rules when configuring the upper bound:\n" +
                            "- The value of upper bound must be greater than or equal to the value of lower bound" +
                            "\n- The value of upper bound must be a number");
            return false;
        }
    }

    public String getUpper() {
        return upper;
    }

    private boolean validateParam(String param) {
        return true;
    }

    public BoundOfDataTypes getBoundOfDataTypes() {
        return boundOfDataTypes;
    }

    public void setBoundOfDataTypes(BoundOfDataTypes boundOfDataTypes) {
        this.boundOfDataTypes = boundOfDataTypes;
    }
}
