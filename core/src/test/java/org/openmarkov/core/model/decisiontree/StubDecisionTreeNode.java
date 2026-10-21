/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

/**
 * Minimal concrete subclass of {@link DecisionTreeNode} used only for unit testing.
 * Avoids depending on inference-module subclasses.
 */
final class StubDecisionTreeNode<T> extends DecisionTreeNode<T> {

    StubDecisionTreeNode(Node node) {
        super(node);
    }

    StubDecisionTreeNode(Node node, ProbNet network) {
        super(node, network);
    }

    StubDecisionTreeNode(Variable variable, ProbNet probNet) {
        super(variable, probNet);
    }

    @Override
    public boolean isBestDecision(DecisionTreeBranch<T> treeBranch) {
        return false;
    }

    /** Reads a single-criterion evaluation, the only kind these tests give the stub. */
    @Override
    @SuppressWarnings("unchecked")
    public T getUtility() {
        if (getEvaluation() == null) {
            return null;
        }
        return (T) Double.valueOf(((UnicritNodeEvaluation) getEvaluation()).getUtility());
    }

    @Override
    public EvaluationType getEvaluationType() {
        return EvaluationType.UNICRITERION;
    }
}
