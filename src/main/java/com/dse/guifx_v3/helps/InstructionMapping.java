package com.dse.guifx_v3.helps;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.HashMap;

public class InstructionMapping extends HashMap<String, Integer> {
    /**
     * Load the number of statements/branches
     */
    public static void loadInstructions(){
        // load the number of statements/branches/mcdcs
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        String nStmContent = Utils.readFileContent(new File(new WorkspaceConfig().fromJson().getnStatementPath()));
        InstructionMapping nStmObject = gson.fromJson(nStmContent, InstructionMapping.class);
        Environment.getInstance().setStatementsMapping(nStmObject);

        String nBranchContent = Utils.readFileContent(new File(new WorkspaceConfig().fromJson().getnBranchPath()));
        InstructionMapping nBranchObject = gson.fromJson(nBranchContent, InstructionMapping.class);
        Environment.getInstance().setBranchesMapping(nBranchObject);

        String nMcdcContent = Utils.readFileContent(new File(new WorkspaceConfig().fromJson().getnMcdcPath()));
        InstructionMapping nMcdcObject = gson.fromJson(nMcdcContent, InstructionMapping.class);
        Environment.getInstance().setMcdcMapping(nMcdcObject);

        String nBasicPathContent = Utils.readFileContent(new File(new WorkspaceConfig().fromJson().getnBasicPath()));
        InstructionMapping nBasicPathObject = gson.fromJson(nBasicPathContent, InstructionMapping.class);
        Environment.getInstance().setBasicPathMapping(nBasicPathObject);
    }
}
