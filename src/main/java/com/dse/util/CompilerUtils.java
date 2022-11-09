package com.dse.util;

import auto_testcase_generation.testdatagen.RandomInputGeneration;
import auto_testcase_generation.testdatagen.fastcompilation.randomgeneration.BasicTypeRandom;
import com.dse.compiler.Compiler;
import com.dse.compiler.ICompiler;
import com.dse.compiler.message.ICompileMessage;
import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.project_init.ProjectClone;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CompilerUtils {
    private final static AkaLogger logger = AkaLogger.get(CompilerUtils.class);

    public static String generateLinkCommand(String linkCommand, String outputFlag,
                                             String executableFilePath, String... outputPaths) {
        if (outputPaths == null || outputPaths.length == 0)
            return null;

        for (int i = 0; i < outputPaths.length; i++) {
            outputPaths[i] = PathUtils.toRelative(outputPaths[i]);
        }

        executableFilePath = PathUtils.toRelative(executableFilePath);

        StringBuilder builder = new StringBuilder();

        builder.append(linkCommand)
                .append(SpecialCharacter.SPACE);

        for (String output : outputPaths)
            builder.append("\"").append(output).append("\"")
                    .append(SpecialCharacter.SPACE);

        builder.append(outputFlag)
                .append("\"").append(executableFilePath).append("\"");

        return builder.toString();
    }

    public static String getDirectoryPath(String filePath) {
        String fileName = getFileName(filePath);

        int end = filePath.lastIndexOf(fileName);
        int begin = 0;

        return filePath.substring(begin, end);
    }

    public static String getFileName(String filePath) {
        String pattern = Pattern.quote(File.separator);

        String[] pathItems = filePath.split(pattern);
        int length = pathItems.length;
        return pathItems[length - 1];
    }

    public static String getOutfilePath(String filePath, String extension) {
//        String fileName = getFileName(filePath);
//        int lastDotPos = filePath.lastIndexOf(SpecialCharacter.DOT);
//        String simpleName = filePath.substring(0, lastDotPos);
//        return new File(filePath).getParentFile().getAbsolutePath() + File.separator + simpleName;
        return ProjectClone.getClonedFilePath(filePath) + extension;
    }

    private static int isMatch(char[] cmd, int idx, String flag) {
        for (int i = 0; i < flag.length(); i++) {
            if (cmd[idx + i] != flag.charAt(i))
                return -1;
        }

        return idx + flag.length();
    }

    public static Compiler parseCommand(String command, Compiler compiler) {
        List<String> includes = new ArrayList<>();
        List<String> defines = new ArrayList<>();

        char[] chars = command.toCharArray();
        int idx = 0;

        String includeFlag = compiler.getIncludeFlag();
        String defineFlag = compiler.getDefineFlag();

        while (idx < chars.length) {
            int isDefine = isMatch(chars, idx, defineFlag);
            int isInclude = isMatch(chars, idx, includeFlag);

            if (isDefine > 0 || isInclude > 0) {
                String content = SpecialCharacter.EMPTY;
                int contentIdx = isDefine > 0 ? isDefine : isInclude;

                /*
                 * Skip all space
                 * Ex: -D   MAX_SIZE=50
                 */
                while (contentIdx < chars.length && (chars[contentIdx] == SpecialCharacter.SPACE || chars[contentIdx] == '\n')) {
                    contentIdx++;
                }

                while (contentIdx < chars.length && chars[contentIdx] != SpecialCharacter.SPACE & chars[contentIdx] != '\n') {
                    content += chars[contentIdx];
                    contentIdx++;
                }

                content = content.trim();

                if (isDefine > 0) {
                    if (content.matches("[^=]+=[^=]+") || content.matches("[^=]+"))
                        if (!defines.contains(content))
                            defines.add(content);
                } else if (!includes.contains(content)) {
                    includes.add(content);
                }

                idx = contentIdx;
            }

            idx++;
        }

        Compiler container = new Compiler();

        container.setDefines(defines);
        container.setIncludePaths(includes);

        return container;
    }

    public static String[] prepareForTerminal(Compiler compiler, String command) {
//        String[] origin = command.split("\"");
        List<String> script = new ArrayList<>();
//
//        for (String item : origin) {
//            if (item.contains(File.separator)) {
//                if (item.contains(compiler.getDefineFlag()) || item.contains(compiler.getIncludeFlag())
//                    || item.contains(compiler.getOutputFlag()) || item.contains(compiler.getDebugFlag())
//                    || item.contains(compiler.getCompileCommand()) || item.contains(compiler.getLinkCommand())
//                    || item.contains(compiler.getPreprocessCommand()) || item.contains(compiler.getDebugCommand())) {
//                    script.addAll(Arrays.asList(item.split(" ")));
//                } else
//                    script.add(item);
//            } else {
//                script.addAll(Arrays.asList(item.split(" ")));
//            }
//        }
        String[] origin = command.split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?");


        for (String item : origin) {
            if (item.contains("\"")) {
                if (!item.endsWith("\""))
                    item = item + "\"";

                if (item.startsWith(compiler.getOutputFlag())
                        || item.startsWith(compiler.getIncludeFlag())
                        || item.startsWith(ICompiler.INCLUDE_FILE_FLAG)) {
                    script.addAll(Arrays.asList(item.split("\"")));
                } else if (item.startsWith("\"") && item.endsWith("\"")) {
                    script.add(item.substring(1, item.length() - 1));
                } else
                    script.add(item);
            } else
                script.add(item);
        }

        if (!script.isEmpty()) {
            int i = 0;
            while (!(script.get(i).contains(File.separator) || script.get(i).contains("."))) {
                if (script.get(i).startsWith("-l")) {
                    String temp = script.remove(i);
                    script.add(temp);
                } else {
                    i++;
                }
            }
        }

        script = script
                .stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return script.toArray(new String[0]);
    }

    public static ICompileMessage testCompile(String returnType, String prototype,
                                              ICommonFunctionNode functionNode, INode context) {
        String functionDefinition = prototype;

        functionDefinition += " {\n";

        if (!returnType.equals(VariableTypeUtils.VOID_TYPE.VOID))
            functionDefinition += "return ";

        functionDefinition += Utils.getFullFunctionCall(functionNode);

        functionDefinition += "\n";

        functionDefinition += "}";

        String template = "#include \"" + context.getAbsolutePath() + "\"\n" +
                functionDefinition;

        logger.debug("File source: " + template);

        String filePath;
        String temporaryPath;

        filePath = context.getAbsolutePath();
        temporaryPath = ProjectClone.getClonedFilePath(filePath);

        String ext = context.getName().substring(context.getName().lastIndexOf(SpecialCharacter.DOT));
        int lastSeparator = temporaryPath.lastIndexOf(File.separator) + 1;

        // add .akaignore to avoid parsing again
        temporaryPath = temporaryPath.substring(0, lastSeparator) // container folder
                + "temporary" + BasicTypeRandom.random(100000, 999999) // random name
                + ProjectClone.CLONED_FILE_EXTENSION // .akaignore ext
                + ext; //

        logger.debug("Compile " + temporaryPath);
        Utils.writeContentToFile(template, temporaryPath);

        String outfilePath = temporaryPath + Environment.getInstance().getCompiler().getOutputExtension();

        ICompileMessage message = Environment.getInstance().getCompiler().compile(temporaryPath, outfilePath);

        Utils.deleteFileOrFolder(new File(temporaryPath));

        return message;
    }
}
