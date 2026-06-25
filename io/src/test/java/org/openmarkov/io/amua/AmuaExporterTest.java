package org.openmarkov.io.amua;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AmuaExporterTest {

    // AmuaDTValidatorTest verifies whether a tree can or cannot be exported to Amua.

    private AmuaExporter amuaExporter;
    private ProbNet probNet;

    @Test
    void constructorThrowsIfTreeNodeNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new AmuaExporter(null));
        assertEquals("The decision tree root node cannot be null.", ex.getMessage());
    }

    @Test
    void writeInvalidTree() throws Exception {
        EvaluationDecisionTreeNode ddtuRootInvalid = buildDTDUTreeInvalid();
        amuaExporter = new AmuaExporter(ddtuRootInvalid);

        File file = File.createTempFile("InvalidTree", ".amua");
        file.deleteOnExit();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {amuaExporter.writeAmuaDT(file);});

        assertTrue(amuaExporter.getHasBeenValidatedDT());
        assertFalse(amuaExporter.getIsValidDT());
        assertNotNull(amuaExporter.getValidationErrorMessage());
        assertEquals("NOT VALID: \nTree type not supported by Amua.", ex.getMessage());
    }


    @Test
    void writeIDUnicriteriaDTDUTest() throws Exception {
        EvaluationDecisionTreeNode ddtuRoot = buildDTDUTree();
        amuaExporter = new AmuaExporter(ddtuRoot);

        File file = File.createTempFile("Exporter_DTDU-test", ".amua");
        file.deleteOnExit();
        amuaExporter.writeAmuaDT(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void writeCEDAN2TherapiesTest() throws Exception {
        CEADecisionTreeNode dan2Root = buildDAN2TherapiesTree();
        amuaExporter = new AmuaExporter(dan2Root);

        File file = File.createTempFile("Exporter_DAN2Therapies-test", ".amua");
        file.deleteOnExit();
        amuaExporter.writeAmuaDT(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void writeCEDANangioTest() throws Exception {
        CEADecisionTreeNode danRoot = buildDANangioTree();
        amuaExporter = new AmuaExporter(danRoot);

        File file = File.createTempFile("Exporter_DAN-Angio-one-decision", ".amua");
        file.deleteOnExit();
        amuaExporter.writeAmuaDT(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }


    private EvaluationDecisionTreeNode buildDTDUTree() {
        Variable D = new Variable("Decision", 2);
        State dYes = D.getStates()[0]; dYes.setName("yes");
        State dNo = D.getStates()[1]; dNo.setName("no");

        Variable C_yes = new Variable("C_yes", 2);
        Variable C_no = new Variable("C_no", 2);

        Variable U1 = new Variable("U1");
        Variable U2 = new Variable("U2");
        Variable U3 = new Variable("U3");
        Variable U4 = new Variable("U4");

        probNet = new ProbNet();
        probNet.addNode(D, NodeType.DECISION);
        probNet.addNode(C_yes, NodeType.CHANCE);
        probNet.addNode(C_no, NodeType.CHANCE);
        probNet.addNode(U1, NodeType.UTILITY);
        probNet.addNode(U2, NodeType.UTILITY);
        probNet.addNode(U3, NodeType.UTILITY);
        probNet.addNode(U4, NodeType.UTILITY);

        EvaluationDecisionTreeNode decisionNode = new EvaluationDecisionTreeNode(D, probNet);
        EvaluationDecisionTreeNode chanceYes = new EvaluationDecisionTreeNode(C_yes, probNet);
        EvaluationDecisionTreeNode chanceNo = new EvaluationDecisionTreeNode(C_no, probNet);
        EvaluationDecisionTreeNode utilityNode1 = new EvaluationDecisionTreeNode(U1, probNet);
        EvaluationDecisionTreeNode utilityNode2 = new EvaluationDecisionTreeNode(U2, probNet);
        EvaluationDecisionTreeNode utilityNode3 = new EvaluationDecisionTreeNode(U3, probNet);
        EvaluationDecisionTreeNode utilityNode4 = new EvaluationDecisionTreeNode(U4, probNet);

        utilityNode1.setUtility(8.0);
        utilityNode2.setUtility(9.0);
        utilityNode3.setUtility(3.0);
        utilityNode4.setUtility(10.0);

        State cYes1 = C_yes.getStates()[0];
        cYes1.setName("absent");
        State cYes2 = C_yes.getStates()[1];
        cYes2.setName("present");
        State cNo1 = C_no.getStates()[0];
        cNo1.setName("absent");
        State cNo2 = C_no.getStates()[1];
        cNo2.setName("present");

        chanceYes.setScenarioProbability(1);
        chanceNo.setScenarioProbability(1);

        DecisionTreeBranch branchYes_U1 = createBranch(probNet, C_yes, cYes1, 0.14, utilityNode1);
        DecisionTreeBranch branchYes_U2 = createBranch(probNet, C_yes, cYes2, 0.86, utilityNode2);
        DecisionTreeBranch branchNo_U3 = createBranch(probNet, C_no, cNo1, 0.14, utilityNode3);
        DecisionTreeBranch branchNo_U4 = createBranch(probNet, C_no, cNo2, 0.86, utilityNode4);

        chanceYes.addChild(branchYes_U1);
        chanceYes.addChild(branchYes_U2);
        chanceNo.addChild(branchNo_U3);
        chanceNo.addChild(branchNo_U4);

        DecisionTreeBranch branchYes = createBranch(probNet, D, dYes, 1.0, chanceYes);
        DecisionTreeBranch branchNo = createBranch(probNet, D, dNo, 1.0, chanceNo);
        decisionNode.addChild(branchYes);
        decisionNode.addChild(branchNo);

        return decisionNode;
    }

    private EvaluationDecisionTreeNode buildDTDUTreeInvalid() {
        Variable D = new Variable("Decision", 2);
        State dYes = D.getStates()[0]; dYes.setName("yes");
        State dNo = D.getStates()[1]; dNo.setName("no");

        Variable C_yes = new Variable("C_yes", 2);
        Variable C_no = new Variable("C_no", 2);

        Variable U1 = new Variable("U1");
        Variable U2 = new Variable("U2");
        Variable U3 = new Variable("U3");
        Variable U4 = new Variable("U4");

        probNet = new ProbNet();
        probNet.addNode(D, NodeType.DECISION);
        probNet.addNode(C_yes, NodeType.CHANCE);
        probNet.addNode(C_no, NodeType.CHANCE);
        probNet.addNode(U1, NodeType.UTILITY);
        probNet.addNode(U2, NodeType.UTILITY);
        probNet.addNode(U3, NodeType.UTILITY);
        probNet.addNode(U4, NodeType.UTILITY);

        probNet.setDecisionCriteria(assignInvalidCriteriaList(0.0));

        EvaluationDecisionTreeNode decisionNode = new EvaluationDecisionTreeNode(D, probNet);
        EvaluationDecisionTreeNode chanceYes = new EvaluationDecisionTreeNode(C_yes, probNet);
        EvaluationDecisionTreeNode chanceNo = new EvaluationDecisionTreeNode(C_no, probNet);
        EvaluationDecisionTreeNode utilityNode1 = new EvaluationDecisionTreeNode(U1, probNet);
        EvaluationDecisionTreeNode utilityNode2 = new EvaluationDecisionTreeNode(U2, probNet);
        EvaluationDecisionTreeNode utilityNode3 = new EvaluationDecisionTreeNode(U3, probNet);
        EvaluationDecisionTreeNode utilityNode4 = new EvaluationDecisionTreeNode(U4, probNet);

        utilityNode1.setUtility(8.0);
        utilityNode2.setUtility(9.0);
        utilityNode3.setUtility(3.0);
        utilityNode4.setUtility(10.0);

        State cYes1 = C_yes.getStates()[0];
        cYes1.setName("absent");
        State cYes2 = C_yes.getStates()[1];
        cYes2.setName("present");
        State cNo1 = C_no.getStates()[0];
        cNo1.setName("absent");
        State cNo2 = C_no.getStates()[1];
        cNo2.setName("present");

        chanceYes.setScenarioProbability(1);
        chanceNo.setScenarioProbability(1);

        DecisionTreeBranch branchYes_U1 = createBranch(probNet, C_yes, cYes1, 0.14, utilityNode1);
        DecisionTreeBranch branchYes_U2 = createBranch(probNet, C_yes, cYes2, 0.86, utilityNode2);
        DecisionTreeBranch branchNo_U3 = createBranch(probNet, C_no, cNo1, 0.14, utilityNode3);
        DecisionTreeBranch branchNo_U4 = createBranch(probNet, C_no, cNo2, 0.86, utilityNode4);

        chanceYes.addChild(branchYes_U1);
        chanceYes.addChild(branchYes_U2);
        chanceNo.addChild(branchNo_U3);
        chanceNo.addChild(branchNo_U4);

        DecisionTreeBranch branchYes = createBranch(probNet, D, dYes, 1.0, chanceYes);
        DecisionTreeBranch branchNo = createBranch(probNet, D, dNo, 1.0, chanceNo);
        decisionNode.addChild(branchYes);
        decisionNode.addChild(branchNo);

        return decisionNode;
    }


    private CEADecisionTreeNode buildDAN2TherapiesTree() {
        Variable D = new Variable("Decision", 3);
        D.getStates()[0].setName("NO");
        D.getStates()[1].setName("THERAPY 1");
        D.getStates()[2].setName("THERAPY 2");

        Variable C_no = new Variable("C_no", 2);
        Variable C_t1 = new Variable("C_t1", 2);
        Variable C_t2 = new Variable("C_t2", 2);
        for (Variable v : List.of(C_no, C_t1, C_t2)) {
            v.getStates()[0].setName("ABSENT");
            v.getStates()[1].setName("PRESENT");
        }

        Variable U1 = new Variable("U1");
        Variable U2 = new Variable("U2");
        Variable U3 = new Variable("U3");
        Variable U4 = new Variable("U4");
        Variable U5 = new Variable("U5");
        Variable U6 = new Variable("U6");

        probNet = new ProbNet();
        probNet.addNode(D, NodeType.DECISION);
        probNet.addNode(C_no, NodeType.CHANCE);
        probNet.addNode(C_t1, NodeType.CHANCE);
        probNet.addNode(C_t2, NodeType.CHANCE);
        probNet.addNode(U1, NodeType.UTILITY);
        probNet.addNode(U2, NodeType.UTILITY);
        probNet.addNode(U3, NodeType.UTILITY);
        probNet.addNode(U4, NodeType.UTILITY);
        probNet.addNode(U5, NodeType.UTILITY);
        probNet.addNode(U6, NodeType.UTILITY);

        probNet.setDecisionCriteria(assignBasicCriteriaList(30000.0));

        CEADecisionTreeNode root = new CEADecisionTreeNode(D, probNet);
        CEADecisionTreeNode chanceNo = new CEADecisionTreeNode(C_no, probNet);
        CEADecisionTreeNode chanceT1 = new CEADecisionTreeNode(C_t1, probNet);
        CEADecisionTreeNode chanceT2 = new CEADecisionTreeNode(C_t2, probNet);
        CEADecisionTreeNode u1 = new CEADecisionTreeNode(U1, probNet);
        CEADecisionTreeNode u2 = new CEADecisionTreeNode(U2, probNet);
        CEADecisionTreeNode u3 = new CEADecisionTreeNode(U3, probNet);
        CEADecisionTreeNode u4 = new CEADecisionTreeNode(U4, probNet);
        CEADecisionTreeNode u5 = new CEADecisionTreeNode(U5, probNet);
        CEADecisionTreeNode u6 = new CEADecisionTreeNode(U6, probNet);

        u1.setUtility(new CEP.CEPBuilder().build(null, 0.0, 10.0));
        u2.setUtility(new CEP.CEPBuilder().build(null, 0.0, 1.2));
        u3.setUtility(new CEP.CEPBuilder().build(null, 20000.0, 9.9));
        u4.setUtility(new CEP.CEPBuilder().build(null, 20000.0, 4.0));
        u5.setUtility(new CEP.CEPBuilder().build(null, 70000.0, 9.3));
        u6.setUtility(new CEP.CEPBuilder().build(null, 70000.0, 6.5));

        State sNo = D.getStates()[0];
        State sT1 = D.getStates()[1];
        State sT2 = D.getStates()[2];

        State absentNo = C_no.getStates()[0];
        State presentNo = C_no.getStates()[1];
        State absentT1 = C_t1.getStates()[0];
        State presentT1 = C_t1.getStates()[1];
        State absentT2 = C_t2.getStates()[0];
        State presentT2 = C_t2.getStates()[1];

        chanceNo.setScenarioProbability(1);
        chanceT1.setScenarioProbability(1);
        chanceT2.setScenarioProbability(1);

        DecisionTreeBranch bNo = createBranch(probNet, D, sNo, 1.0, chanceNo);
        DecisionTreeBranch bT1 = createBranch(probNet, D, sT1, 1.0, chanceT1);
        DecisionTreeBranch bT2 = createBranch(probNet, D, sT2, 1.0, chanceT2);

        root.addChild(bNo);
        root.addChild(bT1);
        root.addChild(bT2);

        chanceNo.addChild(createBranch(probNet, C_no, absentNo, 0.86, u1));
        chanceNo.addChild(createBranch(probNet, C_no, presentNo, 0.14, u2));
        chanceT1.addChild(createBranch(probNet, C_t1, absentT1, 0.86, u3));
        chanceT1.addChild(createBranch(probNet, C_t1, presentT1, 0.14, u4));
        chanceT2.addChild(createBranch(probNet, C_t2, absentT2, 0.86, u5));
        chanceT2.addChild(createBranch(probNet, C_t2, presentT2, 0.14, u6));

        return root;
    }


    private CEADecisionTreeNode buildDANangioTree() {
        Variable decision = new Variable("Decision", 3);
        decision.getStates()[0].setName("angiogram");
        decision.getStates()[1].setName("no AC");
        decision.getStates()[2].setName("AC");

        Variable dieAngio = new Variable("Die from angio", 2);
        dieAngio.getStates()[0].setName("no");
        dieAngio.getStates()[1].setName("yes");

        Variable pe = new Variable("PE", 2);
        pe.getStates()[0].setName("absent");
        pe.getStates()[1].setName("present");

        Variable die = new Variable("Die", 2);
        die.getStates()[0].setName("no");
        die.getStates()[1].setName("yes");

        Variable qale = new Variable("QALE");
        Variable costAngio = new Variable("C: Angio");
        Variable costAC = new Variable("C: AC");

        probNet = new ProbNet();
        probNet.addNode(decision, NodeType.DECISION);
        probNet.addNode(dieAngio, NodeType.CHANCE);
        probNet.addNode(pe, NodeType.CHANCE);
        probNet.addNode(die, NodeType.CHANCE);
        probNet.addNode(qale, NodeType.UTILITY);
        probNet.addNode(costAngio, NodeType.UTILITY);
        probNet.addNode(costAC, NodeType.UTILITY);

        probNet.setDecisionCriteria(assignBasicCriteriaList(25000));

        CEADecisionTreeNode root = new CEADecisionTreeNode(decision, probNet);
        CEADecisionTreeNode dieAngioNode = new CEADecisionTreeNode(dieAngio, probNet);
        CEADecisionTreeNode peNode = new CEADecisionTreeNode(pe, probNet);
        CEADecisionTreeNode dieNodeAbsent = new CEADecisionTreeNode(die, probNet);
        CEADecisionTreeNode dieNodePresent = new CEADecisionTreeNode(die, probNet);

        CEADecisionTreeNode uDieAngio = new CEADecisionTreeNode(qale, probNet);
        uDieAngio.setUtility(new CEP.CEPBuilder().build(null, 0.0, 0.0));

        CEADecisionTreeNode uPEAbsentDieNo = new CEADecisionTreeNode(qale, probNet);
        uPEAbsentDieNo.setUtility(new CEP.CEPBuilder().build(null, 1000.0, 7.0));

        CEADecisionTreeNode uPEAbsentDieYes = new CEADecisionTreeNode(qale, probNet);
        uPEAbsentDieYes.setUtility(new CEP.CEPBuilder().build(null, 300.0, 4.0));

        CEADecisionTreeNode uPEPresentDieNo = new CEADecisionTreeNode(qale, probNet);
        uPEPresentDieNo.setUtility(new CEP.CEPBuilder().build(null, 500.0, 8.0));

        CEADecisionTreeNode uPEPresentDieYes = new CEADecisionTreeNode(qale, probNet);
        uPEPresentDieYes.setUtility(new CEP.CEPBuilder().build(null, 600.0, 5.5));

        dieAngioNode.setScenarioProbability(1);
        peNode.setScenarioProbability(1);
        dieNodeAbsent.setScenarioProbability(1);
        dieNodePresent.setScenarioProbability(1);

        State angiogram = decision.getStates()[0];
        State noAC = decision.getStates()[1];
        State ac = decision.getStates()[2];

        State dieAngioNo = dieAngio.getStates()[0];
        State dieAngioYes = dieAngio.getStates()[1];

        State peAbsent = pe.getStates()[0];
        State pePresent = pe.getStates()[1];

        State dieNo = die.getStates()[0];
        State dieYes = die.getStates()[1];

        DecisionTreeBranch dieNoBranch1 = createBranch(probNet, die, dieNo, 1.0, uPEAbsentDieNo);
        DecisionTreeBranch dieYesBranch1 = createBranch(probNet, die, dieYes, 0.0, uPEAbsentDieYes);

        DecisionTreeBranch dieNoBranch2 = createBranch(probNet, die, dieNo, 0.95, uPEPresentDieNo);
        DecisionTreeBranch dieYesBranch2 = createBranch(probNet, die, dieYes, 0.05, uPEPresentDieYes);

        dieNodeAbsent.addChild(dieNoBranch1);
        dieNodeAbsent.addChild(dieYesBranch1);
        dieNodePresent.addChild(dieNoBranch2);
        dieNodePresent.addChild(dieYesBranch2);

        DecisionTreeBranch peAbsentBranch = createBranch(probNet, pe, peAbsent, 0.81, dieNodeAbsent);
        DecisionTreeBranch pePresentBranch = createBranch(probNet, pe, pePresent, 0.19, dieNodePresent);

        peNode.addChild(peAbsentBranch);
        peNode.addChild(pePresentBranch);

        DecisionTreeBranch angioDeath = createBranch(probNet, dieAngio, dieAngioYes, 0.01, uDieAngio);
        DecisionTreeBranch angioSurvive = createBranch(probNet, dieAngio, dieAngioNo, 0.99, peNode);

        dieAngioNode.addChild(angioDeath);
        dieAngioNode.addChild(angioSurvive);

        DecisionTreeBranch bAngio = createBranch(probNet, decision, angiogram, 1.0, dieAngioNode);
        DecisionTreeBranch bNoAC = createBranch(probNet, decision, noAC, 1.0, peNode);
        DecisionTreeBranch bAC = createBranch(probNet, decision, ac, 1.0, peNode);

        root.addChild(bAngio);
        root.addChild(bNoAC);
        root.addChild(bAC);

        return root;
    }


    private List<Criterion> assignBasicCriteriaList(double wtp){
        Criterion costCriterion = new Criterion("Cost", "$");
        costCriterion.setCECriterion(Criterion.CECriterion.Cost);
        costCriterion.setUnicriterizationScale(wtp);

        Criterion effectivenessCriterion = new Criterion("Effectiveness", "QALY");
        effectivenessCriterion.setCECriterion(Criterion.CECriterion.Effectiveness);
        return List.of(costCriterion, effectivenessCriterion);
    }


    private List<Criterion> assignInvalidCriteriaList(double wtp){
        Criterion costCriterion = new Criterion("Cost", "$");
        costCriterion.setCECriterion(Criterion.CECriterion.Cost);
        costCriterion.setUnicriterizationScale(wtp);

        Criterion costCriterionB = new Criterion("Cost", "$");
        costCriterionB.setCECriterion(Criterion.CECriterion.Cost);
        return List.of(costCriterion, costCriterionB);
    }


    private DecisionTreeBranch createBranch(ProbNet net, Variable variable, State state, double probability, DecisionTreeNode<?> child) {
        DecisionTreeBranch branch = new DecisionTreeBranch(net, variable, state);
        branch.setScenarioProbability(probability);
        branch.setChild(child);
        return branch;
    }

}
