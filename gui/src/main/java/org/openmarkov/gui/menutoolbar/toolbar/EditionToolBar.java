/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.gui.dialog.node.NodeTypeInfo;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.EditorPanel;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.EnumMap;

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
    private JToggleButton linkCreationButton = null;
    
    
    /**
     * Button group to make autoexclusive the edition options.
     */
    private final ButtonGroup editionButtonGroup = new ButtonGroup();
    
    
    private final EnumMap<NodeType, JToggleButton> nodeCreationButtons = new EnumMap<>(NodeType.class);
    
    /**
     * This method initialises this instance.
     *
     * @param newListener object that listens to the buttons events.
     */
    public EditionToolBar(ActionListener newListener) {
        super(newListener);
        initialize();
    }

    public void updateFor(EditorPanel editorPanel) {
        if (editorPanel instanceof NetworkEditorPanel networkEditorPanel) {
            for (var entry : nodeCreationButtons.entrySet()) {
                JToggleButton toggleButton = entry.getValue();
                NodeType nodeType = entry.getKey();
                toggleButton.setVisible(NodeTypeInfo.of(nodeType).canBeUsedInProbnet(networkEditorPanel.getProbNet()));
            }
            getObjectSelectionButton().setSelected(false);
            getLinkCreationButton().setSelected(false);
            this.nodeCreationButtons.values().forEach(jToggleButton -> jToggleButton.setSelected(false));
            var selectedButton = switch (networkEditorPanel.getBaseTool()) {
                case SELECTION -> getObjectSelectionButton();
                case LINK -> getLinkCreationButton();
                case NODE -> this.nodeCreationButtons.get(networkEditorPanel.getPreferredNodeToCreate());
            };
            selectedButton.setSelected(true);
        }
    }

    /**
     * This method configures the toolbar.
     */
    private void initialize() {
        generateNodeCreationButtons();
        
        add(getCutButton());
        add(getCopyButton());
        add(getPasteButton());
        add(getRemoveButton());
        addSeparator();
        add(getUndoButton());
        add(getRedoButton());

        addSeparator();
        add(getObjectSelectionButton());
        this.nodeCreationButtons.values().forEach(this::add);
        add(getLinkCreationButton());
        
        add(Box.createHorizontalGlue());
    }
    
    private void generateNodeCreationButtons() {
        for (var nodeInfo : Arrays.stream(NodeTypeInfo.NODE_TYPE_INFOS)
                                  .filter(NodeTypeInfo::isVisuallyRepresented)
                                  .toList()) {
            
            var nodeCreationButton = new JToggleButton();
            nodeCreationButton.setIcon(nodeInfo.iconBind.icon());
            nodeCreationButton.setName(nodeInfo.codeName + "CreationMode");
            nodeCreationButton.setFocusable(false);
            nodeCreationButton
                    .setToolTipText(this.stringDatabase.getString("Edit.Mode." + nodeInfo.codeName + this.STRING_TOOLTIP_SUFFIX));
            nodeCreationButton.addActionListener(e -> {
                var currentNetworkEditorPanel = MainPanel.getCurrentNetworkEditorPanel();
                currentNetworkEditorPanel.setPreferredNodeToCreate(nodeInfo.nodeType);
                currentNetworkEditorPanel.setBaseTool(NetworkEditorPanel.BaseTool.NODE);
            });
            this.nodeCreationButtons.put(nodeInfo.nodeType, nodeCreationButton);
            this.editionButtonGroup.add(nodeCreationButton);
        }
    }
    
    /**
     * This method initialises cutButton.
     *
     * @return a cut button.
     */
    private JButton getCutButton() {
        if (this.cutButton == null) {
            this.cutButton = new JButton();
            this.cutButton.setIcon(IconBind.CUT_ENABLED.icon());
            this.cutButton.setFocusable(false);
            this.cutButton.setActionCommand(ActionCommands.CLIPBOARD_CUT.getCommandName());
            this.cutButton.setToolTipText(this.stringDatabase.getString(ActionCommands.CLIPBOARD_CUT + this.STRING_TOOLTIP_SUFFIX));
            this.cutButton.addActionListener(this.listener);
            this.cutButton.addMouseMotionListener(this);
        }
        return this.cutButton;
    }

    /**
     * This method initialises copyButton.
     *
     * @return a copy button.
     */
    private JButton getCopyButton() {
        if (this.copyButton == null) {
            this.copyButton = new JButton();
            this.copyButton.setIcon(IconBind.COPY_ENABLED.icon());
            this.copyButton.setFocusable(false);
            this.copyButton.setActionCommand(ActionCommands.CLIPBOARD_COPY.getCommandName());
            this.copyButton.setToolTipText(this.stringDatabase.getString(ActionCommands.CLIPBOARD_COPY + this.STRING_TOOLTIP_SUFFIX));
            this.copyButton.addActionListener(this.listener);
            this.copyButton.addMouseMotionListener(this);
        }
        return this.copyButton;
    }

    /**
     * This method initialises pasteButton.
     *
     * @return a paste button.
     */
    private JButton getPasteButton() {
        if (this.pasteButton == null) {
            this.pasteButton = new JButton();
            this.pasteButton.setIcon(IconBind.PASTE_ENABLED.icon());
            this.pasteButton.setFocusable(false);
            this.pasteButton.setActionCommand(ActionCommands.CLIPBOARD_PASTE.getCommandName());
            this.pasteButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.CLIPBOARD_PASTE + this.STRING_TOOLTIP_SUFFIX));
            this.pasteButton.addActionListener(this.listener);
            this.pasteButton.addMouseMotionListener(this);
        }
        return this.pasteButton;
    }

    /**
     * This method initialises removeButton.
     *
     * @return a remove button.
     */
    private JButton getRemoveButton() {
        if (this.removeButton == null) {
            this.removeButton = new JButton();
            this.removeButton.setIcon(IconBind.REMOVE_ENABLED.icon());
            this.removeButton.setFocusable(false);
            this.removeButton.setActionCommand(ActionCommands.OBJECT_REMOVAL.getCommandName());
            this.removeButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.OBJECT_REMOVAL + this.STRING_TOOLTIP_SUFFIX));
            this.removeButton.addActionListener(this.listener);
            this.removeButton.addMouseMotionListener(this);
        }
        return this.removeButton;
    }

    /**
     * This method initialises undoButton.
     *
     * @return a undo button.
     */
    public JButton getUndoButton() {
        if (this.undoButton == null) {
            this.undoButton = new JButton();
            this.undoButton.setIcon(IconBind.UNDO_ENABLED.icon());
            this.undoButton.setFocusable(false);
            this.undoButton.setActionCommand(ActionCommands.UNDO.getCommandName());
            this.undoButton.setToolTipText(this.stringDatabase.getString(ActionCommands.UNDO + this.STRING_TOOLTIP_SUFFIX));
            this.undoButton.addActionListener(this.listener);
            this.undoButton.addMouseMotionListener(this);
        }
        return this.undoButton;
    }

    /**
     * This method initialises redoButton.
     *
     * @return a redo button.
     */
    public JButton getRedoButton() {
        if (this.redoButton == null) {
            this.redoButton = new JButton();
            this.redoButton.setIcon(IconBind.REDO_ENABLED.icon());
            this.redoButton.setFocusable(false);
            this.redoButton.setActionCommand(ActionCommands.REDO.getCommandName());
            this.redoButton.setToolTipText(this.stringDatabase.getString(ActionCommands.REDO + this.STRING_TOOLTIP_SUFFIX));
            this.redoButton.addActionListener(this.listener);
            this.redoButton.addMouseMotionListener(this);
        }
        return this.redoButton;
    }

    /**
     * This method initialises objectSelectionButton.
     *
     * @return a object selection button.
     */
    private JToggleButton getObjectSelectionButton() {
        if (this.objectSelectionButton == null) {
            this.objectSelectionButton = new JToggleButton();
            this.objectSelectionButton.setIcon(IconBind.SELECTION_ENABLED.icon());
            this.objectSelectionButton.setName("ObjectSelectionMode");
            this.objectSelectionButton.setFocusable(false);
            this.objectSelectionButton.setToolTipText(this.stringDatabase.getString(ActionCommands.OBJECT_SELECTION + this.STRING_TOOLTIP_SUFFIX));
            this.objectSelectionButton.addActionListener(e -> MainPanel.getCurrentNetworkEditorPanel()
                                                                       .setBaseTool(NetworkEditorPanel.BaseTool.SELECTION));
            this.objectSelectionButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.objectSelectionButton);
        }
        return this.objectSelectionButton;
    }
    
    /**
     * This method initialises linkCreationButton.
     *
     * @return a link creation button.
     */
    private JToggleButton getLinkCreationButton() {
        if (this.linkCreationButton == null) {
            this.linkCreationButton = new JToggleButton();
            this.linkCreationButton.setIcon(IconBind.LINK_PARENT_ENABLED.icon());
            this.linkCreationButton.setFocusable(false);
            this.linkCreationButton.setName("LinkCreationMode");
            this.linkCreationButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.LINK_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.linkCreationButton.addActionListener(e -> MainPanel.getCurrentNetworkEditorPanel()
                                                                    .setBaseTool(NetworkEditorPanel.BaseTool.LINK));
            this.linkCreationButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.linkCreationButton);
        }
        return this.linkCreationButton;
    }

    /**
     * Returns the component that correspond to an action command.
     *
     * @param actionCommand action command that identifies the component.
     * @return a components identified by the action command.
     */
    @Override
    protected JComponent getJComponentActionCommand(String actionCommand) {
        JComponent component = switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.CLIPBOARD_CUT -> this.cutButton;
            case ActionCommands.CLIPBOARD_COPY -> this.copyButton;
            case ActionCommands.CLIPBOARD_PASTE -> this.pasteButton;
            case ActionCommands.OBJECT_REMOVAL -> this.removeButton;
            case ActionCommands.UNDO -> this.undoButton;
            case ActionCommands.REDO -> this.redoButton;
            case ActionCommands.OBJECT_SELECTION -> this.objectSelectionButton;
            case ActionCommands.CHANCE_CREATION -> this.nodeCreationButtons.get(NodeType.CHANCE);
            case ActionCommands.DECISION_CREATION -> this.nodeCreationButtons.get(NodeType.DECISION);
            case ActionCommands.UTILITY_CREATION -> this.nodeCreationButtons.get(NodeType.UTILITY);
            case ActionCommands.EVENT_CREATION -> this.nodeCreationButtons.get(NodeType.EVENT);
            case ActionCommands.LINK_CREATION -> this.linkCreationButton;
            case null, default -> null;
        };
        return component;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(getCutButton())) {
            getCutButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.CLIPBOARD_CUT + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getCopyButton())) {
            getCopyButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.CLIPBOARD_COPY + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getPasteButton())) {
            getPasteButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.CLIPBOARD_PASTE + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getRemoveButton())) {
            getRemoveButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.OBJECT_REMOVAL + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getUndoButton())) {
            getUndoButton().setToolTipText(this.stringDatabase.getString(ActionCommands.UNDO + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getRedoButton())) {
            getRedoButton().setToolTipText(this.stringDatabase.getString(ActionCommands.REDO + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getObjectSelectionButton())) {
            getObjectSelectionButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.OBJECT_SELECTION + this.STRING_TOOLTIP_SUFFIX));
        }
    }

}
