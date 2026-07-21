/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.exception.ZoomOutOfRangeException;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * This class fills its combobox and listen to it to send action commands
 * defined in the class ActionCommands.
 *
 * @author jmendoza
 */
public class ZoomComboBox extends JComboBox<String> implements ItemListener, KeyListener {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 5380198895714343936L;
    /**
     * Prefixed elements of the combobox.
     */
    private static final String[] ZOOM_VALUES = {"500%", "200%", "150%", "100%", "75%", "50%", "25%", "10%"};
    
    private static final int MIN_ZOOM = 10;
    private static final int MAX_ZOOM = 500;
    /**
     * Old value of the combobox
     */
    private String oldValue;
    /**
     * Object that listen to the user's actions.
     */
    private final ActionListener listener;
    
    /**
     * Constructor that fills and initialize the combobox.
     *
     * @param newListener object that listens to the zoomManager values.
     */
    public ZoomComboBox(ActionListener newListener) {
        super(ZOOM_VALUES);
        listener = newListener;
        initialize();
    }
    
    /**
     * This method initialises this instance.
     */
    private void initialize() {
        setName("ZoomComboBox");
        setEditable(true);
        setPrototypeDisplayValue("500%X");
        Dimension size = new Dimension(getPreferredSize().width, 25);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        setSelectedIndex(3); // 100% item
        oldValue = (String) getSelectedItem();
        addItemListener(this);
        addComponentKeyListener(this);
    }
    
    /**
     * Adds a new key listener to the component where edition is performed.
     *
     * @param newListener new key listener.
     */
    private void addComponentKeyListener(KeyListener newListener) {
        Component componentEditor = getEditor().getEditorComponent();
        if (componentEditor != null) {
            componentEditor.addKeyListener(newListener);
        }
    }
    
    /**
     * Returns an integer that contains the value of the string. The string can
     * contain the symbol '%' at the end.
     *
     * @param zoomString string that contains a zoomManager value.
     *
     * @return the integer zoomManager value or null if the string has not a correct
     * value.
     */
    private static Integer getZoomValue(String zoomString) {
        zoomString = zoomString.trim();
        int length = zoomString.length();
        if (length == 0) {
            return null;
        }
        int percentajePosition = zoomString.indexOf('%');
        if (percentajePosition >= 0) {
            if (percentajePosition == (length - 1)) {
                zoomString = zoomString.substring(0, percentajePosition);
            } else {
                zoomString = "";
            }
        }
        try {
            return Integer.parseInt(zoomString);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Invoked when an item has been selected.
     *
     * @param e event information.
     */
    @Override public void itemStateChanged(ItemEvent e) {
        String newActionCommand;
        Integer zoomValue;
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        if (e.getItem().equals(oldValue)) {
            return;
        }
        zoomValue = getZoomValue((String) e.getItem());
        if ((zoomValue == null) || (zoomValue < 10) || (zoomValue > 500)) {
            setSelectedItem(oldValue);
            throw new UnrecoverableException(new ZoomOutOfRangeException((String) e.getItem(), ZoomComboBox.MIN_ZOOM, ZoomComboBox.MAX_ZOOM));
        }
        newActionCommand = ActionCommands.getZoomActionCommandValue(zoomValue.doubleValue() / 100);
        setSelectedItem(zoomValue + "%");
        oldValue = (String) getSelectedItem();
        listener.actionPerformed(new ActionEvent(this, 0, newActionCommand));
        getRootPane().requestFocusInWindow();
    }
    
    /**
     * This method sets the value of the combobox.
     *
     * @param value new value of zoomManager.
     */
    public void setZoom(double value) {
        setSelectedItem((int) Math.round(value * 100) + "%");
    }
    
    /**
     * Invoked when a key has been pressed.
     *
     * @param e event information.
     */
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getEditor().setItem(oldValue);
            getRootPane().requestFocusInWindow();
        }
    }
    
    /**
     * Invoked when a key has been released. This method does nothing.
     *
     * @param e event information.
     */
    @Override public void keyReleased(KeyEvent e) {
    }
    
    /**
     * Invoked when a key has been typed. This method does nothing.
     *
     * @param e event information.
     */
    @Override public void keyTyped(KeyEvent e) {
    }
    
    /**
     * Enables the combo box so that items can be selected. When the combo box
     * is disabled, items cannot be selected, values cannot be typed into its
     * field and no elements are selected.
     *
     * @param b true enables the combobox and false disables it.
     */
    @Override public void setEnabled(boolean b) {
        if (!b) {
            setSelectedIndex(-1);
        }
        super.setEnabled(b);
    }
}
