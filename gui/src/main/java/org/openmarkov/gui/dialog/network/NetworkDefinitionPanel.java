/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.core.ChangeNetworkTypeEdit;
import org.openmarkov.core.action.core.NetworkCommentEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeUtils;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.dialog.CommentListener;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.java.classUtils.ClassUtils;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * Panel to set the definition of a network.
 *
 * @author jlgozalo
 * @version 1.1 ibermejo
 */
public class NetworkDefinitionPanel extends JPanel implements CommentListener {
    /**
     * internal serial id
     */
    private static final long serialVersionUID = 1047978130482205148L;
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * The Network Type Label
     */
    private JLabel jLabelNetworkTypes = null;
    /**
     * The Network Types Combo Box Drop Down List
     */
    private JComboBox<Class<? extends NetworkType>> jComboBoxNetworkTypes = null;
    /**
     * The Network Definition Comment Label
     */
    private JTextArea jTextAreaLabelNetworkDefinitionComment;
    /**
     * The label of the comment together with the button that edits it.
     */
    private JPanel commentHeaderPanel;
    /**
     * The button that opens the comment editor.
     */
    private JButton jButtonEditComment;
    /**
     * The Network Comment Scroll Panel box
     */
    private CommentHTMLScrollPane commentHTMLScrollPaneNetworkDefinition = null;
    /**
     * Checkbox to define Object Orientedness of Network
     */
    private JCheckBox jcheckBoxShowCommentOnOpening = null;
    /**
     * Specifies if the network whose additionalProperties are edited is new.
     */
    private final boolean newNetwork;
    private final ProbNet probNet;
    private final NetworkPropertiesDialog parent;
    /**
     * Space between the comment label and the button that edits it.
     */
    private static final int COMMENT_BUTTON_GAP = 6;
    
    /**
     * Constructor.
     *
     * @param probNet manage the network access
     */
    public NetworkDefinitionPanel(NetworkPropertiesDialog parent, ProbNet probNet) {
        this.parent = parent;
        this.newNetwork = probNet == null;
        this.probNet = probNet;
        initialize();
        if (probNet != null) {
            setFieldsFromProperties(probNet);
        }
    }
    
    /**
     * initialises the panel
     */
    private void initialize() {
        setName("NetworkDefinitionPanel");
        final GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup().addContainerGap().addGroup(
                        groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                                           groupLayout.createSequentialGroup()
                                                      .addComponent(getCommentHeaderPanel(),
                                                                    GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                                    GroupLayout.PREFERRED_SIZE)
                                                      .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                      .addComponent(getCommentHTMLScrollPaneNetworkDefinition(),
                                                                    GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                                                      .addContainerGap())
                                   .addGroup(groupLayout.createSequentialGroup()
                                                        .addGroup(
                                                                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                           .addComponent(getJLabelNetworkTypes(), GroupLayout.DEFAULT_SIZE,
                                                                                         GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                           .addGroup(
                                                                                   groupLayout.createSequentialGroup()
                                                                                              .addComponent(getJComboBoxNetworkTypes(),
                                                                                                            GroupLayout.PREFERRED_SIZE, 255,
                                                                                                            GroupLayout.PREFERRED_SIZE)
                                                                                              .addContainerGap())))
                                   .addGroup(groupLayout.createParallelGroup()
                                                        .addComponent(getCheckBoxShowCommentOnOpening(), GroupLayout.PREFERRED_SIZE,
                                                                      280, GroupLayout.PREFERRED_SIZE)))));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                groupLayout.createSequentialGroup().addContainerGap().addGroup(
                                   groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                              .addComponent(getJLabelNetworkTypes(), GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                                              .addComponent(getJComboBoxNetworkTypes(), GroupLayout.PREFERRED_SIZE,
                                                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                           .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
                                   groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                              .addComponent(getCommentHeaderPanel())
                                              .addComponent(getCommentHTMLScrollPaneNetworkDefinition(), GroupLayout.DEFAULT_SIZE,
                                                            117, Short.MAX_VALUE)).addGroup(
                                   groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                              .addComponent(getCheckBoxShowCommentOnOpening()))
                           .addComponent(getCheckBoxShowCommentOnOpening())
                           .addContainerGap(189, Short.MAX_VALUE)));
        setLayout(groupLayout);
    }
    
    /**
     * initializes the getJLabelNetworkTypes
     *
     * @return jLabelNetworkTypes the label for the NetworkTypes field
     */
    private JLabel getJLabelNetworkTypes() {
        if (jLabelNetworkTypes == null) {
            jLabelNetworkTypes = new JLabel();
            jLabelNetworkTypes.setText("a Label :");
            jLabelNetworkTypes.setText(stringDatabase.getString("NetworkDefinitionPanel.NetworkTypes.Text"));
            jLabelNetworkTypes.setMinimumSize(new Dimension(25, 0));
            jLabelNetworkTypes.setName("jLabelNetworkTypes");
            jLabelNetworkTypes.setDisplayedMnemonic(
                    stringDatabase.getString("NetworkDefinitionPanel.NetworkTypes.Mnemonic").charAt(0));
            jLabelNetworkTypes.setLabelFor(getJComboBoxNetworkTypes());
        }
        return jLabelNetworkTypes;
    }
    
    /**
     * initialises the jComboBoxNetworkTypes
     *
     * @return jComboBoxNetworkTypes the comboBox of the Network Types field
     */
    private JComboBox<Class<? extends NetworkType>> getJComboBoxNetworkTypes() {
        if (jComboBoxNetworkTypes == null) {
            Class[] networkClasses = NetworkTypeUtils.NETWORK_TYPE_CLASSES
                    .stream()
                    .filter(ClassUtils::isConcrete)
                    .toArray(Class[]::new);
            jComboBoxNetworkTypes = new JComboBox<>(networkClasses);
            jComboBoxNetworkTypes.setRenderer(new JComboBoxFunctionRender<Class<? extends NetworkType>>(
                    networkTypeClass -> NetworkTypeUtils.getInfo(networkTypeClass).visualName()));
            jComboBoxNetworkTypes.setName("jComboBoxNetworkTypes");
            jComboBoxNetworkTypes.setEditable(false);
            //
            if (newNetwork) {
                // Set Bayesian Network as default
                jComboBoxNetworkTypes.setSelectedItem(BayesianNetworkType.class);
            } else {
                jComboBoxNetworkTypes.setSelectedItem(probNet.getNetworkType().getClass());
                jComboBoxNetworkTypes.addActionListener(arg0 -> {
                    try {
                        networkTypeChanged();
                    } catch (DoEditException e) {
                        throw new UnrecoverableException(e);
                    }
                });
            }
        }
        return jComboBoxNetworkTypes;
    }
    
    /**
     * initialises the getJTextAreaLabelNetworkDefinitionComment
     *
     * @return jTextAreaLabelNetworkDefinitionComment the extended label for the
     * comment field of Network Definition
     */
    protected JTextArea getJTextAreaLabelNetworkDefinitionComment() {
        if (jTextAreaLabelNetworkDefinitionComment == null) {
            jTextAreaLabelNetworkDefinitionComment = new JTextArea();
            jTextAreaLabelNetworkDefinitionComment.setLineWrap(true);
            jTextAreaLabelNetworkDefinitionComment.setOpaque(false);
            jTextAreaLabelNetworkDefinitionComment.setName("jTextAreaLabelNetworkDefinitionComment");
            jTextAreaLabelNetworkDefinitionComment.setFocusable(false);
            jTextAreaLabelNetworkDefinitionComment.setEditable(false);
            jTextAreaLabelNetworkDefinitionComment.setFont(getJLabelNetworkTypes().getFont());
            jTextAreaLabelNetworkDefinitionComment.setText("an Extended Label");
            jTextAreaLabelNetworkDefinitionComment
                    .setText(stringDatabase.getString("NetworkDefinitionPanel.NetworkDefinitionComment.Text"));
            // A text area stretches as far as it is given room to, and it shares a column with
            // the comment box, which is far taller. Without a ceiling it swallows that height
            // and pushes the button below it out of sight of the label.
            jTextAreaLabelNetworkDefinitionComment.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE,
                                  jTextAreaLabelNetworkDefinitionComment.getPreferredSize().height));
        }
        return jTextAreaLabelNetworkDefinitionComment;
    }

    /**
     * The column to the left of the comment: what the box holds, and the way to change it. The
     * button replaces the instruction that the label used to carry, which told the user to
     * double-click but could not be pressed, reached with the keyboard or seen as an action.
     *
     * @return the panel with the comment label and the button that edits the comment
     */
    private JPanel getCommentHeaderPanel() {
        if (commentHeaderPanel == null) {
            commentHeaderPanel = new JPanel();
            commentHeaderPanel.setName("commentHeaderPanel");
            commentHeaderPanel.setOpaque(false);
            commentHeaderPanel.setLayout(new BoxLayout(commentHeaderPanel, BoxLayout.Y_AXIS));
            getJTextAreaLabelNetworkDefinitionComment().setAlignmentX(LEFT_ALIGNMENT);
            getJButtonEditComment().setAlignmentX(LEFT_ALIGNMENT);
            commentHeaderPanel.add(getJTextAreaLabelNetworkDefinitionComment());
            commentHeaderPanel.add(Box.createVerticalStrut(COMMENT_BUTTON_GAP));
            commentHeaderPanel.add(getJButtonEditComment());
            // The column is as tall as the comment box beside it; without this the spare height
            // is shared out and the button drifts away from the label it belongs to.
            commentHeaderPanel.add(Box.createVerticalGlue());
        }
        return commentHeaderPanel;
    }

    /**
     * initialises the button that opens the comment editor
     *
     * @return jButtonEditComment the button that edits the comment
     */
    private JButton getJButtonEditComment() {
        if (jButtonEditComment == null) {
            jButtonEditComment = new JButton(stringDatabase.getString("NetworkDefinitionPanel.EditComment.Text"));
            jButtonEditComment.setName("jButtonEditComment");
            jButtonEditComment.setMnemonic(
                    stringDatabase.getString("NetworkDefinitionPanel.NetworkDefinitionComment.Mnemonic").charAt(0));
            jButtonEditComment.addActionListener(
                    e -> getCommentHTMLScrollPaneNetworkDefinition().openEditor());
        }
        return jButtonEditComment;
    }

    /**
     * initialises the getCommentHTMLScrollPaneForNetworkDefinition
     *
     * @return commentHTMLScrollPaneNetworkDefinition the comment for the Node
     * definition
     */
    private CommentHTMLScrollPane getCommentHTMLScrollPaneNetworkDefinition() {
        if (commentHTMLScrollPaneNetworkDefinition == null) {
            commentHTMLScrollPaneNetworkDefinition = new CommentHTMLScrollPane();
            commentHTMLScrollPaneNetworkDefinition.setName("commentHTMLScrollPaneNetworkDefinition");
            commentHTMLScrollPaneNetworkDefinition.setToolTipText(
                    stringDatabase.getString("NetworkDefinitionPanel.NetworkDefinitionComment.Hint"));
            if (!newNetwork) {
                commentHTMLScrollPaneNetworkDefinition.addCommentListener(this);
            }
        }
        return commentHTMLScrollPaneNetworkDefinition;
    }
    
    private JCheckBox getCheckBoxShowCommentOnOpening() {
        if (jcheckBoxShowCommentOnOpening == null) {
            jcheckBoxShowCommentOnOpening = new JCheckBox(
                    stringDatabase.getString("NetworkDefinitionPanel.ShowComment.Text"), false);
            jcheckBoxShowCommentOnOpening.setEnabled(true);
            jcheckBoxShowCommentOnOpening.setSelected(probNet != null && probNet.getShowCommentWhenOpening());
            jcheckBoxShowCommentOnOpening.addActionListener(e -> {
                try {
                    new NetworkCommentEdit(probNet, probNet.getComment(), jcheckBoxShowCommentOnOpening.isSelected())
                            .executeEdit();
                } catch (DoEditException ex) {
                    throw new UnreachableException(ex);
                }
            });
        }
        return jcheckBoxShowCommentOnOpening;
    }
    
    /**
     * This method fills the content of the fields from a NetworkProperties
     * object.
     *
     * @param probNet network from where load the information.
     */
    private void setFieldsFromProperties(ProbNet probNet) {
        getJComboBoxNetworkTypes().setSelectedItem(probNet.getNetworkType().getClass());
        // set the title for comment
        MessageFormat messageForm = new MessageFormat(
                stringDatabase.getString("NetworkDefinitionPanel." + "CommentHTMLScrollPaneNetworkDefinition.Text"));
        // String shortNetworkName = (String)network.properties.
        // get(netPropertyNames.NAME.toString());
        String shortNetworkName = probNet.getName();
        int lastIndexOfSlashPath = shortNetworkName.lastIndexOf('\\');
        shortNetworkName = shortNetworkName.substring(lastIndexOfSlashPath + 1);
        Object[] labelArgs = new Object[]{shortNetworkName};
        getCommentHTMLScrollPaneNetworkDefinition().setTitle(messageForm.format(labelArgs));
        // String comment = (String)network.properties.get(
        // netPropertyNames.COMMENT.toString());
        getCommentHTMLScrollPaneNetworkDefinition().setCommentHTMLTextPaneText(probNet.getComment());
    }
    
    /**
     * This method checks the name field.
     *
     * @return true, if the name field isn't empty; otherwise, false.
     */
    protected static boolean checkName() {
        // String name = getJTextFieldNetworkName().getText();
        return true;
    }
    
    @Override public void commentHasChanged() throws DoEditException {
        // check if the comment is empty
        String comment = getCommentHTMLScrollPaneNetworkDefinition().isEmpty() ?
                "" :
                getCommentHTMLScrollPaneNetworkDefinition().getCommentText();
        NetworkCommentEdit networkCommentEdit = new NetworkCommentEdit(probNet, comment, jcheckBoxShowCommentOnOpening.isSelected());
        networkCommentEdit.executeEdit();
    }
    
    private void networkTypeChanged() throws DoEditException {
        Class<? extends NetworkType> itemSelected = (Class<? extends NetworkType>) jComboBoxNetworkTypes.getSelectedItem();
        if (itemSelected == null) {
            return;
        }
        NetworkType selectedNetworkType = NetworkTypeUtils.safeInstanciate(itemSelected);
        if (selectedNetworkType == null) {
            return;
        }
        if (probNet.getNetworkType().toString().compareTo(selectedNetworkType.toString()) == 0) {
            return;
        }
        /*
        28/10/2014
        Fixing issue 169
        https://bitbucket.org/cisiad/org.openmarkov.issues/issue/169/opening-the-network-properties-dialog
        The ChangeNetworkTypeEdit should only be invoked if the network type has actually changed
         */
        try {
            new ChangeNetworkTypeEdit(probNet, selectedNetworkType).executeEdit();
            parent.update();
            //parent.getNetworkAdvancedPanel().update(probNet); SUSTITUIDA POR 342
        } catch (DoEditException.CannotDoEditException e) {
            // TODO maintain comboBox with the current probNet
            // TODO temporal change in exception management
            throw e;
        }
        
    }
    
    public NetworkType getNetworkType() {
        return NetworkTypeUtils.safeInstanciate((Class<? extends NetworkType>) jComboBoxNetworkTypes.getSelectedItem());
    }
    
    public String getNetworkComment() {
        return getCommentHTMLScrollPaneNetworkDefinition().getCommentText();
    }
    
}