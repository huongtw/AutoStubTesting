package dependency_generation;

import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.INode;
import com.dse.parser.object.SourcecodeFileNode;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class AbstractIncludeHeaderTest {

    /**
     * @param sourceFile
     * @param headerFilePath
     * @return
     */
    protected INode findHeaderNode(INode sourceFile, String headerFilePath) {
        INode headerNode = null;

        for (Dependency d : sourceFile.getDependencies())
            if (d instanceof IncludeHeaderDependency)
                if (Utils.normalizePath(d.getEndArrow().getAbsolutePath()).endsWith(Utils.normalizePath(headerFilePath))) {
                    headerNode = d.getEndArrow();
                    break;
                }

        return headerNode;
    }

    protected INode warm(String srcPath, String sourceFilePath) {
        srcPath = Utils.normalizePath(srcPath);
        sourceFilePath = Utils.normalizePath(sourceFilePath);
        File path = new File(srcPath);
        ProjectParser parser = new ProjectParser(path);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);

        INode root = parser.getRootTree();

        List<INode> files = Search.searchNodes(root, new SourcecodeFileNodeCondition(), sourceFilePath);
        assertEquals(files.size(), 1);

        INode firstNode = files.get(0);
        assertEquals(firstNode instanceof SourcecodeFileNode, true);

        return firstNode;
    }
}
