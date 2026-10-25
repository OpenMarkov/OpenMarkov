/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when an algorithm that only learns probabilities starts from a network where some nodes have no table.
 *
 * @author Manuel Arias
 */
public class NodesWithoutProbabilityException extends OpenMarkovException {

    public NodesWithoutProbabilityException(Class<? extends LearningAlgorithm> algorithmClass, List<Node> nodes) {
        this.algorithmType = algorithmClass.getAnnotation(LearningAlgorithmType.class);
        this.nodeNames = nodes.stream().map(Node::getName).collect(Collectors.joining(", "));
    }

    public final LearningAlgorithmType algorithmType;
    /** The names of the nodes without a table, separated by commas. */
    public final String nodeNames;

    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
