package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

import java.io.File;

/**
 * For example, <br/>
 * <p>
 * <p>
 * 
 * <pre>
 * union RGBA;
 * </pre>
 *
 * @author ducanhnguyen
 */
public class EmptyUnionNode extends UnionNode implements IEmptyStructureNode {

	@Override
	public String getNewType() {
		String name = ((IASTElaboratedTypeSpecifier) getAST().getDeclSpecifier()).getName().toString();
		/*
		 * Ex: union RGB
		 * 
		 * 
		 * Delete union keywork in name
		 */
		name = name.replaceAll("^union\\s*", "");
		return name;
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
		return getAST().getRawSignature();
	}

}
