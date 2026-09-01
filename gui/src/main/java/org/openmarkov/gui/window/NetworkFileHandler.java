/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.StartupAction;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.gui.dialog.common.CommonOptions;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.common.OptionDialog;
import org.openmarkov.gui.dialog.io.DBReaderOMFileChooser;
import org.openmarkov.gui.dialog.io.FileFilterByExtension;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.dialog.io.NetworkOMFileChooser;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.gui.dialog.io.URLNetworkChooserDialog;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O operations: open, save, close, backup, evidence, and network creation.
 * Package-private — only accessed from {@link MainPanelListenerAssistant}.
 *
 * @author Manuel Arias
 */
public class NetworkFileHandler {
    
    private final MainPanel mainPanel;
    private final List<NetworkEditorPanel> networkPanels;
    private final StringDatabase stringDatabase;
    
    NetworkFileHandler(MainPanel mainPanel, List<NetworkEditorPanel> networkPanels, StringDatabase stringDatabase) {
        this.mainPanel = mainPanel;
        this.networkPanels = networkPanels;
        this.stringDatabase = stringDatabase;
    }
    
    // ── Network creation ──────────────────────────────────────────
    
    public void createNewNetwork() {
        ProbNet newNetwork = new ProbNet();
        newNetwork.setName("New network");
        NetworkPropertiesDialog dialogProperties = new NetworkPropertiesDialog(GUIUtils.getOwner(mainPanel), newNetwork, false);
        if (dialogProperties.showProperties() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        newNetwork = dialogProperties.getProbNet();
        
        if (!newNetwork.hasConstraintOfClass(OnlyChanceNodes.class) && (
                newNetwork.getDecisionCriteria() == null || newNetwork.getDecisionCriteria().isEmpty()
        )) {
            List<Criterion> criteria = new ArrayList<>();
            criteria.add(new Criterion());
            newNetwork.setDecisionCriteria(criteria);
        }
        String networkName = stringDatabase.getString("InternalFrame.Title");
        newNetwork.setName(networkName);
        newNetwork.getPNESupport().setWithUndo(true);
        newNetwork.getPNESupport().removeDoneEdits();
        networkPanels.add(this.createNewFrame(newNetwork, null));
        newNetwork.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
    }
    
    NetworkEditorPanel createNewFrame(ProbNet probNet, String networkFile) {
        NetworkEditorPanel networkPanel = new NetworkEditorPanel(probNet, mainPanel);
        if (networkFile != null) {
            networkPanel.setNetworkFile(networkFile);
        }
        probNet.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        mainPanel.addCloseableTab(probNet.getName(), networkPanel);
        mainPanel.getNetworksTabPanel().setSelectedComponent(networkPanel);
        networkPanel.setContextualMenuFactory(mainPanel.getContextualMenuFactory());
        networkPanel.getEditorPanel().getVisualNetwork().addSelectionListener(mainPanel.getMainPanelMenuAssistant());
        mainPanel.getMainPanelMenuAssistant().updateOptionsNewNetworkOpen();
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(networkPanel);
        mainPanel.getInferenceToolBar().setCurrentEvidenceCaseName(networkPanel.getCurrentCase());
        networkPanel.requestFocusInWindow();
        return networkPanel;
    }
    
    // ── Open ──────────────────────────────────────────────────────
    
    void openNetwork() throws ProbNetParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        this.openNetwork((String) null);
    }
    
    void openNetwork(ProbNet probNet) {
        NetworkEditorPanel newNetworkEditorPanel = this.createNewFrame(probNet, null);
        networkPanels.add(newNetworkEditorPanel);
    }
    
    void openNetwork(@Nullable String fileName) throws ProbNetParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        openNetworkSource(new NetworkSource.SourceFile(fileName == null || fileName.isBlank() ? null : new File(fileName)));
    }
    
    void openNetworkURL() throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        openNetworkSource(new NetworkSource.SourceURL(null));
    }
    
    public sealed interface NetworkSource {
        public record SourceFile(@Nullable File file) implements NetworkSource {
        }
        
        public record SourceURL(@Nullable URL url) implements NetworkSource {
        }
    }
    
    void openNetworkSource(NetworkSource source) throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        source = switch (source) {
            case NetworkSource.SourceFile sourceFile -> {
                if (sourceFile.file != null) {
                    yield source;
                }
                yield new NetworkSource.SourceFile(new File(this.requestNetworkFileToOpen()));
            }
            case NetworkSource.SourceURL sourceURL -> {
                if (sourceURL.url != null) {
                    yield sourceURL;
                }
                yield new NetworkSource.SourceURL(this.requestURLFileToOpen());
            }
        };
        if (null == switch (source) {
            case NetworkSource.SourceFile sourceFile -> sourceFile.file;
            case NetworkSource.SourceURL sourceURL -> sourceURL.url;
        }) {
            return;
        }
        ProbNetInfo probNetInfo = switch (source) {
            case NetworkSource.SourceFile sourceFile -> NetsIO.openNetworkFile(sourceFile.file.getAbsolutePath());
            case NetworkSource.SourceURL sourceURL -> NetsIO.openNetworkURL(sourceURL.url);
        };
        ProbNet netReadFrom = probNetInfo.probNet();
        netReadFrom.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        netReadFrom.getPNESupport().setWithUndo(true);
        netReadFrom.setName((switch (source) {
            case NetworkSource.SourceFile sourceFile -> sourceFile.file;
            case NetworkSource.SourceURL sourceURL -> new File(sourceURL.url.getFile());
        }).getName());
        String networkFile = switch (source) {
            case NetworkSource.SourceFile sourceFile -> sourceFile.file.getAbsolutePath();
            case NetworkSource.SourceURL sourceURL -> sourceURL.url.getFile();
        };
        var networksMatchingThisOne = mainPanel.getNetworkEditors()
                                               .stream()
                                               .filter(editor -> editor.getNetworkFile().equals(networkFile))
                                               .toList();
        if (!networksMatchingThisOne.isEmpty()) {
            OptionDialog<CommonOptions.YesNo> dialog = new OptionDialog<>(MainGUI.INSTANCE,
                                                                          "Network already opened",
                                                                          "The network " + networkFile + " is already open, do you want to reload it?",
                                                                          CommonOptions.YesNo.class);
            if (dialog.request(CommonOptions.YesNo.NO) != CommonOptions.YesNo.YES) {
                return;
            }
        }
        networksMatchingThisOne.forEach(networkEditor -> mainPanel.getNetworksTabPanel().remove(networkEditor));
        NetworkEditorPanel networkPanel = this.createNewFrame(netReadFrom, networkFile);
        networkPanel.setWriter(probNetInfo.writer());
        networkPanel.setReader(probNetInfo.reader());
        List<EvidenceCase> evidence = probNetInfo.evidence();
        if (evidence != null && !evidence.isEmpty()) {
            EvidenceCase preResolutionEvidence = evidence.getFirst();
            evidence.removeFirst();
            networkPanel.getEditorPanel().getEvidenceManager().setEvidence(preResolutionEvidence, evidence);
        }
        networkPanels.add(networkPanel);
        switch (source) {
            case NetworkSource.SourceFile sourceFile -> {
                LastOpenFiles.setLastFileName(sourceFile.file.getAbsolutePath());
                getDirectoryFileName(sourceFile.file.getAbsolutePath());
                UserPreferences.LATEST_OPEN_DIRECTORY.set(sourceFile.file.getAbsoluteFile());
            }
            case NetworkSource.SourceURL sourceURL -> {
                //LastOpenFiles.setLastFileName(sourceURL.url.getFile());
                mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(true);
            }
        }
        System.out.println(stringDatabase.getString("NetworkLoaded.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        if (netReadFrom.getShowCommentWhenOpening()) {
            this.showNetworkComment(netReadFrom);
        }
    }
    
    private void showNetworkComment(ProbNet probNet) {
        CommentHTMLScrollPane commentPane = new CommentHTMLScrollPane();
        commentPane.setEditable(false);
        commentPane.setCommentHTMLTextPaneText(probNet.getComment());
        commentPane.setPreferredSize(new Dimension(500, 300));
        JOptionPane networkMessagePane = new JOptionPane(commentPane, JOptionPane.INFORMATION_MESSAGE);
        JDialog networkMessageDialog = networkMessagePane.createDialog(GUIUtils.getOwner(mainPanel),
                                                                       stringDatabase.getString("NetworkCommentWindow.Title"));
        networkMessageDialog.setResizable(true);
        networkMessageDialog.setMinimumSize(new Dimension(500, 300));
        networkMessageDialog.setVisible(true);
    }
    
    // ── Save ──────────────────────────────────────────────────────
    
    boolean saveNetwork(NetworkEditorPanel networkPanel) throws WriterException {
        String fileName = networkPanel.getNetworkFile();
        if (fileName != null) {
            this.createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
        }
        return (fileName != null && networkPanel.getWriter() != null)
                ? this.saveNetworkActions(networkPanel, fileName)
                : this.saveNetworkAs(networkPanel);
    }
    
    boolean saveNetworkAs(NetworkEditorPanel networkPanel) throws WriterException {
        ArrayList<Object> fileNameAndFormat = this.requestNetworkFileAndFormatToSave(networkPanel);
        String fileName = (String) fileNameAndFormat.get(0);
        if (fileName == null) {
            return false;
        }
        String fileFormat = (String) fileNameAndFormat.get(1);
        networkPanel.setNetworkFile(fileName);
        networkPanel.getProbNet().setName(new File(fileName).getName());
        var formatInfo = FormatManager.info((Class<?>) fileNameAndFormat.get(2));
        networkPanel.setWriter(
                FormatManager.writersInstances()
                             .filter(probNetWriter -> FormatManager.formatEquals(formatInfo, FormatManager.info(probNetWriter)))
                             .findFirst()
                             .orElse(null));
        networkPanel.setReader(
                FormatManager.readersInstances()
                             .filter(probNetReader -> FormatManager.formatEquals(formatInfo, FormatManager.info(probNetReader)))
                             .findFirst()
                             .orElse(null));
        return this.saveNetworkActions(networkPanel, fileName, fileFormat);
    }
    
    void saveOpenNetwork(NetworkEditorPanel networkPanel) throws ProbNetParserException, IOException, NoReaderForFileException, CorruptNetworkFile, WriterException {
        String fileName = networkPanel.getNetworkFile();
        if (fileName != null) {
            this.createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
        }
        this.saveNetwork(networkPanel);
        fileName = networkPanel.getNetworkFile();
        this.closeCurrentNetwork();
        this.openNetwork(fileName);
    }
    
    private boolean saveNetworkActions(NetworkEditorPanel networkPanel, String fileName, String fileFormat) throws WriterException {
        System.out.println(stringDatabase.getString("SavingNetwork.Text") + " " + fileName);
        NetsIO.saveNetworkFile(networkPanel, fileName);
        networkPanel.onSave();
        networkPanel.setNetworkFile(fileName);
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkSaved();
        LastOpenFiles.setLastFileName(fileName);
        UserPreferences.LATEST_SAVED_DIRECTORY.set(new File(fileName).getAbsoluteFile());
        System.out.println(stringDatabase.getString("NetworkSaved.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        return true;
    }
    
    private boolean saveNetworkActions(NetworkEditorPanel networkPanel, String fileName) throws WriterException {
        String fileFormat = UserPreferences.LATEST_NETWORK_FORMAT.get();
        return this.saveNetworkActions(networkPanel, fileName, fileFormat);
    }
    
    private void createBackUpNetworkFile(String fileName, String newFileName) {
        File inFile = new File(fileName);
        File outFile = new File(newFileName);
        try (
                FileInputStream in = new FileInputStream(inFile);
                FileOutputStream out = new FileOutputStream(outFile);
        ) {
            while (true) {
                int c = in.read();
                if (c == -1) break;
                out.write(c);
            }
        } catch (IOException e) {
            System.out.println(stringDatabase.getString("NetworkBackupError.Text"));
        }
        System.out.println(stringDatabase.getString("NetworkBackup.Text"));
    }
    
    private static String toBakExtension(String nameFile) {
        String newName;
        int index = nameFile.lastIndexOf('.');
        if (index > 0) {
            newName = nameFile.substring(0, index);
        } else
            newName = nameFile;
        return newName + ".bak";
    }
    
    // ── Close ─────────────────────────────────────────────────────
    
    void closeCurrentTab() throws WriterException {
        var selectedComponent = mainPanel.getNetworksTabPanel().getSelectedComponent();
        switch (selectedComponent) {
            case NetworkEditorPanel networkPanel -> this.closeCurrentNetwork();
            case null -> {
            }
            default -> mainPanel.getNetworksTabPanel().remove(selectedComponent);
        }
    }
    
    private boolean closeCurrentNetwork() throws WriterException {
        return this.closeNetwork(this.getCurrentNetworkEditorPanel());
    }
    
    private boolean closeNetwork(NetworkEditorPanel currentNetworkEditorPanel) throws WriterException {
        if (currentNetworkEditorPanel == null) {
            return true;
        }
        boolean canClose = this.networkCanBeClosed(currentNetworkEditorPanel);
        if (canClose) {
            mainPanel.getNetworksTabPanel().remove(currentNetworkEditorPanel);
            if (networkPanels.isEmpty()) {
                mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
                
                mainPanel.updateFor(mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel());
            }
        }
        return canClose;
    }
    
    boolean networkCanBeClosed(NetworkEditorPanel networkPanel) throws WriterException {
        boolean canClose = !networkPanel.getModified();
        if (networkPanel.getModified()) {
            String title = StringDatabase.getUniqueInstance()
                                         .getFormattedString("NetworkNotSaved.Title", networkPanel.getProbNet()
                                                                                                  .getName());
            String message = StringDatabase.getUniqueInstance()
                                           .getFormattedString("NetworkNotSaved.Text", networkPanel.getProbNet()
                                                                                                   .getName());
            
            OptionDialog<CommonOptions.YesNoCancel> dialog = new OptionDialog<>(MainGUI.INSTANCE, title, message, CommonOptions.YesNoCancel.class);
            canClose = switch (dialog.request(CommonOptions.YesNoCancel.CANCEL)) {
                case CommonOptions.YesNoCancel.YES -> this.saveNetwork(networkPanel);
                case CommonOptions.YesNoCancel.NO -> true;
                case CommonOptions.YesNoCancel.CANCEL -> false;
            };
        }
        if (canClose) {
            networkPanels.remove(networkPanel);
        }
        return canClose;
    }
    
    void closeApplication() throws WriterException {
        var editors = this.mainPanel.getNetworkEditors();
        if (this.mainPanel.closeAllTabs()) {
            if (UserPreferences.STARTUP_ACTIONS.get().contains(StartupAction.RESTORE_LAST_SESSION)) {
                UserPreferences.LAST_SESSION_NETWORK_FILES.set(new ArrayList<>(editors.stream()
                                                                                      .map(NetworkEditorPanel::getNetworkFile)
                                                                                      .toList()));
            }
            System.exit(0);
        }
    }
    
    // ── Evidence ──────────────────────────────────────────────────
    
    void loadEvidence(NetworkEditorPanel currentNetworkEditorPanel) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ParsingSourceException, IOException, EmptyDatabaseException, ConstraintViolatedException, CannotNormalizePotentialException, ThereIsNoPotentialsInNodeException {
        OMFileChooser evidenceOMFileChooser = new DBReaderOMFileChooser(false);
        evidenceOMFileChooser.setDialogTitle(stringDatabase.getString("LoadEvidence.Title"));
        String lastFileFilter = UserPreferences.LATEST_LOADED_EVIDENCE_FORMAT.get();
        evidenceOMFileChooser.setFileFilter(lastFileFilter);
        if ((evidenceOMFileChooser.showOpenDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION)) {
            System.out.println("Load evidence file " + evidenceOMFileChooser.getSelectedFile().getAbsolutePath());
            CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
            CaseDatabaseReader caseDbReader;
            try {
                caseDbReader = caseDbManager
                        .getReader(FilenameUtils.getExtension(evidenceOMFileChooser.getSelectedFile().getName()));
            } catch (NoWriterForExtensionException e) {
                throw new UnrecoverableException(e);
            }
            ProbNet currentNet = currentNetworkEditorPanel.getProbNet();
            CaseDatabase caseDatabase = caseDbReader.load(evidenceOMFileChooser.getSelectedFile());
            List<Variable> variables = caseDatabase.getVariables();
            int[][] cases = caseDatabase.getCases();
            for (int i = 0; i < cases.length; ++i) {
                EvidenceCase newEvidenceCase = new EvidenceCase();
                for (int j = 0; j < cases[i].length; ++j) {
                    if (!variables.get(j).getStateName(cases[i][j]).isEmpty()
                            && !variables.get(j).getStateName(cases[i][j]).equals("?")) {
                        Variable variable = currentNet.getVariable(variables.get(j).getName());
                        int stateIndex = variable.getStateIndex(variables.get(j)
                                                                         .getStateName(cases[i][j]));
                        if (stateIndex == -1) continue;
                        newEvidenceCase.addFinding(new Finding(variable, stateIndex));
                    }
                }
                currentNetworkEditorPanel.getEditorPanel().getEvidenceManager().addNewEvidenceCase(newEvidenceCase);
            }
            UserPreferences.LATEST_LOADED_EVIDENCE_FORMAT.set(((FileFilterByExtension<?>) evidenceOMFileChooser.getFileFilter()).getExtensions()
                                                                                                                                .getFirst());
            UserPreferences.LATEST_OPEN_DIRECTORY.set(evidenceOMFileChooser.getSelectedFile());
        }
    }
    
    void saveEvidence(NetworkEditorPanel currentNetworkEditorPanel) {
        List<EvidenceCase> evidence = currentNetworkEditorPanel.getEditorPanel().getEvidenceManager().getEvidence();
        evidence.add(0, currentNetworkEditorPanel.getEditorPanel().getEvidenceManager().getPreResolutionEvidence());
        OMFileChooser omFileChooser = new OMFileChooser();
        File currentDirectory = UserPreferences.LATEST_OPEN_DIRECTORY.get();
        omFileChooser.setCurrentDirectory(currentDirectory);
        String suggestedFileName = currentNetworkEditorPanel.getProbNet().getName();
        omFileChooser.setSelectedFile(new File(suggestedFileName));
        omFileChooser.setAcceptAllFileFilterUsed(false);
        if (omFileChooser.showSaveDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            System.out.println("Save evidence file " + omFileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    // ── File chooser dialogs ──────────────────────────────────────
    
    private String requestNetworkFileToOpen() {
        NetworkOMFileChooser fileChooser = new NetworkOMFileChooser();
        fileChooser.setDialogTitle(stringDatabase.getString("OpenNetwork.Title"));
        String fileName = null;
        if (fileChooser.showOpenDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            fileName = fileChooser.getSelectedFile().getAbsolutePath();
        }
        return fileName;
    }
    
    private URL requestURLFileToOpen() {
        URLNetworkChooserDialog urlNetworkChooserDialog = new URLNetworkChooserDialog(GUIUtils.getOwner(mainPanel));
        if (urlNetworkChooserDialog.requestNetworkURL() == OkCancelDialog.ChosenOption.Ok) {
            return urlNetworkChooserDialog.getNetworkURL();
        }
        return null;
    }
    
    private ArrayList<Object> requestNetworkFileAndFormatToSave(NetworkEditorPanel networkPanel) {
        String fileName = networkPanel.getNetworkFile();
        String suggestedFileName = (fileName != null) ? fileName : new File(networkPanel.getProbNet()
                                                                                        .getName()).getName();
        NetworkOMFileChooser fileChooser = new NetworkOMFileChooser(false, false);
        String title = stringDatabase.getString("SaveNetwork.Title");
        fileChooser.setDialogTitle(title);
        fileChooser.setCurrentDirectory(UserPreferences.LATEST_SAVED_DIRECTORY.get());
        fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), new File(suggestedFileName).getName()));
        if (networkPanel.getWriter() != null) {
            for (var filter : fileChooser.getChoosableFileFilters()) {
                if (filter instanceof FileFilterByExtension<?> fileFilterByExtension) {
                    if (fileFilterByExtension.getFormatInfo() instanceof Class<?> formatClass && formatClass == networkPanel.getWriter()
                                                                                                                            .getClass()) {
                        fileChooser.setFileFilter(fileFilterByExtension);
                        break;
                    }
                    ;
                }
            }
        }
        ArrayList<Object> fileNameAndFormat = new ArrayList<>();
        String filename = null;
        FileFilterByExtension<?> fileFormat = null;
        if (fileChooser.showSaveDialog(GUIUtils.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
            filename = fileChooser.getSelectedFile().getAbsolutePath();
            fileFormat = (FileFilterByExtension<?>) fileChooser.getFileFilter();
        }
        fileNameAndFormat.add(filename);
        fileNameAndFormat.add(fileFormat == null ? null : fileFormat.getFileDescription());
        fileNameAndFormat.add(fileFormat == null ? null : fileFormat.getFormatInfo());
        return fileNameAndFormat;
    }
    
    // ── Helpers ───────────────────────────────────────────────────
    
    private NetworkEditorPanel getCurrentNetworkEditorPanel() {
        return mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel();
    }
    
    List<NetworkEditorPanel> getNetworkEditorPanels() {
        return networkPanels;
    }
    
    private static String getDirectoryFileName(String fileName) {
        return (new File(fileName)).getAbsolutePath();
    }
}
