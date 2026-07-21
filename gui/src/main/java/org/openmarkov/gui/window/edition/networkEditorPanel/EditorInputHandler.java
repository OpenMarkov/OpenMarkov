package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.exception.PreResolutionNodeInInferenceException;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.graphic.VisualState;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenu;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Handles all mouse and keyboard input for the {@link NetworkEditorPanel},
 * delegating to the current {@link EditionMode} and managing contextual menus.
 */
class EditorInputHandler implements MouseListener, MouseMotionListener, KeyListener, FocusListener {
    
    private final NetworkEditorPanel networkEditorPanel;
    
    EditorInputHandler(NetworkEditorPanel networkEditorPanel) {
        this.networkEditorPanel = networkEditorPanel;
    }
    
    /**
     * Invoked when a mouse button has been clicked (pressed and released) on
     * the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseClicked(MouseEvent e) {
        this.networkEditorPanel.requestFocus();
    }
    
    private int lastClickCount = 0;
    private boolean lastLeftClickProducedANode = false;
    
    
    /**
     * Invoked when a mouse button has been pressed on the component.
     *
     * @param e mouse event information.
     */
    @Override public void mousePressed(MouseEvent e) {
        this.networkEditorPanel.requestFocus();
        // requestFocusInWindow(); Activate if nodes can't be moved by arrows.
        if (e.getClickCount() <= (this.lastClickCount + 1)) {
            this.lastLeftClickProducedANode = false;
            this.lastClickCount = Math.max(e.getClickCount() - 1, 0);
        } else {
            this.lastClickCount += 1;
        }
        // requestFocusInWindow(); Activate if nodes can't be moved by arrows.
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        this.cursorPosition.setLocation(this.networkEditorPanel.getZoomManager()
                                                               .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                .screenToPanel(e.getY()));
        // Specific functionality depending on the edition mode;
        try {
            var oldNodesCount = this.networkEditorPanel.getNetworkEditorPanel().getProbNet().getNodes().size();
            this.networkEditorPanel.getEditionMode().mousePressed(e, this.cursorPosition, g);
            if (e.getClickCount() == 1) {
                int newNodesCount = this.networkEditorPanel.getNetworkEditorPanel().getProbNet().getNodes().size();
                this.lastLeftClickProducedANode = oldNodesCount < newNodesCount;
            }
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        }
        // Generic functionality regardless of the edition mode
        if (SwingUtilities.isRightMouseButton(e)) {
            this.showContextualMenu(e, g);
            this.networkEditorPanel.repaint();
            return;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) {
            this.networkEditorPanel.repaint();
            return;
        }
        VisualNode node;
        if (e.isAltDown() && e.getClickCount() != 2) {
            node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
            if (node != null) {
                if (!node.isSelected()) {
                    this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
                    this.networkEditorPanel.getVisualNetwork().setSelectedNode(node, true);
                }
                try {
                    this.networkEditorPanel.showPotentialDialog(this.networkEditorPanel.getNetworkEditorPanel()
                                                                                       .getWorkingMode() != NetworkEditorPanel.WorkingMode.EDITION);
                } finally {
                    this.networkEditorPanel.repaint();
                    return;
                }
            }
        }
        if (!(e.getClickCount() == 2 && GUIUtils.noMouseModifiers(e))) {
            this.networkEditorPanel.repaint();
            return;
        }
        this.networkEditorPanel.getEditionMode().tryCancelCurrentAction(e, this.cursorPosition, g);
        
        if (this.networkEditorPanel.getNetworkEditorPanel()
                                   .getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION) {
            // If we are in Edition Mode a double click must open
            // the corresponding properties dialog (for node, link
            // or network)
            node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
            if (node != null) {
                try {
                    boolean userAcceptedChanges = this.networkEditorPanel.changeNodeProperties(node, this.lastLeftClickProducedANode);
                    if (!userAcceptedChanges && this.lastLeftClickProducedANode) {
                        ArrayList<PNEdit> undone;
                        do {
                            undone = this.networkEditorPanel.getNetworkEditorPanel()
                                                            .getProbNet()
                                                            .getPNESupport()
                                                            .undo();
                        } while (undone != null && undone.stream().noneMatch(AddNodeEdit.class::isInstance));
                        this.networkEditorPanel.getNetworkEditorPanel()
                                               .getProbNet()
                                               .getPNESupport()
                                               .removeUndoneEdits();
                    }
                } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                         IncompatibleEvidenceException | ConstraintViolatedException | NotSupportedOperationException |
                         CannotNormalizePotentialException ex) {
                    this.networkEditorPanel.repaint();
                    throw new UnrecoverableException(ex);
                }
            } else {
                VisualLink link = this.networkEditorPanel.getVisualNetwork().whatLinkInPosition(this.cursorPosition, g);
                if (link != null) {
                    this.networkEditorPanel.changeLinkProperties(link);
                } else {
                    this.networkEditorPanel.changeNetworkProperties();
                }
            }
            this.networkEditorPanel.repaint();
            return;
        }
        
        VisualState visualState = this.networkEditorPanel.getVisualNetwork().whatStateInPosition(this.cursorPosition, g);
        if (visualState == null) {
            if ((this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g) != null) && (
                    this.networkEditorPanel.getVisualNetwork().whatInnerBoxInPosition(this.cursorPosition, g) == null
            )) {
                try {
                    this.networkEditorPanel.changeNodeProperties();
                } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                         IncompatibleEvidenceException | ConstraintViolatedException | NotSupportedOperationException |
                         CannotNormalizePotentialException ex) {
                    throw new UnrecoverableException(ex);
                } finally {
                    this.networkEditorPanel.repaint();
                }
            }
            this.networkEditorPanel.repaint();
            return;
        }
        
        // If we are in Inference Mode a double click inside a
        // visual state of a node without pre-resolution finding
        // must introduce evidence in that node.
        // If the double click is inside a node but outside its
        // inner box (in its 'expanded external shape'), its
        // properties dialog should be open
        
        VisualNode visualNode = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
        if (visualNode.isPreResolutionFinding()) {
            throw new UnrecoverableException(new PreResolutionNodeInInferenceException(visualNode));
        }
        
        try {
            this.networkEditorPanel.getEvidenceManager().toggleFinding(visualNode, visualState);
        } catch (IncompatibleEvidenceException | NotEvaluableNetworkException | NonProjectablePotentialException |
                 NotEnoughMemoryException | DoEditException | CannotNormalizePotentialException |
                 ConstraintViolatedException ex) {
            throw new UnreachableException(ex);
        }
        
        
    }
    
    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     *
     * @param e mouse event information.
     */
    @Override public void mouseDragged(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double point = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                         .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                          .screenToPanel(e.getY()));
        double diffX = point.getX() - this.cursorPosition.getX();
        double diffY = point.getY() - this.cursorPosition.getY();
        this.cursorPosition.setLocation(point);
        this.networkEditorPanel.getEditionMode().mouseMoved(e, point, diffX, diffY, g);
    }
    
    /**
     * Invoked when a mouse button has been released on the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseReleased(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double position = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                            .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                             .screenToPanel(e.getY()));
        try {
            this.networkEditorPanel.getEditionMode().mouseReleased(e, position, g);
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    /**
     * Invoked when the mouse button enters the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseEntered(MouseEvent e) {
    }
    
    /**
     * Invoked when the mouse button exits the component.
     *
     * @param e mouse event information.
     */
    @Override public void mouseExited(MouseEvent e) {
    }
    
    private VisualNode visualNodeOfToolTip;
    
    public VisualNode getVisualNodeOfToolTip() {
        return this.visualNodeOfToolTip;
    }
    
    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     *
     * @param e mouse event information.
     */
    @Override public void mouseMoved(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double point = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                                                                         .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                                                                                                                          .screenToPanel(e.getY()));
        double diffX = point.getX() - this.cursorPosition.getX();
        double diffY = point.getY() - this.cursorPosition.getY();
        this.cursorPosition.setLocation(point);
        this.networkEditorPanel.getEditionMode().mouseMoved(e, point, diffX, diffY, g);
        if (this.visualNodeOfToolTip != this.networkEditorPanel.getVisualNetwork()
                                                               .whatNodeInPosition(this.cursorPosition, g)) {
            
            this.networkEditorPanel.setToolTipText(null);
            //This forces to reset the tooltip "enter" timer when moving between visual elements.
            ToolTipManager.sharedInstance().mousePressed(new MouseEvent(
                    this.networkEditorPanel,
                    MouseEvent.MOUSE_EXITED,
                    System.currentTimeMillis(),
                    0,
                    0, 0,
                    0, false
            ));
            
        }
        this.visualNodeOfToolTip = this.networkEditorPanel.getVisualNetwork()
                                                          .whatNodeInPosition(this.cursorPosition, g);
        if (this.visualNodeOfToolTip instanceof VisualNode visualNode) {
            this.networkEditorPanel.setToolTipText(visualNode.getNode().getComment());
        }
    }
    
    
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        this.networkEditorPanel.getEditionMode().keyPressed(keyEvent);
    }
    
    @Override
    public void keyReleased(KeyEvent keyEvent) {
        this.networkEditorPanel.getEditionMode().keyReleased(keyEvent);
    }
    
    
    @Override
    public void keyTyped(KeyEvent keyEvent) {
        this.networkEditorPanel.getEditionMode().keyTyped(keyEvent);
    }
    
    /**
     * Position of the mouse cursor when it is pressed.
     */
    private final Point2D.Double cursorPosition = new Point2D.Double();
    
    /**
     * Shows contextual menu
     *
     * @param e MouseEvent
     * @param g Graphics2D
     */
    private void showContextualMenu(MouseEvent e, Graphics2D g) {
        VisualNetwork visualNetwork = this.networkEditorPanel.getVisualNetwork();
        VisualElement selectedElement = visualNetwork
                .getElementInPosition(this.cursorPosition, g);
        ContextualMenu contextualMenu;
        if (selectedElement != null) {
            contextualMenu = this.getContextualMenu(selectedElement, this.networkEditorPanel);
            if (!visualNetwork.isSelected(selectedElement)) {
                visualNetwork.setSelectedAllObjects(false);
            }
            visualNetwork.setSelectionOfElement(selectedElement, true);
        } else {
            boolean canBeExpanded = this.networkEditorPanel.getNetworkEditorPanel()
                                                           .getProbNet()
                                                           .thereAreTemporalNodes();
            contextualMenu = this.contextualMenuFactory.getNetworkContextualMenu(canBeExpanded);
        }
        contextualMenu.show(this.networkEditorPanel, e.getX(), e.getY());
    }
    
    /**
     * Object that creates the contextual menus.
     */
    private ContextualMenuFactory contextualMenuFactory = null;
    
    
    void setContextualMenuFactory(ContextualMenuFactory contextualMenuFactory) {
        this.contextualMenuFactory = contextualMenuFactory;
    }
    
    /**
     * Retrieves the contextual menu that corresponds to the selectedElement.
     *
     * @return the contextual menu corresponding the the parameter.
     */
    private @Nullable ContextualMenu getContextualMenu(VisualElement selectedElement, NetworkEditorPanel panel) {
        return Optional.ofNullable(this.contextualMenuFactory)
                       .map(menuFactory -> menuFactory.getContextualMenu(selectedElement, panel))
                       .orElse(null);
    }
    
    @Override public void focusGained(FocusEvent e) {
        this.networkEditorPanel.getEditionMode().focusGained(e);
    }
    
    @Override public void focusLost(FocusEvent e) {
        this.networkEditorPanel.getEditionMode().focusLost(e);
    }
}
