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

import java.util.ArrayList;
import java.util.List;

/**
 * @author manolo
 */
public class DirichletFamilyTest extends FamilyDistributionTest {
    
    @Override public List<UncertainValue> initializeListUncertainValues() {
        
        double[] alpha = {1.0, 2.0, 3.0, 4.0};
        List<UncertainValue> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            list.add(new UncertainValue(new DirichletFunction(alpha[i])));
        }
        return list;
    }
    
    @Override public FamilyDistribution newFamilyDistribution(List<UncertainValue> list) {
        return new DirichletFamily(list);
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
    @Test public void repeatTestMeanAndVariance() {
        super.repeatTestMeanAndVariance();
    }
    
}
