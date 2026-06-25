/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

import org.junit.jupiter.api.Test;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.aic.AICMetric;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;
import org.openmarkov.learning.metric.bde.BDeMetric;
import org.openmarkov.learning.metric.entropy.EntropyMetric;
import org.openmarkov.learning.metric.k2.K2Metric;
import org.openmarkov.learning.metric.mdlm.MDLMetric;

import static org.assertj.core.api.Assertions.assertThat;

class MeasureTypeTest {

    @Test
    void scoringTypesProvideMatchingMetric() {
        assertThat(MeasureType.BAYES.newMetric()).isInstanceOf(BayesianMetric.class);
        assertThat(MeasureType.AIC.newMetric()).isInstanceOf(AICMetric.class);
        assertThat(MeasureType.ENTROPY.newMetric()).isInstanceOf(EntropyMetric.class);
        assertThat(MeasureType.BDE.newMetric()).isInstanceOf(BDeMetric.class);
        assertThat(MeasureType.K2.newMetric()).isInstanceOf(K2Metric.class);
        assertThat(MeasureType.MDL.newMetric()).isInstanceOf(MDLMetric.class);
    }

    @Test
    void scoringTypesReturnFreshInstancesEachCall() {
        Metric first = MeasureType.AIC.newMetric();
        Metric second = MeasureType.AIC.newMetric();
        assertThat(first).isNotSameAs(second);
    }

    @Test
    void nonScoringTypesReturnNull() {
        assertThat(MeasureType.CONFUSIONMATRIX.newMetric()).isNull();
        assertThat(MeasureType.LOGLIKELIHOOD.newMetric()).isNull();
    }
}
