package com.dse.parser.object;

import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

import java.io.File;

public interface ISourceNavigable {

    IASTFileLocation getNodeLocation();

    File getSourceFile();
}
