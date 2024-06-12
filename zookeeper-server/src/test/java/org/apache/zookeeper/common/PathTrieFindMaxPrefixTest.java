package org.apache.zookeeper.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PathTrieFindMaxPrefixTest {
    private PathTrie pathTrie;

    @Before
    public void setUp() {
        pathTrie = new PathTrie();
    }

    @Test
    public void test_emptyString() {
        Assert.assertEquals("/", pathTrie.findMaxPrefix(""));
    }

    @Test(expected = NullPointerException.class)
    public void test_nullString() {
        pathTrie.findMaxPrefix(null);
    }

    @Test
    public void testAddPath_validPath_singleNode() {
        String path = "node1";
        pathTrie.addPath(path);
        assertEquals("/node1", pathTrie.findMaxPrefix("node1"));
    }
    @Test
    public void testAddPath_validPath_multipleNode() {
        pathTrie.addPath("node1");
        pathTrie.addPath("node1/node2/");
        pathTrie.addPath("node1/node2/node3");
        assertEquals("/node1/node2", pathTrie.findMaxPrefix("node1/node2"));
    }
    @Test
    public void test_invalidPath_multipleNodes() {
        String path = "node1/node2";
        assertEquals("/", pathTrie.findMaxPrefix(path));
    }

}
