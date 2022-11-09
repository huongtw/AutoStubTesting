package auto_testcase_generation.pairwise;

import java.util.Objects;

public class Value {
    private Param paramOwner = null;
    private Object val = null;

    public Value(){}

    public Value(Object o) {
        val = o;
    }

    public Value(Param param, Object o) {
        paramOwner = param;
        val = o;
    }

    public Param getParamOwner() {
        return paramOwner;
    }

    public void setParamOwner(Param paramOwner) {
        this.paramOwner = paramOwner;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Value)) {
            return false;
        }
        if (((Value)o).getParamOwner() != paramOwner) {
            return false;
        }
        return (((Value)o).getVal() == this.val);
    }

    @Override
    public String toString() {
        return (paramOwner.getName() + val.toString());
    }
}
