package org.openmarkov.gui.dialog.common;

import org.apache.logging.log4j.Logger;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TableWithEvents;
import org.openmarkov.core.model.network.potential.TableWithFunctions;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;
import org.openmarkov.gui.component.TableWithEventsCellRenderer;
import org.openmarkov.gui.component.TableWithEventsModel;
import org.openmarkov.gui.component.ValuesTableWithEvents;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.menu.TableWithEventsContextualMenu;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * This class implements a Table eventTablePotential table.
 * Transition class to be merged with the new structure of tables
 * @author cmyago
 * @version 1.0 - cmyago - 24/03/2019
 * @version 1.1 - cmyago - 20/08/2022 impossible configuration commented
 * @version 2 - cmyago - 29/08/2023 impossible configuration commented; doble click listener commented
 */

public class TableWithEventsPanel
		extends ProbabilityTablePanel {
	protected Logger logger;
	/**
	 * JTable where show the values.
	 */
	protected ValuesTableWithEvents valuesTableWithEvents = null;
	/**
	 * Indicates if the data of the table is modifiable.
	 */
	protected boolean modifiable=true;

	/**
	 * Selected row
	 * SelectedColum is in ProbabilityTablePanel
	 */
	protected int selectedRow = -1;


	/**
	 * Panel to scroll the table.
	 */
	protected JScrollPane valuesTableScrollPane = null;
	protected Node node;

	/**
	 * First eventTablePotential of node;  its class  should be  org.openmarkov.core.model.network.eventTablePotential.EventTablePotential or
	 *
	 */
	protected TableWithEvents tableWithEvents = null;



	/**
	 * TablePotential of EventTablePotential
	 */

	protected TablePotential tablePotential = null;


	/**
	 * TableWithFuntions of TableWithEvents
	 */

	protected TableWithFunctions tableWithFunctions = null;


	/**
	 * Numeric variables for tableWithFunctions
	 */
	protected List<Variable> functionParameters = null;


	/**
	 * True if functions are allowed
	 */
	private  boolean hasFunctions = true;

	/**
	 * Variables of the TablePotential of EventTablePotential
	 */
	protected List<Variable> tableVariables = null;




	/**
	 * Pseudo-util class with common operations used in eventTablePotential tables
	 */
	protected PotentialsTablePanelOperations tablePotentialsPanelOperations;

	/**
	 * ContextualMenu to assign/remove uncertainty.
	 * <p>
	 * This method creates the evidenceCase object when the user do right click on the table.
	 */

	protected TableWithEventsContextualMenu tableWithEventsContextualMenu;


//    /**
//     *
//     */
//    protected boolean[] impossibleColumns;



	public TableWithEventsPanel() {
		super();
	}



	public TableWithEventsPanel(Node node, TableWithEvents tableWithEvents, boolean hasFunctions ) {
		this(node, tableWithEvents,null);
		this.hasFunctions = hasFunctions;

	}

	public TableWithEventsPanel(Node node, TableWithEvents tableWithEvents, List<Variable> functionParameters) {
		super();

		this.node = node;
		this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();

		this.tableWithEvents = tableWithEvents;
		this.functionParameters = functionParameters;
		initialize();

	}



	public void initialize() {
        tablePotential = tableWithEvents.getTablePotential();
		tableWithFunctions = tableWithEvents.getTableWithFunctions();


        // The list of variables of eventTablePotential
        variables = tableWithEvents.getVariables();

        // The list of variables of the tablePotential of EventTablePotential
        tableVariables = tableWithEvents.getTableVariables();


		valuesTableWithEvents = new ValuesTableWithEvents(node,tableWithEvents,  new TableWithEventsModel(data, columns, firstEditableRow), modifiable);
        valuesTableWithEvents.setName("EventPotentialsTablePanel.valuesTableWithEvents");
        valuesTableWithEvents.setVisible(true);



        setTableSpecificListeners();

        setData();

        setLayout(new BorderLayout());

        // If the ScrollPane is not created, initialise it and set the Viewport.
        // Then add the element to the Layout.
        add(getValuesTableScrollPane(), BorderLayout.CENTER);

        repaint();

    }



	/**
	 * Sets a new table model with new data and new columns in valuesTableWithEvents
	 *
	 * @param newData    new data for the table
	 * @param newColumns new columns for the table
	 * @author carmenyago
	 * revised--> minor changes
	 * Previously named setData; I find this name confusing because coincides with setData()
	 */
	public void setDataInValuesTable(Object[][] newData, String[] newColumns) {

		// Table data
		data = newData.clone();
		// Table columns
		columns = newColumns.clone();

		// resets the tableWithEventsModel
		valuesTableWithEvents.resetModel();

		// Sets the valuesTableWithEvents tableWithEventsModel with columns, data
		valuesTableWithEvents.setModel(new TableWithEventsModel(data, columns, firstEditableRow));

		// Initialises a false an array which tells which data are modified
		valuesTableWithEvents.initializeDataModified(false);

		valuesTableWithEvents.setLastEditableRow(lastEditableRow);

		//show/hide rows based on the showingAllParameters attribute using a RowFilter mechanism.
		valuesTableWithEvents.setShowingAllParameters(true);

		valuesTableWithEvents.setNodeType(node.getNodeType());
	}

	/**
	 * It is necessary to implement setData(Node node)
	 * Here I deal with eventTablePotential = null or eventTablePotential =0;
	 * <p>
	 * UNCLEAR--> Called in PotentialEditDialog.showFields(Node)
	 *
	 * @author cmyago
	 */
	public void setData(Node node) {
		this.node = node;
//		try {
//			tablePotentialsPanelOperations.checkIfNoPotential(node.getPotentials());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
//					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
//			return;
//		}
		setData();
	}

	/**
	 * Sets a new table model with new data and new columns based on three
	 * items: <li>list of Potentials of the variable</li> <li>states of the
	 * variable</li> <li>parents of the variable</li>
	 * This method obtains if the node has link restrictions and store it in hasLinkrestriction,
	 * stores the probNet in ValuesTableWithEvents
	 * fills the tableData (tableData consists of headers + data),
	 * sets the columns name in a Excel mode (A,B,C,...AA,AB...),
	 * sets the tableWithEventsModel in valuesTableWithEvents( tableData + column names),
	 * sets uncertaintyInColumns with the columns with uncertainty,
	 * sets the cell renders according to the type of node, and
	 * in the tableModel, sets the not editable cells due to links restrictions and uncertainty in columns.
	 * Finally, this method adjust the size of the cells in valuesTableWithEvents
	 *
	 *
	 */
	// Using node sets in variable node
	// What to do with the exception
	public void setData() {

		// Sets the probNet in the table

		valuesTableWithEvents.setData(node);

		Object[][] tableData = null;
		String[] newColumns = null;

		// tableData contains the table to be displayed in ValuesTableWithEvents
		tableData = convertListPotentialsToTableFormat();

		// Sets the column names in Excel style: A, B, C,....AA,AB...
		// These column names aren't displayed
		newColumns = ValuesTableWithEvents.getColumnsIdsSpreadSheetStyle(tableData[0].length);


		//Sets the table model in valuesTableWithEvents
		setDataInValuesTable(tableData, newColumns);
//        impossibleColumns= getImpossibleColumns();

        // set the Cell Renders according to NodeType (a different renderer for some DECISON nodes) and the uncertainty
//		setCellRenderers(impossibleColumns);
		setCellRenderers();


		// Establish the column width
		valuesTableWithEvents.fitColumnsWidthToContent();
	}

//	/**
//	 * Sets the columns that have uncertainty a true in a boolean array
//	 * To do that, this method extracts the uncertainty for every column configuration (parents state set)
//	 * <p>
//	 * UNCLEAR-->When we reach this method eventTablePotential!=null
//	 *
//	 * @return Boolean array that represents the columns (true = the column has
//	 * an uncertainty, false = the column has not an uncertainty). This array only contains the data columns
//	 * @author carmenyago
//	 */
//	protected boolean[] getImpossibleColumns() {
//
//		int size = valuesTableWithEvents.getColumnCount();
//
//		// Column 0 contains the name of the states
//		boolean[] newImpossibleColumns = new boolean[size - 1];
//
//		for (int i = 1; i < size; i++) {
//			boolean isImpossible = false;
//			try {
//				// Returns an evidence case with one finding for every parent variable and its state in the column
//				Configuration configuration = getConfiguration(i);
//				// If the column configuration has uncertainty hasUncertainty= true
////				isImpossible = tableWithEvents.isImpossibleConfiguration(configuration);
//			} catch (InvalidStateException | IncompatibleEvidenceException e) {
//				e.printStackTrace();
//				JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
//						stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
//			}
//			// Indicates whether this column has uncertainty or not
//			newImpossibleColumns[i - 1] = isImpossible;
//		}
//		return newImpossibleColumns;
//	}



	/**
	 * Creates an array[number_of_rows][number_of_columns] with the objects displayed in the cells of valueTable
	 * Considers the eventTablePotential is not null
	 *
	 * @return the table data to be set
	 * @author carmenyago
	 */
	protected Object[][] convertListPotentialsToTableFormat() {
		Object[][] values = null;

		// Empty array values[number_of_rows][number_of_colums]
		values = createEmptyTable();

		// Sets the number of the parent variables
		values = setParentsNameInUpperLeftCornerArea(values);

		// Set the states of the parents on  the top of the table
		// UNCLEAR--> what happens when the parent variable is continuous????
		values = setParentsStatesInTopArea(values);
		// Set the states of the node variable on the left column
		values = setNodeStatesInLeftArea(values);

		// Set the TablePotential/TableDeltaPotential Data on values
		if (tableWithFunctions ==null) {
			values = setPotentialDataInCentreArea(values);
		} else{
			values = setPotentialFunctionDataInCentreArea(values);
		}

		// The variable position stores the number o data cells
		setNumberOfPostions();
		return values;
	}

	/**
	 * Creates and empty array of empty objects with the [number_of_rows][number_of_columns] of the valuesTableWithEvents
	 * Considers the eventTablePotential is not null
	 * UNCLEAR --> setBaseIndexForCoordinates
	 *
	 * @author carmenyago
	 * <p>
	 * Continuous variables have only one state
	 * tableSize is always >0
	 */
	protected Object[][] createEmptyTable() {

		// If there is no eventTablePotential
		int numRows = 0;
		int numColumns = 1; // Variables column

		// First editable row coincides with the number of parents
		firstEditableRow = PotentialsTablePanelOperations.calculateFirstEditableRow(tablePotential);

		// The baseIndexForCoordinates is the first editable row-->What for-->UNCLEAR
		// The property baseIndexForCoordinates is not Visible. baseIndexForCoordinates= row
		setBaseIndexForCoordinates(firstEditableRow);


		// Number of data elements of tablePotential
		int tableSize = tablePotential
				.getTableSize();//-->UNCLEAR What happens when there is no parent (f.e. when Tree/ADD )

		// Number of states of the variable of the node; if isTableDeltaPotential numDimensions=1
		int numDimensions = 1;
		numDimensions = tablePotential.getDimensions()[0];

		// Parent variables + states of node variable
		numRows = firstEditableRow + numDimensions;
		lastEditableRow = numRows - 1;

		/*if (!isTableDeltaPotential) numRows++;*/ //--> UNCLEAR Last row with the name of the variable and the state with '1' is REMOVED
		numColumns = numColumns + tableSize / numDimensions;

		// create the array of arrays
		return new Object[numRows][numColumns];
	}

	/**
	 * This methods fills the Upper Left corner of the table with the name of
	 * the parents of the node
	 *
	 * @param oldValues - the table that is being modified
	 * @author carmenyago
	 */
	protected Object[][] setParentsNameInUpperLeftCornerArea(Object[][] oldValues) {
		Object[][] values = oldValues;
		// Adding the parent
		// The first variable is always the node variable
		for (int i = 1; i < tableVariables.size(); i++) {
			values[i - 1][0] = tableVariables.get(i);
		}
		return values;
	}

	/**
	 * Sets the states of the parents in the top of the table
	 * Potential is not null
	 *
	 * @param oldValues - the table that is being modified. oldValues !=null and oldValues.lenght is always > 0
	 * @author carmenyago
	 */
	protected Object[][] setParentsStatesInTopArea(Object[][] oldValues) {
		Object[][] values = oldValues;

		int numColumns = values[0].length;

		// Initialise the variable with the number of data columns
		int numRepetitions = numColumns - 1;
		int numParentVariables = tableVariables.size() - 1;
		State[] states;

		for (int row = 0; row < numParentVariables; row++) {

			states = tableVariables.get(row + 1).getStates();
			numRepetitions = numRepetitions / states.length;

			for (int column = 1; column < numColumns; column++) {
				// Find the index of the state. We start in zero position of the
				// array of states, and thus we need to substract a unit to
				// column
				// The ratio divides the table in sections and the module
				// get the position relative to the section.
				int stateIndex = ((column - 1) / numRepetitions) % states.length;
				State state = states[stateIndex];
				values[row][column] = state.getName();
			}

		}
		return values;
	}

	/**
	 * this method sets the first row with the values of the states of the node
	 * (if it is a node chance) or the name of the variable of the node (if it
	 * is a utility node)
	 *
	 * @param oldValues - the table that is being modified
	 */
	protected Object[][] setNodeStatesInLeftArea(Object[][] oldValues) {
		Object[][] values = oldValues;
        Variable leftVariable=tableVariables.get(0);
		int length = lastEditableRow;
		if (leftVariable.getVariableType()==VariableType.EVENT){
			values[length][0] =leftVariable.getName() ;

		} else {

			for (State state : leftVariable.getStates()) {
				values[length--][0] = state.getName();
			}
		}
		return values;
	}

	/**
	 * Sets the data table from eventTablePotential in oldValues
	 *
	 * @param oldValues
	 * @return an array filled with the date table from tablePotential or tableDeltaPotential filled with the data values
	 * from tablePotential or tableDeltaPotential in the correct positions to be displayed by ValuesTableWithEvents
	 */
	protected Object[][] setPotentialDataInCentreArea(Object[][] oldValues) {
		Object[][] values = oldValues;

		int numColumns = values[0].length;

		// rounding initial values
		double[] initialValues = tablePotential.getValues();
		double[] roundedValues = new double[initialValues.length];
		int maxDecimals = 10;
		double epsilon;
		epsilon = Math.pow(10, -(maxDecimals + 2));
		for (int i = 0; i < initialValues.length; i++) {
			roundedValues[i] = Util.roundAndReduce(initialValues[i], epsilon, maxDecimals);
		}
		// UNCLEAR-->What for??
		//tablePotential.setValues(roundedValues);

		for (int j = 1; j <= numColumns - 1; j++) {

			// put the values on the table
			for (int i = getLastEditableRow(); i >= getFirstEditableRow(); i--) {
				int potentialIndex = PotentialsTablePanelOperations.getPotentialIndex(i, j, tablePotential);
				double value = roundedValues[potentialIndex];
				values[i][j] = value;
			}
		}
		return values;
	}


	/**
	 * Sets the data table from eventTablePotential when there is TableWithFunctions
	 *
	 */
	protected Object[][] setPotentialFunctionDataInCentreArea(Object[][] oldValues) {
		Object[][] values = oldValues;

		int numColumns = values[0].length;


		for (int j = 1; j <= numColumns - 1; j++) {

			// put the values on the table
			for (int i = getLastEditableRow(); i >= getFirstEditableRow(); i--) {
				int potentialIndex = PotentialsTablePanelOperations.getPotentialIndex(i, j, tablePotential);
				values[i][j] = tableWithFunctions.getFunctionValues()[potentialIndex];
			}
		}
		return values;
	}





	/**
	 * This method calculates the number of data cells and stores it in the attribute positions.
	 * The number of data cell is the product of the number of states of all variables
	 *
	 * @author carmenyago
	 * minor changes
	 */
	protected int setNumberOfPostions() {
		int numPositions = 1;
		try {
			for (Variable variable : tableVariables) {
				numPositions = numPositions * variable.getNumStates();
			}
		} catch (NullPointerException exception) {
			numPositions = 0;
			logger.error("not enough memory");
		}
		setPosition(numPositions);
		return numPositions;
	}

	/**
	 * Calculates the position on valuesTableWithEvents for a state combination
	 *
	 * @param stateIndices - indexes of the states
	 * @return an array containing the row at the first position and the column
	 * at the second position.
	 * revised--> only changed the code between , 
	 */
	protected int[] getRowAndColumnForStateCombination(int[] stateIndices, TablePotential potential) {
		int numStates = node.getVariable().getNumStates();
		int position = potential.getPosition(stateIndices);
		int tempMultiplier = tablePotential.getTableSize() / numStates;
		int tempColumnPosition = 0;

		// We start at index 1 because the state of the node is irrelevant for
		// obtain the column (only is relevant for the row)
		// We multiply the number of columns above each state and the index of
		// this variable (in wich state is)
		for (int i = 1; i < potential.getVariables().size(); i++) {
			Variable var = potential.getVariables().get(i);
			tempMultiplier = tempMultiplier / var.getNumStates();
			tempColumnPosition += stateIndices[i] * tempMultiplier;
		}

		// The column will be the column in the data structure plus one row at
		// the beginning
		int column = tempColumnPosition + 1;
		// The row will be the last row in the table minus the relative position
		// in the node state
		int row = getLastEditableRow() - (position % numStates);
		return new int[] { row, column };
	}


	/**
	 * This method generates the evidenceCase based on the column selected on
	 * the <code>valuesTableWithEvents</code> object.
	 * The evidence case has a finding for every parent of the node and its state in column
	 * <p>
	 * UNCLEAR When is the parents list reordered???
	 *
	 * @param col The column selected. Never is 0 , because the column 0 is the
	 *            states column
	 * @return An evidence case object
	 * @throws IncompatibleEvidenceException
	 */
	protected Configuration getConfiguration(int col) throws IncompatibleEvidenceException {
	//I don't know what to do
		List<Variable> parents = tableVariables.subList(1, tablePotential.getNumVariables());

		Configuration resultConfiguration = new Configuration();

		int[] parentsConfiguration = new int[parents.size()];

		/*
		 * If there is no eventTablePotential, an exception is shown (caught) and startPosition=0
		 */
		int startPosition = PotentialsTablePanelOperations.getPotentialStartIndexOfColumn(col, tablePotential);

		// gets the configuration of startPosition--> the data position in tablePotential corresponding to
		// the beginning of the column
		// I suppose configuration=[Node Variable, parent_1,----,parent_n]
		int[] numericConfiguration = tablePotential.getConfiguration(startPosition);

		// Extracts the configuration of the parents from configuration
		// It is the same for every cell of the selected column

		for (int i = numericConfiguration.length - 1; i > 0; i--) {
			parentsConfiguration[i - 1] = numericConfiguration[i];
		}

		// Gets the resultConfiguration
		int j = 0;
		// Adds to resultConfiguration a finding containing the parent and its configuration
		Finding finding;
		for (Variable var : parents) {
			finding = new Finding(var, parentsConfiguration[j]);
			resultConfiguration.addFinding(finding);
			j++;
		}
		return resultConfiguration;
	}

	/**
	 * This method gets the Evidence Case from the selected column
	 *
	 * @return Evidence case
	 */
	public Configuration getConfigurationFromSelectedColumn() throws IncompatibleEvidenceException {
        return getConfiguration(selectedColumn);
	}


	/**
	 * This method initialises valuesTableWithEvents and defines that first two columns cannot be selected
	 *
	 * @return a new values table.
	 * revised-->not changed
	 */
	public ValuesTableWithEvents getEventValuesTable() {

		return valuesTableWithEvents;
	}

	/**
	 * This method initialises valuesTableScrollPane.
	 *
	 * @return a new values table scroll pane.
	 * revised-->not changed
	 */
	protected JScrollPane getValuesTableScrollPane() {
		if (valuesTableScrollPane == null) {
			valuesTableScrollPane = new JScrollPane();
			valuesTableScrollPane.setName("TablePotentialPanel.valuesTableScrollPane");
			valuesTableScrollPane.setViewportView(getEventValuesTable());
		}
		return valuesTableScrollPane;
	}

	/**
	 * special method to show/hide the values table
	 * revised-->not changed
	 */
	public void showValuesTable(final boolean visible) {
		getEventValuesTable().setVisible(visible);
	}

	/**
	 * This method returns the tableWithEventsModel of valuesTableWithEvents. If valuesTableWithEvents has not a tableWithEventsModel, this method creates one.
	 *
	 * @return the tableWithEventsModel of valuesTableWithEvents.
	 * @see ValuesTableWithEvents
	 * revised-->minor changes
	 */
	protected TableWithEventsModel getTableModel() {
		TableWithEventsModel tableModel = null;
		if ((valuesTableWithEvents == null) || (valuesTableWithEvents.getEventValuesTableModel() == null))
			tableModel = new TableWithEventsModel(data, columns, firstEditableRow);
		else
			tableModel = (TableWithEventsModel) valuesTableWithEvents.getModel();

		return tableModel;
	}

	/**
	 * Show/Hide all the parameters
	 *
	 * @param showAllParameters the showAllParameters to set
	 */
	public void setShowAllParameters(boolean showAllParameters) {
		this.showAllParameters = showAllParameters;
		valuesTableWithEvents.setShowingAllParameters(showAllParameters);
	}

//	/**
//	 * Handles an action performed
//	 */
//	public void actionPerformed(ActionEvent e) {
//		String actionCommand = e.getActionCommand();
//		if (actionCommand.equals(ActionCommands.SET_IMPOSSIBLE_CONFIGURATION) ) {
//            try {
//                setImpossibleColumn();
//            } catch (WrongCriterionException ex) {
//                ex.printStackTrace();
//            }
//
//        } else if (actionCommand.equals(ActionCommands.UNSET_IMPOSSIBLE_CONFIGURATION)) {
//            try {
//                unSetImpossibleColumn();
//            } catch (WrongCriterionException ex) {
//                 ex.printStackTrace();
//            }
//         } else if (actionCommand.equals(ActionCommands.ADD_FUNCTION)){
//			try{
//				String functionString = valuesTableWithEvents.getValueAt(selectedRow,selectedColumn).toString();
//
//				ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null,functionParameters , functionString);
//				expressionDialog.setVisible(true);
//				if (expressionDialog.getSelectedButton() == OkCancelHorizontalDialog.OK_BUTTON) {
//					functionString = expressionDialog.getExpression();
//					valuesTableWithEvents.setValueAt(functionString,selectedRow,selectedColumn);
//
//				}
//
//
//			}catch(Exception ex){
//				ex.printStackTrace();
//			}
//		}
//
//	}

	/**
	 * Handles an action performed
	 */
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		//29/08/2023 There will be more action commands (PSA)
		if (actionCommand.equals(ActionCommands.ADD_FUNCTION)){
			try{
				String functionString = valuesTableWithEvents.getValueAt(selectedRow,selectedColumn,e.getSource()).toString();

				ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null,functionParameters , functionString);
				expressionDialog.setVisible(true);
				if (expressionDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
					functionString = expressionDialog.getExpression();
					valuesTableWithEvents.setValueAt(functionString,selectedRow,selectedColumn, e.getSource());

				}


			}catch(Exception ex){
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}

	}


//	protected void  setImpossibleColumn() throws WrongCriterionException {
//		// Generates the evidenceCase based on the column
//		// selected on the JTable object
//
//        valuesTableWithEvents.getImpossibleConfigurations().add( getConfigurationFromSelectedColumn());
//        impossibleColumns[selectedColumn -1] = true;
//
//        if (selectedColumn > 0) {
//					(
//							(TableWithEventsCellRenderer) getEventValuesTable().getDefaultRenderer(Double.class)
//					).setMark(selectedColumn - 1);
//					getEventValuesTable().repaint();
//				}
//
//
//	}


//    protected void  unSetImpossibleColumn() throws WrongCriterionException {
//        // Generates the evidenceCase based on the column
//        // selected on the JTable object
//
//        Configuration impossibleConfiguration= new Configuration(getConfigurationFromSelectedColumn());
//        valuesTableWithEvents.getImpossibleConfigurations().remove(impossibleConfiguration);
//        impossibleColumns[selectedColumn -1] = false;
//
//        if (selectedColumn > 0) {
//            (
//                    (TableWithEventsCellRenderer) getEventValuesTable().getDefaultRenderer(Double.class)
//            ).setUnMark(selectedColumn - 1);
//            getEventValuesTable().repaint();
//        }
//
//
//    }


//	/**
//	 * This method initialises uncertaintyContextualMenu.
//	 *
//	 * @return the node contextual menu.
//	 * revised-->not changed
//	 */
//	protected TableWithEventsContextualMenu getTableWithEventsContextualMenu(int row, int column) {
//// TODO Check if it is necessary to maintain the object created
////		if (tableWithEventsContextualMenu == null) {
////			tableWithEventsContextualMenu = new TableWithEventsContextualMenu(this);
////			tableWithEventsContextualMenu.setName("impossibleConfigurationContextualMenu");
////		}
////		return tableWithEventsContextualMenu;
//
//		if (valuesTableWithEvents.isCellEditable(row,column)){
//			tableWithEventsContextualMenu = new TableWithEventsContextualMenu(this);
//			tableWithEventsContextualMenu.setName("impossibleConfigurationContextualMenu");
//
//		} else {
//			tableWithEventsContextualMenu = new TableWithEventsContextualMenu(this, false);
//			tableWithEventsContextualMenu.setName("impossibleConfigurationContextualMenu");
//		}
//
////		boolean isImpossible = tableWithEvents.isImpossibleConfiguration(getConfigurationFromSelectedColumn());
////		if (isImpossible) {
////				tableWithEventsContextualMenu.getJComponentActionCommand(ActionCommands.SET_IMPOSSIBLE_CONFIGURATION.toString())
////						.setEnabled(false);
////				tableWithEventsContextualMenu.getJComponentActionCommand(ActionCommands.UNSET_IMPOSSIBLE_CONFIGURATION.toString())
////						.setEnabled(true);
////
////		} else {
//		tableWithEventsContextualMenu.getJComponentActionCommand(ActionCommands.SET_IMPOSSIBLE_CONFIGURATION.toString())
//				.setEnabled(true);
//		tableWithEventsContextualMenu.getJComponentActionCommand(ActionCommands.UNSET_IMPOSSIBLE_CONFIGURATION.toString())
//				.setEnabled(false);
////		}
//
//		return tableWithEventsContextualMenu;
//	}


	/**
	 * This method initialises uncertaintyContextualMenu.
	 *
	 * @return the node contextual menu.
	 * revised-->not changed
	 */
	protected TableWithEventsContextualMenu getTableWithEventsContextualMenu(int row, int column, MouseEvent e) {
		if (valuesTableWithEvents.isCellEditable(row,column, e.getSource())){
			tableWithEventsContextualMenu = new TableWithEventsContextualMenu(this);
		}
		return tableWithEventsContextualMenu;
	}





//	/**
//	 * Currently only with Chance and event nodes and no link restrictions
//	 * If extended to Decision and Utility nodes then look at TablePotentialPanel
//	 *
//	 * @param impossibleColumns
//	 */
//	protected void setCellRenderers(boolean[] impossibleColumns) {
//
//		TableCellRenderer cellRenderer = null;
//
//		// Creates the TableCellRenderer distinguishing if the node has or not link restrictions
//		cellRenderer = new TableWithEventsCellRenderer(firstEditableRow, impossibleColumns);
//		cellRenderer = new TableWithEventsCellRenderer(firstEditableRow);
//		valuesTableWithEvents.setDefaultRenderer(Double.class, cellRenderer);
//		valuesTableWithEvents.setDefaultRenderer(String.class, cellRenderer);
//	}

	/**
	 * Currently only with Chance and event nodes and no link restrictions
	 * If extended to Decision and Utility nodes then look at TablePotentialPanel
	 *
	 */
	protected void setCellRenderers() {

		TableCellRenderer cellRenderer = null;
		// Creates the TableCellRenderer distinguishing if the node has or not link restrictions
		cellRenderer = new TableWithEventsCellRenderer(firstEditableRow);
		TableCellRenderer finalCellRenderer = cellRenderer;
		valuesTableWithEvents.onTables(omjTable -> {
			omjTable.setDefaultRenderer(Double.class, finalCellRenderer);
			omjTable.setDefaultRenderer(String.class, finalCellRenderer);
		});
	}



	/**
	 * Method to define the specific listeners in this table (not defined in the
	 * common KeyTable hierarchy. This method creates the evidenceCase object
	 * when the user do right click on the table.
	 */
	protected void setTableSpecificListeners() {
		valuesTableWithEvents.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = valuesTableWithEvents.rowAtPoint(e.getPoint(), e.getSource());
				int column = valuesTableWithEvents.columnAtPoint(e.getPoint(), e.getSource());
				selectedColumn = column;
				selectedRow =row;
				if (SwingUtilities.isLeftMouseButton(e)) {
					valuesTableWithEvents
							.editCellAt(row, column,
									e);
				}
				if (SwingUtilities.isRightMouseButton(e)) {

                    int selectedColumn = valuesTableWithEvents.columnAtPoint(e.getPoint(), e.getSource());
					if ((row > -1) && (column > 0) && !isReadOnly()) {
//						if (getTableWithEventsContextualMenu() != null) {
//							updateContextualMenuOptions();
//							getTableWithEventsContextualMenu().show(valuesTableWithEvents, e.getX(), e.getY());
//
//                        }
						if (hasFunctions)
							getTableWithEventsContextualMenu(row, column, e).show(valuesTableWithEvents, e.getX(), e.getY());

					}
				}
			}

		});
//		valuesTableWithEvents.addMouseListener(new DoubleClickListener());
	}





	/**
	 * Close the table
	 * revised-->not changed
	 */
	@Override public void close() {
		getEventValuesTable().close();
	}

//	/**
//	 * This method sets the attributes this.readOnly= readOnly and modifiable = !readOnly to indicate
//	 * if the table is read only (readOnly=true) or editable (readOnly = false).
//	 * It also changes the cell renderer according to readOnly
//	 *
//	 * @param readOnly - if true, all the table cells become not editable, if false the data cells become editable
//	 *                 revised-->minor changes; only changed the call to getUncertaintyInColumns
//	 */
//	@Override public void setReadOnly(boolean readOnly) {
//		boolean wasReadOnly = super.isReadOnly();
//		super.setReadOnly(readOnly);
//		/*
//		The read only attribute is set after the constructor is invoked and then,
//		after the setData(node) method is called. Thus, the cell renderer may need to be changed.
//		This is the case if the new read only value is different from the previous one.
//		 */
//		if (wasReadOnly != readOnly) {
//			if (node.getPotentials() != null) {
//			    setCellRenderers(impossibleColumns);
//			} else {
//				setCellRenderers(impossibleColumns);
//			}
//		}
//		getEventValuesTable().setModifiable(!readOnly);
//	}


	/**
	 * This method sets the attributes this.readOnly= readOnly and modifiable = !readOnly to indicate
	 * if the table is read only (readOnly=true) or editable (readOnly = false).
	 * It also changes the cell renderer according to readOnly
	 *
	 * @param readOnly - if true, all the table cells become not editable, if false the data cells become editable
	 *                 revised-->minor changes; only changed the call to getUncertaintyInColumns
	 */
	@Override public void setReadOnly(boolean readOnly) {
		boolean wasReadOnly = super.isReadOnly();
		super.setReadOnly(readOnly);
		/*
		The read only attribute is set after the constructor is invoked and then,
		after the setData(node) method is called. Thus, the cell renderer may need to be changed.
		This is the case if the new read only value is different from the previous one.
		 */
		if (wasReadOnly != readOnly) {
			setCellRenderers();
		}
		getEventValuesTable().setModifiable(!readOnly);
	}




//	/**
//	 * This class overrides the double click listener calling the
//	 *
//	 * @see DoubleClickListener
//	 * revised-->not changed
//	 */
//	public class DoubleClickListener extends MouseAdapter {
//
//		@Override public void mouseClicked(MouseEvent e) {
//			if (e.getClickCount() == 2) {
//				doubleClickEvent(e);
//			}
//		}
//	}

}




