/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFamily;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFamily;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.FamilyDistribution;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionType;
import org.openmarkov.core.model.network.modelUncertainty.RangeFunction;
import org.openmarkov.core.model.network.modelUncertainty.Tools;
import org.openmarkov.core.model.network.modelUncertainty.TriangularFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TableWithEvents;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.exception.FamilyDistributionRuleBrokenException;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class UncertainValuesDialog extends OkCancelDialog {
    
    private static final int STATE_COLUMN_INDEX = 0;
    private static final int DISTRIBUTION_COLUMN_INDEX = 1;
    private static final int PARAMETERS_COLUMN_INDEX = 2;
    private static final int NAME_COLUMN_INDEX = 3;
    /** Name of the Complement distribution, as declared in its {@code @ProbDensFunctionType}. */
    private static final String COMPLEMENT_DISTRIBUTION_NAME =
            ComplementFunction.class.getAnnotation(ProbDensFunctionType.class).name();
    /** English help shown when hovering a Complement row, explaining its {@code nu} parameter. */
    private static final String NU_HELP_TOOLTIP =
            "<html><b>nu</b> &mdash; weight of this Complement entry.<br>"
            + "The probability mass left unused by the other entries in this column is shared among<br>"
            + "the Complement entries in proportion to their nu values, so the column still adds up to 1.<br>"
            + "If there is a single Complement entry, nu is not needed: it takes all the remaining mass.</html>";
    private static final long serialVersionUID = 1L;
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    // Components related to the distributions box
    private DistributionTableModel distributionTableModel;
    private JTable distributionTable;
    private final JPanel distributionsPanel;
    private Variable variable;
    private final List<String> distributionTypes;
    private boolean isChanceVariable;
    // List of uncertain values
    private List<UncertainValue> uncertainColumn;
    // List of doubles calculated from uncertainColum by taking the mean value
    private List<Double> valuesColumn;
    // Base position for storing the array of uncertain values in the table
    // potential
    private final int posBase;
    
    // 20/05/2025 Begin DES uncertainty
    private boolean isDES = false;
    
    /**
     * @param owner
     * @param configuration
     */
    public UncertainValuesDialog(Window owner, EvidenceCase configuration, TableWithEvents tableWithEvents) throws NonProjectablePotentialException {
        super(owner);
        isDES = true;
        distributionTypes = new ArrayList<>();
        posBase = getDESPositionBaseUncertainValue(tableWithEvents.getTablePotential(), configuration);
        
        setResizable(true);
        JPanel componentsPanel = getComponentsPanel();
        // Panel of distributions
        distributionsPanel = new JPanel();
        fillDistributionsTableModel(tableWithEvents.getTablePotential().getConditionedVariable(), configuration, tableWithEvents.getTablePotential());
        distributionTable.getModel().addTableModelListener(new DistributionsTableListener());
        distributionTable.addMouseListener(new DistributionsTableMouseListener());
        distributionsPanel.setBorder(new TitledBorder("Distributions"));
        JScrollPane distributionsTablePane = new JScrollPane(distributionTable);
        distributionsPanel.add(distributionsTablePane);
        distributionsTablePane.setPreferredSize(new Dimension(300, 100));
        distributionsPanel.setPreferredSize(new Dimension(350, 150));
        componentsPanel.add(distributionsPanel);
        try {
            initialize();
        } catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, stringDatabase.getString(e.getMessage()),
                                          stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
        }
        
        Point parentLocation = owner.getLocation();
        Dimension parentSize = owner.getSize();
        int x = (int) (parentLocation.getX() + parentSize.getWidth() / 2 - getSize().getWidth() / 2);
        int y = (int) (parentLocation.getY() + parentSize.getHeight() / 2 - getSize().getHeight() / 2);
        setLocation(new Point(x, y));
    }
    
    private int getDESPositionBaseUncertainValue(TablePotential potential, EvidenceCase configuration) {
        int[] coordinates;
        int sizeCoordinates;
        int pos;
        int sizeEvi = configuration.getFindings().size();
        //If there is only one state; then it is multiplied by 1
        sizeCoordinates = 1+ sizeEvi;
        coordinates = new int[sizeCoordinates];
        List<Variable> varsTable = potential.getVariables();
        for (int i = 1; i < sizeCoordinates; i++) {
            coordinates[i] = configuration.getFinding(varsTable.get(i)).getStateIndex();
        }
        pos = potential.getPosition(coordinates);
        return pos;
    }
    // End DES uncertainty
    
    
    /**
     * @param owner the owner
     * @param configuration the configuration
     * @param potential the potential
     */
    public UncertainValuesDialog(Window owner, EvidenceCase configuration, TablePotential potential) throws NonProjectablePotentialException {
        super(owner);
        //This constructor is never called by a utility node
        /*
        isChanceVariable = !(potential.isUtility());
        */
        isChanceVariable = true;
        //
        distributionTypes = new ArrayList<>();
        //Now the variable of the node is always at potential.getVariable(0)
        /*
        variable = isChanceVariable ? potential.getVariable(0) : potential.getUtilityVariable();
        */
        variable = potential.getVariable(0);
        //
        setTitle(getConfigurationDescription(variable, isChanceVariable, configuration));
        
        posBase = getPositionBaseUncertainValue(potential, configuration);
        setResizable(true);
        JPanel componentsPanel = getComponentsPanel();
        // Panel of distributions
        distributionsPanel = new JPanel();
        fillDistributionsTableModel(variable, configuration, potential);
        distributionTable.getModel().addTableModelListener(new DistributionsTableListener());
        distributionTable.addMouseListener(new DistributionsTableMouseListener());
        distributionsPanel.setBorder(new TitledBorder("Distributions"));
        JScrollPane distributionsTablePane = new JScrollPane(distributionTable);
        distributionsPanel.add(distributionsTablePane);
        distributionsTablePane.setPreferredSize(new Dimension(300, 100));
        distributionsPanel.setPreferredSize(new Dimension(350, 150));
        componentsPanel.add(distributionsPanel);
        initialize();
        Point parentLocation = owner.getLocation();
        Dimension parentSize = owner.getSize();
        int x = (int) (parentLocation.getX() + parentSize.getWidth() / 2 - getSize().getWidth() / 2);
        int y = (int) (parentLocation.getY() + parentSize.getHeight() / 2 - getSize().getHeight() / 2);
        setLocation(new Point(x, y));
    }
    
    /**
     * Creates and displays the UncertainValuesDialog for a ExactDistrPotential
     *
     * @param owner the owner
     * @param configuration the configuration
     * @param potential     - exactDistrPotential for which we will set uncertainty
     */
    public UncertainValuesDialog(Window owner, EvidenceCase configuration, ExactDistrPotential potential) throws NonProjectablePotentialException {
        super(owner);
        TablePotential tablePotential = potential.getTablePotential();
        //This constructor is always called in a utility node
        /*
        isChanceVariable = !(potential.isUtility());
        */
        isChanceVariable = false;
        //
        distributionTypes = new ArrayList<>();
        //Mow the variable of the node is always at potential.getVariable(0)
        /*
        variable = isChanceVariable ? potential.getVariable(0) : potential.getUtilityVariable();
        */
        variable = potential.getVariable(0);
        setTitle(getConfigurationDescription(variable, isChanceVariable, configuration));
        posBase = getPositionBaseUncertainValue(tablePotential, configuration);
        setResizable(true);
        JPanel componentsPanel = getComponentsPanel();
        // Panel of distributions
        distributionsPanel = new JPanel();
        fillDistributionsTableModel(variable, configuration, tablePotential);
        distributionTable.getModel().addTableModelListener(new DistributionsTableListener());
        distributionTable.addMouseListener(new DistributionsTableMouseListener());
        distributionsPanel.setBorder(new TitledBorder("Distributions"));
        JScrollPane distributionsTablePane = new JScrollPane(distributionTable);
        distributionsPanel.add(distributionsTablePane);
        distributionsTablePane.setPreferredSize(new Dimension(300, 100));
        distributionsPanel.setPreferredSize(new Dimension(350, 150));
        componentsPanel.add(distributionsPanel);
        initialize();
        Point parentLocation = owner.getLocation();
        Dimension parentSize = owner.getSize();
        int x = (int) (parentLocation.getX() + parentSize.getWidth() / 2 - getSize().getWidth() / 2);
        int y = (int) (parentLocation.getY() + parentSize.getHeight() / 2 - getSize().getHeight() / 2);
        setLocation(new Point(x, y));
    }
    
    public static boolean hasUncertainValues(UncertainValue[] auxUncertainTable) {
        boolean hasUncertainValues;
        if ((auxUncertainTable == null) || (auxUncertainTable.length == 0)) {
            hasUncertainValues = false;
        } else {
            hasUncertainValues = false;
            for (int i = 0; (i < auxUncertainTable.length) && !hasUncertainValues; i++) {
                hasUncertainValues = (auxUncertainTable[i] != null);
            }
        }
        return hasUncertainValues;
    }
    
    public static List<Double> calculateReferenceValues(List<UncertainValue> uncertainColumn) {
        List<Integer> complementIndexes = new ArrayList<Integer>();
        List<Integer> dirichletIndexes = new ArrayList<Integer>();
        List<Integer> otherIndexes = new ArrayList<Integer>();
        double[] refValues = new double[uncertainColumn.size()];
        ComplementFamily comp = new ComplementFamily(uncertainColumn);
        DirichletFamily dir = new DirichletFamily(uncertainColumn);
        for (int i = 0; i < uncertainColumn.size(); i++) {
            UncertainValue uncertainValue = uncertainColumn.get(i);
            if (uncertainValue.getProbDensFunction() instanceof ComplementFunction) {
                complementIndexes.add(i);
            } else if (uncertainValue.getProbDensFunction() instanceof DirichletFunction) {
                dirichletIndexes.add(i);
            } else {
                otherIndexes.add(i);
            }
        }
        List<UncertainValue> otherUncertain = getElementsFromIndexes(uncertainColumn, otherIndexes);
        // Process other
        FamilyDistribution other = new FamilyDistribution(otherUncertain);
        double[] meanOther = other.getMean();
        placeInArray(refValues, otherIndexes, meanOther);
        // Process Dirichlet
        double[] meanDir = dir.getMean();
        placeInArray(refValues, dirichletIndexes, meanDir);
        // Process complements
        double massForComp = 1.0 - (Tools.sum(meanOther) + Tools.sum(meanDir));
        comp.setProbMass(massForComp);
        double[] meanComp = comp.getMean();
        placeInArray(refValues, complementIndexes, meanComp);
        List<Double> ref = new ArrayList<Double>();
        for (int i = 0; i < refValues.length; i++) {
            ref.add(refValues[i]);
        }
        return ref;
    }
    
    private static void placeInArray(double[] refValue, List<Integer> indexes, double[] x) {
        for (int i = 0; i < indexes.size(); i++) {
            refValue[indexes.get(i)] = x[i];
        }
    }
    
    private static List<UncertainValue> getElementsFromIndexes(List<UncertainValue> column, List<Integer> index) {
        List<UncertainValue> list = new ArrayList<UncertainValue>();
        for (Integer aux : index) {
            list.add(column.get(aux));
        }
        return list;
    }
    
    private static List<UncertainValue> getUncertainValuesOfClasses(List<UncertainValue> uncertainValues,
                                                                    List<Class<? extends ProbDensFunction>> classes) {
        List<UncertainValue> filtered = new ArrayList<UncertainValue>();
        for (UncertainValue aux : uncertainValues) {
            boolean isInClasses = false;
            for (int i = 0; (i < classes.size()) && !isInClasses; i++) {
                isInClasses = classes.get(i).isAssignableFrom(aux.getProbDensFunction().getClass());
            }
            if (isInClasses) {
                filtered.add(aux);
            }
        }
        return filtered;
    }
    
    private static boolean thereAreExactValuesGreaterThanZero(List<UncertainValue> arrayUncertain) {
        boolean thereAre = false;
        for (int i = 0; (i < arrayUncertain.size()) && !thereAre; i++) {
            UncertainValue aux = arrayUncertain.get(i);
            ProbDensFunction probDensityFunction = aux.getProbDensFunction();
            thereAre = (probDensityFunction instanceof ExactFunction) && probDensityFunction.getMean() > 0;
        }
        return thereAre;
    }
    
    /**
     * @param uncertainValues the uncertain values
     * @param types the types
     *
     * @return The indexes of uncertain values
     */
    private static int[] getIndexesUncertainValuesOfClasses(List<UncertainValue> uncertainValues,
                                                            List<Class<? extends ProbDensFunction>> types) {
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < uncertainValues.size(); i++) {
            UncertainValue uncertainValue = uncertainValues.get(i);
            ProbDensFunction probDensFunction = uncertainValue.getProbDensFunction();
            boolean isInTypes = false;
            for (int j = 0; (j < types.size()) && !isInTypes; j++) {
                isInTypes = types.get(j).isAssignableFrom(probDensFunction.getClass());
            }
            if (isInTypes) {
                indexes.add(i);
            }
        }
        int numIndexesOfTypes = indexes.size();
        int[] intIndexes = new int[numIndexesOfTypes];
        for (int i = 0; i < numIndexesOfTypes; i++) {
            intIndexes[i] = indexes.get(i);
        }
        return intIndexes;
    }
    
    public static int[] getIndexesUncertainValuesOfClass(List<UncertainValue> uncertainValues,
                                                         Class<? extends ProbDensFunction> functionClass) {
        List<Class<? extends ProbDensFunction>> classes = new ArrayList<>();
        classes.add(functionClass);
        return getIndexesUncertainValuesOfClasses(uncertainValues, classes);
    }
    
    private static List<UncertainValue> getUncertainValuesOfClass(List<UncertainValue> arrayUncertain,
                                                                  Class<? extends ProbDensFunction> type) {
        List<Class<? extends ProbDensFunction>> types = new ArrayList<>();
        types.add(type);
        return getUncertainValuesOfClasses(arrayUncertain, types);
    }
    
    public ChosenOption requestUncertainValues() {
        setVisible(true);
        return this.getSelectedOption();
    }
    
    public List<Double> getValuesColumn() {
        return valuesColumn;
    }
    
    public List<UncertainValue> getUncertainColumn() {
        return uncertainColumn;
    }
    
    public int getPosBase() {
        return posBase;
    }
    
    private int getPositionBaseUncertainValue(TablePotential potential, EvidenceCase configuration) {
        int sizeEvi = configuration.getFindings().size();
        int sizeCoordinates = sizeEvi + (isChanceVariable ? 1 : 0);
        int[] coordinates = new int[sizeCoordinates];
        List<Variable> varsTable = potential.getVariables();
        int startLoop;
        if (isChanceVariable) {
            coordinates[0] = 0;
            startLoop = 1;
        } else {
            startLoop = 0;
        }
        for (int i = startLoop; i < sizeCoordinates; i++) {
            coordinates[i] = configuration.getFinding(varsTable.get(i)).getStateIndex();
        }
        int pos = potential.getPosition(coordinates);
        return pos;
    }
    
    private String getColumnString(String column) {
        return stringDatabase.getString("UncertainValuesDialog.DistributionsTable.Columns." + column);
    }
    
    private void fillDistributionsTableModel(Variable variable, EvidenceCase configuration, TablePotential potential) throws NonProjectablePotentialException {
        potential.getUncertainValues();
        TablePotential projectedPotential = potential.tableProject(configuration, null);
        UncertainValue[] projectedUncertainTable = projectedPotential.getUncertainValues();
        // Get the table of uncertain values
        UncertainValue[] uncertainTable = !hasUncertainValues(projectedUncertainTable) ?
                createExactUncertainValuesFromDouble(projectedPotential) :
                projectedPotential.getUncertainValues();
        // Fill the table for the dialog
        
        String[] englishColumnNames = new String[]{"State", "Distribution", "Parameters", "Name"};
        int numColumns = englishColumnNames.length;
        String[] columnNames = new String[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columnNames[i] = getColumnString(englishColumnNames[i]);
        }
        
        List<String> allowedDistributionTypes = ProbDensFunctionManager.getUniqueInstance()
                                                                       .getValidProbDensFunctions(isChanceVariable);
        State[] states = variable.getStates();
        int numStates = states.length;
        Object[][] initialData = new Object[numStates][columnNames.length];
        JComboBox<String> distributionTypesCombo = new JComboBox<String>();
        for (String allowedDistributionType : allowedDistributionTypes) {
            distributionTypesCombo.addItem(allowedDistributionType);
        }
        int lastPosStates = numStates - 1;
        for (int i = 0; i < numStates; i++) {
            UncertainValue uncertainValue = uncertainTable[i];
            ProbDensFunction probDensFunction = uncertainValue.getProbDensFunction();
            String distribution = probDensFunction.getClass().getAnnotation(ProbDensFunctionType.class).name();
            distributionTypes.add(distribution);
            int iPosInitialData = lastPosStates - i;
            Object[] initialDataIPosInitialData = initialData[iPosInitialData];
            initialDataIPosInitialData[STATE_COLUMN_INDEX] = states[i].getName();
            initialDataIPosInitialData[DISTRIBUTION_COLUMN_INDEX] = distribution;
            initialDataIPosInitialData[PARAMETERS_COLUMN_INDEX] = getString(probDensFunction.getParameters());
            initialDataIPosInitialData[NAME_COLUMN_INDEX] = uncertainValue.getName();
        }
        distributionTableModel = new DistributionTableModel(initialData, columnNames);
        distributionTable = new JTable(distributionTableModel) {
            private static final long serialVersionUID = 1L;
            @Override public String getToolTipText(MouseEvent event) {
                Point point = event.getPoint();
                int row = rowAtPoint(point);
                int col = columnAtPoint(point);
                if (row >= 0 && (col == DISTRIBUTION_COLUMN_INDEX || col == PARAMETERS_COLUMN_INDEX)) {
                    Object distribution = getModel().getValueAt(row, DISTRIBUTION_COLUMN_INDEX);
                    if (distribution != null && COMPLEMENT_DISTRIBUTION_NAME.equals(distribution.toString())) {
                        return NU_HELP_TOOLTIP;
                    }
                }
                return super.getToolTipText(event);
            }
        };
        // Model for the column "Distribution"
        TableColumnModel columnModel = distributionTable.getColumnModel();
        TableColumn column = columnModel.getColumn(DISTRIBUTION_COLUMN_INDEX);
        column.setCellEditor(new DefaultCellEditor(distributionTypesCombo));
        columnModel.getColumn(0).setCellEditor(null);
        // Register the table so the per-cell getToolTipText override (the nu help) is shown on hover.
        ToolTipManager.sharedInstance().registerComponent(distributionTable);
    }
    
    private static String getString(double[] parameters) {
        return Arrays.stream(parameters).mapToObj(String::valueOf).collect(Collectors.joining(" "));
    }
    
    /**
     * @param projectedPotential Table potential which has no uncertain values. Its values are
     *                           used for creating the uncertain values
     *
     * @return An array of uncertain values
     */
    private static UncertainValue[] createExactUncertainValuesFromDouble(TablePotential projectedPotential) {
        double[] tableProjected = projectedPotential.getValues();
        UncertainValue[] uncertainTable = new UncertainValue[tableProjected.length];
        for (int i = 0; i < tableProjected.length; i++) {
            uncertainTable[i] = new UncertainValue(tableProjected[i]);
        }
        return uncertainTable;
    }
    
    private static String getConfigurationDescription(Variable variable, boolean isChanceVariable,
                                                      EvidenceCase configuration) {
        return ((isChanceVariable) ? "P" : "U")
                + "(" + variable.getName() + " | "
                + configuration.getFindings().stream().map(finding ->
                                                                   finding.getVariable()
                                                                          .getName() + " = '" + finding.getState() + "', ")
                               .collect(Collectors.joining(", "))
                + ")";
    }
    
    /**
     * This method initializes this instance.
     */
    private void initialize() {
        setName("UncertainValuesDialog");
        configureButtonsPanel();
        setDefaultButton(getOKButton());
        quitIconsOfButtons();
        pack();
    }
    
    /**
     * This method carries out the actions when the user press the Ok button
     * before hide the dialog.
     *
     * @return true if the dialog box can be closed.
     */
    @Override
    protected boolean doOkClickBeforeHide() throws FamilyDistributionRuleBrokenException.Rule2Broken, FamilyDistributionRuleBrokenException.Rule3Broken, FamilyDistributionRuleBrokenException.Rule1Broken {
        TableCellEditor currentEditor = distributionTable.getCellEditor();
        if (currentEditor != null) {
            currentEditor.stopCellEditing();
        }
        List<UncertainValue> uncertainValues;
        try {
            uncertainValues = readDataFromTable();
            verifyLocalConstraintsUncertainty(uncertainValues);
        } catch (IllegalArgumentException ex) {
            // Covers NumberFormatException (missing/non-numeric parameter) and InvalidArgumentException
            // (e.g. a Complement nu <= 0). Show a message and keep the dialog open instead of letting
            // an uncaught exception reach the event-dispatch thread.
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid value for every distribution parameter.\n"
                    + "For the Complement distribution, nu must be a number greater than 0\n"
                    + "(it is only optional when there is a single Complement entry).",
                    "Invalid distribution parameter",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (isChanceVariable) {
            verifyGlobalConstraintUncertainty(uncertainValues);
        }
        uncertainColumn = reverse(uncertainValues);
        valuesColumn = calculateReferenceValues();
        return true;
    }
    
    private static List<UncertainValue> reverse(List<UncertainValue> list) {
        List<UncertainValue> rev = new ArrayList<UncertainValue>();
        for (int i = list.size() - 1; i >= 0; i--) {
            rev.add(list.get(i));
        }
        return rev;
    }
    
    private List<Double> calculateReferenceValues() {
        return calculateReferenceValues(uncertainColumn);
    }
    
    private void verifyLocalConstraintsUncertainty(List<UncertainValue> uncertainvalues) {
        // Verify individual constraints for each Uncertain Value
        for (int i = 0; i < uncertainvalues.size(); i++) {
            @ToCheck(reasonKind = ToCheck.ReasonKind.USER_EXPERIENCE,
                    reasonDescription = "The exception thrown here should tell which distribution and uncertain value failed")
            UncertainValue uncertainValue = uncertainvalues.get(i);
            /*
            String distributionName = uncertainValue.getProbDensFunction().getClass()
                                                    .getAnnotation(ProbDensFunctionType.class).name();
            */
            uncertainValue.verifyParametersDomain(isChanceVariable);
        }
    }
    
    private static boolean verifyGlobalConstraintUncertainty(List<UncertainValue> uncertainValues) throws FamilyDistributionRuleBrokenException.Rule3Broken, FamilyDistributionRuleBrokenException.Rule2Broken, FamilyDistributionRuleBrokenException.Rule1Broken {
        @ToCheck(reasonKind = ToCheck.ReasonKind.CODE_QUALITY,
                reasonDescription = "Rules verified are 1, 2 and 3, but the method doVerifyRule4 is never used")
        FamilyDistribution family = new FamilyDistribution(uncertainValues);
        return (doVerifyRule1(family) && doVerifyRule2(family) && doVerifyRule3(family));
    }
    
    /*
     * If one of the distributions is Exact, Range, or Triangular, then:
     * • all of the others must be either exact, or range, or triangular, or complement;
     * • at least one of the others must be Complement;
     * • the sum of the maxima of all the distributions (different from Complement)
     * cannot be greater than 1.
     */
    private static boolean doVerifyRule1(FamilyDistribution family) throws FamilyDistributionRuleBrokenException.Rule1Broken {
        List<UncertainValue> exactRangeOrUncertain;
        List<Class<? extends ProbDensFunction>> rangeOrTriangTypes = new ArrayList<>();
        rangeOrTriangTypes.add(RangeFunction.class);
        rangeOrTriangTypes.add(TriangularFunction.class);
        List<UncertainValue> uncertainFamily = family.getFamily();
        List<UncertainValue> exactUncertain = getUncertainValuesOfClass(uncertainFamily, ExactFunction.class);
        int totalSizeFamily = uncertainFamily.size();
        List<UncertainValue> rangeOrTriangUncertain = getUncertainValuesOfClasses(uncertainFamily, rangeOrTriangTypes);
        int sizeRangeOrTriang = rangeOrTriangUncertain.size();
        int sizeExact = exactUncertain.size();
        if (sizeRangeOrTriang > 0 || thereAreExactValuesGreaterThanZero(exactUncertain)) {
            int numComplement = getUncertainValuesOfClass(uncertainFamily, ComplementFunction.class).size();
            exactRangeOrUncertain = new ArrayList<UncertainValue>(rangeOrTriangUncertain);
            exactRangeOrUncertain.addAll(exactUncertain);
            boolean verify = ((numComplement > 0) && (sizeExact + sizeRangeOrTriang + numComplement == totalSizeFamily)) && (
                    Tools.sum(new FamilyDistribution(exactRangeOrUncertain).getMaximum()) <= 1.0
            );
            if (!verify) {
                throw new FamilyDistributionRuleBrokenException.Rule1Broken(family);
            }
        }
        return true;
    }
    
    /*
     * If one of the distributions is a Beta, then:
     * • all the others must be Exact, with v = 0, or Complement;
     * • at least one of the others must be Complement.
     */
    private static boolean doVerifyRule2(FamilyDistribution family) throws FamilyDistributionRuleBrokenException.Rule2Broken {
        List<UncertainValue> uncertainFamily = family.getFamily();
        int totalSizeFamily = uncertainFamily.size();
        List<UncertainValue> betaUncertain = getUncertainValuesOfClass(uncertainFamily, BetaFunction.class);
        switch (betaUncertain.size()) {
            case 0 -> {
            }
            case 1 -> {
                List<UncertainValue> exactUncertain = getUncertainValuesOfClass(uncertainFamily, ExactFunction.class);
                List<UncertainValue> compUncertain = getUncertainValuesOfClass(uncertainFamily, ComplementFunction.class);
                int numExact = exactUncertain.size();
                int numComp = compUncertain.size();
                boolean verify = (
                        (numExact + numComp + 1 == totalSizeFamily) && areAllZero(
                                new FamilyDistribution(exactUncertain).getMean()) && (numComp >= 1)
                );
                if (!verify) {
                    throw new FamilyDistributionRuleBrokenException.Rule2Broken(family);
                }
            }
            default -> {
                throw new FamilyDistributionRuleBrokenException.Rule2Broken(family);
            }
        }
        return true;
    }
    
    private static boolean doVerifyRule3(FamilyDistribution family) throws FamilyDistributionRuleBrokenException.Rule3Broken {
        List<UncertainValue> uncertainFamily = family.getFamily();
        int totalSizeFamily = uncertainFamily.size();
        List<UncertainValue> dirUncertain = getUncertainValuesOfClass(uncertainFamily, DirichletFunction.class);
        int numDirichlet = dirUncertain.size();
        switch (numDirichlet) {
            case 0 -> {
            }
            case 1 -> {
                List<UncertainValue> exactUncertain = getUncertainValuesOfClass(uncertainFamily, ExactFunction.class);
                int numExact = exactUncertain.size();
                boolean verify = (
                        (numExact + numDirichlet == totalSizeFamily) && areAllZero(
                                new FamilyDistribution(exactUncertain).getMean())
                );
                if (!verify) {
                    throw new FamilyDistributionRuleBrokenException.Rule3Broken(family);
                }
            }
            default -> {
                throw new FamilyDistributionRuleBrokenException.Rule3Broken(family);
            }
        }
        return true;
    }
    
    @SuppressWarnings("unused")
    private static void doVerifyRule4(FamilyDistribution family) throws FamilyDistributionRuleBrokenException.Rule4Broken {
        List<UncertainValue> uncertainFamily = family.getFamily();
        int totalSizeFamily = uncertainFamily.size();
        List<UncertainValue> compUncertain = getUncertainValuesOfClass(uncertainFamily, ComplementFunction.class);
        boolean verify = (totalSizeFamily != compUncertain.size());
        if (!verify) {
            throw new FamilyDistributionRuleBrokenException.Rule4Broken(family);
        }
    }
    
    private static boolean areAllZero(double[] x) {
        boolean allZero = true;
        for (int i = 0; (i < x.length) && allZero; i++) {
            allZero = x[i] == 0.0;
        }
        return allZero;
    }
    
    public boolean isChanceVariable() {
        return isChanceVariable;
    }
    
    private List<UncertainValue> readDataFromTable() {
        Vector<?> data = distributionTableModel.getDataVector();
        int numRows = data.size();
        // A single Complement entry absorbs all the leftover probability mass, so its nu is irrelevant;
        // count the Complement rows to know whether nu may be defaulted.
        long complementCount = 0;
        for (int i = 0; i < numRows; i++) {
            Vector<?> row = (Vector<?>) data.get(i);
            if (COMPLEMENT_DISTRIBUTION_NAME.equals(row.get(DISTRIBUTION_COLUMN_INDEX).toString())) {
                complementCount++;
            }
        }
        boolean singleComplement = complementCount == 1;
        List<UncertainValue> uncertainValues = new ArrayList<UncertainValue>();
        ProbDensFunctionManager distributionManager = ProbDensFunctionManager.getUniqueInstance();
        for (int i = 0; i < numRows; i++) {
            Vector<?> row = (Vector<?>) data.get(i);
            String distributionType = row.get(DISTRIBUTION_COLUMN_INDEX).toString();
            double[] parameterArray;
            if (COMPLEMENT_DISTRIBUTION_NAME.equals(distributionType) && singleComplement) {
                // Lone complement: nu is not needed (it takes all the remaining mass). Use 1 so the
                // normalisation in ComplementFamily is well-defined regardless of what the user typed.
                parameterArray = new double[] { 1.0 };
            } else {
                parameterArray = parseParameters(row.get(PARAMETERS_COLUMN_INDEX).toString());
            }
            String name = (String) row.get(NAME_COLUMN_INDEX);
            ProbDensFunction probDensFunction = distributionManager.newInstance(distributionType, parameterArray);
            UncertainValue uncertainValue = new UncertainValue(probDensFunction, name);
            uncertainValues.add(uncertainValue);
        }
        return uncertainValues;
    }

    /**
     * Parses the space-separated parameters of a distribution cell. Throws {@link NumberFormatException}
     * (a subclass of {@link IllegalArgumentException}, handled by {@link #doOkClickBeforeHide()}) when a
     * value is missing or not a number, so the user gets a message instead of an uncaught exception.
     */
    private static double[] parseParameters(String cell) {
        String trimmed = cell.trim();
        if (trimmed.isEmpty()) {
            throw new NumberFormatException("missing parameter value");
        }
        String[] parameters = trimmed.split(" ");
        double[] parameterArray = new double[parameters.length];
        for (int j = 0; j < parameters.length; ++j) {
            parameterArray[j] = Double.parseDouble(parameters[j].trim());
        }
        return parameterArray;
    }
    
    private void quitIconsOfButtons() {
        this.getOKButton().setIcon(null);
        this.getCancelButton().setIcon(null);
    }
    
    /**
     * Sets up the panel where the buttons of the buttons panel will be appear.
     */
    private void configureButtonsPanel() {
        addButtonToButtonsPanel(getOKButton());
        // addButtonToButtonsPanel(getJButtonRemove());
        addButtonToButtonsPanel(getCancelButton());
    }
    
    public class DistributionsTableListener implements TableModelListener {
        @Override public void tableChanged(TableModelEvent e) {
            if (e.getColumn() == DISTRIBUTION_COLUMN_INDEX) {
                int selectedRow = distributionTable.getSelectedRow();
                String distributionType = distributionTableModel.getValueAt(selectedRow, DISTRIBUTION_COLUMN_INDEX)
                                                                .toString();
                DistributionParameterDialog parameterDialog = new DistributionParameterDialog(getOwner(),
                                                                                              distributionType);
                if (!distributionTypes.get(selectedRow).equals(distributionType)) {
                    parameterDialog.setVisible(true);
                    if (parameterDialog.getSelectedOption() == ChosenOption.Ok) {
                        String parameterString = Arrays.stream(parameterDialog.getParameters())
                                                       .mapToObj(String::valueOf)
                                                       .map(v -> v + " ")
                                                       .collect(Collectors.joining());
                        distributionTableModel
                                .setValueAt(parameterString.toString(), selectedRow, PARAMETERS_COLUMN_INDEX);
                        distributionTypes.set(selectedRow, distributionType);
                    } else {
                        distributionTableModel
                                .setValueAt(distributionTypes.get(selectedRow), selectedRow, DISTRIBUTION_COLUMN_INDEX);
                    }
                }
            }
        }
    }
    
    public class DistributionsTableMouseListener extends MouseAdapter {
        
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && distributionTable.getSelectedColumn() == PARAMETERS_COLUMN_INDEX) {
                int selectedRow = distributionTable.getSelectedRow();
                String distributionType = distributionTableModel.getValueAt(selectedRow, DISTRIBUTION_COLUMN_INDEX)
                                                                .toString();
                String currentParameters = distributionTableModel.getValueAt(selectedRow, PARAMETERS_COLUMN_INDEX)
                                                                 .toString();
                double[] parameters = null;
                if (!currentParameters.isEmpty()) {
                    String[] parameterArray = currentParameters.split(" ");
                    parameters = new double[parameterArray.length];
                    for (int i = 0; i < parameters.length; ++i) {
                        parameters[i] = Double.parseDouble(parameterArray[i]);
                    }
                }
                DistributionParameterDialog parameterDialog = new DistributionParameterDialog(getOwner(),
                                                                                              distributionType, parameters);
                parameterDialog.setVisible(true);
                if (parameterDialog.getSelectedOption() == ChosenOption.Ok) {
                    String parameterString = Arrays.stream(parameterDialog.getParameters())
                                                   .mapToObj(String::valueOf)
                                                   .map(v -> v + " ")
                                                   .collect(Collectors.joining());
                    distributionTableModel.setValueAt(parameterString.toString(), selectedRow, PARAMETERS_COLUMN_INDEX);
                }
            }
        }
    }
    
    public static class DistributionTableModel extends DefaultTableModel {
        
        private static final long serialVersionUID = 1L;
        
        public DistributionTableModel(Object[][] initialData, String[] columnNames) {
            super(initialData, columnNames);
        }
        
        @Override public boolean isCellEditable(int row, int col) {
            return (col == DISTRIBUTION_COLUMN_INDEX) || (col == NAME_COLUMN_INDEX);
        }
    }
    
}
