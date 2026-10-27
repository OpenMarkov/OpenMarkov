/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * When several states of a variable lead to the same plan, the branch that joins them lists them in the order of the
 * variable, and writes them apart. They came in the order of a HashSet, and the plan of DAN-reactor said «Result of
 * test = excellentgood» (error 360 of the pending list).
 *
 * @author Manuel Arias
 */
class ABranchOfAPlanKeepsTheOrderOfItsStatesTest {

    @Test void theStatesOfABranchKeepTheOrderOfTheVariable() {
        Variable result = new Variable("Result of test", "bad", "good", "excellent");
        Variable build = new Variable("Build decision", "none", "build");
        State[] results = result.getStates();
        State[] builds = build.getStates();

        StrategyTree plan = new StrategyTree(result, Arrays.asList(results),
                                             List.of(new StrategyTree(build, builds[0]),
                                                     new StrategyTree(build, builds[1]),
                                                     new StrategyTree(build, builds[1])));

        List<TreeADDBranch> branches = plan.getBranches();
        assertEquals(2, branches.size());
        assertEquals(List.of(results[1], results[2]), branches.get(1).getStates());
        assertTrue(branches.get(1).toString().startsWith("Result of test = good, excellent -> "),
                   branches.get(1).toString());
    }
}
