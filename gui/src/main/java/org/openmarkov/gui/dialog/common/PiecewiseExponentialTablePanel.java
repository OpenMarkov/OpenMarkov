package org.openmarkov.gui.dialog.common;

import org.openmarkov.gui.loader.element.IconBind;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PiecewiseExponentialTablePanel extends KeyTablePanel {

    /** Column of the time where each stretch starts; the first column is hidden. */
    private static final int TIME_COLUMN = 1;

    protected JFileChooser chooser = new JFileChooser();
//    protected Pattern dataPattern = Pattern.compile(".*(\\d+)\\s*;\\s*(\\d+\\.?\\d*).*");
    protected Pattern dataPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*[;,]\\s*(\\d+\\.?\\d*)");
    protected JButton loadButton = null;

    public PiecewiseExponentialTablePanel(Object[][] data) {
        //KeyTablePanel always consider that the first column of Key Table is hidden.
        //The method can be overridden but KeyTable always considers the first column as not editable
        super(new String[]{"Hidden", "Time", "Probability"}, data, true, true, true, true);
        Object[][] newData = new Object[data.length][3];
//        for (int i = 0;  i< data.length ; i++) {
//            Object[] dataRow= data[i];
//            Arrays.parallelSetAll(newData[i], j->j==0?0:dataRow[j-1]);
//        }
        initialize();
        this.setBorder(new TitledBorder("Life Table"));
        setEnabledRemoveValue(true);

        chooser.setDialogTitle("Choose a CSV data file");
        chooser.setApproveButtonText("Load data");
    }

    protected JButton getLoadButton() {
        if (loadButton == null) {
            loadButton = new JButton();
            loadButton.setName("KeyTablePanel.loadButton");
            loadButton.setText("Load data file");
//			loadButton.setMnemonic(stringDatabase.getString("Up.Text.Mnemonic").charAt(0));
            loadButton.setIcon(IconBind.OPEN_ENABLED.icon());
            loadButton.setVisible(reorderable);
            loadButton.setEnabled(true);
            loadButton.addActionListener(this);
        }
        return loadButton;
    }

    /**
     * This method initializes buttonPanel. There are no buttons to move a row up or down: the stretches are saved
     * in the order of their times, whatever the order of the rows.
     *
     * @return a new button panel.
     */
    @Override
    protected JPanel getButtonPanel() {

        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setName("KeyTablePanel.buttonPanel");
            final GroupLayout groupLayout = new GroupLayout((JComponent) buttonPanel);
            groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
                    groupLayout.createSequentialGroup().addGroup(
                            groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(getLoadButton(), GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                    .addComponent(getAddValueButton(), GroupLayout.Alignment.LEADING,
                                            GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                    .addComponent(getRemoveValueButton(), GroupLayout.Alignment.LEADING,
                                            GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)).addContainerGap()));
            groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                    groupLayout.createSequentialGroup()
                            .addComponent(getLoadButton()).addGap(5, 5, 5)
                            .addComponent(getAddValueButton()).addGap(5, 5, 5)
                            .addComponent(getRemoveValueButton()).addGap(88, 88, 88)));
            buttonPanel.setLayout(groupLayout);
        }
        return buttonPanel;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event information.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(loadButton)) {
            actionPerformedLoadValues();
        } else super.actionPerformed(e);
    }

    protected void actionPerformedLoadValues() {
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path dataFile = chooser.getSelectedFile().toPath();
                List<Double[]> data = new ArrayList<>();
                for (String line : Files.readAllLines(dataFile)) {
                    final Matcher matcher = dataPattern.matcher(line);
                    if (matcher.matches()) {
                        try {
                            data.add(new Double[]{
                                    0.0,
                                    Double.valueOf(matcher.group(1)),
                                    Double.valueOf(matcher.group(2))
                            });
                        } catch (NumberFormatException ignored) {
                            // Line matches dataPattern, but has an invalid number.
                            // This should not happen, but here we silently ignore that, just in case.
                        }
                    }
                }
                if (!data.isEmpty()) {
                    // Any better way to replace all rows?
                    while (tableModel.getRowCount() > 0) {
                        tableModel.removeRow(0);
                    }
                    for (Object[] row : data)
                        tableModel.addRow(row);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Could not load data: " + e.getMessage(), "Error loading data", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Invoked when the button 'add' is pressed.
     */
    @Override
    protected void actionPerformedAddValue(ActionEvent e) {
        // A time no other row has, so that the new row does not replace a stretch when saving
        double nextTime = times(-1).stream().mapToDouble(Double::doubleValue).map(time -> time + 1).max().orElse(0);
        tableModel.addRow(new Object[]{0, nextTime, 0.0});
    }

    /**
     * The table, which refuses a time that another row already has: the cell keeps its value and the user is told.
     */
    @Override
    public KeyTable getValuesTable() {
        if (valuesTable == null) {
            valuesTable = new KeyTable(getTableModel(), true, true, true) {
                @Override
                public void setValueAt(Object newValue, int row, int column, Object source) {
                    if (column == TIME_COLUMN && times(row).contains(number(newValue))) {
                        warnRepeatedTime();
                        return;
                    }
                    super.setValueAt(newValue, row, column, source);
                }
            };
            valuesTable.setName("KeyTablePanel.valuesTable");
            valuesTable.onTables(omjTable -> omjTable.setListSelectionListener(this));
        }
        return valuesTable;
    }

    /**
     * @param skippedRow a row whose time is left out, or -1
     *
     * @return the times of the rows that are numbers; the others are left out
     */
    private List<Double> times(int skippedRow) {
        List<Double> times = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Double time = number(tableModel.getValueAt(row, TIME_COLUMN));
            if (row != skippedRow && time != null) {
                times.add(time);
            }
        }
        return times;
    }

    /** The number in a cell, or null if it is not a number. */
    private static Double number(Object value) {
        try {
            return value == null ? null : Double.valueOf(value.toString().strip());
        } catch (NumberFormatException notANumber) {
            return null;
        }
    }

    /**
     * Tells the user that the time was refused because another row already has it. In a method of its own so tests
     * can silence the dialog.
     */
    protected void warnRepeatedTime() {
        JOptionPane.showMessageDialog(this, stringDatabase.getString("RepeatedLifeTableTime.Text"),
                                      stringDatabase.getString("RepeatedLifeTableTime.Title"),
                                      JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Invoked when the button 'remove' is pressed.
     */
    protected void actionPerformedRemoveValue(ActionEvent e) {
        int selectedRowIndex = valuesTable.getSelectedRow();
        if (selectedRowIndex > -1) tableModel.removeRow(selectedRowIndex);
    }

    /**
     * Invoked when the row selection changes.
     *
     * @param e selection event information.
     */
    public void valueChanged(ListSelectionEvent e) {
        removeValueButton.setEnabled(valuesTable.getRowCount() > 1);
    }

}
