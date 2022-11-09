package com.dse.environment.object;

public class EnviroTestingMethod extends AbstractEnvironmentNode {
    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return super.toString() + ": testing method = " + getMethod();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_TESTING_METHOD + " " + getMethod();
    }

    public final static String TRADITIONAL_UNIT_TESTING = "TRADITIONAL_UNIT_TESTING";
    public final static String OBJECT_FILE_TESTING = "OBJECT_FILE_TESTING";
    public final static String LIBRARY_INTERFACE_TESTING = "LIBRARY_INTERFACE_TESTING";
    public final static String TEST_DRIVEN_DEVELOPMENT = "TEST_DRIVEN_DEVELOPMENT";
}
