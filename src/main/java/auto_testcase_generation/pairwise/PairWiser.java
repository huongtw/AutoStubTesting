package auto_testcase_generation.pairwise;

import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.SourcecodeFileParser;
import org.reactfx.value.Val;

import java.io.File;
import java.util.*;

public class PairWiser {
    //set of pair Pi in order to generate horizontal
    private Set<Pair> piSet;
    //set of test case
    private List<Testcase> testSetT;
    //list of parameter input
    private List<Param> params;

    public PairWiser() {
        piSet = new LinkedHashSet<>();
    }

    public PairWiser(List<Param> params) {
        this.params = params;
        piSet = new LinkedHashSet<>();
    }

    public void setPiSet(Set<Pair> piSet) {
        this.piSet = piSet;
    }

    public List<Testcase> getTestSetT() {
        return testSetT;
    }

    public void setTestSetT(List<Testcase> testSetT) {
        this.testSetT = testSetT;
    }

    public Set<Pair> getPiSet() {
        return piSet;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public List<Param> getParams() {
        return params;
    }

    public boolean checkParams() {
        for (int i = 0; i < params.size(); i++) {
            if(params.get(i).getValues().size() == 0) {
                return false;
            }
        }
        return true;
    }

    public void fillNullInTestSet() {
        for(Testcase test: testSetT) {
            List<Value> valueList = test.getListTestData();
            for (int i = 0; i < valueList.size(); i++) {
                Value value = valueList.get(i);
                if (value == null) {
                    valueList.set(i, params.get(i).getValues().get(0));
                }
            }
        }
    }

    /**
     * Start pairwise
     */
    public void inOrderParameter() {
        if(!checkParams()) {
            testSetT = new ArrayList<>();
            return;
        }
        Collections.sort(params);
        initTestSet();
        for (int i = 2; i < params.size(); i++) {
            Param param = params.get(i);
            IPO_H(param);
            // System.out.println("IPO_H: ");
            // System.out.println("TESTSET: ");
            // System.out.println(testSetT);
            // System.out.println("PISET: ");
            // System.out.println(piSet);
            // System.out.println("End IPO_H!");
            IPO_V(param);
            // System.out.println("IPO_V: ");
            // System.out.println("TESTSET: ");
            // System.out.println(testSetT);
            // System.out.println("PISET: ");
            // System.out.println(piSet);
            // System.out.println("End IPO_V");
        }
        fillNullInTestSet();
    }

    /**
     * Init the first 2-parameter
     */
    private void initTestSet() {
        testSetT = new ArrayList<>();
        if(params.size() < 1) {
            return;
        }
        Param param1 = params.get(0);
        if(params.size() == 1) {
            for (Value value1 : param1.getValues()) {
                List list = Arrays.asList(param1);
                List<Param> paramList = new ArrayList<>(list);
                list = Arrays.asList(value1);
                List<Value> valueList = new ArrayList<>(list);
                Testcase testcase = new Testcase(paramList, valueList);
                testSetT.add(testcase);
            }
            return;
        }
        Param param2 = params.get(1);
        for (Value value2 : param2.getValues()) {
            for (Value value1 : param1.getValues()) {
                List list = Arrays.asList(param1, param2);
                List<Param> paramList = new ArrayList<>(list);
                list = Arrays.asList(value1, value2);
                List<Value> valueList = new ArrayList<>(list);
                Testcase testcase = new Testcase(paramList, valueList);
                testSetT.add(testcase);
            }
        }
    }

    /**
     * Generate the horizontal of the strategy
     */
    private void IPO_H(Param p) {
        //create Pi set of pairs
        for (Value valInTest : getTestSetValues()) {
            for (Value valP : p.getValues()) {
                Pair pair = new Pair(valInTest, valP);
                piSet.add(pair);
            }
        }


        for (Testcase testcase : testSetT) {
            Value value = getHighestCoveredValue(testcase, p); // chon ra value dang con nhieu nhat trong list pair
            testcase.addValue(p, value);
            deletePairOfPiSet(testcase, piSet);
        }
    }

    /**
     * delete the pair in PiSet which existed in testcase
     * @param testcase testcase to check
     * @param piSet PiSet is a set of pairs
     */
    private void deletePairOfPiSet(Testcase testcase, Set<Pair> piSet) {
        while (piSet.size() > 0) {
            boolean c = false;
            for (Pair pair : piSet) {
                if (testcase.isContain(pair)) {
                    piSet.remove(pair);
                    c = true;
                    break;
                }
            }
            if (c == false) {
                break;
            }
        }
    }

    /**
     * get a Value of a Parameter which is inserted into a Testcase and would would cover the most pairs in the PiSet.
     * @param testcase test case
     * @param param parameter
     * @return the value
     */
    private Value getHighestCoveredValue(Testcase testcase, Param param) {
        if (testcase.getListParam().contains(param)) {
            return null;
        }
        int max = 0;
        int indexMax = 0;
        for (Value value : param.getValues()) {
            int count = 0;
            Testcase test = new Testcase(testcase);
            test.addValue(param, value);
            for (Pair pair : piSet) {
                if (test.isContain(pair)) {
                    count++;
                }
            }
            if (count > max) {
                max = count;
                indexMax = param.getValues().indexOf(value);
            }
        }
        return param.getValues().get(indexMax);
    }

    /**
     * Generate the vertical of the strategy.
     */
    private void IPO_V(Param p) {
        List<Testcase> testSetT_ = new ArrayList<>();

        for(Pair pair: piSet) {
            Value v1 = (Value)pair.getFirst();
            Value v2 = (Value)pair.getSecond();
            Param p1 = v1.getParamOwner();
            Param p2 = v2.getParamOwner();
            int id1 = params.indexOf(p1);
            int id2 = params.indexOf(p2);

            boolean check = false;

            for(Testcase test: testSetT_) {
                List<Value> valueList = test.getListTestData();
                if(v1.equals(valueList.get(id1)) && valueList.get(id2) == null) {
                    valueList.set(id2, v2);
                    check = true;
                } else if(v2.equals(valueList.get(id2)) && valueList.get(id1) == null) {
                    valueList.set(id1, v1);
                    check = true;
                } else if(valueList.get(id2) == null && valueList.get(id1) == null) {
                    valueList.set(id1, v1);
                    valueList.set(id2, v2);
                    check = true;
                }
            }

            if(!check) {
                Testcase tempTestcase = new Testcase();
                for(int i=0; i<=params.indexOf(p); i++) {
                    tempTestcase.addValue(params.get(i), null);
                }
                tempTestcase.fillValue(v1);
                tempTestcase.fillValue(v2);
                testSetT_.add(tempTestcase);
            }
        }

        piSet.clear();

        testSetT.addAll(testSetT_);
    }

    public boolean verifyTestSet() {
        int n = params.size();
        for(int i=0; i<n; i++) {
            Param pi = params.get(i);
            List<Value> vi = pi.getValues();
            for(int j=i+1; j<n; j++) {
                Param pj = params.get(j);
                List<Value> vj = pj.getValues();
                for(Value vali: vi) {
                    for(Value valj: vj) {
                        int cnt = 0;
                        for(Testcase testcase : testSetT) {
                            for(Value value : testcase.getListTestData()) {
                                if(value.equals(vali) || value.equals(valj)) {
                                    cnt += 1;
                                }
                            }
                        }
                        if(cnt < 2) {
                            return false;
                        }
                    }
                }
            }
        }
        // 6 nested loops lmao
        return true;
    }

    /**
     * get the Set of unique Value in TestSet.
     * @return
     */
    public Set<Value> getTestSetValues() {
        Set<Value> set = new LinkedHashSet<>();
        for (Testcase testcase : testSetT) {
            for (Value value : testcase.getListTestData()) {
                set.add(value);
            }
        }
        set.remove(null);
        return set;
    }

//     public static void main(String[] args) throws Exception {
//         List<Value> va = new ArrayList<Value>();
//         List<Value> vb = new ArrayList<Value>();
//         List<Value> vc = new ArrayList<Value>();
//         List<Value> vd = new ArrayList<Value>();
//         List<Value> ve = new ArrayList<Value>();
////         List<Value> vf = new ArrayList<Value>();
////         List<Value> vg = new ArrayList<Value>();
////         List<Value> vh = new ArrayList<Value>();
//
//         Param a = new Param("A", va);
//         Param b = new Param("B", vb);
//         Param c = new Param("C", vc);
//         Param d = new Param("D", vd);
//         Param e = new Param("E", ve);
////         Param f = new Param("F", vf);
////         Param g = new Param("G", vg);
////         Param h = new Param("H", vh);
//
//         va.add(new Value(a, new Integer(1)));
//         va.add(new Value(a, new Integer(2)));
////         va.add(new Value(a, new Integer(3)));
////         va.add(new Value(a, new Integer(4)));
////         va.add(new Value(a, new Integer(5)));
//
//         vb.add(new Value(b, new Integer(1)));
//         vb.add(new Value(b, new Integer(2)));
////         vb.add(new Value(b, new Integer(3)));
////         vb.add(new Value(b, new Integer(4)));
////         vb.add(new Value(b, new Integer(5)));
//
//         vc.add(new Value(c, new Integer(1)));
//         vc.add(new Value(c, new Integer(2)));
////         vc.add(new Value(c, new Integer(3)));
////         vc.add(new Value(c, new Integer(4)));
////         vc.add(new Value(c, new Integer(5)));
//
//         vd.add(new Value(d, new Integer(1)));
//         vd.add(new Value(d, new Integer(2)));
//         vd.add(new Value(d, new Integer(3)));
//         vd.add(new Value(d, new Integer(4)));
//         vd.add(new Value(d, new Integer(5)));
//
//         ve.add(new Value(e, new Integer(1)));
//         ve.add(new Value(e, new Integer(2)));
////         ve.add(new Value(e, new Integer(3)));
////         ve.add(new Value(e, new Integer(4)));
////         ve.add(new Value(e, new Integer(5)));
//
////         vf.add(new Value(f, new Integer(1)));
////         vf.add(new Value(f, new Integer(2)));
////         vf.add(new Value(f, new Integer(3)));
////         vf.add(new Value(f, new Integer(4)));
////         vf.add(new Value(f, new Integer(5)));
////
////         vg.add(new Value(g, new Integer(1)));
////         vg.add(new Value(g, new Integer(2)));
////         vg.add(new Value(g, new Integer(3)));
////         vg.add(new Value(g, new Integer(4)));
////         vg.add(new Value(g, new Integer(5)));
////
////         vh.add(new Value(h, new Integer(1)));
////         vh.add(new Value(h, new Integer(2)));
////         vh.add(new Value(h, new Integer(3)));
////         vh.add(new Value(h, new Integer(4)));
////         vh.add(new Value(h, new Integer(5)));
//
//         List<Param> paramList = new ArrayList<>();
//         paramList.add(a);
//         paramList.add(b);
//         paramList.add(c);
//         paramList.add(d);
//         paramList.add(e);
////         paramList.add(f);
////         paramList.add(g);
////         paramList.add(h);
//
//         PairWiser p = new PairWiser(paramList);
//
//         p.inOrderParameter();
//
//         System.out.println(p.verifyTestSet());
//
//         System.out.println(p.getTestSetT().size());
//         System.out.println(p.getTestSetT());
//     }
}
