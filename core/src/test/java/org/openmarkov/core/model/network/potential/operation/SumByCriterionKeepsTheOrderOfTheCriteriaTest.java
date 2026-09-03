package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The utilities are summed by criterion and come back one potential per criterion. They must
 * come back in the same order every run: that order is the one in which they are multiplied
 * later, and multiplying in floating point is not associative.
 *
 * <p>Every run builds new criteria, whose identity hash codes are different each time.
 *
 * @author Manuel Arias
 */
class SumByCriterionKeepsTheOrderOfTheCriteriaTest {

    private static final int RUNS = 50;

    @Tag(TestSpeed.FAST)
    @Test void theCriteriaComeBackInTheOrderTheyAppear() {
        for (int run = 0; run < RUNS; run++) {
            Variable variable = new Variable("X", "yes", "no");
            List<Criterion> criteria = List.of(
                    new Criterion("cost"), new Criterion("effectiveness"),
                    new Criterion("time"), new Criterion("risk"));

            List<TablePotential> utilities = new ArrayList<>();
            for (Criterion criterion : criteria) {
                TablePotential utility = new TablePotential(List.of(variable), PotentialRole.UNSPECIFIED);
                utility.setCriterion(criterion);
                utilities.add(utility);
            }

            List<TablePotential> summed = DiscretePotentialOperations.sumByCriterion(utilities);
            List<Criterion> answered = new ArrayList<>();
            for (TablePotential potential : summed) {
                answered.add(potential.getCriterion());
            }
            assertEquals(criteria, answered, "run " + run);
        }
    }
}
