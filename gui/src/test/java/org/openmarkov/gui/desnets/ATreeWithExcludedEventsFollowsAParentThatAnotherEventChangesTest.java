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
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network of events, simulated for 10 years. «X» has a tree with excluded events on the event «E1» and the chance
 * node «Y»: it is «x0» if «Y» is «y0» and «x1» if it is «y1». The event «E2», at year 3, makes «Y» pass to «y1»;
 * «E1» comes at year 6. «Care» costs 100 a year if «X» is «x0» and 2000 if it is «x1». When «E2» changes «Y», «X»
 * is sampled again with no parent event in the configuration, and the simulation stopped there (error 105).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ATreeWithExcludedEventsFollowsAParentThatAnotherEventChangesTest {

    private static Variable event(String name) {
        Variable variable = new Variable(name, "no", "yes");
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    @Test void xChangesWithY() throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Criterion cost = new Criterion("Cost");
        probNet.setDecisionCriteria(List.of(cost, new Criterion("Effectiveness")));
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(10);

        Variable e1 = event("E1");
        Variable e2 = event("E2");
        Variable y = new Variable("Y", "y0", "y1");
        Variable x = new Variable("X", "x0", "x1");
        Variable care = new Variable("Care");
        care.setDecisionCriterion(cost);
        Node e1Node = probNet.addNode(e1, NodeType.EVENT);
        Node e2Node = probNet.addNode(e2, NodeType.EVENT);
        Node yNode = probNet.addNode(y, NodeType.CHANCE);
        Node xNode = probNet.addNode(x, NodeType.CHANCE);
        Node careNode = probNet.addNode(care, NodeType.UTILITY);
        probNet.addLink(e2Node, yNode, true);
        probNet.addLink(e1Node, xNode, true);
        probNet.addLink(yNode, xNode, true);
        probNet.addLink(xNode, careNode, true);
        e1Node.setPotential(new IndicatorPotential(List.of(e1), PotentialRole.CONDITIONAL_PROBABILITY, 6, 1));
        e2Node.setPotential(new IndicatorPotential(List.of(e2), PotentialRole.CONDITIONAL_PROBABILITY, 3, 1));
        TransitionTablePotential yTransition = new TransitionTablePotential(new ArrayList<>(List.of(y, e2)),
                                                                            PotentialRole.CONDITIONAL_PROBABILITY);
        yTransition.setValues(new double[]{0, 1});
        yNode.setPotential(yTransition);
        TreeWithExcludedEventsPotential xTree = new TreeWithExcludedEventsPotential(List.of(x, e1, y),
                                                                                    PotentialRole.CONDITIONAL_PROBABILITY);
        for (TreeADDBranch branch : xTree.getNoEventTree().getBranches()) {
            boolean y0 = branch.getBranchStates().getFirst().getName().equals("y0");
            ((TablePotential) branch.getPotential()).setValues(y0 ? new double[]{1, 0} : new double[]{0, 1});
        }
        xNode.setPotential(xTree);
        careNode.setPotential(new ExactDistrPotential(List.of(care, x), PotentialRole.UNSPECIFIED,
                                                      new double[]{100, 2000}));

        DESInference inference = new DESInference(probNet);
        CriteriaValues results = inference.simulateOneIndividual(inference.decisionVariable.getStates()[0]);

        assertEquals(3 * 100 + 7 * 2000, results.getValue(new EqualCriterion(cost)), 1e-9);
    }
}
