package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.util.Date;

public interface ISourcecodeFileNode extends INode {

    <N extends IASTTranslationUnit> N getAST();

    void setAST(IASTTranslationUnit aST);

    @Override
    String toString();

    File getFile();

    boolean isExpandedToMethodLevelState();

    void setExpandedToMethodLevelState(boolean b);

    String getMd5();

    void setLastModifiedDate(Date date);

    void setMd5(String checksum);

    Date getLastModifiedDate();

    boolean isIncludeHeaderDependencyState();

    void setIncludeHeaderDependencyState(boolean b);
}