/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

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
    
}
