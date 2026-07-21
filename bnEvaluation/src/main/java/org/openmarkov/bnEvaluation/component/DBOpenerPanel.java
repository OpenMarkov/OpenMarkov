package org.openmarkov.bnEvaluation.component;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.gui.dialog.io.DBReaderOMFileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Reusable Swing panel that lets the user open a case database file.
 * Notifies registered listeners when a database is successfully loaded.
 */
public class DBOpenerPanel extends JPanel {
    
    private @Nullable CaseDatabase database;
    private @Nullable File databaseFile;
    
    
    private final Component parent;
    
    private final CaseDatabaseManager caseDbManager;
    
    private final DBReaderOMFileChooser databaseFileChooser;
    private final ArrayList<OnOpenDb> onOpen;
    
    
    private JTextPane caseFileTextPane;
    
    
    public DBOpenerPanel(Component parent) {
        super();
        this.parent = parent;
        this.caseDbManager = new CaseDatabaseManager();
        this.databaseFileChooser = new DBReaderOMFileChooser(false);
        this.onOpen = new ArrayList<>();
        
        this.setBorder(BorderFactory.createTitledBorder("Dataset"));
        this.setPreferredSize(new Dimension(700, 60));
        
        // create components
        JLabel caseFileLabel = new JLabel();
        caseFileTextPane = new JTextPane();
        JScrollPane jScrollCaseFileTextPane = new JScrollPane();
        JButton loadCaseFileButton = new JButton();
        
        // component properties
        caseFileLabel.setText("Dataset");
        caseFileTextPane.setEditable(false);
        caseFileTextPane.setEnabled(false);
        jScrollCaseFileTextPane.setViewportView(caseFileTextPane);
        loadCaseFileButton.setText("Open");
        loadCaseFileButton.addActionListener(e -> {
            try {
                open();
            } catch (NoWriterForExtensionException | ParsingSourceException | IOException | EmptyDatabaseException ex) {
                throw new UnrecoverableException(ex);
            }
        });
        
        // add components to caseDatabasePanel with a GroupLayout
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                                        .addGap(10)
                                        .addComponent(jScrollCaseFileTextPane)
                                        .addGap(10)
                                        .addComponent(loadCaseFileButton, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
                                        .addGap(10));
        layout.setVerticalGroup(layout.createParallelGroup()
                                      .addGap(5)
                                      .addComponent(jScrollCaseFileTextPane, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                                      .addGap(5)
                                      .addComponent(loadCaseFileButton)
                                      .addGap(5));
    }
    
    
    private boolean open() throws NoWriterForExtensionException, ParsingSourceException, IOException, EmptyDatabaseException {
        this.databaseFileChooser.setDialogTitle("Open dataset");
        if (this.databaseFileChooser.showOpenDialog(this.parent) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        CaseDatabaseReader reader = this.caseDbManager.getReader(FilenameUtils.getExtension(this.databaseFileChooser.getSelectedFile()
                                                                                                                    .getName()));
        this.database = reader.load(this.databaseFileChooser.getSelectedFile());
        this.databaseFile = this.databaseFileChooser.getSelectedFile();
        caseFileTextPane.setText(this.databaseFile.getName());
        onOpen.forEach(db -> db.onOpen(this.databaseFile, this.database));
        return true;
    }
    
    /**
     * Registers a callback to be invoked when a database is opened.
     *
     * @param onOpenDb the callback to register
     */
    public void onOpen(OnOpenDb onOpenDb) {
        this.onOpen.add(onOpenDb);
    }
    
    /** Callback invoked when a case database file is successfully opened. */
    @FunctionalInterface
    public interface OnOpenDb {
        public void onOpen(File databaseFile, CaseDatabase database);
    }
    
    /** Clears the currently loaded database and resets the display. */
    public void removeCurrentDB() {
        this.database = null;
        this.databaseFile = null;
        caseFileTextPane.setText("");
    }
    
    public @Nullable CaseDatabase getDatabase() {
        return this.database;
    }
    
    public @Nullable File getDatabaseFile() {
        return this.databaseFile;
    }
}
