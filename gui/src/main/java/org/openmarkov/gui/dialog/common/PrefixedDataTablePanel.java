/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.exception.ThereIsNoNodeInDataException;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a key table with the following features:
 * <ul>
 * <li>Its elements aren't modifiable.</li>
 * <li>New elements can be added selecting them of a prefixed set.</li>
 * <li>An element of the prefixed set can be added only once.</li>
 * <li>The first column is treated as the rest of columns.</li>
 * <li>The information of a row (except the first column) can't be taken up or
 * down.</li>
 * <li>The rows can be removed.</li>
 * </ul>
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.0 jlgozalo - change class modifier to public
 */
public class PrefixedDataTablePanel extends KeyTablePanel {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 2127072068749928448L;
    final ArrayList<PNEdit> edits = new ArrayList<PNEdit>();
    /**
     * Prefixed data.
     */
    private final Object[][] prefixedData;
    /**
     * Array that contains the prefixed data that is not in the table.
     */
    private Object[][] absentData = null;
    /**
     * String that appears in the titlebar of the dialog box showed to add new
     * rows.
     */
    private final String titleToSelectRows;
    private final Node node;
    
    /**
     * This is the default constructor
     *
     * @param newColumns           array of texts that appear in the header of the columns.
     * @param newData              content of the cells (subset of prefixedData).
     * @param newPrefixedData      content that can appears into the cells.
     * @param newTitleToSelectRows title of the window where the user can select new rows.
     */
    public PrefixedDataTablePanel(Node node, String[] newColumns, Object[][] newData, Object[][] newPrefixedData,
                                  String newTitleToSelectRows, boolean firstColumnHidden) {
        
        super(newColumns, new Object[0][0], false, false);
        this.node = node;
        prefixedData = newPrefixedData.clone();
        titleToSelectRows = newTitleToSelectRows;
        initialize();
        valuesTable.setFirstColumnHidden(firstColumnHidden);
        setData(newData);
    }
    
    private static Object[][] fillArrayWithNodes(List<Node> nodes) {
        
        int i;
        int l = nodes.size();
        Object[][] result = new Object[l][2];
        for (i = 0; i < l; i++) {
            result[i][0] = "p_" + i; //internal name for the parent
            result[i][1] = nodes.get(i).getName();
        }
        
        return result;
    }
    
    /**
     * Sets a new table model with new data.
     *
     * @param newData new data for the table.
     */
    @Override public void setData(Object[][] newData) {
        
        data = newData.clone();
        tableModel = null;
        valuesTable.setModel(getTableModel());
        absentData = absentPrefixedData();
        setEnabledAddValue(absentData.length != 0);
        
    }
    
    /**
     * Invoked when the button 'add' is pressed.
     */
    @Override protected void actionPerformedAddValue(ActionEvent e) throws DoEditException, ThereIsNoNodeInDataException {
        Object[][] newData;
        int newIndex = valuesTable.getRowCount();
        if (absentData == null) {
            throw new ThereIsNoNodeInDataException();
        }
        newData = requestNewData();
        if (newData != null) {
            for (int i = 0; i < newData.length; i++) {
                String name = (String) newData[i][1];
                for (PNEdit edit : edits) {
                    if (((AddLinkEdit) edit).getNodeFrom().getName().equals(name)) {
                        edit.executeEdit();
                        tableModel.insertRow(newIndex + i, newData[i]);
                        edits.remove(edit);
                        break;
                    }
                    
                }
            }
            valuesTable.setRowSelectionInterval(newIndex, newIndex);
            absentData = absentPrefixedData();
            setEnabledAddValue(absentData.length != 0);
        }
        
    }
    
    /**
     * This method request the user to select one or more new elements to add.
     * The new elements are the subset of the prefixed set that aren't in the
     * array 'data'.
     *
     * @return the elements that the user has selected or null if he/she has
     * selected nothing.
     */
    private Object[][] requestNewData() {
        
        Object[][] possibleData = absentData;
        KeyListSelectionDialog dialog = new KeyListSelectionDialog(GUIUtils.getOwner(this), titleToSelectRows, possibleData, columns);
        
        return (dialog.requestSelectRows() == OkCancelDialog.ChosenOption.Ok) ? dialog.getSelectedRows() : null;
        
    }
    
    /**
     * This method returns an array of arrays of strings whose elements are the
     * prefixed ones that aren't in the array 'data'.
     *
     * @return the prefixed data that aren't in the array 'data'.
     */
    private Object[][] absentPrefixedData() {
        List<Node> allNodes = node.getProbNet().getNodes();
        List<Node> nodes = new ArrayList<Node>();
        edits.clear();
        
        for (Node otherNode : allNodes) {
            if (!node.getParents().contains(otherNode) && otherNode != node) {
                
                //LinkEdit linkEdit = new LinkEdit(node.getProbNet(),pNode.getName(), node.getName(), true, true);
                AddLinkEdit linkEdit = new AddLinkEdit(node.getProbNet(), otherNode.getVariable(), node.getVariable(),
                                                       true);
                @ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG, reasonDescription = "An edit is added without it being done")
                var check = false;
                edits.add(linkEdit);
                nodes.add(otherNode);
                
                
            }
            
        }
        return fillArrayWithNodes(nodes);
    }
    
    /**
     * Invoked when the button 'remove' is pressed.
     */
    @Override
    protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        int selectedRow = valuesTable.getSelectedRow();
        String name = (String) valuesTable.getValueAt(selectedRow, 1, e.getSource());
		/*LinkEdit linkEdit;
		linkEdit = new LinkEdit(node.getProbNet(), name,
				node.getName(), true, 
				false);*/
        ProbNet probNet = node.getProbNet();
        RemoveLinkEdit linkEdit;
        linkEdit = new RemoveLinkEdit(probNet, probNet.getVariable(name), node.getVariable(), true);
        ProbNet nodeProbNet = node.getProbNet();
        linkEdit.executeEdit();
        tableModel.removeRow(selectedRow);
        valuesTable.getRowCount();
        // Fixing issue #249
        // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/249/removing-the-two-parents-of-a-node
        // Removed the "if" clause
        // No parent is selected after a removal
			/* if ((rowCount > 0) && (selectedRow >= rowCount)) {
				valuesTable.getSelectionModel().setSelectionInterval(
					selectedRow - 1, selectedRow - 1);
			}*/
        absentData = absentPrefixedData();
        setEnabledAddValue(true);
        // After deleting an item from the list,
        // the remove value button is disabled
        // till a new element is selected from the list
        setEnabledRemoveValue(false);
        
    }
    
    // ESCA-JAVA0025:
    
    /**
     * Invoked when the button 'up' is pressed.
     */
    @Override protected void actionPerformedUpValue(ActionEvent e) {
    
    }
    
    // ESCA-JAVA0025:
    
    /**
     * Invoked when the button 'down' is pressed.
     */
    @Override protected void actionPerformedDownValue(ActionEvent e) {
    
    }
    
    /**
     * Invoked when the row selection changes.
     *
     * @param e selection event information.
     */
    /*
    Fixing issue https://bitbucket.org/cisiad/org.openmarkov.issues/issue/221/button-delete-in-node-properties-parents
    The remove button was always set to disabled, unless more than two parents were present
    We need to override the method from KeyTablePanel
    as in it we are not able to determine in which panel we are located and thus
    if the button needs to be enabled or not.
     */
    @Override public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        
        boolean removeValueButtonEnabled = true;
        int rowCount = valuesTable.getRowCount();
        
        // If there are less than two rows
        if (rowCount <= 2) {
            // But at least there is one, it has not to be the nodes parent table, as
            // one parent may be removable
            if (rowCount >= 1 && this.titleToSelectRows != StringDatabase.getUniqueInstance()
                                                                         .getString("NodeParentsPanel.prefixedDataTablePanelParentsTable.Title")) {
                removeValueButtonEnabled = false;
            }
        }
        // The button is enabled or disabled accordingly
        removeValueButton.setEnabled(removeValueButtonEnabled);
    }
}
