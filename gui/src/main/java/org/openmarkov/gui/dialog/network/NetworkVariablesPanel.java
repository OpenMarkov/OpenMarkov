/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.core.NetworkDefaultStatesEdit;
import org.openmarkov.core.action.core.RemoveConstraintEdit;
import org.openmarkov.core.action.core.VariableTypeConstraintEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.DefaultStates;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.constraint.OnlyContinuousVariables;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.util.GUIDefaultStates;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.util.List;

/**
 * Panel to set the definition of the variables of a network. It will have a
 * variable type group selector with two check boxes, and a drop-down list with
 * the default values for the nodes
 *
 * @author jlgozalo
 * @version 1.0 jlgozalo initial
 */
public class NetworkVariablesPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -5183671164848473079L;
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * Label of the variables Type checkboxes
     */
    private JLabel jLabelVariablesType = null;
    /**
     * Label of the default states field.
     */
    private JLabel jLabelDefaultStates = null;
    /**
     * Combobox where the user can choose the default states.
     */
    private JComboBox<String> jComboBoxDefaultStates = null;
    /**
     * Specifies if the network whose additionalProperties are edited is new.
     */
    private final boolean newNetwork;
    private ProbNet probNet;
    private JComboBox<String> jComboBoxVariableType;
    
    /**
     * constructor without construction parameters
     */
    public NetworkVariablesPanel(ProbNet probNet) {
        this.probNet = probNet;
        this.newNetwork = probNet == null;
        setName("NetworkVariablesPanel");
        initialize();
        fill();
    }
    
    /**
     * This method initialises this instance.
     */
    public NetworkVariablesPanel() {
        this.newNetwork = true;
        setName("NetworkVariablesPanel");
        initialize();
        fill();
    }
    
    /**
     * initialises the layout for this panel.
     */
    private void initialize() {
        final GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup()
                           .addGap(26)
                           .addGroup(
                                   groupLayout.createParallelGroup(Alignment.LEADING)
                                              .addGroup(groupLayout.createSequentialGroup()
                                                                   .addComponent(getJLabelVariablesType(), GroupLayout.PREFERRED_SIZE, 125,
                                                                                 GroupLayout.PREFERRED_SIZE)
                                                                   .addPreferredGap(ComponentPlacement.RELATED)
                                                                   .addComponent(getJComboBoxVariableType(), GroupLayout.PREFERRED_SIZE, 194,
                                                                                 GroupLayout.PREFERRED_SIZE))
                                              .addGroup(groupLayout.createSequentialGroup()
                                                                   .addComponent(getJLabelDefaultStates(), GroupLayout.PREFERRED_SIZE, 125,
                                                                                 GroupLayout.PREFERRED_SIZE)
                                                                   .addPreferredGap(ComponentPlacement.RELATED)
                                                                   .addComponent(getJComboBoxDefaultStates(), GroupLayout.PREFERRED_SIZE, 194,
                                                                                 GroupLayout.PREFERRED_SIZE)))
                           .addContainerGap(17, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup()
                           .addGap(11)
                           .addGroup(
                                   groupLayout.createParallelGroup(Alignment.BASELINE)
                                              .addComponent(getJLabelVariablesType(), GroupLayout.PREFERRED_SIZE, 39,
                                                            GroupLayout.PREFERRED_SIZE)
                                              .addComponent(getJComboBoxVariableType(), GroupLayout.PREFERRED_SIZE,
                                                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                           .addGap(6)
                           .addGroup(
                                   groupLayout.createParallelGroup(Alignment.BASELINE)
                                              .addComponent(getJLabelDefaultStates())
                                              .addComponent(getJComboBoxDefaultStates(), GroupLayout.PREFERRED_SIZE,
                                                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                           .addContainerGap(224, Short.MAX_VALUE)));
        setLayout(groupLayout);
    }
    
    /**
     * This method initialises jLabelVariablesType
     *
     * @return the Variables Type JLabel
     */
    private JLabel getJLabelVariablesType() {
        if (jLabelVariablesType == null) {
            jLabelVariablesType = new JLabel();
            jLabelVariablesType.setName("jLabelVariablesType");
            jLabelVariablesType.setText("a Label : ");
            jLabelVariablesType.setText(stringDatabase.getString("NetworkVariablesPanel.jLabelVariablesType.Text"));
        }
        return jLabelVariablesType;
    }
    
    /**
     * This method initialises jLabelDefaultStates.
     *
     * @return the Default States JLabel
     */
    private JLabel getJLabelDefaultStates() {
        if (jLabelDefaultStates == null) {
            jLabelDefaultStates = new JLabel();
            jLabelDefaultStates.setName("jLabelDefaultStates");
            jLabelDefaultStates.setText("a Label : ");
            jLabelDefaultStates.setText(stringDatabase.getString("NetworkVariablesPanel.jLabelDefaultStates.Text"));
            jLabelDefaultStates.setLabelFor(jComboBoxDefaultStates);
        }
        return jLabelDefaultStates;
    }
    
    /**
     * This method initialises jComboBoxDefaultStates.
     *
     * @return the Default States JCombo Box
     */
    private JComboBox<String> getJComboBoxDefaultStates() {
        if (jComboBoxDefaultStates == null) {
            jComboBoxDefaultStates = new JComboBox<String>(GUIDefaultStates.getListStrings());
            jComboBoxDefaultStates.setName("jComboBoxDefaultStates");
            // jComboBoxDefaultStates.addItemListener(this);
        }
        return jComboBoxDefaultStates;
    }
    
    /**
     * This method initialises jComboBoxDefaultStates.
     *
     * @return the Default States JCombo Box
     */
    private JComboBox<String> getJComboBoxVariableType() {
        if (jComboBoxVariableType == null) {
            jComboBoxVariableType = new JComboBox<String>(getListOfTypes());
            jComboBoxVariableType.setName("jComboBoxVariableType");
        }
        return jComboBoxVariableType;
    }
    
    private String[] getListOfTypes() {
        // TODO only discrete variable are enabled
        String[] types = {
                stringDatabase.getString("NetworkVariablesPanel.ConstraintVariableType.Items." + "onlydiscrete"),
                stringDatabase.getString(
                        "NetworkVariablesPanel.ConstraintVariableType." + "Items.discreteandcontinuous")};
        return types;
    }
    
    /**
     * This method fills the content of the fields from a network Properties
     * (ProbNet object)
     */
    private void fill() {
        if (!newNetwork) {
            State[] states = probNet.getDefaultStates();
            // TODO modificar el indice a 2 cuando la creacion de variables
            // continuas este implementado
            int index = 1;
            for (PNConstraint constraint : probNet.getConstraints()) {
                if (constraint instanceof OnlyDiscreteVariables) {
                    index = 0;
                    break;
                }
                /*
                 * }else if (constraint instanceof OnlyContinuousVariables){
                 * index=1; break; }
                 */
            }
            jComboBoxVariableType.setSelectedIndex(index);
            jComboBoxDefaultStates.setSelectedIndex(DefaultStates.getIndex(states));
            jComboBoxVariableType.addActionListener(arg0 -> {
                try {
                    variableTypeChanged();
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
            });
            jComboBoxDefaultStates.addActionListener(arg0 -> {
                try {
                    defaultStatesChanged();
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
            });
        }
    }
    
    private void variableTypeChanged() throws DoEditException {
        VariableTypeConstraintEdit variableTypeCE = null;
        RemoveConstraintEdit removeCE;
        Object itemSelected = jComboBoxVariableType.getSelectedItem();
        if (itemSelected != null && itemSelected.equals(stringDatabase
                                                                .getString("NetworkVariablesPanel.ConstraintVariableType." + "Items.onlydiscrete"))) {

            variableTypeCE = new VariableTypeConstraintEdit(probNet, new OnlyDiscreteVariables());

        } else if (itemSelected != null && itemSelected.equals(stringDatabase
                                                                       .getString("NetworkVariablesPanel.ConstraintVariableType." + "Items.onlycontinuous"))) {

            variableTypeCE = new VariableTypeConstraintEdit(probNet, new OnlyContinuousVariables());

        } else if (itemSelected != null && itemSelected.equals(stringDatabase
                                                                        .getString("NetworkVariablesPanel.ConstraintVariableType." + "Items.discreteandcontinuous"))) {

            List<PNConstraint> constraints = probNet.getConstraints().stream()
                    .filter(o -> o.equals(new OnlyDiscreteVariables()) || o.equals(new OnlyContinuousVariables()))
                    .toList();
            if(!constraints.isEmpty()){
                removeCE = new RemoveConstraintEdit(probNet, constraints.getFirst());
                removeCE.executeEdit();
            }

        }
        if (variableTypeCE != null) {
            variableTypeCE.executeEdit();
        }
    }
    
    private void defaultStatesChanged() throws DoEditException {
        // warning mpalacios relative function to options position.
        // Review "otros" option
        Object itemSelected = jComboBoxDefaultStates.getSelectedItem();
        if (itemSelected == null) {
            return;
        }
        NetworkDefaultStatesEdit networkDefaultStatesEdit = new NetworkDefaultStatesEdit(probNet, getDefaultStates());
        networkDefaultStatesEdit.executeEdit();
    }
    
    public State[] getDefaultStates() {
        int i = 0;
        int selectedIndex = jComboBoxDefaultStates.getSelectedIndex();
        String[] defaultStateNames = DefaultStates.getByIndex(selectedIndex);
        State[] defaultStates = new State[defaultStateNames.length];
        for (String str : defaultStateNames) {
            defaultStates[i] = new State(str);
            i++;
        }
        return defaultStates;
    }
}
