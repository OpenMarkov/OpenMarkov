package org.openmarkov.stochasticPropagationOutput;

import org.junit.jupiter.api.*;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation;
import org.openmarkov.inference.algorithm.huginPropagation.HuginPropagation;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LikelihoodWeighting;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LogicSampling;
import org.openmarkov.inference.algorithm.likelihoodWeighting.StochasticPropagation;

import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class StorageTest {

    private double maxError; // Maximum error allowed when comparing to exact result, here if assertions are added.

    private StochasticPropagation algorithm; // A place to put the algorithm instance that tests need
    public enum AlgorithmName {LIKELIHOOD_WEIGHTING, LOGIC_SAMPLING} // Possible algorithms

    private ProbNet probNet;  // Mock dialog to anchor the writing


    /*
     * Sets a five-node net as default, a max error for stochastic deviation.
     * Create a dummy dialog to anchor the xlsx writer.
     */
    @BeforeEach
    public void setUp() {
        maxError = 2E-2;
        probNet = ExampleNets.bigNet();
    }

    /**
     * It tests the availability of the data needed to print a xlsx file. In particular the samples from the
     * stochastic algorithm and the posterior values of clustering as an exact algorithm. The test only tries
     * to get, it doesn't check the values.
     *
     * Net: ExamplesNets.bigNet()
     *
     * Algorithms:
     * - Likelihood Weighting
     * - Logic Sampling
     *
     * Evidence cases:
     * - 0 findings
     * - 1 finding
     * - 2 findings
     * - 3 findings
     *
     *
     * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
     * @throws IncompatibleEvidenceException when evidence can't be propagated by the algorithm.
     */
    @Tag(TestSpeed.SLOW)
    @Test
    public void testGetDataToPrint()
            throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException, CannotNormalizePotentialException {

        AlgorithmName algorithmName;
        for (int i = 0; i < 2; i++) {
            for (int numberOfFindings = 0; numberOfFindings < 4; numberOfFindings++) {

                // Algorithm to use
                algorithmName = AlgorithmName.values()[i];
                roundByBars(algorithmName.name());


                // Get the variable list to add findings
                List<Variable> variables = probNet.getVariables();

                EvidenceCase evidence;

                switch (numberOfFindings) {

                    // No findings
                    default:
                        evidence = new EvidenceCase();
                        break;

                    // One finding: A = A1
                    case 1:
                        evidence = new EvidenceCase();
                        evidence.addFinding(new Finding(variables.get(0), 1));
                        break;

                    // Two findings: C = C0, D = D0
                    case 2:
                        evidence = new EvidenceCase();
                        evidence.addFinding(new Finding(variables.get(2), 0));
                        evidence.addFinding(new Finding(variables.get(3), 0));
                        break;

                    // Three findings: B = B1, C = C1, E = E0
                    case 3:
                        evidence = new EvidenceCase();
                        evidence.addFinding(new Finding(variables.get(1), 1));
                        evidence.addFinding(new Finding(variables.get(2), 1));
                        evidence.addFinding(new Finding(variables.get(4), 0));
                        break;
                }

                // Propagation
                algorithm = returnChosenAlgorithm(probNet, algorithmName);
                algorithm.setPostResolutionEvidence(evidence);

                // Use a small sample (because it will be printed) and activate the storing
                algorithm.setSampleSize(100);
                algorithm.setStoringSamples(true);

                HashMap<Variable, TablePotential> results = algorithm.getPosteriorValues();
                printResults(results);


                // Printing
                System.out.println(algorithm.getVariablesToSample());
                matrixPrettyPrint(algorithm.getSamples());

                // Prepare exact algorithm
                ClusterPropagation exactAlgorithm = new HuginPropagation(probNet);
                exactAlgorithm.setStorageLevel(ClusterPropagation.StorageLevel.MEDIUM);
                exactAlgorithm.compilePriorPotentials();
                exactAlgorithm.setPreResolutionEvidence(evidence);
                HashMap<Variable,TablePotential> exactPosteriorValues = exactAlgorithm.getPosteriorValues();

            }


        }
    }

    /**
     * This subroutine applies the algorithm named {@code name} to a {@code net}.
     * Allowing the looping of algorithms through an enum.
     *
     * @param net The net suffering the algorithm.
     * @param name The name of the algorithm.
     * @return The algorithm instance.
     *
     * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
     */
    private StochasticPropagation returnChosenAlgorithm(ProbNet net, AlgorithmName name) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        switch (name) {
            case LIKELIHOOD_WEIGHTING:
                return new LikelihoodWeighting(net);
            case LOGIC_SAMPLING:
                return new LogicSampling(net);
            default:
                return new LikelihoodWeighting(net);

        }
    }

    /*
     * Auxiliary method that prints nicely a matrix of doubles to standard output.
     */
    private static void matrixPrettyPrint(double[][] matrix){
        int rows, columns; // size of the matrix in column and rows
        rows = matrix.length;
        columns = matrix[0].length;

        System.out.println();
        for (int row = 0; row < rows; row++) {
            System.out.print("  |");
            for (int column = 0; column < columns; column++) {
                // print each element formated to create pretty columns
                if (column == 0) {
                    System.out.printf("%4.2f", matrix[row][column]);
                } else {
                    System.out.printf("%5.2f", matrix[row][column]);
                }
            }
            System.out.println("  |"); // pretty matrix bounds
        }
        System.out.println();
    } // end matrixPrettyPrint

    private void printResults (HashMap < Variable, TablePotential > results){

        // Nested class to compare variable's names and order them
        class VariableNameOrder implements Comparator<Variable> {
            @Override public int compare(Variable v1, Variable v2) {
                String s1 = v1.getName().toLowerCase(); // Convert to lower case.
                String s2 = v2.getName().toLowerCase();
                return s1.compareTo(s2); // Compare lower-case Strings.
            }
        }

        // Ordering
        TreeMap<Variable, TablePotential> orderedLwInferenceResult =
                new TreeMap<>(new VariableNameOrder());
        orderedLwInferenceResult.putAll(results);

        // Printing
        for (Map.Entry<Variable, TablePotential> node : orderedLwInferenceResult.entrySet()) {
            
            String key = node.getKey().getName();
            String value = node.getValue().toString();
            System.out.println(key + " " + value);

            // If likelihoodWeighting or LogicSampling, print more data

            System.out.println(algorithm.getNumPositiveSamples());
            System.out.println(algorithm.getPositiveSampleRatio());

        }
        System.out.println();
    }

    /*
     * Prints a "string" like:
     * ----------
     * - string -
     * ----------
     */
    private void roundByBars(String string) {

        // Top bar
        for (int i = 0; i < string.length() + 3; i++) {
            System.out.print("-");
        }
        System.out.println("-");

        // String
        System.out.println("- " + string + " -");

        // Bottom bar
        for (int i = 0; i < string.length() + 3; i++) {
            System.out.print("-");
        }
        System.out.println("-");
    }

}
