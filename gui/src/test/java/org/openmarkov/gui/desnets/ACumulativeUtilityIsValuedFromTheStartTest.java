/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.desnets;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.IndicatorPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network of events, simulated for 10 years. The event «Onset» would make «Ill» pass from «no» to «yes», but it
 * comes at year 20, so «Ill» keeps its value at the start, «no», its first state. The utility «QALY» is 1.0 if «Ill»
 * is «no» and 0.6 if it is «yes»; «Care» costs 100 a year if «no» and 2000 if «yes»; «Monitoring» costs 10 a year,
 * whatever happens. The three are cumulative.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACumulativeUtilityIsValuedFromTheStartTest {

    private static Variable utility(String name, Criterion criterion) {
        Variable variable = new Variable(name);
        variable.setDecisionCriterion(criterion);
        return variable;
    }

    /** Before, «QALY» and «Care» counted as 0 until an event reached them, which here is never. */
    @Test void aUtilityThatDependsOnANodeAnEventCanChangeIsValuedFromTheStart() throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Criterion cost = new Criterion("Cost");
        Criterion effectiveness = new Criterion("Effectiveness");
        probNet.setDecisionCriteria(List.of(cost, effectiveness));
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(10);

        Variable onset = new Variable("Onset", "no", "yes");
        onset.setVariableType(VariableType.EVENT);
        Variable ill = new Variable("Ill", "no", "yes");
        Variable qaly = utility("QALY", effectiveness);
        Variable care = utility("Care", cost);
        Variable monitoring = utility("Monitoring", cost);
        Node onsetNode = probNet.addNode(onset, NodeType.EVENT);
        Node illNode = probNet.addNode(ill, NodeType.CHANCE);
        Node qalyNode = probNet.addNode(qaly, NodeType.UTILITY);
        Node careNode = probNet.addNode(care, NodeType.UTILITY);
        Node monitoringNode = probNet.addNode(monitoring, NodeType.UTILITY);
        probNet.addLink(onsetNode, illNode, true);
        probNet.addLink(illNode, qalyNode, true);
        probNet.addLink(illNode, careNode, true);
        onsetNode.setPotential(new IndicatorPotential(List.of(onset), PotentialRole.CONDITIONAL_PROBABILITY, 20, 1));
        illNode.setPotential(new TablePotential(List.of(ill, onset), PotentialRole.CONDITIONAL_PROBABILITY));
        qalyNode.setPotential(new ExactDistrPotential(List.of(qaly, ill), PotentialRole.UNSPECIFIED, new double[] { 1.0, 0.6 }));
        careNode.setPotential(new ExactDistrPotential(List.of(care, ill), PotentialRole.UNSPECIFIED, new double[] { 100, 2000 }));
        monitoringNode.setPotential(new ExactDistrPotential(List.of(monitoring), PotentialRole.UNSPECIFIED, new double[] { 10 }));

        DESInference inference = new DESInference(probNet);
        CriteriaValues results = inference.simulateOneIndividual(inference.decisionVariable.getStates()[0]);

        assertEquals(10 * 1.0, results.getValue(new EqualCriterion(effectiveness)), 1e-9);
        assertEquals(10 * 100 + 10 * 10, results.getValue(new EqualCriterion(cost)), 1e-9);
    }
}
