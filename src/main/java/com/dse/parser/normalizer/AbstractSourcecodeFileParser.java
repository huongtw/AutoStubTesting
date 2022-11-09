package com.dse.parser.normalizer;

import java.io.File;

import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.SourcecodeFileNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Abstract class for parsing source code file, e.g., *.c, *.cpp, *.h
 *
 * @author ducanhnguyen
 */
public abstract class AbstractSourcecodeFileParser extends AbstractParser {

    protected ISourcecodeFileNode sourcecodeNode;

    protected IASTTranslationUnit translationUnit;

    public IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    public void setTranslationUnit(IASTTranslationUnit translationUnit) {
        this.translationUnit = translationUnit;
    }

    public File getSourcecodeFile() {
        return new File(sourcecodeNode.getAbsolutePath());
    }

    public void setSourcecodeFile(File sourcecodeFile) {
        // nothing to do
    }

    public ISourcecodeFileNode getSourcecodeNode() {
        return sourcecodeNode;
    }

    public void setSourcecodeNode(ISourcecodeFileNode sourcecodeNode) {
        this.sourcecodeNode = sourcecodeNode;
    }
}
