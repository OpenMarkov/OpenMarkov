package org.openmarkov.gui.action;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TableWithEvents;
import org.openmarkov.core.model.network.potential.TableWithFunctions;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;

import java.util.Iterator;
import java.util.List;



/**
 * <code>NodePotentialEdit</code> is a simple edit that allows to modify the
 * node's <code>Potential</code> values. It is implemented for TablePotential
 * Only
 *
 * @author cmyago - copied/adapted from TablePotentialValueEdit;
 * 04/10/2023 FIXME check if it complies with OM wiki
 * @version 1.1 18/05/2022 - Changed to be used with numeric variables
 */
@SuppressWarnings("serial") public class EventTablePotentialValueEdit extends PNEdit {
	/**
	 * The column of the table where is the eventTablePotential
	 */
	private int col;
	/**
	 * The row of the table where is the eventTablePotential
	 */
	private int row;

	/**
	 * The new value of the eventTablePotential
	 */
	private double newDoubleValue;

	/**
	 * The new value of the eventTablePotential
	 */
	private String newFunctionValue;




	/**
	 * A list that store the edition order
	 */
	private List<Integer> priorityList;

	/**
	 * The index of the value selected in the graphic table
	 */
	private int indexSelected;
	private int potentialSelected;

	/**
	 * For doEdit
	 *
	 */

	private TablePotential tablePotential;
	private TableWithFunctions tableWithFunctions;

	private TableWithEvents tableWithEvents;

	/**
	 * the increment to get the real position of the value modified
	 */
	private int increment;

	/**
	 * Pseudo-util class with common operations used  in eventTablePotential tables
	 */
	private PotentialsTablePanelOperations tablePotentialsPanelOperations;

	/**
	 * the table eventTablePotential
	 */
	private double[] newDoubleTable;

	/**
	 * The table eventTablePotential
	 */
	private String[] newFunctionTable;

	private Node node;

	// Constructor

	/**
	 * Creates a new <code>EventTablePotentialEdit</code> specifying the node to be
	 * edited, the new value of the eventTablePotential, the row and column where is the
	 * value to be modified and a priority list for potentials updating.
	 *
	 * @param node                 the node to be edited
	 * @param newValue             the new value
	 * @param col                  the column in the edited table
	 * @param row                  the row in the edited table
	 * @param priorityList         the priority lists for potentials update.
	 */
	public EventTablePotentialValueEdit(Node node, TableWithEvents tableWithEvents, Object newValue, int row, int col, List<Integer> priorityList) {
		super(node.getProbNet());
		this.node = node;
			this.tableWithEvents = tableWithEvents;
			this.tablePotential = tableWithEvents.getTablePotential();
			this.tableWithFunctions = tableWithEvents.getTableWithFunctions();
		this.row = row;
		this.col = col;
		this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();
		if (tableWithFunctions == null) {
			try {
				this.newDoubleValue = ((Double) newValue).doubleValue();
			}catch (Exception e){
				Evaluator evaluator = new Evaluator();
				try {
					this.newDoubleValue = evaluator.getNumberResult((String)newValue);
				} catch (EvaluationException ex) {
					ex.printStackTrace();
				}
			}
			this.newDoubleTable = this.tablePotential.getValues();
		} else {
			try{
				this.newFunctionValue =(String) newValue;
			}catch(Exception e){
				this.newFunctionValue = newValue.toString();
			}
			this.newFunctionTable = tableWithFunctions.getFunctionValues();
		}
		this.priorityList = priorityList;
		this.indexSelected = PotentialsTablePanelOperations.calculateLastEditableRow(tablePotential) - row;
		this.increment = PotentialsTablePanelOperations.getPotentialStartIndexOfColumn(col, tablePotential);

		// Get the eventTablePotential index
		this.setPotentialSelected(PotentialsTablePanelOperations.getPotentialIndex(row, col, tablePotential));

	}

	/**
	 * This method fills the new table of tablePotential with the new values calculated after the edition of a cell
	 * and updates the probNet
	 *
	 * @throws <code>DoEditException</code> cyago only eliminated the different treatment for UTILITY role and introduced eventTablePotential
	 */
	@Override public void doEdit() throws DoEditException {
		//18/05/2022 - Changed to be used with numeric variables
		VariableType variableType = node.getVariable().getVariableType();
		if ((variableType == VariableType.EVENT) || (variableType == VariableType.NUMERIC)){
			try {
				newDoubleTable[getPotentialSelected()] = newDoubleValue;
			} catch(Exception e){

					newFunctionTable[getPotentialSelected()] = (String) newFunctionValue;

			}
		}else {
			if (priorityList.isEmpty()) {
				// User is editing a new column of potentials //node
				priorityList = getPriorityListInitialization();
			} else {
				// the user is editing a the same column of potentials that last
				// time
				priorityList.remove((Integer) getPotentialSelected());
				priorityList.add(getPotentialSelected());
			}
			Iterator<Integer> listIterator = priorityList.listIterator();
			Double sum = 0.0;
			Double rest;
			int position;
			int maxDecimals = 10;
			double epsilon;
			epsilon = Math.pow(10, -(maxDecimals + 2));
			newDoubleTable[getPotentialSelected()] = Util.roundAndReduce(newDoubleValue, epsilon, maxDecimals);
			while (listIterator.hasNext()) {
				position = listIterator.next();
					sum = Util.roundAndReduce(sum + newDoubleTable[position], epsilon, maxDecimals);
			}
			rest = Math.abs(Util.roundAndReduce(1 - sum, epsilon, maxDecimals));
			if (sum > 1.0) {
				listIterator = priorityList.listIterator();
				while (listIterator.hasNext() && rest != 0) {
					position = listIterator.next();
						rest = Util.roundAndReduce(rest - newDoubleTable[position], epsilon, maxDecimals);
						if (rest < 0) {// it is because the value of the table
							// is bigger than the rest
							// and now there's nothing left to reach
							// one
							newDoubleTable[position] = Math.abs(Util.roundAndReduce(rest, epsilon, maxDecimals));
							break;
						} else
							newDoubleTable[position] = 0.0;
//					}
				}
			} else {// =< 1
				boolean updated = false;
				listIterator = priorityList.listIterator();
				while (listIterator.hasNext() && !updated) {
					position = listIterator.next();
						newDoubleTable[position] = Util.roundAndReduce(newDoubleTable[position] + rest, epsilon, maxDecimals);
						updated = true;
				}
			}

		}


	}

	/**
	 * Gets the TablePotential of the node
	 *
	 * @return variable1 <code>Variable</code>
	 */
	public TablePotential getTablePotential() {
		return tablePotential;
	}


	/**
	 * Gets the TablePotential of the node
	 *
	 * @return variable1 <code>Variable</code>
	 */
	public TableWithEvents getTableWithEvents() {
		return tableWithEvents;
	}





	/**
	 * Gets the priority list initialisation.
	 * Creates a list with the positions in the table of tablePotential of the column containing the edited value.
	 * The list first contains the positions corresponding to the not edited cells
	 * and the last position corresponds to the edited cell
	 *
	 * @return the priority list initialised with the the value edited in the
	 * last place of the list
	 * <p>
	 * revised-->not changed
	 */
	private List<Integer> getPriorityListInitialization() {
		for (int i = 0; i < node.getVariable().getNumStates(); i++) {
			if (i != indexSelected)
				priorityList.add(i + increment);
		}
		priorityList.add(indexSelected + increment);
		return priorityList;
	}

	/**
	 * Gets the priority list
	 *
	 * @return the priority list
	 */
	public List<Integer> getPriorityList() {
		return priorityList;
	}


	/**
	 * Gets the row position associated to value edited if priorityList exists
	 *
	 * @param position position of the value in the array of values
	 * @return the position in the table
	 */
	public int getRowPosition(int position) {
		int lastRow = PotentialsTablePanelOperations.calculateLastEditableRow(tablePotential);
		return lastRow - position % tablePotential.getDimensions()[0];
	}

	/**
	 * Gets the row position associated to value edited if priorityList no
	 * exists
	 *
	 * @return the position in the table
	 */
	public int getRowPosition() {
		return row;
	}

	/**
	 * Gets the column where the value is edited
	 *
	 * @return the column edited
	 */
	public int getColumnPosition() {
		return col;
	}

    /**
     * Index of the value selected
     */
    public int getPotentialSelected() {
        return potentialSelected;
    }

    public void setPotentialSelected(int potentialSelected) {
        this.potentialSelected = potentialSelected;
    }
}
