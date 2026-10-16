/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Adding, removing or moving a decision criterion, undoing it, redoing it and undoing it again leaves the
 * criteria as they were at the beginning.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class UndoingACriteriaEditAfterRedoingItUndoesItTest {

    private ProbNet net;
    private Criterion cost;

    /** An influence diagram with the criteria Cost and Effectiveness. */
    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        cost = new Criterion("Cost");
        net.setDecisionCriteria(new ArrayList<>(List.of(cost, new Criterion("Effectiveness"))));
        net.getPNESupport().setWithUndo(true);
    }

    private List<String> names() {
        return net.getDecisionCriteria().stream().map(Criterion::getCriterionName).toList();
    }

    @ParameterizedTest
    @EnumSource(value = StateAction.class, names = { "ADD", "REMOVE", "DOWN" })
    void theCriteriaAreTheOnesAtTheBeginning(StateAction action) throws Exception {
        Criterion modified = action == StateAction.ADD ? new Criterion("New") : cost;
        new DecisionCriteriaEdit(net, action, modified, null).executeEdit();
        net.getPNESupport().undo();
        net.getPNESupport().redo();
        net.getPNESupport().undo();

        assertEquals(List.of("Cost", "Effectiveness"), names());
    }
}
