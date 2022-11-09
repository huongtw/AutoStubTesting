package com.dse.thread.task;

import com.dse.compiler.Compiler;
import com.dse.compiler.message.ICompileMessage;
import com.dse.config.CommandConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.project_init.ProjectClone;
import com.dse.thread.AbstractAkaTask;
import com.dse.util.Utils;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class CompileSourcecodeFilesTask extends AbstractAkaTask<Boolean> {

    /**
     *
     * @return false if we found compilation error
     */
    @Override
    protected Boolean call() {
        // some changes make a source code file unable to compile
        StringBuilder error = new StringBuilder();

        boolean foundError = false;

        WorkspaceConfig workspaceConfig = new WorkspaceConfig().fromJson();

        String commandFilePath = workspaceConfig.getCommandFile();
        CommandConfig commandConfig = new CommandConfig().fromJson(commandFilePath);

        int total = commandConfig.getCompilationCommands().size();
        AtomicInteger counter = new AtomicInteger();

        for (String filePath : commandConfig.getCompilationCommands().keySet()) {
            if (!isCancelled()) {
                Compiler c = Environment.getInstance().getCompiler();
                String outPath = ProjectClone.getClonedFilePath(filePath) + c.getOutputExtension();
                ICompileMessage message = c.compile(filePath, outPath);
                if (message.getType() == ICompileMessage.MessageType.ERROR) {
                    error.append(filePath)
                            .append("\nMESSSAGE:\n")
                            .append(message.getMessage()).append("\n----------------\n");
                    foundError = true;

                }
                updateProgress(counter.incrementAndGet(), total);
            }
        }

        if (foundError) {
            String compilationMessageFile = workspaceConfig.getCompilationMessageWhenComplingProject();
            Utils.deleteFileOrFolder(new File(compilationMessageFile));
            Utils.writeContentToFile(error.toString(), compilationMessageFile);
            UIController.showDetailDialog(Alert.AlertType.ERROR,
                    "Compilation error",
                    "Unable to compile the environment",
                    error.toString());
            Environment.restoreEnvironment();
        }

        return foundError;
    }
}
