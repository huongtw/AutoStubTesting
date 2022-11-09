package com.dse.cli;

import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.InstructionComputator;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentAnalyzer;
import com.dse.environment.PhysicalTreeImporter;
import com.dse.environment.WorkspaceLoader;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.guifx_v3.helps.InstructionMapping;
import com.dse.parser.object.INode;
import com.dse.parser.object.ProjectNode;
import com.dse.regression.ChangesBetweenSourcecodeFiles;
import com.dse.regression.WorkspaceUpdater;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.thread.task.ProjectTreeExpandTask;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.util.SpecialCharacter;

import java.io.File;

public class CliEnvironmentLoader {
    public static void openEnvironment(File scriptFile) throws Exception {
        setUpWorkingDirectoryAgain(scriptFile);

        // analyze environment script file
        EnvironmentAnalyzer analyzer = new EnvironmentAnalyzer();
        analyzer.analyze(scriptFile);
        IEnvironmentNode root = analyzer.getRoot();

        if (root instanceof EnvironmentRootNode) {
            Environment.getInstance().setEnvironmentRootNode((EnvironmentRootNode) root);

            // update config at application level
            AkaConfig akaConfig = new AkaConfig().fromJson();
            String wd = akaConfig.getWorkingDirectory();

            // update workspace directory
            String workspaceDirectory = wd + File.separator + Environment.getInstance().getName();
            akaConfig.setOpeningWorkspaceDirectory(workspaceDirectory);
            akaConfig.addOpeningWorkspaces(workspaceDirectory);

            // update workspace configuration
            String workspaceConfig = wd + File.separator + Environment.getInstance().getName()
                    + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;
            akaConfig.setOpenWorkspaceConfig(workspaceConfig);

            // export to file
            akaConfig.exportToJson();

        } else
            throw new Exception("Failed to open environment " + scriptFile.getName());
    }

    // change the working directory to the directory contains the environment file
    private static void setUpWorkingDirectoryAgain(File environmentFile) {
        AkaConfig akaConfig = new AkaConfig().fromJson();

        String environmentFilePath = environmentFile.getAbsolutePath();

        String newWorkingDirectory = environmentFile.getParentFile().getAbsolutePath();
        akaConfig.setWorkingDirectory(newWorkingDirectory);

        String workspace = environmentFilePath.replace(WorkspaceConfig.ENV_EXTENSION, SpecialCharacter.EMPTY);
        akaConfig.setOpeningWorkspaceDirectory(workspace);
        akaConfig.addOpeningWorkspaces(workspace);

        String workspaceConfig = workspace + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;
        akaConfig.setOpenWorkspaceConfig(workspaceConfig);

        akaConfig.exportToJson();
    }

    public static void rebuildEnvironment() throws Exception {
        // STEP 1: load project
        boolean findCompilationError = getProjectNode() == null;

        if (findCompilationError)
            throw new Exception("Found compilation error");

        // STEP 2: check whether we need to
        if (ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.size() == 0) {
            InstructionMapping.loadInstructions();
        }
        // STEP 3: show a dialog to inform changes
        else {
            new WorkspaceUpdater().update();
            InstructionComputator.compute();
        }
    }

    private static INode getProjectNode() {
        EnvironmentUserCode.getInstance().importFromFile();

        // STEP: load project
        ChangesBetweenSourcecodeFiles.reset();
        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();

        INode root = new PhysicalTreeImporter().importTree(new File(physicalTreePath));
        Environment.getInstance().setProjectNode((ProjectNode) root);

        ProjectTreeExpandTask expandTask = new ProjectTreeExpandTask(root);
        try {
            expandTask.expand();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return root;
    }

    public static void loadTestCaseTree() {
        TestCaseManager.clearMaps();

        Environment.getInstance().getTestcaseScriptRootNode();

        TestCaseManager.initializeMaps();
    }
}
