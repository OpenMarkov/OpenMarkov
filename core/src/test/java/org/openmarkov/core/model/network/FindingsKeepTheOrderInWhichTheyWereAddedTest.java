/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An evidence case gives its findings in the order they were added. It used to give them in an order that depended
 * on where each variable was in memory, so the same findings came out in a different order in each run, for example in
 * the sheet exported by the stochastic propagation (error 107). Each test builds new variables many times, because a
 * single run could come out in the right order by chance.
 *
 * @author Manuel Arias
 */
class FindingsKeepTheOrderInWhichTheyWereAddedTest {

    private static final int RUNS = 50;
    private static final int VARIABLES = 20;

    private final Random random = new Random(107);

    /** New variables, in an order that changes from run to run. */
    private List<Variable> shuffledVariables() {
        List<Variable> variables = new ArrayList<>();
        for (int index = 0; index < VARIABLES; index++) {
            variables.add(new Variable("V" + index, "a", "b"));
        }
        Collections.shuffle(variables, random);
        return variables;
    }

    private static EvidenceCase evidence(List<Variable> variables, int state) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        for (Variable variable : variables) {
            evidence.addFinding(new Finding(variable, state));
        }
        return evidence;
    }

    private static List<Variable> variablesOfTheFindings(EvidenceCase evidence) {
        return evidence.getFindings().stream().map(Finding::getVariable).toList();
    }

    @Test void findingsAddedOneByOneComeOutInThatOrder() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            List<Variable> variables = shuffledVariables();
            EvidenceCase evidence = evidence(variables, 0);

            assertEquals(variables, evidence.getVariables());
            assertEquals(variables, variablesOfTheFindings(evidence));
        }
    }

    @Test void findingsGivenAsAListComeOutInThatOrder() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            List<Variable> variables = shuffledVariables();
            List<Finding> findings = variables.stream().map(variable -> new Finding(variable, 1)).toList();

            assertEquals(variables, new EvidenceCase(findings).getVariables());
        }
    }

    @Test void aCopyKeepsTheOrder() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            List<Variable> variables = shuffledVariables();

            assertEquals(variables, new EvidenceCase(evidence(variables, 0)).getVariables());
        }
    }

    /** As the stochastic propagation joins the evidence given before and after the decisions. */
    @Test void joiningTwoCasesKeepsTheFirstOneAndThenTheNewFindingsInOrder() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            List<Variable> variables = shuffledVariables();
            List<Variable> first = variables.subList(0, 12);
            List<Variable> second = variables.subList(8, VARIABLES);
            EvidenceCase joined = new EvidenceCase(evidence(first, 0));

            joined.fuse(evidence(second, 1), true);

            assertEquals(variables, joined.getVariables());
            for (Variable variable : second) {
                assertEquals(1, joined.getState(variable), "the second case overwrites the findings in common");
            }
        }
    }

    @Test void changingAFindingLeavesItInItsPlace() throws Exception {
        for (int run = 0; run < RUNS; run++) {
            List<Variable> variables = shuffledVariables();
            EvidenceCase evidence = evidence(variables, 0);

            evidence.changeFinding(new Finding(variables.get(3), 1));

            assertEquals(variables, evidence.getVariables());
            assertEquals(1, evidence.getState(variables.get(3)));
        }
    }
}
