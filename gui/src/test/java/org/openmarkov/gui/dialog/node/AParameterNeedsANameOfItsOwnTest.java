/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The editor of the uncertainty of a column refuses a name that another parameter of the network already has: the
 * analyses tell parameters apart by their names, and two with the same one used to show as one (error 308 of the
 * pending list). The network has «p» in the column of A being edited and «q» in B.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AParameterNeedsANameOfItsOwnTest {

    private Variable a;
    private Map<String, String> others;

    private static void withUncertainty(ProbNet probNet, Variable variable, String name) {
        TablePotential table = new UncertainTablePotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY,
                                                           new double[]{ 0.3, 0.7 });
        table.setUncertainValues(new UncertainValue[]{ new UncertainValue(new DirichletFunction(3), name),
                                                       new UncertainValue(new DirichletFunction(7), "") });
        probNet.addNode(variable, NodeType.CHANCE).setPotential(table);
    }

    private static UncertainValue named(String name) {
        return new UncertainValue(new DirichletFunction(1), name);
    }

    @BeforeEach void twoNodesWithANamedParameterEach() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        a = new Variable("A", "yes", "no");
        withUncertainty(probNet, a, "p");
        withUncertainty(probNet, new Variable("B", "yes", "no"), "q");
        others = UncertainValuesDialog.namesOfTheOtherParameters(SystematicSampling.getUncertainParameters(probNet), a,
                                                                 List.of("p", ""));
    }

    @Test void theNamesOfTheColumnBeingEditedAreNotOthers() {
        assertEquals(Map.of("q", "B"), others);
        assertNull(UncertainValuesDialog.repeatedName(List.of(named("p"), named("")), others));
        assertNull(UncertainValuesDialog.repeatedName(List.of(named("r"), named("")), others));
    }

    @Test void theNameOfAnotherParameterIsRefused() {
        String refused = UncertainValuesDialog.repeatedName(List.of(named("q"), named("")), others);
        assertTrue(refused.startsWith("«q» is already the name of a parameter of B."), refused);
    }

    @Test void aNameTwiceInTheColumnIsRefused() {
        String refused = UncertainValuesDialog.repeatedName(List.of(named("r"), named("r")), others);
        assertTrue(refused.startsWith("«r» is the name of two parameters of this column."), refused);
    }
}
