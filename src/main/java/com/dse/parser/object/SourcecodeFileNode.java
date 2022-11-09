package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.util.Date;

public abstract class SourcecodeFileNode<N extends IASTTranslationUnit> extends Node
        implements IHasFileNode, ISourcecodeFileNode {
    // true if the current node is analyzed include dependency before
    protected boolean includeHeaderDependencyState = false;

    // true if the current node is expaned down to method level before
    protected boolean expandedToMethodLevelState = false;

    protected Date lastModifiedDate;

    protected String md5;

    protected N AST;

    @Override
    public void setAbsolutePath(String absolutePath) {
        super.setAbsolutePath(absolutePath);
//        Utils.computeMd5(Utils.readFileContent(absolutePath));
    }

    @Override
    public N getAST() {
        return this.AST;
    }

    @Override
    public void setAST(IASTTranslationUnit aST) {
        this.AST = (N) aST;
    }

    @Override
    public File getFile() {
        return new File(getAbsolutePath());
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isIncludeHeaderDependencyState() {
        return includeHeaderDependencyState;
    }

    public void setIncludeHeaderDependencyState(boolean includeHeaderDependencyState) {
        this.includeHeaderDependencyState = includeHeaderDependencyState;
    }

    public boolean isExpandedToMethodLevelState() {
        return expandedToMethodLevelState;
    }

    public void setExpandedToMethodLevelState(boolean expandedToMethodLevelState) {
        this.expandedToMethodLevelState = expandedToMethodLevelState;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
