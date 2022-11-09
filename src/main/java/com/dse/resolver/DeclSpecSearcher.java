package com.dse.resolver;

import com.dse.parser.dependency.finder.Level;
import com.dse.parser.object.CustomASTNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.util.ArrayList;
import java.util.List;

public class DeclSpecSearcher extends ASTVisitor {
    private List<IASTDeclSpecifier> declSpecs = new ArrayList<>();
    private String target;
    private List<Level> space;
    private boolean exact = true;

    public DeclSpecSearcher(String target, List<Level> space) {
        shouldVisitDeclSpecifiers = true;
        this.target = target;
        this.space = space;
        exec();
    }

    public DeclSpecSearcher(String regex, List<Level> space, boolean exact) {
        shouldVisitDeclSpecifiers = true;
        this.target = regex;
        this.space = space;
        this.exact = exact;
        exec();
    }

    public List<IASTDeclSpecifier> getDeclSpecs() {
        return declSpecs;
    }

    public IASTDeclSpecifier getFirstDeclSpec() {
        return declSpecs.isEmpty() ? null : declSpecs.get(0);
    }

    private void exec() {
        for (Level level : space) {
            for (INode node : level) {
                if (node instanceof CustomASTNode) {
                    IASTNode aSTNode = ((CustomASTNode) node).getAST();
                    aSTNode.accept(this);
                } else if (node instanceof ISourcecodeFileNode) {
                    IASTNode aSTNode = ((ISourcecodeFileNode) node).getAST();
                    aSTNode.accept(this);
                }
            }
        }
    }

    @Override
    public int visit(IASTDeclSpecifier declSpec) {
        if (exact) {
            if (target.equals(declSpec.toString()))
                if (!isDeclSpecInList(declSpec))
                    declSpecs.add(declSpec);
        } else {
            if (declSpec.toString().matches(target))
                if (!isDeclSpecInList(declSpec))
                    declSpecs.add(declSpec);
        }
        return PROCESS_CONTINUE;
    }

    private boolean isDeclSpecInList(IASTDeclSpecifier target) {
        for (IASTDeclSpecifier declSpec : declSpecs) {
            if (target.getRawSignature().equals(declSpec.getRawSignature()))
                return true;
        }

        return false;
    }
}
