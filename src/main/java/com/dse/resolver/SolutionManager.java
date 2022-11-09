package com.dse.resolver;

import com.dse.compiler.message.error_tree.node.UndeclaredStructureMemberErrorNode;
import com.dse.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SolutionManager {
    private static SolutionManager instance;

    private List<ResolvedSolution> solutions = new ArrayList<>();

    public static SolutionManager getInstance() {
        if (instance == null)
            instance = new SolutionManager();

        return instance;
    }

    public void use(ResolvedSolution solution) {
        String sourceCode = Utils.readFileContent(solution.getSourcecodeFile());

        StringBuilder builder = new StringBuilder(sourceCode);

        String resolvedSourceCode = solution.getResolvedSourceCode();

        if (solution.getErrorNode() instanceof UndeclaredStructureMemberErrorNode)
            resolvedSourceCode = "public: " + resolvedSourceCode;

        builder.insert(solution.getOffset(), resolvedSourceCode + "\n");

        Utils.writeContentToFile(builder.toString(), solution.getSourcecodeFile());
    }

    public void updateOffset(String filePath) {
        for (ResolvedSolution solution : solutions) {
//            if ()
        }
    }

    public List<ResolvedSolution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<ResolvedSolution> solutions) {
        this.solutions = solutions;
    }
}
