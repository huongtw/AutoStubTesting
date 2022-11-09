package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

import java.io.File;

/**
 * * Ex:
 * <p>
 * <p>
 * <pre>
 * typedef enum XXXX {
 * int x;
 * } myEnum;
 * </pre>
 * <p>
 * Represent XXXX
 *
 * Created by DucToan on 13/07/2017.
 */
public class EnumTypedefNode extends EnumNode {
    public EnumTypedefNode(){
        super();
    }

    @Override
    public String getNewType() {
        return getAST().getDeclarators()[0].getRawSignature();
    }

    @Override
    public IASTFileLocation getNodeLocation() {
        return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier()).getName().getFileLocation();
    }

    @Override
    public File getSourceFile() {
        return new File(getAST().getContainingFilename());
    }

    @Override
    public String toString() {
        return /* "enum " + */ super.toString();
    }
}
