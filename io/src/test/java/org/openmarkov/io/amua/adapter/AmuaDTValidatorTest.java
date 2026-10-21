package org.openmarkov.io.amua.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.CENodeEvaluation;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.UnicritNodeEvaluation;
import org.openmarkov.core.model.network.*;
import org.openmarkov.io.amua.adatper.AmuaDTValidator;
import org.openmarkov.io.amua.model.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AmuaDTValidatorTest {

    private ProbNet simpleNet;
    private DecisionTreeNode ceaRoot;
    private DecisionTreeNode unicriteriaRoot;

    @BeforeEach
    void setUp() {
        simpleNet = createSimpleNet();
        ceaRoot = createCEATree();
        unicriteriaRoot = createUnicriteriaTree();
    }

    @Test
    void exceptionWhenCriteriaIsNull() {
        AmuaDTValidator validator = new AmuaDTValidator(null);
        assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(null));
    }

    @Test
    void exceptionWhenCriteriaIsEmpty() {
        AmuaDTValidator validator = new AmuaDTValidator(Collections.emptyList());
        assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(null));
    }

    @Test
    void exceptionForDuplicatedCriterionTest() {
        AmuaDTValidator validator = new AmuaDTValidator(getWrongCriteria());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(ceaRoot));
        assertEquals("Tree type not supported by Amua.", exception.getMessage());
    }

    @Test
    void exceptionForOverloadedCriterion() {
        AmuaDTValidator validator = new AmuaDTValidator(getOverloadedCriteria());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(ceaRoot));
        assertEquals("Tree type not supported by Amua.", exception.getMessage());
    }

    @Test
    void exceptionWhenTreeTypeNotSupported() {
        Criterion c1 = new Criterion("c1");
        Criterion c2 = new Criterion("c2");
        AmuaDTValidator validator = new AmuaDTValidator(List.of(c1, c2));
        assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(null));
    }

    @Test
    void exceptionWhenTwoCriteriaButNotCE() {
        Criterion c1 = new Criterion("c1");
        Criterion c2 = new Criterion("c2");

        AmuaDTValidator validator = new AmuaDTValidator(List.of(c1, c2));
        assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(ceaRoot));
    }

    @Test
    void validatorShouldBeReusable() {
        AmuaDTValidator validator = new AmuaDTValidator(getCorrectCriteria());
        assertDoesNotThrow(() -> validator.determineAmuaDTType(ceaRoot));
        assertDoesNotThrow(() -> validator.determineAmuaDTType(ceaRoot));
    }

    @Test
    void determineCEValidTest() {
        AmuaDTValidator validator = new AmuaDTValidator(getCorrectCriteria());
        assertEquals(AmuaModel.COST_EFFECTIVENESS_DT, validator.determineAmuaDTType(ceaRoot));
    }

    @Test
    void determineCEInvalidDecisionNodesTest() {
        AmuaDTValidator validator = new AmuaDTValidator(getCorrectCriteria());

        Variable D2 = new Variable("SecondDecision", 2);
        simpleNet.addNode(D2, NodeType.DECISION);

        DecisionTreeNode secondDecisionNode = new DecisionTreeNode(D2, simpleNet);
        secondDecisionNode.setEvaluation(new CENodeEvaluation(1.0, null));
        DecisionTreeBranch firstBranch = (DecisionTreeBranch) ceaRoot.getChildren().get(0);
        DecisionTreeNode firstChanceNode = firstBranch.getChild();
        DecisionTreeBranch newBranch = new DecisionTreeBranch(D2.getStates()[0]);

        newBranch.setChild(secondDecisionNode);
        firstChanceNode.addChild(newBranch);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(ceaRoot));

        assertEquals("Amua supports only one decision node.", exception.getMessage());
    }

    @Test
    void determineCEInvalidWrongStructureTest() {
        AmuaDTValidator validator = new AmuaDTValidator(getCorrectCriteria());
        DecisionTreeBranch firstBranch = (DecisionTreeBranch) ceaRoot.getChildren().get(0);

        Variable wrongVar = new Variable("WrongNode");
        simpleNet.addNode(wrongVar, NodeType.CHANCE);

        DecisionTreeNode wrongChild = new DecisionTreeNode(wrongVar, simpleNet);
        wrongChild.setEvaluation(new UnicritNodeEvaluation(1.0, 0.0));
        firstBranch.setChild(wrongChild);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(ceaRoot));
        assertEquals("Invalid node type for Cost-Effectiveness tree.", exception.getMessage());
    }

    @Test
    void determineUnicriteriaTwoCriterionValidTest() {
        AmuaDTValidator validator = new AmuaDTValidator(getCorrectCriteria());
        assertEquals(AmuaModel.UNICRITERIA_DT, validator.determineAmuaDTType(unicriteriaRoot));
    }

    @Test
    void determineUnicriteriaOneCriterionValidTest() {
        Criterion utility = new Criterion("utility", "units");
        utility.setCECriterion(Criterion.CECriterion.Cost);

        AmuaDTValidator validator = new AmuaDTValidator(List.of(utility));
        assertEquals(AmuaModel.UNICRITERIA_DT, validator.determineAmuaDTType(unicriteriaRoot));
    }

    @Test
    void determineUnicriterionInvalidWrongStructureTest() {
        AmuaDTValidator validator = new AmuaDTValidator(getCorrectCriteria());
        DecisionTreeBranch firstBranch = (DecisionTreeBranch) unicriteriaRoot.getChildren().get(0);

        Variable wrongVar = new Variable("WrongNode");
        simpleNet.addNode(wrongVar, NodeType.CHANCE);

        DecisionTreeNode wrongChild = new DecisionTreeNode(wrongVar, simpleNet);
        wrongChild.setEvaluation(new CENodeEvaluation(1.0, null));
        firstBranch.setChild(wrongChild);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.determineAmuaDTType(unicriteriaRoot));
        assertEquals("Invalid node type for Unicriteria tree.", exception.getMessage());
    }
    
    
    private ProbNet createSimpleNet() {
        ProbNet net = new ProbNet();

        Variable D = new Variable("Decision", 3);
        Variable C_no = new Variable("C_no", 2);
        Variable C_t1 = new Variable("C_t1", 2);
        Variable C_t2 = new Variable("C_t2", 2);
        Variable U1 = new Variable("U1");
        Variable U2 = new Variable("U2");
        Variable U3 = new Variable("U3");
        Variable U4 = new Variable("U4");
        Variable U5 = new Variable("U5");
        Variable U6 = new Variable("U6");

        net.addNode(D, NodeType.DECISION);
        net.addNode(C_no, NodeType.CHANCE);
        net.addNode(C_t1, NodeType.CHANCE);
        net.addNode(C_t2, NodeType.CHANCE);
        net.addNode(U1, NodeType.UTILITY);
        net.addNode(U2, NodeType.UTILITY);
        net.addNode(U3, NodeType.UTILITY);
        net.addNode(U4, NodeType.UTILITY);
        net.addNode(U5, NodeType.UTILITY);
        net.addNode(U6, NodeType.UTILITY);

        return net;
    }

    private DecisionTreeNode createCEATree() {

        Variable D = simpleNet.getVariable("Decision");
        Variable C_no = simpleNet.getVariable("C_no");
        Variable C_t1 = simpleNet.getVariable("C_t1");
        Variable C_t2 = simpleNet.getVariable("C_t2");

        DecisionTreeNode root = new DecisionTreeNode(D, simpleNet);
        root.setEvaluation(new CENodeEvaluation(1.0, null));

        DecisionTreeNode chanceNo = new DecisionTreeNode(C_no, simpleNet);
        chanceNo.setEvaluation(new CENodeEvaluation(1.0, null));
        DecisionTreeNode chanceT1 = new DecisionTreeNode(C_t1, simpleNet);
        chanceT1.setEvaluation(new CENodeEvaluation(1.0, null));
        DecisionTreeNode chanceT2 = new DecisionTreeNode(C_t2, simpleNet);
        chanceT2.setEvaluation(new CENodeEvaluation(1.0, null));

        root.addChild(branch(D.getStates()[0], chanceNo));
        root.addChild(branch(D.getStates()[1], chanceT1));
        root.addChild(branch(D.getStates()[2], chanceT2));

        chanceNo.addChild(branch(C_no.getStates()[0], utilityNode("U1", 0, 10)));
        chanceNo.addChild(branch(C_no.getStates()[1], utilityNode("U2", 0, 1.2)));
        chanceT1.addChild(branch(C_t1.getStates()[0], utilityNode("U3", 20000, 9.9)));
        chanceT1.addChild(branch(C_t1.getStates()[1], utilityNode("U4", 20000, 4)));
        chanceT2.addChild(branch(C_t2.getStates()[0], utilityNode("U5", 70000, 9.3)));
        chanceT2.addChild(branch(C_t2.getStates()[1], utilityNode("U6", 70000, 6.5)));

        return root;
    }

    private DecisionTreeNode utilityNode(String name, double cost, double eff) {
        Variable v = simpleNet.getVariable(name);
        DecisionTreeNode node = new DecisionTreeNode(v, simpleNet);

        node.setEvaluation(new CENodeEvaluation(1.0, new CEP.CEPBuilder().build(null, cost, eff)));

        return node;
    }

    private DecisionTreeBranch branch(State state, DecisionTreeNode child) {
        DecisionTreeBranch b = new DecisionTreeBranch(state);
        b.setChild(child);

        return b;
    }

    private DecisionTreeNode createUnicriteriaTree() {
        Variable decision = simpleNet.getVariable("Decision");

        DecisionTreeNode root = new DecisionTreeNode(simpleNet.getNode("Decision"));
        root.setEvaluation(new UnicritNodeEvaluation(1.0, 0.0));

        Variable[] chanceVars = {
            simpleNet.getVariable("C_no"),
            simpleNet.getVariable("C_t1"),
            simpleNet.getVariable("C_t2")
        };

        Variable[] utilVars = {
            simpleNet.getVariable("U1"),
            simpleNet.getVariable("U2"),
            simpleNet.getVariable("U3"),
            simpleNet.getVariable("U4"),
            simpleNet.getVariable("U5"),
            simpleNet.getVariable("U6")
        };

        int utilIndex = 0;

        for (int i = 0; i < chanceVars.length; i++) {
            DecisionTreeNode chance = new DecisionTreeNode(chanceVars[i], simpleNet);
            chance.setEvaluation(new UnicritNodeEvaluation(1.0, 0.0));
            DecisionTreeBranch b = new DecisionTreeBranch(decision.getStates()[i]);

            b.setChild(chance);
            root.addChild(b);

            for (State s : chanceVars[i].getStates()) {
                DecisionTreeNode util = new DecisionTreeNode(utilVars[utilIndex++], simpleNet);
                util.setEvaluation(new UnicritNodeEvaluation(1.0, 2.0));
                DecisionTreeBranch cb = new DecisionTreeBranch(s);
                cb.setChild(util);
                chance.addChild(cb);
            }
        }
        return root;
    }

    private List<Criterion> getCorrectCriteria() {
        Criterion cost = new Criterion("Cost", "units");
        cost.setCECriterion(Criterion.CECriterion.Cost);
        Criterion eff = new Criterion("Effectiveness", "units");
        eff.setCECriterion(Criterion.CECriterion.Effectiveness);
        return List.of(cost, eff);
    }

    private List<Criterion> getWrongCriteria() {
        Criterion cost = new Criterion("Cost", "units");
        cost.setCECriterion(Criterion.CECriterion.Cost);
        Criterion eff = new Criterion("Effectiveness", "units");
        eff.setCECriterion(Criterion.CECriterion.Cost);
        return List.of(cost, eff);
    }

    private List<Criterion> getOverloadedCriteria() {
        Criterion cost = new Criterion("Cost", "units");
        cost.setCECriterion(Criterion.CECriterion.Cost);
        Criterion eff = new Criterion("Effectiveness", "units");
        eff.setCECriterion(Criterion.CECriterion.Effectiveness);
        Criterion extra = new Criterion("anotherCriterion", "units");
        return List.of(cost, eff, extra);
    }
}