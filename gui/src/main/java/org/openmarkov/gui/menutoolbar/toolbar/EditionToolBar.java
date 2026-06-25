/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * This class implements the edition toolbar of the application.
 *
 * @author jmendoza
 */
public class EditionToolBar extends ToolBarBasic implements MouseMotionListener {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 2660826021862866432L;
    /**
     * Button to invoke cut.
     */
    private JButton cutButton = null;
    /**
     * Button to invoke copy.
     */
    private JButton copyButton = null;
    /**
     * Button to invoke paste.
     */
    private JButton pasteButton = null;
    /**
     * Button to invoke remove.
     */
    private JButton removeButton = null;
    /**
     * Button to invoke undo.
     */
    private JButton undoButton = null;
    /**
     * Button to invoke redo.
     */
    private JButton redoButton = null;
    /**
     * Button to activate object selection.
     */
    private JToggleButton objectSelectionButton = null;
    /**
     * Button to activate chance creation.
     */
    private JToggleButton chanceCreationButton = null;
    /**
     * Button to activate decision creation.
     */
    private JToggleButton decisionCreationButton = null;
    
    // 03/2019
    /**
     * Button to activate chance creation.
     */
    private JToggleButton eventCreationButton = null;
    //
    
    /**
     * Button to activate utility creation.
     */
    private JToggleButton utilityCreationButton = null;
    /**
     * Button to activate link creation.
     */
    private JToggleButton linkCreationButton = null;
    /**
     * Button group to make autoexclusive the edition options.
     */
    private final ButtonGroup editionButtonGroup = new ButtonGroup();
    
    /**
     * This method initialises this instance.
     *
     * @param newListener object that listens to the buttons events.
     */
    public EditionToolBar(ActionListener newListener) {
        super(newListener);
        initialize();
    }
    
    /**
     * This method configures the toolbar.
     */
    private void initialize() {
        add(getCutButton());
        add(getCopyButton());
        add(getPasteButton());
        add(getRemoveButton());
        addSeparator();
        add(getUndoButton());
        add(getRedoButton());
        addSeparator();
        add(getObjectSelectionButton());
        add(getChanceCreationButton());
        add(getDecisionCreationButton());
        add(getUtilityCreationButton());
        add(getEventCreationButton());
        add(getLinkCreationButton());
        add(Box.createHorizontalGlue());
    }
    
    /**
     * This method initialises cutButton.
     *
     * @return a cut button.
     */
    private JButton getCutButton() {
        if (cutButton == null) {
            cutButton = new JButton();
            cutButton.setIcon(IconBind.CUT_ENABLED.icon());
            cutButton.setFocusable(false);
            cutButton.setActionCommand(ActionCommands.CLIPBOARD_CUT.getCommandName());
            cutButton.setToolTipText(stringDatabase.getString(ActionCommands.CLIPBOARD_CUT + STRING_TOOLTIP_SUFFIX));
            cutButton.addActionListener(listener);
            cutButton.addMouseMotionListener(this);
        }
        return cutButton;
    }
    
    /**
     * This method initialises copyButton.
     *
     * @return a copy button.
     */
    private JButton getCopyButton() {
        if (copyButton == null) {
            copyButton = new JButton();
            copyButton.setIcon(IconBind.COPY_ENABLED.icon());
            copyButton.setFocusable(false);
            copyButton.setActionCommand(ActionCommands.CLIPBOARD_COPY.getCommandName());
            copyButton.setToolTipText(stringDatabase.getString(ActionCommands.CLIPBOARD_COPY + STRING_TOOLTIP_SUFFIX));
            copyButton.addActionListener(listener);
            copyButton.addMouseMotionListener(this);
        }
        return copyButton;
    }
    
    /**
     * This method initialises pasteButton.
     *
     * @return a paste button.
     */
    private JButton getPasteButton() {
        if (pasteButton == null) {
            pasteButton = new JButton();
            pasteButton.setIcon(IconBind.PASTE_ENABLED.icon());
            pasteButton.setFocusable(false);
            pasteButton.setActionCommand(ActionCommands.CLIPBOARD_PASTE.getCommandName());
            pasteButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.CLIPBOARD_PASTE + STRING_TOOLTIP_SUFFIX));
            pasteButton.addActionListener(listener);
            pasteButton.addMouseMotionListener(this);
        }
        return pasteButton;
    }
    
    /**
     * This method initialises removeButton.
     *
     * @return a remove button.
     */
    private JButton getRemoveButton() {
        if (removeButton == null) {
            removeButton = new JButton();
            removeButton.setIcon(IconBind.REMOVE_ENABLED.icon());
            removeButton.setFocusable(false);
            removeButton.setActionCommand(ActionCommands.OBJECT_REMOVAL.getCommandName());
            removeButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.OBJECT_REMOVAL + STRING_TOOLTIP_SUFFIX));
            removeButton.addActionListener(listener);
            removeButton.addMouseMotionListener(this);
        }
        return removeButton;
    }
    
    /**
     * This method initialises undoButton.
     *
     * @return a undo button.
     */
    public JButton getUndoButton() {
        if (undoButton == null) {
            undoButton = new JButton();
            undoButton.setIcon(IconBind.UNDO_ENABLED.icon());
            undoButton.setFocusable(false);
            undoButton.setActionCommand(ActionCommands.UNDO.getCommandName());
            undoButton.setToolTipText(stringDatabase.getString(ActionCommands.UNDO + STRING_TOOLTIP_SUFFIX));
            undoButton.addActionListener(listener);
            undoButton.addMouseMotionListener(this);
        }
        return undoButton;
    }
    
    /**
     * This method initialises redoButton.
     *
     * @return a redo button.
     */
    public JButton getRedoButton() {
        if (redoButton == null) {
            redoButton = new JButton();
            redoButton.setIcon(IconBind.REDO_ENABLED.icon());
            redoButton.setFocusable(false);
            redoButton.setActionCommand(ActionCommands.REDO.getCommandName());
            redoButton.setToolTipText(stringDatabase.getString(ActionCommands.REDO + STRING_TOOLTIP_SUFFIX));
            redoButton.addActionListener(listener);
            redoButton.addMouseMotionListener(this);
        }
        return redoButton;
    }
    
    /**
     * This method initialises objectSelectionButton.
     *
     * @return a object selection button.
     */
    private JToggleButton getObjectSelectionButton() {
        if (objectSelectionButton == null) {
            objectSelectionButton = new JToggleButton();
            objectSelectionButton.setIcon(IconBind.SELECTION_ENABLED.icon());
            objectSelectionButton.setName("ObjectSelectionMode");
            objectSelectionButton.setActionCommand(ActionCommands.OBJECT_SELECTION.getCommandName());
            objectSelectionButton.setFocusable(false);
            objectSelectionButton.setToolTipText(stringDatabase.getString(ActionCommands.OBJECT_SELECTION + STRING_TOOLTIP_SUFFIX));
            objectSelectionButton.addActionListener(listener);
            objectSelectionButton.addMouseMotionListener(this);
            editionButtonGroup.add(objectSelectionButton);
        }
        return objectSelectionButton;
    }
    
    /**
     * This method initialises chanceCreationButton.
     *
     * @return a chance creation button.
     */
    private JToggleButton getChanceCreationButton() {
        if (chanceCreationButton == null) {
            chanceCreationButton = new JToggleButton();
            chanceCreationButton.setIcon(IconBind.CHANCE_ENABLED.icon());
            chanceCreationButton.setName("ChanceCreationMode");
            chanceCreationButton.setActionCommand(ActionCommands.CHANCE_CREATION.getCommandName());
            chanceCreationButton.setFocusable(false);
            chanceCreationButton.setToolTipText(stringDatabase.getString(ActionCommands.CHANCE_CREATION + STRING_TOOLTIP_SUFFIX));
            chanceCreationButton.addActionListener(listener);
            chanceCreationButton.addMouseMotionListener(this);
            editionButtonGroup.add(chanceCreationButton);
        }
        return chanceCreationButton;
    }
    
    /**
     * This method initialises decisionCreationButton.
     *
     * @return a decision creation button.
     */
    private JToggleButton getDecisionCreationButton() {
        if (decisionCreationButton == null) {
            decisionCreationButton = new JToggleButton();
            decisionCreationButton.setIcon(IconBind.DECISION_ENABLED.icon());
            decisionCreationButton.setName("DecisionCreationMode");
            decisionCreationButton.setActionCommand(ActionCommands.DECISION_CREATION.getCommandName());
            decisionCreationButton.setFocusable(false);
            decisionCreationButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.DECISION_CREATION + STRING_TOOLTIP_SUFFIX));
            decisionCreationButton.addActionListener(listener);
            decisionCreationButton.addMouseMotionListener(this);
            editionButtonGroup.add(decisionCreationButton);
        }
        return decisionCreationButton;
    }
    
    /**
     * This method initialises utilityCreationButton.
     *
     * @return a utility creation button.
     */
    private JToggleButton getUtilityCreationButton() {
        if (utilityCreationButton == null) {
            utilityCreationButton = new JToggleButton();
            utilityCreationButton.setIcon(IconBind.UTILITY_ENABLED.icon());
            utilityCreationButton.setActionCommand(ActionCommands.UTILITY_CREATION.getCommandName());
            utilityCreationButton.setFocusable(false);
            utilityCreationButton.setName("UtilityCreationMode");
            utilityCreationButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.UTILITY_CREATION + STRING_TOOLTIP_SUFFIX));
            utilityCreationButton.addActionListener(listener);
            utilityCreationButton.addMouseMotionListener(this);
            editionButtonGroup.add(utilityCreationButton);
        }
        return utilityCreationButton;
    }
    
    // 03/2019
    /**
     * This method initialises eventCreationButton.
     *
     * @return an event creation button.
     */
    private JToggleButton getEventCreationButton() {
        if (eventCreationButton == null) {
            eventCreationButton = new JToggleButton();
            eventCreationButton.setIcon(IconBind.EVENT_ENABLED.icon());
            eventCreationButton.setActionCommand(ActionCommands.EVENT_CREATION.getCommandName());
            eventCreationButton.setFocusable(false);
            eventCreationButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.EVENT_CREATION + STRING_TOOLTIP_SUFFIX));
            eventCreationButton.addActionListener(listener);
            eventCreationButton.addMouseMotionListener(this);
            editionButtonGroup.add(eventCreationButton);
        }
        return eventCreationButton;
    }
    
    /**
     * This method initialises linkCreationButton.
     *
     * @return a link creation button.
     */
    private JToggleButton getLinkCreationButton() {
        if (linkCreationButton == null) {
            linkCreationButton = new JToggleButton();
            linkCreationButton.setIcon(IconBind.LINK_PARENT_ENABLED.icon());
            linkCreationButton.setActionCommand(ActionCommands.LINK_CREATION.getCommandName());
            linkCreationButton.setFocusable(false);
            linkCreationButton.setName("LinkCreationMode");
            linkCreationButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.LINK_CREATION + STRING_TOOLTIP_SUFFIX));
            linkCreationButton.addActionListener(listener);
            linkCreationButton.addMouseMotionListener(this);
            editionButtonGroup.add(linkCreationButton);
        }
        return linkCreationButton;
    }
    
    /**
     * Returns the component that correspond to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        JComponent component = switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.CLIPBOARD_CUT -> cutButton;
            case ActionCommands.CLIPBOARD_COPY -> copyButton;
            case ActionCommands.CLIPBOARD_PASTE -> pasteButton;
            case ActionCommands.OBJECT_REMOVAL -> removeButton;
            case ActionCommands.UNDO -> undoButton;
            case ActionCommands.REDO -> redoButton;
            case ActionCommands.OBJECT_SELECTION -> objectSelectionButton;
            case ActionCommands.CHANCE_CREATION -> chanceCreationButton;
            case ActionCommands.DECISION_CREATION -> decisionCreationButton;
            case ActionCommands.UTILITY_CREATION -> utilityCreationButton;
            case ActionCommands.EVENT_CREATION -> eventCreationButton;
            case ActionCommands.LINK_CREATION -> linkCreationButton;
            case null, default -> null;
        };
        return component;
    }
    
    @Override public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(getCutButton())) {
            getCutButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.CLIPBOARD_CUT + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getCopyButton())) {
            getCopyButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.CLIPBOARD_COPY + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getPasteButton())) {
            getPasteButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.CLIPBOARD_PASTE + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getRemoveButton())) {
            getRemoveButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.OBJECT_REMOVAL + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getUndoButton())) {
            getUndoButton().setToolTipText(stringDatabase.getString(ActionCommands.UNDO + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getRedoButton())) {
            getRedoButton().setToolTipText(stringDatabase.getString(ActionCommands.REDO + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getObjectSelectionButton())) {
            getObjectSelectionButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.OBJECT_SELECTION + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getChanceCreationButton())) {
            getChanceCreationButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.CHANCE_CREATION + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getDecisionCreationButton())) {
            getDecisionCreationButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.DECISION_CREATION + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getUtilityCreationButton())) {
            getUtilityCreationButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.UTILITY_CREATION + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getLinkCreationButton())) {
            getLinkCreationButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.LINK_CREATION + STRING_TOOLTIP_SUFFIX));
        }
    }
    
    public void addEditionButton(AbstractButton button) {
        add(button);
        editionButtonGroup.add(button);
    }
}
