package com.dse.guifx_v3.objects;

import com.dse.parser.object.INode;
import com.dse.util.PathUtils;
import javafx.scene.control.Tab;
import org.fxmisc.richtext.CodeArea;

import java.util.Objects;

public class SourceCodeViewTab extends Tab {

    private INode sourceCodeFileNode;
    private CodeArea codeArea;

    public SourceCodeViewTab(INode sourceCodeFileNode) {
        this.sourceCodeFileNode = sourceCodeFileNode;
    }

    public INode getSourceCodeFileNode() {
        return sourceCodeFileNode;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    public void setSourceCodeFileNode(INode sourceCodeFileNode) {
        this.sourceCodeFileNode = sourceCodeFileNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SourceCodeViewTab that = (SourceCodeViewTab) o;

        if (sourceCodeFileNode == null || that.sourceCodeFileNode == null)
            return false;

        return PathUtils.equals(sourceCodeFileNode.getAbsolutePath(), that.sourceCodeFileNode.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        if (sourceCodeFileNode != null)
            return Objects.hash(sourceCodeFileNode.getAbsolutePath());
        else
            return super.hashCode();
    }
}
