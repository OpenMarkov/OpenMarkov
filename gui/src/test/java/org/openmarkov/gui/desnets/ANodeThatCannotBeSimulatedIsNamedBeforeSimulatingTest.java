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
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.IndicatorPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.DES.exception.ChanceNodeCannotBeDiscretized;
import org.openmarkov.inference.DES.exception.RelationCannotBeSimulated;
import org.openmarkov.inference.DES.exception.RelationDoesNotTakeEventsIntoAccount;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Before simulating, every chance node, event and utility of the network is checked, and a node that the simulation
 * cannot use stops it with a message that names the node. The simulation used to start and end in the window of
 * unexpected errors, which named no node: with the uniform relation that adding a link leaves (error 99), with a table
 * that has an event among its parents, when that event happened (error 98), and with a chance node divided into
 * intervals (error 106). The network: «Onset» happens at year 3 and makes «Ill» pass to «yes»; the utility «QALY» is
 * 1.0 if «Ill» is «no» and 0.6 if it is «yes».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANodeThatCannotBeSimulatedIsNamedBeforeSimulatingTest {

    private ProbNet probNet;
    private Variable onset;
    private Variable qaly;
    private Criterion effectiveness;

    @BeforeEach void aNetworkThatIsSimulated() {
        probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Criterion cost = new Criterion("Cost");
        effectiveness = new Criterion("Effectiveness");
        probNet.setDecisionCriteria(List.of(cost, effectiveness));
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(10);

        onset = new Variable("Onset", "no", "yes");
        onset.setVariableType(VariableType.EVENT);
        Variable ill = new Variable("Ill", "no", "yes");
        qaly = new Variable("QALY");
        qaly.setDecisionCriterion(effectiveness);
        Variable monitoring = new Variable("Monitoring");
        monitoring.setDecisionCriterion(cost);
        Node onsetNode = probNet.addNode(onset, NodeType.EVENT);
        Node illNode = probNet.addNode(ill, NodeType.CHANCE);
        Node qalyNode = probNet.addNode(qaly, NodeType.UTILITY);
        Node monitoringNode = probNet.addNode(monitoring, NodeType.UTILITY);
        probNet.addLink(onsetNode, illNode, true);
        probNet.addLink(illNode, qalyNode, true);
        onsetNode.setPotential(new IndicatorPotential(List.of(onset), PotentialRole.CONDITIONAL_PROBABILITY, 3, 1));
        TransitionTablePotential illTransition = new TransitionTablePotential(new ArrayList<>(List.of(ill, onset)),
                                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        illTransition.setValues(new double[]{ 0, 1 });
        illNode.setPotential(illTransition);
        qalyNode.setPotential(new ExactDistrPotential(List.of(qaly, ill), PotentialRole.UNSPECIFIED, new double[]{ 1.0, 0.6 }));
        monitoringNode.setPotential(new ExactDistrPotential(List.of(monitoring), PotentialRole.UNSPECIFIED, new double[]{ 10 }));
    }

    /** The message of {@code thrown}, as the window shows it. */
    private static String message(OpenMarkovException thrown) {
        return thrown.toString();
    }

    @Test void theNetworkAsItIsBuiltIsSimulated() throws Exception {
        DESInference inference = new DESInference(probNet);

        CriteriaValues results = inference.simulateOneIndividual(inference.decisionVariable.getStates()[0]);
        assertEquals(3 * 1.0 + 7 * 0.6, results.getValue(new EqualCriterion(effectiveness)), 1e-9);
    }

    /** Linking «Onset» to «QALY» leaves it the uniform relation, without its values (error 99). */
    @Test void aRelationThatCannotBeSimulatedIsNamed() throws Exception {
        new AddLinkEdit(probNet, onset, qaly, true).executeEdit();

        RelationCannotBeSimulated thrown = assertThrows(RelationCannotBeSimulated.class, () -> new DESInference(probNet));
        assertTrue(message(thrown).startsWith("The relation of QALY is «Uniform»"), message(thrown));
    }

    /**
     * A new chance node gets a table, which linking «Onset» to it keeps: the simulation would read out of the table
     * when «Onset» happens (error 98).
     */
    @Test void aTableWithAnEventAmongItsParentsIsNamed() throws Exception {
        Variable pain = new Variable("Pain", "no", "yes");
        probNet.addNode(pain, NodeType.CHANCE)
               .setPotential(new TablePotential(List.of(pain), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{ 0.8, 0.2 }));
        new AddLinkEdit(probNet, onset, pain, true).executeEdit();

        RelationDoesNotTakeEventsIntoAccount thrown = assertThrows(RelationDoesNotTakeEventsIntoAccount.class,
                                                                   () -> new DESInference(probNet));
        assertTrue(message(thrown).startsWith("The relation of Pain, «ProbTable», does not take events into account, "
                                              + "and Onset is one of its parents"), message(thrown));
    }

    /** A chance node divided into intervals (error 106). */
    @Test void aChanceNodeDividedIntoIntervalsIsNamed() {
        Variable age = new Variable("Age", new State[]{ new State("young"), new State("old") },
                                    new PartitionedInterval(new double[]{ 0, 50, 120 }, new boolean[]{ false, false, false }),
                                    1);
        probNet.addNode(age, NodeType.CHANCE)
               .setPotential(new TablePotential(List.of(age), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{ 0.5, 0.5 }));

        ChanceNodeCannotBeDiscretized thrown = assertThrows(ChanceNodeCannotBeDiscretized.class,
                                                            () -> new DESInference(probNet));
        assertTrue(message(thrown).startsWith("Age is a numeric variable divided into intervals"), message(thrown));
    }
}
