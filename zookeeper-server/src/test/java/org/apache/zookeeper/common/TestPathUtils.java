package org.apache.zookeeper.common;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class TestPathUtils {
    private final String inputValidPath;
    private final String inputInvalidPath;

    public TestPathUtils(String inputValidPath, String inputInvalidPath){
        this.inputInvalidPath = inputInvalidPath;
        this.inputValidPath = inputValidPath;
    }

    @Parameterized.Parameters
    public static Collection<?> primeNumbers() {
        return Arrays.asList(new Object[][] {
                {"/znode1", null},
                {"/a/b", "a/b"},
                {"/a/b/c", "a/b/c"},
                {"/a", ""},
        });
    }

    @Test
    public void testInvalidPath(){

        IllegalArgumentException error = null;

        try{
            PathUtils.validatePath(this.inputInvalidPath);
        }catch (IllegalArgumentException e){
            error = e;
        }

        Assert.assertNotNull(error);
    }

    @Test
    public void testValidPath(){

        IllegalArgumentException error = null;

        try{
            PathUtils.validatePath(this.inputValidPath);
        }catch (IllegalArgumentException e){
            error = e;
        }

        Assert.assertNull(error);
    }
}