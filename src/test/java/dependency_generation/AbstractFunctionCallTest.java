package dependency_generation;

import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class AbstractFunctionCallTest {

    /**
     * Find every called function by callee which have absolute path end with calledPath.
     *
     * @param callee     function
     * @param calledPath The path of called path should be start from the root of tested project.
     * @return list of called function node which have absolute path end with calledPath.
     */
    protected List<ICommonFunctionNode> findCalledFunctions(IFunctionNode callee, String calledPath) {
        List<ICommonFunctionNode> calledFuntions = new ArrayList<>();

        for (Dependency d : callee.getDependencies())
            if (d instanceof FunctionCallDependency)
                if (Utils.normalizePath(d.getEndArrow().getAbsolutePath()).endsWith(Utils.normalizePath(calledPath)))
                    calledFuntions.add((ICommonFunctionNode) d.getEndArrow());

        return calledFuntions;
    }

    protected IFunctionNode warm(String srcPath, String calleeFuncPath) {
        File path = new File(Utils.normalizePath(srcPath));
        ProjectParser parser = new ProjectParser(path);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setParentReconstructor_enabled(true);
        INode root = parser.getRootTree();

//        System.out.println(new DependencyTreeDisplayer(root).getTreeInString());

        List<INode> functionNodes = Search.searchNodes(root, new AbstractFunctionNodeCondition(), calleeFuncPath);
        assertEquals(functionNodes.size(), 1);

        INode firstNode = functionNodes.get(0);
        assertEquals(firstNode instanceof IFunctionNode, true);

        return (IFunctionNode) firstNode;
    }
}
