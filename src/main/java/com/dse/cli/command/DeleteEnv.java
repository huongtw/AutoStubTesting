package com.dse.cli.command;

import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentAnalyzer;
import com.dse.environment.PhysicalTreeImporter;
import com.dse.environment.WorkspaceLoader;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.Environment;
import com.dse.parser.object.INode;
import com.dse.parser.object.ProjectNode;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dse.cli.command.ICommand.DELETE_ENV;

@Command(name = DELETE_ENV,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "delete an exist environment.")
public class DeleteEnv extends AbstractCommand<String[]> {
    @Parameters(index = "0..*",
            description = "The script file path archive environment information.")
    private File[] scriptFiles;

    public DeleteEnv() {
        super();
    }

    @Override
    public String[] call() {
        List<String> envNames = new ArrayList<>();

        AkaConfig akaConfig = new AkaConfig().fromJson();

        for (File file : scriptFiles) {
            String envName = deleteEnvironment(file, akaConfig);
            envNames.add(envName);
        }

        Environment.setInstance(null);

        return envNames.toArray(new String[0]);
    }

    private String deleteEnvironment(File scriptFile, AkaConfig akaConfig) {
        logger.info("collecting environment data");
        EnvironmentAnalyzer analyzer = new EnvironmentAnalyzer();
        analyzer.analyze(scriptFile);
        EnvironmentRootNode root = (EnvironmentRootNode) analyzer.getRoot();
        Environment env = new Environment();
        env.setEnvironmentRootNode(root);

        String workspacePath = akaConfig.getWorkingDirectory() + File.separator + env.getName();
        String workspaceConfigPath = akaConfig.getWorkingDirectory() + File.separator
                + env.getName() + File.separator
                + WorkspaceConfig.WORKSPACE_CONFIG_NAME;

        logger.info("loading workspace");
        WorkspaceConfig workspaceConfig = new WorkspaceConfig().load(workspaceConfigPath);
        String physicalTreePath = workspaceConfig.getPhysicalJsonFile();

        INode projectRoot = new PhysicalTreeImporter().importTree(new File(physicalTreePath));
        env.setProjectNode((ProjectNode) projectRoot);

        Environment.setInstance(env);

        logger.info("delete cloned source code file of " + env.getName());
        deleteClonedSourceFile(env);

        logger.info("delete workspace " + env.getName());
        Utils.deleteFileOrFolder(new File(workspacePath));

        logger.info("delete test script file " + new File(workspaceConfig.getTestscriptFile()).getName());
        Utils.deleteFileOrFolder(new File(workspaceConfig.getTestscriptFile()));

        logger.info("delete environment script file " + scriptFile.getName());
        Utils.deleteFileOrFolder(scriptFile);

        return env.getName();
    }

    private void deleteClonedSourceFile(Environment env) {
        List<INode> sources = Search.searchNodes(env.getProjectNode(), new SourcecodeFileNodeCondition());

        for (INode source : sources) {
            String clonedPath = ProjectClone.getClonedFilePath(source.getAbsolutePath());

            Utils.deleteFileOrFolder(new File(clonedPath));
            logger.info("\t" + source.getName() + " - done");
        }
    }
}
