package org.openmarkov.gui.window.edition.networkEditorPanel;/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */


import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.core.*;
import org.openmarkov.core.exception.*;

import org.openmarkov.core.inference.tasks.OptimalPolicies;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.gui.action.AutoArrangeEdit;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.layout.bayesian.StressLayout;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.inference.temporalevolution.TemporalEvolutionDialog;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.gui.dialog.node.*;
import org.openmarkov.gui.exception.*;
import org.openmarkov.gui.graphic.*;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.MainPanelMenuAssistant;
import org.openmarkov.gui.window.EditorPanel;
import org.openmarkov.gui.window.decisiontree.DecisionTreeEditor;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;
import org.openmarkov.gui.window.edition.ZoomManager;
import org.openmarkov.gui.window.edition.mode.EditionMode;
import org.openmarkov.gui.window.edition.mode.EditionModeManager;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEExpectedUtilityDecision;
import org.openmarkov.java.swing.PointUtils;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class implements the behaviour of a panel where a network will be
 * edited.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.3 - asaez - Functionality added: - Explanation capabilities, -
 * Management of working modes (edition/inference), - Expansion and
 * contraction of nodes, - Introduction and elimination of evidence -
 * Management of multiple evidence cases.
 */
public final class NetworkEditorPanel extends EditorPanel implements PNEditListener {
    /**
     * Static field for serializable class.
     */
    @Serial
    private static final long serialVersionUID = 2789011585460326400L;
    /**
     * Maximum width of the panel.
     */
    private static final double MAX_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 20;
    /**
     * Maximum height of the panel.
     */
    private static final double MAX_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 20;
    /**
     * Constant that indicates the value of the Expansion Threshold by default.
     */
    // This should be in a future a configuration option that should be read on
    // start
    private static final int DEFAULT_THRESHOLD_VALUE = 5;
    
    
    private final EditorInputHandler editorInputHandler;
    private final EvidenceManager evidenceManager;
    private final InferencePresenter inferencePresenter;
    
    /**
     * Object to convert coordinates of the screen to the panel and vice versa.
     */
    private final ZoomManager zoomManager;
    /**
     * Visual representation of the network
     */
    private final VisualNetwork visualNetwork;
    /**
     * Maximum width of the panel.
     */
    private double currentWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 20;
    /**
     * Maximum height of the panel.
     */
    private double currentHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 20;
    /**
     * Current edition mode.
     */
    private EditionMode editionMode = null;
    /**
     * This variable indicates which is the expansion threshold of the network
     */
    private double currentExpansionThreshold = NetworkEditorPanel.DEFAULT_THRESHOLD_VALUE;
    
    /**
     * This variable indicates if the propagation mode is automatic or manual.
     */
    private boolean automaticPropagation;
    /**
     * This variable indicates if propagation should be done right now (if being
     * in Inference Mode).
     */
    private boolean propagationActive;
    /**
     * Object that assists this panel in the operations with the clipboard.
     */
    private static final EditorPanelClipboardAssistant CLIPBOARD_ASSISTANT = new EditorPanelClipboardAssistant();
    
    private final EditionModeManager editionModeManager;
    
    /**
     * Constructor that creates the instance.
     *
     * @param probNet   network that will be edited.
     * @param mainPanel application main panel.
     */
    public NetworkEditorPanel(ProbNet probNet, MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.onModificationListener = new ArrayList<>();
        probNet.getPNESupport().addListener(this);
        this.zoomManager = new ZoomManager();
        this.visualNetwork = new VisualNetwork(probNet, this);
        this.evidenceManager = new EvidenceManager(this);
        this.visualNetwork.getProbNet().getPNESupport().addListener(new PNEditEventHandler(this));
        this.automaticPropagation = true;
        this.propagationActive = true;
        this.editorInputHandler = new EditorInputHandler(this);
        this.addMouseListener(this.editorInputHandler);
        this.addMouseMotionListener(this.editorInputHandler);
        this.addKeyListener(this.editorInputHandler);
        this.addFocusListener(this.editorInputHandler);
        this.setZoomToFitNetwork();
        this.editionModeManager = new EditionModeManager(this, this.visualNetwork.getProbNet());
        this.editionMode = this.editionModeManager.getDefaultEditionMode();
        this.inferencePresenter = new InferencePresenter(this);
        this.setLayout(new BorderLayout());
        this.scrollPanel.setViewportView(this);
        this.scrollPanel.getVerticalScrollBar().setUnitIncrement(25);
        decisionTreeEditors = new ArrayList<>();
    }
    
    
    @Override
    public void updateUI() {
        super.updateUI();
        this.setBackground(GUIColors.Network.BACKGROUND.getColor());
    }
    
    public double getCurrentWidth() {
        return this.currentWidth;
    }
    
    public double getCurrentHeight() {
        return this.currentHeight;
    }
    
    /**
     * Changes the presentation mode of the foreground of the nodes.
     *
     * @param value new value of the presentation mode of the foreground of the nodes.
     */
    public void setByTitle(boolean value) {
        this.visualNetwork.setByTitle(value);
        this.readjustAndRepaint();
    }
    
    @Override protected void doPaint(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.scale(this.zoomManager.getZoom(), this.zoomManager.getZoom());
        for (VisualLink visualLink : this.visualNetwork.getVisualLinks()) {
            visualLink.paint(graphics2D);
        }
        this.visualNetwork.reorderVisualNodes();
        for (int i = (this.visualNetwork.getAllNodes().size() - 1); i >= 0; i--) {
            if (this.visualNetwork.getAllNodes().get(i).isVisible()) {
                this.visualNetwork.getAllNodes().get(i).paint(graphics2D);
            }
        }
        this.visualNetwork.getNewLinksArrows().forEach(arrow -> arrow.paint(graphics2D));
        if (this.visualNetwork.getSelection() != null) {
            this.visualNetwork.getSelection().paint(graphics2D);
        }
    }
    
    /**
     * Reader used to read this network.
     */
    private ProbNetReader reader;
    
    /**
     * Writer used to save this network
     */
    private ProbNetWriter writer;
    
    public ProbNetReader getReader() {
        return this.reader;
    }
    
    public void setReader(@Nullable ProbNetReader reader) {
        this.reader = reader;
    }
    
    public ProbNetWriter getWriter() {
        return this.writer;
    }
    
    public void setWriter(@Nullable ProbNetWriter writer) {
        this.writer = writer;
    }
    
    /**
     * Changes the state of the edition and carries out the necessary actions in
     * each case.
     *
     * @param newEditionModeName new edition mode state.
     */
    public void setEditionMode(String newEditionModeName) {
        EditionMode newEditionMode = this.editionModeManager.getEditionMode(newEditionModeName);
        if (!this.editionMode.equals(newEditionMode)) {
            this.setCursor(this.editionModeManager.getCursor(newEditionModeName));
            this.visualNetwork.setSelectedAllObjects(false);
            this.editionMode = newEditionMode;
            this.repaint();
        }
    }
    
    /**
     * Selects all nodes and links.
     */
    public void selectAllObjects() {
        this.visualNetwork.setSelectedAllObjects(true);
        this.repaint();
    }
    
    /**
     * Re-positions every node of the current network using stress-
     * majorization with a directional bias that keeps parents above
     * children (geared towards Bayesian DAGs). Wrapped in a single
     * undoable edit so Ctrl+Z restores the previous layout.
     */
    public void autoArrangeNodes() {
        ProbNet probNet = this.visualNetwork.getProbNet();
        if (probNet == null || probNet.getNodes().isEmpty()) return;
        var positions = new StressLayout().compute(probNet);
        if (positions.isEmpty()) return;
        var minX = positions.values().stream().mapToDouble(Point2D.Double::getX).min().getAsDouble();
        var minY = positions.values().stream().mapToDouble(Point2D.Double::getY).min().getAsDouble();
        if(minX<0){
            positions.values().forEach(position -> position.setX(position.getX()-minX));
        }
        if(minY<0){
            positions.values().forEach(position -> position.setY(position.getY()-minY));
        }
        try {
            new AutoArrangeEdit(probNet, positions).executeEdit();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Return the height of the panel after applying the zoomManager.
     *
     * @return height of the panel after applying the zoomManager.
     */
    private double getNewHeight() {
        return this.zoomManager.panelToScreen(this.currentHeight);
    }
    
    /**
     * Return the width of the panel after applying the zoomManager.
     *
     * @return width of the panel after applying the zoomManager.
     */
    private double getNewWidth() {
        return this.zoomManager.panelToScreen(this.currentWidth);
    }
    
    /**
     * Returns the value of the zoomManager.
     *
     * @return actual value of zoomManager.
     */
    public double getZoom() {
        return this.zoomManager.getZoom();
    }
    
    /**
     * Changes the value of the zoomManager.
     *
     * @param value new zoomManager.
     */
    public void setZoom(double value) {
        if (Double.compare(this.zoomManager.getZoom(), value) != 0) { // jlgozalo. 24/08 fix condition to !=
            this.zoomManager.setZoom(value);
            Dimension newDimension = new Dimension((int) Math.round(this.getNewWidth()), (int) Math.round(this.getNewHeight()));
            this.setPreferredSize(newDimension);
            this.setSize(newDimension);
            this.readjustAndRepaint();
        }
    }
    
    /**
     * Sets a new contextual menu factory.
     *
     * @param newContextualMenuFactory contextual menu factory to be set.
     */
    public void setContextualMenuFactory(ContextualMenuFactory newContextualMenuFactory) {
        this.editorInputHandler.setContextualMenuFactory(newContextualMenuFactory);
    }
    
    
    public Node getSelectedNode() {
        VisualNode selectedNode = this.visualNetwork.getLastSelectedNode();
        if (selectedNode == null) { // This never happens
            throw new UnreachableException(new NoSelectedNodeException(this.visualNetwork));
        }
        return selectedNode.getNode();
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of a node.
     * If some property has changed, insert a new undo point into the network
     * undo manager.
     *
     * @param selectedNode the selected node
     * @param newNode      the new node
     *
     * @return the result
     */
    boolean changeNodeProperties(VisualNode selectedNode, boolean newNode) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        boolean userAcceptedChanges = NetworkEditorPanel.requestNodePropertiesToUser2(GUIUtils.getOwner(this), this, selectedNode, newNode);
        if (userAcceptedChanges) {
            this.adjustPanelDimension();
            selectedNode.updateNumCases(this.evidenceManager.getPostResolutionEvidence().size());
            this.repaint();
            this.evidenceManager.removeNodeEvidenceInAllCases(selectedNode.getNode());
        }
        return userAcceptedChanges;
    }
    
    public void changeNodeProperties() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        var selectedNode = this.visualNetwork.getLastSelectedNode();
        if (selectedNode == null) {
            return;
        }
        this.changeNodeProperties(selectedNode, false);
    }
    
    public void showPotentialDialog(boolean readOnly) throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, ConstraintViolatedException, CannotNormalizePotentialException {
        Node node = this.visualNetwork.getLastSelectedNode().getNode();
        if (this.requestPotentialValues(GUIUtils.getOwner(this), node, readOnly)) {
            // if the user has selected the ok button when closing the dialog
            this.readjustAndRepaint();
            this.evidenceManager.removeNodeEvidenceInAllCases(node);
        }
    }
    
    /**
     * This method requests to the user the additionalProperties of a node.
     *
     * @param owner              owner window that shows the dialog box.
     * @param networkEditorPanel
     * @param node               object that contains the additionalProperties of the node
     *                           and where changes will be saved.
     * @param newNode            specifies if the node whose additionalProperties are going
     *                           to be edited is new.
     *
     * @return true, if the user save the changes on node; otherwise, false.
     */
    private static boolean requestNodePropertiesToUser2(Window owner, NetworkEditorPanel networkEditorPanel, VisualNode node, boolean newNode) {
        NodePropertiesDialog nodePropertiesDialog = new NodePropertiesDialog(owner, networkEditorPanel, node, newNode, networkEditorPanel.workingMode != WorkingMode.EDITION);
        if (owner instanceof MainGUI gui) {
            gui.freeze();
        }
        boolean result = nodePropertiesDialog.requestProperties() == OkCancelDialog.ChosenOption.Ok;
        if (owner instanceof MainGUI gui) {
            gui.unfreeze();
        }
        return result;
    }
    
    private boolean requestPotentialValues(Window owner, Node node, boolean readOnly) {
        /**
         * Object Dialog for potentials edition
         */
        PotentialEditDialog potentialsDialog = new PotentialEditDialog(owner, node, readOnly);
        this.visualNetwork.cancelLinkCreation();
        return (
                potentialsDialog.requestValues()// to know if the user has
                        // selected the ok button when
                        // closing the dialog
                        == OkCancelDialog.ChosenOption.Ok
        );
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of a link.
     * If some property has changed, insert a new undo point into the network
     * undo manager.
     *
     * @param link the link
     */
    void changeLinkProperties(VisualLink link) {
        /*
         * This method must be implemented to activate the possibility of
         * editing the additionalProperties of a link in future versions.
         */
    }
    
    /**
     * This method shows a dialog box with the additionalProperties of the
     * network. If some property has changed, insert a new undo point into the
     * network undo manager.
     */
    public void changeNetworkProperties() {
        // TODO be careful with local pNESupport and extern pNESupport
        Window owner = GUIUtils.getOwner(this);
        NetworkPropertiesDialog dialogProperties = new NetworkPropertiesDialog(owner, this.visualNetwork.getProbNet(), this.workingMode != WorkingMode.EDITION);
        dialogProperties.showProperties();
    }
    
    /**
     * This method imposes a policy in a decision node.
     */
    public void imposePolicyInNode() {
        VisualNode visualNode =  this.visualNetwork.getLastSelectedNode();
        NetworkEditorPanel.requestImposePolicyValues(GUIUtils.getOwner(this), visualNode.getNode());
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    /**
     * This method edits an imposed policy of a decision node.
     */
    public void editNodePolicy() {
        VisualNode visualNode =  this.visualNetwork.getLastSelectedNode();
        NetworkEditorPanel.requestImposePolicyValues(GUIUtils.getOwner(this), visualNode.getNode());
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    /**
     * This method removes an imposed policy from a decision node.
     */
    public void removePolicyFromNode() {
        VisualNode visualNode = this.visualNetwork.getLastSelectedNode();
        try {
            new RemovePolicyEdit(visualNode.getNode()).executeEdit();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
        //setNetworkChangedWithOutEdit(true);
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    private static void requestImposePolicyValues(Window owner, Node node) {
        PotentialEditDialog imposePolicyDialog = new ImposePolicyDialog(owner, false, node);
        imposePolicyDialog.requestValues();
    }
    
    
    /**
     * This method shows the expected utility of a decision node.
     */
    public void showExpectedUtilityOfNode() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        VisualNode visualNode = this.visualNetwork.getLastSelectedNode();
        Node node = visualNode.getNode();
        VEExpectedUtilityDecision veExpectedUtilityDecision
                = new VEExpectedUtilityDecision(this.visualNetwork.getProbNet(), node.getVariable());
        Potential expectedUtility = veExpectedUtilityDecision.getExpectedUtility();
        Node dummyNode = new Node(new ProbNet(), node.getVariable(), node.getNodeType());
        dummyNode.setPotential(expectedUtility);
        PotentialEditDialog expectedUtilityDialog = new PotentialEditDialog(GUIUtils.getOwner(this), dummyNode, true);
        expectedUtilityDialog.setTitle("ExpectedUtilityDialog.Title");
        expectedUtilityDialog.requestValues();
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    /**
     * This method shows the optimal policy for a decision node.
     */
    public void showOptimalPolicyOfNode() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        VisualNode visualNode = this.visualNetwork.getLastSelectedNode();
        ProbNet dummyProbNet = new ProbNet();
        OptimalPolicies veOptimalPolicy = new VEEvaluation(this.visualNetwork.getProbNet());
        Potential optimalPolicy = veOptimalPolicy.getOptimalPolicy(visualNode.getNode().getVariable());
        dummyProbNet.addPotential(optimalPolicy);
        Variable conditionedVariable = optimalPolicy.getVariable(0);
        Node dummyNode = dummyProbNet.getNode(conditionedVariable);
        dummyNode.setNodeType(NodeType.DECISION);
        dummyNode.setPolicyType(PolicyType.OPTIMAL);
        for (Variable variable : optimalPolicy.getVariables()) {
            if (variable.equals(conditionedVariable)) {
                continue;
            }
            dummyProbNet.addLink(variable, conditionedVariable, true);
        }
        PotentialEditDialog optimalPolicyDialog =
                new PotentialEditDialog(GUIUtils.getOwner(this), dummyNode, true);
        optimalPolicyDialog.setTitle("OptimalPolicyDialog.Title");
        optimalPolicyDialog.requestValues();
        this.visualNetwork.setSelectedAllNodes(false);
        this.repaint();
    }
    
    
    /**
     * This method returns true if propagation type currently set is automatic;
     * false if manual.
     *
     * @return true if the current propagation type is automatic.
     */
    public boolean isAutomaticPropagation() {
        return this.automaticPropagation;
    }
    
    /**
     * This method sets the current propagation type.
     *
     * @param automaticPropagation new value of the propagation type.
     */
    public void setAutomaticPropagation(boolean automaticPropagation) {
        this.automaticPropagation = automaticPropagation;
    }
    
    /**
     * This method sets the propagation status.
     *
     * @param propagationActive new value of the propagation status.
     */
    public void setPropagationActive(boolean propagationActive) {
        this.propagationActive = propagationActive;
        this.visualNetwork.setPropagationActive(propagationActive);
    }
    
    /**
     * This method returns the associated network panel.
     *
     * @return the associated network panel.
     */
    public NetworkEditorPanel getNetworkEditorPanel() {
        return this;
    }
    
    /**
     * This method returns the current expansion threshold.
     *
     * @return the value of the current expansion threshold.
     */
    public double getExpansionThreshold() {
        return this.currentExpansionThreshold;
    }
    
    /**
     * This method changes the current expansion threshold.
     *
     * @param expansionThreshold new value of the expansion threshold.
     */
    public void setExpansionThreshold(double expansionThreshold) {
        this.currentExpansionThreshold = expansionThreshold;
    }
    
    /**
     * This method updates the expansion state (expanded/contracted) of the
     * nodes. It is used in transitions from edition to inference mode and vice
     * versa, and also when the user modifies the current expansion threshold in
     * the Inference tool bar
     *
     * @param newWorkingMode new value of the working mode.
     */
    public void updateNodesExpansionState(WorkingMode newWorkingMode) {
        switch (newWorkingMode) {
            case EDITION -> {
                List<VisualNode> allNodes = this.visualNetwork.getAllNodes();
                if (!allNodes.isEmpty()) {
                    for (VisualNode visualNode : allNodes) {
                        if (visualNode.isExpanded()) {
                            visualNode.setExpanded(false);
                        }
                    }
                    this.repaint();
                }
            }
            case INFERENCE -> {
                List<VisualNode> allNodes = this.visualNetwork.getAllNodes();
                if (!allNodes.isEmpty()) {
                    for (VisualNode visualNode : allNodes) {
                        visualNode.setExpanded(visualNode.getNode().getRelevance() >= this.currentExpansionThreshold);
                    }
                    this.repaint();
                }
            }
        }
    }
    
    public void temporalEvolution() {
        List<VisualNode> selectedNode = this.visualNetwork.getSelectedNodes();
        if (selectedNode.size() == 1) {
            VisualNode node = this.visualNetwork.getLastSelectedNode();
            new TemporalEvolutionDialog(GUIUtils.getOwner(this), node.getNode(), this.evidenceManager.getPreResolutionEvidence());
            this.visualNetwork.setSelectedAllNodes(false);
            this.repaint();
            // TODO - Change code
        } else if (selectedNode.isEmpty()) {
            new TemporalEvolutionDialog(GUIUtils.getOwner(this), this
                    .getProbNet(), this.evidenceManager.getPreResolutionEvidence());
        }
    }
    
    
    /**
     * This method updates all visual states of all visual nodes when it is
     * needed for a navigation operation among the existing evidence cases, a
     * creation of a new case or when all cases are cleared out.
     *
     * @param option the specific operation to be done over the visual states.
     */
    public void updateAllVisualStates(String option, int caseNumber) {
        List<VisualNode> allVisualNodes = this.visualNetwork.getAllNodes();
        for (VisualNode visualNode : allVisualNodes) {
            InnerBox innerBox = visualNode.getInnerBox();
            VisualState visualState = null;
            for (int i = 0; i < innerBox.getNumStates(); i++) {
                if (innerBox instanceof FSVariableBox) {
                    visualState = ((FSVariableBox) innerBox).getVisualState(i);
                } else if (innerBox instanceof NumericVariableBox) {
                    visualState = ((NumericVariableBox) innerBox).getVisualState();
                }
                if ("new".equals(option)) {
                    visualState.createNewStateValue();
                } else if ("clear".equals(option)) {
                    visualState.clearAllStateValues();
                }
                visualState.setCurrentStateValue(caseNumber);
            }
        }
        this.repaint();
    }
    
    /**
     * Returns the visualNetwork.
     *
     * @return the visualNetwork.
     */
    public VisualNetwork getVisualNetwork() {
        return this.visualNetwork;
    }
    
    /**
     * Sets workingMode
     *
     * @param newWorkingMode the new working mode
     */
    public void setWorkingMode(WorkingMode newWorkingMode) {
        this.workingMode = newWorkingMode;
        this.visualNetwork.setWorkingMode(newWorkingMode);
        if (newWorkingMode == WorkingMode.INFERENCE) {
            this.editionMode = this.editionModeManager.getDefaultEditionMode();
            this.setCursor(this.editionModeManager.getDefaultCursor());
        }
    }
    
    private static final int EXTRA_PIXELS_SPACE_ON_RIGHT_SIDE = 300;
    private static final int EXTRA_PIXELS_SPACE_ON_BOTTOM_SIDE = 140;
    
    /**
     * If the dimensions of the network are greater than the dimensions of the
     * panel, changes the dimensions of the panel in order to accommodate the
     * whole network.
     */
    public void adjustPanelDimension() {
        double[] bounds = this.visualNetwork.getNetworkBounds((Graphics2D) this.getGraphics());
        this.currentWidth = Math.min(NetworkEditorPanel.MAX_WIDTH, bounds[1]);
        this.currentHeight = Math.min(NetworkEditorPanel.MAX_HEIGHT, bounds[3]);
        Dimension newDimension = new Dimension(
                (int) Math.round(this.getNewWidth()) + NetworkEditorPanel.EXTRA_PIXELS_SPACE_ON_RIGHT_SIDE,
                (int) Math.round(this.getNewHeight()) + NetworkEditorPanel.EXTRA_PIXELS_SPACE_ON_BOTTOM_SIDE
        );
        
        this.setPreferredSize(newDimension);
        this.setSize(newDimension);
    }
    
    /**
     * Sets the zoomManager so the displayed network fits in the panel.
     */
    private void setZoomToFitNetwork() {
        double[] networkBounds = this.visualNetwork.getNetworkBounds((Graphics2D) this.getGraphics());
        Dimension panelBounds = this.getMainPanel().getNetworksTabPanel().getSize();
        double zoom = 1;
        
        while (((networkBounds[1] * zoom) > panelBounds.getWidth())
                || ((networkBounds[3] * zoom) > panelBounds.getHeight()) && zoom > 0.1) {
            zoom -= 0.1;
        }
        this.setZoom(zoom);
    }
    
    // The key listener needs a focusable object to listen
    @Override
    public boolean isFocusable() {
        return true;
    }
    
    public EvidenceManager getEvidenceManager() {
        return this.evidenceManager;
    }
    
    public void readjustAndRepaint() {
        this.adjustPanelDimension();
        this.revalidate();
        this.repaint();
    }
    
    public InferencePresenter getInferencePresenter() {
        return inferencePresenter;
    }
    
    public ZoomManager getZoomManager() {
        return this.zoomManager;
    }
    
    public EditionMode getEditionMode() {
        return this.editionMode;
    }
    
    public void updateName(String baseName) {
        this.setName("NetworkEditorOf" + baseName);
    }
    
    /**
     * Application main
     */
    private final MainPanel mainPanel;
    /**
     * Name of the file where the network is saved (updated or not).
     */
    private String networkFile = null;
    
    /**
     * Indicates if the network has been modified.
     */
    private boolean modified = false;
    /**
     * This variable indicates in which mode is the network currently working It
     * is initially set to Edition Mode
     */
    private WorkingMode workingMode = WorkingMode.EDITION;
    
    private final ArrayList<DecisionTreeEditor> decisionTreeEditors;
    
    public enum WorkingMode {
        EDITION, INFERENCE
    }
    
    private final List<Consumer<NetworkEditorPanel>> onModificationListener;
    
    /**
     * This method initializes this.
     *
     * @return a new editor panel.
     */
    public NetworkEditorPanel getEditorPanel() {
        return this;
    }
    
    /**
     * Returns the network which is edited.
     *
     * @return network which is edited.
     */
    public ProbNet getProbNet() {
        return visualNetwork.getProbNet();
    }
    
    /**
     * Returns the application main panel.
     *
     * @return the application main panel.
     */
    public MainPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Returns the modification state of the network.
     *
     * @return true if the network has been modified; otherwise, false.
     */
    public boolean getModified() {
        return modified;
    }
    
    public void addOnModification(Consumer<NetworkEditorPanel> action) {
        this.onModificationListener.add(action);
    }
    
    /**
     * Sets the modification state of the network to a new value.
     *
     * @param value new value of the modification state of the network.
     */
    public void setModified(boolean value) {
        modified = value;
        for (Consumer<NetworkEditorPanel> onModification : this.onModificationListener) {
            onModification.accept(this);
        }
    }
    
    public void onSave() {
        this.getProbNet().getPNESupport().onSave();
        modified = false;
        for (Consumer<NetworkEditorPanel> onModification : this.onModificationListener) {
            onModification.accept(this);
        }
    }
    
    /**
     * Returns the name of the file where the network is saved.
     *
     * @return a string that contains the name of the file.
     */
    public String getNetworkFile() {
        return networkFile;
    }
    
    
    /**
     * Sets the name of the file where the network is saved.
     *
     * @param name name of the file.
     */
    public void setNetworkFile(String name) {
        networkFile = name;
    }
    
    /**
     * Returns the current working mode.
     *
     * @return the value of the current working mode (Edition or Inference).
     */
    public WorkingMode getWorkingMode() {
        return workingMode;
    }
    
    @Override
    public JToolTip createToolTip() {
        JToolTip customTip = new JToolTip();
        if(this.getToolTipText()==null || this.getToolTipText().isBlank()){
            return customTip;
        }
        customTip.addAncestorListener(new AncestorListener() {
            private int originalDismissDelay;
            
            @Override
            public void ancestorAdded(AncestorEvent event) {
                originalDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
                ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            }
            
            @Override
            public void ancestorRemoved(AncestorEvent event) {
                ToolTipManager.sharedInstance().setDismissDelay(originalDismissDelay);
            }
            
            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        
        JEditorPane htmlPane = new JEditorPane();
        htmlPane.setContentType("text/html");
        htmlPane.setText(this.getToolTipText());
        htmlPane.setEditable(false);
        htmlPane.setBackground(customTip.getBackground());
        
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        
        customTip.setLayout(new BorderLayout());
        customTip.add(scrollPane, BorderLayout.CENTER);
        customTip.setPreferredSize(new Dimension(
                (int) Math.min(500, htmlPane.getPreferredSize().getWidth()+22),
                (int) Math.min(250, htmlPane.getPreferredSize().getHeight()+12)
        ));
        return customTip;

    }
    
    @Override public Point getToolTipLocation(MouseEvent event) {
        if (this.editorInputHandler.getVisualNodeOfToolTip() instanceof VisualNode visualNodeOfToolTip) {
            JToolTip tempTip = this.createToolTip();
            tempTip.setTipText(this.getToolTipText(event));
            Dimension size = tempTip.getPreferredSize();
            Rectangle2D nodeBounds = visualNodeOfToolTip.getShape((Graphics2D) this.getGraphics()).getBounds2D();
            Point thisLocation = this.getLocationOnScreen();
            Point nodeLocation = new Point(
                    (int) ((nodeBounds.getX() / 2 + nodeBounds.getWidth() / 3.4) * this.getZoomManager().getZoom()),
                    (int) ((nodeBounds.getY() / 2 + nodeBounds.getHeight() / 2) * this.getZoomManager().getZoom())
            );
            return PointUtils.sumPoints(
                    thisLocation,
                    nodeLocation,
                    new Point((int) (-size.width / 3.65), 0)
            );
        }
        
        return super.getToolTipLocation(event);
    }
    
    /**
     * This method absorbs a node into the rest of the net arc-reversal style. This means updating the only utility
     * child it might have and removing it next.
     */
    public void absorbNode() throws DoEditException {
        Node node = this.getSelectedNode();
        new AbsorbNodeEdit(this.getVisualNetwork().getProbNet(), node.getVariable()).executeEdit();
    }
    
    /**
     * This method absorbs intermediate utility nodes.
     */
    public void absorbParents() throws DoEditException {
        Node node = this.getSelectedNode();
        new AbsorbParentsEdit(this.getVisualNetwork().getProbNet(), node).executeEdit();
    }
    
    /**
     * This method has been created for testing.
     */
    public void changePotential() throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.showPotentialDialog(workingMode != WorkingMode.EDITION);
    }
    
    /**
     * This method adds a finding in a node.
     */
    public void addFinding() {
        this.getEvidenceManager().addFinding();
    }
    
    /**
     * This method removes findings from selected nodes.
     */
    public void removeFinding() throws PreResolutionNodeInInferenceException, DoEditException {
        this.getEvidenceManager().removeFinding();
    }
    
    /**
     * This method updates the value of each state for each node in the network
     * with the current individual probabilities.
     */
    public void updateIndividualProbabilitiesAndUtilities() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().updateIndividualProbabilitiesAndUtilities();
    }
    
    /**
     * This method removes all the findings established in the current evidence
     * case.
     */
    public void removeAllFindings() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().removeAllFindings();
    }
    
    /**
     * This method returns true if there are any finding in the current evidence
     * case.
     *
     * @return true if the current evidence case has at least one finding.
     */
    public boolean areThereFindingsInCase() {
        return this.getEvidenceManager().areThereFindingsInCase();
    }
    
    /**
     * This method copies the selected nodes to the clipboard.
     *
     * @param cut if true, the nodes copied to the clipboard are also removed.
     */
    public void exportToClipboard(boolean cut) {
        this.getVisualNetwork().exportToClipboard(cut, NetworkEditorPanel.CLIPBOARD_ASSISTANT);
    }
    
    
    public EditorPanelClipboardAssistant getClipboardAssistant() {
        return NetworkEditorPanel.CLIPBOARD_ASSISTANT;
    }
    
    /**
     * This method imports the content from the clipboard and creates it in the
     * network.
     */
    public void pasteFromClipboard(Point2D.Double centerNodesTo) throws DoEditException {
        if (!NetworkEditorPanel.CLIPBOARD_ASSISTANT.isThereDataStored() || this.getWorkingMode() != WorkingMode.EDITION) {
            return;
        }
        new PasteEdit(this.getProbNet(), NetworkEditorPanel.CLIPBOARD_ASSISTANT.paste(), centerNodesTo).executeEdit();
    }
    
    public boolean hasPasteContents() {
        return this.getClipboardAssistant().isThereDataStored();
    }
    
    /**
     * This method says if there is data stored in the clipboard.
     *
     * @return true if there is data stored in the clipboard; otherwise, false.
     */
    public boolean isThereDataStored() {
        return NetworkEditorPanel.CLIPBOARD_ASSISTANT.isThereDataStored();
    }
    
    /**
     * This method removes the selected objects. First removes the selected
     * links and then removes the selected nodes. Also notifies that there
     * aren't selected elements and creates a new undo point.
     */
    public void removeSelectedObjects() {
        this.getVisualNetwork().removeSelectedObjects();
    }
    
    /**
     * Returns the presentation mode of the foreground of the nodes.
     *
     * @return true if the title of the nodes is the name or false if it is the
     * name.
     */
    public boolean getByTitle() {
        return this.getVisualNetwork().getByTitle();
    }
    
    /**
     * Selects or deselects all nodes of the network.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllNodes(boolean selected) {
        this.getVisualNetwork().setSelectedAllNodes(selected);
    }
    
    /**
     * Selects or deselects all objects of the network.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllObjects(boolean selected) {
        this.getVisualNetwork().setSelectedAllObjects(selected);
    }
    
    @Override public void afterEditExecutes(PNEdit edit) {
        this.repaint();
        this.setModified(this.getProbNet().getPNESupport().networkIsModified());
    }
    
    @Override public void beforeEditExecutes(PNEdit edit) {
        this.repaint();
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        this.repaint();
        this.setModified(this.getProbNet().getPNESupport().networkIsModified());
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        this.repaint();
        this.setModified(this.getProbNet().getPNESupport().networkIsModified());
    }
    
    /**
     * This method returns the number of the current Evidence Case.
     *
     * @return the number of the current Evidence Case.
     */
    public int getCurrentCase() {
        return this.getEvidenceManager().getCurrentCase();
    }
    
    /**
     * This method returns the number of Evidence Cases that the ArrayList is
     * currently holding .
     *
     * @return the number of Evidence Cases in the ArrayList.
     */
    public int getNumberOfCases() {
        return this.getEvidenceManager().getNumberOfCases();
    }
    
    /**
     * This method creates a new evidence case
     */
    public void createNewEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().createNewEvidenceCase();
    }
    
    /**
     * This method makes the first evidence case to be the current
     */
    public void goToFirstEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().goToFirstEvidenceCase();
    }
    
    /**
     * This method makes the previous evidence case to be the current
     */
    public void goToPreviousEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().goToPreviousEvidenceCase();
    }
    
    /**
     * This method makes the next evidence case to be the current
     */
    public void goToNextEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().goToNextEvidenceCase();
    }
    
    /**
     * This method makes the last evidence case to be the current
     */
    public void goToLastEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().goToLastEvidenceCase();
    }
    
    /**
     * This method clears out all the evidence cases. It returns to an 'initial
     * state' in which there is only an initial evidence case with no findings
     * (corresponding to prior probabilities)
     */
    public void clearOutAllEvidenceCases() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().clearOutAllEvidenceCases();
    }
    
    /**
     * This method does the propagation of the evidence for all the evidence
     * cases in memory.
     *
     * @param mainPanelMenuAssistant the menu assistant associated to the main
     *                               panel.
     */
    public void propagateEvidence(MainPanelMenuAssistant mainPanelMenuAssistant) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.getEvidenceManager().propagateEvidence(mainPanelMenuAssistant);
    }
    
    /**
     * This method returns the propagation status: true if propagation should be
     * done right now; false otherwise.
     *
     * @return true if propagation should be done right now.
     */
    public boolean isPropagationActive() {
        return this.propagationActive;
    }
    
    private final ArrayList<Consumer<NetworkEditorPanel>> onNetworkClose = new ArrayList<>();
    
    public void onNetworkClose(Consumer<NetworkEditorPanel> onNetworkClose) {
        this.onNetworkClose.add(onNetworkClose);
    }
    
    @Override public boolean close() {
        try {
            if (!MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().networkCanBeClosed(this)) {
                return false;
            }
        } catch (WriterException e) {
            throw new UnrecoverableException(e);
        }
        boolean close = super.close();
        if (close) {
            new ArrayList<>(this.decisionTreeEditors).forEach(DecisionTreeEditor::close);
            this.onNetworkClose.forEach(action -> action.accept(this));
        }
        return close;
    }
    
    // TODO OOPN end
    
    public void addDecisionTreeWindows(DecisionTreeEditor decisionTreeWindows) {
        this.decisionTreeEditors.add(decisionTreeWindows);
    }
    
    public void removeDecisionTreeWindows(DecisionTreeEditor decisionTreeWindows) {
        this.decisionTreeEditors.remove(decisionTreeWindows);
    }
    
}
