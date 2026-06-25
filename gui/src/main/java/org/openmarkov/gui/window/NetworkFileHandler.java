/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.io.*;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Handles all file I/O operations: open, save, close, backup, evidence, and network creation.
 * Package-private — only accessed from {@link MainPanelListenerAssistant}.
 *
 * @author Manuel Arias
 */
class NetworkFileHandler {
    
    private final MainPanel mainPanel;
    private final List<NetworkEditorPanel> networkPanels;
    private final StringDatabase stringDatabase;
    
    NetworkFileHandler(MainPanel mainPanel, List<NetworkEditorPanel> networkPanels, StringDatabase stringDatabase) {
        this.mainPanel = mainPanel;
        this.networkPanels = networkPanels;
        this.stringDatabase = stringDatabase;
    }
    
    // ── Network creation ──────────────────────────────────────────
    
    void createNewNetwork() {
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
        networkPanels.add(createNewFrame(newNetwork));
        newNetwork.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
    }
    
    NetworkEditorPanel createNewFrame(ProbNet probNet) {
        NetworkEditorPanel networkPanel = new NetworkEditorPanel(probNet, mainPanel);
        probNet.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        mainPanel.addCloseableTab(probNet.getName(), networkPanel);
        mainPanel.getNetworksTabPanel().setSelectedComponent(networkPanel);
        networkPanel.setContextualMenuFactory(mainPanel.getContextualMenuFactory());
        networkPanel.getEditorPanel().getVisualNetwork().addSelectionListener(mainPanel.getMainPanelMenuAssistant());
        mainPanel.getMainPanelMenuAssistant().updateOptionsNewNetworkOpen();
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(networkPanel);
        mainPanel.getInferenceToolBar().setCurrentEvidenceCaseName(networkPanel.getCurrentCase());
        return networkPanel;
    }
    
    // ── Open ──────────────────────────────────────────────────────
    
    void openNetwork() throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        openNetwork("");
    }
    
    void openNetwork(String fileName) throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        if (fileName.isEmpty()) {
            fileName = requestNetworkFileToOpen();
        }
        if (fileName == null) return;
        System.out.println(stringDatabase.getString("LoadingNetwork.Text") + " " + fileName);
        ProbNetInfo probNetInfo = NetsIO.openNetworkFile(fileName);
        ProbNet netReadFromFile = probNetInfo.probNet();
        netReadFromFile.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        netReadFromFile.getPNESupport().setWithUndo(true);
        netReadFromFile.setName(new File(fileName).getName());
        var now = Instant.now();
        NetworkEditorPanel networkPanel = createNewFrame(netReadFromFile);
        System.out.println("Total: " + Duration.between(now, Instant.now()));
        networkPanel.setNetworkFile(fileName);
        networkPanel.setWriter(probNetInfo.writer());
        networkPanel.setReader(probNetInfo.reader());
        List<EvidenceCase> evidence = probNetInfo.evidence();
        if (evidence != null && !evidence.isEmpty()) {
            EvidenceCase preResolutionEvidence = evidence.getFirst();
            evidence.removeFirst();
            networkPanel.getEditorPanel().getEvidenceManager().setEvidence(preResolutionEvidence, evidence);
        }
        networkPanels.add(networkPanel);
        LastOpenFiles.setLastFileName(fileName);
        getDirectoryFileName(fileName);
        LocalPreferences.LATEST_OPEN_DIRECTORY.set(new File(fileName).getAbsoluteFile());
        System.out.println(stringDatabase.getString("NetworkLoaded.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        
        if (netReadFromFile.getShowCommentWhenOpening()) {
            showNetworkComment(netReadFromFile);
        }
    }
    
    void openNetwork(ProbNet probNet) {
        NetworkEditorPanel newNetworkEditorPanel = createNewFrame(probNet);
        networkPanels.add(newNetworkEditorPanel);
    }
    
    void openNetworkURL() throws NoReaderForFileException, ParserException, IOException, CorruptNetworkFile {
        URL url = requestURLFileToOpen();
        if (url == null) {
            return;
        }
        String urlFile = url.getFile();
        System.out.println(stringDatabase.getString("LoadingNetworkURL.Text") + " " + url);
        ProbNetInfo probNetInfo = NetsIO.openNetworkURL(url);
        ProbNet netReadFromURL = probNetInfo.probNet();
        netReadFromURL.getPNESupport().addListener(mainPanel.getMainPanelMenuAssistant());
        netReadFromURL.getPNESupport().setWithUndo(true);
        netReadFromURL.setName(new File(urlFile).getName());
        NetworkEditorPanel networkPanel = createNewFrame(netReadFromURL);
        networkPanel.setNetworkFile(urlFile);
        networkPanel.setWriter(probNetInfo.writer());
        networkPanel.setReader(probNetInfo.reader());
        List<EvidenceCase> evidence = probNetInfo.evidence();
        if (evidence != null && !evidence.isEmpty()) {
            EvidenceCase preResolutionEvidence = evidence.getFirst();
            evidence.removeFirst();
            networkPanel.getEditorPanel().getEvidenceManager().setEvidence(preResolutionEvidence, evidence);
        }
        networkPanels.add(networkPanel);
        LastOpenFiles.setLastFileName(urlFile);
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(true);
        System.out.println(stringDatabase.getString("NetworkLoaded.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        
        if (netReadFromURL.getShowCommentWhenOpening()) {
            showNetworkComment(netReadFromURL);
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
            createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
        }
        return (fileName != null && networkPanel.getWriter() != null)
                ? saveNetworkActions(networkPanel, fileName)
                : saveNetworkAs(networkPanel);
    }
    
    boolean saveNetworkAs(NetworkEditorPanel networkPanel) throws WriterException {
        ArrayList<Object> fileNameAndFormat = requestNetworkFileAndFormatToSave(networkPanel);
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
        return saveNetworkActions(networkPanel, fileName, fileFormat);
    }
    
    void saveOpenNetwork(NetworkEditorPanel networkPanel) throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile, WriterException {
        String fileName = networkPanel.getNetworkFile();
        if (fileName != null) {
            createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
        }
        saveNetwork(networkPanel);
        fileName = networkPanel.getNetworkFile();
        closeCurrentNetwork();
        openNetwork(fileName);
    }
    
    private boolean saveNetworkActions(NetworkEditorPanel networkPanel, String fileName, String fileFormat) throws WriterException {
        System.out.println(stringDatabase.getString("SavingNetwork.Text") + " " + fileName);
        NetsIO.saveNetworkFile(networkPanel, fileName);
        networkPanel.onSave();
        networkPanel.setNetworkFile(fileName);
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkSaved();
        LastOpenFiles.setLastFileName(fileName);
        LocalPreferences.LATEST_SAVED_DIRECTORY.set(new File(fileName).getAbsoluteFile());
        System.out.println(stringDatabase.getString("NetworkSaved.Text"));
        mainPanel.getMainMenu().rechargeFileMenu();
        return true;
    }
    
    private boolean saveNetworkActions(NetworkEditorPanel networkPanel, String fileName) throws WriterException {
        String fileFormat = LocalPreferences.LATEST_NETWORK_FORMAT.get();
        return saveNetworkActions(networkPanel, fileName, fileFormat);
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
            case NetworkEditorPanel networkPanel -> closeCurrentNetwork();
            case null -> {
            }
            default -> mainPanel.getNetworksTabPanel().remove(selectedComponent);
        }
    }
    
    private boolean closeCurrentNetwork() throws WriterException {
        return closeNetwork(getCurrentNetworkEditorPanel());
    }
    
    private boolean closeNetwork(NetworkEditorPanel currentNetworkEditorPanel) throws WriterException {
        if (currentNetworkEditorPanel == null) {
            return true;
        }
        boolean canClose = networkCanBeClosed(currentNetworkEditorPanel);
        if (canClose) {
            mainPanel.getNetworksTabPanel().remove(currentNetworkEditorPanel);
            if (networkPanels.isEmpty()) {
                mainPanel.setToolBarPanel(NetworkEditorPanel.WorkingMode.EDITION);
                mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
            }
        }
        return canClose;
    }
    
    boolean networkCanBeClosed(NetworkEditorPanel networkPanel) throws WriterException {
        int response;
        boolean canClose = !networkPanel.getModified();
        if (networkPanel.getModified()) {
            String title = StringDatabase.getUniqueInstance()
                                         .getFormattedString("NetworkNotSaved.Title", networkPanel.getProbNet()
                                                                                                  .getName());
            String message = StringDatabase.getUniqueInstance()
                                           .getFormattedString("NetworkNotSaved.Text", networkPanel.getProbNet()
                                                                                                   .getName());
            // Use the main JFrame as parent and force a top-level modal
            // dialog brought to front. The previous owner-from-mainPanel
            // could resolve to null during close cascades, leaving the
            // confirm dialog without a parent — on some window managers
            // it ended up behind the main window and the app appeared
            // frozen waiting for an invisible answer.
            JOptionPane pane = new JOptionPane(message,
                                               JOptionPane.WARNING_MESSAGE,
                                               JOptionPane.YES_NO_CANCEL_OPTION);
            JDialog dialog = pane.createDialog(MainGUI.INSTANCE, title);
            dialog.setAlwaysOnTop(true);
            MainGUI.INSTANCE.toFront();
            dialog.setVisible(true);
            dialog.dispose();
            Object value = pane.getValue();
            response = (value instanceof Integer iv) ? iv : JOptionPane.CLOSED_OPTION;
            canClose = switch (response) {
                case JOptionPane.YES_OPTION -> saveNetwork(networkPanel);
                case JOptionPane.NO_OPTION -> true;
                default -> false;
            };
        }
        if (canClose) {
            networkPanels.remove(networkPanel);
        }
        return canClose;
    }
    
    void closeApplication() throws WriterException {
        if (this.mainPanel.closeAllTabs()) {
            System.exit(0);
        }
    }
    
    // ── Evidence ──────────────────────────────────────────────────
    
    void loadEvidence(NetworkEditorPanel currentNetworkEditorPanel) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ParsingSourceException, IOException, EmptyDatabaseException, ConstraintViolatedException, CannotNormalizePotentialException {
        OMFileChooser evidenceOMFileChooser = new DBReaderOMFileChooser(false);
        evidenceOMFileChooser.setDialogTitle(stringDatabase.getString("LoadEvidence.Title"));
        String lastFileFilter = LocalPreferences.LATEST_LOADED_EVIDENCE_FORMAT.get();
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
            LocalPreferences.LATEST_LOADED_EVIDENCE_FORMAT.set(((FileFilterByExtension<?>) evidenceOMFileChooser.getFileFilter()).getExtensions()
                                                                                                                                 .getFirst());
            LocalPreferences.LATEST_OPEN_DIRECTORY.set(evidenceOMFileChooser.getSelectedFile());
        }
    }
    
    void saveEvidence(NetworkEditorPanel currentNetworkEditorPanel) {
        List<EvidenceCase> evidence = currentNetworkEditorPanel.getEditorPanel().getEvidenceManager().getEvidence();
        evidence.add(0, currentNetworkEditorPanel.getEditorPanel().getEvidenceManager().getPreResolutionEvidence());
        OMFileChooser omFileChooser = new OMFileChooser();
        File currentDirectory = LocalPreferences.LATEST_OPEN_DIRECTORY.get();
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
        fileChooser.setCurrentDirectory(LocalPreferences.LATEST_SAVED_DIRECTORY.get());
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
            String chosenFilterExtension = ((FileFilterByExtension<?>) fileChooser.getFileFilter()).getExtensions()
                                                                                                   .getFirst();
            if (!filename.toLowerCase().endsWith("." + chosenFilterExtension.toLowerCase())) {
                filename += "." + chosenFilterExtension.toLowerCase();
                File selectedFile = new File(filename);
                if (selectedFile.exists()) {
                    int response = JOptionPane.showConfirmDialog(
                            mainPanel.getNetworksTabPanel().getSelectedComponent(),
                            "The file " + selectedFile.getName()
                                    + " already exists. The file will be renamed to " + selectedFile.getName() + " (1)." + chosenFilterExtension.toLowerCase(),
                            "Network renamed",
                            JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
                    filename = fileChooser.getSelectedFile()
                                          .getAbsolutePath() + " (1)." + chosenFilterExtension.toLowerCase();
                }
            }
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
