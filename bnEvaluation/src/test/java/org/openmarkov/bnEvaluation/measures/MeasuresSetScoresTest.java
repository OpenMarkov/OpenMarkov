/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeasuresSetScoresTest {

    @Test
    void emptyMeasuresProduceEmptyRowList() {
        MeasuresSet set = new MeasuresSet("title");
        assertThat(set.buildScoresRows()).isEmpty();
    }

    @Test
    void scoreOnlySetStartsWithScoreMeasuresSection() {
        MeasuresSet set = new MeasuresSet("title");
        MeasureValue bayes = new MeasureValue(MeasureType.BAYES);
        bayes.setValue(-100.0, 1000);
        set.addMeasureValue(bayes);

        List<ScoresRow> rows = set.buildScoresRows();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).isInstanceOf(ScoresRow.Section.class);
        assertThat(((ScoresRow.Section) rows.get(0)).title()).isEqualTo("Score measures");
        assertThat(rows.get(1)).isInstanceOfSatisfying(ScoresRow.Data.class, data -> {
            assertThat(data.label()).isEqualTo("BAYES score");
            assertThat(data.value()).isEqualTo(-100.0);
        });
    }

    @Test
    void logLikelihoodIsFollowedByLossRow() {
        MeasuresSet set = new MeasuresSet("title");
        MeasureValue ll = new MeasureValue(MeasureType.LOGLIKELIHOOD);
        ll.setValue(-200.0, 100);
        set.addMeasureValue(ll);

        List<ScoresRow> rows = set.buildScoresRows();

        // Section + score + loss = 3 rows when LL is the only measure.
        assertThat(rows).hasSize(3);
        assertThat(((ScoresRow.Section) rows.get(0)).title())
                .isEqualTo("Goodness of fit Log-likelihood measures");
        assertThat(((ScoresRow.Data) rows.get(1)).label()).isEqualTo("LOGLIKELIHOOD score");
        assertThat(((ScoresRow.Data) rows.get(2)).label()).isEqualTo("LOGLIKELIHOOD Loss");
        assertThat(((ScoresRow.Data) rows.get(2)).value()).isEqualTo(2.0); // -(-200)/100
    }

    @Test
    void mixedSetProducesTwoSections() {
        MeasuresSet set = new MeasuresSet("title");
        MeasureValue ll = new MeasureValue(MeasureType.LOGLIKELIHOOD);
        ll.setValue(-200.0, 100);
        MeasureValue bayes = new MeasureValue(MeasureType.BAYES);
        bayes.setValue(-220.0, 100);
        set.addMeasureValue(ll);
        set.addMeasureValue(bayes);

        List<ScoresRow> rows = set.buildScoresRows();

        // LL section, LL score, LL loss, "Score measures" section, BAYES score = 5 rows.
        assertThat(rows).hasSize(5);
        assertThat(rows.get(0)).isInstanceOf(ScoresRow.Section.class);
        assertThat(rows.get(3)).isInstanceOfSatisfying(ScoresRow.Section.class,
                section -> assertThat(section.title()).isEqualTo("Score measures"));
        assertThat(((ScoresRow.Data) rows.get(4)).label()).isEqualTo("BAYES score");
    }

    /**
     * Cross-validation of 10 cases in 4 folds of 3, 3, 2 and 2 cases: the loss is per case,
     * so it is the sum of the four log-likelihoods divided by the 10 cases.
     */
    @Test
    void lossOfSeveralFoldsIsDividedByAllTheirCases() {
        MeasuresSet first = foldWithLogLikelihood(-6.0, 3);
        MeasuresSet mean = new MeasuresSet(first);
        mean.accumulateMeasureSet(first);
        mean.accumulateMeasureSet(foldWithLogLikelihood(-5.0, 3));
        mean.accumulateMeasureSet(foldWithLogLikelihood(-3.0, 2));
        mean.accumulateMeasureSet(foldWithLogLikelihood(-2.0, 2));
        mean.setAveraged();

        List<ScoresRow> rows = mean.buildScoresRows();

        assertThat(((ScoresRow.Data) rows.get(1)).value()).isEqualTo(-4.0); // mean of the four folds
        assertThat(((ScoresRow.Data) rows.get(2)).value()).isEqualTo(1.6);  // 16 / 10
        assertThat(mean.getMeasureInformation())
                .contains("Scores are averages of 4 iterations, with 10 cases in all.");
    }

    @Test
    void aSingleEvaluationTellsItsNumberOfCases() {
        assertThat(foldWithLogLikelihood(-6.0, 3).getMeasureInformation())
                .contains("Scores are calculated with 3 cases.");
    }

    private static MeasuresSet foldWithLogLikelihood(double logLikelihood, int numCases) {
        MeasuresSet set = new MeasuresSet("title");
        MeasureValue measure = new MeasureValue(MeasureType.LOGLIKELIHOOD);
        measure.setValue(logLikelihood, numCases);
        set.addMeasureValue(measure);
        return set;
    }
}
