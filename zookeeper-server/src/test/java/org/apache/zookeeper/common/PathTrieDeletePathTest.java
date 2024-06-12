package org.apache.zookeeper.common;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathTrieDeletePathTest {
    private PathTrie pathTrie;

    @Before
    public void setUp() {
        pathTrie = new PathTrie();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyString() {
        pathTrie.deletePath("");
    }
    @Test
    public void test_invalidPath_multipleNodes() {
        String path = "node1/node2";
        pathTrie.deletePath(path);
        assertFalse(pathTrie.existsNode(path));
    }
    @Test(expected = NullPointerException.class)
    public void testNullString() {
        pathTrie.addPath(null);
    }

    @Test
    public void test_validPath_singleNode() {
        String path = "node1";
        pathTrie.addPath(path);
        pathTrie.deletePath(path);
        assertFalse(pathTrie.existsNode(path));
    }

    @Test
    public void test_validPath_multipleNodes() {
        String path = "node1/node2/node3";
        pathTrie.addPath(path);
        pathTrie.deletePath("node1/node2/node3");
        assertFalse(pathTrie.existsNode(path));
        assertTrue(pathTrie.existsNode("node1/node2"));
    }

    @After
    public void clean(){
        pathTrie.clear();
    }
}


