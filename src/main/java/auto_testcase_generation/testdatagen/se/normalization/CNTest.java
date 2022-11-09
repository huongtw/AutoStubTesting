package auto_testcase_generation.testdatagen.se.normalization;
import org.junit.Assert;
import org.junit.Test;



public class CNTest {
    @Test
    public void test1(){
        String input = "!(cond)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="!cond";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);

    }
    @Test
    public void test2(){
        String input = "!((cond))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(!cond)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test3(){
        String input = "!(!(cond))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(cond)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test4(){
        String input = "!(!((cond)))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(cond)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test5(){
        String input = "!(!cond)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(cond)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test6(){
        String input = "!(!((!cond)))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(((!cond)))";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test7(){
        String input = "!(((!cond)))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(((cond)))";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test8(){
        String input = "!((!(!cond)))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(((!cond)))";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void test9(){
        String input = "((!(!cond)))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(cond)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }

    @Test
    public void testBasicPositive (){
        String input = "a>0";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(a > 0)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testBasicNegative (){
        String input = "!(a>0)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(a <= 0)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testDoubleNegative (){
        String input = "!(!(a%2==0))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(a % 2 == 0)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testDoubleBracket (){
        String input = "((a%2==0))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(a % 2 == 0)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testMultiConstraints (){
        String input = "( !(a%2==0) || a<c || a==5)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="((a % 2 != 0) || a < c || a == 5)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testMultiConstraintsNeg (){
        String input = "!( !(a%2==0) || (a<c) || (a==5))";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="((a % 2 == 0) && (a >= c) && (a != 5))";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testMultiConstraintsNegative (){
        String input = "!(cond && (cond2 || (x+1 == 0)) )";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(!cond || (!cond2 && (x + 1 != 0)))";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testNestedConstraint (){
        String input = "( !(cond1 || (a+2>b)) && cond2 )";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="((!cond1 && (a + 2 <= b)) && cond2)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testNestedConstraintNeg (){
        String input = "!( !(cond1 || (a>b)) && cond2 )";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="((cond1 || (a > b)) || !cond2)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testNestedConstraintsNeg (){
        String input = "!( " +
                "(" +
                "!(" +
                "cond1 || (a>b)" +
                ") " +
                "&& cond2 " +
                ") " +
                "&& c!= 5 " +
                ")";

        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(((cond1 || (a > b)) || !cond2) || c == 5)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testAssignOp (){
        String input = "(a = 5)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(a = 5)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testAssignOpNeg (){
        String input = "!((a=5) || a<10)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="(!(a = 5) && a >= 10)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }
    @Test
    public void testAssignOpNegBasic (){
        String input = "!(a=5)";
        String actualOutput="";
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(input);
        norm.normalize();
        String expectedOutput="!(a = 5)";
        actualOutput = norm.getNormalizedSourcecode();
        Assert.assertEquals(expectedOutput,actualOutput);
    }

}
