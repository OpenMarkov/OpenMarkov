/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * This class implements a dialog box with a horizontal buttons panel placed in
 * the bottom of the window. This panel has two buttons: a 'OK' button that is
 * activated pressing the ENTER key and a 'CANCEL' button activated pressing the
 * ESC key.
 *
 * @author jmendoza
 * @version 1.2 jlgozalo - 30/05/2010 set OK_BUTTON to default
 */
public class OkCancelDialog extends BottomPanelButtonDialog {
    
    @Serial
    private static final long serialVersionUID = 1176820837760605949L;
    
    public enum ChosenOption {
        Ok, Cancel
    }
    
    /**
     * Button selected by the user.
     */
    private ChosenOption selectedOption = ChosenOption.Ok;
    
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * Ok button.
     */
    private final JButton jButtonOK;
    
    /**
     * Constructor. initialises the instance.
     *
     * @param owner window that owns the dialog.
     */
    public OkCancelDialog(Window owner) {
        super(owner);
        // setSize(550, 310);
        this.setName("OKCancelHorizontalDialog");
        
        this.jButtonOK = new JButton();
        this.jButtonOK.setName("jButtonApply");
        this.jButtonOK.setIcon(IconBind.ACCEPT_ENABLED.icon());
        this.jButtonOK.setText(this.stringDatabase.getString("OKCancelHorizontalDialog.jButtonOK.Text"));
        this.jButtonOK.setMnemonic(this.stringDatabase.getString("OKCancelHorizontalDialog.jButtonOK.Mnemonic")
                                                      .charAt(0));
        this.jButtonOK.addActionListener(e1 -> {
            try {
                if (this.doOkClickBeforeHide()) {
                    selectedOption = ChosenOption.Ok;
                    this.dispose();
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new UnrecoverableException(ex);
            }
        });
        this.addButtonToButtonsPanel(jButtonOK);
        this.setDefaultButton(jButtonOK);
        
        var jButtonCancel = generateGenericCancelButton();
        while (jButtonCancel.getActionListeners().length > 0) {
            jButtonCancel.removeActionListener(jButtonCancel.getActionListeners()[0]);
        }
        jButtonCancel.addActionListener(e -> {
            this.doCancelClickBeforeHide();
            this.selectedOption = ChosenOption.Cancel;
            this.setVisible(false);
            this.dispose();
        });
        this.setCancelButton(jButtonCancel);
        this.pack();
    }
    
    /**
     * This method initialises jButtonApply.
     *
     * @return a new Ok button.
     */
    protected JButton getOKButton() {
        return this.jButtonOK;
    }
    
    
    /**
     * Shows or hides this Dialog depending on the value of parameter visible. If visible is
     * true, the selected button is set to OK_BUTTON.
     *
     * @param visible if true, makes the dialog visible, otherwise hides the dialog.
     */
    @Override public void setVisible(boolean visible) {
        if (visible) {
            this.selectedOption = ChosenOption.Ok;
        }
        super.setVisible(visible);
    }
    
    /**
     * This method carries out the actions when the user presses the Ok button
     * before hiding the dialog.
     *
     * @return true if the dialog box can be closed.
     */
    protected boolean doOkClickBeforeHide() throws Exception {
        selectedOption = ChosenOption.Ok;
        return true;
    }
    
    /**
     * This method carries out the actions when the user press the Cancel button
     * before hide the dialog.
     */
    protected void doCancelClickBeforeHide() {
        selectedOption = ChosenOption.Cancel;
    }
    
    /**
     * Returns the option chosen by the user when the dialog was closed.
     *
     * @return {@link ChosenOption#Ok} or {@link ChosenOption#Cancel}
     */
    public ChosenOption getSelectedOption() {
        return this.selectedOption;
    }
    
}
