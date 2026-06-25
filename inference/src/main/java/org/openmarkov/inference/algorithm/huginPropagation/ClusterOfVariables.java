/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation.StorageLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@code ClusterOfVariables} is a node in a {@code ClusterForest}.
 * <p>
 * This is in general a hypernode, in the sense that it contains a set of
 * variables, and each of these variables is represented by a {@code Node}.
 *
 * @author Manuel Arias
 * @author fjdiez
 */
public abstract class ClusterOfVariables {
    // Attributes for performance test
    public static int collectEvidenceInvocations = 0;
    public static int distributeEvidenceInvocations = 0;
    
    /**
     * Used to form the cluster's name.
     */
    protected static final String clusterNamePrefix = "Cluster.";
    /**
     * Variables in common with another {@code ClusterOfVariables}.
     */
    public List<Variable> separatorVariables;
    /**
     * Cluster's name.
     */
    protected String name;
    /**
     * Potentials whose variables are all in the cluster.
     */
    protected final List<TablePotential> priorPotentials;
    /**
     * An {@code evidencePotential} has only one variable with a
     * probability of 1.0 one and only one state and 0.0 in the others.
     */
    protected final List<TablePotential> evidencePotentials;
    /**
     * Resulting potential of multiplying prior and evidence potentials by the
     * messages received from all its neighbors.
     */
    protected TablePotential posteriorPotential = null;
    /**
     * Variables in this cluster
     */
    protected final List<Variable> variables;
    /**
     * Message created in the collect evidence phase, it goes from children to
     * parents with the separator variables.
     */
    protected TablePotential upgoingMessage = null;
    /**
     * Message created in the distribute evidence phase, it goes from parents to
     * children.
     */
    protected TablePotential downgoingMessage = null;
    protected int clusterSize = 0;
    private final ClusterForest clusterForest;
    
    // Constructor
    
    /**
     * @param clusterForest {@code ClusterForest}.
     * @param variables     {@code ArrayList} of {@code Variable}s.
     */
    public ClusterOfVariables(ClusterForest clusterForest, List<Variable> variables) {
        this.clusterForest = clusterForest;
        this.variables = variables;
        // super(null, variables, NodeType.CLUSTER);
        clusterForest.increaseNumNodes();
        name = clusterNamePrefix + (clusterForest.getNumNodes() - 1);
        priorPotentials = new ArrayList<>();
        evidencePotentials = new ArrayList<>();
        separatorVariables = new ArrayList<>();
    }
    
    // Methods
    
    /**
     * @return {@code ArrayList} of {@code ClusterOfVariables}
     */
    public List<ClusterOfVariables> getChildren() {
        return clusterForest.getChildren(this);
    }
    
    /**
     * @return {@code ArrayList} of {@code ClusterOfVariables}
     */
    public List<ClusterOfVariables> getParents() {
        return clusterForest.getParents(this);
    }
    
    /**
     * @return The {@code Object} associated to this node that contains an
     * {@code ArrayList} of {@code Variable}s.
     */
    public List<Variable> getVariables() {
        return new ArrayList<Variable>(variables);
    }
    
    /**
     * @param potential {@code Potential}
     */
    public void addPriorPotential(TablePotential potential) {
        priorPotentials.add(potential);
    }
    
    /**
     * @return priorPotentials {@code ArrayList} of {@code Potential}
     * s.
     */
    public List<TablePotential> getAssignedPotentials() {
        return priorPotentials;
    }
    
    /**
     * Calculates the marginalized multiplication of: {@code priorPotentials,
     * evidencePotentials} and the recursively collected evidence from
     * the children of this {@code ClusterOfVariables}.
     *
     * @param storageLevel If its value is 2 the collected evidence is stored in
     *                     the {@code posteriorPotential} without being marginalized
     *                     {@code int}
     *
     * @return The marginalized multiplication ({@code Potential}).
     */
    public TablePotential collectEvidence(StorageLevel storageLevel) {
        if (upgoingMessage != null) { // It has been calculated before
            return upgoingMessage;
        }
        collectEvidenceInvocations++;
        // adds the prior potentials and evidence potentials
        List<TablePotential> potentials = new ArrayList<>(priorPotentials);
        potentials.addAll(this.evidencePotentials);
        // recursively invokes collectEvidence on its children
        // and add the collected potentials
        for (ClusterOfVariables child : getChildren()) {
            potentials.add(child.collectEvidence(storageLevel));
        }
        boolean isRootClique = separatorVariables.isEmpty();
        posteriorPotential = DiscretePotentialOperations.multiply(potentials);
        upgoingMessage = (isRootClique) ?
                posteriorPotential :
                DiscretePotentialOperations.marginalize(posteriorPotential, separatorVariables);
        TablePotential collectedEvidence = upgoingMessage;
        switch (storageLevel) {
            case NO_STORAGE: {
                // Delete upgoing message
                upgoingMessage = null;
                break;
            }
            case MEDIUM: {
                // Delete posterior potential
                posteriorPotential = null;
                break;
            }
            case FULL: {
                // Do nothing
                break;
            }
        }
        return collectedEvidence;
    }
    
    /**
     * Sends a message to each child. The message is the multiplication of:
     * {@code priorPotentials, evidencePotentials} and the upgoing messages
     * from its other children
     *
     * @param storageLevel the amount of intermediate operation that are stored.
     */
    public void distributeEvidence(StorageLevel storageLevel) {
        // stores the product of priorPotentials, evidencePotentials, and
        // the downgoingMessage
        TablePotential intermediateProduct = getIntermediateProduct();
        // sends a downgoingMessage to each child;
        // this message is the product of the intermediateProduct multiplied
        // by the upgoing messages from its other children
        List<ClusterOfVariables> children = getChildren();
        List<ClusterOfVariables> otherChildren;
        for (ClusterOfVariables child : children) {
            List<TablePotential> potentials = new ArrayList<>();
            potentials.add(intermediateProduct);
            otherChildren = new ArrayList<>(children);
            otherChildren.remove(child);
            for (ClusterOfVariables otherChild : otherChildren) {
                potentials.add(otherChild.getUpgoingMessage(storageLevel));
            }
            child.setDowngoingPotential(
                    DiscretePotentialOperations.multiplyAndMarginalize(potentials, child.getSeparatorVariables()));
            child.setPosteriorPotential(child.getPosteriorPotential(storageLevel));
        }
    }
    
    /**
     * @return The product of prior potentials, evidence potentials and the
     * downgoing message if it exists (does not exist in root clusters).
     */
    private TablePotential getIntermediateProduct() {
        // adds the prior potentials and evidence potentials
        List<TablePotential> potentials = new ArrayList<>();
        potentials.addAll(priorPotentials);
        potentials.addAll(evidencePotentials);
        // downgoingMessage is null for root clusters
        if (downgoingMessage != null) {
            potentials.add(downgoingMessage);
        }
        // stores the product of priorPotentials, evidencePotentials, and
        // the downgoingMessage
        if (potentials.isEmpty()) {
            return null;
        }
        return DiscretePotentialOperations.multiply(potentials);
    }
    
    /**
     * @param child        {@code ClusterOfVariables}.
     * @param storageLevel {@code int}.
     *
     * @return The {@code Potential} sended to {@code child}.
     */
    protected Potential getDowngoingPotential(ClusterOfVariables child, StorageLevel storageLevel) {
        TablePotential intermediateProduct = getIntermediateProduct();
        // sends a downgoingMessage to each child;
        // this message is the product of the intermediateProduct multiplied
        // by the upgoing messages from its other children
        List<ClusterOfVariables> children = getChildren();
        List<TablePotential> potentials = new ArrayList<>();
        if (intermediateProduct != null) {
            potentials.add(intermediateProduct);
        }
        List<ClusterOfVariables> otherChildren = new ArrayList<>(children);
        otherChildren.remove(child);
        for (ClusterOfVariables otherChild : otherChildren) {
            potentials.add(otherChild.getUpgoingMessage(storageLevel));
        }
        return PotentialOperations.multiplyAndMarginalize(potentials, child.getSeparatorVariables());
    }
    
    /**
     * @param storageLevel {@code int}.
     *
     * @return posteriorPotential {@code Potential}.
     */
    public TablePotential getPosteriorPotential(StorageLevel storageLevel) {
        if (posteriorPotential != null) {
            return posteriorPotential;
        }
        // adds the prior potentials and evidence potentials
        List<TablePotential> potentials = new ArrayList<>(priorPotentials);
        potentials.addAll(evidencePotentials);
        // recursively invokes collectEvidence on its children
        // and add the collected potentials
        List<ClusterOfVariables> children = getChildren();
        for (ClusterOfVariables child : children) {
            potentials.add(child.collectEvidence(storageLevel));
        }
        return DiscretePotentialOperations.multiply(potentials);
    }
    
    /**
     * @param posteriorPotential {@code Potential}.
     */
    public void setPosteriorPotential(TablePotential posteriorPotential) {
        this.posteriorPotential = posteriorPotential;
    }
    
    /**
     * @param potential {@code Potential}.
     */
    public void addEvidencePotential(TablePotential potential) {
        evidencePotentials.add(potential);
    }
    
    /**
     * @return separatorVariables {@code ArrayList} of {@code Variable}
     * s.
     */
    public List<Variable> getSeparatorVariables() {
        return separatorVariables;
    }
    
    /**
     * @param separatorVariables {@code ArrayList} of {@code Variable}
     *                           s. Must be included in {@code cliqueVariables}.
     */
    public void setSeparatorVariables(List<Variable> separatorVariables) {
        this.separatorVariables = separatorVariables;
    }
    
    /**
     * @param storageLevel {@code int}.
     *
     * @return upgoingMessage {@code Potential}.
     */
    public TablePotential getUpgoingMessage(StorageLevel storageLevel) {
        if (upgoingMessage != null) {
            return upgoingMessage;
        }
        return collectEvidence(storageLevel);
    }
    
    /**
     * @return name {@code String}.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Multiplies the priorPotentials and replaces them with the product.
     * It does the same recursively in its children
     *
     */
    public void compilePriorPotentials() {
        if (separatorVariables.isEmpty()) { // root clique, without separator
            if (priorPotentials.size() > 1) {
                TablePotential priorPotential = DiscretePotentialOperations.multiply(priorPotentials);
                priorPotentials.clear();
                priorPotentials.add(priorPotential);
            }
        } else { // no root clique, with separator
            TablePotential priorPotential = DiscretePotentialOperations
                    .multiplyAndMarginalize(priorPotentials, getVariables());
            priorPotentials.clear();
            priorPotentials.add(priorPotential);
        }
        // recursive call
        List<ClusterOfVariables> children = getChildren();
        for (ClusterOfVariables child : children) {
            child.compilePriorPotentials();
        }
    }
    
    /**
     * Overrides {@code toString} method. Mainly for test purposes.
     */
    public String toString() {
        String out = name + ": {" + variables.stream()
                                             .map(Variable::getName)
                                             .collect(Collectors.joining(", ")) + "} - ";
        if ((separatorVariables != null) && (!separatorVariables.isEmpty())) {
            out += ((separatorVariables.size() == 1) ? "Separator" : "Separators") + ": {"
                    + separatorVariables.stream()
                                        .map(Variable::getName)
                                        .collect(Collectors.joining(", "))
                    + "}";
        } else {
            out += "No separator";
        }
        out += " - ";
        if (posteriorPotential != null) {
            out += "Posterior potential: " + posteriorPotential.toShortString() + " - ";
        }
        if (priorPotentials != null && !priorPotentials.isEmpty()) {
            out += "Prior potentials (" + priorPotentials.size() + "): " +
                    priorPotentials.stream().map(TablePotential::toShortString).collect(Collectors.joining(", "))
                    + " - ";
        }
        if (evidencePotentials != null && !evidencePotentials.isEmpty()) {
            out += "Evidence potentials (" + evidencePotentials.size() + "): " +
                    evidencePotentials.stream().map(TablePotential::toShortString).collect(Collectors.joining(", "))
                    + " - ";
        }
        if (upgoingMessage != null) {
            out += "Upgoing message: " + upgoingMessage.toShortString() + " - ";
        }
        if (downgoingMessage != null) {
            out += "Downgoing message: " + downgoingMessage.toShortString() + " - ";
        }
        return out.toString();
    }
    
    /**
     * @param potential {@code Potential}.
     */
    protected void setDowngoingPotential(TablePotential potential) {
        downgoingMessage = potential;
    }
    
    /**
     * @return Clique size = product of number of states of variable (all of
     * them discrete) {@code int}
     */
    public int size() {
        if (clusterSize == 0) {
            clusterSize = 1;
            for (Variable variable : variables) {
                if (variable.getVariableType() == VariableType.FINITE_STATES) {
                    clusterSize *= variable.getNumStates();
                }
            }
        }
        return clusterSize;
    }
}
