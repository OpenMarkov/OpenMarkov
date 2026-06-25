/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.openmarkov.core.model.network.NodeType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This enum class encapsulates the types of a network.
 *
 * @author jmendoza
 * @version 1.1 - jrico revision of overcomplexity
 */
public enum NetworkType {

    BAYESIAN_NET,
    INFLUENCE_DIAGRAM,
    MARKOV_NET,
    CHAIN_GRAPH,
    SIMPLE_MARKOV_MODEL,
    /** Simple Markov Model. */
    MARKOV_DECISION_PROCESS,
    POMDP,
    /** Decision Analysis Network */
    DAN,
    LIMID,
    /** Dynamic Bayesian Network */
    DYN_BAYESIAN_NET,
    /** Dynamic LIMID */
    DYN_LIMID,
    DEC_POMDP,
    OOPN,
    TUNING;
    
    /**
     * Types of node that can be created into the network.
     */
    private final HashSet<NodeType> nodeTypes = new HashSet<NodeType>();
    
    static {
        for (NetworkType type : NetworkType.values()) {
            switch (type) {
                case BAYESIAN_NET -> type.nodeTypes.add(NodeType.CHANCE);
                case INFLUENCE_DIAGRAM, SIMPLE_MARKOV_MODEL, MARKOV_DECISION_PROCESS, POMDP, DAN -> {
                    type.nodeTypes.add(NodeType.CHANCE);
                    type.nodeTypes.add(NodeType.DECISION);
                    type.nodeTypes.add(NodeType.UTILITY);
                }
                case MARKOV_NET, CHAIN_GRAPH, TUNING, OOPN, DEC_POMDP, DYN_LIMID, DYN_BAYESIAN_NET, LIMID -> {
                }
            }
        }
    }
    
    /**
     * Returns types of node that can be created into this type of network.
     *
     * @return a list of types of node that can be created into this type of
     * network.
     */
    public Set<NodeType> getNodeTypes() {
        return Collections.unmodifiableSet(this.nodeTypes);
    }
}
