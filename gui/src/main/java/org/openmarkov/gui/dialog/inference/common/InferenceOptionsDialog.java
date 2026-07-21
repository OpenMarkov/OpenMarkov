/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.action.core.MonteCarloOptionsEdit;
import org.openmarkov.core.action.core.MulticriteriaEdit;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.gui.component.ValuesTableCellRenderer;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.java.reflectionUtils.ReflectionEquality;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Locale;

/**
 * Dialog for configuring inference options, including multi-criteria weights,
 * discount rates, temporal settings, and analysis scope.
 */
public class InferenceOptionsDialog extends OkCancelDialog {
    
    /**
     * Constant for Criteria column
     */
    public static final int CRITERION_COLUMN = 0;
    /**
     * Constant for Unicriterion scales column
     */
    public static final int UNICRITERIA_SCALE_COLUMN = 1;
    /**
     * Constant for Discounts column if unicriteria
     */
    public static final int UNICRITERIA_DISCOUNT_COLUMN = 2;
    /**
     * Constant for Discounts column if Unicriteria
     */
    public static final int UNICRITERIA_DISCOUNT_UNIT_COLUMN = 3;
    /**
     * Constant for Uses column if CE
     */
    public static final int CE_USE_COLUMN = 1;
    /**
     * Constant for Cost-Effectiveness scales column
     */
    public static final int CE_SCALE_COLUMN = 2;
    /**
     * Constant for Discounts column
     */
    public static final int CE_DISCOUNT_COLUMN = 3;
    /**
     * Constant for Discounts column if CE
     */
    public static final int CE_DISCOUNT_UNIT_COLUMN = 4;
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1L;
    /**
     * Reference to the localize object
     */
    final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * Scroll Pane for the table
     */
    private JScrollPane tableScrollPane;
    /**
     * Table
     */
    private JTable table;
    /**
     * Unicriterion radio button
     */
    private JRadioButton unicriterion;
    /**
     * Cost Effectiveness radio button
     */
    private JRadioButton costEffectiveness;
    /**
     * Temporal copy of the decisionCriteria
     */
    private java.util.List<Criterion> decisionCriteria;
    /**
     * Main panel of the layout
     */
    private final JPanel mainPanel;
    /**
     * Panel in which the user can select the main unit of the unicriterion conversion
     */
    private JPanel unitsPanel;
    /**
     * ProbNet in which we are working
     */
    private final ProbNet probNet;
    /**
     * Combobox with all the possible units of the decision criteria
     */
    private JComboBox<String> existingUnits;
    /**
     * Boolean attribute that indicates if the probnet is temporal or not
     */
    private final boolean isTemporal;
    /**
     * Boolean attribute that indicates if the probnet have multicriteria
     */
    private boolean isMulticriteria;
    // 25/07/11019
    /**
     * Boolean attribute that indicates if the simulation will be done using Monte Carlo Simulation
     */
    private boolean isMonteCarloSimulation;
    /**
     * Temporal copy of Multicriteria options
     */
    private MulticriteriaOptions multicriteriaOptions;
    /**
     * Temporal copy of Tempora options
     */
    private final TemporalOptions temporalOptions;
    /**
     * Number of slices label
     */
    private JLabel numSlicesLabel;
    /**
     * Number of slices JTextField
     */
    private JTextField numSlicesTextField;
    
    /**
     * Number of slices panel
     */
    private JPanel numSlicesPanel;
    
    /**
     * Beginning of cycle radio button
     */
    private JRadioButton beginningOfCycleButton;
    /**
     * End of cycle radio button
     */
    private JRadioButton endOfCycleButton;
    /**
     * Half cyle radio button
     */
    private JRadioButton halfCycleButton;
    /**
     * Buttongroup with transitions
     */
    private ButtonGroup transitionsButtonGroup;
    /**
     * Transitions panel
     */
    private JPanel transitionsPanel;
    
    /**
     * Number of simulations
     */
    private Integer numSimulations;
    /**
     * Panel with multicriteria options
     */
    private JPanel multicriteriaPanel;
    
    private JPanel temporalPanel;

    private boolean expandNetwork = false;
    //  16/12/2019 Extracted Monte Carlo Panel to an independent class
    /**
     * Panel with Monte Carlo Options
     */
    private MonteCarloOptionsPanel monteCarloOptionsPanel;
    
    /**
     * Constructor of the dialog
     *
     * @param probNet the prob net
     * @param owner the owner
     * @param onlyShowThisType The task must filter by multicriteria type. Null if not necessary
     */
    public InferenceOptionsDialog(ProbNet probNet, Window owner, MulticriteriaOptions.Type onlyShowThisType) {
        super(owner);
        
        this.probNet = probNet;
        
        this.setTitle(stringDatabase.getString("InferenceOptionsDialog.Title"));
        // If the net has more than atemporal variables, the net would be temporal
        isTemporal = !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);
        
        if (probNet.getDecisionCriteria() != null && probNet.getDecisionCriteria().size() > 1) {
            this.isMulticriteria = true;
        }
        
        // 25/08/2019
        //isMCSimulation may be used with another networks but currently is used only with DesNet
        isMonteCarloSimulation = false;
        if (probNet.getNetworkType() instanceof DESNetworkType) {
            isMonteCarloSimulation = true;
        }
        //
        
        // Center the dialog
        setLocationRelativeTo(owner);
        
        // Make a working copy of the criteria
        this.decisionCriteria = new ArrayList<>();
        
        for (Criterion criterion : probNet.getDecisionCriteria()) {
            this.decisionCriteria.add(criterion.clone());
        }
        
        // Make a working copy of the temporal and multicriteria options
        this.multicriteriaOptions = probNet.getInferenceOptions().getMultiCriteriaOptions().clone();
        this.temporalOptions = probNet.getInferenceOptions().getTemporalOptions().clone();
        
        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        // 25/08/2019 -- if we have a Monte Carlo simulation, the panel with the Monte Carlo options is added. Currently it is only for DESNets
        if (isMonteCarloSimulation) {
            mainPanel.add(getMonteCarloOptionsPanel());
        }
        
        if (isTemporal) {
            mainPanel.add(getTemporalPanel());
        }
        
        if (isMulticriteria) {
            mainPanel.add(getMulticriteriaPanel());
        }
        
        this.add(mainPanel);
        this.setIconImage(null);
        this.setResizable(false);
        
        this.pack();
        
        if (onlyShowThisType != null) {
            if (onlyShowThisType == MulticriteriaOptions.Type.UNICRITERION) {
                if (unicriterion != null) {
                    unicriterion.doClick();
                }
                
                if (costEffectiveness != null) {
                    costEffectiveness.setEnabled(false);
                }
                probNet.getInferenceOptions().getMultiCriteriaOptions().setUnicriterionOptionsShowed(true);
                multicriteriaOptions.setUnicriterionOptionsShowed(true);
            } else if (onlyShowThisType == MulticriteriaOptions.Type.COST_EFFECTIVENESS) {
                if (costEffectiveness != null) {
                    costEffectiveness.doClick();
                }
                
                if (unicriterion != null) {
                    unicriterion.setEnabled(false);
                }
                
                probNet.getInferenceOptions().getMultiCriteriaOptions().setCeOptionsShowed(true);
                multicriteriaOptions.setCeOptionsShowed(true);
            }
        }
        else {
            if (probNet.getInferenceOptions()
                       .getMultiCriteriaOptions()
                       .getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION) {
                probNet.getInferenceOptions().getMultiCriteriaOptions().setUnicriterionOptionsShowed(true);
                multicriteriaOptions.setUnicriterionOptionsShowed(true);
            } else if (probNet.getInferenceOptions()
                              .getMultiCriteriaOptions()
                              .getMulticriteriaType() == MulticriteriaOptions.Type.COST_EFFECTIVENESS) {
                probNet.getInferenceOptions().getMultiCriteriaOptions().setCeOptionsShowed(true);
                multicriteriaOptions.setCeOptionsShowed(true);
            }
        }

        // Center dialog
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);
        if (isTemporal || isMonteCarloSimulation || isMulticriteria)
            this.setVisible(true);

        
    }
    
    /**
     * Auxiliar constructor that don't force any type of multicriteria analysis (unicriterion or cost-effectiveness)
     **/
    public InferenceOptionsDialog(ProbNet probNet, Window owner) {
        super(owner);

        this.probNet = probNet;
        expandNetwork = true;

        setTitle(stringDatabase.getString("InferenceOptionsDialog.ExpandNetwork"));

        isTemporal = !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);


        setLocationRelativeTo(owner);

        this.temporalOptions = probNet.getInferenceOptions().getTemporalOptions().clone();

        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(getNumSlicesPanel());

        add(mainPanel);
        setIconImage(null);
        setResizable(false);

        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        setVisible(true);
    }
    
    /**
     * Get multicriteria options panel	 *
     *
     * @return the multi criteria panel
     */
    public JPanel getMulticriteriaPanel() {
        if (multicriteriaPanel == null) {
            multicriteriaPanel = new JPanel();
            multicriteriaPanel.setBorder(new TitledBorder(stringDatabase.getString("MulticriteriaDialog.Title")));
            multicriteriaPanel.setLayout(new BoxLayout(multicriteriaPanel, BoxLayout.PAGE_AXIS));
            
            multicriteriaPanel.add(getUnitsAndSelectPanels());
            
            tableScrollPane = getTablePanel();
            multicriteriaPanel.add(tableScrollPane);
            
            multicriteriaPanel.setVisible(true);
            
            if (probNet.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType() != null) {
                if (multicriteriaOptions.getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION) {
                    unicriterion.doClick();
                } else if (multicriteriaOptions.getMulticriteriaType() == MulticriteriaOptions.Type.COST_EFFECTIVENESS) {
                    costEffectiveness.doClick();
                }
            }
        }
        return multicriteriaPanel;
    }
    
    /**
     * Gets the Panel in which we have the conversion unit and the panel in which
     * we have the multi criteria type to be applied
     *
     * @return The units and select panel
     */
    private JPanel getUnitsAndSelectPanels() {
        JPanel mixedPanel = new JPanel();
        mixedPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel selectTypePanel = getSelectTypePanel();
        mixedPanel.add(selectTypePanel);
        
        unitsPanel = getUnitsPanel();
//		unitsPanel.setPreferredSize(selectTypePanel.getPreferredSize());
        mixedPanel.add(unitsPanel);
        
        return mixedPanel;
    }
    
    /**
     * Panel in which we have the Table with the criteria data
     *
     * @return The table panel
     */
    private JScrollPane getTablePanel() {
        
        JComboBox<String> comboBoxUse = null;
        JComboBox<String> comboBoxDiscountUnits = null;

        MultiCriteriaTableModel model = new MultiCriteriaTableModel();
        
        // Construction of the TableModel
        if (costEffectiveness.isSelected()) {
            if (isTemporal) {
                model.addColumn("criterion");
                model.addColumn("use");
                model.addColumn("ceScale");
                model.addColumn("discount");
                model.addColumn("discountUnit");
                model.addRow(new Object[]{stringDatabase.getString("MulticriteriaDialog.TableHeader.Criterion"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Use"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Scale"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Discount"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Unit")});
            } else {
                model.addColumn("criterion");
                model.addColumn("use");
                model.addColumn("ceScale");
                model.addRow(new Object[]{stringDatabase.getString("MulticriteriaDialog.TableHeader.Criterion"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Use"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Scale")});
            }
        } else { // Unicriteria is selected
            if (isTemporal) {
                model.addColumn("criterion");
                model.addColumn("scale");
                model.addColumn("discount");
                model.addColumn("discountUnit");
                model.addRow(new Object[]{stringDatabase.getString("MulticriteriaDialog.TableHeader.Criterion"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Scale"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Discount"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Unit")});
            } else {
                model.addColumn("criterion");
                model.addColumn("scale");
                model.addRow(new Object[]{stringDatabase.getString("MulticriteriaDialog.TableHeader.Criterion"),
                        stringDatabase.getString("MulticriteriaDialog.TableHeader.Scale")});
            }
            
        }
        
        // Fill the rows with the criteria
        if (costEffectiveness.isSelected()) {
            
            for (Criterion criterion : decisionCriteria) {
                
                // Prepare the Combobox for the CE_USE of the criterion
                //Creates a new ComboBox object for each row (for each criterion)
                comboBoxUse = new JComboBox<String>();
                //				comboBoxUse.addItem(Criterion.CECriterion.Null.toString());
                comboBoxUse.addItem(Criterion.CECriterion.Cost.toString());
                comboBoxUse.addItem(Criterion.CECriterion.Effectiveness.toString());
                
                // Set the selected item in the combobox with criterion data
                if (criterion.getCECriterion() == Criterion.CECriterion.Cost) {
                    comboBoxUse.setSelectedItem(Criterion.CECriterion.Cost.toString());
                } else {/*if(criterion.getCECriterion().equals(Criterion.CECriterion.Effectiveness)){*/
                    comboBoxUse.setSelectedItem(Criterion.CECriterion.Effectiveness.toString());
                } /*else {
					comboBoxUse.setSelectedItem(Criterion.CECriterion.Null.toString());
				} */
                
                if (isTemporal) {
                    // Prepare the ComboBox for the discount units and creates a new object for each criterion
                    comboBoxDiscountUnits = new JComboBox<String>();
                    for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
                        String newUnit = StringDatabase.getUniqueInstance()
                                                       .getString("NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit." + unit.toString());
                        comboBoxDiscountUnits.addItem(newUnit);
                    }
                    // Set the selected discount unit with criteron data
                    if (criterion.getDiscountUnit() == null) {
                        comboBoxDiscountUnits.setSelectedItem(StringDatabase.getUniqueInstance()
                                                                            .getString("NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit.YEAR"));
                        criterion.setDiscountUnit(CycleLength.DiscountUnit.YEAR);
                    } else {
                        for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
                            
                            if (criterion.getDiscountUnit() == unit) {
                                String newUnit = StringDatabase.getUniqueInstance().getString(
                                        "NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit." + unit.toString());
                                comboBoxDiscountUnits.setSelectedItem(newUnit);
                            }
                        }
                    }
                    
                    // Used BigDecimal to avoid loosed precisin
                    BigDecimal discount = new BigDecimal(String.valueOf(criterion.getDiscount()));
                    discount = discount.multiply(new BigDecimal(100));
                    
                    // Set the new row with criterion data
                    model.addRow(new Object[]{criterion.getCriterionName(), comboBoxUse, criterion.getCeScale(),
                            discount + " %", comboBoxDiscountUnits});
                } else {
                    // Set the new row with criterion data
                    model.addRow(new Object[]{criterion.getCriterionName(), comboBoxUse, criterion.getCeScale()});
                }
            }
        } else { // If unicriteria is selected
            for (Criterion criterion : decisionCriteria) {
                
                // Gets the string of the scale (with units)
                String scale = String.valueOf(criterion.getUnicriterizationScale());
                if (criterion.getCriterionUnit() != null && !criterion.getCriterionUnit()
                                                                      .equals(multicriteriaOptions.getMainUnit())) {
                    scale += " " + multicriteriaOptions.getMainUnit() + "/" + criterion.getCriterionUnit();
                }
                
                if (isTemporal) {
                    // Prepare the ComboBox for the discount units and creates a new object for each criterion
                    comboBoxDiscountUnits = new JComboBox<String>();
                    for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
                        String newUnit = StringDatabase.getUniqueInstance()
                                                       .getString("NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit." + unit.toString());
                        comboBoxDiscountUnits.addItem(newUnit);
                    }
                    
                    // Set the selected discount unit with criteron data
                    if (criterion.getDiscountUnit() == null) {
                        comboBoxDiscountUnits.setSelectedItem(StringDatabase.getUniqueInstance()
                                                                            .getString("NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit.YEAR"));
                        criterion.setDiscountUnit(CycleLength.DiscountUnit.YEAR);
                    } else {
                        for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
                            
                            if (criterion.getDiscountUnit() == unit) {
                                String newUnit = StringDatabase.getUniqueInstance().getString(
                                        "NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit." + unit.toString());
                                comboBoxDiscountUnits.setSelectedItem(newUnit);
                            }
                        }
                    }
                    
                    // Used BigDecimal to avoid loosed precisin
                    BigDecimal discount = new BigDecimal(String.valueOf(criterion.getDiscount()));
                    discount = discount.multiply(new BigDecimal(100));
                    
                    // Set the new row with criterion data
                    model.addRow(new Object[]{criterion.getCriterionName(), scale, discount + " %",
                            comboBoxDiscountUnits});
                } else {
                    // Set the new row with criterion data
                    model.addRow(new Object[]{criterion.getCriterionName(), scale});
                }
                
            }
        }

        // Creates table with the model
        table = new JTable(model) {
            /**
             * Serial UID
             */
            private static final long serialVersionUID = 1L;
            
            // Adjust the size of the table
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(
                        Math.max(rendererWidth + getIntercellSpacing().width + 20, tableColumn.getPreferredWidth()));
                return component;
            }
            
            // Gets only the numerical value and select all the foreground in editing mode
            @Override public boolean editCellAt(int row, int column, EventObject e) {
                boolean result = super.editCellAt(row, column, e);
                final Component editor = getEditorComponent();
                if (!(editor instanceof JTextComponent text) || (
                        isTemporal && costEffectiveness.isSelected() && column == CE_USE_COLUMN
                ) || (isTemporal && unicriterion.isSelected() && column == UNICRITERIA_DISCOUNT_UNIT_COLUMN)) {
                    return result;
                }
                
                if (e instanceof MouseEvent) {
                    EventQueue.invokeLater(() -> {
                        if (text.getText().indexOf(' ') != -1) {
                            text.setText(text.getText().substring(0, text.getText().indexOf(' ')));
                        }
                        text.selectAll();
                    });
                    
                } else {
                    if (text.getText().indexOf(' ') != -1) {
                        text.setText(text.getText().substring(0, text.getText().indexOf(' ')));
                    }
                    text.selectAll();
                }
                return result;
            }
            
            @Override public void setValueAt(Object aValue, int row, int column) {
                if ((
                        unicriterion.isSelected() && (
                                column == UNICRITERIA_SCALE_COLUMN || column == UNICRITERIA_DISCOUNT_COLUMN
                        )
                ) || (costEffectiveness.isSelected() && (column == CE_DISCOUNT_COLUMN || column == CE_SCALE_COLUMN))) {
                    if (aValue instanceof String) {
                        double value = Double.parseDouble((String) aValue);
                        super.setValueAt(value, row, column);
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
                
            }
            
            // If any cell is changed, save the new value in the temporal object
            @Override public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                int row = e.getFirstRow();
                int column = e.getColumn();
                
                if (costEffectiveness.isSelected()) {
                    // If the edited cell is a cost effectiveness use
                    if (column == CE_USE_COLUMN) {
                        String use = table.getValueAt(row, CE_USE_COLUMN).toString();
						/*
						if(use.equals(Criterion.CECriterion.Null.toString())){
							decisionCriteria.get(row - 1).setCECriterion(Criterion.CECriterion.Null);
						} else */
                        if (use.equals(Criterion.CECriterion.Cost.toString())) {
                            decisionCriteria.get(row - 1).setCECriterion(Criterion.CECriterion.Cost);
                        } else if (use.equals(Criterion.CECriterion.Effectiveness.toString())) {
                            decisionCriteria.get(row - 1).setCECriterion(Criterion.CECriterion.Effectiveness);
                        }
                        // If the edited cell is a cost effectiveness scale
                    } else if (column == CE_SCALE_COLUMN) {
                        String scale = table.getValueAt(row, CE_SCALE_COLUMN).toString();
                        if (scale.indexOf(' ') != -1) {
                            scale = scale.substring(0, scale.indexOf(' '));
                        }
                        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
                        format.applyLocalizedPattern("#.###");
                        scale = format.format(Double.parseDouble(scale));
                        decisionCriteria.get(row - 1).setCeScale(Double.parseDouble(scale));
                    }
                    
                    if (isTemporal) {
                        // If the edited cell is a discount
                        if (column == CE_DISCOUNT_COLUMN) {
                            String discount = table.getValueAt(row, CE_DISCOUNT_COLUMN).toString();
                            if (discount.indexOf(' ') != -1) {
                                discount = discount.substring(0, discount.indexOf(' '));
                            }
                            DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
                            format.applyLocalizedPattern("#.###");
                            discount = format.format(Double.parseDouble(discount));
                            double discountDouble = Double.parseDouble(discount);
                            discountDouble /= 100;
                            decisionCriteria.get(row - 1).setDiscount(discountDouble);
                            // If the edited cell is a discount unit
                        } else if (column == CE_DISCOUNT_UNIT_COLUMN) {
                            CycleLength.DiscountUnit unitSelected = CycleLength.DiscountUnit.YEAR;
                            for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
                                if (StringDatabase.getUniqueInstance().getString(
                                                          "NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit." + unit.toString())
                                                  .equals(table.getValueAt(row, CE_DISCOUNT_UNIT_COLUMN).toString())) {
                                    unitSelected = unit;
                                    break;
                                }
                            }
                            decisionCriteria.get(row - 1).setDiscountUnit(unitSelected);
                        }
                    }
                } else { //If unicriteria is selected
                    // If the edited cell is a unicriterion scale
                    if (column == UNICRITERIA_SCALE_COLUMN) {
                        String scale = table.getValueAt(row, UNICRITERIA_SCALE_COLUMN).toString();
                        if (scale.indexOf(' ') != -1) {
                            scale = scale.substring(0, scale.indexOf(' '));
                        }
                        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
                        format.applyLocalizedPattern("#.###");
                        scale = format.format(Double.parseDouble(scale));
                        decisionCriteria.get(row - 1).setUnicriterizationScale(Double.parseDouble(scale));
                    }
                    
                    if (isTemporal) {
                        // If the edited cell is a discount
                        if (column == UNICRITERIA_DISCOUNT_COLUMN) {
                            String discount = table.getValueAt(row, UNICRITERIA_DISCOUNT_COLUMN).toString();
                            if (discount.indexOf(' ') != -1) {
                                discount = discount.substring(0, discount.indexOf(' '));
                            }
                            DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
                            format.applyLocalizedPattern("#.###");
                            discount = format.format(Double.parseDouble(discount));
                            double discountDouble = Double.parseDouble(discount);
                            discountDouble /= 100;
                            decisionCriteria.get(row - 1).setDiscount(discountDouble);
                        } else if (column == UNICRITERIA_DISCOUNT_UNIT_COLUMN) {
                            // If the edited cell is a discount unit
                            CycleLength.DiscountUnit unitSelected = CycleLength.DiscountUnit.YEAR;
                            for (CycleLength.DiscountUnit unit : CycleLength.DiscountUnit.values()) {
                                if (StringDatabase.getUniqueInstance().getString(
                                                          "NetworkTemporalOptionsPanel.TemporalOptions.DiscountUnit." + unit.toString())
                                                  .equals(table.getValueAt(row, UNICRITERIA_DISCOUNT_UNIT_COLUMN)
                                                               .toString())) {
                                    unitSelected = unit;
                                    break;
                                }
                            }
                            decisionCriteria.get(row - 1).setDiscountUnit(unitSelected);
                        }
                    }
                }
            }
            
            // If the editing mode is stopped, refresh the table to show the units again
            @Override public void editingStopped(ChangeEvent e) {
                super.editingStopped(e);
                typeChanged();
            }
        };
        
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ValuesTableCellRenderer renderer = new ValuesTableCellRenderer(table, 1, null);
        
        // Set the model and renderer of columns
        if (costEffectiveness.isSelected()) {
            table.getColumnModel().getColumn(CE_USE_COLUMN)
                 .setCellRenderer(new MultiCriteriaComboBoxRenderer(table, MultiCriteriaComboBoxRenderer.USE_RENDERER));
            table.getColumnModel().getColumn(CE_USE_COLUMN).setCellEditor(new DefaultCellEditor(comboBoxUse));
            table.getColumnModel().getColumn(CE_SCALE_COLUMN).setCellRenderer(renderer);
            
            if (isTemporal) {
                table.getColumnModel().getColumn(CE_DISCOUNT_COLUMN).setCellRenderer(renderer);
                
                table.getColumnModel().getColumn(CE_DISCOUNT_UNIT_COLUMN).setCellRenderer(
                        new MultiCriteriaComboBoxRenderer(table, MultiCriteriaComboBoxRenderer.DISCOUNT_UNIT_RENDERER));
                table.getColumnModel().getColumn(CE_DISCOUNT_UNIT_COLUMN)
                     .setCellEditor(new DefaultCellEditor(comboBoxDiscountUnits));
            }
            
        } else {
            table.getColumnModel().getColumn(UNICRITERIA_SCALE_COLUMN).setCellRenderer(renderer);
            
            if (isTemporal) {
                table.getColumnModel().getColumn(UNICRITERIA_DISCOUNT_COLUMN).setCellRenderer(renderer);
                
                table.getColumnModel().getColumn(UNICRITERIA_DISCOUNT_UNIT_COLUMN).setCellRenderer(
                        new MultiCriteriaComboBoxRenderer(table, MultiCriteriaComboBoxRenderer.DISCOUNT_UNIT_RENDERER));
                table.getColumnModel().getColumn(UNICRITERIA_DISCOUNT_UNIT_COLUMN)
                     .setCellEditor(new DefaultCellEditor(comboBoxDiscountUnits));
            }
        }
        
        table.getColumnModel().getColumn(CRITERION_COLUMN).setCellRenderer(renderer);
        
        // Put the table in a scroll pane
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(table);
        tableScrollPane = new JScrollPane(panel);
        tableScrollPane.setPreferredSize(new Dimension(500, 100));
        tableScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        return tableScrollPane;
    }
    
    /**
     * Gets the Panel with the units
     *
     * @return The units panel
     */
    private JPanel getUnitsPanel() {
        JPanel unitsPanel = new JPanel();
        
        unitsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        unitsPanel.setBorder(new TitledBorder(stringDatabase.getString("MulticriteriaDialog.Unit.Title")));

//		JLabel unitsLabel = new JLabel(stringDatabase.getString("MulticriteriaDialog.Unit.Title"));
//		unitsPanel.add(unitsLabel, BorderLayout.LINE_START);
//		unitsLabel.setSize(new Dimension(50, 50));
        
        existingUnits = new JComboBox<String>();
        
        HashMap<String, String> criteriaUnits = new HashMap<String, String>();
        for (Criterion criterion : decisionCriteria) {
            criteriaUnits.put(criterion.getCriterionUnit(), criterion.getCriterionUnit());
        }
        
        for (String unitKey : criteriaUnits.keySet()) {
            existingUnits.addItem(criteriaUnits.get(unitKey));
        }
        
        if (criteriaUnits.get(multicriteriaOptions.getMainUnit()) != null) {
            existingUnits.setSelectedItem(multicriteriaOptions.getMainUnit());
        } else {
            existingUnits.setSelectedIndex(0);
            if (existingUnits.getSelectedItem() != null) {
                multicriteriaOptions.setMainUnit(existingUnits.getSelectedItem().toString());
            }
        }
        
        existingUnits.addActionListener(new ActionListener() {
            
            @Override public void actionPerformed(ActionEvent e) {
                changeMainUnit();
                
            }
        });
        unitsPanel.add(existingUnits);
        
        return unitsPanel;
    }
    
    /**
     * Set a new main conversion unit and refresh the table
     */
    private void changeMainUnit() {
        if (existingUnits.getSelectedItem() != null) {
            multicriteriaOptions.setMainUnit(existingUnits.getSelectedItem().toString());
        }
        typeChanged();
    }
    
    /**
     * Gets the panel with the selection of the multicriteria type
     *
     * @return The select type panel
     */
    private JPanel getSelectTypePanel() {
        JPanel selectTypePanel = new JPanel();
        selectTypePanel.setBorder(new TitledBorder(stringDatabase.getString("MulticriteriaDialog.Type.Title")));
        selectTypePanel.setLayout(new GridLayout(0, 1));
        
        ButtonGroup group = new ButtonGroup();
        
        unicriterion = new JRadioButton();
        unicriterion.setText(stringDatabase.getString("MulticriteriaDialog.Type.Unicriterion"));
        unicriterion.setSelected(true);
        unicriterion.addActionListener(new ActionListener() {
            
            @Override public void actionPerformed(ActionEvent e) {
                typeChanged();
            }
        });
        group.add(unicriterion);
        
        costEffectiveness = new JRadioButton();
        costEffectiveness.setText(stringDatabase.getString("MulticriteriaDialog.Type.CostEffectiveness"));
        group.add(costEffectiveness);
        costEffectiveness.addActionListener(new ActionListener() {
            
            @Override public void actionPerformed(ActionEvent e) {
                typeChanged();
            }
        });
        
        selectTypePanel.add(unicriterion);
        selectTypePanel.add(costEffectiveness);
        
        return selectTypePanel;
    }
    
    /**
     * If the type of multicriteria analysis is changed, save the new type and refresh the table
     */
    protected void typeChanged() {
        if (costEffectiveness.isSelected()) {
            multicriteriaOptions.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
            multicriteriaPanel.remove(tableScrollPane);
            tableScrollPane = getTablePanel();
            multicriteriaPanel.add(tableScrollPane);
            unitsPanel.setVisible(false);
            tableScrollPane.setVisible(true);
            multicriteriaPanel.setVisible(true);
            
        } else if (unicriterion.isSelected()) {
            multicriteriaOptions.setMulticriteriaType(MulticriteriaOptions.Type.UNICRITERION);
            multicriteriaPanel.remove(tableScrollPane);
            tableScrollPane = getTablePanel();
            multicriteriaPanel.add(tableScrollPane);
            multicriteriaPanel.setVisible(true);
            unitsPanel.setVisible(true);
            tableScrollPane.setVisible(true);
            multicriteriaPanel.repaint();
        }
        this.pack();
        this.repaint();
    }
    
    /**
     * Get temporal options panel
     *
     * @return The temporal panel
     */
    public JPanel getTemporalPanel() {
        if (temporalPanel == null) {
            temporalPanel = new JPanel();
            temporalPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            temporalPanel.setBorder(
                    new TitledBorder(stringDatabase.getString("NetworkTemporalOptionsPanel.TemporalOptions.Title")));
            temporalPanel.add(getNumSlicesPanel());
            temporalPanel.add(getTransitionsPanel());
        }
        return temporalPanel;
    }
    
    // -16/12/2019 extracted Monte Carlo Options to an independent panel
    /**
     * Returns a panel with the Monte Carlo Options
     *
     * @return
     */
    public JPanel getMonteCarloOptionsPanel() {
        if (monteCarloOptionsPanel == null) {
            monteCarloOptionsPanel= new MonteCarloOptionsPanel(probNet);
        }
        return monteCarloOptionsPanel;
    }
    
    private JPanel getNumSlicesPanel() {
        if (numSlicesPanel == null) {
            numSlicesPanel = new JPanel();
            
            JPanel slicesPanel = new JPanel();
            slicesPanel.setLayout(new GridLayout(1, 2, 10, 10));
            slicesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            slicesPanel.add(getJLabelNumSlices());
            slicesPanel.add(getNumSlicesTextField());
            slicesPanel.add(new JLabel(stringDatabase.getString("CostEffectiveness.Cycles")));
            
            numSlicesPanel.setLayout(new BoxLayout(numSlicesPanel, BoxLayout.LINE_AXIS));
            numSlicesPanel.add(slicesPanel);
            
        }
        return numSlicesPanel;
    }
    
    private JLabel getJLabelNumSlices() {
        if (numSlicesLabel == null) {
            numSlicesLabel = new JLabel(stringDatabase.getString("CostEffectiveness.NumberOfCycles"));
        }
        return numSlicesLabel;
    }
    
    private JTextField getNumSlicesTextField() {
        if (numSlicesTextField == null) {
            numSlicesTextField = new JTextField();
            numSlicesTextField.setText("" + this.temporalOptions.getHorizon());
            numSlicesTextField.setColumns(10);
            numSlicesTextField.setName("numSlicesTextField");
        }
        return numSlicesTextField;
    }
    
    private JRadioButton getBeginningOfCycleButton() {
        if (beginningOfCycleButton == null) {
            beginningOfCycleButton = new JRadioButton(stringDatabase.getString("CostEffectiveness.BeginningOfCycle"),
                                                      true);
        }
        return beginningOfCycleButton;
    }
    
    private JRadioButton getEndOfCycleButton() {
        if (endOfCycleButton == null) {
            endOfCycleButton = new JRadioButton(stringDatabase.getString("CostEffectiveness.EndOfCycle"), true);
        }
        return endOfCycleButton;
    }
    
    private JRadioButton getHalfCycleButton() {
        if (halfCycleButton == null) {
            halfCycleButton = new JRadioButton(stringDatabase.getString("CostEffectiveness.HalfCycle"), true);
        }
        return halfCycleButton;
    }
    
    private void initTransitionsButtonGroup() {
        transitionsButtonGroup = new ButtonGroup();
        transitionsButtonGroup.add(getBeginningOfCycleButton());
        transitionsButtonGroup.add(getHalfCycleButton());
        transitionsButtonGroup.add(getEndOfCycleButton());
        
        if (this.temporalOptions.getTransition() == TemporalOptions.TransitionTime.BEGINNING) {
            beginningOfCycleButton.setSelected(true);
        } else if (this.temporalOptions.getTransition() == TemporalOptions.TransitionTime.HALF) {
            halfCycleButton.setSelected(true);
        } else if (this.temporalOptions.getTransition() == TemporalOptions.TransitionTime.END) {
            endOfCycleButton.setSelected(true);
        }
    }
    
    /**
     * @return the panel with the transition buttons
     */
    private JPanel getTransitionsPanel() {
        if (transitionsPanel == null) {
            transitionsPanel = new JPanel();
            transitionsPanel.setLayout(new GridLayout(3, 1));
            transitionsPanel.setBorder(new TitledBorder("Transitions"));
            transitionsPanel.setName("transitionsPanel");
            initTransitionsButtonGroup();
            transitionsPanel.add(getBeginningOfCycleButton());
            transitionsPanel.add(getHalfCycleButton());
            transitionsPanel.add(getEndOfCycleButton());
            if (isMonteCarloSimulation){
                getBeginningOfCycleButton().setEnabled(false);
                getHalfCycleButton().setEnabled(false);
                getEndOfCycleButton().setEnabled(false);
                transitionsPanel.setEnabled(false);
                transitionsPanel.setVisible(false);
            }
        }
        return transitionsPanel;
    }
    
    public MulticriteriaOptions getMulticriteriaOptions() {
        return multicriteriaOptions;
    }
    
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    @Override protected boolean doOkClickBeforeHide() throws Exception {
        super.doOkClickBeforeHide();
        // If the is user is editing a cell, stop the edition to save the data
        if (table != null && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
        probNet.getPNESupport().openNewSubEditHistory();
        if (isMulticriteria) {
            boolean decisionCriteriaIsTheSame = ReflectionEquality.areEquals(probNet.getDecisionCriteria(), decisionCriteria);
            boolean multiCriteriaOptionsIsTheSame = ReflectionEquality.areEquals(probNet.getInferenceOptions().getMultiCriteriaOptions(), multicriteriaOptions);
            
            if(!decisionCriteriaIsTheSame || !multiCriteriaOptionsIsTheSame) {
                MulticriteriaEdit editMulticriteria = new MulticriteriaEdit(probNet, decisionCriteria, multicriteriaOptions);
                editMulticriteria.executeEdit();
            }
        }
        if (isTemporal) {
            int numSlices;
            try {
                numSlices = Integer.parseInt(numSlicesTextField.getText());
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException("Slices number", numSlicesTextField.getText(), "is not a valid number, as it must be a number between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE);
            }
            this.temporalOptions.setHorizon(numSlices);
            if(!expandNetwork && !isMonteCarloSimulation) {
                if (beginningOfCycleButton.isSelected()) {
                    this.temporalOptions.setTransition(TemporalOptions.TransitionTime.BEGINNING);
                } else if (halfCycleButton.isSelected()) {
                    this.temporalOptions.setTransition(TemporalOptions.TransitionTime.HALF);
                } else if (endOfCycleButton.isSelected()) {
                    this.temporalOptions.setTransition(TemporalOptions.TransitionTime.END);
                }
            }
            probNet.getInferenceOptions().setTemporalOptions(temporalOptions);

        }
        
        if (isMonteCarloSimulation) {
            new MonteCarloOptionsEdit(probNet, monteCarloOptionsPanel.getMonteCarloOptions()).executeEdit();
        }
        
        probNet.getPNESupport().closeSubEditHistory();
        
        return super.doOkClickBeforeHide();
    }
    
    public ChosenOption requestData() {
        return getSelectedOption();
    }
}
