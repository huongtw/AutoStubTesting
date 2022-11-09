package com.dse.parser.systemlibrary;

import com.dse.environment.Environment;
import com.dse.util.Utils;

import java.io.File;

public class SystemHeaderGenerator {
    private String path;
    private String workingDir;

    public SystemHeaderGenerator(String wd, String path) {
        this.path = path;
        this.workingDir = wd;

        if (!workingDir.endsWith(File.separator))
            workingDir += File.separator;
    }

    private static final String IN_EXTENSION = ".h";
    private static final String OUT_EXTENSION = ".i";

    public String generate() {
        String fileIn = workingDir + new File(path).getName() + IN_EXTENSION;
        Utils.writeContentToFile(String.format("#include \"%s\"", path), fileIn);

        String fileOut = workingDir + new File(path).getName() + OUT_EXTENSION;

        String content = Environment.getInstance().getCompiler().preprocess(fileIn, fileOut);

        Utils.deleteFileOrFolder(new File(fileIn));

        if (content == null || content.isEmpty())
            return null;

        return fileOut;
    }
}
