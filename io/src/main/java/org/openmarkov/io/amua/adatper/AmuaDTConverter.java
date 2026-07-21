package org.openmarkov.io.amua.adatper;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;
import org.openmarkov.io.amua.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts OpenMarkov decision trees into Amua-compatible decision trees.
 * An Amua decision tree is a series of linked nodes.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaDTConverter {
    private final AmuaModel amuaModel;
    private int index;

    private final Map<AmuaDTNode<?>, Integer> childPositionCounter = new HashMap<>();
    private final Map<AmuaDTNode<?>, AmuaDTNode<?>> lastChildMap = new HashMap<>();

    /**
     * Creates a converter for the specified Amua tree type.
     * @param amuaModel the type of Amua decision tree
     */
    public AmuaDTConverter(AmuaModel amuaModel) {
        this.amuaModel = amuaModel;
    }


    /**
     * Converts an OpenMarkov decision tree into an Amua decision tree.
     *
     * @param root the root node of the OpenMarkov decision tree
     * @return the root of the converted Amua tree
     * @throws IllegalArgumentException if the root node is null
     */
    public AmuaDTNode<?> convertToAmuaTree(DecisionTreeNode<?> root) {
        if (root == null) {
            throw new IllegalArgumentException("Tree cannot be null.");
        }

        this.index = 0;
        return convertToAmuaTree(root, null, null, 0);
    }


    /**
     * Returns the type of the node in Amua format (int)
     *
     * @param nodeType the type of node in OpenMarkov format. It is transformed to Amua format
     * @return the integer representation of the node type (Amua format)
     * @throws IllegalArgumentException if the node type is not recognized by Amua.
     */
    public int typeNode(NodeType nodeType) {
        return switch (nodeType) {
            case DECISION -> 0;
            case CHANCE -> 1;
            case UTILITY -> 2;
            default -> throw new IllegalArgumentException("Unsupported node type: " + nodeType);
        };
    }


    /**
     * Recursively converts an OpenMarkov decision tree node into an Amua node.
     * @param decisionTreeNode the OpenMarkov node to convert
     * @param branch the branch from the parent node, or null if root
     * @param parent the parent Amua node, or null if root
     * @param level the depth level in the Amua tree
     * @return the corresponding Amua node
     */
    private AmuaDTNode<?> convertToAmuaTree(DecisionTreeNode<?> decisionTreeNode, DecisionTreeBranch branch, AmuaDTNode<?> parent, int level) {
        // collapsed, visible, hasVarUpdates: default values
        AmuaDTNode<?> amuaNode = createNodeInstance();

        amuaNode.setIndex(index++);
        amuaNode.setType(typeNode(decisionTreeNode.getNodeType()));
        amuaNode.setLevel(level);

        // Name
        amuaNode.setName(branch == null ? "Root" : branch.getBranchState().getName());

        // Probability
        if (branch == null || typeNode(branch.getParent().getNodeType()) == 0) {
            amuaNode.setProb(0);
        } else {
            double prob = branch.getBranchProbability();
            if (Double.isNaN(prob)) {
                prob = 0.0;
            }
            amuaNode.setProb(prob);
        }

        // Parent
        if (parent != null) {
            amuaNode.setParentNode(parent);
        }

        // position (xPos, yPos, parentX, parentY)
        setGraphicInformation(amuaNode);

        assignUtilities(amuaNode, decisionTreeNode);

        // recursive call
        List<AmuaDTNode<?>> childrenNodes = new ArrayList<>();
        for (DecisionTreeElement child : decisionTreeNode.getChildren()) {
            if (child instanceof DecisionTreeBranch childBranch) {
                DecisionTreeNode<?> childNode = childBranch.getChild();
                AmuaDTNode<?> amuaChildNode = convertToAmuaTree(childNode, childBranch, amuaNode,level + 1);
                childrenNodes.add(amuaChildNode);
            }
        }

        amuaNode.setChildNodes(childrenNodes);

        if (amuaNode.getType() == 1) { // sum of probability must be 1
            double sum = 0.0;
            for (AmuaDTNode<?> child : childrenNodes) {
                sum += child.getProbability();
            }
            if (Math.abs(sum - 1.0) > 1e-4) {
                int size = childrenNodes.size();
                double uniformProb = 1.0 / size;
                for (int i = size - 1; i >= 0; i--) {
                    childrenNodes.get(i).setProb(uniformProb);
                }
            }
        }

        return amuaNode;
    }


    /**
     * Factory method for Amua node creation.
     */
    private AmuaDTNode<?> createNodeInstance() {
        return switch (amuaModel) {
            case COST_EFFECTIVENESS_DT -> new AmuaDTCENode();
            case UNICRITERIA_DT -> new AmuaDTUnicriteriaNode();
            default -> throw new IllegalStateException("Unsupported Amua model: " + amuaModel + " during tree conversion.");
        };
    }


    /**
     * Assigns payoff and partial utility values to an Amua node.
     * @param amuaNode the Amua node to update
     * @param node the OpenMarkov node to read utility values from
     */
    private void assignUtilities(AmuaDTNode<?> amuaNode, DecisionTreeNode<?> node) {

        if (amuaNode instanceof AmuaDTCENode ceNode) {

            double cost = getCEACost(node);
            double eff = getCEAEffectiveness(node);

            AmuaCEvalue payoffValues = new AmuaCEvalue(
                amuaNode.getType() == 2 ? cost : 0,
                amuaNode.getType() == 2 ? eff : 0
            );

            AmuaCEvalue partialValues = new AmuaCEvalue(
                    amuaNode.getType() == 2 ? cost : 0,
                    amuaNode.getType() == 2 ? eff : 0
            );

            ceNode.setPayoff(payoffValues);
            ceNode.setPartialUtility(partialValues);

        } else {
            AmuaDTUnicriteriaNode unicriteriaNode = (AmuaDTUnicriteriaNode) amuaNode;

            double utility = getUnicriteriaUtility(node);

            unicriteriaNode.setPayoff(amuaNode.getType() == 2 ? utility : 0);
            unicriteriaNode.setPartialUtility(utility);
        }
    }

    /**
     * Assigns the x and y position, and parent connection coordinates for a node.
     * @param amuaNode the node whose position and parent coordinates are set
     */
    private void setGraphicInformation(AmuaDTNode<?> amuaNode) {
        // width, height: default values
        int margin = amuaNode.getMargin();
        int xOffset = 5 * margin;
        int yOffset = margin + amuaNode.getHeight()/2;

        AmuaDTNode<?> parent = amuaNode.getParentNode();
        if (parent == null) { // root position
            amuaNode.setXPos(margin);
            amuaNode.setYPos(margin);
            return;
        }

        // xPos
        amuaNode.setXPos(margin + amuaNode.getLevel() * xOffset);

        // yPos
        int index = getNodeIndex(parent);
        if (index == 0) {
            amuaNode.setYPos(parent.getYPos()); // first child aligns with parent
        } else {
            AmuaDTNode<?> previousSibling = lastChildMap.get(parent);
            int lastYPos = getMaxYPos(previousSibling);
            amuaNode.setYPos(lastYPos + yOffset);
        }

        lastChildMap.put(parent, amuaNode);

        // parentX
        amuaNode.setParentX(parent.getXPos() + parent.getWidth());

        // parentY
        amuaNode.setParentY(parent.getYPos() + parent.getHeight()/2);
    }


    /**
     * Calculates the position index of a node among its siblings.
     *
     * @param parent the parent node of the current node
     * @return the zero-based index of the node among the parent's children
     */
    private int getNodeIndex(AmuaDTNode<?> parent) {
        if (parent == null) return 0;
        int index = childPositionCounter.getOrDefault(parent, 0);
        childPositionCounter.put(parent, index + 1);
        return index;
    }


    /**
     * Computes the maximum Y position in the subtree rooted at the
     * specified node.
     *
     * @param node the root node of the subtree to inspect
     * @return the maximum Y coordinate found in the subtree
     */
    private int getMaxYPos(AmuaDTNode<?> node) {
        int maxY = node.getYPos(); // inicia con el nodo actual
        for (AmuaDTNode<?> child : node.getChildNodes()) {
            maxY = Math.max(maxY, getMaxYPos(child)); // recursivamente verifica hijos
        }
        return maxY;
    }


    /**
     * Retrieves the cost component of a Cost-Effectiveness decision tree
     *
     * @param node the decision tree node whose cost will be obtained.
     * @return the cost value associated with the node
     */
    private double getCEACost(DecisionTreeNode<?> node) {
        CEP cep = getCEAUtility(node);
        return (cep != null) ? cep.getCost(0) : 0.0;
    }


    /**
     * Retrieves the effectiveness component of a Cost-Effectiveness decision tree
     *
     * @param node the decision tree node whose effectiveness will be obtained.
     * @return the effectiveness value associated with the node
     */
    private double getCEAEffectiveness(DecisionTreeNode<?> node) {
        CEP cep = getCEAUtility(node);
        return (cep != null) ? cep.getEffectiveness(0) : 0.0;
    }


    /**
     * Retrieves the cost-effectivenes utility (CEP) associated with a given node
     * of a Cost-Effectiveness decision tree
     *
     * @param node the decision tree node whose utility will be obtained.
     * @return the CEP object containing cost and effectiveness values.
     * @throws IllegalArgumentException if the tree type is not COST_EFFECTIVENESS
     */
    private CEP getCEAUtility(DecisionTreeNode<?> node) {
        if (amuaModel != AmuaModel.COST_EFFECTIVENESS_DT) {
            throw new IllegalArgumentException("Invalid tree type for CEA utility.");
        }
        if (!(node instanceof CEADecisionTreeNode ceaNode)) {
            return null;
        }
        return ceaNode.getUtility();
    }


    /**
     * Retrieves the utility of a unicriteria node.
     * @param node the OpenMarkov evaluation node
     * @return the utility value
     * @throws IllegalArgumentException if the tree type is not UNICRITERIA
     */
    private double getUnicriteriaUtility(DecisionTreeNode<?> node) {
        if (amuaModel != AmuaModel.UNICRITERIA_DT) {
            throw new IllegalArgumentException("Invalid tree type for unicriteria utility.");
        }

        Double utility = ((EvaluationDecisionTreeNode) node).getUtility();
        return (utility != null) ? utility : 0.0;
    }
}
