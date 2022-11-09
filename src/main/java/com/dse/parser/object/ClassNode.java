package com.dse.parser.object;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

public class ClassNode extends StructOrClassNode implements ISourceNavigable {

	public ClassNode() {
		try {
			Icon ICON_CLASS = new ImageIcon(Node.class.getResource("/image/node/ClassNode.png"));
			setIcon(ICON_CLASS);
		} catch (Exception e) {
		}
	}

	@Override
	public String getNewType() {
		return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier()).getName().toString();
	}

	@Override
	public IASTFileLocation getNodeLocation() {
		return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier()).getName().getFileLocation();
	}

	@Override
	public File getSourceFile() {
		return new File(getAST().getContainingFilename());
	}

	public boolean isTemplate() {
		return getAST().getParent() instanceof ICPPASTTemplateDeclaration;
	}

	public IASTCompositeTypeSpecifier getSpecifiedAST() {
		return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier());
	}

	@Override
	public String toString() {
		return this.getNewType();
	}

	@Override
	public INode clone() {
		ClassNode clone = new ClassNode();
		clone.setAST(getAST());
		clone.setName(getName());
		clone.setAbsolutePath(getAbsolutePath());
		clone.setParent(getParent());
		return clone;
	}
}
