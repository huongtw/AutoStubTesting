package com.dse.compiler.message.error_tree.node;


import com.dse.parser.object.INode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public abstract class UndeclaredErrorNode<T extends INode> extends ErrorNode implements IUndeclaredErrorNode {
    protected int line;

    protected int offset;

    protected String name;

    protected IASTNode call;

    protected T location;

    public IASTNode getCall() {
        return call;
    }

    public void setCall(IASTNode call) {
        this.call = call;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getLocation() {
        return location;
    }

    public void setLocation(T location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("Missing %s at %d:%d", name, line, offset);
    }
}
