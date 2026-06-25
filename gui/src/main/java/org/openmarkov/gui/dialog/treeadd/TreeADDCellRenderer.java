/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.ComponentIcon;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeNodePanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Custom {@link TreeCellRenderer} that renders TreeADD nodes with appropriate
 * variable icons (chance, decision, utility) and branch labels.
 */
public class TreeADDCellRenderer extends JPanel implements TreeCellRenderer {
    private static final long serialVersionUID = 1L;
    /**
     * Container of SummaryBox' foreground or the variable icon
     */
    private final JLabel leftLabel = new JLabel();
    /**
     * Container of leaf data: Potential description or value
     */
    private final JLabel rightLabel = new JLabel();
    /**
     * Font used in icon foreground
     */
    private final Font textIconFont;
    
    private final ProbNet probNet;
    /**
     * Precision Proxy: every node of the tree could have its own precision
     * (number of decimals)
     */
    // protected PrecisionProxy precisionProxy;
    
    /**
     * TODO: Add a new constructor with font and default precision values
     *
     * @param probNet the prob net
     */
    public TreeADDCellRenderer(ProbNet probNet) {
        super(new BorderLayout());
        this.probNet = probNet;
        this.add(leftLabel, BorderLayout.WEST);
        this.add(rightLabel, BorderLayout.CENTER);
        leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
        leftLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        setBackground(GUIColors.DecisionTree.BACKGROUND.getColor());
        // TODO: Add a background color attribute
        textIconFont = new Font("Helvetica", Font.BOLD, 15);
        // precisionProxy= new PrecisionProxy(2);
    }

    @Override
    public @Nullable Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {

        leftLabel.setText(null);
        rightLabel.setText(null);

        // LIMPIAR BORDE Y TEXTO
        rightLabel.setBorder(null);
        rightLabel.setIcon(null);

        if (value instanceof TreeADDBranch) {
            return getTreeCellRendererBranch(tree, (TreeADDBranch) value,
                    selected, expanded, leaf, row, hasFocus);
        }

        if (value instanceof Potential) {
            return getTreeCellRendererPotential((Potential) value);
        }

        if (value instanceof String) {
            leftLabel.setText("@" + value);
            return this;
        }

        return null;
    }
    
    /**
     * Draws a TreeADDBranch node
     *
     * @param tree the tree
     * @param branch   TreeADDBranch being painted
     * @param selected Selection Flag: true when this treenode is selected
     * @param expanded true when this treenode is expanded
     * @param leaf     true when this treenode is a leaf
     * @param row the row
     * @param hasFocus the has focus
     * @return the tree cell renderer branch
     */
    public Component getTreeCellRendererBranch(JTree tree, TreeADDBranch branch, boolean selected, boolean expanded,
                                               boolean leaf, int row, boolean hasFocus) {
        // This kind of nodes won't display an icon
        leftLabel.setIcon(null);
        // TreeADDBranch always have only one child: a potential that it would
        // be a TreeADD or a Potential
        Object child = tree.getModel().getChild(branch, 0);
        boolean isLeaf = tree.getModel().isLeaf(child);
        if (!leaf && child instanceof TreeADDPotential && !expanded) {
            rightLabel.setText("    ...");
        }
        if (isLeaf && !expanded) {
            getTreeCellRendererComponent(tree, child, selected, expanded, leaf, row, hasFocus);
            String txt = rightLabel.getText();

            if (txt != null && txt.length() > 15) {
                txt = txt.substring(0, 20) + "...";
            }

            rightLabel.setText(txt);
        }
        if (branch.isLabeled()) {
            String oldText = (rightLabel.getText() != null) ? rightLabel.getText() : "";
            rightLabel.setText(" {" + branch.getLabel() + "}" + oldText);
        }
        leftLabel.setText(getBranchDescriptiontHTML(branch));
        return this;
    }
    
    /**
     * Draws a TreeADDPotential or a TablePotential node
     *
     * @param potential Potential Node of the ADD/Tree
     */
    public Component getTreeCellRendererPotential(Potential potential) {
        if (potential instanceof TreeADDPotential treeADDPotential) {
            Variable topVariable = treeADDPotential.getRootVariable();
            var node = probNet.getNode(topVariable);
            leftLabel.setIcon(new ComponentIcon(DecisionTreeNodePanel.visualNodePanel(node.getNodeType(), topVariable.getName())));
        } else {
            String text = potential.treeADDString();
            if (text.contains("P(")){
                text = text.replaceFirst("P", "<i>P</i>");
            }
            rightLabel.setText("<html>" + text + "</html>");
            Border normalBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)
            );
            Border leftSpacing = BorderFactory.createEmptyBorder(0, 6, 0, 0);
            if (potential.isUncertain()) {
                rightLabel.setIcon(IconBind.UNCERTAINTY.icon());
                rightLabel.setHorizontalTextPosition(SwingConstants.LEFT);
                rightLabel.setHorizontalAlignment(SwingConstants.LEFT);
                rightLabel.setVerticalAlignment(SwingConstants.TOP);
                rightLabel.setIconTextGap(0);
                rightLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                                leftSpacing,
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(Color.GRAY, 1),
                                        BorderFactory.createEmptyBorder(0, 4, 0, 2)
                                )
                        )
                );
                rightLabel.setPreferredSize(null);
            } else {
                rightLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                                leftSpacing,
                                normalBorder
                        )
                );
            }
        }
        return this;
    }
    
    /**
     * Creates what is displayed in a branch for discretized or finite states
     * variables
     */
    public static String getBranchDescriptiontHTML(TreeADDBranch treeBranch) {
        String txtLeft = "<html>";
        Variable topVariable = treeBranch.getRootVariable();
        if (topVariable == null) {
        } else if (topVariable.getVariableType() == VariableType.NUMERIC) {
            String varName = topVariable.getName();
            Threshold min = treeBranch.getLowerBound();
            Threshold max = treeBranch.getUpperBound();
            String intervalString = "";
            String minimun;
            String maximun;
            intervalString += !min.belongsToLeft() ? "[" : "(";
            if (min.getLimit() == Double.NEGATIVE_INFINITY) {
                minimun = "-" + "\u221E";
            } else {
                minimun = String.valueOf(min.getLimit());
            }
            intervalString += minimun;
            intervalString += ", ";
            if (max.getLimit() == Double.POSITIVE_INFINITY) {
                maximun = "\u221E";
            } else {
                maximun = String.valueOf(max.getLimit());
            }
            intervalString += maximun;
            intervalString += max.belongsToLeft() ? "]" : ")";
            txtLeft += "<td align=center border=0>" + varName + "=" + intervalString + "</td>";
        } else {
            String varName = topVariable.getName();
            List<State> branchStates = treeBranch.getBranchStates();
            String varStateNames = "";
            if (branchStates.size() > 1) {
                varStateNames += "{";
            }
            boolean bFirst = true;
            for (State state : branchStates) {
                if (bFirst) {
                    bFirst = false;
                } else {
                    varStateNames += ", ";
                }
                varStateNames += state.getName();
            }
            if (branchStates.size() > 1) {
                varStateNames += "}";
            }
            txtLeft += "<td align=center border=0>" + varStateNames + "</td>";
        }
        txtLeft += "</html>";
        return txtLeft;
    }
}
