/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.validator;

import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.BinomialPotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.exception.CannotInvertUndirectedLinks;
import org.openmarkov.gui.exception.LinkInversionRequiresChanceVariablesWithPotential;

import java.util.List;

/******
 * This class validates if a link can be inverted arc-reversal style
 *
 * @author iagoparís - summer
 * @author Manuel Arias
 */
public class LinkInversionWithPotentialsUpdateValidator {
    
    /**
     * A link can be inverted when two conditions are met:<ol>
     * <li>Each potential attached to its two nodes is a TablePotential or can be projected to it.</li>
     * <li>The new links created do not create a cycle.</li>
     * </ol>
     *
     */
    public static void validate(Link<Node> link) throws LinkInversionRequiresChanceVariablesWithPotential, ConstraintViolatedException, CannotInvertUndirectedLinks {
        if (!link.isDirected()) {
            throw new CannotInvertUndirectedLinks(link);
        }
        Node node1 = link.getFrom();
        Node node2 = link.getTo();
        LinkInversionWithPotentialsUpdateValidator.validNode(node1);
        LinkInversionWithPotentialsUpdateValidator.validNode(node2);
        LinkInversionWithPotentialsUpdateValidator.validNewLinks(node1, node2);
        
        /*Iago code on this method is:
        boolean validPotentials;

		try {
			Potential potential1 = link.getNode1().getPotentials().get(0);
			Potential potential2 = link.getNode2().getPotentials().get(0);
			validPotentials = validatePotential(potential1) && validatePotential(potential2);
		} catch (IndexOutOfBoundsException ex) {
			// This exception is thrown when one of the involved nodes is decision.
			return false; // Arc reversal is only applicable if both nodes are chance.
		}

		// 1. The link must be directed
		// 2. The potential must be convertible to TablePotential
		return (link.isDirected() && validPotentials);
         */
    }
    
    private static void validNewLinks(Node node1, Node node2) throws ConstraintViolatedException {
        new InvertLinkEdit(node1.getProbNet(), node1.getVariable(), node2.getVariable(), true).tryConstraintsWillBeMet();
    }
    
    /**
     * A node is valid when is a chance node and it contains a valid potential type.
     *
     */
    private static void validNode(Node node) throws LinkInversionRequiresChanceVariablesWithPotential {
        if (node.getNodeType() != NodeType.CHANCE) {
            throw new LinkInversionRequiresChanceVariablesWithPotential(node);
        }
        List<Potential> potentials = node.getPotentials();
        if (potentials.isEmpty() || !validPotentialType(potentials.getFirst())) {
            throw new LinkInversionRequiresChanceVariablesWithPotential(node);
        }
    }
    
    /**
     * A potential is valid when can be projected to a TablePotential.
     *
     * @return boolean
     */
    private static boolean validPotentialType(Potential potential) {
        
        return (!(potential instanceof AugmentedProbTable ||
                potential instanceof AugmentedProbTablePotential ||
                potential instanceof BinomialPotential ||
                potential instanceof FunctionPotential ||
                potential instanceof SameAsPrevious ||
                potential instanceof UnivariateDistrPotential));
    }
    
    /**
     *
     * @param node1 the node1
     * @param node2 the node2
     *
     * @return boolean
     */
    private static boolean validPotentials(Node node1, Node node2) {
        
        boolean validPotentials = false;
        if (node1.getNodeType() == NodeType.CHANCE && node2.getNodeType() == NodeType.CHANCE) {
            List<Potential> potentials1 = node1.getPotentials();
            List<Potential> potentials2 = node2.getPotentials();
            if (!potentials1.isEmpty() && !potentials1.isEmpty()) {
                validPotentials = validPotential(potentials1.getFirst()) && validPotential(potentials2.getFirst());
            }
        }
        return validPotentials;
    }
    
    private static boolean validPotential(Potential potential) {
        
        return (!(potential instanceof AugmentedProbTable ||
                potential instanceof AugmentedProbTablePotential ||
                potential instanceof BinomialPotential ||
                potential instanceof FunctionPotential ||
                potential instanceof SameAsPrevious ||
                potential instanceof UnivariateDistrPotential));
    }
    
}
