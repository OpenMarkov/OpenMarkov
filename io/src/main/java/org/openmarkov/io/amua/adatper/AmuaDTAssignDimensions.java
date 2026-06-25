package org.openmarkov.io.amua.adatper;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.io.amua.model.AmuaDTCENode;
import org.openmarkov.io.amua.model.AmuaDTNode;
import org.openmarkov.io.amua.model.AmuaModel;
import org.openmarkov.io.amua.model.AmuaDTDimensions;
import org.openmarkov.io.amua.model.AmuaDimensionInfo;
import static org.openmarkov.io.amua.model.AmuaConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AmuaDTAssignDimensions {

    /**
     * Creates an AmuaDTDimensions instance based on the provided criteria, tree type, and root node.
     *
     * @param criteria list of decision criteria associated with the tree
     * @param amuaModel type of AMUA tree (UNICRITERIA or COST_EFFECTIVENESS)
     * @param tree root node of the decision tree
     * @return fully populated AmuaDTDimensions instance
     * @throws IllegalStateException if required criteria are missing or type is unsupported
     */
    public AmuaDTDimensions assignDimensions(List<Criterion> criteria, AmuaModel amuaModel, AmuaDTNode<?> tree) {

        Objects.requireNonNull(tree, "tree cannot be null");

        List<AmuaDimensionInfo> dimensions = new ArrayList<>();
        int analysisType;
        int objective;
        int costDim = -1;
        int effectDim = -1;
        int objectiveDim = 0;
        int extendedDim = 0;

        switch (amuaModel) {

            case COST_EFFECTIVENESS_DT:
                for (int i = 0; i < criteria.size(); i++) {
                    Criterion c = criteria.get(i);
                    dimensions.add(new AmuaDimensionInfo(c.getCriterionName(), c.getCriterionUnit(), DEFAULT_DECIMALS));

                    if (c.getCECriterion() == Criterion.CECriterion.Cost) {
                        costDim = i;
                    } else if (c.getCECriterion() == Criterion.CECriterion.Effectiveness) {
                        effectDim = i;
                    }
                }

                if (costDim == -1 || effectDim == -1) {
                    throw new IllegalStateException("CEA requires one Cost and one Effectiveness criterion.");
                }

                analysisType = ANALYSIS_TYPE_CEA;
                objective = OBJECTIVE_MAXIMIZE;
                break;

            case UNICRITERIA_DT:
                dimensions.add(new AmuaDimensionInfo("Utility", "u", DEFAULT_DECIMALS));
                analysisType = ANALYSIS_TYPE_EV;
                objective = OBJECTIVE_MAXIMIZE;
                costDim = 0;
                effectDim = 0;
                break;

            default:
                throw new IllegalStateException("Unsupported Amua Model: " + amuaModel);
        }

        String baseScenario = (amuaModel == AmuaModel.COST_EFFECTIVENESS_DT) ? calcBaseScenario(amuaModel, tree) : null;
        double WTP = calcWTP(amuaModel, criteria);

        return new AmuaDTDimensions(
                dimensions,
                analysisType,
                objective,
                objectiveDim,
                costDim,
                effectDim,
                baseScenario,
                WTP,
                extendedDim
        );
    }


    /**
     * Retrieves the Willingness-To-Pay (WTP) associated with
     * the current decision tree configuration.
     *
     * @return the WTP value if defined for COST_EFFECTIVENESS; otherwise 0.
     *
     * @throws IllegalStateException if the AmuaDTType is unsupported.
     */
    private static double calcWTP(AmuaModel amuaModel, List<Criterion> criteria) {
        switch (amuaModel) { // only works with Cost-Effectiveness Tree
            case COST_EFFECTIVENESS_DT:
                for (Criterion criterion : criteria) {
                    if (criterion.getCECriterion() == Criterion.CECriterion.Effectiveness) {
                        return criterion.getUnicriterizationScale();
                    }
                }
            case UNICRITERIA_DT:
                return 0;
            default:
                throw new IllegalStateException("Unsupported Amua Model: " + amuaModel);
        }
    }


    /**
     * Selects the base scenario for which the analysis will be performed
     *
     * @return the name of the base scenario.
     * @throws IllegalStateException if no decision node is found or if the AmuaDTType is unsupported.
     */
    private static String calcBaseScenario(AmuaModel amuaModel, AmuaDTNode<?> tree) {
        switch(amuaModel) { // only works with Cost-Effectiveness Tree
            case COST_EFFECTIVENESS_DT:
                double bestCost = Double.POSITIVE_INFINITY;
                double bestEffectiveness = Double.NEGATIVE_INFINITY;
                AmuaDTCENode decisionNode = (AmuaDTCENode) getDecisionNode(tree);
                AmuaDTCENode bestScenario = null;

                // iterate over the children of the decision node
                for (AmuaDTNode<?> amuaChildNode : decisionNode.getChildNodes()){
                    AmuaDTCENode ceNode = (AmuaDTCENode) amuaChildNode;
                    double cost = ceNode.getPartialUtility().getCost();
                    double effectiveness = ceNode.getPartialUtility().getEffectiveness();
                    // lowest cost, and in case of a tie, highest effectiveness.
                    if (cost < bestCost || (cost == bestCost && effectiveness > bestEffectiveness)) {
                        bestScenario  = ceNode;
                        bestCost = cost;
                        bestEffectiveness = effectiveness;
                    }
                }

                if (bestScenario == null) {throw new IllegalArgumentException("Base Scenario was not found");}
                return bestScenario.getName(); // return baseScenario

            case UNICRITERIA_DT:
                return null;

            default:
                throw new IllegalStateException("Unsupported Amua model:: " + amuaModel);
        }
    }


    /**
     * Get the decision node (type 0) from the tree whose root is {@code amuaRootNode}.
     *
     * @return the decision node found
     * @throws IllegalStateException if no decision node is found in the tree
     */
    private static AmuaDTNode<?> getDecisionNode(AmuaDTNode<?> node){
        AmuaDTNode<?> decisionNode = null;
        if (node.getType()==0){
            decisionNode = node;
        } else {
            for (AmuaDTNode<?> childNode : node.getChildNodes()) {
                if (childNode.getType() == 0) {
                    decisionNode = childNode;
                    break;
                }
            }
        }
        if (decisionNode == null) {throw new IllegalStateException("Decision node not found");}
        return decisionNode;
    }
}