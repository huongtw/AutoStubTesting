package com.dse.compiler;

import com.dse.compiler.message.ICompileMessage;
import com.dse.parser.object.INode;

import java.util.List;

public interface ICompiler {
    ICompileMessage compile(INode root);

    ICompileMessage compile(String filePath, String outPath);

    ICompileMessage link(String executableFilePath, String... outputPaths);

    String generateCompileCommand(String filePath, String outPath);

    String generatePreprocessCommand(String filePath);

    String preprocess(String inPath, String outPath);

    // the project path will contain the executable file
    String generateLinkCommand(String executablePath, String... outputPaths);

    String getCompileCommand();

    String getPreprocessCommand();

    String getLinkCommand();

    String getDebugCommand();

    List<String> getIncludePaths();

    List<String> getDefines();

    String getName();

    String getDefineFlag();

    String getIncludeFlag();

    String getOutputExtension();

    String getOutputFlag();

    String getDebugFlag();

    String INCLUDE_FILE_FLAG = "-include";
}
