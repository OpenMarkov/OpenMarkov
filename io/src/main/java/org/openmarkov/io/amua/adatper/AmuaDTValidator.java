package org.openmarkov.io.amua.adatper;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;
import org.openmarkov.io.amua.model.AmuaModel;

import java.util.EnumSet;
import java.util.List;

/**
 * Validates an OpenMarkov decision tree for AMUA export.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaDTValidator {

    private final List<Criterion> criteria;
    private int decisionNodeCount = 0;

    public AmuaDTValidator(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    /**
     * Determines the AMUA decision tree type.
     *
     * @throws IllegalStateException if the decision tree type is not supported
     */
    public AmuaModel determineAmuaDTType(DecisionTreeNode<?> treeNode) {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalStateException("The tree has no defined criteria.");
        }

        boolean hasCE = hasCECriteria();

        IllegalStateException ceError = null;

        // Try COST_EFFECTIVENESS
        if (hasCE) {
            try {
                decisionNodeCount = 0;
                hasValidStructure(treeNode, 1, true);
                return AmuaModel.COST_EFFECTIVENESS_DT;
            } catch (IllegalStateException e) {
                if (isCriticalError(e)) { // critical error
                    throw e;
                }
                ceError = e; // non-critical error
            }
        }

        // Try UNICRITERIA
        // An inferred unicriteria tree may internally include cost-effectiveness criteria
        // (hasCECriteria == true), even if they are not explicitly displayed.
        if (criteria.size() == 1 || hasCE) {
            try {
                decisionNodeCount = 0;
                hasValidStructure(treeNode, 1, false);
                return AmuaModel.UNICRITERIA_DT;
            } catch (IllegalStateException e) {
                if (isCriticalError(e)) {
                    throw e;
                }
                if (ceError != null && (treeNode instanceof CEADecisionTreeNode)){
                    throw ceError;
                }
                throw e;
            }
        }

        throw new IllegalStateException("Tree type not supported by Amua.");
    }


    /**
     * Performs DFS traversal to validate the tree structure.
     *
     * @param node current node.
     * @param maxDecisionNodes maximum allowed decision nodes.
     * @param isCE whether CE structure is expected.
     * @throws IllegalStateException if the subtree is invalid.
     */
    private void hasValidStructure(DecisionTreeNode<?> node, int maxDecisionNodes, boolean isCE){
        isValidNode(node, maxDecisionNodes, isCE);
        for (DecisionTreeElement child : node.getChildren()) {
            if (child instanceof DecisionTreeBranch branch) {
                hasValidStructure(branch.getChild(), maxDecisionNodes, isCE);
            }
        }
    }


    /**
     * Checks if a single node is valid in Amua.
     *
     * @param node current node.
     * @param maxDecisionNodes maximum allowed decision nodes.
     * @param isCE whether CE structure is expected.
     * @throws IllegalStateException if the node is invalid.
     */
    private void isValidNode(DecisionTreeNode<?> node, int maxDecisionNodes, boolean isCE) {
        // validate node class type
        if (isCE && !(node instanceof CEADecisionTreeNode)) {
            throw new IllegalStateException("Invalid node type for Cost-Effectiveness tree.");
        }

        if (!isCE && !(node instanceof EvaluationDecisionTreeNode)) {
            throw new IllegalStateException("Invalid node type for Unicriteria tree.");
        }

        NodeType type = node.getNodeType();

        if (type != NodeType.UTILITY && type != NodeType.CHANCE && type != NodeType.DECISION) {
            throw new IllegalStateException("Unsupported node type: " + type);
        }

        if (type == NodeType.DECISION) {
            decisionNodeCount++;
            if (decisionNodeCount > maxDecisionNodes) {
                throw new IllegalStateException("Amua supports only one decision node.");
            }
        }
    }


    /**
     * Checks whether the criteria correspond to a Cost-Effectiveness (CEA) analysis.
     *
     * @return true if there are exactly two criteria: Cost and Effectiveness
     */
    private boolean hasCECriteria(){
        if (criteria.size() != 2) return false;
        EnumSet<Criterion.CECriterion> types = EnumSet.noneOf(Criterion.CECriterion.class);
        for (Criterion c : criteria) types.add(c.getCECriterion());
        return types.contains(Criterion.CECriterion.Cost) && types.contains(Criterion.CECriterion.Effectiveness);
    }


    /**
     * Determines whether an IllegalStateException is considered a critical error
     *
     * @return true if the exception represents a critical error that should not be suppressed or deferred; false otherwise
     */
    private boolean isCriticalError(IllegalStateException e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("Unsupported node type") || msg.contains("Amua supports only one decision node."));
    }

}
