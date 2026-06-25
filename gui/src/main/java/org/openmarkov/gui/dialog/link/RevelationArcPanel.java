
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.link;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.component.DiscretizeTablePanel;
import org.openmarkov.gui.component.RevelationArcDiscretizeTablePanel;
import org.openmarkov.gui.dialog.common.KeyTablePanel;
import org.openmarkov.gui.dialog.common.PrefixedKeyTablePanel;
import org.openmarkov.gui.dialog.common.SelectableKeyTablePanel;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.List;

/*****
 * Graphic panel to display and set the revealing conditions for a link. The
 * conditions can be either states or intervals.
 * @author ckonig
 */
@SuppressWarnings("serial") public class RevelationArcPanel extends JPanel implements ItemListener {
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /***
     * Object where all the information will be saved
     */
    private final Link<Node> link;
    /****
     * Variable Type of the revelation conditions
     */
    private final VariableType variableType;
    /**
     * label for the table to show the values of the node
     */
    private JLabel jLabelValuesPanel = null;
    /**
     * table to show the states of the variable`s domain.
     */
    private SelectableKeyTablePanel discreteNodeStatesTablePanel;
    /**
     * table to show intervals of the variable's domain.
     */
    private RevelationArcDiscretizeTablePanel discretizedNodeStatesTablePanel;
    
    /****
     * Constructor - constructs a JPanel for displaying the revelation
     * conditions of a link
     * @param link the link
     */
    public RevelationArcPanel(Link<Node> link) {
        this.link = link;
        this.variableType = link.getFrom().getVariable().getVariableType();
        initialize();
    }
    
    public void initialize() {
        setPreferredSize(new Dimension(600, 375));
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(Box.createRigidArea(new Dimension(5, 10)));
        JLabel label = this.getJLabelValuesPanel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(label);
        this.add(Box.createRigidArea(new Dimension(5, 10)));
        JPanel centerPanel = new JPanel();
        JPanel panel = this.getNodeStatesTablePanel();
        centerPanel.add(panel);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(centerPanel);
    }
    
    /**
     * @return The JLabel of the values panel
     */
    protected JLabel getJLabelValuesPanel() {
        if (jLabelValuesPanel == null) {
            jLabelValuesPanel = new JLabel();
            jLabelValuesPanel.setName("jLabelValuesPanel");
            jLabelValuesPanel.setText("a Label");
            Node node1 = link.getFrom();
            Node node2 = link.getTo();
            MessageFormat messageForm = new MessageFormat(
                    stringDatabase.getString("RevelationArcPanel.jLabelValuesPanel.Text"));
            Object[] labelArgs = new Object[]{node1.getName(), node2.getName()};
            String msgText = messageForm.format(labelArgs);
            jLabelValuesPanel.setText(msgText);
        }
        return jLabelValuesPanel;
    }
    
    /**
     * This method initializes NodeValuesTable.
     *
     * @return the PrefixedKeyTablePanel for the Node Values
     */
    protected KeyTablePanel getNodeStatesTablePanel() {
        if (this.variableType != VariableType.NUMERIC) {
            return getNodeDiscreteStatesTablePanel();
        }
        return getNodeDiscretizedStatesTablePanel();
    }
    
    /**
     * This method initializes NodeValuesTable.
     *
     * @return the PrefixedKeyTablePanel for the Node Values
     */
    protected PrefixedKeyTablePanel getNodeDiscreteStatesTablePanel() {
        if (discreteNodeStatesTablePanel == null) {
            String[] columnNames = {
                    stringDatabase.getString("DiscreteValuesTablePanel.ValuesTable." + "Columns.Name.Text"), "",
                    stringDatabase.getString("DiscreteValuesTablePanel.ValuesTable." + "Columns.Value.Text")};
            discreteNodeStatesTablePanel = new SelectableKeyTablePanel(columnNames,
                                    stringDatabase.getString("DiscreteValuesTablePanel.ValuesTable." + "Columns.Id.Prefix"),
                     true,link);
        }
        discreteNodeStatesTablePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        return discreteNodeStatesTablePanel;
    }
    
    /**
     * This method initializes NodeValuesTable.
     *
     * @return the DiscretizeTablePanel for the Node Values
     */
    protected DiscretizeTablePanel getNodeDiscretizedStatesTablePanel() {
        if (discretizedNodeStatesTablePanel == null) {
            String[] columnNames = {stringDatabase.getString("DiscretizeTableModel.Columns." + "IntervalId.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "IntervalName.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "LowLimitSymbol.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "LowLimitValue.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "ValuesSeparator.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "UpperLimitValue.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "UpperLimitSymbol.Text")};
            discretizedNodeStatesTablePanel = new RevelationArcDiscretizeTablePanel(columnNames, link);
            discretizedNodeStatesTablePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        return discretizedNodeStatesTablePanel;
    }
    
    public void setFieldsFromProperties(Link<Node> link) {
        if (link != null) {
            if (this.variableType != VariableType.NUMERIC) {
                discreteNodeStatesTablePanel.setData(convertStringsToTableDiscreteFormat(link));
                discreteNodeStatesTablePanel.adjustColumnSize();
            } else {
                discretizedNodeStatesTablePanel.setPartitionedInterval();
            }
        }
    }
    
    protected static Object[][] convertStringsToTableDiscreteFormat(Link<Node> link) {
        Node node = link.getFrom();
        State[] values = node.getVariable().getStates();
        List<State> revealingStates = link.getRevealingStates();
        int i;
        int l = values.length;
        Object[][] data = new Object[l][2];
        i = l - 1;
        for (State value : values) {
            data[i][0] = revealingStates.contains(value);
            data[i--][1] = value.getName();
        }
        return data;
    }
    
    @Override public void itemStateChanged(ItemEvent e) {
    }
    
    public void saveChanges() {
    }
}
