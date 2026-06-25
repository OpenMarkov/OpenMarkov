/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class that centralises the two copy strategies for {@link ProbNet}:
 *
 * <ul>
 *   <li>{@link #shallowCopy} — structural copy that shares {@code Variable} and
 *       {@code Potential} object references with the original (previously known as
 *       {@code auxCopy}).</li>
 *   <li>{@link #deepCopy} — full deep copy where all mutable objects are cloned.</li>
 * </ul>
 */
final class ProbNetCopier {

    private ProbNetCopier() {
    }

    /**
     * Creates a shallow structural copy of {@code source}: the graph topology,
     * node metadata, and link properties are copied, but {@code Variable} and
     * {@code Potential} objects are shared with the original.
     */
    static ProbNet shallowCopy(ProbNet source) {
        ProbNet dest = new ProbNet(source.getNetworkType());
        dest.setName(source.getName());
        for (PNConstraint constraint : source.getConstraints()) {
            dest.addConstraint(constraint);
        }
        for (Node node : source.getNodes()) {
            Variable variable = node.getVariable();
            Node newNode = dest.addNode(variable, node.getNodeType());
            newNode.setCoordinateX(node.getCoordinateX());
            newNode.setCoordinateY(node.getCoordinateY());
            newNode.setPotentials(node.getPotentials());
            // TODO Hacer clon para node y quitar estas lineas
            newNode.setPurpose(node.getPurpose());
            newNode.setRelevance(node.getRelevance());
            newNode.setComment(node.getComment());
            newNode.setAdditionalProperties(node.getAdditionalProperties());
            newNode.setAlwaysObserved(node.isAlwaysObserved());
        }
        copyLinks(source, dest, false);
        dest.getPNESupport().setListeners(source.getPNESupport().getListeners());
        dest.setAdditionalProperties(source.getAdditionalProperties());
        if (source.getDecisionCriteria() != null) {
            dest.setDecisionCriteria(source.getDecisionCriteria());
        }
        if (source.getCycleLength() != null) {
            dest.setCycleLength(source.getCycleLength());
        }
        dest.getInferenceOptions().setMultiCriteriaOptions(source.getInferenceOptions().getMultiCriteriaOptions());
        dest.getInferenceOptions().setTemporalOptions(source.getInferenceOptions().getTemporalOptions());
        return dest;
    }

    /**
     * Creates a full deep copy of {@code source}: all mutable objects (criteria,
     * cycle length, inference options, nodes, potentials, link intervals) are
     * cloned into independent instances.
     */
    static ProbNet deepCopy(ProbNet source) {
        ProbNet dest = new ProbNet(source.getNetworkType());
        dest.clearConstraints();

        dest.setName(source.getName());

        if (source.getCycleLength() != null) {
            dest.setCycleLength(new CycleLength(source.getCycleLength()));
        }
        dest.setInferenceOptions(new InferenceOptions(source.getInferenceOptions()));

        if (source.getDecisionCriteria() != null) {
            List<Criterion> newCriteria = new ArrayList<>();
            for (Criterion criterion : source.getDecisionCriteria()) {
                newCriteria.add(new Criterion(criterion));
            }
            dest.setDecisionCriteria(newCriteria);
        }

        List<PNConstraint> sourceConstraints = source.getConstraints();
        int numConstraints = sourceConstraints.size();
        sourceConstraints.stream().skip(1).limit(numConstraints - 1)
                         .forEach(dest::addConstraint);

        List<Node> nodes = source.getNodes();
        for (Node node : nodes) {
            Node newNode = node.clone(dest);
            dest.addNode(newNode);
        }

        // Deep-copy potentials and update neighbour lists
        for (Node node : nodes) {
            // TODO - Problem?
            for (Node neighbour : source.getNeighbors(node)) {
                dest.getNode(neighbour.getName());
            }
            List<Potential> newPotentials = new ArrayList<>();
            for (Potential potential : node.getPotentials()) {
                newPotentials.add(potential.deepCopy(dest));
            }
            Objects.requireNonNull(dest.getNode(node.getName()),
                    "Node not found in dest: " + node.getName()).setPotentials(newPotentials);
        }

        copyLinks(source, dest, true);
        dest.getPNESupport().setListeners(source.getPNESupport().getListeners());
        dest.setAdditionalProperties(source.getAdditionalProperties());
        return dest;
    }

    /**
     * Copies the link structure from {@code source} into {@code dest}.
     * When {@code deep} is {@code true}, restriction potentials and revealing
     * intervals are deep-copied; otherwise the same object references are shared.
     */
    private static void copyLinks(ProbNet source, ProbNet dest, boolean deep) {
        if (source.hasExplicitLinks()) {
            dest.makeLinksExplicit(false);
            for (Link<Node> link : source.getLinks()) {
                Node destFrom = Objects.requireNonNull(dest.getNode(link.getFrom().getVariable().getName()),
                        "Node not found in dest: " + link.getFrom().getVariable().getName());
                Node destTo = Objects.requireNonNull(dest.getNode(link.getTo().getVariable().getName()),
                        "Node not found in dest: " + link.getTo().getVariable().getName());
                Link<Node> destLink = dest.addLink(destFrom, destTo, link.isDirected());
                if (deep) {
                    if (link.getRestrictionsPotential() != null) {
                        destLink.setRestrictionsPotential((TablePotential) link.getRestrictionsPotential().deepCopy(dest));
                    }
                    List<PartitionedInterval> newIntervals = new ArrayList<>();
                    for (PartitionedInterval interval : link.getRevealingIntervals()) {
                        newIntervals.add(new PartitionedInterval(interval.limits, interval.belongsToLeftSide));
                    }
                    destLink.setRevealingIntervals(newIntervals);
                    destLink.setRevealingStates(new ArrayList<>(link.getRevealingStates()));
                } else {
                    destLink.setRestrictionsPotential(link.getRestrictionsPotential());
                    destLink.setRevealingIntervals(link.getRevealingIntervals());
                    destLink.setRevealingStates(link.getRevealingStates());
                }
            }
        } else {
            for (Node node : source.getNodes()) {
                Node destNode = dest.getNode(node.getVariable().getName());
                for (Node sibling : source.getSiblings(node)) {
                    Node destSibling = dest.getNode(sibling.getVariable().getName());
                    if (!dest.isSibling(destNode, destSibling)) {
                        dest.addLink(destNode, destSibling, false);
                    }
                }
                for (Node child : source.getChildren(node)) {
                    Node destChild = dest.getNode(child.getVariable().getName());
                    dest.addLink(destNode, destChild, true);
                }
            }
        }
    }
}
