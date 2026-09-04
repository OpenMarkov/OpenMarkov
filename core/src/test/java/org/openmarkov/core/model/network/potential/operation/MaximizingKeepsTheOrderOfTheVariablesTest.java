package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The short forms of the two maximizations work out for themselves which variables to keep.
 * They must answer the same order every run: that order is the one of the variables of the
 * resulting table, and therefore the layout of its numbers.
 *
 * <p>Every run builds new variables, whose identity hash codes are different each time, so a
 * collection that follows those codes gives itself away within a few runs.
 *
 * @author Manuel Arias
 */
class MaximizingKeepsTheOrderOfTheVariablesTest {

    private static final int RUNS = 50;

    @Tag(TestSpeed.FAST)
    @Test void multiplyAndMaximizeAnswersTheOrderOfThePotentials() {
        for (int run = 0; run < RUNS; run++) {
            Variables variables = new Variables();
            Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                    variables.potentials(), variables.toMaximize);
            assertEquals(variables.expectedOrder(), ((TablePotential) result[0]).getVariables(),
                         "run " + run);
        }
    }

    @Tag(TestSpeed.FAST)
    @Test void multiplyAndMaximizeUniformlyAnswersTheOrderOfThePotentials() {
        for (int run = 0; run < RUNS; run++) {
            Variables variables = new Variables();
            List<TablePotential> tables = new ArrayList<>();
            for (Potential potential : variables.potentials()) {
                tables.add((TablePotential) potential);
            }
            TablePotential[] result = DiscretePotentialOperations.multiplyAndMaximizeUniformly(
                    tables, variables.toMaximize);
            assertEquals(variables.expectedOrder(), result[0].getVariables(), "run " + run);
        }
    }

    /** Five fresh variables and the two potentials that name them, in a fixed order. */
    private static final class Variables {
        private final Variable a = new Variable("A", "yes", "no");
        private final Variable b = new Variable("B", "yes", "no");
        private final Variable c = new Variable("C", "yes", "no");
        private final Variable d = new Variable("D", "yes", "no");
        private final Variable e = new Variable("E", "yes", "no");
        private final Variable toMaximize = a;

        private ArrayList<Potential> potentials() {
            ArrayList<Potential> potentials = new ArrayList<>();
            potentials.add(new TablePotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY));
            potentials.add(new TablePotential(List.of(d, b, e), PotentialRole.CONDITIONAL_PROBABILITY));
            return potentials;
        }

        private List<Variable> expectedOrder() {
            return List.of(c, d, b, e);
        }
    }
}
