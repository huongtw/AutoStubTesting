package com.dse.parser;

import com.dse.parser.dependency.RealParentDependencyGeneration;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.parser.dependency.*;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.*;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectParser {
    final static UILogger uiLogger = UILogger.getUiLogger();
    final static AkaLogger logger = AkaLogger.get(ProjectParser.class);

    protected File projectPath;
    protected List<File> ignoreFolders = new ArrayList<>();
    private ProjectNode root;

    private boolean expandTreeuptoMethodLevel_enabled = false;
    private boolean cpptoHeaderDependencyGeneration_enabled = false;
    private boolean parentReconstructor_enabled = false;
    private boolean extendedDependencyGeneration_enabled = false;
    private boolean generateSetterandGetter_enabled = false;
    private boolean funcCallDependencyGeneration_enabled = false;
    private boolean globalVarDependencyGeneration_enabled = false;
    private boolean sizeOfDependencyGeneration_enabled = false;
    private boolean typeDependency_enable = false;
    private boolean typedefDependency_enable = true;

    public static void main(String[] args) throws Exception {
//		IASTNode ast = Utils.convertToIAST("Hello<int, float[]>*[5]");
//
//		if (ast instanceof IASTDeclarationStatement)
//			ast = ((IASTDeclarationStatement) ast).getDeclaration();
//
//		VariableNode variableNode = new VariableNode();
//		variableNode.setAST(ast);

        ProjectParser projectParser = new ProjectParser(new File("/mnt/e/akautauto/datatest/lamnt/macro/"));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        INode foo = Search.searchNodes(projectRoot, new VariableNodeCondition(), "foo(Scope::B,Color)/sc").get(0);

        INode type = ((VariableNode) foo).resolveCoreType();
//
//		INode source = Utils.getSourcecodeFile(foo);
//
//		String content = Utils.readFileContent(source);
//
//		int offset = ((FunctionNode) foo).getAST().getFileLocation().getNodeOffset();
//
//		content = content.substring(0, offset) + "XSGVDV";
//
//		VariableSearchingSpace space = new VariableSearchingSpace(foo);
//
//		DeclSpecSearcher searcher = new DeclSpecSearcher("Pair<.+>", space.generateExtendSpaces(), false);
//
//		/*
//		  Display tree of project
//		 */
//		System.out.println(new DependencyTreeDisplayer(projectRoot).getTreeInString());
    }

    public ProjectParser(ProjectNode root) {
        this.setRoot(root);
    }

    public ProjectParser(File projectPath) {
        this.projectPath = projectPath;
        ProjectLoader loader = new ProjectLoader();
        loader.setIgnoreFolders(getIgnoreFolders());
        logger.debug("Ignore folders: " + getIgnoreFolders());

        logger.debug("Loading the project " + projectPath.getAbsolutePath());
        setRoot(loader.load(projectPath));
        logger.debug("Loaded the project " + projectPath.getAbsolutePath() + " successfully");
    }

    public ProjectParser(IProjectNode root) {
        this.projectPath = new File(root.getAbsolutePath());
    }

    public ProjectNode getRootTree() {
        if (root != null) {
            List<INode> sourcecodeFileNodes = Search.searchNodes(root, new SourcecodeFileNodeCondition());
            if (expandTreeuptoMethodLevel_enabled)
                try {
                    uiLogger.log("calling expandTreeuptoMethodLevel");
                    logger.debug("calling expandTreeuptoMethodLevel");
                    for (INode sourceCodeNode : sourcecodeFileNodes)
                        if (sourceCodeNode instanceof ISourcecodeFileNode) {
                            try {
                                ISourcecodeFileNode fNode = (ISourcecodeFileNode) sourceCodeNode;
                                File dir = fNode.getFile();

                                // if the source code file is not expanded to method level
                                if (dir != null && !((ISourcecodeFileNode) sourceCodeNode).isExpandedToMethodLevelState()) {
                                    SourcecodeFileParser cppParser = new SourcecodeFileParser();
                                    cppParser.setSourcecodeNode((ISourcecodeFileNode) sourceCodeNode);
                                    INode sourcecodeTreeRoot = cppParser.generateTree(false);
                                    fNode.setAST(cppParser.getTranslationUnit());

                                    if (sourcecodeTreeRoot != null && sourcecodeTreeRoot.getChildren() != null)
                                        for (Node sourcecodeItem : sourcecodeTreeRoot.getChildren()) {
                                            List<INode> nodes = Search.searchNodes(sourceCodeNode, new NodeCondition(), sourcecodeItem.getAbsolutePath());
                                            if (nodes.isEmpty()) {
                                                sourceCodeNode.getChildren().add(sourcecodeItem);
                                                sourcecodeItem.setParent(sourceCodeNode);
                                            } else {
                                                nodes.removeIf(node -> !node.getClass().isInstance(sourcecodeItem));
                                                if (nodes.isEmpty()) {
                                                    sourceCodeNode.getChildren().add(sourcecodeItem);
                                                    sourcecodeItem.setParent(sourceCodeNode);
                                                }
                                            }
//											// due to reanalyzing dependencies, some sourcecodeItem are re generate,
//											// so need to check if the sourcecodeItems are existed or not
//											List<INode> nodes = Search.searchNodes(sourceCodeNode, new NodeCondition(), sourcecodeItem.getAbsolutePath());
//											if (nodes.isEmpty()) {
//												sourceCodeNode.getChildren().add(sourcecodeItem);
//												sourcecodeItem.setParent(sourceCodeNode);
//											}
//
//											nodes.forEach(node -> {
//												if (!node.getClass().isInstance(sourcecodeItem) || node == sourcecodeItem) {
//													sourceCodeNode.getChildren().add(sourcecodeItem);
//													sourcecodeItem.setParent(sourceCodeNode);
//												}
//											});
////											if (nodes.size() == 0) {
////												sourceCodeNode.getChildren().add(sourcecodeItem);
////												sourcecodeItem.setParent(sourceCodeNode);
////											}
                                        }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            // STEP
            if (cpptoHeaderDependencyGeneration_enabled) {
                uiLogger.log("calling CpptoHeaderDependencyGeneration");
                logger.debug("calling CpptoHeaderDependencyGeneration");

                for (INode cppFileNode : sourcecodeFileNodes)
                    try {
                        new IncludeHeaderDependencyGeneration().dependencyGeneration(cppFileNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }

            // STEP
            if (expandTreeuptoMethodLevel_enabled)
                try {
                    uiLogger.log("calling expandTreeuptoMethodLevel");
                    logger.debug("calling expandTreeuptoMethodLevel");
                    for (INode sourceCodeNode : sourcecodeFileNodes)
                        if (sourceCodeNode instanceof ISourcecodeFileNode) {
                            try {
                                ISourcecodeFileNode fNode = (ISourcecodeFileNode) sourceCodeNode;
                                File dir = fNode.getFile();
                                // if the source code file is not expanded to method level
                                if (dir != null && !((ISourcecodeFileNode) sourceCodeNode).isExpandedToMethodLevelState()) {
                                    Map<String, String> macrosValue = Utils.getIncludeHeaderMacrosValue(fNode);
                                    SourcecodeFileParser cppParser = new SourcecodeFileParser();
                                    cppParser.appendMacroDefinition(macrosValue);
                                    cppParser.setSourcecodeNode((ISourcecodeFileNode) sourceCodeNode);
                                    INode sourcecodeTreeRoot = cppParser.generateTree();
                                    fNode.setAST(cppParser.getTranslationUnit());
                                    for (int i=sourceCodeNode.getChildren().size()-1;i>=0;i--){
                                        boolean contain = false;
                                        for (INode node1 : sourcecodeTreeRoot.getChildren()) {
                                            if (sourceCodeNode.getChildren().get(i).equals(node1)) {
                                                contain = true;
                                                break;
                                            }
                                        }
                                        if (!contain) {
                                            sourceCodeNode.getChildren().remove(i);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            // STEP
            if (extendedDependencyGeneration_enabled) {
                uiLogger.log("calling ExtendedDependencyGeneration");
                logger.debug("calling ExtendedDependencyGeneration");
                List<SearchCondition> conditions = new ArrayList<>();
                conditions.add(new ClassNodeCondition());
                conditions.add(new StructNodeCondition());
//				conditions.add(new NamespaceNodeCondition());
                List<INode> nodes = Search.searchNodes(root, conditions);
                nodes = nodes.stream().filter(node -> !(node.getParent() instanceof ClassNode))
                        .collect(Collectors.toList()); // why?
                for (INode node : nodes)
                    try {
                        new ExtendedDependencyGeneration().dependencyGeneration(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }

            // STEP
            if (parentReconstructor_enabled) {
                uiLogger.log("calling ParentReconstructor");
                logger.debug("calling ParentReconstructor");
                List<INode> functionNodes = Search.searchNodes(root, new AbstractFunctionNodeCondition());
                for (INode function : functionNodes)
                    if (function instanceof AbstractFunctionNode)
                        try {
                            new RealParentDependencyGeneration().dependencyGeneration(function);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
            }

            // STEP
            if (generateSetterandGetter_enabled) {
                uiLogger.log("calling generateSetterandGetter");
                logger.debug("calling generateSetterandGetter");
                List<INode> variableNodes = Search.searchNodes(root, new VariableNodeCondition());

                for (INode var : variableNodes)
                    if (var instanceof AttributeOfStructureVariableNode) {
                        try {
                            new SetterGetterDependencyGeneration().dependencyGeneration(var);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }

            // STEP
            List<INode> functionNodes = Search.searchNodes(root, new FunctionNodeCondition());
            if (funcCallDependencyGeneration_enabled) {
                uiLogger.log("calling funcCallDependencyGeneration");
                logger.debug("calling funcCallDependencyGeneration");
                for (INode node : functionNodes) {
                    if (node instanceof IFunctionNode) {
                        try {
                            logger.debug("Analyzing function " + node.getAbsolutePath());
                            new FunctionCallDependencyGeneration().dependencyGeneration(node);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // STEP
            if (globalVarDependencyGeneration_enabled) {
                uiLogger.log("calling globalVarDependencyGeneration");
                logger.debug("calling globalVarDependencyGeneration");
                for (INode node : functionNodes) {
                    if (node instanceof IFunctionNode)
                        try {
                            new GlobalVariableDependencyGeneration().dependencyGeneration(node);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }

            // STEP
            // Find dependency between arguments in a function: a pointer/array and its size
            if (isSizeOfDependencyGeneration_enabled()) {
                uiLogger.log("calling sizeDependencyGeneration");
                logger.debug("calling sizeDependencyGeneration");
                for (INode node : functionNodes) {
                    if (node instanceof IFunctionNode && !(node instanceof ConstructorNode))
                        try {
                            new SizeOfArrayDepencencyGeneration().dependencyGeneration(node);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }

            // STEP
            if (typeDependency_enable) {
                uiLogger.log("calling typeDpendency");
                logger.debug("calling typeDpendency");
                for (INode node : functionNodes) {
                    if (node instanceof IFunctionNode)
                        for (IVariableNode var : ((IFunctionNode) node).getArguments())
                            try {
                                CTypeDependencyGeneration gen = new CTypeDependencyGeneration();
                                gen.setAddToTreeAutomatically(true);
                                gen.dependencyGeneration(var);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                }
            }

            if (typedefDependency_enable) {
                uiLogger.log("calling typedefDependency");
                logger.debug("calling typedefDependency");

                List<SearchCondition> conditions = new ArrayList<>();
                conditions.add(new EnumNodeCondition());
                conditions.add(new StructNodeCondition());
                conditions.add(new UnionNodeCondition());
                List<INode> structures = Search.searchNodes(root, conditions);

                List<INode> allTypedefs = Search.searchNodes(root, new TypedefNodeCondition());
                TypedefDependencyGeneration gen = new TypedefDependencyGeneration(allTypedefs);

                for (INode node : structures) {
                    gen.dependencyGeneration(node);
                }
            }
        }

        return root;
    }

    public boolean isExpandTreeuptoMethodLevel_enabled() {
        return expandTreeuptoMethodLevel_enabled;
    }

    public void setExpandTreeuptoMethodLevel_enabled(boolean expandTreeuptoMethodLevel_enabled) {
        this.expandTreeuptoMethodLevel_enabled = expandTreeuptoMethodLevel_enabled;
    }

    public boolean isCpptoHeaderDependencyGeneration_enabled() {
        return cpptoHeaderDependencyGeneration_enabled;
    }

    public void setCpptoHeaderDependencyGeneration_enabled(boolean cpptoHeaderDependencyGeneration_enabled) {
        this.cpptoHeaderDependencyGeneration_enabled = cpptoHeaderDependencyGeneration_enabled;
    }

    public boolean isParentReconstructor_enabled() {
        return parentReconstructor_enabled;
    }

    public void setParentReconstructor_enabled(boolean parentReconstructor_enabled) {
        this.parentReconstructor_enabled = parentReconstructor_enabled;
    }

    public boolean isExtendedDependencyGeneration_enabled() {
        return extendedDependencyGeneration_enabled;
    }

    public void setExtendedDependencyGeneration_enabled(boolean extendedDependencyGeneration_enabled) {
        this.extendedDependencyGeneration_enabled = extendedDependencyGeneration_enabled;
    }

    public boolean isGenerateSetterandGetter_enabled() {
        return generateSetterandGetter_enabled;
    }

    public void setGenerateSetterandGetter_enabled(boolean generateSetterandGetter_enabled) {
        this.generateSetterandGetter_enabled = generateSetterandGetter_enabled;
    }

    public boolean isFuncCallDependencyGeneration_enabled() {
        return funcCallDependencyGeneration_enabled;
    }

    public void setFuncCallDependencyGeneration_enabled(boolean funcCallDependencyGeneration_enabled) {
        this.funcCallDependencyGeneration_enabled = funcCallDependencyGeneration_enabled;
    }

    public boolean isGlobalVarDependencyGeneration_enabled() {
        return globalVarDependencyGeneration_enabled;
    }

    public void setGlobalVarDependencyGeneration_enabled(boolean globalVarDependencyGeneration_enabled) {
        this.globalVarDependencyGeneration_enabled = globalVarDependencyGeneration_enabled;
    }

    public boolean isSizeOfDependencyGeneration_enabled() {
        return sizeOfDependencyGeneration_enabled;
    }

    public void setSizeOfDependencyGeneration_enabled(boolean sizeOfDependencyGeneration_enabled) {
        this.sizeOfDependencyGeneration_enabled = sizeOfDependencyGeneration_enabled;
    }

    public ProjectNode getRoot() {
        return root;
    }

    public void setRoot(ProjectNode root) {
        this.root = root;
    }

    public File getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(File projectPath) {
        this.projectPath = projectPath;
    }

    public List<File> getIgnoreFolders() {
        return ignoreFolders;
    }

    public void setIgnoreFolders(List<File> ignoreFolders) {
        this.ignoreFolders = ignoreFolders;
    }

    public void setTypeDependency_enable(boolean typeDependency_enable) {
        this.typeDependency_enable = typeDependency_enable;
    }

    public boolean isTypeDependency_enable() {
        return typeDependency_enable;
    }
}