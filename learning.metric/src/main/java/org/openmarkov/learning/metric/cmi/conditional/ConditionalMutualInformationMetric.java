package org.openmarkov.learning.metric.cmi.conditional;

import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.metric.cache.Cache;
import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.metric.cmi.mutualInformation.MutualInformationMetric;
import org.openmarkov.learning.metric.cmi.util.MetricUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


@MetricType(name = "ConditionalMutualInformation", classConditionedMetric = true)
public class ConditionalMutualInformationMetric extends MutualInformationMetric {

    /**
     * Computes the Conditional Mutual Information metric
     * @param tablePotential the table potential
     * @return the result
     */
    @Override public double score(TablePotential tablePotential) {

        int xNumStates = tablePotential.getVariable(0).getNumStates();
        int classNumStates = tablePotential.getVariable(1).getNumStates();
        int yNumStates = tablePotential.getVariable(2).getNumStates();
        double caseCount = caseDatabase.getNumCases();
        int crossedFeatures = xNumStates * yNumStates;

        double[] freq = tablePotential.getValues();
        double cmi = 0.0;


        for (int k =0; k < classNumStates; k++){
            double pZ = MetricUtils.sumArray(freq, k*crossedFeatures, crossedFeatures*(1+k), 1)/ caseCount;

            for(int j=0; j < yNumStates; j++){
                int yOffset = j*xNumStates+k*crossedFeatures;
                double pYZ = MetricUtils.sumArray(freq, yOffset, yOffset+xNumStates, 1) / caseCount;

                for (int i =0; i < xNumStates; i++){
                    double pXZ = MetricUtils.sumArray(freq, i+k*crossedFeatures, (1+k)*crossedFeatures, xNumStates) / caseCount;
                    double pXYZ = freq[i+j*xNumStates+ k* crossedFeatures] / caseCount;
                    cmi+= (pXZ*pYZ>0 && pXYZ*pZ>0)?pXYZ * Math.log((pXYZ*pZ)/(pXZ*pYZ)):0;
                 }
            }

        }
        return cmi;
    }

    @Override protected void initCache() {
        this.cache = new Cache();
        utils = new MetricUtils(probNet, classVariable);
        cachedNodeScores = new HashMap<String, Double>();
        cache.flush(probNet);
        cachedScore = 0;
        
        
        getNonRootNodes().forEach(node -> {
            BaseLinkEdit edit = new AddLinkEdit(probNet, getRootNode().getVariable(), node.getVariable(), true);
            cache.cacheScore(edit, this.scoreEdit(edit, false));
            cachedNodeScores.put(node.getName(), 0.0);
        });
        
        
        getNonRootVariables().forEach(tail -> {
            getNonRootVariables().stream().filter(v -> v != tail).toList().forEach(head -> {
                BaseLinkEdit edit = (!probNet.getNode(head).isParent(probNet.getNode(tail)))?
                        new AddLinkEdit(probNet, tail, head, true): new RemoveLinkEdit(probNet, tail, head, true);
                cache.cacheScore(edit, (scoreEdit(edit, false)));
            });
        });

    }
    /**
     * Scores the associated network with the link given in the received edition added. We only have to recalculate the score
     * of the destination node.
     *
     * @param edition {@code BaseLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is definitive (UndoableEditHappened called this method) or not.
     * @return {@code double} score of the net with the given edition
     */
    @Override protected double scoreEdit(BaseLinkEdit edition, boolean change) {
        return this.score(buildAbsoluteFreqCrossTab(probNet, caseDatabase,
                                                    probNet.getNode(edition.getVariableFrom()), probNet.getNode(edition.getVariableTo()), getRootNode()));
    }


    @Override protected double score(AddLinkEdit edition, boolean change) {
        return scoreEdit(edition, change);
    }

    @Override protected double score(RemoveLinkEdit edition, boolean change) {
        return scoreEdit(edition, change);
    }
    
    
    private static TablePotential buildAbsoluteFreqCrossTab(ProbNet probNet, CaseDatabase caseDatabase, Node nodeX,
                                                            Node nodeY, Node root) {
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
        int[][] cases = caseDatabase.getCases();
        List<Node> nodes = probNet.getNodes(variables);
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            for (int j = 0; j < nodes.size(); ++j) {
                iParent = indexesOfParents[j];
                iCPT = iCPT * nodes.get(j).getVariable().getNumStates() + cases[i][iParent];
            }
            absoluteFreqs[numValues * iCPT + cases[i][iNode]]++;
        }
        return potential;
    }


}
