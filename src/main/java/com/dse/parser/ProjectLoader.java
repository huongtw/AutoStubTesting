package com.dse.parser;

import com.dse.parser.object.*;
import com.dse.util.Utils;
import com.dse.util.tostring.NameDisplayer;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Construct structure tree corresponding to the given C/C++ project
 */
public class ProjectLoader implements IProjectLoader {
	final static AkaLogger logger = AkaLogger.get(ProjectLoader.class);
	private List<File> ignoreFolders = new ArrayList<>();
	private int ID = 0;
	// True: Parse the current folder and all its sub-directories
	private boolean recursive = true;

	public ProjectLoader() {
	}

	public static void main(String[] args) {
		ProjectLoader loader = new ProjectLoader();
		IProjectNode projectRootNode = loader.load(new File("datatest/duc-anh/SeparateFunctionTest"));

		/*
		  display tree of project
		 */
		NameDisplayer treeDisplayer = new NameDisplayer(projectRootNode);
		System.out.println(treeDisplayer.getTreeInString());
	}

	/**
	 * Generate the structure tree of the given project down to file level. Each
	 * node in the structure has an unique id
	 *
	 * @param projectPath
	 * @return
	 */
	@Override
	public ProjectNode load(File projectPath) {
		if (projectPath.exists()) {
			ProjectNode projectNode = new ProjectNode();
			try {
				projectNode.setAbsolutePath(Utils.normalizePath(projectPath.getCanonicalPath()));
				parseSrcFolder(projectNode, new File(Utils.normalizePath(projectNode.getAbsolutePath())));

				generateId(projectNode);
			} catch (IOException e) {
				 e.printStackTrace();
			}

			return projectNode;
		} else
			return null;
	}

	/**
	 * @param dir
	 *            Duong dan tuyet doi
	 * @return Cac duong dan tuyet doi cua cac thanh phan ben trong
	 */
	private ArrayList<String> getChildren(File dir) throws IOException {
		ArrayList<String> pathOfChildren = new ArrayList<>();
		String[] names = dir.list();

		for (String name : names)
			pathOfChildren.add(Utils.normalizePath(dir.getCanonicalPath() + File.separator + name));

		return pathOfChildren;
	}

	/**
	 * @param pathItem
	 *            Duong dan tuyet doi cua doi tuong
	 * @return Kieu doi tuong
	 */
	private int getTypeOfPath(String pathItem) {
//		if (pathItem.endsWith(IProjectLoader.CPP_IGNORE_FILE_SYMBOL) || pathItem.endsWith(IProjectLoader.C_IGNORE_FILE_SYMBOL))
//			return IProjectLoader.IGNORE_SOURCE_FILE;
//		else
		if (pathItem.endsWith(IProjectLoader.C_FILE_SYMBOL))
			return IProjectLoader.C_FILE;
		else if (pathItem.endsWith(IProjectLoader.CPP_FILE_SYMBOL) || pathItem.endsWith(IProjectLoader.CC_FILE_SYMBOL))
			return IProjectLoader.CPP_FILE;
		else if (pathItem.endsWith(IProjectLoader.HEADER_FILE_SYMBOL_TYPE_1)
				|| pathItem.endsWith(IProjectLoader.HEADER_FILE_SYMBOL_TYPE_2)
				|| pathItem.endsWith(IProjectLoader.HEADER_FILE_SYMBOL_TYPE_3))
			return IProjectLoader.HEADER_FILE;
		else if (pathItem.endsWith(IProjectLoader.EXE_SYMBOL))
			return IProjectLoader.EXE;
		else if (pathItem.endsWith(IProjectLoader.OBJECT_FILE_SYMBOL))
			return IProjectLoader.OBJECT;

		// check whether is folder
		File file = new File(pathItem);
		if (file.isDirectory())
			return IProjectLoader.FOLDER;

		return IProjectLoader.UNDEFINED_COMPONENT;
	}

	/**
	 * Bo qua folder khong can thiet
	 *
	 * @param dir
	 * @return
	 */
	private boolean isIgnoredComponent(File dir) {
		String absolutePath = dir.getAbsoluteFile().toString();

		for (String ignoredName : IProjectLoader.IGNORED_FILE_SYMBOLS)
			if (absolutePath.endsWith(ignoredName))
				return true;

		return false;
	}

	private void parseSrcFolder(Node parent, File path) throws IOException {
		logger.debug("Loading "  + path.getAbsolutePath());
		ArrayList<String> children = getChildren(path);

		for (String pathItem : children)
			if (!isIgnoredComponent(new File(pathItem)))
				switch (getTypeOfPath(pathItem)) {
				case C_FILE:
					CFileNode cNode = new CFileNode();
					cNode.setAbsolutePath(pathItem);
					cNode.setParent(parent);
					parent.getChildren().add(cNode);
					break;

				case CPP_FILE:
					CppFileNode cppNode = new CppFileNode();
					cppNode.setAbsolutePath(pathItem);
					cppNode.setParent(parent);
					parent.getChildren().add(cppNode);
					break;

				case HEADER_FILE:
					HeaderNode headerNode = new HeaderNode();
					headerNode.setAbsolutePath(pathItem);
					headerNode.setParent(parent);
					parent.getChildren().add(headerNode);
					break;

				case FOLDER:
					boolean isIgnore = false;
					for (File ignoreFolder : ignoreFolders)
						if (new File(pathItem).equals(ignoreFolder))
							isIgnore = true;
					if (!isIgnore) {
						FolderNode folderNode = new FolderNode();
						folderNode.setAbsolutePath(pathItem);
						folderNode.setParent(parent);
						parent.getChildren().add(folderNode);

						if (isRecursive())
							parseSrcFolder(folderNode, new File(pathItem));
					}
					break;

				case EXE:
					ExeNode exeFile = new ExeNode();
					exeFile.setAbsolutePath(pathItem);
					exeFile.setParent(parent);
					parent.getChildren().add(exeFile);
					break;

				case OBJECT:
					ObjectNode objectNode = new ObjectNode();
					objectNode.setAbsolutePath(pathItem);
					objectNode.setParent(parent);
					parent.getChildren().add(objectNode);
					break;

				case UNDEFINED_COMPONENT:
					UnknowObjectNode undefinedComponentNode = new UnknowObjectNode();
					undefinedComponentNode.setAbsolutePath(pathItem);
					undefinedComponentNode.setParent(parent);
					parent.getChildren().add(undefinedComponentNode);
					break;
				}
	}

	@Override
	public void generateId(INode root) {
		root.setId(ID++);
		for (INode child : root.getChildren())
			generateId(child);
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public List<File> getIgnoreFolders() {
		return ignoreFolders;
	}

	public void setIgnoreFolders(List<File> ignoreFolders) {
		this.ignoreFolders = ignoreFolders;
	}
}
