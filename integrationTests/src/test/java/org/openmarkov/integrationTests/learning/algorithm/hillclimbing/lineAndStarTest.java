package org.openmarkov.integrationTests.learning.algorithm.hillclimbing;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.learning.algorithm.hillclimbing.HillClimbingAlgorithm;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.exception.EmptyModelNetException;
import org.openmarkov.learning.core.exception.UnobservedVariablesException;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.metric.k2.K2Metric;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

public class lineAndStarTest {
    
    @Test
    @Tag(TestSpeed.SLOW)
    public void testLineAndStarXLSX() throws NoWriterForExtensionException, ParsingSourceException, IOException, EmptyDatabaseException, EmptyModelNetException, UnobservedVariablesException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, DoEditException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException, ConstraintViolatedException {
        test();
    }
    
    private static void test() throws NoWriterForExtensionException, IOException, ParsingSourceException, EmptyDatabaseException, EmptyModelNetException, UnobservedVariablesException, DoEditException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException, ConstraintViolatedException {
        var xlsxReader = new CaseDatabaseManager().getReader("xlsx");
        String xlsxDatabase = IntegrationTest.class.getResource("/networks/learning/lineAndStar.xlsx").getPath();
        CaseDatabase cases = xlsxReader.load(new File(xlsxDatabase));
        LearningManager learningManager = new LearningManager(cases, HillClimbingAlgorithm.class, null, null);
        LearningAlgorithm learningAlgorithm = new HillClimbingAlgorithm(learningManager.getLearnedNet(), cases, 0.5, new K2Metric());
        learningManager.init(learningAlgorithm);
        
        int stepIndex = 0;
        while (true) {
            ArrayList<LearningEditProposal> proposedEdits = new ArrayList<>();
            LearningEditProposal bestEdition = learningManager.getLearningAlgorithm().getBestEdit(true, true);
            while (bestEdition != null) {
                proposedEdits.add(bestEdition);
                bestEdition = learningManager.getLearningAlgorithm().getNextEdit(true, true);
            }
            if (proposedEdits.isEmpty()) {
                break;
            }
            var net = learningManager.getLearnedNet();
            
            List<PNEdit> edits = proposedEdits.stream().map(LearningEditProposal::getEdit).toList();
            List<PNEdit> expectedEdits = editsOfStep(stepIndex, net);
            if (!bothEditListsAreTheSame(expectedEdits, edits)) {
                fail("The expected edits and the proposed edits differ in step " + stepIndex + " (Steps are 0-based)."
                             + System.lineSeparator() + "Expected edits: " + expectedEdits
                             + System.lineSeparator() + "Proposed edits: " + edits);
            }
            learningManager.applyEdit(proposedEdits.get(0).getEdit());
            stepIndex += 1;
        }
        if (stepIndex < EXPECTED_EDITS.size()) {
            fail("Less steps than expected have been made, remaining steps are: " + EXPECTED_EDITS.subList(stepIndex, EXPECTED_EDITS.size()));
        } else if (stepIndex > EXPECTED_EDITS.size()) {
            fail("More steps than expected have been made");
        }
    }
    
    private static @Nullable List<PNEdit> editsOfStep(int stepIndex, ProbNet net) {
        return EXPECTED_EDITS.stream().skip(stepIndex).findFirst()
                             .map(editsGenerators -> editsGenerators.stream()
                                                                    .map(generator -> generator.apply(net))
                                                                    .toList())
                             .orElse(null);
    }
    
    private static boolean bothEditListsAreTheSame(List<PNEdit> pnEditList1, List<PNEdit> pnEditList2) {
        if (pnEditList1 == null && pnEditList2 != null) return false;
        if (pnEditList1 != null && pnEditList2 == null) return false;
        if (pnEditList1.size() != pnEditList2.size()) return false;
        for (int i = 0; i < pnEditList1.size(); i++) {
            PNEdit edit1 = pnEditList1.get(i);
            PNEdit edit2 = pnEditList2.get(i);
            if (edit1.getClass() != edit2.getClass()) return false;
            
            switch (edit1) {
                case AddLinkEdit addLinkEdit1 -> {
                    var addLinkEdit2 = (AddLinkEdit) edit2;
                    if (addLinkEdit1.getNodeFrom() != addLinkEdit2.getNodeFrom() || addLinkEdit1.getNodeTo() != addLinkEdit2.getNodeTo() || addLinkEdit1.isDirected() != addLinkEdit2.isDirected()) {
                        return false;
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static final List<List<Function<ProbNet, PNEdit>>> EXPECTED_EDITS = List.of(
            List.of(
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("B"), probNet.getVariable("A"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("A"), probNet.getVariable("B"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("B"), probNet.getVariable("I"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("I"), probNet.getVariable("B"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("A"), probNet.getVariable("I"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("I"), probNet.getVariable("A"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("H"), probNet.getVariable("F"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("F"), probNet.getVariable("H"), true)
            ),
            List.of(
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("B"), probNet.getVariable("I"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("I"), probNet.getVariable("B"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("A"), probNet.getVariable("I"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("H"), probNet.getVariable("F"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("F"), probNet.getVariable("H"), true)
            ),
            List.of(
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("I"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("H"), probNet.getVariable("F"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("F"), probNet.getVariable("H"), true)
            ),
            List.of(
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("H"), probNet.getVariable("F"), true),
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("F"), probNet.getVariable("H"), true)
            ),
            List.of(
                    probNet -> new AddLinkEdit(probNet, probNet.getVariable("I"), probNet.getVariable("F"), true)
            )
    );
    
}
