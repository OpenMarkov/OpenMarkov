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
            newNode.setPotentials(node.getPotentials());
            // The properties come from Node itself, which is what the note that used to sit here
            // asked for: "Hacer clon para node y quitar estas lineas". Node.clone is not usable
            // as it stands because it clones the variable, and a shallow copy shares it.
            node.copyPropertiesTo(newNode);
        }
        copyLinks(source, dest, false);
        copyMetadata(source, dest, false);
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
        dest.getInferenceOptions().setMonteCarloOptions(source.getInferenceOptions().getMonteCarloOptions());
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

        // Every constraint, with no skipping: the destination was emptied by clearConstraints just
        // above, so there is nothing here to deduplicate against.
        source.getConstraints().forEach(dest::addConstraint);

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
        copyMetadata(source, dest, true);
        dest.getPNESupport().setListeners(source.getPNESupport().getListeners());
        dest.setAdditionalProperties(source.getAdditionalProperties());
        return dest;
    }

    /**
     * Copies the descriptive metadata of {@code source} into {@code dest}: the comment, whether
     * that comment is shown when the file is opened, the default states offered to new variables,
     * and the agents. Both copies need all four, so the list lives here only once.
     *
     * <p>The agents are the only ones where deep and shallow differ: when {@code deep} the copy
     * gets its own {@link StringWithProperties} objects, and otherwise it gets the same ones, as
     * the rest of a shallow copy does. Sharing them is safe because agents are matched by name,
     * not by identity ({@link ProbNetAgentManager#modifyAgent}). The list itself is always the
     * copy's own, so adding an agent to one network never adds it to the other. A {@code null}
     * list is copied as {@code null} because that is what tells the network it has a single
     * agent, which is what the {@code OnlyOneAgent} constraint reads.
     */
    private static void copyMetadata(ProbNet source, ProbNet dest, boolean deep) {
        dest.setComment(source.getComment());
        dest.setShowCommentWhenOpening(source.getShowCommentWhenOpening());
        // getDefaultStates already hands out a copy of the array and of each state, so there is
        // nothing to share here even in a shallow copy.
        dest.setDefaultStates(source.getDefaultStates());
        List<StringWithProperties> sourceAgents = source.getAgents();
        if (sourceAgents == null) {
            dest.setAgents(null);
        } else {
            List<StringWithProperties> destAgents = new ArrayList<>(sourceAgents.size());
            for (StringWithProperties agent : sourceAgents) {
                destAgents.add(deep ? agent.clone() : agent);
            }
            dest.setAgents(destAgents);
        }
    }

    /**
     * Copies the link structure from {@code source} into {@code dest}.
     * When {@code deep} is {@code true}, restriction potentials and revealing
     * intervals are deep-copied; otherwise the same object references are shared.
     */
    private static void copyLinks(ProbNet source, ProbNet dest, boolean deep) {
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
    }
}
