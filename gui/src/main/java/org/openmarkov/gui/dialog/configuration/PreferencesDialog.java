/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 * PreferencesDialog.java
 */

package org.openmarkov.gui.dialog.configuration;

import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.configuration.LocalPreference;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.dialog.io.OMFileChooser;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * Class to help user to configure the system and user preferences in the
 * OPENMARKOV system
 *
 * @author jlgozalo
 * @version 1.1 11 Mar 2010 fix import/export/reset errors
 */
public class PreferencesDialog extends JDialog implements ActionListener {
    /**
     * generate serial id
     */
    private static final long serialVersionUID = -8957131079235183957L;
    /**
     * constants for graphical drawing
     */
    private static final int DIVIDER_LOCATION = 250;
    private static final int PREFERENCE_WIDTH = 640;
    private static final int PREFERENCE_HEIGHT = 480;
    /**
     * file chooser for export/import options
     */
    final private OMFileChooser chooser = new OMFileChooser();
    /**
     * String database
     */
    protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * main display components
     */
    JTree jTreePreferences = null;
    JTable jTableEdition = null;
    /**
     * buttons for the dialog
     */
    JButton jButtonSave = null;
    JButton jButtonCancel = null;
    JButton jButtonExport = null;
    JButton jButtonImport = null;
    JButton jButtonReset = null;
    
    /**
     * Creates PreferencesEditor dialog that show all System and User
     * preferences.
     *
     * @param owner owner JFrame
     */
    public PreferencesDialog(JFrame owner) throws BackingStoreException {
        this(owner, "OPENMARKOV User Preferences", OPENMARKOV_NODE_PREFERENCES, true/*
         * ,
         * OpenMarkovPreferences
         * .
         * OPENMARKOV_KERNEL_PREFERENCES
         * , false
         */);
    }
    
    /**
     * Creates PreferencesEditor dialog that show all System and User
     * preferences.
     *
     * @param owner owner JFrame
     * @param title title of dialog
     */
    public PreferencesDialog(JFrame owner, String title) throws BackingStoreException {
        this(owner, title, OPENMARKOV_NODE_PREFERENCES, true/*
         * ,
         * OpenMarkovPreferences
         * .
         * OPENMARKOV_KERNEL_PREFERENCES
         * ,
         * false
         */);
    }
    
    /**
     * @param owner         owner JFrame
     * @param title         title of dialog
     * @param userObj       the package to which this object belongs is used as the
     *                      root-node of the User preferences tree (if userObj is null,
     *                      then the rootnode of all user preferences will be used)
     * @param showUserPrefs if true, then show user preferences
     */
    public PreferencesDialog(JFrame owner, String title, Object userObj, boolean showUserPrefs/*
     * ,
     * Object
     * systemObj
     * ,
     * boolean
     * showSystemPrefs
     */) throws BackingStoreException {
        super(owner);
        setTitle(title);
        setChooser();
        int width = PREFERENCE_WIDTH;
        int height = PREFERENCE_HEIGHT;
        int x = owner.getX() + (owner.getWidth() - width) / 2;
        int y = owner.getY() + (owner.getHeight() - height) / 2;
        this.setBounds(x, y, width, height);
        getContentPane().setLayout(new BorderLayout(5, 5));
        createTree(userObj, showUserPrefs/* , systemObj, showSystemPrefs */);
        jTableEdition = new JTable();
        createSplitPane();
        createButtonPanel();
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    /**
     * create the Tree where the Preferences will be displayed
     *
     * @param userObj       class to define the User Preferences
     * @param showUserPrefs true if User Preferences will be shown
     */
    private void createTree(Object userObj, boolean showUserPrefs/*
     * , Object
     * systemObj,
     * boolean
     * showSystemPrefs
     */) throws BackingStoreException {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Preferences");
        /*
         * if (showSystemPrefs) { rootNode.add(createSystemRootNode(systemObj));
         * //rootNode.add(createSystemNodeForPackage(systemObj)); }
         */
        if (showUserPrefs) {
            rootNode.add(createUserRootNode(userObj));
            // rootNode.add(createUserNodeForPackage(userObj));
        }
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        jTreePreferences = new JTree(model);
        TreeNode openmarkov = rootNode.getChildAt(0);
        // not shown color preferences in tree
        ((PreferenceTreeNode) openmarkov).removeChildAt(0);
        jTreePreferences.addTreeSelectionListener(new PrefTreeSelectionListener());
    }
    
    private MutableTreeNode createSystemRootNode(Object obj) throws BackingStoreException {
        if (obj == null) {
            return new PreferenceTreeNode(Preferences.systemRoot());
        }
        return new PreferenceTreeNode(Preferences.systemRoot().node(obj.toString()));
    }
    
    private MutableTreeNode createUserRootNode(Object obj) throws BackingStoreException {
        if (obj == null) {
            return new PreferenceTreeNode(Preferences.userRoot());
        }
        return new PreferenceTreeNode(Preferences.userRoot().node((String) obj));
    }
    
    private MutableTreeNode createSystemNodeForPackage(Object obj) throws BackingStoreException {
        if (obj == null) {
            return new PreferenceTreeNode(Preferences.systemRoot());
        }
        return new PreferenceTreeNode(Preferences.systemNodeForPackage((Class<?>) obj));
    }
    
    private MutableTreeNode createUserNodeForPackage(Object obj) throws BackingStoreException {
        if (obj == null) {
            return new PreferenceTreeNode(Preferences.userRoot());
        }
        return new PreferenceTreeNode(Preferences.userNodeForPackage((Class<?>) obj));
    }
    
    private void createSplitPane() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setLeftComponent(new JScrollPane(jTreePreferences));
        splitPane.setRightComponent(new JScrollPane(jTableEdition));
        splitPane.setDividerLocation(DIVIDER_LOCATION);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }
    
    private void createButtonPanel() {
        JPanel jPanelButtons = new JPanel(new FlowLayout());
        jPanelButtons.add(getJButtonSave());
        jPanelButtons.add(getJButtonReset());
        jPanelButtons.add(getJButtonExport());
        jPanelButtons.add(getJButtonImport());
        jPanelButtons.add(getJButtonCancel());
        getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
    }
    
    /**
     * @return JButton save
     */
    protected JButton getJButtonSave() {
        if (jButtonSave == null) {
            jButtonSave = new JButton();
            jButtonSave.setName("jButtonSave");
            jButtonSave.setText("Save");
            jButtonSave.addActionListener(this);
        }
        return jButtonSave;
    }
    
    /**
     * @return JButton cancel
     */
    protected JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setName("jButtonCancel");
            jButtonCancel.setText("Cancel");
            jButtonCancel.addActionListener(this);
        }
        return jButtonCancel;
    }
    
    /**
     * @return JButton export
     */
    protected JButton getJButtonExport() {
        if (jButtonExport == null) {
            jButtonExport = new JButton();
            jButtonExport.setName("jButtonExport");
            jButtonExport.setText("Export preferences");
            jButtonExport.addActionListener(this);
        }
        return jButtonExport;
    }
    
    /**
     * @return JButton import
     */
    protected JButton getJButtonImport() {
        if (jButtonImport == null) {
            jButtonImport = new JButton();
            jButtonImport.setName("jButtonImport");
            jButtonImport.setText("Import preferences");
            jButtonImport.addActionListener(this);
        }
        return jButtonImport;
    }
    
    /**
     * @return JButton reset
     */
    protected JButton getJButtonReset() {
        if (jButtonReset == null) {
            jButtonReset = new JButton();
            jButtonReset.setName("jButtonReset");
            jButtonReset.setText("Reset user preferences");
            jButtonReset.addActionListener(this);
        }
        return jButtonReset;
    }
    
    /**
     * Invoked when an action occurs.
     *
     * @param e event information.
     */
    @Override public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        try {
            if (source.equals(this.jButtonCancel)) {
                actionPerformedCancel();
            } else if (source.equals(this.jButtonExport)) {
                actionPerformedExport();
            } else if (source.equals(this.jButtonImport)) {
                actionPerformedImport();
            } else if (source.equals(this.jButtonSave)) {
                actionPerformedSave();
            } else if (source.equals(this.jButtonReset)) {
                actionPerformedReset();
            }
        } catch (BackingStoreException | IOException | InvalidPreferencesFormatException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    /**
     * execute the Cancel action by doing an undo operation in the system and
     * user preferences
     */
    protected void actionPerformedCancel() throws BackingStoreException {
        new PreferencesTableModel(Preferences.systemRoot()).undo();
        new PreferencesTableModel(Preferences.userRoot()).undo();
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * execute the Save action by doing a sync in the system and user
     * preferences
     */
    protected void actionPerformedSave() throws BackingStoreException {
        new PreferencesTableModel(Preferences.systemRoot()).syncSave();
        new PreferencesTableModel(Preferences.userRoot()).syncSave();
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * execute the Cancel action
     */
    protected void actionPerformedExport() throws BackingStoreException, IOException {
        Preferences root = Preferences.userRoot();
        Preferences node = root.node(OPENMARKOV_NODE_PREFERENCES);
        System.out.println("Export selected");
        if (chooser.showSaveDialog(PreferencesDialog.this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (OutputStream out = new FileOutputStream(chooser.getSelectedFile())) {
            node.exportSubtree(out);
        }
    }
    
    /**
     * execute the Cancel action
     */
    protected void actionPerformedImport() throws IOException, InvalidPreferencesFormatException {
        System.out.println("Import selected");
        if (chooser.showOpenDialog(PreferencesDialog.this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (InputStream in = new FileInputStream(chooser.getSelectedFile())) {
            Preferences.importPreferences(in);
            this.invalidate();
            this.jTableEdition.repaint();
            this.jTreePreferences.repaint();
            this.repaint();
        }
    }
    
    /**
     * execute the Reset action by cleaning preferences in the user preferences
     */
    protected void actionPerformedReset() {
        LocalPreferences.getAllPreferences().forEach(LocalPreference::clear);
        LocalPreferences.getAllPreferences().forEach(LocalPreference::initialize);
        this.jTableEdition.repaint();
        this.jTreePreferences.repaint();
        this.repaint();
    }
    
    /**
     * set initial configuration for the chooser that shows ONLY .xml files
     */
    private void setChooser() {
        chooser.setCurrentDirectory(new File("."));
        // accept all files ending with .xml
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
            }
            
            @Override public String getDescription() {
                return "XML files";
            }
        });
    }
    
    /**
     * convenience internal class to handle Tree value changes
     *
     * @author jlgozalo
     * @version 1.0 13 Sep 2009
     */
    class PrefTreeSelectionListener implements TreeSelectionListener {
        @Override public void valueChanged(TreeSelectionEvent e) {
            try {
                PreferenceTreeNode node = (PreferenceTreeNode) e.getPath().getLastPathComponent();
                Preferences pref = node.getPrefObject();
                jTableEdition.setModel(new PreferencesTableModel(pref));
            } catch (ClassCastException | BackingStoreException ex) {
                throw new UnrecoverableException(ex);
            }
        }
    }
    
    /**
     * the package nodes in the Preferences
     */
    public static final String OPENMARKOV_NODE_PREFERENCES = "OPENMARKOV";
}
