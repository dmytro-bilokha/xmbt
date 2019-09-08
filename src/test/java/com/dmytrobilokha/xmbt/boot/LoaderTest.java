package com.dmytrobilokha.xmbt.boot;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class LoaderTest {

    public void returnsGivenName() {
        Assert.assertEquals(new Loader().getName(), "xmbt");
    }
}
