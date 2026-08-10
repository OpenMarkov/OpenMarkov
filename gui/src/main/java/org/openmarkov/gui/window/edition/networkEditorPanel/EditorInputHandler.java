package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.DefaultStates;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.configuration.KeyTracker;
import org.openmarkov.gui.dialog.node.NodeTypeInfo;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.exception.PreResolutionNodeInInferenceException;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.graphic.VisualState;
import org.openmarkov.gui.loader.element.CursorLoader;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenu;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.GUIDefaultStates;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles all mouse and keyboard input for the {@link NetworkEditorPanel},
 * delegating to the current {@link EditionMode} and managing contextual menus.
 */
public class EditorInputHandler implements MouseListener, MouseMotionListener, KeyListener, FocusListener {

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
    @Override
    public void mouseClicked(MouseEvent e) {
        this.networkEditorPanel.requestFocus();
    }

    private int lastClickCount = 0;
    private boolean lastLeftClickProducedANode = false;


    /**
     * Invoked when a mouse button has been pressed on the component.
     *
     * @param e mouse event information.
     */
    @Override
    public void mousePressed(MouseEvent e) {
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
        var oldNodesCount = this.networkEditorPanel.getNetworkEditorPanel().getProbNet().getNodes().size();
        this.currentlyHoldingMouse = true;
        if (this.selectionState == SelectionState.NOTHING && SwingUtilities.isLeftMouseButton(e)) {
            if (e.isControlDown() || e.isShiftDown()) {
                this.networkEditorPanel.getVisualNetwork().addToSelection(this.cursorPosition, g);
            } else {
                if (this.networkEditorPanel.getVisualNetwork().selectElementInPosition(this.cursorPosition, g) == null) {
                    this.networkEditorPanel.getVisualNetwork().startSelectionRectangle(this.cursorPosition);
                    this.setSelectionState(SelectionState.SELECTING);
                }
            }
        }
        if (e.getClickCount() == 1 && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.NODE
                && SwingUtilities.isLeftMouseButton(e) && GUIUtils.noMouseModifiers(e) && networkEditorPanel.getVisualNetwork().getElementInPosition(this.cursorPosition, g) == null
        ) {
            createNode(this.networkEditorPanel.getProbNet(), this.networkEditorPanel.getPreferredNodeToCreate(), this.cursorPosition, networkEditorPanel);
            this.lastLeftClickProducedANode = true;
            return;
        }
        if (e.getClickCount() == 1 && this.networkEditorPanel.getBaseTool() == NetworkEditorPanel.BaseTool.LINK && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && GUIUtils.noMouseModifiers(e)) {
            var node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(cursorPosition, g);
            if (node == null) {
                return;
            }
            if (!this.networkEditorPanel.getVisualNetwork().getSelectedNodes().contains(node)) {
                this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
                this.networkEditorPanel.getVisualNetwork().setSelectionOfElement(node, true);
            }
            ;
            this.networkEditorPanel.getVisualNetwork().startLinkCreation(cursorPosition, g, VisualNetwork.LinkCreationSourceDirection.PARENT, false, this.networkEditorPanel.getVisualNetwork().getSelectedNodes());
            this.setSelectionState(SelectionState.CREATING_LINK);
            return;
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
        this.currentlyHoldingMouse = false;
        switch (this.selectionState) {
            case SelectionState.NOTHING -> {
            }
            case SelectionState.MOVING -> {
                try {
                    this.tryFinishNodesMovements();
                } catch (DoEditException ex1) {
                    throw new UnreachableException(ex1);
                }
            }
            case SelectionState.SELECTING ->
                    this.networkEditorPanel.getVisualNetwork().finishSelectionRectangle(this.cursorPosition);
            case CREATING_LINK -> this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
        }
        this.setSelectionState(SelectionState.NOTHING);
        this.networkEditorPanel.repaint();


        switch (this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode()) {
            case EDITION -> {
                // If we are in Edition Mode a double click might open the corresponding properties dialog (for node or link)

                VisualLink link = this.networkEditorPanel.getVisualNetwork().whatLinkInPosition(this.cursorPosition, g);
                if (link != null) {
                    this.networkEditorPanel.changeLinkProperties(link);
                } else {
                    node = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g);
                    if (node == null) {
                        createNode(this.networkEditorPanel.getProbNet(), NodeType.CHANCE, this.cursorPosition, networkEditorPanel);
                        node = this.networkEditorPanel.getVisualNetwork()
                                .whatNodeInPosition(this.cursorPosition, g);
                        this.lastLeftClickProducedANode = true;
                    }
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
                    } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
                             NotEnoughMemoryException |
                             IncompatibleEvidenceException | ConstraintViolatedException |
                             NotSupportedOperationException |
                             CannotNormalizePotentialException ex) {
                        this.networkEditorPanel.repaint();
                        throw new UnrecoverableException(ex);
                    }
                }
                this.networkEditorPanel.repaint();
            }
            case INFERENCE -> {
                VisualState visualState = this.networkEditorPanel.getVisualNetwork()
                        .whatStateInPosition(this.cursorPosition, g);
                if (visualState == null) {
                    if ((this.networkEditorPanel.getVisualNetwork()
                            .whatNodeInPosition(this.cursorPosition, g) != null) && (
                            this.networkEditorPanel.getVisualNetwork()
                                    .whatInnerBoxInPosition(this.cursorPosition, g) == null
                    )) {
                        try {
                            this.networkEditorPanel.changeNodeProperties();
                        } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
                                 NotEnoughMemoryException |
                                 IncompatibleEvidenceException | ConstraintViolatedException |
                                 NotSupportedOperationException |
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

                VisualNode visualNode = this.networkEditorPanel.getVisualNetwork()
                        .whatNodeInPosition(this.cursorPosition, g);
                if (visualNode.isPreResolutionFinding()) {
                    throw new UnrecoverableException(new PreResolutionNodeInInferenceException(visualNode));
                }
                try {
                    this.networkEditorPanel.getEvidenceManager().toggleFinding(visualNode, visualState);
                } catch (IncompatibleEvidenceException | NotEvaluableNetworkException |
                         NonProjectablePotentialException |
                         NotEnoughMemoryException | DoEditException | CannotNormalizePotentialException |
                         ConstraintViolatedException ex) {
                    throw new UnreachableException(ex);
                }

            }
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     *
     * @param e mouse event information.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double point = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                .screenToPanel(e.getY()));
        double diffX = point.getX() - this.cursorPosition.getX();
        double diffY = point.getY() - this.cursorPosition.getY();
        this.cursorPosition.setLocation(point);
        this.lastMousePos = point;

        if (this.selectionState == SelectionState.SELECTING) {
            this.networkEditorPanel.getVisualNetwork().updateSelectionRectangle(diffX, diffY, g);
        } else if (this.selectionState == SelectionState.CREATING_LINK) {
            this.networkEditorPanel.getVisualNetwork().updateLinkCreation(point, g);
            this.networkEditorPanel.repaint();
        } else if (this.selectionState == SelectionState.MOVING ||
                (this.selectionState == SelectionState.NOTHING && SwingUtilities.isLeftMouseButton(e) && !this.networkEditorPanel.getVisualNetwork().getSelectedNodes()
                        .isEmpty())) {
            this.setSelectionState(SelectionState.MOVING);
            this.networkEditorPanel.getVisualNetwork().moveSelectedElements(diffX, diffY);
        }
        this.networkEditorPanel.repaint();
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     *
     * @param e mouse event information.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double point = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                .screenToPanel(e.getY()));
        double diffX = point.getX() - this.cursorPosition.getX();
        double diffY = point.getY() - this.cursorPosition.getY();
        this.cursorPosition.setLocation(point);
        this.lastMousePos = point;
        if (this.selectionState == SelectionState.SELECTING) {
            this.networkEditorPanel.getVisualNetwork().updateSelectionRectangle(diffX, diffY, g);
        } else if (this.selectionState == SelectionState.CREATING_LINK) {
            this.networkEditorPanel.getVisualNetwork().updateLinkCreation(point, g);
            this.networkEditorPanel.repaint();
        } else if (this.selectionState == SelectionState.MOVING ||
                (this.selectionState == SelectionState.NOTHING && SwingUtilities.isLeftMouseButton(e) && !this.networkEditorPanel.getVisualNetwork().getSelectedNodes()
                        .isEmpty())) {
            this.setSelectionState(SelectionState.MOVING);
            this.networkEditorPanel.getVisualNetwork().moveSelectedElements(diffX, diffY);
        }
        this.networkEditorPanel.repaint();
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
        switch (this.selectionState) {
            case NOTHING -> {
                switch (this.networkEditorPanel.getBaseTool()) {
                    case SELECTION -> {
                    }
                    case LINK -> {
                        this.networkEditorPanel.setCursor(CursorLoader.CURSOR_LINK.get());
                    }
                    case NODE -> {
                        boolean wouldCreateANode = this.networkEditorPanel.getVisualNetwork().whatNodeInPosition(this.cursorPosition, g) == null
                                && this.networkEditorPanel.getVisualNetwork().whatLinkInPosition(this.cursorPosition, g) == null;
                        Cursor cursor = null;
                        if (wouldCreateANode) {
                            cursor = NodeTypeInfo.of(this.networkEditorPanel.getPreferredNodeToCreate()).cursor.get();
                        }
                        this.networkEditorPanel.setCursor(cursor);
                    }
                }
            }
            case MOVING, CREATING_LINK, SELECTING -> {
            }
        }

    }

    /**
     * Invoked when a mouse button has been released on the component.
     *
     * @param e mouse event information.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        Graphics2D g = (Graphics2D) this.networkEditorPanel.getGraphics();
        Point2D.Double position = new Point2D.Double(this.networkEditorPanel.getZoomManager()
                .screenToPanel(e.getX()), this.networkEditorPanel.getZoomManager()
                .screenToPanel(e.getY()));
        try {
            boolean finished = false;
            this.currentlyHoldingMouse = false;
            switch (this.selectionState) {
                case SelectionState.NOTHING -> {
                }
                case SelectionState.MOVING -> this.tryFinishNodesMovements();
                case SelectionState.SELECTING ->
                        this.networkEditorPanel.getVisualNetwork().finishSelectionRectangle(position);
                case CREATING_LINK -> {
                    this.networkEditorPanel.getVisualNetwork().finishLinkCreation(position, g);
                    if (this.currentlyHeldKeys.contains(KeyEvent.VK_SHIFT)) {
                        this.networkEditorPanel.getVisualNetwork().startLinkCreation(position, g, VisualNetwork.LinkCreationSourceDirection.PARENT, true, this.networkEditorPanel.getVisualNetwork().getSelectedNodes());
                        this.networkEditorPanel.repaint();
                        finished = true;
                    }
                }
            }
            if (!finished) {
                this.setSelectionState(SelectionState.NOTHING);
            }
        } catch (DoEditException ex) {
            throw new UnrecoverableException(ex);
        } finally {
            this.networkEditorPanel.repaint();
        }
    }

    /**
     * Invoked when the mouse button enters the component.
     *
     * @param e mouse event information.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse button exits the component.
     *
     * @param e mouse event information.
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }


    @Override
    public void keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        onPresses(keyEvent, keyCode);
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        onReleases(keyEvent.getKeyCode());
    }


    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    private void onPresses(@Nullable KeyEvent e, int keyCode) {
        this.currentlyHeldKeys.add(keyCode);
        KeyTracker.isHeld(keyCode);

        if (this.networkEditorPanel.getVisualNetwork().getSelectedNodes().isEmpty()) {
            return;
        }
        switch (this.selectionState) {
            case MOVING -> applyKeyArrowsOnNodes();
            case NOTHING -> {
                switch (keyCode) {
                    case KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT ->
                            applyKeyArrowsOnNodes();
                    case KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL -> {
                        if (this.currentlyHeldKeys.contains(KeyEvent.VK_SHIFT) && this.currentlyHeldKeys.contains(KeyEvent.VK_CONTROL)) {
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
                        this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                        this.setSelectionState(SelectionState.NOTHING);
                    }
                    case KeyEvent.VK_ALT -> {
                        this.networkEditorPanel.getVisualNetwork().toggleLinkCreationSource(this.lastMousePos);
                        if (e != null) {
                            e.consume();
                        }
                    }
                }
            }
        }
        this.networkEditorPanel.repaint();
        if (keyCode == KeyEvent.VK_ALT) {
            e.consume();
        }
    }

    private void onReleases(int keyCode) {
        switch (this.selectionState) {
            case NOTHING -> {
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    this.networkEditorPanel.setBaseTool(NetworkEditorPanel.BaseTool.SELECTION);
                }
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
                switch (keyCode) {
                    case KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL -> {
                        if (this.linkCreationStartedWithKey) {
                            this.networkEditorPanel.getVisualNetwork().cancelLinkCreation();
                            this.linkCreationStartedWithKey = false;
                            this.setSelectionState(SelectionState.NOTHING);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        var currentlyHeldKeys1 = KeyTracker.getHeldKeys().boxed()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var removedKeys = this.currentlyHeldKeys.stream().filter(key -> !currentlyHeldKeys1.contains(key)).toList();
        var newlyPressedKeys = currentlyHeldKeys1.stream().filter(key -> !this.currentlyHeldKeys.contains(key)).toList();
        removedKeys.forEach(this::onReleases);
        newlyPressedKeys.forEach(key -> this.onPresses(null, key));
    }

    @Override
    public void focusLost(FocusEvent e) {

    }


    private static final int NODE_SPEED_ON_ARROW_PRESS = 2;

    private SelectionState selectionState = SelectionState.NOTHING;

    private boolean currentlyHoldingMouse;
    private final Set<Integer> currentlyHeldKeys = new HashSet<>();


    public boolean startLinkCreation(Point2D.Double cursorPosition, VisualNetwork.LinkCreationSourceDirection sourceDirection) {
        if (this.networkEditorPanel.getVisualNetwork().getSelectedNodes().isEmpty()) {
            return false;
        }
        this.linkCreationStartedWithKey = false;
        this.networkEditorPanel.getVisualNetwork().startLinkCreation(cursorPosition, (Graphics2D) this.networkEditorPanel.getGraphics(), sourceDirection, false, this.networkEditorPanel.getVisualNetwork().getSelectedNodes());
        this.setSelectionState(SelectionState.CREATING_LINK);
        return true;
    }

    private void tryFinishNodesMovements() throws DoEditException {
        if (this.isMovingNodes()) {
            return;
        }
        List<VisualNode> movedNodes = this.networkEditorPanel.getVisualNetwork().fillVisualNodesSelected();
        new MoveNodeEdit(movedNodes).executeEdit();
        this.networkEditorPanel.adjustPanelDimension();
        this.setSelectionState(SelectionState.NOTHING);
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


    private void applyKeyArrowsOnNodes() {
        int diffX = 0, diffY = 0;
        for (var key : this.currentlyHeldKeys) {
            switch (key) {
                case KeyEvent.VK_UP -> diffY -= EditorInputHandler.NODE_SPEED_ON_ARROW_PRESS;
                case KeyEvent.VK_RIGHT -> diffX += EditorInputHandler.NODE_SPEED_ON_ARROW_PRESS;
                case KeyEvent.VK_DOWN -> diffY += EditorInputHandler.NODE_SPEED_ON_ARROW_PRESS;
                case KeyEvent.VK_LEFT -> diffX -= EditorInputHandler.NODE_SPEED_ON_ARROW_PRESS;
            }
        }
        if (diffX == 0 && diffY == 0) {
            return;
        }
        this.setSelectionState(SelectionState.MOVING);
        this.networkEditorPanel.getVisualNetwork().moveSelectedElements(diffX, diffY);
    }


    private Point2D.Double lastMousePos;
    private boolean linkCreationStartedWithKey;


    private VisualNode visualNodeOfToolTip;

    public VisualNode getVisualNodeOfToolTip() {
        return this.visualNodeOfToolTip;
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
    
    public static void createNode(ProbNet currentNetwork, NodeType nodeType, Point2D.Double position, NetworkEditorPanel networkEditorPanel) {
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
        try {
            currentNetwork.getPNESupport().setWithUndo(true);
            currentNetwork.getPNESupport().openNewSubEditHistory();
            PNEdit addNodeEdit = new AddNodeEdit(currentNetwork, variable, nodeType, position);
            addNodeEdit.executeEdit();
            var visualNode = networkEditorPanel.getVisualNetwork()
                                               .getAllNodes()
                                               .stream()
                                               .filter(node -> node.getNode().getVariable() == variable)
                                               .findFirst()
                                               .get();
            var visualNodeShape = visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics());
            visualNode.setTemporalCoordinateX(visualNode.getTemporalPosition().x - (visualNodeShape.getBounds2D()
                                                                                                   .getWidth() / 2));
            visualNode.setTemporalCoordinateY(visualNode.getTemporalPosition().y - (visualNodeShape.getBounds2D()
                                                                                                   .getHeight() / 2));
            new MoveNodeEdit(List.of(visualNode)).executeEdit();
            currentNetwork.getPNESupport().closeSubEditHistory();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
        
        networkEditorPanel.adjustPanelDimension();
        networkEditorPanel.repaint();
    }
}
