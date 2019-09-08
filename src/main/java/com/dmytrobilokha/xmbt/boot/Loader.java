package com.dmytrobilokha.xmbt.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loader {

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    public static void main(String[] cliArgs) {
        LOG.info("Hello, Java 11!");
    }

    public String getName() {
        return "xmbt";
    }

}
