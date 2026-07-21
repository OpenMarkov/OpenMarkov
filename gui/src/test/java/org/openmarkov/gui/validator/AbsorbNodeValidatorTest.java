package org.openmarkov.gui.validator;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AbsorbNodeValidator}, including the informational-predecessor
 * check for decision nodes (issue #506).
 */
class AbsorbNodeValidatorTest {

    /**
     * Absorbing a decision into a utility child is valid when the utility node's
     * other parents are informational predecessors of the decision.
     */
    @Test
    void absorbDecision_validWhenAllParentsAreInformationalPredecessors() {
        // D1 → D2 → U, D1 → U
        // D1 is an informational predecessor of D2 (direct path)
        // U has parents D2 and D1. D1 is info predecessor of D2 → valid
        ProbNet net = buildID();
        Variable d1Var = addDecision(net, "D1");
        Variable d2Var = addDecision(net, "D2");
        Variable uVar = addUtility(net, "U");
        net.addLink(d1Var, d2Var, true);
        net.addLink(d2Var, uVar, true);
        net.addLink(d1Var, uVar, true);

        assertTrue(AbsorbNodeValidator.validate(net.getNode(d2Var)));
    }

    /**
     * Absorbing a decision into a utility child is INVALID when a parent of the
     * utility node is NOT an informational predecessor of the decision (issue #506).
     */
    @Test
    void absorbDecision_invalidWhenChanceParentIsNotInformationalPredecessor() {
        // Disease → Health (utility), Disease → Result, Result → Therapy → Health
        // Therapy's info predecessors do NOT include Disease
        ProbNet net = buildID();
        Variable disease = addChance(net, "Disease");
        Variable result = addChance(net, "Result");
        Variable therapy = addDecision(net, "Therapy");
        Variable health = addUtility(net, "Health");

        net.addLink(disease, result, true);
        net.addLink(disease, health, true);
        net.addLink(result, therapy, true);
        net.addLink(therapy, health, true);

        assertFalse(AbsorbNodeValidator.validate(net.getNode(therapy)));
    }

    /**
     * Absorbing a decision that is the sole parent of a utility node is always valid.
     */
    @Test
    void absorbDecision_validWhenSoleParent() {
        ProbNet net = buildID();
        Variable d = addDecision(net, "D");
        Variable u = addUtility(net, "U");
        net.addLink(d, u, true);

        assertTrue(AbsorbNodeValidator.validate(net.getNode(d)));
    }

    /**
     * Absorbing a chance node does not require the informational-predecessor check.
     */
    @Test
    void absorbChance_noInfoPredecessorCheckNeeded() {
        ProbNet net = buildID();
        Variable c = addChance(net, "C");
        Variable u = addUtility(net, "U");
        net.addLink(c, u, true);

        assertTrue(AbsorbNodeValidator.validate(net.getNode(c)));
    }

    /**
     * Reproduces the exact ID-decide-test scenario from issue #506:
     * Disease → Health state, Disease → Result, Result → Therapy → Health state,
     * Therapy → Cost of therapy, Do test? → Cost of test, Do test? → Therapy
     */
    @Test
    void absorbDecision_idDecideTestScenario() {
        ProbNet net = buildID();
        Variable disease = addChance(net, "Disease");
        Variable result = addChance(net, "Result of test");
        Variable therapy = addDecision(net, "Therapy");
        Variable doTest = addDecision(net, "Do test?");
        Variable healthState = addUtility(net, "Health state");
        Variable costOfTherapy = addUtility(net, "Cost of therapy");
        Variable costOfTest = addUtility(net, "Cost of test");

        net.addLink(disease, healthState, true);
        net.addLink(disease, result, true);
        net.addLink(result, therapy, true);
        net.addLink(therapy, healthState, true);
        net.addLink(therapy, costOfTherapy, true);
        net.addLink(doTest, costOfTest, true);
        net.addLink(doTest, therapy, true);

        // Therapy should NOT be absorbable: Disease is parent of Health state
        // but not an informational predecessor of Therapy
        assertFalse(AbsorbNodeValidator.validate(net.getNode(therapy)),
                "Therapy should not be absorbable because Disease is not an informational predecessor");

        // Do test? should NOT be absorbable either: it has non-utility child (Therapy)
        assertFalse(AbsorbNodeValidator.validate(net.getNode(doTest)),
                "Do test? should not be absorbable because it has non-utility children");
    }

    // --- helpers ---

    private static ProbNet buildID() {
        return new ProbNet(InfluenceDiagramType.getUniqueInstance());
    }

    private static Variable addDecision(ProbNet net, String name) {
        Variable v = new Variable(name, new State[]{new State("a"), new State("b")});
        net.addNode(new Node(net, v, NodeType.DECISION));
        return v;
    }

    private static Variable addChance(ProbNet net, String name) {
        Variable v = new Variable(name, new State[]{new State("a"), new State("b")});
        net.addNode(new Node(net, v, NodeType.CHANCE));
        return v;
    }

    private static Variable addUtility(ProbNet net, String name) {
        Variable v = new Variable(name);
        net.addNode(new Node(net, v, NodeType.UTILITY));
        return v;
    }
}
