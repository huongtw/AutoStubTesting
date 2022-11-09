package com.dse.parser;

import com.dse.parser.object.INode;
import com.dse.parser.object.ProjectNode;

import java.io.File;

/**
 * @author DucAnh
 */
public interface IProjectLoader {

    int OBJECT = 6;
    int EXE = 5;
    int FOLDER = 3;
    int CPP_FILE = 2;
    int C_FILE = 1;
    int HEADER_FILE = 0;
    int UNDEFINED_COMPONENT = -1;
    int IGNORE_SOURCE_FILE = -2;
    String EXE_SYMBOL = ".exe";

    /**
     * Contains signals for detecting kind of project
     */
    // Represent project that has created by using Dev-Cpp
    String MAKEFILE_IN_DEVCPP_SYMBOL = "Makefile.win";
    // Represent project that has created by using Visual studio
    String MAKEFILE_IN_VISUAL_LEVEL2 = ".vcxproj";
    String MAKEFILE_IN_VISUAL_LEVEL1 = ".sln";
    // Represent project that has an own makefile named Makefile. In Unix, we
    // use command "make all" to run this makefile.
    String MAKEFILE_IN_PROJECT_SYMBOL = "Makefile";

    /**
     * Contains signals for detecting kind of file
     */
    String CPP_FILE_SYMBOL = ".cpp";
    String CC_FILE_SYMBOL = ".cc";
    String C_FILE_SYMBOL = ".c";
    String CPP_IGNORE_FILE_SYMBOL = ".akaignore.cpp";
    String CC_IGNORE_FILE_SYMBOL = ".akaignore.cc";
    String C_IGNORE_FILE_SYMBOL = ".akaignore.c";
    String OBJECT_FILE_SYMBOL = ".o";
    String HEADER_FILE_SYMBOL_TYPE_1 = ".h";
    String HEADER_FILE_SYMBOL_TYPE_2 = ".hh";
    String HEADER_FILE_SYMBOL_TYPE_3 = ".hpp";
    String H_IGNORE_FILE_SYMBOL = ".akaignore.h";
    String HH_IGNORE_FILE_SYMBOL = ".akaignore.hh";
    String HHP_IGNORE_FILE_SYMBOL = ".akaignore.hpp";

    String[] IGNORED_FILE_SYMBOLS = {
            CC_IGNORE_FILE_SYMBOL,
            CPP_IGNORE_FILE_SYMBOL,
            C_IGNORE_FILE_SYMBOL,
            H_IGNORE_FILE_SYMBOL,
            HH_IGNORE_FILE_SYMBOL,
            HHP_IGNORE_FILE_SYMBOL
    };

    /**
     * Construct structure path for the given C/C++ project
     *
     * @param projectPath
     * @return
     */
    ProjectNode load(File projectPath);

    /**
     * Generate an unique id for all nodes in the structure tree.
     *
     * @param root
     */
    void generateId(INode root);

    boolean isRecursive();

    void setRecursive(boolean recursive);
}
