package com.dse.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void getIndexOfArray() {
        {
            List<String> indexes = Utils.getIndexOfArray("int[]");
            Assert.assertEquals(indexes.size(), 1);
            Assert.assertEquals(indexes.get(0), "");
        }
        {
            List<String> indexes = Utils.getIndexOfArray("int[2]");
            Assert.assertEquals(indexes.size(), 1);
            Assert.assertEquals(indexes.get(0), "2");
        }
        {
            List<String> indexes = Utils.getIndexOfArray("a[]");
            Assert.assertEquals(indexes.size(), 1);
            Assert.assertEquals(indexes.get(0), "");
        }
        {
            List<String> indexes = Utils.getIndexOfArray("a[1]");
            Assert.assertEquals(indexes.size(), 1);
            Assert.assertEquals(indexes.get(0), "1");
        }

        {
            List<String> indexes = Utils.getIndexOfArray("a[1][2]");
            Assert.assertEquals(indexes.size(), 2);
            Assert.assertEquals(indexes.get(0), "1");
            Assert.assertEquals(indexes.get(1), "2");
        }

        {
            List<String> indexes = Utils.getIndexOfArray("a[0].trie[1][2][3]");
            Assert.assertEquals(indexes.size(), 3);
            Assert.assertEquals(indexes.get(0), "1");
            Assert.assertEquals(indexes.get(1), "2");
            Assert.assertEquals(indexes.get(2), "3");
        }

        {
            List<String> indexes = Utils.getIndexOfArray("a");
            Assert.assertEquals(indexes.size(), 0);
        }
    }

    @Test
    public void getNameVariable() {
        Assert.assertEquals(Utils.getNameVariable("int[1]"), "int");
        Assert.assertEquals(Utils.getNameVariable("int[]"), "int");
        Assert.assertEquals(Utils.getNameVariable("a[1]"), "a");
        Assert.assertEquals(Utils.getNameVariable("a[1][2][3]"), "a");
        Assert.assertEquals(Utils.getNameVariable("a[0].trie[1][2][3]"), "a[0].trie");
        Assert.assertEquals(Utils.getNameVariable("a"), "a");
    }
}