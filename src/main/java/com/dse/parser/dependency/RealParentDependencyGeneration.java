package com.dse.parser.dependency;

import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.ClassvsStructvsNamespaceCondition;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.SpecialCharacter;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

import java.io.File;
import java.util.List;

/**
 * Get the real parent of a function.
 *
 * I do not consider DefinitionFunctionNode.
 *
 * I just consider ConstructorNode, DescontructorNode, and FunctionNode
 *
 * Real parent is physical parent if the function belongs to file level (not in any class/namespace)
 * For example: source code file x.cpp
 #include "../Person.h"
 int Person::getDoubleWeight(){...} // this function is defined in the class .../Person.h/Person
 *
 *
 * The function Person::getDoubleWeight() has a physical parent: x.cpp
 *
 * The function Person::getDoubleWeight() has a real parent (or logical parent): .../Person.h/Person
 */
public class RealParentDependencyGeneration extends AbstractDependencyGeneration {
    final static AkaLogger logger = AkaLogger.get(RealParentDependencyGeneration.class);

    public RealParentDependencyGeneration() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        ProjectParser projectParser = new ProjectParser(new File("/home/lamnt/IdeaProjects/akautauto/datatest/cpp-httplib"));
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
//        projectParser.setParentReconstructor_enabled(true);

        INode root = projectParser.getRootTree();
        IFunctionNode sampleNode = (IFunctionNode) Search.searchNodes(root,
                new FunctionNodeCondition(), "ClientImpl::send(const Request&,Response&)").get(0);
        new RealParentDependencyGeneration().dependencyGeneration(sampleNode);
        System.out.println(sampleNode.getRealParent().getAbsolutePath());
    }

    @Override
    public void dependencyGeneration(INode functionNode) {
        if (functionNode instanceof AbstractFunctionNode) {

            AbstractFunctionNode f = (AbstractFunctionNode) functionNode;

            /*
             * Ex: void SinhVien::timSinhVien(int msv){...}
             *
             */
            if (f.getSimpleName().contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)) {
                // the above code is disable. Not sure why we need this code because reconstructWithAST is enough!
//                StringBuilder address = new StringBuilder();
//
//                String[] elements = f.getSimpleName().split(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS);
//                for (String element : elements)
//                    if (f.getSimpleName().contains(element + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
//                        address.append(element).append(File.separator);
//
//                address = new StringBuilder(address.toString().replaceAll(File.separator + File.separator + "$", ""));
//                /*
//                 * Search
//                 */
//                VariableNode vituralVar = new VariableNode();
//                vituralVar.setRawType(address.toString());
//                vituralVar.setCoreType(address.toString());
//                vituralVar.setReducedRawType(address.toString());
//                vituralVar.setParent(f);
//
//                INode realParentNode = vituralVar.resolveCoreType();
//                /*
//                 *
//                 */
//                if (realParentNode instanceof ClassNode || realParentNode instanceof NamespaceNode) {
//                    f.setRealParent(realParentNode);
//                    RealParentDependency d = new RealParentDependency(functionNode, realParentNode);
//                    if (!functionNode.getDependencies().contains(d))
//                        functionNode.getDependencies().add(d);
//                    if (!realParentNode.getDependencies().contains(d))
//                        realParentNode.getDependencies().add(d);
//                }
//                else
                    reconstructWithAST(f);
            } else{
                RealParentDependency d = new RealParentDependency(functionNode, f.getParent());
                if (!functionNode.getDependencies().contains(d))
                    functionNode.getDependencies().add(d);
                if (!f.getParent().getDependencies().contains(d))
                    f.getParent().getDependencies().add(d);
            }
        }
    }

    private void reconstructWithAST(AbstractFunctionNode functionNode) {
        IASTName name = functionNode.getAST().getDeclarator().getName();

        if (name instanceof ICPPASTQualifiedName) {
            ICPPASTNameSpecifier[] chainNames = ((ICPPASTQualifiedName) name).getQualifier();
            String realParentName = chainNames[chainNames.length - 1].getRawSignature();

            List<Level> space = new VariableSearchingSpace(functionNode).generateExtendSpaces();

            boolean found = false;

            for (Level l : space) {
                for (INode n : l) {
                    List<INode> possibleNodes = Search.searchNodes(n, new ClassvsStructvsNamespaceCondition());
                    for (INode possibleNode : possibleNodes) {
                        if (possibleNode.getName().equals(realParentName)) {

                            RealParentDependency d = new RealParentDependency(functionNode, possibleNode);
                            if (!functionNode.getDependencies().contains(d))
                                functionNode.getDependencies().add(d);
                            if (!possibleNode.getDependencies().contains(d))
                                possibleNode.getDependencies().add(d);
                            found = true;
                            break;
                        }
                    }

                    if (found) break;
                }

                if (found) break;
            }
        }
    }
}
