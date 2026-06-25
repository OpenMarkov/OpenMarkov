/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.core.PrecisionEdit;
import org.openmarkov.core.action.core.UnitEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.gui.action.PartitionedIntervalEdit;
import org.openmarkov.gui.component.DiscretizeTablePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

/**
 * Auxiliary class to handle events for the primary class
 * NodeDiscretizeValuesTablePanel
 *
 * @author jlgozalo
 * @version 1.0 5 Feb 2010
 */
public class NodeDomainValuesTablePanelListener
        implements ActionListener, ItemListener, FocusListener, PropertyChangeListener {
    private static final int DOWN = 0;
    private static final int UP = 1;
    /**
     * the panel to handle the events
     */
    private final NodeDomainValuesTablePanel panel;
    private int previousMonotony = -1;
    
    /**
     * Constructor
     *
     * @param panel - the panel to handle the events
     */
    public NodeDomainValuesTablePanelListener(NodeDomainValuesTablePanel panel) {
        this.panel = panel;
    }
    
    /**
     * @return the panel
     */
    private NodeDomainValuesTablePanel getPanel() {
        return panel;
    }
    
    @Override public void actionPerformed(ActionEvent event) {
    }
    
    // button initially selected down
    @Override public void itemStateChanged(ItemEvent e) {
        getPanel().getDiscretizedStatesPanel().getNegativeInfinityButton().setVisible(false);
        getPanel().getDiscretizedStatesPanel().getNegativeInfinityButton().setEnabled(false);
        getPanel().getDiscretizedStatesPanel().getPositiveInfinityButton().setVisible(false);
        getPanel().getDiscretizedStatesPanel().getPositiveInfinityButton().setEnabled(false);
        if (e.getItem().equals(getPanel().getJRadioButtonIncreasing())) {
            itemStateChangedUp(e);
        }
        if (e.getItem().equals(getPanel().getJRadioButtonDecreasing())) {
            itemStateChangedDown(e);
        }
    }
    
    private void itemStateChangedUp(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            previousMonotony = UP; // deselected up
            getPanel().getDiscretizedStatesPanel().setUpMonotony(false);
        } else if (e.getStateChange() == ItemEvent.SELECTED) {
            if (previousMonotony == UP) { // UP --&gt; UP
                // do nothing
                getPanel().getDiscretizedStatesPanel().setUpMonotony(true);
            } else if (previousMonotony == DOWN) { // DOWN --&gt; UP
                DiscretizeTablePanel panel = getPanel().getDiscretizedStatesPanel();
                panel.setUpMonotony(true);
                Object[][] data = panel.getData();
                Object[][] intermediateRows = new Object[data.length][data[0].length - 2];
                for (int i = 0; i < data.length; i++) {// for each row
                    for (int j = 2; j < data[0].length; j++) {
                        intermediateRows[i][j - 2] = data[i][j];
                    }
                }
                for (int i = 0; i < intermediateRows.length; i++) {
                    for (int j = 0; j < intermediateRows[0].length; j++) {
                        data[i][j + 2] = intermediateRows[intermediateRows.length - 1 - i][j];
                    }
                }
                Object[][] newData = new Object[data.length][data[0].length - 1];
                for (int i = 0; i < data.length; i++) {
                    for (int j = 1; j < data[0].length; j++) {
                        newData[i][j - 1] = data[i][j];
                    }
                }
                panel.setData(newData); // set data fill the first key column
            }
        }
    }
    
    private void itemStateChangedDown(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            previousMonotony = DOWN;// deselected down
            getPanel().getDiscretizedStatesPanel().setUpMonotony(true);
        } else if (e.getStateChange() == ItemEvent.SELECTED) {
            if (previousMonotony == UP) { // UP --&gt; DOWN
                DiscretizeTablePanel panel = getPanel().getDiscretizedStatesPanel();
                panel.setUpMonotony(false);
                Object[][] data = panel.getData();
                Object[][] intermediateRows = new Object[data.length][data[0].length - 2];
                for (int i = 0; i < data.length; i++) {// for each row
                    for (int j = 2; j < data[0].length; j++) {
                        intermediateRows[i][j - 2] = data[i][j];
                    }
                }
                for (int i = 0; i < intermediateRows.length; i++) {
                    for (int j = 0; j < intermediateRows[0].length; j++) {
                        data[i][j + 2] = intermediateRows[intermediateRows.length - 1 - i][j];
                    }
                }
                Object[][] newData = new Object[data.length][data[0].length - 1];
                for (int i = 0; i < data.length; i++) {
                    for (int j = 1; j < data[0].length; j++) {
                        newData[i][j - 1] = data[i][j];
                    }
                }
                panel.setData(newData); // set data fill the first key column
            } else if (previousMonotony == DOWN) { // DOWN --&gt; DOWN
                // do nothing
                getPanel().getDiscretizedStatesPanel().setUpMonotony(false);
            }
        }
    }
    
    /**
     * Invoked when an item of the type of states of the node has been selected.
     *
     * @param e event information.
     */
    protected void subItemStateChanged(ItemEvent e) {
        if (e.getItemSelectable().equals(getPanel().getJComboBoxStatesValues())) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // getPanel().getNodeDiscretizedValuesTablePanel().setNewDataInTable(
                // getPanel().getJComboBoxStatesValues().getSelectedIndex() );
            }
        }
    }
    
    @Override public void focusGained(FocusEvent e) {
        if (e.getSource().equals(getPanel().getJFormattedTextFieldPrecision())) {
            System.out.println("precision focus gained");
            getPanel().getJFormattedTextFieldPrecision().selectAll();
        } else if (e.getSource().equals(getPanel().getJTextFieldUnit())) {
            getPanel().getJTextFieldUnit().selectAll();
        }
    }
    
    @Override public void focusLost(FocusEvent evt) {
        if (evt.getSource().equals(getPanel().getJFormattedTextFieldPrecision())) {
            PrecisionEdit precisionEdit = new PrecisionEdit(getPanel().getNode(),
                                                            (Double) getPanel().getJFormattedTextFieldPrecision()
                                                                               .getValue());
            try {
                precisionEdit.executeEdit();
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
            System.out.println("precision set to " + ((Double) getPanel().getJFormattedTextFieldPrecision().getValue())
                    .toString());
        } else if (evt.getSource().equals(getPanel().getJTextFieldUnit())) {
            UnitEdit unitEdit = new UnitEdit(getPanel().getNode(), getPanel().getJTextFieldUnit().getText());
            try {
                unitEdit.executeEdit();
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
            getPanel().getJTextFieldUnit().setText(getPanel().getJTextFieldUnit().getText());
        }
    }
    
    @Override public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(getPanel().getJFormattedTextFieldPrecision())) {
            PrecisionEdit precisionEdit = new PrecisionEdit(panel.getNode(),
                                                            (Double) getPanel().getJFormattedTextFieldPrecision()
                                                                               .getValue());
            ProbNet probNet = getPanel().getNode().getProbNet();
            try {
                precisionEdit.executeEdit();
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setGroupingUsed(false); // don't group by threes
            if (getPanel().getNode().getVariable().getVariableType() == VariableType.DISCRETIZED
                    || getPanel().getNode().getVariable().getVariableType() == VariableType.NUMERIC) {
                double precision = (Double) getPanel().getJFormattedTextFieldPrecision().getValue();
                double[] limits = getPanel().getNode().getVariable().getPartitionedInterval().getLimits();
                boolean[] belongs = getPanel().getNode().getVariable().getPartitionedInterval().getBelongsToLeftSide();
                for (int i = 0; i < limits.length; i++) {
                    if (limits[i] != Double.POSITIVE_INFINITY && limits[i] != Double.NEGATIVE_INFINITY) {
                        double newLimit = Util.roundWithPrecision(limits[i], Double.toString(precision));
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
                        limits[m] = Util.roundWithPrecision(limits[m], Double.toString(precision));
                    }
                }
                PartitionedInterval newPartitionedInterval = new PartitionedInterval(limits, belongs);
                PartitionedIntervalEdit partitionedIntervalEdit = new PartitionedIntervalEdit(getPanel().getNode(),
                                                                                              newPartitionedInterval);
                ProbNet panelProbNet = getPanel().getNode().getProbNet();
                try {
                    partitionedIntervalEdit.executeEdit();
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
                PartitionedInterval newPartitionInterval = getPanel().getNode().getVariable().getPartitionedInterval();
                State[] states = getPanel().getNode().getVariable().getStates();
                getPanel().getDiscretizedStatesPanel().setDataFromPartitionedInterval(newPartitionInterval, states);
            }
        } else if (evt.getSource().equals(getPanel().getJTextFieldUnit())) {
            UnitEdit unitEdit = new UnitEdit(getPanel().getNode(), getPanel().getJTextFieldUnit().getText());
            try {
                ProbNet probNet = getPanel().getNode().getProbNet();
                unitEdit.executeEdit();
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
        }
    }
}
