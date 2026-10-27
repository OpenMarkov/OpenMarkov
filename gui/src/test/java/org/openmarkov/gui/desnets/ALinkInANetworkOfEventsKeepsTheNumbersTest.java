/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.desnets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
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
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * In a network of events, adding or removing a link left the relation of the child uniform, without its numbers, and
 * the simulation could not be run until it was written again (error 99 of the pending list). Now the relation keeps
 * them, and a parent that changes nothing changes no result. The network: «Onset» happens at year 3 and makes «Ill»
 * pass to «yes»; «QALY» is 1.0 if «Ill» is «no» and 0.6 if it is «yes»; «Monitoring» costs 10 a year; «Sex» is a
 * chance node without children.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ALinkInANetworkOfEventsKeepsTheNumbersTest {

    private ProbNet probNet;
    private Variable onset;
    private Variable ill;
    private Variable sex;
    private Variable qaly;
    private Variable monitoring;
    private Criterion cost;
    private Criterion effectiveness;

    @BeforeEach void aNetworkThatIsSimulated() {
        probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        cost = new Criterion("Cost");
        effectiveness = new Criterion("Effectiveness");
        probNet.setDecisionCriteria(List.of(cost, effectiveness));
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(10);

        onset = new Variable("Onset", "no", "yes");
        onset.setVariableType(VariableType.EVENT);
        ill = new Variable("Ill", "no", "yes");
        sex = new Variable("Sex", "male", "female");
        qaly = new Variable("QALY");
        qaly.setDecisionCriterion(effectiveness);
        monitoring = new Variable("Monitoring");
        monitoring.setDecisionCriterion(cost);
        Node onsetNode = probNet.addNode(onset, NodeType.EVENT);
        Node illNode = probNet.addNode(ill, NodeType.CHANCE);
        Node sexNode = probNet.addNode(sex, NodeType.CHANCE);
        Node qalyNode = probNet.addNode(qaly, NodeType.UTILITY);
        Node monitoringNode = probNet.addNode(monitoring, NodeType.UTILITY);
        probNet.addLink(onsetNode, illNode, true);
        probNet.addLink(illNode, qalyNode, true);
        onsetNode.setPotential(new IndicatorPotential(List.of(onset), PotentialRole.CONDITIONAL_PROBABILITY, 3, 1));
        TransitionTablePotential illTransition = new TransitionTablePotential(new ArrayList<>(List.of(ill, onset)),
                                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        illTransition.setValues(new double[]{ 0, 1 });
        illNode.setPotential(illTransition);
        sexNode.setPotential(new TablePotential(List.of(sex), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{ 0.5, 0.5 }));
        qalyNode.setPotential(new ExactDistrPotential(List.of(qaly, ill), PotentialRole.UNSPECIFIED, new double[]{ 1.0, 0.6 }));
        monitoringNode.setPotential(new ExactDistrPotential(List.of(monitoring), PotentialRole.UNSPECIFIED, new double[]{ 10 }));
    }

    private void assertTheResultsDoNotChange() throws Exception {
        DESInference inference = new DESInference(probNet);
        CriteriaValues results = inference.simulateOneIndividual(inference.decisionVariable.getStates()[0]);
        assertEquals(3 * 1.0 + 7 * 0.6, results.getValue(new EqualCriterion(effectiveness)), 1e-9);
        assertEquals(10 * 10, results.getValue(new EqualCriterion(cost)), 1e-9);
    }

    @Test void aNewParentOfAUtilityKeepsItsValues() throws Exception {
        new AddLinkEdit(probNet, sex, qaly, true).executeEdit();
        new AddLinkEdit(probNet, sex, monitoring, true).executeEdit();

        assertInstanceOf(ExactDistrPotential.class, probNet.getNode(qaly).getPotential());
        assertTheResultsDoNotChange();
    }

    @Test void aNewParentOfATransitionTableKeepsItsValues() throws Exception {
        new AddLinkEdit(probNet, sex, ill, true).executeEdit();

        assertInstanceOf(TransitionTablePotential.class, probNet.getNode(ill).getPotential());
        assertTheResultsDoNotChange();
    }

    @Test void removingAParentKeepsTheValuesOfItsFirstState() throws Exception {
        new AddLinkEdit(probNet, sex, qaly, true).executeEdit();
        new RemoveLinkEdit(probNet, sex, qaly, true).executeEdit();

        assertTheResultsDoNotChange();
    }

    /** A link from «Onset» to itself, which the networks of events allow, used to leave it a uniform relation. */
    @Test void anEventLinkedToItselfKeepsItsTime() throws Exception {
        new AddLinkEdit(probNet, onset, onset, true).executeEdit();

        IndicatorPotential atYear3 = assertInstanceOf(IndicatorPotential.class, probNet.getNode(onset).getPotential());
        assertEquals(3, atYear3.getTte());
        assertEquals(List.of(onset, onset), atYear3.getVariables());
        DESInference.checkThatEveryNodeCanBeSimulated(probNet);

        new RemoveLinkEdit(probNet, onset, onset, true).executeEdit();
        assertEquals(List.of(onset), probNet.getNode(onset).getPotential().getVariables());
    }
}
