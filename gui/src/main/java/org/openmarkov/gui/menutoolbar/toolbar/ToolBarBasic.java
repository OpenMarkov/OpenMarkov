/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasicImpl;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionListener;

/**
 * Abstract base class for all application toolbars. Provides common initialization,
 * button creation utilities, and implements {@link MenuToolBarBasic} for state management.
 *
 * @author jmendoza
 */
@ImplementationRequirements(hasToExtendOneOfTheseClasses = ToolBarBasic.class, requiresOneOfTheseConstructors = @RequiredConstructor(ActionListener.class))
public abstract class ToolBarBasic extends JToolBar implements MenuToolBarBasic {
    
    /**
     *
     */
    private static final long serialVersionUID = 3038855074348761900L;
    
    /**
     * Suffix to retrieve tooltip strings from a string resource.
     */
    protected final String STRING_TOOLTIP_SUFFIX = ".ToolTip";
    
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * Object that listen to the user's actions.
     */
    protected final ActionListener listener;
    
    /**
     * This method initialises this instance.
     *
     * @param newListener listener that listen to the user's actions.
     */
    public ToolBarBasic(ActionListener newListener) {
        
        super();
        listener = newListener;
        initialize();
    }
    
    /**
     * This method initialises this instance.
     */
    private void initialize() {
        
        setFloatable(false);
        setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        setOrientation(SwingConstants.HORIZONTAL);
        setRollover(false);
    }
    
    /**
     * Returns the component that correspond to an action command.
     *
     * @param actionCommand action command that identifies the component.
     * @return a components identified by the action command.
     */
    protected abstract JComponent getJComponentActionCommand(String actionCommand);
    
    /**
     * Enables or disabled an option identified by an action command.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to enable the option, false to disable.
     */
    @Override public void setOptionEnabled(String actionCommand, boolean b) {
        
        MenuToolBarBasicImpl.setOptionEnabled(getJComponentActionCommand(actionCommand), b);
    }
    
    /**
     * Selects or unselects an option identified by an action command. Only
     * selects or unselects the components that are AbstractButton.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to select the option, false to unselect.
     */
    @Override public void setOptionSelected(String actionCommand, boolean b) {
        
        MenuToolBarBasicImpl.setOptionSelected(getJComponentActionCommand(actionCommand), b);
    }
    
    /**
     * Adds a foreground to the label of an option identified by an action command.
     * Only adds a foreground to the components that are AbstractButton.
     *
     * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
     *                      added.
     */
    @Override public void addOptionText(String actionCommand, String text) {
    
    }
    
    /**
     * changes a foreground to the label of an option identified by an action command.
     *
     * @param actionCommand action command that identifies the option.
     * @param text          foreground to set to Item. If null, nothing is
     *                      added.
     */
    @Override public void setText(String actionCommand, String text) {
    
    }
    
}

