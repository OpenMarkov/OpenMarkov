/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.DistributionTablePotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The selector of the type of relation shows readable names instead of the names written in the files, such as
 * «ProbTable» or «UnivariateDistr» (error 179 of the pending list).
 *
 * @author Manuel Arias
 */
class TheRelationTypesHaveReadableNamesTest {

    private final ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
    private final Node chance = this.probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
    private final Node utility = this.probNet.addNode(new Variable("Cost"), NodeType.UTILITY);

    @Test void theTypesWithAnIdentifierShowAReadableName() {
        assertEquals("Table of probabilities", PotentialEditPanel.relationTypeName(TablePotential.class, this.chance));
        assertEquals("Table with formulas", PotentialEditPanel.relationTypeName(AugmentedProbTablePotential.class, this.chance));
        assertEquals("Probability distribution", PotentialEditPanel.relationTypeName(UnivariateDistrPotential.class, this.chance));
        assertEquals("Table of distributions", PotentialEditPanel.relationTypeName(DistributionTablePotential.class, this.chance));
        assertEquals("Transition table", PotentialEditPanel.relationTypeName(TransitionTablePotential.class, this.chance));
        assertEquals("Cycle length shift", PotentialEditPanel.relationTypeName(CycleLengthShift.class, this.chance));
    }

    @Test void anExactTableHoldsUtilitiesOnlyInAUtilityNode() {
        assertEquals("Table of utilities", PotentialEditPanel.relationTypeName(ExactDistrPotential.class, this.utility));
        assertEquals("Table of values", PotentialEditPanel.relationTypeName(ExactDistrPotential.class, this.chance));
    }

    @Test void aTypeWithoutItsOwnNameTakesTheOneOfTheTypeItExtends() {
        assertEquals("Table of probabilities", PotentialEditPanel.relationTypeName(UncertainTablePotential.class, this.chance));
    }

    @Test void aTypeThatAlreadyHadAReadableNameKeepsIt() {
        assertEquals("Linear combination", PotentialEditPanel.relationTypeName(LinearCombinationPotential.class, this.utility));
    }
}
