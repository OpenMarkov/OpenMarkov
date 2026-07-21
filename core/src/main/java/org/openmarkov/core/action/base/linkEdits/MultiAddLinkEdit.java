/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a directed or undirected link between two nodes associated to two
 * variables in a {@code ProbNet}
 */
public final class MultiAddLinkEdit extends CompoundPNEdit {
    
    private final List<AddLinkEdit> edits;
    
    // Constructor
    public MultiAddLinkEdit(ProbNet probNet, List<Node> from, List<Node> to, boolean isDirected) {
        super(probNet);
        this.edits = resolveEdits(probNet, from, to, isDirected);
    }
    
    @Override protected ArrayList<PNEdit> generateEdits() {
        return new ArrayList<>(this.edits);
    }
    
    private static List<AddLinkEdit> resolveEdits(ProbNet probNet, List<Node> from, List<Node> to, boolean isDirected) {
        if (from.isEmpty() || to.isEmpty()) {
            return Collections.emptyList();
        }
        boolean selfLoopIsAllowed = !probNet.hasConstraintOfClass(NoSelfLoop.class);
        return from
                .stream()
                .flatMap(origin -> to
                        .stream()
                        .map(destination1 -> Tuples.record(origin, destination1)))
                .filter(tuple1 -> probNet.getLink(tuple1.v0(), tuple1.v1(), isDirected) == null)
                .filter(tuple1 -> selfLoopIsAllowed || tuple1.v0() != tuple1.v1())
                .map(tuple -> new AddLinkEdit(probNet, tuple.v0().getVariable(), tuple.v1().getVariable(), isDirected))
                .toList();
    }
    
    
}
