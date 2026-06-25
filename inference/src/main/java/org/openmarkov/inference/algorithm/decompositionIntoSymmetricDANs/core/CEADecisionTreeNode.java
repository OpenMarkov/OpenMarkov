package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * This class represents a node of the decision tree used in the evaluation of a DAN with CEPs. It extends the class
 * DecisionTreeNode, and it is used to store the CEPs resulting from the evaluation of the branches of the decision tree.
 *
 * @author Manuel Arias
 *
 */
public class CEADecisionTreeNode extends DecisionTreeNode<CEP> {
    
    // Constructors
    public CEADecisionTreeNode(Node node) {
        super(node);
        // TODO Auto-generated constructor stub
    }
    
    public CEADecisionTreeNode(Node node, ProbNet dan) {
        super(node, dan);
    }
    
    public CEADecisionTreeNode(Variable variable, ProbNet dan) {
        super(variable, dan);
    }
    
    @Override
    public boolean isBestDecision(DecisionTreeBranch<CEP> treeBranch) {
        // In cost-effectiveness analysis there is no single "best" decision:
        // the optimal choice depends on the willingness-to-pay threshold.
        return false;
    }
    
    @Override
    public void setOnlyValueForUtility(Potential tablePotential) {
        setUtility(DANOperations.getOnlyValuePotentialCEP((GTablePotential) tablePotential));
    }

}
