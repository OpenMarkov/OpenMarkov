package org.openmarkov.learning.algorithm.pc;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.core.util.LearningEditProposal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** A removal that the user blocks in the interactive learning stays blocked, and the learning goes on. */
class PCBlockedRemovalTest {

    /**
     * What the interactive dialog does: it fills its table with proposals, the user blocks a removal
     * and presses "Finish", which runs the learning to the end. Whichever removal is blocked, it must not fail nor remove that link.
     */
    @Test
    void theLearningFinishesAndKeepsTheLinkOfABlockedRemoval() throws Exception {
        int numRemovals = proposals(newAlgorithm()).size();
        assertThat(numRemovals).isGreaterThan(1);

        for (int blocked = 0; blocked < numRemovals; blocked++) {
            PCAlgorithm pc = newAlgorithm();
            LearningEditProposal proposal = proposals(pc).get(blocked);
            RemoveLinkEdit removal = (RemoveLinkEdit) proposal.getEdit();
            pc.blockEdit(proposal);

            // "Finish" starts the algorithm again before it runs it to the end
            pc.init(null);
            for (int step = 0; step < 1000; step++) {
                LearningEditProposal next = pc.getBestEdit(true, true);
                if (next == null) {
                    break;
                }
                PNEdit edit = next.getEdit();
                edit.executeEdit();
                pc.afterEditExecutes(edit);
            }

            ProbNet net = removal.getProbNet();
            Node from = net.getNode(removal.getVariableFrom());
            Node to = net.getNode(removal.getVariableTo());
            assertThat(from.getNeighbors()).as("blocked removal %d: %s", blocked, removal).contains(to);
        }
    }

    /** The removals that the dialog shows at the start: the best one and the following ones. */
    private static List<LearningEditProposal> proposals(PCAlgorithm pc) {
        List<LearningEditProposal> proposals = new ArrayList<>();
        LearningEditProposal proposal = pc.getBestEdit(true, true);
        while (proposal != null && proposal.getEdit() instanceof RemoveLinkEdit && proposals.size() < 50) {
            proposals.add(proposal);
            proposal = pc.getNextEdit(true, true);
        }
        return proposals;
    }

    /** A complete graph over cases sampled from F -> A, A -> C, E -> C, C -> B, E -> B. */
    private static PCAlgorithm newAlgorithm() {
        List<Variable> variables = new ArrayList<>();
        for (String name : new String[]{"F", "A", "E", "C", "B"}) {
            variables.add(new Variable(name, 2));
        }
        ProbNet net = new ProbNet();
        List<Node> nodes = new ArrayList<>();
        for (Variable variable : variables) {
            nodes.add(net.addNode(variable, NodeType.CHANCE));
        }
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                net.addLink(nodes.get(i), nodes.get(j), false);
            }
        }
        Random random = new Random(7);
        int[][] cases = new int[5000][];
        for (int i = 0; i < cases.length; i++) {
            int f = random.nextInt(2);
            int a = random.nextDouble() < 0.8 ? f : 1 - f;
            int e = random.nextInt(2);
            int c = random.nextDouble() < new double[]{0.1, 0.6, 0.5, 0.95}[a + 2 * e] ? 1 : 0;
            int b = random.nextDouble() < new double[]{0.1, 0.7, 0.4, 0.9}[c + 2 * e] ? 1 : 0;
            cases[i] = new int[]{f, a, e, c, b};
        }
        return new PCAlgorithm(net, new CaseDatabase(variables, cases), 0.5,
                new CrossEntropyIndependenceTester(), 0.05, null);
    }
}
