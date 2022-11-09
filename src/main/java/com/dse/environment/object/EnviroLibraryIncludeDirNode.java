package com.dse.environment.object;

import com.dse.util.PathUtils;

public class EnviroLibraryIncludeDirNode extends AbstractEnvironmentNode {
    private String libraryIncludeDir;//just a path

    public String getLibraryIncludeDir() {
        return libraryIncludeDir;
    }

    public void setLibraryIncludeDir(String libraryIncludeDir) {
        this.libraryIncludeDir = libraryIncludeDir;
    }

    @Override
    public String exportToFile() {
        return ENVIRO_LIBRARY_INCLUDE_DIR + " " + PathUtils.toRelative(libraryIncludeDir);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        EnviroLibraryIncludeDirNode that = (EnviroLibraryIncludeDirNode) o;

        return libraryIncludeDir.equals(that.getLibraryIncludeDir());
    }
}
