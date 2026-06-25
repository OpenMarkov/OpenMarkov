/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.core.NodeReplaceStatesEdit;
import org.openmarkov.core.action.core.PrecisionEdit;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.gui.action.PartitionedIntervalEdit;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.component.DiscretizeTablePanel;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.util.GUIDefaultStates;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Panel to set the values of a node with discretized values
 *
 * @author jlgozalo
 * @author mkpalacio
 * @author myebra
 * @version 1.2 myebra
 */
public class NodeDomainValuesTablePanel extends JPanel implements ItemListener, ActionListener {
    
    /**
     * serial uid
     */
    private static final long serialVersionUID = 1047978130482205148L;
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * Object where all information will be saved.
     */
    private Node node = null;
    /**
     * Specifies if the node whose additionalProperties are edited is new.
     */
    private boolean newNode;
    /**
     * label for the values comboBox for the states of the node
     */
    private JLabel jLabelStatesValues;
    /**
     * combo box to select the values for the states of the node
     */
    private JComboBox<String> jComboBoxStatesValues;
    /**
     * panel, buttonGroup and radioButtons to define monotony in the panel
     */
    private JPanel jPanelNodeType;
    private JLabel jLabelNodeVariableType;
    // private PrefixedKeyTablePanel nodeDiscreteStatesTablePanel;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JRadioButton jRadioButtonDecreasing;
    private JRadioButton jRadioButtonIncreasing;
    private JPanel jPanelMonotonyUpDown;
    /**
     * label for the table to show the values of the node
     */
    private JLabel jLabelValuesPanel = null;
    /**
     * table to show the states of the node
     */
    private DiscretizeTablePanel discretizedStatesPanel = null;
    /**
     * The Node Values Comment Label
     */
    private JTextArea jTextAreaLabelNodeValuesComment;
    /**
     * The Node Values Comment Scroll Panel box
     */
    private CommentHTMLScrollPane commentHTMLScrollPaneNodeValuesComment = null;
    /**
     * partitioned interval
     */
    private PartitionedInterval partitionedInterval = null;
    /**
     * unit field
     */
    private JTextField jFieldUnit;
    /**
     * precision combobox
     */
    private JComboBox<String> jComboBoxPrecision;
    /**
     * precision field
     */
    private JFormattedTextField jFormattedTextFieldPrecision;
    /**
     * label for the precision field
     */
    private JLabel jLabelPrecision;
    /**
     * label for the unit field
     */
    private JLabel jLabelUnit;
    /**
     * TODO listener for actions -
     */
    private final NodeDomainValuesTablePanelListener listener;
    private JComboBox<VariableType> jComboBoxNodeVariableType;
    private boolean uploadingData = false;
    
    /**
     * constructor without construction parameters
     */
    public NodeDomainValuesTablePanel(Node node) {
        this(true);// , notifier);
        this.node = node;
        initialize();
    }
    
    /**
     * This method initialises this instance.
     *
     * @param newNode true if the node is a new node; otherwise false
     */
    public NodeDomainValuesTablePanel(final boolean newNode) {
        // ElementObservable notifier) {
        setName("NodeDomainValuesTablePanel");
        this.newNode = newNode;
        this.listener = new NodeDomainValuesTablePanelListener(this);
    }
    
    /**
     * {@code Initialize}
     * <p>
     * initialize the layout for this panel
     */
    private void initialize() {
        if (node.getNodeType() == NodeType.UTILITY) {
            getJComboBoxNodeVariableType().setSelectedItem(stringDatabase
                                                                   .getString("NodeDomainValuesTablePanel.jComboBoxNodeVariableType." + "Items.Continuous"));
            getDiscretizedStatesPanel().setEnabled(false);
            getDiscretizedStatesPanel().setVisible(false);
            getJPanelMonotonyUpDown().setEnabled(false);
            getJPanelMonotonyUpDown().setVisible(false);
            getJLabelValuesPanel().setEnabled(false);
            getJLabelValuesPanel().setVisible(false);
            getJFormattedTextFieldPrecision().setValue(node.getVariable().getPrecision());
            // getJFormattedTextFieldUnit().setValue(value);
        }
        setPreferredSize(new Dimension(600, 375));
        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup().addContainerGap().addGroup(
                        groupLayout.createParallelGroup(Alignment.LEADING)
                                   .addGroup(groupLayout.createSequentialGroup()
                                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                                             .addGroup(
                                                                                     groupLayout.createSequentialGroup()
                                                                                                .addComponent(getJLabelNodeVariableType())
                                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                                .addComponent(getJComboBoxNodeVariableType(),
                                                                                                              GroupLayout.PREFERRED_SIZE, 203, GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(18)
                                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                                .addComponent(getJPanelMonotonyUpDown(), GroupLayout.PREFERRED_SIZE,
                                                                                                              212, GroupLayout.PREFERRED_SIZE))
                                                                             .addGroup(
                                                                                     groupLayout.createSequentialGroup()
                                                                                                .addComponent(getJLabelPrecision())
                                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                                .addComponent(/*
                                                                                                         * getJFormattedTextFieldPrecision
                                                                                                         * (
                                                                                                         * )
                                                                                                         */getJComboBoxPrecision(), GroupLayout.PREFERRED_SIZE, 49,
                                                                                                           GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(18)
                                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                                .addComponent(getJLabelUnit())
                                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                                .addComponent(getJTextFieldUnit(), GroupLayout.PREFERRED_SIZE, 75,
                                                                                                              GroupLayout.PREFERRED_SIZE))
                                                                             .addGroup(
                                                                                     groupLayout.createSequentialGroup()
                                                                                                .addComponent(getJLabelValuesPanel(), GroupLayout.DEFAULT_SIZE, 49,
                                                                                                              Short.MAX_VALUE)
                                                                                                .addComponent(getDiscretizedStatesPanel(), GroupLayout.DEFAULT_SIZE,
                                                                                                              627, Short.MAX_VALUE)))
                                                        .addContainerGap()))));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup()
                           .addContainerGap()
                           .addGroup(
                                   groupLayout.createParallelGroup(Alignment.BASELINE)
                                              .addComponent(getJLabelNodeVariableType())
                                              .addComponent(getJComboBoxNodeVariableType(), GroupLayout.PREFERRED_SIZE, /* 20 */
                                                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                              .addComponent(getJPanelMonotonyUpDown(), GroupLayout.PREFERRED_SIZE, 22,
                                                            GroupLayout.PREFERRED_SIZE))
                           .addPreferredGap(ComponentPlacement.RELATED)
                           .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(getJLabelPrecision(), GroupLayout.PREFERRED_SIZE,
                                                              GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(/*
                                                 * getJFormattedTextFieldPrecision
                                                 * ( )
                                                 */getJComboBoxPrecision())
                                                .addComponent(getJLabelUnit(), GroupLayout.PREFERRED_SIZE, /* 25 */
                                                              GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(getJTextFieldUnit()))
                           .addGap(21)
                           .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(getJLabelValuesPanel(), GroupLayout.PREFERRED_SIZE, 24,
                                                              GroupLayout.PREFERRED_SIZE)
                                                .addComponent(getDiscretizedStatesPanel(), GroupLayout.PREFERRED_SIZE,/* 24 */
                                                              GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
                           .addContainerGap(77, Short.MAX_VALUE)));
        Component[] components = new Component[2];
        components[0] = getJTextFieldUnit();
        components[1] = getJComboBoxPrecision();
        groupLayout.linkSize(components);
        Component[] labelComponents = new Component[2];
        labelComponents[0] = getJLabelNodeVariableType();
        labelComponents[1] = getJLabelPrecision();
        groupLayout.linkSize(labelComponents);
        setLayout(groupLayout);
    }
    
    /**
     * @param newNode the newNode to set
     */
    public void setNewNode(boolean newNode) {
        this.newNode = newNode;
    }
    
    public Node getNode() {
        return node;
    }
    
    /**
     * This method fills the content of the fields from a NodeProperties object.
     *
     * @param properties object from where load the information.
     */
    public void setFieldsFromProperties(Node properties) {
        setUploadingData(true);
        // jComboBoxStatesValues.removeItemListener(this);
        (getDiscretizedStatesPanel().getStandardDomainButton()).removeActionListener(this);
        // jFormattedTextFieldPrecision.setValue( Double.valueOf(
        // properties.getVariable().getPrecision() ) );
        getJComboBoxPrecision().setSelectedItem(String.valueOf(properties.getVariable().getPrecision()));
        // getJComboBoxPrecision().setSelectedIndex(4);// initialized precision
        // to 0.01
        jFieldUnit.setText(properties.getVariable().getUnit().getString());
        if (properties != null) {
            if (properties.getVariable().getVariableType() == VariableType.DISCRETIZED
                    || properties.getVariable().getVariableType() == VariableType.NUMERIC) {
                getDiscretizedStatesPanel().setDataFromPartitionedInterval(node.getVariable().getPartitionedInterval(),
                                                                           node.getVariable().getStates());
                Object[][] tableData = getDiscretizedStatesPanel().getData();
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 3; j < tableData[0].length; j++) {
                        if (j == 3 || j == 5) {
                            String value = (String) tableData[i][j];
                            // Infinity values
                            if (value != "\u221E" && value != "-" + "\u221E") {
                                getDiscretizedStatesPanel().getValuesTable().setValueAt(value, i, j, null);
                            }
                        }
                    }
                }
            }
            switch (properties.getVariable().getVariableType()) {
                case FINITE_STATES: {
                    getJComboBoxNodeVariableType().setSelectedItem(stringDatabase
                                                                           .getString("NodeDomainValuesTablePanel." + "jComboBoxNodeVariableType.Items.Discrete"));
                    getJLabelPrecision().setEnabled(false);
                    getJFormattedTextFieldPrecision().setEnabled(false);
                    getJLabelValuesPanel().setVisible(true);
                    getJLabelDomainValues().setVisible(true);
                    getJComboBoxStatesValues().setVisible(true);
                    getJTextFieldUnit().setEnabled(false);
                    getJTextFieldUnit().setVisible(false);
                    getJLabelUnit().setEnabled(false);
                    getJLabelUnit().setVisible(false);
                    getJComboBoxPrecision().setVisible(false);
                    getJComboBoxPrecision().setEnabled(false);
                    getJLabelPrecision().setVisible(false);
                    // getJFormattedTextFieldPrecision().setVisible(false);
                    getJPanelMonotonyUpDown().setVisible(false);
                    jRadioButtonIncreasing.setEnabled(false);
                    jRadioButtonDecreasing.setEnabled(false);
                    jRadioButtonIncreasing.setSelected(true);
                    getDiscretizedStatesPanel().setEnablePanelButton(true);
                    getDiscretizedStatesPanel().setVisibleButtonPanel(true);
                    getJLabelDomainValues().setVisible(false);
                    getJComboBoxStatesValues().setVisible(false);
                    getDiscretizedStatesPanel().getStandardDomainButton().setVisible(true);
                    getDiscretizedStatesPanel().getStandardDomainButton().setEnabled(true);
                    // node comment title
                    // states of the node
                    State[] states = properties.getVariable().getStates();
                    State[] reorderedStates = states.clone();
                    Collections.reverse(Arrays.asList(reorderedStates));
                    Object[][] tableData = getDataFromStates(reorderedStates);
                    getDiscretizedStatesPanel().setData(tableData);
                    getDiscretizedStatesPanel().setVisibleAddValue(true);
                    getDiscretizedStatesPanel().setVisibleRemoveValue(true);
                    getDiscretizedStatesPanel().setVisibleUpValue(true);
                    getDiscretizedStatesPanel().setVisibleDownValue(true);
                    
                    // }
                    break;
                }
                case NUMERIC: {
                    getJComboBoxNodeVariableType().setSelectedItem(stringDatabase
                                                                           .getString("NodeDomainValuesTablePanel." + "jComboBoxNodeVariableType.Items.Continuous"));
                    getJLabelPrecision().setEnabled(true);
                    getJComboBoxPrecision().setVisible(true);
                    getJComboBoxPrecision().setEnabled(true);
                    // getJFormattedTextFieldPrecision().setEnabled(true);
                    getJLabelValuesPanel().setVisible(false);
                    getJLabelDomainValues().setVisible(false);
                    getJComboBoxStatesValues().setVisible(false);
                    getJLabelPrecision().setVisible(true);
                    getJFormattedTextFieldPrecision().setVisible(true);
                    getJPanelMonotonyUpDown().setVisible(false);
                    jRadioButtonIncreasing.setEnabled(false);
                    jRadioButtonDecreasing.setEnabled(false);
                    jRadioButtonIncreasing.setSelected(false);
                    jRadioButtonDecreasing.setSelected(false);
                    getDiscretizedStatesPanel().setEnablePanelButton(true);
                    getDiscretizedStatesPanel().setVisibleButtonPanel(true);
                    getDiscretizedStatesPanel().setEnabledAddValue(false);
                    getDiscretizedStatesPanel().setEnabledRemoveValue(false);
                    getDiscretizedStatesPanel().setEnabledUpValue(false);
                    getDiscretizedStatesPanel().setEnabledDownValue(false);
                    getDiscretizedStatesPanel().setVisibleAddValue(false);
                    getDiscretizedStatesPanel().setVisibleRemoveValue(false);
                    getDiscretizedStatesPanel().setVisibleUpValue(false);
                    getDiscretizedStatesPanel().setVisibleDownValue(false);
                    getDiscretizedStatesPanel().getStandardDomainButton().setVisible(false);
                    getDiscretizedStatesPanel().getStandardDomainButton().setEnabled(false);
                    getJTextFieldUnit().setEnabled(true);
                    getJTextFieldUnit().setVisible(true);
                    getJLabelUnit().setEnabled(true);
                    getJLabelUnit().setVisible(true);
                    break;
                }
                case DISCRETIZED: {
                    getJComboBoxNodeVariableType().setSelectedItem(stringDatabase
                                                                           .getString("NodeDomainValuesTablePanel." + "jComboBoxNodeVariableType.Items.Discretized"));
                    getJLabelPrecision().setEnabled(true);
                    getJComboBoxPrecision().setVisible(true);
                    getJComboBoxPrecision().setEnabled(true);
                    // getJFormattedTextFieldPrecision().setEnabled(true);
                    getJLabelValuesPanel().setVisible(true);
                    getJLabelDomainValues().setVisible(true);
                    getJComboBoxStatesValues().setVisible(true);
                    getJLabelPrecision().setVisible(true);
                    // getJFormattedTextFieldPrecision().setVisible(true);
                    getJPanelMonotonyUpDown().setVisible(true);
                    jRadioButtonIncreasing.setEnabled(true);
                    jRadioButtonDecreasing.setEnabled(true);
                    jRadioButtonIncreasing.setSelected(true);
                    getDiscretizedStatesPanel().setEnablePanelButton(true);
                    getDiscretizedStatesPanel().setVisibleButtonPanel(true);
                    getDiscretizedStatesPanel().setVisibleUpValue(false);
                    getDiscretizedStatesPanel().setVisibleDownValue(false);
                    getJLabelDomainValues().setVisible(false);
                    getJComboBoxStatesValues().setVisible(false);
                    getDiscretizedStatesPanel().getStandardDomainButton().setVisible(true);
                    getDiscretizedStatesPanel().getStandardDomainButton().setEnabled(true);
                    getJTextFieldUnit().setEnabled(true);
                    getJTextFieldUnit().setVisible(true);
                    getJLabelUnit().setEnabled(true);
                    getJLabelUnit().setVisible(true);
                    break;
                }
                case EVENT: {
                    getJComboBoxNodeVariableType().setSelectedItem(stringDatabase
                                                                           .getString("NodeDomainValuesTablePanel." + "jComboBoxNodeVariableType.Items.Event"));
                    getJComboBoxNodeVariableType().setEnabled(false);
                    getJLabelPrecision().setEnabled(false);
                    getJComboBoxPrecision().setVisible(false);
                    getJComboBoxPrecision().setEnabled(false);
                    // getJFormattedTextFieldPrecision().setEnabled(true);
                    getJLabelValuesPanel().setVisible(false);
                    getJLabelDomainValues().setVisible(false);
                    getJComboBoxStatesValues().setVisible(false);
                    getJLabelPrecision().setVisible(false);
                    getJFormattedTextFieldPrecision().setVisible(false);
                    getJPanelMonotonyUpDown().setVisible(false);
                    jRadioButtonIncreasing.setEnabled(false);
                    jRadioButtonDecreasing.setEnabled(false);
                    jRadioButtonIncreasing.setSelected(false);
                    jRadioButtonDecreasing.setSelected(false);
                    getDiscretizedStatesPanel().setVisible(false);
                    getDiscretizedStatesPanel().setEnabled(false);
//					getDiscretizedStatesPanel().setVisibleButtonPanel(false);
//					getDiscretizedStatesPanel().setEnabledAddValue(false);
//					getDiscretizedStatesPanel().setEnabledRemoveValue(false);
//					getDiscretizedStatesPanel().setEnabledUpValue(false);
//					getDiscretizedStatesPanel().setEnabledDownValue(false);
//					getDiscretizedStatesPanel().setVisibleAddValue(false);
//					getDiscretizedStatesPanel().setVisibleRemoveValue(false);
//					getDiscretizedStatesPanel().setVisibleUpValue(false);
//					getDiscretizedStatesPanel().setVisibleDownValue(false);
//					getDiscretizedStatesPanel().getStandardDomainButton().setVisible(false);
//					getDiscretizedStatesPanel().getStandardDomainButton().setEnabled(false);
                    getJTextFieldUnit().setEnabled(false);
                    getJTextFieldUnit().setVisible(false);
                    getJLabelUnit().setEnabled(false);
                    getJLabelUnit().setVisible(false);
                    break;
                }
            }
        }
        // jComboBoxStatesValues.addItemListener(this);
        (getDiscretizedStatesPanel().getStandardDomainButton()).addActionListener(this);
        setUploadingData(false);
    }
    
    /**
     * This method initializes NodeValuesTable.
     *
     * @return the DiscretizeTablePanel for the Node Values
     */
    protected DiscretizeTablePanel getDiscretizedStatesPanel() {
        if (discretizedStatesPanel == null) {
            String[] columnNames = {stringDatabase.getString("DiscretizeTableModel.Columns." + "IntervalId.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "IntervalName.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "LowLimitSymbol.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "LowLimitValue.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "ValuesSeparator.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "UpperLimitValue.Text"),
                    stringDatabase.getString("DiscretizeTableModel.Columns." + "UpperLimitSymbol.Text")};
            discretizedStatesPanel = new DiscretizeTablePanel(columnNames, node);
            discretizedStatesPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        return discretizedStatesPanel;
    }
    
    /**
     * @return The JLabel domain values
     */
    protected JLabel getJLabelDomainValues() {
        if (jLabelStatesValues == null) {
            jLabelStatesValues = new JLabel();
            jLabelStatesValues.setName("jLabelStatesValues");
            jLabelStatesValues.setText("a Label");
            jLabelStatesValues.setText(stringDatabase.getString("NodeDomainValuesTablePanel.jLabelStatesValues.Text"));
            jLabelStatesValues.setVisible(false);
            jLabelStatesValues.setEnabled(false);
        }
        return jLabelStatesValues;
    }
    
    /**
     * @return The JComboBox precision
     */
    protected JComboBox<String> getJComboBoxPrecision() {
        if (jComboBoxPrecision == null) {
            String[] precisions = {"1",/* "0.25","0.5", */"0.1", "0.01", "0.001", "0.0001"};
            jComboBoxPrecision = new JComboBox<String>(precisions);
            jComboBoxPrecision.setName("jComboBoxPrecision");
            // jComboBoxPrecision.setMinimumSize(minimumSize);
            // jComboBoxPrecision.setPreferredSize(getMinimumSize());
            jComboBoxPrecision.addItemListener(this);
        }
        return jComboBoxPrecision;
    }
    
    /**
     * @return The JComboBox states values
     */
    protected JComboBox<String> getJComboBoxStatesValues() {
        if (jComboBoxStatesValues == null) {
            jComboBoxStatesValues = new JComboBox<String>(GUIDefaultStates.getListStrings());
            jComboBoxStatesValues.setName("jComboBoxStatesValues");
            // jComboBoxStatesValues.addItemListener(listener);
            jComboBoxStatesValues.setEnabled(false);
            jComboBoxStatesValues.setVisible(false);
        }
        return jComboBoxStatesValues;
    }
    
    /**
     * @return The JPanel monotony up down
     */
    protected JPanel getJPanelMonotonyUpDown() {
        if (jPanelMonotonyUpDown == null) {
            jPanelMonotonyUpDown = new JPanel();
            jPanelMonotonyUpDown.setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
            jPanelMonotonyUpDown.setSize(329, 24);
            jPanelMonotonyUpDown.setName("jPanelMonotonyUpDown");
            jPanelMonotonyUpDown.setLayout(new GridLayout(0, 2, 0, 0));
            getJRadioButtonIncreasing().setEnabled(false);
            getJRadioButtonDecreasing().setEnabled(false);
            jPanelMonotonyUpDown.add(getJRadioButtonIncreasing());
            jPanelMonotonyUpDown.add(getJRadioButtonDecreasing());
            initButtonGroupMonotonyUpDown();
        }
        return jPanelMonotonyUpDown;
    }
    
    /**
     * @return The JRadioButton increasing
     */
    protected JRadioButton getJRadioButtonIncreasing() {
        if (jRadioButtonIncreasing == null) {
            jRadioButtonIncreasing = new JRadioButton();
            jRadioButtonIncreasing.setName("jRadioButtonMonotonyUp");
            jRadioButtonIncreasing
                    .setText(stringDatabase.getString("NodeDomainValuesTablePanel." + "jRadioButtonMonotonyUp.Text"));
            jRadioButtonIncreasing.addItemListener(listener);
        }
        return jRadioButtonIncreasing;
    }
    
    /**
     * @return The JRadioButton decreasing
     */
    protected JRadioButton getJRadioButtonDecreasing() {
        if (jRadioButtonDecreasing == null) {
            jRadioButtonDecreasing = new JRadioButton();
            jRadioButtonDecreasing.setName("jRadioButtonMonotonyDown");
            jRadioButtonDecreasing
                    .setText(stringDatabase.getString("NodeDomainValuesTablePanel.jRadioButtonMonotonyDown.Text"));
            jRadioButtonDecreasing.addItemListener(listener);
        }
        return jRadioButtonDecreasing;
    }
    
    /**
     *
     */
    protected void initButtonGroupMonotonyUpDown() {
        buttonGroup.add(jRadioButtonIncreasing);
        buttonGroup.add(jRadioButtonDecreasing);
    }
    
    /**
     * @return The JLabel unit
     */
    protected JLabel getJLabelUnit() {
        if (jLabelUnit == null) {
            jLabelUnit = new JLabel();
            jLabelUnit.setHorizontalAlignment(SwingConstants.LEADING);
            jLabelUnit.setHorizontalTextPosition(SwingConstants.RIGHT);
            jLabelUnit.setName("jLabelUnit");
            jLabelUnit.setText("New JLabel");
            jLabelUnit.setText(stringDatabase.getString("NodeDomainValuesTablePanel.jLabelUnit.Text"));
        }
        return jLabelUnit;
    }
    
    /**
     *
     */
    protected JTextField getJTextFieldUnit() {
        if (jFieldUnit == null) {
            jFieldUnit = new JTextField();
            jFieldUnit.setText(node.getVariable().getUnit().getString());
            jFieldUnit.setName("jFormattedTextFieldPrecision");
            jFieldUnit.addFocusListener(listener);
        }
        return jFieldUnit;
    }
    
    /**
     * get the label for the precision field
     *
     * @return the JLabelPrecision
     */
    protected JLabel getJLabelPrecision() {
        if (jLabelPrecision == null) {
            jLabelPrecision = new JLabel();
            jLabelPrecision.setHorizontalAlignment(SwingConstants.LEADING);
            jLabelPrecision.setHorizontalTextPosition(SwingConstants.RIGHT);
            jLabelPrecision.setName("jLabelPrecision");
            jLabelPrecision.setText("New JLabel");
            jLabelPrecision.setText(stringDatabase.getString("NodeDomainValuesTablePanel.jLabelPrecision.Text"));
        }
        return jLabelPrecision;
    }
    
    /**
     * get the TextField Precision field
     *
     * @return the precision field
     */
    // protected JFormattedTextField getJFormattedTextFieldPrecision() {
    protected JFormattedTextField getJFormattedTextFieldPrecision() {
        if (jFormattedTextFieldPrecision == null) {
            NumberFormatter dnFormat = new NumberFormatter(NumberFormat.getNumberInstance(Locale.ENGLISH));
            /*
             * DecimalFormat decimalFormat = new DecimalFormat();
             * NumberFormatter textFormatter = new
             * NumberFormatter(decimalFormat);
             * textFormatter.setOverwriteMode(true);
             * textFormatter.setAllowsInvalid(true);
             */
            DefaultFormatterFactory currFactory = new DefaultFormatterFactory(dnFormat, dnFormat, dnFormat);
            jFormattedTextFieldPrecision = new JFormattedTextField(currFactory);
            // NumberFormatter nf =
            // (NumberFormatter)jFormattedTextFieldPrecision.getFormatter();
            // jFormattedTextFieldPrecision = new JTextField();
            // nf.setCommitsOnValidEdit(true);
            jFormattedTextFieldPrecision.setName("jFormattedTextFieldPrecision");
            // jFormattedTextFieldPrecision.addActionListener( listener );
            // jFormattedTextFieldPrecision.addFocusListener( listener );
            jFormattedTextFieldPrecision.addPropertyChangeListener("value", listener);
            // jFormattedTextFieldPrecision.setDocument( new
            // ValidDoubleDocument() );
        }
        return jFormattedTextFieldPrecision;
    }
    
    /**
     * @return The JLabel values panel
     */
    protected JLabel getJLabelValuesPanel() {
        if (jLabelValuesPanel == null) {
            jLabelValuesPanel = new JLabel();
            jLabelValuesPanel.setName("jLabelValuesPanel");
            jLabelValuesPanel.setText("a Label");
            jLabelValuesPanel.setText(stringDatabase.getString("NodeDomainValuesTablePanel.jLabelValuesPanel.Text"));
        }
        return jLabelValuesPanel;
    }
    
    /**
     * This method initialises jLabelNodeValuesComment
     *
     * @return a new label for the comment
     */
    protected JTextArea getJTextAreaLabelNodeValuesComment() {
        if (jTextAreaLabelNodeValuesComment == null) {
            jTextAreaLabelNodeValuesComment = new JTextArea();
            jTextAreaLabelNodeValuesComment.setLineWrap(true);
            jTextAreaLabelNodeValuesComment.setOpaque(false);
            jTextAreaLabelNodeValuesComment.setName("jTextAreaLabelNetworkValuesComment");
            jTextAreaLabelNodeValuesComment.setFocusable(false);
            jTextAreaLabelNodeValuesComment.setEditable(false);
            jTextAreaLabelNodeValuesComment.setFont(getJLabelDomainValues().getFont());
            jTextAreaLabelNodeValuesComment.setText("an Extended Label");
            jTextAreaLabelNodeValuesComment.setText(
                    stringDatabase.getString("NodeDomainValuesTablePanel." + "jTextAreaLabelNodeValuesComment.Text"));
        }
        return jTextAreaLabelNodeValuesComment;
    }
    
    /**
     * This method initializes commentHTMLScrollPaneNodeValuesComment
     *
     * @return a new comment HTML scroll pane.
     */
    protected CommentHTMLScrollPane getCommentHTMLScrollPaneNodeValuesComment() {
        if (commentHTMLScrollPaneNodeValuesComment == null) {
            commentHTMLScrollPaneNodeValuesComment = new CommentHTMLScrollPane();
            commentHTMLScrollPaneNodeValuesComment
                    .setName("NodeDomainValuesTablePanel." + "commentHTMLScrollPaneNodeValuesComment");
        }
        return commentHTMLScrollPaneNodeValuesComment;
    }
    
    /**
     * Initialize the data structure for finite states variables
     *
     * @param states the states
     *
     * @return The data from states
     */
    protected static Object[][] getDataFromStates(State[] states) {
        int numColumns = 6; // key column is assigned in setData
        int rows = states.length;
        Object[][] data = new Object[rows][numColumns];
        for (int i = 0; i < rows; i++) {
            // data [i][0] = GUIDefaultStates.getValuesInAString(states[i].getName());
            data[i][0] = states[i].getName();
        }
        return data;
    }
    
    /**
     * Convert an array of strings in an array of arrays of objects with the
     * same elements. As the Elvira parser is still unable to process the
     * discretize values this method will separate the elements of each interval
     * in the proper values for the columns
     *
     * @param states array of strings.
     *
     * @return an array of arrays of objects that has the same elements.
     */
    // TODO this method must be changed when the Elvira parser will retrieve
    // data in an proper separated format
    protected Object[][] convertStringsToTableFormat(State[] states) {
        int numColumns = 6; // name-symbol-value-separator-value-symbol
        Object[][] data = new Object[states.length][numColumns];
        for (int i = 0; i < states.length; i++) {
            // for (i = numIntervals-1; i >= 0; i--) {
            int position = 0;
            // aString = GUIDefaultStates.getValuesInAString(states[i].getName());
            String aString = states[i].getName();
            // find name & lowSymbol
            int index = aString.indexOf('[');
            if (index < 0)
                index = aString.indexOf('(');
            String name = aString.substring(0, index);
            data[i][position++] = name; // position 0
            aString = aString.substring(index);
            String lowSymbol = aString.substring(0, 1);
            data[i][position++] = lowSymbol; // position 1
            // find lowValue
            aString = aString.substring(1);
            index = aString.indexOf(',');
            double lowValue = Double.parseDouble(aString.substring(0, index));
            data[i][position++] = lowValue; // position 2
            // find separator
            aString = aString.substring(index);
            data[i][position++] = aString.substring(0, 1); // position 3
            // find upperValue
            aString = aString.substring(1);
            index = aString.indexOf(']');
            if (index < 0)
                index = aString.indexOf(')');
            double upperValue = Double.parseDouble(aString.substring(0, index));
            data[i][position++] = upperValue; // position 4
            // find upperSymbol
            aString = aString.substring(index);
            String upperSymbol = aString.substring(0, 1);
            data[i][position++] = upperSymbol; // position 5
        }
        return data;
    }
    
    /**
     * Convert an array of strings in an array of arrays of objects with the
     * same elements.
     *
     * @param values array of strings.
     *
     * @return an array of arrays of objects that has the same elements.
     */
    protected static Object[][] convertStringsToTableDiscreteFormat(State[] values) {
        int l = values.length;
        Object[][] data = new Object[l][1];
        int i = l - 1;
        for (State value : values) {
            data[i--][0] = GUIDefaultStates.getString(value.getName());
        }
        return data;
    }
    
    /**
     * Convert an array of arrays of objects in an array of strings with the
     * same elements.
     *
     * @param values array of arrays of objects.
     *
     * @return array of strings that has the same elements.
     */
    protected static String[] convertTableFormatToStrings(Object[][] values) {
        int i;
        String oneValue;
        int l = values.length;
        String[] data = new String[l];
        // name-symbol-value-separator-value-symbol
        for (i = 0; i < l; i++) {
            // do nothing with values[i][0] = internal id
            oneValue = (String) values[i][1]; // name
            oneValue += (String) values[i][2]; // low limit symbol
            oneValue += values[i][3]; // low limit value
            oneValue += (String) values[i][4]; // comma separator
            oneValue += values[i][5]; // high limit value
            oneValue += (String) values[i][6]; // high limit symbol
            data[i] = oneValue;
        }
        return data;
    }
    
    /**
     * This method checks the states table, ensuring that there aren't
     * duplicated states and empty states.
     *
     * @return true if all the states are defined and appears only once.
     */
    public static boolean checkStates() {
        return true;
    }
    
    /**
     * This method initialises jLabelNodeType
     *
     * @return a new name label.
     */
    private JLabel getJLabelNodeVariableType() {
        if (jLabelNodeVariableType == null) {
            jLabelNodeVariableType = new JLabel();
            jLabelNodeVariableType.setName("jLabelNodeVariableType");
            jLabelNodeVariableType.setHorizontalAlignment(SwingConstants.LEFT);
            jLabelNodeVariableType.setHorizontalTextPosition(SwingConstants.LEFT);
            jLabelNodeVariableType.setText("a Label");
            jLabelNodeVariableType.setText(stringDatabase.getString("NodeDomainValuesTablePanel." + "jLabelNodeVariableType.Text"));
            jLabelNodeVariableType.setDisplayedMnemonic(stringDatabase.getString("NodeDomainValuesTablePanel.jLabelNodeVariableType.Mnemonic").charAt(0));
            // jLabelNodeVariableType.setLabelFor( getJPanelNodeType() );
        }
        return jLabelNodeVariableType;
    }
    
    private JComboBox<VariableType> getJComboBoxNodeVariableType() {
        if (jComboBoxNodeVariableType == null) {
            jComboBoxNodeVariableType = new JComboBox<VariableType>();
            jComboBoxNodeVariableType.setRenderer(new JComboBoxFunctionRender<VariableType>(VariableType::toString));
            jComboBoxNodeVariableType.setName("jComboBoxNodeVariableType");
            for (VariableType variableType : VariableType.of(node.getNodeType())) {
                jComboBoxNodeVariableType.addItem(variableType);
            }
            // jComboBoxNodeVariableType.setSize(181, 80);
            // jComboBoxNodeVariableType.addItemListener(listener);
            jComboBoxNodeVariableType.setSelectedItem(node.getVariable().getVariableType());
            jComboBoxNodeVariableType.addItemListener(this);
        }
        return jComboBoxNodeVariableType;
    }
    
    @Override @SuppressWarnings("unchecked") public void itemStateChanged(ItemEvent itemEvent) {
        int optionDeselected = 0;
        ItemSelectable itemSelectable = itemEvent.getItemSelectable();
        Object[] selected = itemSelectable.getSelectedObjects();
        String itemSelected = selected.length == 0 ? "null" : selected[0].toString();
        JComboBox<String> comboBox = (JComboBox<String>) itemEvent.getSource();
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        List<Node> nodeRelatedNodes = TemporalNetOperations.getRelatedNodesOtherTimeSlices(node);
        // @@@
        if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
            optionDeselected = comboBox.getSelectedIndex();
        }
        if (comboBox.getName().equals("jComboBoxNodeVariableType")) {
            if (itemSelected != null && itemEvent.getStateChange() == ItemEvent.SELECTED && !isUploadingData()) {
                VariableType variableType = (VariableType) jComboBoxNodeVariableType.getSelectedItem();
                VariableTypeEdit variableTypeEdit = new VariableTypeEdit(node, variableType, true);
                ProbNet nodeProbNet = node.getProbNet();
                try {
                    variableTypeEdit.executeEdit();
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
                // @ 2014/11/18. Issue 145.
                // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
                // Propagation of the domain in related variables in temporal models
                if (nodeRelatedNodes != null) {
                    if (!nodeRelatedNodes.isEmpty()) {
                        for (Node relatedNode : nodeRelatedNodes) {
                            variableTypeEdit = new VariableTypeEdit(relatedNode, variableType, true);
                            ProbNet probNet = relatedNode.getProbNet();
                            try {
                                variableTypeEdit.executeEdit();
                            } catch (DoEditException e) {
                                throw new UnrecoverableException(e);
                            }
                        }
                    }
                }
                // @@@
                this.removeAll();
                initialize();
                setFieldsFromProperties(node);
                
            }
        } else if (comboBox.getName().equals("jComboBoxStatesValues")) {
            // warning mpalacios relative function to options position.
            // Review "otros" option
            if (itemSelected != null && itemEvent.getStateChange() == ItemEvent.SELECTED) {
                int i = 0;
                State[] newStates = new State[DefaultStates.getByIndex(comboBox.getSelectedIndex()).length];
                for (String str : DefaultStates.getByIndex(comboBox.getSelectedIndex())) {
                    newStates[i] = new State(str);
                    i++;
                }
                NodeReplaceStatesEdit nodeReplaceStatesEdit = new NodeReplaceStatesEdit(node, newStates);
                try {
                    ProbNet probNet1 = node.getProbNet();
                    nodeReplaceStatesEdit.executeEdit();
                    // @ 2014/11/18. Issue 145.
                    // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
                    // Propagation of the domain in related variables in temporal models
                    if (nodeRelatedNodes != null) {
                        if (!nodeRelatedNodes.isEmpty()) {
                            for (Node relatedNode : nodeRelatedNodes) {
                                nodeReplaceStatesEdit = new NodeReplaceStatesEdit(relatedNode, newStates);
                                ProbNet probNet = relatedNode.getProbNet();
                                nodeReplaceStatesEdit.executeEdit();
                            }
                        }
                    }
                    // @@@
                    this.removeAll();
                    initialize();
                    setFieldsFromProperties(node);
                } catch (DoEditException e) {
                    comboBox.setSelectedIndex(optionDeselected);
                    comboBox.requestFocus();
                    throw new UnrecoverableException(e);
                }
            }
            // PRECISION
        } else if (comboBox.getName().equals("jComboBoxPrecision")) {
            if (itemSelected != null && itemEvent.getStateChange() == ItemEvent.SELECTED) {
                if (node.getVariable().getPrecision() != Double.parseDouble(itemSelected)) {
                    PrecisionEdit precisionEdit = new PrecisionEdit(node, Double.parseDouble(itemSelected));
                    try {
                        ProbNet probNet1 = node.getProbNet();
                        precisionEdit.executeEdit();
                        // @ 2014/11/18. Issue 145.
                        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
                        // Propagation of the domain in related variables in temporal models
                        if (nodeRelatedNodes != null) {
                            if (!nodeRelatedNodes.isEmpty()) {
                                for (Node relatedNode : nodeRelatedNodes) {
                                    precisionEdit = new PrecisionEdit(relatedNode, Double.parseDouble(itemSelected));
                                    ProbNet probNet = relatedNode.getProbNet();
                                    precisionEdit.executeEdit();
                                }
                            }
                        }
                        // @@@
                    } catch (DoEditException e1) {
                        throw new UnrecoverableException(e1);
                    }
                }
            }
            if (node.getVariable().getVariableType() == VariableType.DISCRETIZED
                    || node.getVariable().getVariableType() == VariableType.NUMERIC) {
                // double precision = (Double)
                // getPanel().getJFormattedTextFieldPrecision().getValue();
                double precision = Double.parseDouble(itemSelected);
                double[] limits = node.getVariable().getPartitionedInterval().getLimits();
                boolean[] belongs = node.getVariable().getPartitionedInterval().getBelongsToLeftSide();
                for (int i = 0; i < limits.length; i++) {
                    if (limits[i] != Double.POSITIVE_INFINITY && limits[i] != Double.NEGATIVE_INFINITY) {
                        double newLimit = Util.roundWithPrecision(limits[i], itemSelected);
                        if (limits[i] != newLimit) {
                            limits[i] = newLimit;
                            int j = i;
                            while (j + 1 <= limits.length - 1 && limits[j] >= limits[j + 1]) {
                                if (!belongs[j] && belongs[j + 1]) {
                                    limits[j + 1] = limits[j];
                                } else {
                                    if (j + 1 == limits.length - 1) {
                                        limits[j + 1] = Double.POSITIVE_INFINITY;
                                        break;
                                    } else
                                        limits[j + 1] = limits[j] + precision;
                                }
                                j++;
                            }
                            // previous limits
                            int k = i;
                            while (k - 1 >= 0 && limits[k] <= limits[k - 1]) {
                                if (belongs[k] && !belongs[k - 1]) {
                                    limits[k - 1] = limits[k];
                                } else {
                                    if (k - 1 == 0) {
                                        limits[k - 1] = Double.NEGATIVE_INFINITY;
                                        break;
                                    } else
                                        limits[k - 1] = limits[k] - precision;
                                }
                                k--;
                            }
                        } else {
                            limits[i] = newLimit;
                        }
                    }
                }
                for (int m = 0; m < limits.length; m++) {
                    if (limits[m] != Double.POSITIVE_INFINITY && limits[m] != Double.NEGATIVE_INFINITY) {
                        limits[m] = Util.roundWithPrecision(limits[m], itemSelected);
                    }
                }
                PartitionedInterval newPartitionedInterval = new PartitionedInterval(limits, belongs);
                PartitionedIntervalEdit partitionedIntervalEdit = new PartitionedIntervalEdit(node,
                                                                                              newPartitionedInterval);
                
                try {
                    partitionedIntervalEdit.executeEdit();
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
                // @ 2014/11/18. Issue 145.
                // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
                // Propagation of the domain in related variables in temporal models
                if (nodeRelatedNodes != null) {
                    if (!nodeRelatedNodes.isEmpty()) {
                        for (Node relatedNode : nodeRelatedNodes) {
                            partitionedIntervalEdit = new PartitionedIntervalEdit(relatedNode,
                                                                                  newPartitionedInterval);
                            ProbNet probNet = relatedNode.getProbNet();
                            try {
                                partitionedIntervalEdit.executeEdit();
                            } catch (DoEditException e) {
                                throw new UnrecoverableException(e);
                            }
                        }
                    }
                    
                }
                // @@@
                PartitionedInterval newPartitionInterval = node.getVariable().getPartitionedInterval();
                State[] states = node.getVariable().getStates();
                getDiscretizedStatesPanel().setDataFromPartitionedInterval(newPartitionInterval, states);
            }
        }
    }
    
    public boolean isUploadingData() {
        return uploadingData;
    }
    
    public void setUploadingData(boolean uploadingData) {
        this.uploadingData = uploadingData;
    }
    
    @Override public void actionPerformed(ActionEvent arg0) {
        String actionComand = arg0.getActionCommand();
        if (actionComand.equals("StandardDomain")) {
            try {
                actionPerformedStandardDomain(arg0);
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
        }
    }
    
    private void actionPerformedStandardDomain(ActionEvent arg0) throws DoEditException {
        StandardDomainsDialog standardDomainDialog = new StandardDomainsDialog(GUIUtils.getOwner(this));
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        List<Node> nodeRelatedNodes = TemporalNetOperations.getRelatedNodesOtherTimeSlices(node);
        //
        if (standardDomainDialog.requestValues() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        List<JRadioButton> radioButtons = ((StandardDomainPanel) (standardDomainDialog.getJPanelStandardDomains()))
                .getRadioButtons();
        int index = 0;
        for (int j = 0; j < radioButtons.size(); j++) {
            if (radioButtons.get(j).isSelected()) {
                index = j;
            }
        }
        int i = 0;
        State[] newStates = new State[DefaultStates.getByIndex(index).length];
        for (String str : DefaultStates.getByIndex(index)) {
            newStates[i] = new State(GUIDefaultStates.getString(str));
            i++;
        }
        NodeReplaceStatesEdit nodeReplaceStatesEdit = new NodeReplaceStatesEdit(node, newStates);
        
        ProbNet nodeProbNet = node.getProbNet();
        nodeReplaceStatesEdit.executeEdit();
        // @ 2014/11/18. Issue 145.
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
        // Propagation of the domain in related variables in temporal models
        if (nodeRelatedNodes != null) {
            if (!nodeRelatedNodes.isEmpty()) {
                for (Node relatedNode : nodeRelatedNodes) {
                    nodeReplaceStatesEdit = new NodeReplaceStatesEdit(relatedNode, newStates);
                    ProbNet probNet = relatedNode.getProbNet();
                    nodeReplaceStatesEdit.executeEdit();
                }
            }
        }
        // @@@
        this.removeAll();
        initialize();
        setFieldsFromProperties(node);
        
    }
}
