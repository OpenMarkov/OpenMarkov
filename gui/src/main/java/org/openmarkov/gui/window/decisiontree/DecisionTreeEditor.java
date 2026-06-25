/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.decisiontree;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.operation.DecisionTreeManager;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.costeffectiveness.CEPDialog;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.menutoolbar.menu.TreeContextualMenu;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.util.TreeNodeToDot;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeBranchPanel;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeElementPanel;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeNodePanel;
import org.openmarkov.gui.window.edition.ZoomManager;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.EditorPanel;
import org.openmarkov.inference.decisiontree.operation.DecisionTreeManagerImpl;
import org.openmarkov.java.swing.MouseListenerUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


@SuppressWarnings("serial")
public class DecisionTreeEditor extends EditorPanel {
    
    /** The source network panel associated with this tree window. */
    private final NetworkEditorPanel networkPanel;
    
    /** The visual tree component. */
    private final JTree jTree;
    
    /** Factory for creating contextual menus based on node types. */
    private final ContextualMenuFactory contextualMenuFactory;
    
    /** Listener for mouse and action events. */
    private final TreePanelListener listener;
    
    private final DecisionTreeManager decisionTreeManager;
    
    /**
     * Object to convert coordinates of the screen to the panel and vice versa.
     */
    private final ZoomManager zoomManager;
    
    /**
     * Creates a new window to display the decision tree derived from a network panel.
     *
     * @param networkPanel The panel containing the probabilistic network.
     *
     * @throws IncompatibleEvidenceException                                   if the evidence is incompatible with the network
     * @throws NotEvaluableNetworkException                                    If
     * @throws NonProjectablePotentialException                                if the potential cannot be projected
     * @throws PotentialOperationException.DifferentSizesInPotentialsAndStates if different sizes in potentials and states occurs
     */
    public DecisionTreeEditor(NetworkEditorPanel networkPanel) throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        super();
        this.setLayout(new BorderLayout());
        getScrollPanel().getVerticalScrollBar().setUnitIncrement(20);
        getScrollPanel().getHorizontalScrollBar().setUnitIncrement(20);
        
        this.networkPanel = networkPanel;
        this.listener = new TreePanelListener();
        this.contextualMenuFactory = new ContextualMenuFactory(this.listener);
        this.zoomManager = new ZoomManager();
        this.decisionTreeManager = new DecisionTreeManagerImpl();
        
        DecisionTreeElement root = buildDecisionTree(networkPanel.getProbNet());
        
        this.jTree = new JTree(new DecisionTreeModel(root)) {
            @Override public void paint(Graphics g) {
                Graphics2D g2D = (Graphics2D) g;
                g2D.scale(DecisionTreeEditor.this.zoomManager.getZoom(), DecisionTreeEditor.this.zoomManager.getZoom());
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paint(g2D);
            }
        };
        this.jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // Allows JTree nodes to accept CR/LF codes
        this.jTree.setShowsRootHandles(true);
        this.jTree.setRowHeight(0);
        this.jTree.setCellRenderer((tree, object, selected, expanded, leaf, row, hasFocus) -> {
            if (object instanceof DecisionTreeElementPanel) {
                ((DecisionTreeElementPanel) object).update(selected, expanded, leaf, row, hasFocus);
            }
            return (Component) object;
        });
        this.jTree.setUI(new BasicTreeUI() {
            @Override protected MouseListener createMouseListener() {
                return DecisionTreeEditor.this.zoomManager.redelegatedMouseAdapter(MouseListenerUtils.mouseListenerToMouseAdapter(super.createMouseListener()));
            }
        });
        
        this.jTree.addMouseListener(this.listener);
        this.add(this.jTree);
        updateVisualInformation(root);
        this.networkPanel.addDecisionTreeWindows(this);
        this.setBackground(GUIColors.DecisionTree.WINDOW.getColor());
    }
    
    /**
     * Handles the window closure by unregistering itself from the parent network panel.
     *
     * @return True if the window closed successfully.
     */
    @Override public boolean close() {
        this.networkPanel.removeDecisionTreeWindows(null);
        return super.close();
    }
    
    @Override protected void doPaint(Graphics2D graphics2D) {
    }
    
    @Override public void updateUI() {
        super.updateUI();
        this.setBackground(GUIColors.DecisionTree.BACKGROUND.getColor());
    }
    
    
    /**
     * Rebuilds the visual model and refreshes the viewport.
     *
     * @param root The root element of the decision tree.
     */
    private void updateVisualInformation(DecisionTreeElement root) {
        DecisionTreeModel model = new DecisionTreeModel(root);
        this.jTree.setModel(model);
        for (int i = 0; i < this.jTree.getRowCount(); i++) {
            this.jTree.expandRow(i);
        }
    }
    
    private static final int DEFAULT_DEPTH = 5;
    
    /**
     * Builds a decision tree from a ProbNet with a default depth.
     *
     * @see #buildDecisionTree(ProbNet, int)
     */
    private DecisionTreeElement buildDecisionTree(ProbNet probNet)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return buildDecisionTree(probNet, DEFAULT_DEPTH);
    }
    
    /**
     * Builds a decision tree from a ProbNet up to a specified depth.
     *
     * @param probNet The probabilistic network.
     * @param depth   The maximum depth of the decision tree.
     *
     * @return The root element of the constructed decision tree.
     *
     * @throws NotEvaluableNetworkException                                    if the network cannot be evaluated
     * @throws IncompatibleEvidenceException                                   if the evidence is incompatible with the network
     * @throws NonProjectablePotentialException                                if the potential cannot be projected
     * @throws PotentialOperationException.DifferentSizesInPotentialsAndStates if different sizes in potentials and states occurs
     */
    private DecisionTreeElement buildDecisionTree(ProbNet probNet, int depth)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return this.decisionTreeManager.buildDecisionTree(probNet, depth);
    }
    
    /**
     * Returns the zoomManager.
     *
     * @return the zoomManager.
     */
    public double getZoom() {
        return this.zoomManager.getZoom();
    }
    
    @Override public void setZoom(double zoom) {
        this.zoomManager.setZoom(zoom);
        repaint();
    }
    
    /**
     * Expands the tree by one additional level of inference.
     */
    public void inferenceExpandNextLevel()
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceExpandLevels(1);
    }
    
    private void inferenceExpandLevels(int n)
            throws NotEvaluableNetworkException, NonProjectablePotentialException, IncompatibleEvidenceException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        DecisionTreeModel auxModel = (DecisionTreeModel) this.jTree.getModel();
        DecisionTreeBranchPanel root = (DecisionTreeBranchPanel) auxModel.getRoot();
        this.decisionTreeManager.expandLevels(root.getTreeBranch(), n);
        updateVisualInformation(root.getTreeBranch());
    }
    
    private void inferenceExpandAllLevels()
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceExpandLevels(Integer.MAX_VALUE);
    }
    
    /**
     * Internal listener to handle GUI actions and mouse interactions.
     */
    private class TreePanelListener implements ActionListener, MouseListener {
        
        /**
         * Dispatches commands for expansion, opening networks, or saving to Graphviz.
         *
         * @param e The action event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            GUIUtils.executeUIAction(() -> {
                ActionCommands command = ActionCommands.of(e.getActionCommand());
                switch (command) {
                    case ActionCommands.TREE_EXPAND_NEXT -> inferenceExpandNextLevel();
                    case ActionCommands.TREE_EXPAND_ALL -> inferenceExpandAllLevels();
                    case ActionCommands.TREE_OPEN_NETWORK -> openAssociatedNetwork();
                    case ActionCommands.TREE_SHOW_CEP -> openAssociatedCEP();
                    case ActionCommands.TREE_SAVE_GRAPHVIZ -> {
                        TreeNodeToDot tree2dot = new TreeNodeToDot();
                        Object selectedComponent = DecisionTreeEditor.this.jTree.getLastSelectedPathComponent();
                        if (selectedComponent instanceof DecisionTreeNodePanel treeNodePanel) {
                            DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
                            tree2dot.paintDTNode(treeNode);
                        }
                    }
                    case null, default -> {
                    
                    }
                }
            });
        }
        
        private void openAssociatedCEP() {
            Object selectedComponent = DecisionTreeEditor.this.jTree.getLastSelectedPathComponent();
            if (selectedComponent instanceof DecisionTreeNodePanel treeNodePanel) {
                DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
                CEPDialog cepDialog = new CEPDialog(null, (CEP) (treeNode.getUtility()), treeNode.getNetwork());
                cepDialog.setVisible(true);
            }
        }
        
        private void openAssociatedNetwork() {
            Object selectedComponent = DecisionTreeEditor.this.jTree.getLastSelectedPathComponent();
            if (selectedComponent instanceof DecisionTreeNodePanel) {
                MainGUI.INSTANCE.mainPanel
                        .getMainPanelListenerAssistant()
                        .openNetwork(getNetwork(selectedComponent));
            }
        }
        
        static ProbNet getNetwork(Object selectedComponent) {
            return ((DecisionTreeNodePanel) selectedComponent).getTreeNode().getNetwork();
        }
        
        /* Listener methods */
        // Open tree contextual menu on right click
        
        /**
         * Handles right-click events to show contextual menus.
         *
         * @param mouseEvent The mouse event.
         */
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            if (!SwingUtilities.isRightMouseButton(mouseEvent)) {
                DecisionTreeEditor.this.processMouseEvent(mouseEvent);
                return;
            }
            int row = DecisionTreeEditor.this.jTree.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
            DecisionTreeEditor.this.jTree.setSelectionRow(row); // Select the right-clicked component
            
            Object selectedComponent = DecisionTreeEditor.this.jTree.getLastSelectedPathComponent();
            
            boolean shouldShowCEPOption = switch (selectedComponent) {
                case DecisionTreeNodePanel selectedNodePanel -> {
                    NodeType nodeType = selectedNodePanel.getNodeType();
                    if (!(nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION || nodeType == NodeType.UTILITY)) {
                        yield false;
                    }
                    MulticriteriaOptions.Type type = getNetwork(selectedNodePanel).getInferenceOptions()
                                                                                  .getMultiCriteriaOptions()
                                                                                  .getMulticriteriaType();
                    yield type == MulticriteriaOptions.Type.COST_EFFECTIVENESS;
                }
                case null, default -> false;
            };
            
            TreeContextualMenu treeMenu = (TreeContextualMenu) DecisionTreeEditor.this.contextualMenuFactory
                    .getTreeContextualMenu(shouldShowCEPOption);
            treeMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            DecisionTreeEditor.this.processMouseEvent(mouseEvent);
        }
        
        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            DecisionTreeEditor.this.processMouseEvent(mouseEvent);
        }
        
        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
            DecisionTreeEditor.this.processMouseEvent(mouseEvent);
        }
        
        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            DecisionTreeEditor.this.processMouseEvent(mouseEvent);
        }
    }
    
}