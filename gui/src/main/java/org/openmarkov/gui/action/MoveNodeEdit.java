/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.graphic.VisualNode;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code MoveNodeEdi} is a simple edit that allows to modify the position
 * of a group of nodes
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
public class MoveNodeEdit extends PNEdit {
    
    private static final long serialVersionUID = 7578733825996342882L;
    
    private final List<Movement> movements;
    
    record Movement(VisualNode visualNode, String nodeName, Point2D.Double previousPosition,
                    Point2D.Double newPosition) {
        
    }
    
    /**
     * Creates a new {@code MoveNodeEdit} with the nodes, and new X, Y
     * coordinates.
     *
     * @param movedNodes the nodes that will be edited, with their new
     *                   positions.
     */
    public MoveNodeEdit(List<VisualNode> movedNodes) {
        super(movedNodes.getFirst().getNode().getProbNet());
        this.movements = movedNodes.stream()
                                   .map(visualNode -> new Movement(visualNode,
                                                                   visualNode.getNode().getName(),
                                                                   visualNode.getPosition().clone(),
                                                                   visualNode.getTemporalPosition().clone()))
                                   .toList();
    }
    
    @Override protected void doEdit() {
        probNet.moveNode(this.movements.stream().map(Movement::nodeName).toList()
                , this.movements.stream().map(Movement::newPosition).toList());
        for (Movement movement : this.movements) {
            movement.visualNode.setTemporalPosition(movement.newPosition);
        }
    }
    
    @Override public void undo() {
        super.undo();
        probNet.moveNode(this.movements.stream().map(Movement::nodeName).toList()
                , this.movements.stream().map(Movement::previousPosition).toList());
        for (Movement movement : this.movements) {
            movement.visualNode.setTemporalPosition(movement.previousPosition);
        }
        
    }
}
