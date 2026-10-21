/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.GammaFunction;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.RangeFunction;
import org.openmarkov.core.model.network.modelUncertainty.TriangularFunction;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Disease (absent 0.9, present 0.1) is the parent of a numeric chance node N and of a utility U, worth 10 if the
 * disease is present and 0 if it is absent. Observing N has to change the probability of the disease, as observing
 * a test of finite states does.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFindingOnANumericNodeTellsAboutItsParentsTest {

    private Variable disease;
    private Variable n;

    /** The network, with the relation of N given by a distribution and its parameters for absent and present. */
    private ProbNet network(Class<? extends ProbDensFunction> distribution, double... parameters) {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        disease = new Variable("Disease", "absent", "present");
        n = new Variable("N", false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0.01);
        Variable u = new Variable("U");
        Node diseaseNode = probNet.addNode(disease, NodeType.CHANCE);
        Node nNode = probNet.addNode(n, NodeType.CHANCE);
        Node uNode = probNet.addNode(u, NodeType.UTILITY);
        probNet.addLink(diseaseNode, nNode, true);
        probNet.addLink(diseaseNode, uNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[] { 0.9, 0.1 }));
        UnivariateDistrPotential relation = new UnivariateDistrPotential(List.of(n, disease), distribution,
                                                                         PotentialRole.CONDITIONAL_PROBABILITY);
        relation.setParameters(parameters);
        nNode.setPotential(relation);
        TablePotential utility = new TablePotential(List.of(disease), PotentialRole.UNSPECIFIED, new double[] { 0, 10 });
        utility.setCriterion(probNet.getDecisionCriteria().getFirst());
        uNode.setPotential(utility);
        return probNet;
    }

    private EvidenceCase observed(double value) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(n, value));
        return evidence;
    }

    private double[] probabilityOfTheDisease(ProbNet probNet, EvidenceCase evidence) throws Exception {
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(disease));
        propagation.setPreResolutionEvidence(evidence);
        return propagation.getPosteriorValues().get(probNet.getVariable("Disease")).getValues();
    }

    private double expectedUtility(ProbNet probNet, EvidenceCase evidence) throws Exception {
        VEEvaluation evaluation = new VEEvaluation(probNet);
        evaluation.setPreResolutionEvidence(evidence);
        return evaluation.getUtility().getValues()[0];
    }

    /** N is 3 if the disease is absent and 7 if it is present: observing 7 means the disease is present. */
    @Test void anExactRelation() throws Exception {
        ProbNet probNet = network(ExactFunction.class, 3, 7);
        assertArrayEquals(new double[] { 0, 1 }, probabilityOfTheDisease(probNet, observed(7)), 1e-12);
        assertEquals(10, expectedUtility(probNet, observed(7)), 1e-12);
    }

    /** N is normal with mean 5 if the disease is absent and 8 if it is present, and deviation 1. */
    @Test void aNormalDistribution() throws Exception {
        ProbNet probNet = network(NormalFunction.class, 5, 1, 8, 1);
        double absent = 0.9 * Math.exp(-4.5);
        double present = 0.1;
        double[] expected = { absent / (absent + present), present / (absent + present) };
        assertArrayEquals(expected, probabilityOfTheDisease(probNet, observed(8)), 1e-9);
        assertEquals(10 * expected[1], expectedUtility(probNet, observed(8)), 1e-9);
    }

    /** Neither 3 nor 7: the finding is impossible, as for a test of finite states. */
    @Test void aValueTheRelationCannotGiveIsImpossible() {
        ProbNet probNet = network(ExactFunction.class, 3, 7);
        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class,
                     () -> probabilityOfTheDisease(probNet, observed(5)));
    }

    /** In inference mode the finding comes after the resolution, and tells the same. */
    @Test void aFindingAfterTheResolution() throws Exception {
        ProbNet probNet = network(NormalFunction.class, 5, 1, 8, 1);
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(disease));
        propagation.setPostResolutionEvidence(observed(8));
        assertArrayEquals(probabilityOfTheDisease(probNet, observed(8)),
                          propagation.getPosteriorValues().get(probNet.getVariable("Disease")).getValues(), 1e-12);
    }

    /**
     * N2 is a numeric node with a uniform relation, so it needs a finding. Another numeric finding used to make the
     * resolution die with an index out of bounds, because the values of the findings were written as the names of
     * their states, which a numeric finding does not have (error 269 of the pending list).
     */
    @Test void anotherNumericFindingDoesNotStopIt() throws Exception {
        ProbNet probNet = network(ExactFunction.class, 3, 7);
        Variable n2 = new Variable("N2", false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0.01);
        probNet.addNode(n2, NodeType.CHANCE).setPotential(new UniformPotential(List.of(n2), PotentialRole.CONDITIONAL_PROBABILITY));
        EvidenceCase evidence = observed(7);
        evidence.addFinding(new Finding(n2, 5.0));
        assertArrayEquals(new double[] { 0, 1 }, probabilityOfTheDisease(probNet, evidence), 1e-12);
    }

    /**
     * M, a numeric child of the disease, is 3 if it is absent and 7 if it is present; N is normal with mean {M} and
     * deviation 1. The formula of the mean has to read the number of M: it used to die, as in the test above.
     */
    @Test void aNumericParentGivesItsNumberToAFormula() throws Exception {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        disease = new Variable("Disease", "absent", "present");
        Variable m = new Variable("M", false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0.01);
        n = new Variable("N", false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0.01);
        Node diseaseNode = probNet.addNode(disease, NodeType.CHANCE);
        Node mNode = probNet.addNode(m, NodeType.CHANCE);
        Node nNode = probNet.addNode(n, NodeType.CHANCE);
        probNet.addLink(diseaseNode, mNode, true);
        probNet.addLink(mNode, nNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[] { 0.9, 0.1 }));
        UnivariateDistrPotential relationOfM = new UnivariateDistrPotential(List.of(m, disease), ExactFunction.class,
                                                                            PotentialRole.CONDITIONAL_PROBABILITY);
        relationOfM.setParameters(new double[] { 3, 7 });
        mNode.setPotential(relationOfM);
        List<Variable> variablesOfN = new ArrayList<>(List.of(n, m));
        UnivariateDistrPotential relationOfN = new UnivariateDistrPotential(variablesOfN, NormalFunction.class,
                                                                            PotentialRole.CONDITIONAL_PROBABILITY);
        relationOfN.setParameters(new double[] { 0, 1 });
        relationOfN.setParameter(0, new VariableExpression(variablesOfN, "{M}"));
        nNode.setPotential(relationOfN);

        double absent = 0.9 * Math.exp(-8); // the density at 7 of a normal of mean 3, but for the same factor
        double present = 0.1;               // and of mean 7
        assertArrayEquals(new double[] { absent / (absent + present), present / (absent + present) },
                          probabilityOfTheDisease(probNet, observed(7)), 1e-9);
    }

    /**
     * N is a gamma of shape 0.5 and scale 1 if the disease is absent, whose density is infinite at 0, and of shape 2
     * and scale 1 if it is present. Observing 0 used to weigh "absent" infinitely and leave the disease as NaN, without
     * a warning (error 270 of the pending list); now the finding is refused with a message.
     */
    @Test void aFindingWhereTheDensityIsInfiniteIsRefusedWithAMessage() throws Exception {
        ProbNet probNet = network(GammaFunction.class, 0.5, 1, 2, 1);
        String message = "The finding on N cannot be used: at 0, the density of its Gamma distribution is infinite.";
        var before = assertThrows(NonProjectablePotentialException.DensityIsInfinite.class,
                                  () -> probabilityOfTheDisease(probNet, observed(0)));
        assertEquals(message, before.getExceptionMessage());
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(disease));
        var after = assertThrows(NonProjectablePotentialException.DensityIsInfinite.class, () -> {
            propagation.setPostResolutionEvidence(observed(0));
            propagation.getPosteriorValues();
        });
        assertEquals(message, after.getExceptionMessage());
    }

    /**
     * Parameters that do not make a distribution in one configuration of the parents used to make the resolution die
     * with an exception of Commons Math, shown as an unexpected error (error 268 of the pending list); now the
     * finding is refused with a message that says where and which.
     */
    @Test void parametersThatDoNotMakeADistributionAreRefusedWithAMessage() {
        var refused = assertThrows(NonProjectablePotentialException.ParametersAreNotADistribution.class,
                                   () -> probabilityOfTheDisease(network(NormalFunction.class, 5, 0, 8, 1), observed(8)));
        assertEquals("The finding on N cannot be used: with Disease = absent, the parameters of its Normal "
                             + "distribution, mu = 5 and sigma = 0, do not make a distribution.",
                     refused.getExceptionMessage());
        assertThrows(NonProjectablePotentialException.ParametersAreNotADistribution.class,
                     () -> probabilityOfTheDisease(network(RangeFunction.class, 6, 2, 4, 12), observed(5)));
        assertThrows(NonProjectablePotentialException.ParametersAreNotADistribution.class,
                     () -> probabilityOfTheDisease(network(TriangularFunction.class, 0, 4, 5, 2, 10, 6), observed(3)));
    }

    /**
     * Commons Math refuses a beta whose alpha is not positive only at 0, and one whose beta is not positive only at 1;
     * elsewhere the finding was refused saying that the beta cannot tell how likely the value is (error 272 of the
     * pending list).
     */
    @Test void aBetaWithAParameterThatIsNotPositiveIsRefusedWhereverItIsSeen() {
        var refused = assertThrows(NonProjectablePotentialException.ParametersAreNotADistribution.class,
                                   () -> probabilityOfTheDisease(network(BetaFunction.class, 0, 2, 5, 1), observed(0.5)));
        assertEquals("The finding on N cannot be used: with Disease = absent, the parameters of its Beta "
                             + "distribution, alpha = 0 and beta = 2, do not make a distribution.",
                     refused.getExceptionMessage());
        assertThrows(NonProjectablePotentialException.ParametersAreNotADistribution.class,
                     () -> probabilityOfTheDisease(network(BetaFunction.class, 5, 1, 2, -1), observed(0.5)));
    }

    /** A beta of parameter below 1 has an infinite density at 0, as the gamma above, and ends the same way. */
    @Test void aBetaSeenWhereItsDensityIsInfiniteEndsAsTheGamma() {
        var refused = assertThrows(NonProjectablePotentialException.DensityIsInfinite.class,
                                   () -> probabilityOfTheDisease(network(BetaFunction.class, 0.5, 0.5, 2, 2), observed(0)));
        assertEquals("The finding on N cannot be used: at 0, the density of its Beta distribution is infinite.",
                     refused.getExceptionMessage());
    }

    /** Elsewhere the same gamma has a finite density, and the finding is used. */
    @Test void aFindingWhereTheDensityIsFiniteIsUsed() throws Exception {
        ProbNet probNet = network(GammaFunction.class, 0.5, 1, 2, 1);
        double absent = 0.9 * Math.exp(-1) / Math.sqrt(Math.PI);
        double present = 0.1 * Math.exp(-1);
        assertArrayEquals(new double[] { absent / (absent + present), present / (absent + present) },
                          probabilityOfTheDisease(probNet, observed(1)), 1e-9);
    }

    /**
     * N has two parents, A (0.5 / 0.5) and B (0.7 / 0.3), and is 1, 2, 3 or 4 in (a0, b0), (a1, b0), (a0, b1) and
     * (a1, b1). The configurations of the parents used to be walked wrongly from the second parent on: (a0, b1) was
     * never asked and took the value of (a1, b1), so N had no value 3 (error 267 of the pending list).
     */
    private ProbNet twoParents() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable a = new Variable("A", "a0", "a1");
        Variable b = new Variable("B", "b0", "b1");
        n = new Variable("N", false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0.01);
        Node aNode = probNet.addNode(a, NodeType.CHANCE);
        Node bNode = probNet.addNode(b, NodeType.CHANCE);
        Node nNode = probNet.addNode(n, NodeType.CHANCE);
        probNet.addLink(aNode, nNode, true);
        probNet.addLink(bNode, nNode, true);
        aNode.setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5, 0.5 }));
        bNode.setPotential(new TablePotential(List.of(b), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.7, 0.3 }));
        UnivariateDistrPotential relation = new UnivariateDistrPotential(List.of(n, a, b), ExactFunction.class,
                                                                         PotentialRole.CONDITIONAL_PROBABILITY);
        relation.setParameters(new double[] { 1, 2, 3, 4 });
        nNode.setPotential(relation);
        return probNet;
    }

    private double[] probabilityOfA(ProbNet probNet, EvidenceCase evidence) throws Exception {
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(probNet.getVariable("A")));
        propagation.setPreResolutionEvidence(evidence);
        return propagation.getPosteriorValues().get(probNet.getVariable("A")).getValues();
    }

    @Test void withTwoParentsEveryConfigurationGivesItsValue() throws Exception {
        ProbNet converted = ProbNetOperations.convertNumericalVariablesToFS(twoParents(), new EvidenceCase());
        Variable convertedN = converted.getVariable("N");
        assertEquals(List.of("1.0", "2.0", "3.0", "4.0"),
                     Arrays.stream(convertedN.getStates()).map(State::getName).map(Double::parseDouble).map(String::valueOf).toList());
        // The value of N in (a0, b1), the third configuration, is 3
        double[] table = ((TablePotential) converted.getNode(convertedN).getPotentials().getFirst()).getValues();
        assertArrayEquals(new double[] { 0, 0, 1, 0 }, Arrays.copyOfRange(table, 8, 12), 1e-12);
    }

    @Test void withTwoParentsEachValueTellsWhichParentsGaveIt() throws Exception {
        assertArrayEquals(new double[] { 1, 0 }, probabilityOfA(twoParents(), observed(3)), 1e-12);
        assertArrayEquals(new double[] { 0, 1 }, probabilityOfA(twoParents(), observed(4)), 1e-12);
    }

    /** A Dirichlet is a distribution of probabilities: it cannot say how likely a value of N is. */
    @Test void aDistributionWithoutDensityIsRefusedWithAMessage() {
        ProbNet probNet = network(DirichletFunction.class, 1, 1);
        assertThrows(NonProjectablePotentialException.DensityCannotBeCalculated.class,
                     () -> ProbNetOperations.convertNumericalVariablesToFS(probNet, observed(8)));
    }
}
