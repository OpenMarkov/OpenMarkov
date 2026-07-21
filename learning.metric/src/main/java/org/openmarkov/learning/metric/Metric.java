/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.metric.cache.Cache;
import org.openmarkov.learning.core.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class defines the basic elements of a metric.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @author ibermejo
 * @version 1.1
 * @since OpenMarkov 1.0
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = {
        @RequiredConstructor({}),
        @RequiredConstructor(double.class)
})
public abstract class Metric implements PNEditListener {
    
    // Members
    
    /**
     * Cache used to speed up the search of the best editions
     */
    protected Cache cache = null;
    
    /**
     * Net we are generating editions for
     */
    protected ProbNet probNet;
    
    /**
     * Case database we are using for learning
     */
    protected CaseDatabase caseDatabase;
    
    /**
     * Current Score
     */
    protected double cachedScore;
    
    /**
     * HashMap with the score of each node. (We do not want to
     * recalculate the score of all the nodes of the net every time
     * we make an edition)
     */
    protected Map<String, Double> cachedNodeScores;
    
    // Constructor
    
    /**
     *
     */
    public Metric() {
    }
    
    //Methods
    public void init(ProbNet probNet, CaseDatabase caseDatabase) {
        this.probNet = probNet;
        this.caseDatabase = caseDatabase;
        
        probNet.getPNESupport().addListener(this);
    }
    
    /**
     * Scores the node based on a table potential with the absolute
     * frequencies of the values of the child node given the parent nodes.
     *
     * @param nodePotential the node potential
     * @return score for the node
     */
    public abstract double score(TablePotential nodePotential);
    
    /**
     * Scores the associated network.
     */
    public double getScore() {
        if (cache == null)
            initCache();
        return cachedScore;
    }
    
    /**
     * Scores the associated network given the edit.
     *
     * @param edit the edit
     * @return the score of the edit
     */
    public double getScore(PNEdit edit) {
        if (cache == null)
            initCache();
        return cache.getScore(edit);
    }
    
    /**
     * Scores the associated network given the edit.
     *
     * @param edit the edit
     * @return the score of the edit
     */
    protected double score(PNEdit edit) {
        double newScore;
        Class<?> pNEditClass = edit.getClass();
        if (pNEditClass == AddLinkEdit.class) {
            newScore = score((AddLinkEdit) edit, false);
        } else if (pNEditClass == RemoveLinkEdit.class) {
            newScore = score((RemoveLinkEdit) edit, false);
        } else {
            newScore = score((InvertLinkEdit) edit, false);
        }
        return newScore;
    }
    
    /**
     * Scores the associated network with the link given in the received
     * edition added. We only have to recalculate the score
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     *
     * @param edition {@code AddLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is
     *                definitive (UndoableEditHappend called this method) or not.
     * @return {@code double} score of the net with the given edition
     */
    protected double score(AddLinkEdit edition, boolean change) {
        Node destinationNode = probNet.getNode(edition.getVariableTo());
        Node originNode = probNet.getNode(edition.getVariableFrom());
        double lastNodeScore = cachedNodeScores.get(destinationNode.getName());
        TablePotential absFrequencies = Util
                .getAbsoluteFreqExtraParent(probNet, caseDatabase, destinationNode, originNode);
        double newNodeScore = scoreNode(destinationNode, absFrequencies, false);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change) {
            cachedNodeScores.put(destinationNode.getName(), newNodeScore);
        }
        
        return newNodeScore - lastNodeScore;
    }
    
    /**
     * Scores the associated network with the link given in the received
     * edition removed. We only have to recalculate the score
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     *
     * @param edition {@code AddLinkEdit}
     * @param change  {@code boolean} indicates wheter the edition is
     *                definitive (UndoableEditHappend called this method) or not.
     * @return {@code double} score of the net with the given edition
     */
    protected double score(RemoveLinkEdit edition, boolean change) {
        Node destinationNode = probNet.getNode(edition.getVariableTo());
        Node originNode = probNet.getNode(edition.getVariableFrom());
        
        double lastNodeScore = cachedNodeScores.get(destinationNode.getName());
        TablePotential absFrequencies = Util
                .getAbsoluteFreqRemovingParent(probNet, caseDatabase, destinationNode, originNode);
        double newNodeScore = scoreNode(destinationNode, absFrequencies, false);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change) {
            cachedNodeScores.put(destinationNode.getName(), newNodeScore);
        }
        
        return newNodeScore - lastNodeScore;
    }
    
    /**
     * Scores the associated network with the link given in the received
     * edition inverted. We have to recalculate the scores
     * of the destination nodes before and after the inversion. If an undoable
     * edit happened (that is, if parameter change is true) we update the
     * entropy and dimension of the destination node and the net.
     *
     * @param edition {@code AddLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is
     *                definitive (UndoableEditHappend called this method) or not.
     * @return {@code double} score of the net with the given edition
     */
    protected double score(InvertLinkEdit edition, boolean change) {
        Node initialDestinationNode = probNet.getNode(edition.getVariableTo());
        Node initialOriginNode = probNet.getNode(edition.getVariableFrom());
        double lastNodeScore = cachedNodeScores.get(initialDestinationNode.getName());
        TablePotential absFrequencies = Util
                .getAbsoluteFreqRemovingParent(probNet, caseDatabase, initialDestinationNode, initialOriginNode);
        double newNodeScore = scoreNode(initialDestinationNode, absFrequencies, false);
        /*
         * If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations
         */
        if (change) {
            cachedNodeScores.put(initialDestinationNode.getName(), newNodeScore);
        }
        double result = newNodeScore - lastNodeScore;
        lastNodeScore = cachedNodeScores.get(initialOriginNode.getName());
        absFrequencies = Util
                .getAbsoluteFreqExtraParent(probNet, caseDatabase, initialOriginNode, initialDestinationNode);
        newNodeScore = scoreNode(initialOriginNode, absFrequencies, false);
        /*
         * If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations
         */
        if (change) {
            cachedNodeScores.put(initialOriginNode.getName(), newNodeScore);
        }
        result += (newNodeScore - lastNodeScore);
        return result;
    }
    
    /**
     * Scores the given node with the new parent given.
     *
     * @param node           {@code Node}
     * @param absFrequencies the abs frequencies
     * @param change         {@code boolean} indicates whether the edition is
     *                       definitive (UndoableEditHappend called this method) or not.
     * @return {@code double} score of the node with the given parent
     */
    protected double scoreNode(Node node, TablePotential absFrequencies, boolean change) {
        double nodeScore = score(absFrequencies);
        
        /* Store the entropy of the node to avoid repeating the calculations */
        if (change) {
            cachedNodeScores.put(node.getName(), nodeScore);
        }
        return nodeScore;
    }
    
    /**
     * An undoable edit will happen.
     *
     * @param edit {@code UndoableEditEvent} that will happen
     */
    @Override public void beforeEditExecutes(PNEdit edit) {
        if (cache == null) {
            initCache();
        }
    }
    
    /**
     * An undoable edit happened. We have to update the copy of the net and
     * score this new net.
     *
     * @param edit {@code UndoableEditEvent} that happened
     */
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        if (BaseLinkEdit.class.isAssignableFrom(edit.getClass())) {
            updateCache((BaseLinkEdit) edit);
        }
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (BaseLinkEdit.class.isAssignableFrom(edit.getClass())) {
            updateCache(((BaseLinkEdit) edit).getUndoEdit());
        }
    }
    
    /**
     * Method to update the cache after doing an edition to the learnedNet.
     *
     * @param edit the edit
     */
    private void updateCache(BaseLinkEdit edit) {
        if (cache == null) {
            initCache();
        } else {
            Class<?> editClass = edit.getClass();
            if (editClass == AddLinkEdit.class) {
                cachedScore = score((AddLinkEdit) edit, true);
            } else if (editClass == RemoveLinkEdit.class) {
                cachedScore = score((RemoveLinkEdit) edit, true);
            } else if (editClass == InvertLinkEdit.class) {
                cachedScore = score((InvertLinkEdit) edit, true);
            }
            
        }
        PNEdit updatedEdit;
        /* Obtain the destination node of the given edition */
        Variable head = edit.getVariableTo();
        /*
         * Score all the links that have as destination the destination node of
         * the bestEdition
         */
        for (Variable tail : probNet.getVariables()) {
            if ((!tail.equals(head)) && (!isFixedLink(probNet, tail, head))) {
                if (!probNet.getNode(head).isParent(probNet.getNode(tail))) {
                    updatedEdit = new AddLinkEdit(probNet, tail, head, true);
                } else {
                    updatedEdit = new RemoveLinkEdit(probNet, tail, head, true);
                }
                cache.cacheScore(updatedEdit, score(updatedEdit));
            }
        }
        
        /*
         * If we have a link inversion, we have to update the entries of the
         * cache of both origin and destination node of the original links
         */
        if (edit.getClass() == InvertLinkEdit.class) {
            Variable head2 = edit.getVariableFrom();
            for (Variable tail : probNet.getVariables()) {
                if ((!tail.equals(head2)) && (!isFixedLink(probNet, tail, head2))) {
                    if (!probNet.getNode(head2).isParent(probNet.getNode(tail))) {
                        updatedEdit = new AddLinkEdit(probNet, tail, head2, true);
                    } else {
                        updatedEdit = new RemoveLinkEdit(probNet, tail, head2, true);
                    }
                    cache.cacheScore(updatedEdit, score(updatedEdit));
                }
            }
        }
    }
    
    /**
     * Method to check whether the link from var1 to var2 is a fixed link.
     *
     * @param var1 origin variable
     * @param var2 destination variable
     *
     * @return true if the link is fixed, otherwise false.
     */
    private boolean isFixedLink(ProbNet probNet, Variable var1, Variable var2) {
        return (cache.getRemoveScore(probNet, var1, var2) == Double.NEGATIVE_INFINITY);
    }
    
    /**
     * Fills cache with data
     */
    protected void initCache() {
        this.cache = new Cache();
        cachedNodeScores = new HashMap<String, Double>();
        cache.flush(probNet);
        
        cachedScore = 0;
        for (Node node : probNet.getNodes()) {
            cachedScore += scoreNode(node, Util.getAbsoluteFreqExtraParent(probNet, caseDatabase, node, null), true);
        }
        PNEdit edit;
        for (Variable tail : probNet.getVariables()) {
            for (Variable head : probNet.getVariables()) {
                if ((!tail.equals(head)) && (!isFixedLink(probNet, tail, head))) {
                    if (!probNet.getNode(head).isParent(probNet.getNode(tail)))
                        edit = new AddLinkEdit(probNet, tail, head, true);
                    else
                        edit = new RemoveLinkEdit(probNet, tail, head, true);
                    cache.cacheScore(edit, score(edit));
                }
            }
        }
    }
    
}
