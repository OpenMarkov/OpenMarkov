/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.stream.IntStream;

/**
 * This class implements a dialog that has a horizontal button panel in the
 * bottom of the window.
 *
 * @author jmendoza
 * @version 1.0 jmendoza
 */
public class BottomPanelButtonDialog extends DialogBase {
    
    @Serial
    private static final long serialVersionUID = -4648589019411570235L;
    
    private final JPanel componentsPanel;
    private final JPanel buttonsPanel;
    
    
    /**
     * Creates a dialog with a content area and a horizontal button panel at the bottom.
     *
     * @param owner the parent window that owns this dialog
     */
    public BottomPanelButtonDialog(Window owner) {
        super(owner);
        this.setName("BottomPanelButtonDialog");
        this.setResizable(false);
        this.setModal(true);
        
        JPanel jContentPane = new JPanel();
        jContentPane.setLayout(new BorderLayout());
        this.componentsPanel = new JPanel();
        jContentPane.add(this.getComponentsPanel(), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.buttonsPanel = new JPanel();
        this.buttonsPanel.setLayout(new GridLayout(1, 0, 10, 10));
        this.buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(this.buttonsPanel);
        jContentPane.add(bottomPanel, BorderLayout.PAGE_END);
        this.setContentPane(jContentPane);
    }
    
    /**
     * This method initialises componentsPanel.
     *
     * @return a new components panel.
     */
    protected final JPanel getComponentsPanel() {
        return this.componentsPanel;
    }
    
    /**
     * This method adds a new button to the buttons panel and a space of 10
     * units to the right of this button.
     *
     * @param button button that will be added to the panel.
     */
    protected final void addButtonToButtonsPanel(JButton button) {
        this.buttonsPanel.remove(button);
        this.buttonsPanel.add(button);
    }
    
    protected final void addButtonToButtonsPanel(JButton button, int index) {
        this.buttonsPanel.remove(button);
        this.buttonsPanel.add(button, index);
    }
    
    protected final void removeButtonFromButtonsPanel(JButton button) {
        if(button==null) {
            return;
        }
        this.buttonsPanel.remove(button);
    }
    
    @Override public void setCancelButton(JButton cancelButton) {
        JButton oldCancelButton = getCancelButton();
        super.setCancelButton(cancelButton);
        if (oldCancelButton == null) {
            this.addButtonToButtonsPanel(cancelButton);
            return;
        }
        var buttonPanelComponents = this.buttonsPanel.getComponents();
        int oldButtonIndex = IntStream.range(0, buttonPanelComponents.length)
                                      .filter(componentIndex -> oldCancelButton == buttonPanelComponents[componentIndex])
                                      .findFirst()
                                      .orElse(-1);
        if (oldButtonIndex == -1) {
            this.buttonsPanel.add(getCancelButton());
        } else {
            this.buttonsPanel.remove(oldButtonIndex);
            this.buttonsPanel.add(getCancelButton(), oldButtonIndex);
        }
    }
}
