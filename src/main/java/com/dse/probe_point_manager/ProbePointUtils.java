package com.dse.probe_point_manager;

import com.dse.compiler.message.ICompileMessage;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.IProjectLoader;
import com.dse.parser.object.AbstractFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.SourcecodeFileNode;
import com.dse.probe_point_manager.controllers.FailedToCompilePopupController;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.project_init.ProjectClone;
import com.dse.project_init.ProjectCloneMap;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_execution.testdriver.SimpleDriverGeneration;
import com.dse.testcase_execution.testdriver.SimpleDriverGenerationForC;
import com.dse.testcase_execution.testdriver.SimpleDriverGenerationForCpp;
import com.dse.testcase_manager.TestCase;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProbePointUtils {
    final static AkaLogger logger = AkaLogger.get(ProbePointUtils.class);

    private static Stage addEditProbePointControllerStage;

//    public static boolean addProbePointInFile(ProbePoint probePoint) {
//        String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
//        insertIncludes(probePoint);
//        return checkCompile(probePoint.getTestCases(), filePath);
//    }

    public static void insertIncludes(ProbePoint probePoint) {
        String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
        Pair<Integer, Integer> pair = calculateLine(probePoint, filePath);
        List<String> content = readData(filePath);
        String includeBefore = "#include " + "\"" + probePoint.getBefore() + "\"";
        String includeAfter = "#include " + "\"" + probePoint.getAfter() + "\"";
        content.add(pair.getKey(), includeBefore);
        content.add(pair.getValue(), includeAfter);
        String newContent = String.join("\n", content);
        Utils.writeContentToFile(newContent, filePath);
    }

    public static void removeAllIncludes(IFunctionNode functionNode) {
        String filePath = ProjectClone.getClonedFilePath(functionNode.getSourceFile().getAbsolutePath());
        List<String> content = readData(filePath);

        String includeBefore;
        String includeAfter;
        List<ProbePoint> probePoints = ProbePointManager.getInstance()
                .getFunPpMap().get(functionNode);
        if (probePoints != null) {
            for (ProbePoint pp : probePoints) {
                includeBefore = "#include " + "\"" + pp.getBefore() + "\"";
                includeAfter = "#include " + "\"" + pp.getAfter() + "\"";
                content.remove(includeBefore);
                content.remove(includeAfter);
            }

            String newContent = String.join("\n", content);
            Utils.writeContentToFile(newContent, filePath);
        }
    }

    public static void deleteProbePointInFile(ProbePoint probePoint) {
        // if probe point has any test cases then there are two include lines in the ignore source file,
        // remove them and check compile.
        if (!probePoint.getTestCases().isEmpty()) {
            String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
            ProbePointUtils.removeIncludes(probePoint);
            // check after remove, whether the remove incorrect or not
            if (!ProbePointUtils.checkCompile(probePoint.getTestCases(), filePath, false)) {
                logger.error("The akaignore file is incorrect after remove temporary includes");
            }
        }
    }

    public static void removeIncludes(ProbePoint probePoint) {
        String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
        List<String> content = readData(filePath);
        String includeBefore = "#include " + "\"" + probePoint.getBefore() + "\"";
        String includeAfter = "#include " + "\"" + probePoint.getAfter() + "\"";
        content.remove(includeBefore);
        content.remove(includeAfter);
        String newContent = String.join("\n", content);
        Utils.writeContentToFile(newContent, filePath);
    }

    public static boolean checkCompile(List<TestCase> testCases, String filePath, boolean showError) {
        if (testCases.size() == 0) {
            String tempFilePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + "temp.cpp";
            StringBuilder builder = new StringBuilder();
            builder.append(fakePreAKA);
            builder.append("#include \"" + filePath + "\"");
            Utils.writeContentToFile(builder.toString(), tempFilePath);
            String tempOutPath = tempFilePath + Environment.getInstance().getCompiler().getOutputExtension();
            ICompileMessage compileMessage = Environment.getInstance().getCompiler().compile(tempFilePath, tempOutPath);
            if (compileMessage.getType() == ICompileMessage.MessageType.ERROR) {
                logger.debug("Code has bug");
                if (showError) {
                    // show pop up window to log error
                    showCompileError(compileMessage.getMessage());
                }
                Utils.deleteFileOrFolder(new File(tempFilePath));
                return false;
            } else {
                Utils.deleteFileOrFolder(new File(tempFilePath));
                logger.debug("Code is ok");
                return true;
            }
        } else {
            for (TestCase testCase : testCases) {
                if (!checkCompile(testCase, filePath, showError)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static void showCompileError(String message) {
        Stage popUpWindow = FailedToCompilePopupController.getPopupWindow(message);

        // block the environment window
        assert popUpWindow != null;
        popUpWindow.initModality(Modality.WINDOW_MODAL);
        if (addEditProbePointControllerStage != null) {
            popUpWindow.initOwner(addEditProbePointControllerStage.getScene().getWindow());
        } else {
            logger.debug("addEditProbePointControllerStage is null");
        }
        popUpWindow.show();
    }

    public static boolean checkCompile(TestCase testCase, String filePath, boolean showError) {
//        String tempFilePath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + "temp.cpp";
        String tempFilePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + "temp";
//        StringBuilder builder = new StringBuilder();
//        builder.append(fakePreAKA);
//
//        String akaName = "AKA_TC_" + testCase.getName().replace(".", "_").toUpperCase();
//        String tempDefinition = "#define " + akaName + " 1\n";
//        builder.append(tempDefinition);
//
//        builder.append("#include \"" + filePath + "\"");
        SimpleDriverGeneration driverGeneration;
        if (Environment.getInstance().isC()) {
            driverGeneration = new SimpleDriverGenerationForC();
            tempFilePath += IProjectLoader.C_FILE_SYMBOL;
        } else {
            driverGeneration = new SimpleDriverGenerationForCpp();
            tempFilePath += IProjectLoader.CPP_FILE_SYMBOL;
        }

        driverGeneration.setTestCase(testCase);
        driverGeneration.generate();

        Utils.writeContentToFile(driverGeneration.getTestDriver(), tempFilePath);
        String outPath = tempFilePath + Environment.getInstance().getCompiler().getOutputExtension();
        ICompileMessage compileMessage = Environment.getInstance().getCompiler().compile(tempFilePath, outPath);
        if (compileMessage.getType() == ICompileMessage.MessageType.ERROR) {
            logger.debug("Code has bug");
            if (showError) {
                // show pop up window to log error
                showCompileError(compileMessage.getMessage());
            }
            Utils.deleteFileOrFolder(new File(tempFilePath));
            return false;
        } else {
            Utils.deleteFileOrFolder(new File(tempFilePath));
            logger.debug("Code is ok");
            return true;
        }

    }

    /**
     * calculate line in ignore aka file, corresponding to lines to insert "before, after includes"
     * of the probe point
     * Notes: this method is for insertion include of probe point when creating new probe point
     *
     * @param probePoint probe point
     * @param filePath   source code file path
     * @return pair of lines for "before, after includes"
     */
    public static Pair<Integer, Integer> calculateLine(ProbePoint probePoint, String filePath) {
        // get all probe points of the source code file
        List<ProbePoint> probePoints = ProbePointManager.getInstance()
                .searchProbePoints(probePoint.getSourcecodeFileNode());// search from nameToProbePointMap
        if (! probePoints.contains(probePoint))
            probePoints.add(probePoint);
        IFunctionNode functionNode = probePoint.getFunctionNode();
        probePoints.removeIf(pp -> {
            if (pp != probePoint) {
                return pp.getTestCases().size() == 0;
            } else {
                return false;
            }
        });
        Collections.sort(probePoints);
        int lineInIgnore = new ProjectCloneMap(filePath).getLineInFunction(
                (AbstractFunctionNode) functionNode, probePoint.getLineInSourceCodeFile());
//        int lineInIgnore = getLineInSourceCodeFile(probePoint);
        int pos = probePoints.indexOf(probePoint);
        int postEqualNum = 0;
        int equalNum = 0;
        if (pos < 0) pos = 0;
        else {
            int lineInSource = probePoint.getLineInSourceCodeFile();
            for (int i = 0; i < probePoints.size(); i++) {
                ProbePoint temp = probePoints.get(i);
                if (temp.getLineInSourceCodeFile() == lineInSource) {
                    equalNum++;
                    if (i > pos)
                        postEqualNum++;
                } else if (temp.getLineInSourceCodeFile() > lineInSource)
                    break;
            }
        }
        int before = 0;
        int after = 0;
        before = lineInIgnore - postEqualNum;
        after = before + equalNum + 1;

        logger.debug(probePoint.getName() + ": " + before + " - " + after);

        return new Pair<>(before, after);
    }

    /**
     * Read data from file path
     *
     * @param path path to file
     * @return data in string
     */
    public static List<String> readData(String path) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
            // // e.printStackTrace();
        }
        return data;
    }

    public static void setAddEditProbePointControllerStage(Stage addEditProbePointControllerStage) {
        ProbePointUtils.addEditProbePointControllerStage = addEditProbePointControllerStage;
    }

    public static Stage getAddEditProbePointControllerStage() {
        return addEditProbePointControllerStage;
    }

    /**
     * get line of probe point in origin source code file
     *
     * @param probePoint probe point
     * @return line number
     */
    public static int getLineInSourceCodeFile(ProbePoint probePoint) {
        try {
            // get Function position
            IFunctionNode functionNode = (IFunctionNode) UIController.searchFunctionNodeByPath(
                    probePoint.getFunctionNode().getAbsolutePath());
            String filePath = probePoint.getSourcecodeFileNode().getAbsolutePath();
            int funcPos = functionNode.getAST().getFileLocation().getStartingLineNumber(); // from 1
//        int funcPos = new ProjectCloneMap2(filePath).getLineInFunction(functionNode.getAST());
            // get line in function
            return funcPos + probePoint.getLineInFunction(); // line number is from 0, funcPos is from 1
        } catch (FunctionNodeNotFoundException e) {
            e.printStackTrace();
            logger.error("FunctionNodeNotFoundException: " + e.getFunctionPath());
            return -1;
        }
    }

    /**
     * update line in source code of probe point.
     * use this function after any change in source code.
     *
     * @param probePoint probe point
     * @return true if the line number is change
     */
    public static boolean updateLineInSourceCodeFile(ProbePoint probePoint) {
        int correctLine = getLineInSourceCodeFile(probePoint);
        if (correctLine != probePoint.getLineInSourceCodeFile()) {
            logger.debug("Update LIS of probe point " + probePoint.getName());
            logger.debug("" + probePoint.getLineInSourceCodeFile()
                    + " to " + correctLine);
            probePoint.setLineInSourceCodeFile(correctLine);
            return true;
        } else {
            logger.debug("The LIS of probe point " + probePoint.getName() + " is "
                    + correctLine + ", correct.");
            return false;
        }
    }

    private static final String fakePreAKA = "#include <string> \n" +
            "bool " + DriverConstant.MARK + "(std::string append) {\n" +
            "  return true;\n" +
            "}\n" +
            "\n" +
            "int " + DriverConstant.CALL_COUNTER + " = 0;\n";
}

