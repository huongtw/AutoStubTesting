package com.dse.guifx_v3.controllers.object.unit_node;

import com.dse.guifx_v3.controllers.object.build_environment.UnitNamesPath;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.logger.AkaLogger;

public class UnitUnderTestNode extends AbstractUnitNode {
    final static AkaLogger logger = AkaLogger.get(UnitUnderTestNode.class);

    private String name;
    private String absolutePath;
    private UnitNamesPath unitNamesPath;
    private ISourcecodeFileNode sourcecodeFileNode;
    private String stubType = UUT;

    public UnitUnderTestNode(UnitNamesPath unitNamesPath) {
        this.unitNamesPath = unitNamesPath;
        this.name = unitNamesPath.getName();
        this.absolutePath = unitNamesPath.getAbsolutePath();
        this.sourcecodeFileNode = unitNamesPath.getSourcecodeFileNode();
    }

    public void loadDependencies(ISourcecodeFileNode sourcecodeFileNode){
        // add dependencies
        for (Dependency child : sourcecodeFileNode.getDependencies())
            if (child instanceof IncludeHeaderDependency) {
                // Get dependent header files
                IncludeHeaderDependency cast = (IncludeHeaderDependency) child;

                if (cast.getStartArrow().equals(sourcecodeFileNode)) {
                    INode dependentHeaderFile = cast.getEndArrow();

                    if (dependentHeaderFile instanceof ISourcecodeFileNode) {
                        DependencyNode newNode = new DependencyNode((ISourcecodeFileNode) dependentHeaderFile);
                        newNode.loadDependencies(newNode.getSourcecodeFileNode());
                        logger.debug("Found a dependency:" + child.getStartArrow().getAbsolutePath() + "->" + child.getEndArrow().getAbsolutePath());
                        getChildren().add(newNode);
                    }
                    // TODO: get source code file corresponding to header files
                }
            }
    }
    public String getName() {
        return sourcecodeFileNode.getName();
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getStubType() {
        return stubType;
    }

    public UnitNamesPath getUnitNamesPath() {
        return unitNamesPath;
    }

    public void setStubType(String stubType) {
        this.stubType = stubType;
    }

    public ISourcecodeFileNode getSourcecodeFileNode() {
        return sourcecodeFileNode;
    }

    public void setSourcecodeFileNode(ISourcecodeFileNode sourcecodeFileNode) {
        this.sourcecodeFileNode = sourcecodeFileNode;
    }

    public void makeAllChildrenStubByPrototype() {
        for (AbstractUnitNode node : getChildren()) {
            ((DependencyNode) node).setType(DependencyNode.STUB_BY_PROTOTYPE);
        }
    }

    public void makeAllChildrenDontStub() {
        for (AbstractUnitNode node : getChildren()) {
            ((DependencyNode) node).setType(DependencyNode.DONT_STUB);
            ((DependencyNode) node).makeAllChildrenDontStub();
        }
    }


    public final static String UUT = "Set as Unit Under Test (UUT)";
    public final static String SBF = "Set as Stub By Function (SBF)";
    public final static String DONT_STUB = "Do not stub this file ";
    public final static String IGNORE = "Ignore this file";
}
