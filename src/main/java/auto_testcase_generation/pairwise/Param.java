package auto_testcase_generation.pairwise;

import java.util.List;

public class Param implements Comparable<Param>{

    /*
    parameter B co bo gia tri la: b1 b2 b3 means:
        param: B  (name:B   List<Value> = b1 b2 b3
        Value: b1 (paramOwner: B    Val: b1)
        Value: b2 (paramOwner: B    Val: b2)
        Value: b3 (paramOwner: B    Val: b3)
    */

    private String name;
    private List<Value> values;

    public Param(String name, List<Value> values) {
        this.name = name;
        this.values = values;
        for (Value value : values) {
            value.setParamOwner(this);
        }
    }

    public List<Value> getValues() {
        return values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Param{" +
                "name='" + name + '\'' +
                "values='" + values + '\'' +
                '}';
    }

    public int compareTo(Param that){
        int this_size = this.getValues().size();
        int that_size = that.getValues().size();
        if (this_size < that_size) {
            return -1;
        } else if (this_size > that_size) {
            return 1;
        } else {
            return 0;
        }
    }
}
