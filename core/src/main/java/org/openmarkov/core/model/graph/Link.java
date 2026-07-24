/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;

import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.localize.ConsiderAutoLocalizationIsValid;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class implements explicit links.
 * <p>
 * Equality is deliberately by <em>identity</em> (neither {@code equals} nor {@code hashCode} is
 * overridden): the graph stores the <em>same</em> {@link Link} object in the link lists of both
 * endpoints, and {@code Graph.removeLink(Link)} relies on identity to remove exactly that shared
 * object. Redefining {@code equals} by value would break this scheme.
 *
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @see Node
 * @see Graph
 * @since OpenMarkov 1.0
 */
@ConsiderAutoLocalizationIsValid
public class Link<T> implements ClassLocalizable {
    
    // Attributes
    /**
     * The first node. If the link is directed, this node is the parent.
     */
    private final T from;
    
    /**
     * The second node. If the link is directed, this node is the child.
     */
    private final T to;
    
    /**
     * If true, the link is directed. Otherwise, it is an undirected link.
     */
    private final boolean directed;
    
    /****
     * Potential that contains the value of compatibility for the combinations
     * of the variables of node1 and node2
     */
    private TablePotential restrictionsPotential;
    
    /*****
     * List of revealing values of type state
     */
    private List<State> revealingStates;
    
    /*****
     * List of revealing values of type interval
     */
    private List<PartitionedInterval> revealingIntervals;

    /**
     * Midpoint used to read a compatibility value. The restrictions potential holds, by
     * construction, exactly 0 (incompatible) or 1 (compatible) — see
     * {@link #initializesRestrictionsPotential()} and {@link #setCompatibilityValue}. Restrictions
     * can also be read from a file, though, so the values are compared against this midpoint rather
     * than tested for equality with 1: a value that is not exactly 0 or 1 is read as the nearer of
     * the two instead of silently counting as a restriction.
     */
    private static final double COMPATIBILITY_MIDPOINT = 0.5;

    // Constructors
    
    /**
     * Creates an unlabelled link and sets the cross references in the nodes.
     * This constructor should be called only from the {@code addLink}
     * function in the class Graph. Both nodes must belong to the same graph.
     *
     * @param from     {@code Node}.
     * @param to       {@code Node}.
     * @param directed {@code boolean}.
     *
     */
    public Link(T from, T to, boolean directed) {
        this.from = from;
        this.to = to;
        this.directed = directed;
        revealingStates = new ArrayList<>();
        revealingIntervals = new ArrayList<>();
        
    }
    
    // Methods
    
    /**
     * @return The parent (if the link is directed) or the first node (if the
     * link is undirected).
     */
    public T getFrom() {
        return from;
    }
    
    /**
     * @return The child (if the link is directed) or the second node (if the
     * link is undirected).
     */
    public T getTo() {
        return to;
    }
    
    /**
     * @param node {@code Node}.
     *
     * @return {@code true} if {@code node} is one of the two ends of the link. Ends are compared by
     * equality, as {@link Graph} does everywhere else — its maps and lists of neighbours, and the
     * directed branch of {@code Graph.getLink}, all work by {@code equals}. (Comparing by identity
     * here used to make {@code Graph.getLink(node1, node2, false)} miss an undirected link whenever
     * the caller passed an equal but distinct node object.)
     */
    public boolean contains(T node) {
        return from.equals(node) || to.equals(node);
    }
    
    /**
     * @return {@code true} if the link is directed, false if it is
     * undirected
     */
    public boolean isDirected() {
        return directed;
    }
    
    /******
     * @return {@code true} if the link has a linkRestriction
     *                          associates,false otherwise
     */
    public boolean hasRestrictions() {
        return restrictionsPotential != null;
    }
    
    /****
     * @return {@code true} if a value of the first variable makes all
     *                          values of the second variable impossible.
     *
     */
    public boolean hasTotalRestriction() {
        boolean totalRestriction = false;
        if (hasRestrictions()) {
            int numStates = restrictionsPotential.getVariables().getFirst().getNumStates();
            int valuesSize = restrictionsPotential.getValues().length;
            
            for (int index = 0; index < numStates && !totalRestriction; index++) {
                boolean valueRestrictsVariable = true;
                int i = index;
                while (i < valuesSize && valueRestrictsVariable) {
                    if (isCompatible(restrictionsPotential.getValues()[i])) {
                        valueRestrictsVariable = false;

                    }
                    i += numStates;
                }
                
                if (valueRestrictsVariable) {
                    totalRestriction = true;
                }
            }
        }
        
        return totalRestriction;
        
    }
    
    /****
     * @return {@code true} if a value of the first variable makes all
     *                          values of the second variable impossible.
     *
     */
    public Set<State> getStatesRestrictTotally() {
        
        Set<State> statesRestrictTotally = new HashSet<>();
        
        if (hasRestrictions()) {
            Variable parentVariable = restrictionsPotential.getVariables().getFirst();
            int numStates = parentVariable.getNumStates();
            int valuesSize = restrictionsPotential.getValues().length;
            
            for (int index = 0; index < numStates; index++) {
                boolean totalRestriction = true;
                int i = index;
                while (i < valuesSize && totalRestriction) {
                    totalRestriction = !isCompatible(restrictionsPotential.getValues()[i]);
                    i += numStates;
                }
                
                if (totalRestriction) {
                    statesRestrictTotally.add(parentVariable.getStates()[index]);
                }
            }
        }
        
        return statesRestrictTotally;
        
    }
    
    /**
     * @param compatibilityValue a value of the restrictions potential
     *
     * @return {@code true} if the value means "compatible" — see {@link #COMPATIBILITY_MIDPOINT}
     */
    private static boolean isCompatible(double compatibilityValue) {
        return compatibilityValue >= COMPATIBILITY_MIDPOINT;
    }

    /**
     * Initializes a TablePotential for the variable associated to node1 and
     * node2, whose values are all 1.
     */
    public void initializesRestrictionsPotential() {
        List<Variable> variables = new ArrayList<>();
        variables.add(((Node) from).getVariable());
        variables.add(((Node) to).getVariable());
        restrictionsPotential = new TablePotential(variables, PotentialRole.LINK_RESTRICTION);
        Arrays.fill(restrictionsPotential.getValues(), 1);
        
    }
    
    /*****
     * Assigns a null value to the restrictionsPotential if the restrictions
     * potential does not contain restrictions
     *
     */
    public void tryResetRestrictionsPotential() {
        if (restrictionsPotential == null) {
            return;   // nothing to reset; consistent with setCompatibilityValue/areCompatible
        }
        double[] restrictions = this.restrictionsPotential.getValues();
        for (double restriction : restrictions) {
            if (restriction == 0) {
                return;
            }
        }
        restrictionsPotential = null;
    }
    
    /*****
     * Assigns the value of the parameter compatibility to the combination of
     * the variables state1 and state2.
     *
     * @param state1 the state1
     *            state of the variable of node1
     * @param state2 the state2
     *            state of the variable of node2
     * @param compatibility the compatibility
     *            value of compatibility
     */
    public void setCompatibilityValue(State state1, State state2, int compatibility) {
        if (this.restrictionsPotential == null) {
            this.initializesRestrictionsPotential();
        }
        int[] indexes = new int[2];
        indexes[0] = restrictionsPotential.getVariable(0).getStateIndex(state1);
        indexes[1] = restrictionsPotential.getVariable(1).getStateIndex(state2);
        List<Variable> variables = restrictionsPotential.getVariables();
        restrictionsPotential.setValue(variables, indexes, compatibility);
    }
    
    /******
     * Returns the compatibility value of the combination of state1 and state2.
     *
     * @param state1 the state1
     *            state of the variable of node1.
     * @param state2 the state2
     *            state of the variable of node2.
     * @return the value 1 for compatibility and 0 for incompatibility.
     */
    
    public int areCompatible(State state1, State state2) {
        if (this.restrictionsPotential == null) {
            return 1;
        }
        int[] indexes = new int[2];
        indexes[0] = restrictionsPotential.getVariable(0).getStateIndex(state1);
        indexes[1] = restrictionsPotential.getVariable(1).getStateIndex(state2);
        List<Variable> variables = restrictionsPotential.getVariables();

        return isCompatible(restrictionsPotential.getValue(variables, indexes)) ? 1 : 0;

    }
    
    /****
     *
     * @return the potential of the the link restriction.
     */
    public TablePotential getRestrictionsPotential() {
        return restrictionsPotential;
    }
    
    /****
     * Assigns the potential to the restrictionPotential of the link
     *
     * @param potential Potential
     */
    
    public void setRestrictionsPotential(TablePotential potential) {
        this.restrictionsPotential = potential;
    }
    
    /**
     * @return String
     */
    public String toString() {
        if (!directed) {
            return from.toString() + " --- " + to.toString();
        }
        return from.toString() + " --> " + to.toString();
    }
    
    /*****
     * This method indicates whether there are revealing conditions for the
     * link.
     *
     * @return {@code true} if there exist revealing conditions.
     */
    public boolean hasRevealingConditions() {
        
        VariableType varType = ((Node) from).getVariable().getVariableType();
        
        if (varType == VariableType.NUMERIC) {
            return !revealingIntervals.isEmpty();
        }
        return !revealingStates.isEmpty();
    }
    
    /**
     * Returns the link's <em>live</em> list of revealing states (not a copy): some callers, such as
     * {@code RevelationIntervalEdit}, edit the revealing conditions in place through it.
     *
     * @return the revealingStates
     */
    public List<State> getRevealingStates() {
        return revealingStates;
    }

    /**
     * Stores a defensive copy, so the link neither shares nor is later mutated through the caller's
     * list (S4).
     *
     * @param revealingStates the revealingStates to set
     */
    public void setRevealingStates(List<State> revealingStates) {
        this.revealingStates = new ArrayList<>(revealingStates);
    }

    /**
     * Returns the link's <em>live</em> list of revealing intervals (not a copy): some callers, such
     * as {@code RevelationIntervalEdit}, edit them in place through it.
     *
     * @return the revealingIntervals
     */
    public List<PartitionedInterval> getRevealingIntervals() {
        return revealingIntervals;
    }

    /**
     * Stores a defensive copy, so the link does not share the caller's list (S4).
     *
     * @param revealingIntervals the revealingIntervals to set
     */
    public void setRevealingIntervals(List<PartitionedInterval> revealingIntervals) {
        this.revealingIntervals = new ArrayList<>(revealingIntervals);
    }
    
    /*****
     * Adds the state to the revealing condition list.
     *
     * @param state State
     */
    public void addRevealingState(State state) {
        
        revealingStates.add(state);
    }
    
    /*****
     * Removes the revealing state from the revealing condition list.
     *
     * @param state State
     */
    public void removeRevealingState(State state) {
        revealingStates.remove(state);
        
    }
    
    /*****
     * Adds the interval to the revealing condition list.
     *
     * @param interval Interval
     */
    public void addRevealingInterval(PartitionedInterval interval) {
        this.revealingIntervals.add(interval);
    }
    
    /********
     * Removes the interval from the revealing condition list.
     *
     * @param interval Interval
     */
    public void removeRevealingInterval(PartitionedInterval interval) {
        this.revealingIntervals.remove(interval);
    }
    
	public void linkRestrictionPotentialValue(Integer newValue, int row, int col){
		int numStates2 = ((Node) to).getVariable().getNumStates();
		int stateIndex1 = col - 1;
		int stateIndex2 = numStates2 - row;
		State state1 = ((Node) from).getVariable().getStates()[stateIndex1];
		State state2 = ((Node) to).getVariable().getStates()[stateIndex2];
		setCompatibilityValue(state1, state2, newValue);
	}

}
