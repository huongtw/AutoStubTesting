package auto_testcase_generation.cfg.object;

/**
 * Represent a branch in CFG
 *
 * @author ducanhnguyen
 */
public class BranchInCFG {
    private ICfgNode startCfgNode;

    private ICfgNode endCfgNode;

    public BranchInCFG(ICfgNode startCfgNode, ICfgNode endCfgNode) {
        this.startCfgNode = startCfgNode;
        this.endCfgNode = endCfgNode;
    }

    public ICfgNode getStart() {
        return startCfgNode;
    }

    public void setStart(ICfgNode startCfgNode) {
        this.startCfgNode = startCfgNode;
    }

    public ICfgNode getEnd() {
        return endCfgNode;
    }

    public void setEnd(ICfgNode endCfgNode) {
        this.endCfgNode = endCfgNode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BranchInCFG) {
            BranchInCFG b = (BranchInCFG) obj;
            return b.getStart().equals(getStart()) && b.getEnd().equals(getEnd());
        } else
            return false;
    }

    @Override
    public String toString() {
        if (endCfgNode != null)
            return "(" + startCfgNode.getContent() + ", " + endCfgNode.getContent() + ")";
        else
            return "(" + startCfgNode.getContent() + ", [end CFG])";
    }
}
