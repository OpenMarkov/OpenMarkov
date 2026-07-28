/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.node.NodeTypeInfo;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.EditorPanel;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.java.initialization.Lazy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Objects;

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
     * Button to activate node creation.
     */
    private JToggleButton nodeCreationButton;

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
    private Lazy<JComboBox<NodeTypeInfo>> nodeTypeComboBox;


    /**
     * This method initialises this instance.
     *
     * @param newListener object that listens to the buttons events.
     */
    public EditionToolBar(ActionListener newListener) {
        super(newListener);
        this.nodeTypeComboBox = Lazy.of(() -> {
            var comboBox = new JComboBox<NodeTypeInfo>();
            Arrays.stream(NodeType.values()).map(NodeTypeInfo::of).filter(NodeTypeInfo::isVisuallyRepresented).forEach(comboBox::addItem);
            comboBox.setBackground(GUIColors.General.TRANSPARENT.getColor());
            comboBox.setActionCommand(ActionCommands.SET_NODE_MODE_CREATION.getCommandName());

            comboBox.setBorder(BorderFactory.createEmptyBorder());
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (index == -1) {
                        return new JPanel();
                    }
                    NodeTypeInfo nodeTypeInfo = (NodeTypeInfo) value;
                    JLabel jLabel = new JLabel(nodeTypeInfo.iconBind.icon());
                    jLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    jLabel.setHorizontalTextPosition(SwingConstants.LEFT);
                    jLabel.setToolTipText("Insert " + nodeTypeInfo.visualName.toLowerCase() + " nodes");
                    return jLabel;
                }
            });
            comboBox.addItemListener(e -> {
                if (comboBox.getSelectedItem() instanceof NodeTypeInfo nodeTypeInfo) {
                    this.nodeCreationButton.setIcon(nodeTypeInfo.iconBind.icon());
                    MainPanel.getCurrentNetworkEditorPanel().setPreferredNodeToCreate(nodeTypeInfo.nodeType);
                    getNodeCreationButton().setSelected(true);
                }
            });
            comboBox.addActionListener(e -> {
                if (e.getModifiers() == 0) {
                    return;
                }
                this.listener.actionPerformed(e);
            });
            return comboBox;


//        comboBox.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                System.out.println("Main display area");
//            }
//        });
//        for (Component comp : comboBox.getComponents()) {
//            if (comp instanceof JButton) {
//                comp.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mousePressed(MouseEvent e) {
//                        System.out.println("Arrow button");
//                    }
//                });
//            }
//        }
//        Object accessibleChild = comboBox.getUI().getAccessibleChild(comboBox, 0);
//        if (accessibleChild instanceof ComboPopup) {
//            ComboPopup popup = (ComboPopup) accessibleChild;
//            JList<?> list = popup.getList();
//            list.addMouseListener(new MouseAdapter() {
//                @Override
//                public void mousePressed(MouseEvent e) {
//                    int index = list.locationToIndex(e.getPoint());
//                    if (index != -1) {
//                        Object item = list.getModel().getElementAt(index);
//                        System.out.println("Item selected [" + item + "] at index " + index);
//                    }
//                }
//            });
//        }
        });

        initialize();
    }

    public void updateFor(EditorPanel editorPanel) {
        if (editorPanel instanceof NetworkEditorPanel networkEditorPanel) {
            NodeType preferredNodeToCreate = networkEditorPanel.getPreferredNodeToCreate();
            var nodeTypeInfos = Arrays.stream(NodeTypeInfo.NODE_TYPE_INFOS)
                    .filter(NodeTypeInfo::isVisuallyRepresented)
                    .filter(nodeTypeInfo -> nodeTypeInfo.canBeUsedInProbnet(networkEditorPanel.getProbNet()))
                    .toList();

            for (int i = 0; i < this.nodeTypeComboBox.get().getItemCount(); i++) {
                if (!nodeTypeInfos.contains(this.nodeTypeComboBox.get().getItemAt(i))) {
                    nodeTypeComboBox.get().removeItemAt(i);
                    i--;
                }
            }
            nodeTypeInfos.stream()
                    .filter(nodeTypeInfo -> {
                        for (int i = 0; i < this.nodeTypeComboBox.get().getItemCount(); i++) {
                            if (this.nodeTypeComboBox.get().getItemAt(i) == nodeTypeInfo) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .forEach(nodeTypeInfo -> {
                        this.nodeTypeComboBox.get().addItem(nodeTypeInfo);
                    });

            boolean showNodeTypeComboBox = nodeTypeComboBox.get().getItemCount() > 1;
            Dimension size = showNodeTypeComboBox ? new Dimension(25, 25) : new Dimension(0, 0);
            nodeTypeComboBox.get().setSize(size);
            nodeTypeComboBox.get().setPreferredSize(size);
            nodeTypeComboBox.get().setMinimumSize(size);
            nodeTypeComboBox.get().setMaximumSize(size);
            nodeTypeComboBox.get().setVisible(showNodeTypeComboBox);
            this.nodeCreationButton.setToolTipText("Insert " + ((NodeTypeInfo) Objects.requireNonNull(this.nodeTypeComboBox.get().getSelectedItem())).visualName.toLowerCase() + " nodes");

            this.nodeCreationButton.setIcon(NodeTypeInfo.of(preferredNodeToCreate).iconBind.icon());
            networkEditorPanel.setPreferredNodeToCreate(preferredNodeToCreate);

            getObjectSelectionButton().setSelected(false);
            getLinkCreationButton().setSelected(false);
            getNodeCreationButton().setSelected(false);
            var selectedButton = switch (networkEditorPanel.getBaseTool()) {
                case SELECTION -> getObjectSelectionButton();
                case LINK -> getLinkCreationButton();
                case NODE -> getNodeCreationButton();
            };
            selectedButton.setSelected(true);
        }
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
        add(getNodeCreationButton());
        add(this.nodeTypeComboBox.get());
        add(getLinkCreationButton());


        add(Box.createHorizontalGlue());
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
            this.objectSelectionButton.setActionCommand(ActionCommands.OBJECT_SELECTION.getCommandName());
            this.objectSelectionButton.setFocusable(false);
            this.objectSelectionButton.setToolTipText(this.stringDatabase.getString(ActionCommands.OBJECT_SELECTION + this.STRING_TOOLTIP_SUFFIX));
            this.objectSelectionButton.addActionListener(this.listener);
            this.objectSelectionButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.objectSelectionButton);
        }
        return this.objectSelectionButton;
    }

    /**
     * This method initialises chanceCreationButton.
     *
     * @return a chance creation button.
     */
    private JToggleButton getNodeCreationButton() {
        if (this.nodeCreationButton == null) {
            this.nodeCreationButton = new JToggleButton();
            this.nodeCreationButton.setName("NodeCreationMode");
            this.nodeCreationButton.setActionCommand(ActionCommands.SET_NODE_MODE_CREATION.getCommandName());
            this.nodeCreationButton.setFocusable(false);
            this.nodeCreationButton.setToolTipText(this.stringDatabase.getString(ActionCommands.SET_NODE_MODE_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.nodeCreationButton.addActionListener(this.listener);
            this.editionButtonGroup.add(this.nodeCreationButton);
        }
        return this.nodeCreationButton;
    }

    /**
     * This method initialises chanceCreationButton.
     *
     * @return a chance creation button.
     */
    private JToggleButton getChanceCreationButton() {
        if (this.chanceCreationButton == null) {
            this.chanceCreationButton = new JToggleButton();
            this.chanceCreationButton.setIcon(IconBind.CHANCE_ENABLED.icon());
            this.chanceCreationButton.setName("ChanceCreationMode");
            this.chanceCreationButton.setActionCommand(ActionCommands.CHANCE_CREATION.getCommandName());
            this.chanceCreationButton.setFocusable(false);
            this.chanceCreationButton.setToolTipText(this.stringDatabase.getString(ActionCommands.CHANCE_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.chanceCreationButton.addActionListener(this.listener);
            this.chanceCreationButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.chanceCreationButton);
        }
        return this.chanceCreationButton;
    }

    /**
     * This method initialises decisionCreationButton.
     *
     * @return a decision creation button.
     */
    private JToggleButton getDecisionCreationButton() {
        if (this.decisionCreationButton == null) {
            this.decisionCreationButton = new JToggleButton();
            this.decisionCreationButton.setIcon(IconBind.DECISION_ENABLED.icon());
            this.decisionCreationButton.setName("DecisionCreationMode");
            this.decisionCreationButton.setActionCommand(ActionCommands.DECISION_CREATION.getCommandName());
            this.decisionCreationButton.setFocusable(false);
            this.decisionCreationButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.DECISION_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.decisionCreationButton.addActionListener(this.listener);
            this.decisionCreationButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.decisionCreationButton);
        }
        return this.decisionCreationButton;
    }

    /**
     * This method initialises utilityCreationButton.
     *
     * @return a utility creation button.
     */
    private JToggleButton getUtilityCreationButton() {
        if (this.utilityCreationButton == null) {
            this.utilityCreationButton = new JToggleButton();
            this.utilityCreationButton.setIcon(IconBind.UTILITY_ENABLED.icon());
            this.utilityCreationButton.setActionCommand(ActionCommands.UTILITY_CREATION.getCommandName());
            this.utilityCreationButton.setFocusable(false);
            this.utilityCreationButton.setName("UtilityCreationMode");
            this.utilityCreationButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.UTILITY_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.utilityCreationButton.addActionListener(this.listener);
            this.utilityCreationButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.utilityCreationButton);
        }
        return this.utilityCreationButton;
    }

    // 03/2019

    /**
     * This method initialises eventCreationButton.
     *
     * @return an event creation button.
     */
    private JToggleButton getEventCreationButton() {
        if (this.eventCreationButton == null) {
            this.eventCreationButton = new JToggleButton();
            this.eventCreationButton.setIcon(IconBind.EVENT_ENABLED.icon());
            this.eventCreationButton.setActionCommand(ActionCommands.EVENT_CREATION.getCommandName());
            this.eventCreationButton.setFocusable(false);
            this.eventCreationButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.EVENT_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.eventCreationButton.addActionListener(this.listener);
            this.eventCreationButton.addMouseMotionListener(this);
            this.editionButtonGroup.add(this.eventCreationButton);
        }
        return this.eventCreationButton;
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
            this.linkCreationButton.setActionCommand(ActionCommands.LINK_CREATION.getCommandName());
            this.linkCreationButton.setFocusable(false);
            this.linkCreationButton.setName("LinkCreationMode");
            this.linkCreationButton
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.LINK_CREATION + this.STRING_TOOLTIP_SUFFIX));
            this.linkCreationButton.addActionListener(this.listener);
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
            case ActionCommands.CHANCE_CREATION -> this.chanceCreationButton;
            case ActionCommands.DECISION_CREATION -> this.decisionCreationButton;
            case ActionCommands.UTILITY_CREATION -> this.utilityCreationButton;
            case ActionCommands.EVENT_CREATION -> this.eventCreationButton;
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
        } else if (e.getSource().equals(getChanceCreationButton())) {
            getChanceCreationButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.CHANCE_CREATION + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getDecisionCreationButton())) {
            getDecisionCreationButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.DECISION_CREATION + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getUtilityCreationButton())) {
            getUtilityCreationButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.UTILITY_CREATION + this.STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getLinkCreationButton())) {
            getLinkCreationButton()
                    .setToolTipText(this.stringDatabase.getString(ActionCommands.LINK_CREATION + this.STRING_TOOLTIP_SUFFIX));
        }
    }

}
