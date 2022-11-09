package com.dse.parser.object;

import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CppFileNode extends SourcecodeFileNode<CPPASTTranslationUnit> {

    public CppFileNode() {
        try {
            Icon ICON_CPP = new ImageIcon(Node.class.getResource("/image/node/Soucecode-Cpp.png"));
            setIcon(ICON_CPP);
        } catch (Exception e) {
        }
    }

    public List<Dependency> getIncludeHeaderNodes() {
        List<Dependency> includedDependencies = new ArrayList<>();
        for (Dependency dependency : getDependencies())
            if (dependency instanceof IncludeHeaderDependency)
                includedDependencies.add(dependency);
        return includedDependencies;
    }

}
