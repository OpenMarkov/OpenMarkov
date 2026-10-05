package org.openmarkov.learning.algorithm.nbderived.spnb;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.metric.cmi.accuracy.Accuracy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** The superparent is linked to the other features, never to itself. */
class SuperParentIsNotItsOwnChildTest {

    private static final String CLASS = "Class";

    /**
     * With "Unique superparent", the option by default. The class is 1 when F1 and F2 differ,
     * so a link between them is needed to guess it.
     */
    @Test
    void aSingleSuperParentIsLinkedToTheOtherFeatures() throws Exception {
        List<Variable> variables = variables();
        Random random = new Random(7);
        int[][] cases = new int[400][4];
        for (int[] row : cases) {
            row[1] = random.nextInt(2);
            row[2] = random.nextInt(2);
            row[3] = random.nextInt(2);
            row[0] = row[1] ^ row[2];
        }
        ProbNet net = net(variables);
        SuperParentNBAlgorithm algorithm = new SuperParentNBAlgorithm(net,
                new CaseDatabase(new ArrayList<>(variables), cases), new Accuracy(), 0.5, true);
        algorithm.setClassVariableName(CLASS);

        algorithm.run(null);

        List<Link<Node>> linksBetweenFeatures = net.getLinks().stream()
                .filter(link -> !link.getFrom().getName().equals(CLASS)).toList();
        assertThat(linksBetweenFeatures).isNotEmpty();
        assertThat(linksBetweenFeatures).allMatch(link -> link.getFrom() != link.getTo());
        assertThat(linksBetweenFeatures).anyMatch(link ->
                List.of("F1", "F2").containsAll(List.of(link.getFrom().getName(), link.getTo().getName())));
    }

    /** Link by link: the link from the superparent to itself is not a candidate, whatever its score. */
    @Test
    void theLinkFromTheSuperParentToItselfIsNotProposed() {
        ProbNet net = net(variables());
        SuperParentNBAlgorithm algorithm = new SuperParentNBAlgorithm(net, null, new SelfLinkLover(), 0.5, false);
        algorithm.setClassVariableName(CLASS);
        algorithm.init(null);

        LearningEditProposal proposal = algorithm.getBestEdit(false, true);

        assertThat(proposal).isNotNull();
        AddLinkEdit edit = (AddLinkEdit) proposal.getEdit();
        assertThat(edit.getVariableFrom().getName()).isEqualTo("F2");
        assertThat(edit.getVariableTo().getName()).isEqualTo("F1");
    }

    private static List<Variable> variables() {
        return List.of(new Variable(CLASS, 2), new Variable("F1", 2), new Variable("F2", 2), new Variable("F3", 2));
    }

    private static ProbNet net(List<Variable> variables) {
        ProbNet net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        return net;
    }

    /** F2 is the best superparent, and its best link is the one to itself; the next one is the link to F1. */
    private static class SelfLinkLover extends Accuracy {
        @Override
        public double getScore() {
            return 0.5;
        }

        @Override
        public double getScore(PNEdit edit) {
            BaseLinkEdit link = (BaseLinkEdit) edit;
            return switch (link.getVariableFrom().getName() + ":" + link.getVariableTo().getName()) {
                case "Class:F2" -> 0.8;
                case "F2:F2" -> 0.9;
                case "F2:F1" -> 0.7;
                default -> 0.1;
            };
        }

        @Override
        public double score(TablePotential nodePotential) {
            return 0;
        }

        @Override
        protected void initCache() {
            // Nothing to prepare: there is no case database
        }
    }
}
