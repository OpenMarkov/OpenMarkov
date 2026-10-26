/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.ValidCriterionName;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Two criteria cannot have the same name, whatever their capitals and surrounding spaces, but a criterion can be
 * renamed to its own name or to it with other capitals (error 58).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACriterionNameIsUniqueWhateverItsCapitalsTest {

    private ProbNet net;
    private Criterion cost;

    /** An influence diagram with the criteria cost and effectiveness. */
    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        cost = new Criterion("cost");
        net.setDecisionCriteria(new ArrayList<>(List.of(cost, new Criterion("effectiveness"))));
    }

    private void rename(String newName) throws Exception {
        new DecisionCriteriaEdit(net, StateAction.RENAME, cost, newName).executeEdit();
    }

    @Test void aCriterionCanBeRenamedToItsOwnName() throws Exception {
        rename("cost");

        assertEquals("cost", cost.getCriterionName());
    }

    @Test void aCriterionCanBeRenamedToItsNameWithOtherCapitals() throws Exception {
        rename("Cost");

        assertEquals("Cost", cost.getCriterionName());
    }

    @Test void aCriterionCannotBeRenamedToTheNameOfAnotherWithOtherCapitals() {
        assertThrows(DoEditException.CannotDoEditException.class, () -> rename(" Effectiveness "));
        assertEquals("cost", cost.getCriterionName());
    }

    @Test void aNetworkWithTwoNamesThatDifferOnlyInCapitalsAndSpacesIsRefused() {
        net.setDecisionCriteria(List.of(new Criterion("cost"), new Criterion(" Cost")));

        assertFalse(new ValidCriterionName().isMetBy(net));
    }
}
