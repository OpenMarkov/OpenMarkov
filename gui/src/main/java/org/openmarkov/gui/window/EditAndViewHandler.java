/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.gui.dialog.AboutBox;
import org.openmarkov.gui.dialog.LanguageDialog;
import org.openmarkov.gui.dialog.ShortcutsBox;
import org.openmarkov.gui.dialog.configuration.PreferencesDialog;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.prefs.BackingStoreException;

/**
 * Handles edit operations (undo/redo, edition mode), view operations (zoom, by-title),
 * and dialog launchers (configuration, shortcuts, about, language).
 * Package-private — only accessed from {@link MainPanelListenerAssistant}.
 *
 * @author Manuel Arias
 */
class EditAndViewHandler {

    private static final double ZOOM_CHANGE_VALUE = 0.2;

    private final MainPanel mainPanel;

    EditAndViewHandler(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    // ── Undo / Redo ───────────────────────────────────────────────

    void undo() {
        try {
            undoRedo(true);
        } catch (CannotUndoException e) {
            throw new UnrecoverableException(e);
        }
    }

    void redo() {
        try {
            undoRedo(false);
        } catch (CannotRedoException e) {
            throw new UnrecoverableException(e);
        }
    }

    private void undoRedo(boolean undoOperation) throws CannotUndoException, CannotRedoException {
        NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
        networkPanel.getEditorPanel().getVisualNetwork().setSelectedAllObjects(false);
        if (undoOperation) {
            networkPanel.getEditorPanel().getVisualNetwork().getProbNet().getPNESupport().undo();
        } else {
            networkPanel.getEditorPanel().getVisualNetwork().getProbNet().getPNESupport().redo();
        }
    }

    // ── Edition mode ──────────────────────────────────────────────

    void activateEditionMode(String newEditionMode) {
        NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
        networkPanel.setEditionMode(newEditionMode);
        mainPanel.getMainPanelMenuAssistant().setEditionOption(newEditionMode, networkPanel.isThereDataStored());
    }

    // ── View: by title / by name ──────────────────────────────────

    void activateByTitle(boolean byTitle) {
        NetworkEditorPanel actualNetwork = getCurrentNetworkEditorPanel();
        if (actualNetwork.getByTitle() != byTitle) {
            actualNetwork.setByTitle(byTitle);
            mainPanel.getMainPanelMenuAssistant().setByTitle(byTitle);
        }
    }

    // ── Zoom ──────────────────────────────────────────────────────

    void incrementZoom(EditorPanel panel) {
        setZoom(panel, panel.getZoom() + ZOOM_CHANGE_VALUE);
    }

    void decrementZoom(EditorPanel panel) {
        setZoom(panel, panel.getZoom() - ZOOM_CHANGE_VALUE);
    }

    void setZoom(EditorPanel panel, double value) {
        panel.setZoom(value);
        double newZoom = panel.getZoom();
        mainPanel.getMainPanelMenuAssistant().setZoom(newZoom);
    }

    // ── Dialog launchers ──────────────────────────────────────────

    void showLanguageChangeDialog() {
        LanguageDialog.getUniqueInstance(mainPanel.getMainFrame()).setVisible(true);
    }

    void showUserConfigurationDialog() throws BackingStoreException {
        new PreferencesDialog(mainPanel.getMainFrame()).setVisible(true);
    }

    ShortcutsBox showShortcuts() {
        return new ShortcutsBox(mainPanel.getMainFrame());
    }

    AboutBox showAbout() {
        return new AboutBox(mainPanel.getMainFrame());
    }

    // ── Helpers ───────────────────────────────────────────────────

    private NetworkEditorPanel getCurrentNetworkEditorPanel() {
        return mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel();
    }
}
