package org.openmarkov.learning.metric.cmi.util;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Utility class for class-conditioned metrics, providing access to root/non-root nodes
 * and helper methods for frequency computations and array operations.
 */
public class MetricUtils {

    private ProbNet probNet;
    private String classVariable;

    /**
     * Constructs a MetricUtils for the given network and class variable name.
     *
     * @param probNet the probabilistic network
     * @param name    the name of the class variable
     */
    public MetricUtils(ProbNet probNet, String name){
        this.probNet=probNet;
        this.classVariable=name;

    }

    /**
     * Returns all variables except the class variable.
     *
     * @return list of non-root (non-class) variables
     */
    public List<Variable> getNonRootVariables(){
        return getNonRootNodes().stream().map(Node::getVariable).collect(Collectors.toList());
    }

    /**
     * Returns all nodes except the class variable node.
     *
     * @return list of non-root (non-class) nodes
     */
    public List<Node> getNonRootNodes(){
        return probNet.getNodes().stream()
                .filter(n-> !n.getName().equalsIgnoreCase(classVariable))
                .collect(Collectors.toList());
    }

    /**
     * Returns the node corresponding to the class variable.
     *
     * @return the root (class) node
     */
    public Node getRootNode(){
        return probNet.getNodes().stream()
                .filter(n -> n.getName().equalsIgnoreCase(classVariable))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Class variable '" + classVariable + "' not found in network"));
    }

    /**
     * Sums elements of an array from index {@code b} to {@code e} (exclusive), stepping by {@code rate}.
     *
     * @param freq the array of values
     * @param b    the starting index (inclusive)
     * @param e    the ending index (exclusive)
     * @param rate the step size between indices
     * @return the sum of selected elements
     */
    public static double sumArray(double[] freq, int b, int e, int rate){
        double result = 0;
        while (b < e) {
            result+= freq[b];
            b+=rate;
        }
        return result;
    }


    /**
     * Returns the index of the maximum value in the given array.
     *
     * @param probs the array of values
     * @return the index of the maximum value
     */
    public static int getIndexOfMaxValue(double[] probs){
        int hProbClass=0;
        double max=0.0;

        for(int i=0; i< probs.length; i++){
            if(probs[i]>max){
                max=probs[i];
                hProbClass=i;
            }
        }

        return hProbClass;
    }


    /**
     * Calculates absolute frequencies for a node and its parent variables using the given case subset.
     *
     * @param probNet      the probabilistic network
     * @param caseDatabase the full case database (used for variable index lookup)
     * @param node         the node whose frequencies to compute
     * @param variables    the node variable followed by parent variables
     * @param cases        the subset of cases to use for computing frequencies
     * @return a {@code TablePotential} with the absolute frequencies
     */
    public static TablePotential getAbsoluteFrequencies(ProbNet probNet, CaseDatabase caseDatabase, Node node,
                                                 List<Variable> variables, int[][] cases) {
        int parentsConfigurations = 1;
        int numValues = node.getVariable().getNumStates();
        // We miss the first one as it is the node itself, not one of its parents
        int[] indexesOfParents = new int[variables.size() - 1];
        for (int i = 0; i < indexesOfParents.length; ++i) {
            indexesOfParents[i] = caseDatabase.getVariables().indexOf(variables.get(i + 1));
            parentsConfigurations *= variables.get(i + 1).getNumStates();
        }

        TablePotential absoluteFreqPotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] absoluteFreqs = absoluteFreqPotential.getValues();
        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++) {
            absoluteFreqs[i] = 0;
        }
        variables.remove(0);
        // Compute the absolute frequencies
        int iCPT;
        int iParent, iNode = caseDatabase.getVariables().indexOf(node.getVariable());
        List<Node> nodes = probNet.getNodes(variables);
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            // Iterate parents in reverse so the last parent gets the largest
            // multiplier, matching TablePotential's offset convention.
            for (int j = nodes.size() - 1; j >= 0; --j) {
                iParent = indexesOfParents[j];
                iCPT = iCPT * nodes.get(j).getVariable().getNumStates() + cases[i][iParent];
            }
            if (numValues * iCPT + cases[i][iNode] >= absoluteFreqs.length) {
                throw new IndexOutOfBoundsException(
                        "Frequency index out of bounds: " + (numValues * iCPT + cases[i][iNode])
                                + " >= " + absoluteFreqs.length + " for node " + node.getName());
            }

            absoluteFreqs[numValues * iCPT + cases[i][iNode]]++;
        }
        return absoluteFreqPotential;
    }

    /**
     * Builds a three-way cross-tabulation of absolute frequencies for nodes X, Y, and root (class).
     *
     * @param probNet      the probabilistic network
     * @param caseDatabase the full case database (used for variable index lookup)
     * @param nodeX        the first feature node
     * @param nodeY        the second feature node
     * @param root         the class (root) node
     * @param cases        the subset of cases to use
     * @return a {@code TablePotential} with the cross-tabulated absolute frequencies
     */
    public static TablePotential buildAbsoluteFreqCrossTab(ProbNet probNet, CaseDatabase caseDatabase, Node nodeX,
                                                     Node nodeY, Node root, int[][] cases) {
        int parentsConfigurations = 1;
        int numValues = nodeX.getVariable().getNumStates();
        // We miss the first one as it is the node itself, not one of its parents
        int[] indexesOfParents = new int[2];
        List<Variable> variables = new ArrayList<>(Arrays.asList(nodeX.getVariable(), root.getVariable(), nodeY.getVariable()));
        TablePotential potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);

        for (int i = 0; i < indexesOfParents.length; ++i) {
            indexesOfParents[i] = caseDatabase.getVariables().indexOf(variables.get(i + 1));
            parentsConfigurations *= variables.get(i + 1).getNumStates();
        }

        double[] absoluteFreqs = potential.getValues();
        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++) {
            absoluteFreqs[i] = 0;
        }
        variables.remove(0);
        // Compute the absolute frequencies
        int iCPT;
        int iParent, iNode = caseDatabase.getVariables().indexOf(nodeX.getVariable());
        if (iNode == -1) {
            throw new IllegalArgumentException(
                    "Variable " + nodeX.getVariable().getName() + " not found in case database");
        }
        List<Node> nodes = probNet.getNodes(variables);
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            // Iterate parents in reverse to match TablePotential's offset convention.
            for (int j = nodes.size() - 1; j >= 0; --j) {
                iParent = indexesOfParents[j];
                iCPT = iCPT * nodes.get(j).getVariable().getNumStates() + cases[i][iParent];
            }
            absoluteFreqs[numValues * iCPT + cases[i][iNode]]++;
        }
        return potential;
    }

}
