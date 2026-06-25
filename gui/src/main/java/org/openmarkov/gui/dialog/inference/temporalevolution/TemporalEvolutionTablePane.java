/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.jfree.data.xy.XYSeries;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.component.ValuesTable;
import org.openmarkov.gui.component.ValuesTableCellRenderer;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.*;

/**
 * Table to show temporal evolution of temporal variables
 *
 * @author myebra
 * @version 2 cyago - 09/11/2022 - temporal evolution by criterion and discount added
 */
@SuppressWarnings("serial")
public class TemporalEvolutionTablePane extends JScrollPane {

    //09/11/2022 - Took out of the constructor for avoiding code repetition
    final TableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
        private final DecimalFormat formatter = new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (value instanceof Double) {
                if ((Double) value <= 100.0) {
                    value = formatter.format(value);
                } else {
                    value = Math.round((Double) value);
                }
            }
            if (column == 0) {
                setBackground(GUIColors.Tables.FROZEN_CELL_BACKGROUND.getColor());
                setForeground(GUIColors.Tables.FROZEN_CELL_FOREGROUND.getColor());
            } else {
                var colors = GUIColors.Tables.EDITABLE_CELL_COLOR.getEditableCellColor(isSelected, row, column-1);
                if (colors.background != null) {
                    setBackground(colors.background.getColor());
                }
                if (colors.foreground != null) {
                    setForeground(colors.foreground.getColor());
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    };
    private final JTable table;



    public TemporalEvolutionTablePane(Map<Variable, TablePotential> temporalEvolution, ProbNet expandedNetwork,
                                      Variable variableOfInterest, List<Variable> conditioningVariables, int numSlices, boolean isUtility,
                                      boolean isCumulative) {
        super();

        Map<Integer, double[]> temporalEvolutionValues = new LinkedHashMap<>();
        Set<Variable> variables = temporalEvolution.keySet();
        int timeSlice = 0;
        for (int i = 0; i < variables.size(); i++) {
            boolean found = false;
            while (!found) {
                for (Variable variable : variables) {
                    if (variable.getTimeSlice() == timeSlice) {
                        found = true;
                        temporalEvolutionValues.put(timeSlice, temporalEvolution.get(variable).getValues());
                    }
                }
                timeSlice++;
            }
        }
        int numColumns = timeSlice + conditioningVariables.size() + 1;
        if (isUtility) {
            numColumns--;
        }

        int numRows = variableOfInterest.getNumStates();
        for (int i = 0; i < conditioningVariables.size(); i++) {
            numRows *= conditioningVariables.get(i).getNumStates();
        }

        NonEditableModel model = new NonEditableModel();
        table = new JTable(model);

        model.setColumnCount(numColumns);
        model.setNumRows(numRows);
        
        //TODO: info is just initialized, but info is never used in the function, nor it is returned.
        // Why is it created then?
        final Object[][] info = new Object[numRows][numColumns];

        // Fill conditioning variables
        int lastColumnIndex;
        for (lastColumnIndex = 0; lastColumnIndex < conditioningVariables.size(); lastColumnIndex++) {
            String columnName = conditioningVariables.get(lastColumnIndex).getName();
            table.getColumnModel().getColumn(lastColumnIndex).setHeaderValue(columnName);

            for (int i = 0; i < numRows; i++) {
                int stateIndex = (i / variableOfInterest.getNumStates()) % conditioningVariables.get(lastColumnIndex)
                        .getNumStates();
                String stateName = conditioningVariables.get(lastColumnIndex).getStateName(stateIndex);
                model.setValueAt(stateName, i, lastColumnIndex);
            }
        }

        if (!isUtility) {
            // States of the Variable of Interest
            table.getColumnModel().getColumn(lastColumnIndex).setHeaderValue(StringDatabase.getUniqueInstance().
                                                                                           getString("TemporalEvolutionResultDialog.States"));
            for (int i = 0; i < numRows; i++) {
                info[i][lastColumnIndex] = variableOfInterest.getStateName(i % variableOfInterest.getNumStates());
                model.setValueAt(variableOfInterest.getStateName(i % variableOfInterest.getNumStates()), i, lastColumnIndex);
            }
            lastColumnIndex++;
        }

//cmyago 09/11/2022 - Took out of the constructor for avoiding code repetition
//		TableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
//			private DecimalFormat formatter = new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));
//
//			@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//					boolean hasFocus, int row, int column) {
//				if (value instanceof Double) {
//					if ((Double) value <= 100.0) {
//						value = formatter.format((Double) value);
//					} else {
//						value = Math.round((Double) value);
//					}
//				}
//				if (column == 0) {
//					setBackground(GUIColors.Potential.Tables.HEADER_BACKGROUND.getColor());
//				} else {
//					setBackground(Color.WHITE);
//				}
//				setForeground(Color.BLACK);
//				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//			}
//		};
//cmyago end

        // Fill data
        Double[] values = new Double[numRows];
        Arrays.fill(values, 0.0);
        for (int cycle = 0; cycle < timeSlice; ++cycle) { // column
            int columnIndex = lastColumnIndex + cycle;
            table.getColumnModel().getColumn(columnIndex).setHeaderValue(cycle);
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(cellRenderer);
            for (int i = 0; i < numRows; i++) {// row
                if (isUtility && isCumulative) {
                    if (temporalEvolutionValues.containsKey(cycle)) {
                        values[i] += temporalEvolutionValues.get(cycle)[i];
                    }
                    // cell(row, column) = cell(i+1, j+1)
                } else {
                    if (temporalEvolutionValues.containsKey(cycle)) {
                        values[i] = temporalEvolutionValues.get(cycle)[i];
                    } else {
                        values[i] = 0.0;
                    }
                    // cell(row, column) = cell(i+1, j+1)
                }
                info[i][columnIndex] = values[i];
                model.setValueAt(values[i], i, columnIndex);
            }
        }

        //table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setViewportView(table);
        setAutoscrolls(true);
    }

    //09/11/2022 constructor for utility nodes and criteria addressing discount, upfront costs, and state filters
    /**
     * Constructor for getting temporal evolution by criterion table.
     * Displays a table with the temporal evolution of the criteria list given by criterionNames
     *
     * @param decisionVariable conditioning decision
     * @param markedCheckBoxes criteria to be shown
     * @param criterionNames   names of the criteria whose temporal evolution is shown
     * @param arrayXYSeries    list of XYSeries each of one contain one row of the table
     * @param timeSlice        number of time slices to be written (columns)
     */
    public TemporalEvolutionTablePane(Variable decisionVariable, boolean[] markedCheckBoxes, boolean isCumulative, List<String> criterionNames, List<XYSeries> arrayXYSeries, int timeSlice) {
        super();
        // decision + criterion +  value[0] +...+ value[timeSlice]
        int numColumns = 2 + (1 + timeSlice);
        // series marked as true
        int numRows = 0;
        for (int i = 0; i < markedCheckBoxes.length; i++) {
            numRows += markedCheckBoxes[i] ? decisionVariable.getNumStates() : 0;    //arrayXYSeries.size();
        }


        //first slice when variable sequence exists
        int firstSlice = arrayXYSeries.get(0).getX(0).intValue();


        NonEditableModel model = new NonEditableModel();
        table = new JTable(model);

        model.setColumnCount(numColumns);
        model.setNumRows(numRows);


        int firstDataColumn = 2;


        //Fill headers
        int columnIndex;
        //First column with decision
        table.getColumnModel().getColumn(0).setHeaderValue(decisionVariable.getName());
        table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
        //Second column with decision criteria
        table.getColumnModel().getColumn(1).setHeaderValue(StringDatabase.getUniqueInstance().getString("TemporalEvolutionTable.Headers.Criterion"));
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
        //Slices
        for (columnIndex = 0; columnIndex <= timeSlice; columnIndex++) {
            table.getColumnModel().getColumn(firstDataColumn + columnIndex).setHeaderValue(columnIndex);
            table.getColumnModel().getColumn(firstDataColumn + columnIndex).setCellRenderer(cellRenderer);
        }

        // Fill String values (policies and criteria) and upfront values
        int row = 0;
        boolean[] showRow = new boolean[decisionVariable.getNumStates() * criterionNames.size()];
        int seriesIndex = 0;
        for (int i = 0; i < decisionVariable.getNumStates(); i++) {
            for (int j = 0; j < criterionNames.size(); j++) {
                showRow[seriesIndex] = false;
                if (markedCheckBoxes[j]) {
                    String stateName = decisionVariable.getStateName(i);
                    model.setValueAt(stateName, row, 0);
                    model.setValueAt(criterionNames.get(j), row++, 1);
                    showRow[seriesIndex] = true;
                }
                seriesIndex++;
            }

        }

        //Values
        row = 0;
        seriesIndex = 0;
        for (XYSeries xySeries : arrayXYSeries) {
            if (showRow[seriesIndex++]) {
                columnIndex = firstDataColumn;
                //Temporal sequence doesn't exist
                for (int slice = 0; slice < firstSlice; slice++) {
                    model.setValueAt("-", row, columnIndex++);
                }
                //Temporal sequence first value
                model.setValueAt(xySeries.getY(0), row, columnIndex++);
                //Temporal sequence rest of values
                for (int slice = 1; slice < xySeries.getItemCount(); slice++) {
                    if (isCumulative) {
                        double previousValue = ((Number) (model.getValueAt(row, columnIndex - 1))).doubleValue();
                        model.setValueAt(previousValue + xySeries.getY(slice).doubleValue(), row, columnIndex++);
                    } else {
                        model.setValueAt(xySeries.getY(slice), row, columnIndex++);
                    }
                }
                row++;
            }
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setViewportView(table);
        setAutoscrolls(true);
    }

    //
    public JTable getTable() {
        return table;
    }
    
    public static class NonEditableModel extends DefaultTableModel {
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
