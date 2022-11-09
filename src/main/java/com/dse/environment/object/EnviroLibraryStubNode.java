package com.dse.environment.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnviroLibraryStubNode extends AbstractEnvironmentNode {
    public static final String SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER = " -> ";
    public static final String SEPERATOR_BETWEEN_LIB_STUBS = "; ";

    private Map<String, String> libraries = new HashMap<>();

    public Map<String, String> getLibraries() {
        return libraries;
    }

    public void setLibraries(Map<String, String> libraryNames) {
        this.libraries = libraryNames;
    }

    public List<String> getLibraryNames() {
        List<String> output = new ArrayList<>();

        for (Map.Entry<String, String> entry : libraries.entrySet()) {
            String library = entry.getValue();
            String function = entry.getKey();

            output.add(library + SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER + function);
        }

        return output;
    }

    public void setLibraryNames(List<String> libraries) {
        for (String library : libraries) {
            String[] split = library.split(SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER);

            addLibrary(split[1], split[0]);
        }
    }

    public void addLibrary(String function, String library) {
        libraries.put(function, library);
    }

//    public void setLibraryNames(String[] libraryNames) {
//        for (String name: libraryNames)
//            this.libraryNames.add(name.trim());
//    }

    @Override
    public String toString() {
        return super.toString() + ": stubs = " + libraries;
    }

    @Override
    public String exportToFile() {
        String output;
        output = ENVIRO_LIBRARY_STUBS + " ";

        if (!libraries.isEmpty()) {
            for (Map.Entry<String, String> entry : libraries.entrySet()) {
                String library = entry.getValue();
                String function = entry.getKey();

                output += library + SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER + function + SEPERATOR_BETWEEN_LIB_STUBS;
            }

            if (output.endsWith(SEPERATOR_BETWEEN_LIB_STUBS)) {
                output = output.substring(0, output.length() - 2);
            }
        }

        return output;
    }
}
