/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.dialog.configuration;

import org.openmarkov.core.exception.UnrecoverableException;

import javax.swing.table.AbstractTableModel;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Table model for the Preferences Editor
 *
 * @author jlgozalo
 * @version 1.0 28 Aug 2009
 */
class PreferencesTableModel extends AbstractTableModel {
    /**
     *
     */
    private static final long serialVersionUID = 3278473793314149896L;
    /**
     * Preferences to work with
     */
    Preferences pref;
    /**
     * Previous preferences to be used in case of CANCEL actions
     */
    final Preferences prefSaved;
    /**
     * Keys for the preferences
     */
    String[] keys;
    
    /**
     * constructor
     *
     * @param pref The preferences for the table model
     */
    public PreferencesTableModel(Preferences pref) throws BackingStoreException {
        this.pref = pref;
        this.prefSaved = pref;
        keys = pref.keys();
    }
    
    @Override public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Key";
            case 1 -> "Value";
            default -> "-";
        };
    }
    
    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }
    
    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        pref.put(keys[rowIndex], aValue.toString());
        try {
            this.syncSave();
        } catch (BackingStoreException e) {
            pref.remove(keys[rowIndex]);
            throw new UnrecoverableException(e);
        }
    }
    
    @Override public Object getValueAt(int row, int column) {
        String key = keys[row];
        if (column == 0)
            return key;
        Object value = pref.get(key, "(Unknown)");
        return value;
    }
    
    @Override public int getColumnCount() {
        return 2;
    }
    
    @Override public int getRowCount() {
        return keys.length;
    }
    
    /**
     * undo action
     */
    public void undo() throws BackingStoreException {
        var previousValueOfPref = this.pref;
        try {
            this.pref = this.prefSaved;
            this.syncSave();
        } catch (BackingStoreException e) {
            this.pref = previousValueOfPref;
            throw e;
        }
    }
    
    /**
     * sync action
     */
    public void syncSave() throws BackingStoreException {
        pref.sync(); // make sure the backing store is synchronized
    }
    
}
