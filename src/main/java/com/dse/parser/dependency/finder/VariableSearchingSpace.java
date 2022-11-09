package com.dse.parser.dependency.finder;

import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.NamespaceNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Get searching space of a node in the structure tree.
 * <p>
 * If the node is function/attribute, the searching space consists of the
 * containing file, and all included file (represented in #include)
 *
 * @author DucAnh
 */
public class VariableSearchingSpace implements IVariableSearchingSpace {

	private final static AkaLogger logger = AkaLogger.get(VariableSearchingSpace.class);

	public static int STRUCTUTRE_VS_NAMESPACE_INDEX = 0;

	public static int FILE_SCOPE_INDEX = 1;

	public static int INCLUDED_INDEX = 2;

	private List<Level> spaces = new ArrayList<>();

	public static List<INode> includeNodes = new ArrayList<>();

	public VariableSearchingSpace(INode startNode) {
		logger.setLevel(org.apache.log4j.Level.OFF);
//		if (startNode instanceof IFunctionNode || startNode instanceof VariableNode || startNode instanceof SourcecodeFileNode)
			spaces = generateSearchingSpace(startNode);
	}

	public static void main(String[] args) {
		ProjectParser projectParser = new ProjectParser(
				new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithmv2/src"));

		projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
		projectParser.setExpandTreeuptoMethodLevel_enabled(true);
		projectParser.setExtendedDependencyGeneration_enabled(true);
		projectParser.setTypeDependency_enable(false);

//		ProjectParser parser = new ProjectParser(new File(Paths.TSDV_R1));
		IFunctionNode function = (IFunctionNode) Search
				.searchNodes(projectParser.getRootTree(), new FunctionNodeCondition(), "push(struct Node,int)").get(0);

		System.out.println(function.getAST().getRawSignature());
		IVariableSearchingSpace searching = new VariableSearchingSpace(function);

		for (Level l : searching.getSpaces()) {
			System.out.println("*** Level ***");
			for (Node n : l)
				System.out.println(n.getAbsolutePath());
		}
	}

	private Level getAllCurrentClassvsStructvsNamespace(INode n) {
		Level output = new Level();

		INode parent;
		do {
			parent = Utils.getClassvsStructvsNamesapceNodeParent(n);

			if (parent != null) {
				if (!output.contains(parent))
					output.add((Node) parent);
				n = parent.getParent();
			}
		} while (parent != null);

		return output;
	}

	public static List<Node> getAllIncludedNodes(INode n) {
		List<Node> output = new ArrayList<>();

		if (n != null) {
//			logger.debug("getAllIncludedNodes from " + n.getAbsolutePath());
			try {
				for (Dependency child : n.getDependencies()) {
					if (child instanceof IncludeHeaderDependency) {
						if (child.getStartArrow().equals(n)) {
							includeNodes.add(n);

							INode end = child.getEndArrow();
							if (!includeNodes.contains(end)) {
								output.add((Node) end);
								/*
								 * In case recursive include
								 */
								output.addAll(getAllIncludedNodes(end));
							}
						}
					}
				}
			} catch (StackOverflowError e) {
				 e.printStackTrace();
			}
		}

		return output;
	}

	@Override
	public List<Level> getSpaces() {
		return spaces;
	}

	private Level extendLevel = new Level();

	@Override
	public List<Level> generateExtendSpaces() {
		List<Level> extended = new ArrayList<>();

		logger.debug("Search level 4");

		if (!spaces.isEmpty()) {
			for (Level level : spaces) {
				String lastLevelName = level.getName();

				if (lastLevelName.equals(Level.INCLUDED_SCOPE) || lastLevelName.equals(Level.FILE_SCOPE)) {
					extendLevel.setName(Level.EXTENDED_INCLUDED_SCOPE);

					for (INode node : level)
						getAllNodesInclude(node);

					logger.debug("Search level 4. Done.");

					if (extendLevel.size() > 0)
						extended.add(extendLevel);
					for (INode item:extendLevel)
						logger.debug("space element at level 4: " + item.getAbsolutePath());
				}
			}
		}

		extended.addAll(0, spaces);
		return extended;
	}

	private List<Node> getAllNodesInclude(INode n) {

		if (n != null && !ProjectClone.getLibraries().contains(n.getAbsolutePath())) {
//			logger.debug("getAllNodesInclude " + n.getAbsolutePath());
			try {
				for (Dependency child : n.getDependencies()) {
					if (child instanceof IncludeHeaderDependency) {
						if (child.getEndArrow().equals(n)) {

							INode start = child.getStartArrow();
							if (!extendLevel.contains(start)) {
								extendLevel.add((Node) start);
								/*
								 * In case recursive include
								 */
								for (Node node : getAllNodesInclude(start))
									if (!extendLevel.contains(node))
										extendLevel.add(node);
							}
						}
					}
				}
			} catch (StackOverflowError e) {
				 e.printStackTrace();
			}
		}

		return extendLevel;
	}

	/**
	 * Get the searching space of a node. Notice that the order of class/struct/file
	 * nodes in the structure is very important!
	 *
	 * @param n node
	 * @return searching space
	 */
	private List<Level> generateSearchingSpace(INode n) {
		List<Level> outputNodes = new ArrayList<>();
		if (n == null)
			return outputNodes;
		/*
		 * Firstly, we must all its parents that belong to class, struct or namespace
		 * (highest priority)
		 */
//		logger.debug("Creating searching space at level 1");
		INode parent = n.getParent();

		if (n instanceof AbstractFunctionNode)
			if (((AbstractFunctionNode) n).getRealParent() != null)
		    	parent = ((AbstractFunctionNode) n).getRealParent();

		Level level1 = null;
		logger.debug("Level 1: Get all current class/struct/namespace");
		if (parent instanceof ClassNode || parent instanceof StructNode
				|| parent instanceof NamespaceNode) {
			level1 = getAllCurrentClassvsStructvsNamespace(n);
			level1.setName(Level.STRUCTUTRE_AND_NAMESPACE_SCOPE);
			outputNodes.add(level1);
			for (INode item:level1)
				logger.debug("space element at level 1: " + item.getAbsolutePath());
		}
		VariableSearchingSpace.STRUCTUTRE_VS_NAMESPACE_INDEX = 0;
		/*
		 * Secondly, we get the containing file of the given node
		 */
//		logger.debug("Search level 2");
		logger.debug("Level 2: Get the source code file");
		INode sourceCodeFileNode = Utils.getSourcecodeFile(n);
		if (sourceCodeFileNode != null) {
			Level level2 = new Level();
			level2.add((Node) sourceCodeFileNode);
			level2.setName(Level.FILE_SCOPE);
			outputNodes.add(level2);
			for (INode item:level2)
				logger.debug("space element at level 2: " + item.getAbsolutePath());
			VariableSearchingSpace.FILE_SCOPE_INDEX = 1;
		}
		/*
		 * Finally, get all included file (lowest priority)
		 */
//		logger.debug("Search level 3");
		logger.debug("Level 3: Get all included files defined by users");
		List<Node> includedNodes = getAllIncludedNodes(sourceCodeFileNode);
		includeNodes = new ArrayList<>();
		if (includedNodes.size() > 0) {
			Level level3 = new Level(includedNodes);
			level3.setName(Level.INCLUDED_SCOPE);
			outputNodes.add(level3);
			for (INode item:level3)
				logger.debug("space element at level 3: " + item.getAbsolutePath());

			findAllNamespaceInIncluded(level3, level1);
		}
		VariableSearchingSpace.INCLUDED_INDEX = 2;
//		logger.debug("Search level 3. Done.");

		List<Node> dataUserCodes = getAllDataUserCodes();
		if (!dataUserCodes.isEmpty()) {
			Level level4 = new Level(dataUserCodes);
			level4.setName("DATA USER CODE");
			outputNodes.add(level4);
		}

		return outputNodes;
	}

	private List<Node> getAllDataUserCodes() {
		INode dataUserCodeRoot = Environment.getInstance().getUserCodeRoot();
		return Search.searchNodes(dataUserCodeRoot, new SourcecodeFileNodeCondition())
				.stream().map(n -> (Node) n)
				.collect(Collectors.toList());
	}

	private void findAllNamespaceInIncluded(Level level3, Level level1) {
		if (level1 == null)
			return;

		List<INode> namespaces = new ArrayList<>(level1);
		namespaces.removeIf(n -> !(n instanceof NamespaceNode));

		if (namespaces.isEmpty())
			return;

		INode sourceFileNode = Utils.getSourcecodeFile(namespaces.get(0));
		String sourceFilePath = sourceFileNode.getAbsolutePath();

		Map<String, INode> namespacePaths = new HashMap<>();
		for (INode namespace : namespaces) {
			String relativePath = namespace.getAbsolutePath().substring(sourceFilePath.length());
			namespacePaths.put(relativePath, namespace);
		}

		for (INode file : level3) {
			String filePath = file.getAbsolutePath();
			List<INode> namespacesInIncludedFile = Search.searchNodes(file, new NamespaceNodeCondition());

			for (INode namespace : namespacesInIncludedFile) {
				String relativePath = namespace.getAbsolutePath().substring(filePath.length());

				if (namespacePaths.containsKey(relativePath) && !level1.contains(namespace)) {
					INode correspondingNode = namespacePaths.get(relativePath);
					int index = level1.indexOf(correspondingNode);

					level1.add(index, (Node) namespace);
				}
			}
		}
	}

	public List<INode> search(String name, SearchCondition condition) {
		List<INode> result = new ArrayList<>();

		for (Level level : spaces) {
			for (INode node : level) {
				result.addAll(Search.searchNodes(node, condition, name));
			}
		}

		return result;
	}
}
