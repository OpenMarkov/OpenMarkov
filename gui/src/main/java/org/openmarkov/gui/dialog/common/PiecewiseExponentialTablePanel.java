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

    protected JFileChooser chooser = new JFileChooser();
//    protected Pattern dataPattern = Pattern.compile(".*(\\d+)\\s*;\\s*(\\d+\\.?\\d*).*");
    protected Pattern dataPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*[;,]\\s*(\\d+\\.?\\d*)");
    protected JButton loadButton = null;

    public PiecewiseExponentialTablePanel(Object[][] data) {
        //KeyTablePanel always consider that the first column of Key Table is hidden.
        //The method can be overridden but KeyTable always considers the first column as not editable
        super(new String[]{"Hidden", "Time", "Probability"}, data, true, true, true);
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
     * This method initializes buttonPanel.
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
                                    .addComponent(getDownValueButton(), GroupLayout.Alignment.LEADING,
                                            GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                    .addComponent(getUpValueButton(), GroupLayout.Alignment.LEADING,
                                            GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                    .addComponent(getRemoveValueButton(), GroupLayout.Alignment.LEADING,
                                            GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)).addContainerGap()));
            groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                    groupLayout.createSequentialGroup()
                            .addComponent(getLoadButton()).addGap(5, 5, 5)
                            .addComponent(getAddValueButton()).addGap(5, 5, 5)
                            .addComponent(getRemoveValueButton()).addGap(5, 5, 5).addComponent(getUpValueButton())
                            .addGap(5, 5, 5).addComponent(getDownValueButton()).addGap(88, 88, 88)));
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
        tableModel.addRow(new Object[]{0, 0, 0});
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

        int index = valuesTable.getSelectedRow();
        int rowCount = valuesTable.getRowCount();
        if ((rowCount == 0) || (index == -1)) {
            removeValueButton.setEnabled(false);
            upValueButton.setEnabled(false);
            downValueButton.setEnabled(false);
        } else {
            removeValueButton.setEnabled(true);
            if (index == 0) {
                upValueButton.setEnabled(false);
                if (index == (rowCount - 1)) {
                    downValueButton.setEnabled(false);
                } else {
                    downValueButton.setEnabled(true);
                }
            } else if (index == (valuesTable.getRowCount() - 1)) {
                downValueButton.setEnabled(false);
                if (index == 0) {
                    upValueButton.setEnabled(false);
                } else {
                    upValueButton.setEnabled(true);
                }
                upValueButton.setEnabled(true);
            } else {
                upValueButton.setEnabled(true);
                downValueButton.setEnabled(true);
            }
        }
        if (rowCount <= 1) {
            removeValueButton.setEnabled(false);
        } else {
            removeValueButton.setEnabled(true);
        }
    }


}
