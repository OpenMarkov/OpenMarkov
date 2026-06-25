package org.openmarkov.io.amua.adapter;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;
import org.openmarkov.io.amua.adatper.AmuaDTConverter;
import org.openmarkov.io.amua.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmuaDTConverterTest {

    @Test
    void convertNullNodeThrowsTest() {
        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.UNICRITERIA_DT);
        assertThrows(IllegalArgumentException.class, () -> converter.convertToAmuaTree(null));
    }

    @Test
    void convertUnicriteriaSingleNodeTest() {
        ProbNet net = createSimpleProbNet();
        Variable root = net.getVariables().getFirst();

        DecisionTreeNode<?> treeNode = new EvaluationDecisionTreeNode(root, net);

        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.UNICRITERIA_DT);
        AmuaDTUnicriteriaNode amuaNode = (AmuaDTUnicriteriaNode) converter.convertToAmuaTree(treeNode);

        assertNotNull(amuaNode);
        assertEquals("Root", amuaNode.getName());
        assertEquals(0, amuaNode.getType());
        assertEquals(0, amuaNode.getChildNodes().size());
        assertEquals(0.0, amuaNode.getPayoff());
        assertEquals(0, amuaNode.getLevel());
        assertTrue(amuaNode.getXPos() >= 0);
        assertTrue(amuaNode.getYPos() >= 0);
    }

    @Test
    void convertRootUtilityNodeTest() {
        ProbNet net = new ProbNet();
        Variable U = new Variable("U");
        net.addNode(U, NodeType.UTILITY);

        EvaluationDecisionTreeNode root = new EvaluationDecisionTreeNode(U, net);

        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.UNICRITERIA_DT);
        AmuaDTUnicriteriaNode amuaRoot = (AmuaDTUnicriteriaNode) converter.convertToAmuaTree(root);

        assertNotNull(amuaRoot);
        assertEquals("Root", amuaRoot.getName());
        assertEquals(2, amuaRoot.getType());
        assertEquals(0, amuaRoot.getChildNodes().size());
        assertEquals(0.0, amuaRoot.getPayoff());
        assertEquals(0, amuaRoot.getLevel());
        assertTrue(amuaRoot.getXPos() >= 0);
        assertTrue(amuaRoot.getYPos() >= 0);
    }

    @Test
    void convertCESingleNodeTest() {
        ProbNet net = createSimpleProbNet();
        Variable root = net.getVariables().getFirst();

        CEADecisionTreeNode treeNode = new CEADecisionTreeNode(root, net);
        treeNode.setUtility(new CEP.CEPBuilder().build(null, 0.0, 10.0));

        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.COST_EFFECTIVENESS_DT);
        AmuaDTCENode amuaNode = (AmuaDTCENode) converter.convertToAmuaTree(treeNode);

        assertNotNull(amuaNode);
        assertEquals("Root", amuaNode.getName());
        assertEquals(0, amuaNode.getType());
        assertEquals(0, amuaNode.getChildNodes().size());
        assertEquals(0.0, amuaNode.getPayoff().getCost());
        assertEquals(0.0, amuaNode.getPayoff().getEffectiveness());
        assertEquals(0, amuaNode.getLevel());
        assertTrue(amuaNode.getXPos() >= 0);
        assertTrue(amuaNode.getYPos() >= 0);
    }


    @Test
    void chanceNodeProbabilitiesCorrectionTest() {
        ProbNet net = new ProbNet();
        Variable D = new Variable("Decision", 2);
        Variable C = new Variable("Chance", 2);
        Variable U = new Variable("U");
        net.addNode(D, NodeType.DECISION);
        net.addNode(C, NodeType.CHANCE);
        net.addNode(U, NodeType.UTILITY);

        EvaluationDecisionTreeNode root = new EvaluationDecisionTreeNode(D, net);
        EvaluationDecisionTreeNode chanceNode = new EvaluationDecisionTreeNode(C, net);
        EvaluationDecisionTreeNode utilityNode = new EvaluationDecisionTreeNode(U, net);
        utilityNode.setUtility(10.0);

        root.addChild(branch(net, D, D.getStates()[0], chanceNode));
        chanceNode.addChild(branch(net, C, C.getStates()[0], utilityNode, 0.7));
        chanceNode.addChild(branch(net, C, C.getStates()[1], utilityNode, 0.4));

        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.UNICRITERIA_DT);
        AmuaDTNode<?> amuaRoot = converter.convertToAmuaTree(root);

        AmuaDTNode<?> amuaChance = amuaRoot.getChildNodes().get(0);
        double sumProb = 0.0;
        for (AmuaDTNode<?> child : amuaChance.getChildNodes()) {
            sumProb += child.getProbability();
        }
        assertEquals(1.0, sumProb, 1e-4);

        AmuaDTNode<?> leaf0 = amuaChance.getChildNodes().get(0);
        AmuaDTNode<?> leaf1 = amuaChance.getChildNodes().get(1);
        assertEquals(2, amuaChance.getChildNodes().size());
        assertTrue(leaf0.getProbability() > 0);
        assertTrue(leaf1.getProbability() > 0);
    }


    @Test
    void CEAWithMissingUtilitiesTest() {
        ProbNet net = new ProbNet();
        Variable D = new Variable("Decision", 2);
        Variable C = new Variable("Chance", 2);
        net.addNode(D, NodeType.DECISION);
        net.addNode(C, NodeType.CHANCE);
        CEADecisionTreeNode root = new CEADecisionTreeNode(D, net);
        CEADecisionTreeNode chanceNode = new CEADecisionTreeNode(C, net);
        root.addChild(branch(net, D, D.getStates()[0], chanceNode));
        root.addChild(branch(net, D, D.getStates()[1], chanceNode));
        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.COST_EFFECTIVENESS_DT);
        AmuaDTNode<?> amuaRoot = converter.convertToAmuaTree(root);

        for (AmuaDTNode<?> decisionChild : amuaRoot.getChildNodes()) {
            if (decisionChild instanceof AmuaDTCENode) {
                AmuaDTCENode ceNode = (AmuaDTCENode) decisionChild;
                assertNotNull(ceNode.getPayoff());
                assertEquals(0.0, ceNode.getPayoff().getCost());
                assertEquals(0.0, ceNode.getPayoff().getEffectiveness());
            } else {
                for (AmuaDTNode<?> child : decisionChild.getChildNodes()) {
                    assertInstanceOf(AmuaDTCENode.class, child);
                    AmuaDTCENode ceNode = (AmuaDTCENode) child;
                    assertNotNull(ceNode.getPayoff());
                    assertEquals(0.0, ceNode.getPayoff().getCost());
                    assertEquals(0.0, ceNode.getPayoff().getEffectiveness());
                }
            }
        }
    }

    @Test
    void convertDTDUtest() {
        ProbNet net = new ProbNet();
        DecisionTreeNode<?> root = createSimpleDTDU(net);

        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.UNICRITERIA_DT);
        AmuaDTNode<?> amuaRoot = converter.convertToAmuaTree(root);

        assertEquals(2, amuaRoot.getChildNodes().size());

        AmuaDTNode<?> child0 = amuaRoot.getChildNodes().get(0);
        assertNotNull(child0.getPayoff());
        assertInstanceOf(Double.class, child0.getPayoff());

        AmuaDTNode<?> child1 = amuaRoot.getChildNodes().get(1);
        assertNotNull(child1.getPayoff());
        assertInstanceOf(Double.class, child1.getPayoff());
    }

    @Test
    void converterDAN2TherapiesTest() {
        ProbNet net = new ProbNet();
        CEADecisionTreeNode root = createTwoTherapiesTree(net);

        AmuaDTConverter converter = new AmuaDTConverter(AmuaModel.COST_EFFECTIVENESS_DT);
        AmuaDTNode<?> amuaRoot = converter.convertToAmuaTree(root);

        assertNotNull(amuaRoot);
        assertEquals(3, amuaRoot.getChildNodes().size());

        AmuaDTNode<?> decision0 = amuaRoot.getChildNodes().get(0);
        assertEquals(2, decision0.getChildNodes().size());

        AmuaDTNode<?> chanceNode0 = decision0.getChildNodes().get(0);
        assertInstanceOf(AmuaDTCENode.class, chanceNode0);
        assertNotNull(((AmuaDTCENode) chanceNode0).getPayoff());
        assertTrue(((AmuaDTCENode) chanceNode0).getPayoff().getCost() >= 0);
        assertTrue(((AmuaDTCENode) chanceNode0).getPayoff().getEffectiveness() >= 0);

        AmuaDTNode<?> chanceNode1 = decision0.getChildNodes().get(1);
        assertInstanceOf(AmuaDTCENode.class, chanceNode1);
        assertNotNull(((AmuaDTCENode) chanceNode1).getPayoff());
        assertTrue(((AmuaDTCENode) chanceNode1).getPayoff().getCost() >= 0);
        assertTrue(((AmuaDTCENode) chanceNode1).getPayoff().getEffectiveness() >= 0);
    }


    private ProbNet createSimpleProbNet() {
        ProbNet net = new ProbNet();

        Variable D = new Variable("D");
        Variable A = new Variable("A",2);
        Variable U = new Variable("U");

        net.addNode(D, NodeType.DECISION);
        net.addNode(A, NodeType.CHANCE);
        net.addNode(U, NodeType.UTILITY);

        TablePotential pA = new TablePotential(List.of(A), PotentialRole.CONDITIONAL_PROBABILITY);
        pA.getValues()[0] = 0.6; pA.getValues()[1] = 0.4;

        ExactDistrPotential pU = new ExactDistrPotential(List.of(U,A,D), PotentialRole.CONDITIONAL_PROBABILITY);
        pU.setValues(new double[]{10,5,15,20});

        net.addPotential(pA);
        net.addPotential(pU);
        net.addLink(A,U,true);
        net.addLink(D,U,true);

        return net;
    }

    private EvaluationDecisionTreeNode createSimpleDTDU(ProbNet net) {
        Variable D = new Variable("Decision", 2);
        Variable C_yes = new Variable("C_yes", 2);
        Variable C_no = new Variable("C_no", 2);
        Variable U1 = new Variable("U1");
        Variable U2 = new Variable("U2");
        Variable U3 = new Variable("U3");
        Variable U4 = new Variable("U4");

        setStateNames(D, "yes", "no");
        setStateNames(C_yes, "absent", "present");
        setStateNames(C_no, "absent", "present");

        net.addNode(D, NodeType.DECISION);
        net.addNode(C_yes, NodeType.CHANCE);
        net.addNode(C_no, NodeType.CHANCE);
        net.addNode(U1, NodeType.UTILITY);
        net.addNode(U2, NodeType.UTILITY);
        net.addNode(U3, NodeType.UTILITY);
        net.addNode(U4, NodeType.UTILITY);

        EvaluationDecisionTreeNode decision = new EvaluationDecisionTreeNode(D, net);
        EvaluationDecisionTreeNode chanceYes = new EvaluationDecisionTreeNode(C_yes, net);
        EvaluationDecisionTreeNode chanceNo = new EvaluationDecisionTreeNode(C_no, net);

        EvaluationDecisionTreeNode u1 = new EvaluationDecisionTreeNode(U1, net);
        u1.setUtility(8.0);
        EvaluationDecisionTreeNode u2 = new EvaluationDecisionTreeNode(U2, net);
        u2.setUtility(9.0);
        EvaluationDecisionTreeNode u3 = new EvaluationDecisionTreeNode(U3, net);
        u3.setUtility(3.0);
        EvaluationDecisionTreeNode u4 = new EvaluationDecisionTreeNode(U4, net);
        u4.setUtility(10.0);

        decision.addChild(branch(net, D, D.getStates()[0], chanceYes));
        decision.addChild(branch(net, D, D.getStates()[1], chanceNo));

        chanceYes.addChild(branch(net, C_yes, C_yes.getStates()[0], u1, 0.14));
        chanceYes.addChild(branch(net, C_yes, C_yes.getStates()[1], u2, 0.86));
        chanceNo.addChild(branch(net, C_no, C_no.getStates()[0], u3, 0.14));
        chanceNo.addChild(branch(net, C_no, C_no.getStates()[1], u4, 0.86));

        return decision;
    }

    private CEADecisionTreeNode createTwoTherapiesTree(ProbNet net) {
        Variable D = new Variable("Decision",3);
        Variable C_no = new Variable("C_no",2);
        Variable C_t1 = new Variable("C_t1",2);
        Variable C_t2 = new Variable("C_t2",2);
        Variable U1 = new Variable("U1"); Variable U2 = new Variable("U2");
        Variable U3 = new Variable("U3"); Variable U4 = new Variable("U4");
        Variable U5 = new Variable("U5"); Variable U6 = new Variable("U6");

        setStateNames(D, "NO","THERAPY 1","THERAPY 2");
        setStateNames(C_no,"ABSENT","PRESENT");
        setStateNames(C_t1,"ABSENT","PRESENT");
        setStateNames(C_t2,"ABSENT","PRESENT");

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

        CEADecisionTreeNode root = new CEADecisionTreeNode(D, net);
        CEADecisionTreeNode chanceNo = new CEADecisionTreeNode(C_no, net);
        CEADecisionTreeNode chanceT1 = new CEADecisionTreeNode(C_t1, net);
        CEADecisionTreeNode chanceT2 = new CEADecisionTreeNode(C_t2, net);

        CEADecisionTreeNode u1 = new CEADecisionTreeNode(U1, net);
        u1.setUtility(new CEP.CEPBuilder().build(null, 0.0, 10.0));

        CEADecisionTreeNode u2 = new CEADecisionTreeNode(U2, net);
        u2.setUtility(new CEP.CEPBuilder().build(null, 0.0, 1.2));

        CEADecisionTreeNode u3 = new CEADecisionTreeNode(U3, net);
        u3.setUtility(new CEP.CEPBuilder().build(null, 20000.0, 9.9));

        CEADecisionTreeNode u4 = new CEADecisionTreeNode(U4, net);
        u4.setUtility(new CEP.CEPBuilder().build(null, 20000.0, 4.0));

        CEADecisionTreeNode u5 = new CEADecisionTreeNode(U5, net);
        u5.setUtility(new CEP.CEPBuilder().build(null, 70000.0, 9.3));

        CEADecisionTreeNode u6 = new CEADecisionTreeNode(U6, net);
        u6.setUtility(new CEP.CEPBuilder().build(null, 70000.0, 6.5));

        root.addChild(branch(net,D,D.getStates()[0],chanceNo));
        root.addChild(branch(net,D,D.getStates()[1],chanceT1));
        root.addChild(branch(net,D,D.getStates()[2],chanceT2));

        chanceNo.addChild(branch(net,C_no,C_no.getStates()[0],u1,0.86));
        chanceNo.addChild(branch(net,C_no,C_no.getStates()[1],u2,0.14));
        chanceT1.addChild(branch(net,C_t1,C_t1.getStates()[0],u3,0.86));
        chanceT1.addChild(branch(net,C_t1,C_t1.getStates()[1],u4,0.14));
        chanceT2.addChild(branch(net,C_t2,C_t2.getStates()[0],u5,0.86));
        chanceT2.addChild(branch(net,C_t2,C_t2.getStates()[1],u6,0.14));

        return root;
    }

    private DecisionTreeBranch branch(ProbNet net, Variable var, State state, DecisionTreeNode<?> child){
        DecisionTreeBranch b = new DecisionTreeBranch(net,var,state);
        b.setChild(child);
        return b;
    }

    private DecisionTreeBranch branch(ProbNet net, Variable var, State state, DecisionTreeNode<?> child, double probability){
        DecisionTreeBranch b = branch(net,var,state,child);
        b.setScenarioProbability(probability);
        return b;
    }

    private void setStateNames(Variable v, String... names){
        for(int i=0;i<names.length;i++){
            v.getStates()[i].setName(names[i]);
        }
    }
}