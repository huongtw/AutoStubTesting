package com.dse.config;

public interface IProjectType {
	int PROJECT_UNKNOWN_TYPE = -1;
	int PROJECT_DEV_CPP = 0;
	int PROJECT_CODEBLOCK = 1;
	int PROJECT_VISUALSTUDIO = 2;
	int PROJECT_ECLIPSE = 3;
	int PROJECT_CUSTOMMAKEFILE = 4; // Represent project has a makefile named "Makefile"
}