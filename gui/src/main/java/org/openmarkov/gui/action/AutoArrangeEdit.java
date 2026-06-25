/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */
package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compound undoable edit that relocates every node in a {@link ProbNet}
 * to the positions computed by an auto-arrange algorithm. Modelled after
 * {@link MoveNodeEdit}, but takes a name → position map directly so it
 * can run without a visual layer (e.g. right after a learning algorithm
 * produces a network, before any {@code VisualNode} exists).
 */
public class AutoArrangeEdit extends PNEdit {

    private static final long serialVersionUID = 1L;

    private final List<String> namesNode = new ArrayList<>();
    private final List<Point2D.Double> lastPositions = new ArrayList<>();
    private final List<Point2D.Double> newPositions  = new ArrayList<>();

    public AutoArrangeEdit(ProbNet probNet,
                           Map<String, Point2D.Double> targetPositions) {
        super(probNet);
        for (Node node : probNet.getNodes()) {
            Point2D.Double target = targetPositions.get(node.getName());
            if (target == null) continue;
            namesNode.add(node.getName());
            lastPositions.add(new Point2D.Double(node.getCoordinateX(),
                                                 node.getCoordinateY()));
            newPositions.add(target.clone());
        }
    }

    @Override protected void doEdit() {
        probNet.moveNode(namesNode, newPositions);
    }

    @Override public void undo() {
        super.undo();
        probNet.moveNode(namesNode, lastPositions);
    }
}
