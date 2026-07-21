/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.elements;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.window.decisiontree.format.DecisionTreeUtilityFormatters;

import javax.swing.*;
import java.awt.*;

/**
 * GUI representation of a logical decision tree node.
 * It manages node-specific icons and displays utility values for terminal nodes.
 *
 * @author CISIAD, UNED
 * @version 2019
 */
@SuppressWarnings("serial")
public non-sealed class DecisionTreeNodePanel extends DecisionTreeElementPanel {
    
    /** The underlying logical tree node. */
    private final DecisionTreeNode treeNode;
    
    /**
     * Constructs a panel for a specific decision tree node and initializes its icon.
     *
     * @param treeNode The logical node to be represented.
     */
    public DecisionTreeNodePanel(DecisionTreeNode treeNode) {
        this.treeNode = treeNode;
        super.initialize();
    }
    
    @Override public JComponent makeSummary() {
        return visualNodePanel(this.treeNode.getNodeType(), this.treeNode.getVariable().getName());
    }
    
    public static @NotNull JPanel visualNodePanel(NodeType nodeType, String nodeName) {
        var visualNode = new VisualNode(new Node(null,
                                                 new Variable(nodeName),
                                                 nodeType), null);
        JPanel panel = new JPanel() {
            @Override public void paint(Graphics g) {
                visualNode.paint((Graphics2D) g);
            }
        };
        Shape nodeDimensions = visualNode.getShape((Graphics2D) panel.getGraphics());
        double width = nodeDimensions.getBounds2D().getWidth();
        double height = nodeDimensions.getBounds2D().getHeight();
        visualNode.setTemporalPosition(new org.openmarkov.core.model.network.Point2D.Double(2, 0));
        panel.setSize(new Dimension((int) width + 2 + 2, (int) height + 2));
        panel.setMinimumSize(new Dimension((int) width + 2 + 2, (int) height + 2));
        panel.setPreferredSize(new Dimension((int) width + 2 + 2, (int) height + 2));
        return panel;
    }
    
    /**
     * Gets the associated logical tree node.
     *
     * @return The {@link DecisionTreeNode} linked to this panel.
     */
    public DecisionTreeNode getTreeNode() {
        return this.treeNode;
    }
    
    
    /**
     * Updates the right label with formatted utility data if the node is a utility type.
     * {@inheritDoc}
     */
    @Override
    public void update(boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (this.treeNode.getNodeType() == NodeType.UTILITY) {
            this.descriptionLabel.setText(DecisionTreeUtilityFormatters.format(this.treeNode.getUtility(), this.df, false));
        }
    }
    
    /**
     * Gets the type of the underlying node.
     *
     * @return The {@link NodeType} of the associated tree node.
     */
    public NodeType getNodeType() {
        return this.treeNode.getNodeType();
    }
}
