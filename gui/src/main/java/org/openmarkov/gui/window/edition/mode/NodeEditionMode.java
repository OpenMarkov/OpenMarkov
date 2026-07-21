/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.util.GUIDefaultStates;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;

/**
 * Edition mode for creating nodes of a given {@link NodeType}.
 * One instance per node type is registered in {@link EditionModeManager};
 * there is no need for per-type subclasses.
 */
public class NodeEditionMode extends EditionMode {
    private final NodeType nodeType;

    public NodeEditionMode(NetworkEditorPanel networkEditorPanel, ProbNet probNet, NodeType nodeType) {
        super(networkEditorPanel, probNet);
        this.nodeType = nodeType;
    }
    
    @Override
    public void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g) throws DoEditException {
        if (!(SwingUtilities.isLeftMouseButton(e) && GUIUtils.noMouseModifiers(e))) {
            return;
        }
        if (visualNetwork.getElementInPosition(position, g) != null) {
            return;
        }
        NodeEditionMode.createNode(probNet, nodeType, position, networkEditorPanel);
    }
    
    @Override public void mouseReleased(MouseEvent e, Point2D.Double cursorPosition, Graphics2D g) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mouseMoved(MouseEvent e, Point2D.Double position, double diffX, double diffY,
                                     Graphics2D g) {
        // TODO Auto-generated method stub
    }
    
    @Override public void tryCancelCurrentAction(MouseEvent e, Point2D.Double position, Graphics2D g) {
    
    }
    
    @Override public void keyTyped(KeyEvent e) {
    
    }
    
    @Override public void keyPressed(KeyEvent e) {
    
    }
    
    @Override public void keyReleased(KeyEvent e) {
    
    }
    
    public static void createNode(ProbNet currentNetwork, NodeType nodeType, Point2D.Double position, NetworkEditorPanel networkEditorPanel) throws DoEditException {
        HashSet<String> existingNames = new HashSet<>();
        for (Node node : currentNetwork.getNodes()) {
            String name = node.getName();
            if (name.contains("[")) {
                existingNames.add(name.substring(0, name.indexOf(" [")));
            } else {
                existingNames.add(node.getName());
            }
        }
        String nodeName = Util.getNextNodeName(nodeType, existingNames);
        State[] states = DefaultStates.getStatesNodeType(nodeType, currentNetwork.getDefaultStates());
        for (int i = 0; i < states.length; i++) {
            states[i] = new State(GUIDefaultStates.getString(states[i].getName()));
        }
        Variable variable = new Variable(nodeName, states);
        if (currentNetwork.onlyTemporal()) {
            // default value
            variable.setBaseName(nodeName);
            variable.setTimeSlice(0);
        }
        List<Criterion> decisionCriteria = currentNetwork.getDecisionCriteria();
        if (nodeType == NodeType.UTILITY && decisionCriteria != null) {
            variable.setDecisionCriterion(decisionCriteria.getFirst());
        }
        currentNetwork.getPNESupport().setWithUndo(true);
        currentNetwork.getPNESupport().openNewSubEditHistory();
        PNEdit addNodeEdit = new AddNodeEdit(currentNetwork, variable, nodeType, position);
        addNodeEdit.executeEdit();
        var visualNode = networkEditorPanel.getVisualNetwork().getAllNodes().stream().filter(node->node.getNode().getVariable()==variable).findFirst().get();
        var visualNodeShape = visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics());
        visualNode.setTemporalCoordinateX(visualNode.getTemporalPosition().x-(visualNodeShape.getBounds2D().getWidth()/2));
        visualNode.setTemporalCoordinateY(visualNode.getTemporalPosition().y-(visualNodeShape.getBounds2D().getHeight()/2));
        new MoveNodeEdit(List.of(visualNode)).executeEdit();
        currentNetwork.getPNESupport().closeSubEditHistory();
        
        networkEditorPanel.adjustPanelDimension();
        networkEditorPanel.repaint();
    }
    
    @Override public void focusGained(FocusEvent e) {
    
    }
    
    @Override public void focusLost(FocusEvent e) {
    
    }
}
