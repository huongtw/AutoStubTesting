package auto_testcase_generation.pairwise;

import java.util.ArrayList;
import java.util.List;

public class Testcase {
    //List parameters of this test case
     private List<Param> listParam;
    //List data test tuong ung
    private List<Value> listTestData;

    public Testcase() {
        listParam =  new ArrayList<>();
        listTestData =  new ArrayList<>();
    }
    
    public Testcase(Testcase testcase) {
//        this.listParam = new ArrayList<>();
        List<Param> list = testcase.getListParam();
        this.listParam = new ArrayList<>(list);
//        listParam.addAll(testcase.getListParam());
        this.listTestData = new ArrayList<>();
        listTestData.addAll(testcase.getListTestData());
    }

    public Testcase(List<Param> params, List<Value> values) {
        listParam = params;
        listTestData = values;
    }

    public List<Param> getListParam() {
        return listParam;
    }

    public List<Value> getListTestData() {
        return listTestData;
    }

    /**
     * Add the Value into the position of Param in the ListData
     * @param param
     * @param value
     */
    public void addValue(Param param, Value value) {
        if (!listParam.contains(param)) {
            try {
                listParam.add(param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (listTestData.size() < listParam.size()) {
            listTestData.add(value);
        }
    }

    /**
     * Check if this testcase cover the Pair input or not.
     * @param pair Pair input
     * @return
     */
    public boolean isContain(Pair<Value, Value> pair) {
        if (listTestData.contains(pair.getFirst()) && listTestData.contains(pair.getSecond())) {
            return true;
        }
        return false;
    }

    /**
     * fill the Value into this testcase's ListData.
     * @param value input Value
     */
    public void fillValue(Value value) {
        Param p = value.getParamOwner();
        if (listParam == null) {
            System.out.println("List param in testcase is null");
            return;
        }
        if (p == null) {
            System.out.println("Value's paramOwner is null");
            return;
        }
        if (!listParam.contains(p)) {
            return;
        }
        int i = listParam.indexOf(p);
        listTestData.set(i, value);
    }

    @Override
    public String toString() {
        return "Testcase{" + listTestData.toString() + " }";
    }
}
