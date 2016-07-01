package com.mr1holmes.planup;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by mr1holmes on 30/6/16.
 */

public class FullTestSuit extends TestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuit.class)
                .includeAllPackagesUnderHere().build();
    }

    public FullTestSuit() {
        super();
    }
}
