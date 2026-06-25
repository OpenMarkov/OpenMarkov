/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.localize.ClassLocalizable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single state of a finite-state variable.
 * Each state has a name and optional additional properties (key-value metadata).
 */
public class State implements Cloneable, ClassLocalizable {

    // Attributes
    private final Map<String, String> additionalProperties;

    private String name;

    // Constructor
    public State(String name) {
        this.name = name;
        this.additionalProperties = new LinkedHashMap<>();
    }

    public State(State state) {
        this.additionalProperties = new LinkedHashMap<>(state.additionalProperties);
        this.name = state.name;
    }
    
    // Methods
    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        this.name = newName;
    }

    /** @return unmodifiable view of the additional properties. */
    public Map<String, String> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }

    /** Replaces all additional properties with the entries from {@code properties}. */
    public void setAdditionalProperties(Map<String, String> properties) {
        additionalProperties.clear();
        additionalProperties.putAll(properties);
    }

    /** Adds or updates a single additional property. */
    public void putAdditionalProperty(String key, String value) {
        additionalProperties.put(key, value);
    }
    
    public boolean equals(Object other) {
        if (!(other instanceof State state)) {
            return false;
        }
        return this.name.equals(state.name);
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
    @Override public State clone() {
        return new State(this);
    }
}
