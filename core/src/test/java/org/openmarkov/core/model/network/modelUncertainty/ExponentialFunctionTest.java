/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

/**
 * @author manolo
 */
public class ExponentialFunctionTest extends ProbDensFunctionTest {
    
    @Override public ProbDensFunction newProbDensFunctionInstance() {
        return new ExponentialFunction();
    }
    
    @Override public double[] initializeParams() {
        return new double[]{1.3};
    }
    
    @Disabled("This tests takes too long to complete, and seems it was stressing the machine that executes the test rather than verifying functionality" +
            "Trying to reduce the num of samples of org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#testMeanAndVariance from 10000000 to a lower number, such as 10000 would highly reduce its time complexity" +
            "Once that is done, this method can be erased, as it only calls super. The only reason it exist is because JUnit's @Disabled tag is not @Inherit as other tags are")
    @Tag(TestSpeed.SLOW)
    @Test
    public void testMeanAndVariance() {
        super.testMeanAndVariance();
    }
    
    @Disabled("This tests takes too long to complete, and seems it was stressing the machine that executes the test rather than verifying functionality" +
            "Trying to reduce the num of samples of org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#testMeanAndVariance from 10000000 to a lower number, such as 10000 would highly reduce its time complexity" +
            "Once that is done, this method can be erased, as it only calls super. The only reason it exist is because JUnit's @Disabled tag is not @Inherit as other tags are")
    @Tag(TestSpeed.SLOW)
    @Test
    public void repeatTestMeanAndVariance() {
        super.repeatTestMeanAndVariance();
    }

}
