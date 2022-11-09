package com.dse.parser.object;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.search.Search;
import com.dse.search.condition.IncludeHeaderNodeCondition;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import javax.swing.*;
import java.io.File;

public class HeaderNode extends SourcecodeFileNode<IASTTranslationUnit> {
    public static final String HEADER_SIGNALS = ".h";

    public HeaderNode() {
        try {
            Icon ICON_HEADER = new ImageIcon(Node.class.getResource("/image/node/HeaderNode.png"));
            setIcon(ICON_HEADER);
        } catch (Exception e) {

        }
    }

    @Override
    public File getFile() {
        return new File(getAbsolutePath());
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(
                Paths.JOURNAL_TEST));
        Node root = parser.getRootTree();

        IncludeHeaderNode includeHeaderNode = (IncludeHeaderNode) Search
                .searchNodes(root, new IncludeHeaderNodeCondition()).get(1);

        IASTPreprocessorIncludeStatement ast = includeHeaderNode.getAST();
        ASTNodeProperty property = ast.getPropertyInParent();
        System.out.println();
    }
}
