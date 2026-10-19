/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.node.PotentialEditPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.RelationDialog;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel.WorkingMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An influence diagram with the chance node Disease and the decision Therapy, without an imposed policy. «Edit
 * relation» (Alt+click) opens what the menus offer: for a decision, the dialog of its policy in edition mode and none
 * in inference mode. It used to open the dialog of the relation, which failed on a decision without a policy and
 * left the network unable to undo what was done before (error 210).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class EditRelationOpensWhatTheMenusOfferTest {

    private ProbNet net;
    private Node disease;
    private Node therapy;

    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        disease = net.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
        therapy = net.addNode(new Variable("Therapy", "no", "yes"), NodeType.DECISION);
        disease.setPotential(new TablePotential(List.of(disease.getVariable()), PotentialRole.CONDITIONAL_PROBABILITY,
                                                new double[] { 0.9, 0.1 }));
    }

    @Test void aDecisionOpensItsPolicyInEditionModeAndNothingInInferenceMode() {
        assertEquals(RelationDialog.POLICY, RelationDialog.of(therapy, WorkingMode.EDITION));
        assertEquals(RelationDialog.NONE, RelationDialog.of(therapy, WorkingMode.INFERENCE));

        therapy.setPotential(new TablePotential(List.of(therapy.getVariable()), PotentialRole.POLICY,
                                                new double[] { 0.5, 0.5 }));

        assertEquals(RelationDialog.POLICY, RelationDialog.of(therapy, WorkingMode.EDITION));
        assertEquals(RelationDialog.NONE, RelationDialog.of(therapy, WorkingMode.INFERENCE));
    }

    @Test void aChanceNodeOpensItsRelation() {
        assertEquals(RelationDialog.RELATION, RelationDialog.of(disease, WorkingMode.EDITION));
        assertEquals(RelationDialog.RELATION, RelationDialog.of(disease, WorkingMode.INFERENCE));
    }

    /** Whether the dialog of the relation of a decision without a policy can be built or not, undo is not lost. */
    @Test void theDialogOfTheRelationOfADecisionDoesNotLoseUndo() throws Exception {
        net.getPNESupport().setWithUndo(true);
        new NodeStateEdit(disease, StateAction.ADD, 0, "unknown").executeEdit();
        assertEquals(3, disease.getVariable().getNumStates());

        try {
            // Built, it is closed as «Cancel» closes it
            new PotentialEditPanel(therapy, false, true).uncommitChanges();
        } catch (RuntimeException failed) {
            // What matters is what is left behind
        }

        assertTrue(net.getPNESupport().getCanUndo());
        net.getPNESupport().undo();
        assertEquals(2, disease.getVariable().getNumStates());
    }
}
