/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.inference.InferenceOptions;

import java.util.*;

/**
 * Groups all descriptive and configuration metadata of a {@link ProbNet}:
 * name, comment, default states, decision criteria, agents, cycle length,
 * inference options, and additional format-specific properties.
 *
 * <p>This is a mutable value object owned by {@link ProbNet}.
 * {@link ProbNet} exposes its contents through delegating getters and setters
 * so the public API of the network does not change.
 */
public class NetworkMetadata {

    private String name;
    private String comment = "";
    private boolean showCommentWhenOpening = false;
    private State[] defaultStates = { new State("absent"), new State("present") };
    private InferenceOptions inferenceOptions = new InferenceOptions();
    private CycleLength cycleLength = new CycleLength();
    private List<StringWithProperties> agents;
    private List<Criterion> decisionCriteria;
    private final Map<String, String> additionalProperties = new LinkedHashMap<>();

    /** Creates metadata with default values. */
    public NetworkMetadata() {
        this.decisionCriteria = new ArrayList<>();
        this.decisionCriteria.add(new Criterion());
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean getShowCommentWhenOpening() { return showCommentWhenOpening; }
    public void setShowCommentWhenOpening(boolean show) { this.showCommentWhenOpening = show; }

    /** Returns a defensive copy so callers cannot mutate the stored array. */
    public State[] getDefaultStates() {
        State[] copy = new State[defaultStates.length];
        for (int i = 0; i < defaultStates.length; i++) {
            copy[i] = new State(defaultStates[i]);
        }
        return copy;
    }
    public void setDefaultStates(State[] defaultStates) { this.defaultStates = defaultStates; }

    public InferenceOptions getInferenceOptions() { return inferenceOptions; }
    public void setInferenceOptions(InferenceOptions inferenceOptions) { this.inferenceOptions = inferenceOptions; }

    public CycleLength getCycleLength() { return cycleLength; }
    public void setCycleLength(CycleLength cycleLength) { this.cycleLength = cycleLength; }

    public List<StringWithProperties> getAgents() { return agents; }
    public void setAgents(List<StringWithProperties> agents) { this.agents = agents; }

    public List<Criterion> getDecisionCriteria() { return decisionCriteria; }
    public void setDecisionCriteria(List<Criterion> decisionCriteria) { this.decisionCriteria = decisionCriteria; }

    /**
     * Returns an unmodifiable view of the additional properties.
     * Use {@link #setAdditionalProperties(Map)} to bulk-replace or
     * {@link #putAdditionalProperty(String, String)} to add a single entry.
     */
    public Map<String, String> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }

    /** Replaces all additional properties; {@code null} is treated as empty. */
    public void setAdditionalProperties(Map<String, String> props) {
        this.additionalProperties.clear();
        if (props != null) {
            this.additionalProperties.putAll(props);
        }
    }

    /** Adds or replaces a single additional property. */
    public void putAdditionalProperty(String key, String value) {
        this.additionalProperties.put(key, value);
    }
}
