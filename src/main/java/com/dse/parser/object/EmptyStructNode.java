package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

import javax.swing.*;
import java.io.File;

/**
 * Represent empty struct node
 * <p>
 * Ex:
 * <p>
 * <p>
 * <pre>
 * struct SinhVien;
 * </pre>
 *
 * @author lamnt
 */
public class EmptyStructNode extends StructNode implements IEmptyStructureNode {

    @Override
    public String getNewType() {
        return ((IASTElaboratedTypeSpecifier) getAST().getDeclSpecifier()).getName().toString();
    }

    @Override
    public IASTFileLocation getNodeLocation() {
        return ((IASTElaboratedTypeSpecifier) getAST().getDeclSpecifier()).getName().getFileLocation();
    }

    @Override
    public File getSourceFile() {
        return new File(getAST().getContainingFilename());
    }

    @Override
    public String toString() {
        return /* "struct " + */ super.toString();
    }
}
