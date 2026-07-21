/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.configuration.KeyTracker;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EditionState(name = "Edit.Mode.Selection", icon = "selection.png")
public class SelectionEditionMode extends EditionMode {
    
    private static final int NODE_SPEED_ON_ARROW_PRESS = 2;
    
    private SelectionState selectionState;
    
    private boolean currentlyHoldingMouse;
    private final Set<Integer> currentlyHeldKeys;
    
    public SelectionEditionMode(NetworkEditorPanel networkEditorPanel, ProbNet probNet) {
        super(networkEditorPanel, probNet);
        this.selectionState = SelectionState.NOTHING;
        this.currentlyHoldingMouse = false;
        this.currentlyHeldKeys = new HashSet<>();
    }
    
    @Override public void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g) {
        this.currentlyHoldingMouse = true;
        if (this.selectionState != SelectionState.NOTHING || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (e.isControlDown() || e.isShiftDown()) {
            this.visualNetwork.addToSelection(position, g);
            return;
        }
        if (this.visualNetwork.selectElementInPosition(position, g) == null) {
            this.visualNetwork.startSelectionRectangle(position);
            this.setSelectionState(SelectionState.SELECTING);
        }
    }
    
    @Override public void mouseMoved(MouseEvent e, Point2D.Double position, double diffX, double diffY,
                                     Graphics2D g) {
        this.lastMousePos = position;
        if (this.selectionState == SelectionState.SELECTING) {
            this.visualNetwork.updateSelectionRectangle(diffX, diffY);
        } else if (this.selectionState == SelectionState.CREATING_LINK) {
            this.visualNetwork.updateLinkCreation(position, g);
            this.networkEditorPanel.repaint();
        } else if (this.selectionState == SelectionState.MOVING ||
                (this.selectionState == SelectionState.NOTHING && SwingUtilities.isLeftMouseButton(e) && !this.visualNetwork.getSelectedNodes()
                                                                                                                            .isEmpty())) {
            this.setSelectionState(SelectionState.MOVING);
            this.visualNetwork.moveSelectedElements(diffX, diffY);
        }
        this.networkEditorPanel.repaint();
    }
    
    @Override public void tryCancelCurrentAction(MouseEvent e, Point2D.Double position, Graphics2D g) {
        this.currentlyHoldingMouse = false;
        switch (this.selectionState) {
            case SelectionState.NOTHING -> {
            }
            case SelectionState.MOVING -> {
                try {
                    this.tryFinishNodesMovements();
                } catch (DoEditException ex) {
                    throw new UnreachableException(ex);
                }
            }
            case SelectionState.SELECTING -> this.visualNetwork.finishSelectionRectangle(position);
            case CREATING_LINK -> this.visualNetwork.cancelLinkCreation();
        }
        this.setSelectionState(SelectionState.NOTHING);
        this.networkEditorPanel.repaint();
    }
    
    @Override
    public void mouseReleased(MouseEvent e, Point2D.Double position, Graphics2D g) throws DoEditException {
        this.currentlyHoldingMouse = false;
        switch (this.selectionState) {
            case SelectionState.NOTHING -> {
            }
            case SelectionState.MOVING -> this.tryFinishNodesMovements();
            case SelectionState.SELECTING -> this.visualNetwork.finishSelectionRectangle(position);
            case CREATING_LINK -> {
                this.visualNetwork.finishLinkCreation(position, g);
                if (this.currentlyHeldKeys.contains(KeyEvent.VK_SHIFT)) {
                    this.visualNetwork.startLinkCreation(position, g, VisualNetwork.LinkCreationSourceDirection.PARENT, true, this.visualNetwork.getSelectedNodes());
                    this.networkEditorPanel.repaint();
                    return;
                }
            }
        }
        this.setSelectionState(SelectionState.NOTHING);
        this.networkEditorPanel.repaint();
    }
    
    public boolean startLinkCreation(Point2D.Double cursorPosition, VisualNetwork.LinkCreationSourceDirection sourceDirection) {
        if (this.visualNetwork.getSelectedNodes().isEmpty()) {
            return false;
        }
        this.linkCreationStartedWithKey = false;
        this.visualNetwork.startLinkCreation(cursorPosition, (Graphics2D) this.networkEditorPanel.getGraphics(), sourceDirection, false, this.visualNetwork.getSelectedNodes());
        this.setSelectionState(SelectionState.CREATING_LINK);
        return true;
    }
    
    private void tryFinishNodesMovements() throws DoEditException {
        if (this.isMovingNodes()) {
            return;
        }
        List<VisualNode> movedNodes = this.visualNetwork.fillVisualNodesSelected();
        new MoveNodeEdit(movedNodes).executeEdit();
        this.networkEditorPanel.adjustPanelDimension();
        this.setSelectionState(SelectionState.NOTHING);
    }
    
    /**
     * Changes the state of the selection and carries out the necessary actions
     * in each case.
     *
     * @param newState new mouse state.
     */
    private void setSelectionState(SelectionState newState) {
        this.networkEditorPanel.setCursor(newState.getCursor());
        this.selectionState = newState;
    }
    
    @Override public void keyTyped(KeyEvent e) {
        
    }
    
    
    @Override public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        onPresses(e, keyCode);
    }
    
    private void onPresses(@Nullable KeyEvent e, int keyCode) {
        this.currentlyHeldKeys.add(keyCode);
        KeyTracker.isHeld(keyCode);
        
        if (this.visualNetwork.getSelectedNodes().isEmpty()) {
            return;
        }
        switch (this.selectionState) {
            case MOVING -> applyKeyArrowsOnNodes();
            case NOTHING -> {
                switch (keyCode) {
                    case KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT ->
                            applyKeyArrowsOnNodes();
                    case KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL -> {
                        if(this.currentlyHeldKeys.contains(KeyEvent.VK_SHIFT) && this.currentlyHeldKeys.contains(KeyEvent.VK_CONTROL)) {
                            if (this.startLinkCreation(this.lastMousePos, VisualNetwork.LinkCreationSourceDirection.PARENT)) {
                                this.linkCreationStartedWithKey = true;
                            }
                        }
                    }
                }
            }
            case SELECTING -> {
            }
            case CREATING_LINK -> {
                switch (keyCode) {
                    case KeyEvent.VK_ESCAPE -> {
                        this.visualNetwork.cancelLinkCreation();
                        this.setSelectionState(SelectionState.NOTHING);
                    }
                    case KeyEvent.VK_ALT -> {
                        this.visualNetwork.toggleLinkCreationSource(this.lastMousePos);
                        if(e!=null){
                            e.consume();
                        }
                    }
                }
            }
        }
        this.networkEditorPanel.repaint();
    }
    
    @Override public void keyReleased(KeyEvent e) {
        onReleases(e.getKeyCode());
    }
    
    private void onReleases(int keyCode) {
        switch (this.selectionState) {
            case NOTHING -> {
            }
            case MOVING -> {
                boolean wasHoldingAnArrow = this.isHoldingAnArrow();
                this.currentlyHeldKeys.remove(keyCode);
                if (wasHoldingAnArrow && !this.isHoldingAnArrow()) {
                    try {
                        this.tryFinishNodesMovements();
                    } catch (DoEditException ex) {
                        throw new UnrecoverableException(ex);
                    }
                }
            }
            case SELECTING -> {
            }
            case CREATING_LINK -> {
                switch (keyCode){
                    case KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL -> {
                        if (this.linkCreationStartedWithKey) {
                            this.visualNetwork.cancelLinkCreation();
                            this.linkCreationStartedWithKey=false;
                            this.setSelectionState(SelectionState.NOTHING);
                        }
                    }
                }
            }
        }
    }
    
    private void applyKeyArrowsOnNodes() {
        int diffX = 0, diffY = 0;
        for (var key : this.currentlyHeldKeys) {
            switch (key) {
                case KeyEvent.VK_UP -> diffY -= SelectionEditionMode.NODE_SPEED_ON_ARROW_PRESS;
                case KeyEvent.VK_RIGHT -> diffX += SelectionEditionMode.NODE_SPEED_ON_ARROW_PRESS;
                case KeyEvent.VK_DOWN -> diffY += SelectionEditionMode.NODE_SPEED_ON_ARROW_PRESS;
                case KeyEvent.VK_LEFT -> diffX -= SelectionEditionMode.NODE_SPEED_ON_ARROW_PRESS;
            }
        }
        if (diffX == 0 && diffY == 0) {
            return;
        }
        this.setSelectionState(SelectionState.MOVING);
        this.visualNetwork.moveSelectedElements(diffX, diffY);
    }
    
    public boolean isMovingNodes() {
        return this.selectionState == SelectionState.MOVING && (this.currentlyHoldingMouse || isHoldingAnArrow());
    }
    
    private boolean isHoldingAnArrow() {
        return this.currentlyHeldKeys.contains(KeyEvent.VK_UP)
                || this.currentlyHeldKeys.contains(KeyEvent.VK_RIGHT)
                || this.currentlyHeldKeys.contains(KeyEvent.VK_DOWN)
                || this.currentlyHeldKeys.contains(KeyEvent.VK_LEFT);
    }
    
    private Point2D.Double lastMousePos;
    private boolean linkCreationStartedWithKey;
    
    @Override public void focusGained(FocusEvent e) {
        var currentlyHeldKeys = KeyTracker.getHeldKeys().boxed()
                             .collect(Collectors.toCollection(LinkedHashSet::new));
        var removedKeys = this.currentlyHeldKeys.stream().filter(key->!currentlyHeldKeys.contains(key)).toList();
        var newlyPressedKeys = currentlyHeldKeys.stream().filter(key->!this.currentlyHeldKeys.contains(key)).toList();
        removedKeys.forEach(this::onReleases);
        newlyPressedKeys.forEach(key->this.onPresses(null, key));
    }
    
    @Override public void focusLost(FocusEvent e) {
    
    }
}
