package org.apache.zookeeper.common;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


public class PathTrieAddPathTest {
    private PathTrie pathTrie;

    @Before
    public void setUp() {
        pathTrie = new PathTrie();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPath_emptyString() {
        pathTrie.addPath("");
    }

    @Test(expected = NullPointerException.class)
    public void testAddPath_nullString() {
        pathTrie.addPath(null);
    }

    @Test
    public void testAddPath_validPath_singleNode() {
        String path = "node1";
        pathTrie.addPath(path);
        assertTrue(pathTrie.existsNode(path));
    }

    @Test
    public void testAddPath_validPath_multipleNodes() {
        String path = "node1/node2";
        pathTrie.addPath(path);
        assertTrue(pathTrie.existsNode(path));
    }


    @Test
    public void testSpecialMultipleNode(){
        pathTrie.addPath("node1");
        pathTrie.addPath("node1/node2/node3");
        pathTrie.addPath("node1/node3/node2");
        assertTrue(pathTrie.existsNode("node1//node2/node3"));
        assertTrue(pathTrie.existsNode("node1/node3/node2"));
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testAddPath_invalidPath_illegalCharacters() {
//        String path = "node1**node2–node3#";
//        pathTrie.addPath(path);
//    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testAddPath_invalidPath_questionMarks() {
//        String path = "\\\\\\";
//        pathTrie.addPath(path);
//    }
    @After
    public void clean(){
        pathTrie.clear();
    }
}
