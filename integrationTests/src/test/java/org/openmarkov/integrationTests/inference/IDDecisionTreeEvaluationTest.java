package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.IDDecisionTreeEvaluation;
import org.openmarkov.integrationTests.inference.heuristics.Tools;

import java.io.IOException;
import java.net.URISyntaxException;

//@Ignore
public class IDDecisionTreeEvaluationTest extends NetworkEvaluationInferenceTest {
    
    @Override
    protected DANEvaluation buildNetworkEvaluation(ProbNet network) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return buildNetworkEvaluation(network, true);
    }
    
    @Override
    protected ProbNet loadNetwork(String networkName) throws ProbNetParserException, IOException {
        Tools t = new Tools();
        return t.loadID(networkName);
    }
    
    @Test
    public void testIDOnlyUtility() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-utility", 10.0);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testIDOneChance() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("one-chance", 83.7);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testIDOneDecision() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("one-decision", 87.4, "D");
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testIDNoKnowledge() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("no-knowledge", 9.16, "D");
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testIDPerfectKnowledge() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("perfect-knowledge", 9.72, "D", "A");
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testIDTest2Therapies() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("test-2therapies", 9.39366, "Test", "Therapy");
    }
    
    @Test
    public void testIDOnlyDecisionNoUtility() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-decision-no-utility", 0.0, "D");
    }
    
    
    @Override
    public void testNetworkEvaluation(ProbNet network, double expectedEU, String... namesVariablesIntervention) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluationAndDecisionTree(network, expectedEU, namesVariablesIntervention);
    }
    
    @Test
    public void testIDTest2Therapies_Tree() throws NotEvaluableNetworkException, ProbNetParserException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates, IOException {
        ProbNet network = loadNetwork("test-2therapies");
        IDDecisionTreeEvaluation eval = new IDDecisionTreeEvaluation(network, Integer.MAX_VALUE, true, new EvidenceCase());
        Tools.testDecisionTreeNode(eval.getDecisionTree(), false);
    }
    
    @Override
    protected DANEvaluation buildNetworkEvaluation(ProbNet network, boolean computeDecisionTreeForGUI)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return new IDDecisionTreeEvaluation(network, computeDecisionTreeForGUI);
    }
    
    
}
