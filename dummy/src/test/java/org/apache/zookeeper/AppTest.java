package org.apache.zookeeper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class AppTest {
    /**
     * a simple test
     */
    @Test
    public void sum()
    {
        App app = new App();
        int c = app.sum(12, 4);
        assertEquals(16, c);

    }
    @Test
    public void sub(){
        App app = new App();
        int c = app.sub(12,4);
        assertEquals(c, 8);
    }
}

