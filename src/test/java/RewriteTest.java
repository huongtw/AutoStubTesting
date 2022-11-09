import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import org.junit.Assert;
import org.junit.Test;

public class RewriteTest {
    @Test
    public void test1(){
        String input = "*p";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test2(){
        String input = "*(p+x)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[x]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test3(){
        String input = "*(p+3)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[3]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test4(){
        String input = "*(*p+3)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[0][3]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test5(){
        String input = "**(p+1)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[1][0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test6(){
        String input = "**p";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[0][0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test7(){
        String input = "*(*p)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[0][0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test8(){
        String input = "**(p)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[0][0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test9(){
        String input = "**((p))";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[0][0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test10(){
        String input = "*(*(p+1)+2)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[1][2]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test11(){
        String input = "*(*(p+1)+2)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[1][2]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test12(){
        String input = "*p.a";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p.a[0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test13(){
        String input = "*p[2]";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p[2][0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test14(){
        String input = "*p->a";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "p->a[0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test15() throws Exception {
        String input = "*(((p)))";
        String actual = ExpressionRewriterUtils.rewrite(new VariableNodeTable(),input);
        String expected = "p[0]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test16(){
        String input = "*(*a+*b)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "a[0][b[0]]";
        Assert.assertEquals("", expected, actual);
    }

    @Test
    public void test17(){
        String input = "!(*(*(p+1)+2) != 2)";
        String actual = ExpressionRewriterUtils.rewritePointer(input);
        String expected = "!(p[1][2] != 2)";
        Assert.assertEquals("", expected, actual);
    }
}
