/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.component.LastRecentFilesMenuItem;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.gui.license.License;
import org.openmarkov.gui.license.LicenseHolder;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.localize.LocalizedCheckBoxMenuItem;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.localize.MenuLocalizer;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasicImpl;
import org.openmarkov.gui.productTour.tour.TourManager;
import org.openmarkov.gui.productTour.tour.action.UserActionRequester;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.toolplugin.ToolPluginManager;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main menu bar of the OpenMarkov application.
 * <p>
 * Menu items are created via factory methods ({@link #createItem}, {@link #createCheckBox})
 * and stored in an {@link EnumMap} keyed by {@link ActionCommands}. This replaces
 * the former pattern of 53 individual fields + 46 lazy-getter methods.
 */
public class MainMenu extends JMenuBar implements MenuToolBarBasic {

    private static final long serialVersionUID = 8267763502728836096L;
    private static final double UI_SCALE_MAX = 5.0;
    private static final double UI_SCALE_MIN = 0.5;

    private final Map<ActionCommands, JComponent> items = new EnumMap<>(ActionCommands.class);
    final HashMap<JComponent, String> defaultText = new HashMap<>();

    private final MainPanel mainPanel;
    private final ActionListener listener;

    private final ButtonGroup groupEditOptions = new ButtonGroup();
    private final ButtonGroup groupByNameByTitle = new ButtonGroup();

    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu inferenceMenu;
    private @Nullable JMenu toolsMenu;
    private JMenu helpMenu;
    private JMenu viewNodesMenu;

    public MainMenu(MainPanel mainPanel, ActionListener newListener) {
        this.mainPanel = mainPanel;
        this.listener = newListener;
        createAllItems();
        reInitialize();
    }

    // ── Public API ──────────────────────────────────────────────────

    public void reInitialize() {
        this.toolsMenu = null;
        removeAll();
        add(buildFileMenu());
        add(buildEditMenu());
        if (MainPanel.getCurrentNetworkEditorPanel() != null && MainPanel.getCurrentNetworkEditorPanel()
                                                                         .getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
            add(buildInferenceMenu());
        }
        add(buildViewMenu());
        add(buildToolsMenu());
        add(buildHelpMenu());
    }

    public void rechargeFileMenu() {
        this.fileMenu.removeAll();
        this.fileMenu.add(this.items.get(ActionCommands.NEW_NETWORK));
        this.fileMenu.add(this.items.get(ActionCommands.OPEN_NETWORK));

        Stream<? extends @NotNull Component> recentNetworkItems;
        if (LastOpenFiles.existLastOpenFiles()) {
            recentNetworkItems = getLastOpenFiles().stream();
        } else {
            JLabel noRecentItems = new JLabel(StringDatabase.INSTANCE.getString("File.OpenRecent.NoRecent"));
            noRecentItems.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            recentNetworkItems = Stream.of(noRecentItems);
        }
        
        this.fileMenu.add(new JMenuItemBuilder(StringDatabase.INSTANCE.getString("File.OpenRecent")).withItems(
                recentNetworkItems
        ).build());
        
        this.fileMenu.add(this.items.get(ActionCommands.OPEN_NETWORK_URL));
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.items.get(ActionCommands.SAVE_NETWORK));
        this.fileMenu.add(this.items.get(ActionCommands.SAVE_OPEN_NETWORK));
        this.fileMenu.add(this.items.get(ActionCommands.SAVEAS_NETWORK));
        this.fileMenu.add(this.items.get(ActionCommands.CLOSE_TAB));
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.items.get(ActionCommands.NETWORK_PROPERTIES));
        this.fileMenu.add(this.items.get(ActionCommands.LOAD_EVIDENCE));
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.items.get(ActionCommands.EXIT_APPLICATION));
        this.fileMenu.repaint();
    }

    public void addPropagateNowItem() {
        if (this.inferenceMenu != null) rebuildInferenceMenu(true);
    }

    public void removePropagateNowItem() {
        if (this.inferenceMenu != null) rebuildInferenceMenu(false);
    }

    public JMenuItem getSwitchWorkingMode() {
        return (JMenuItem) this.items.get(ActionCommands.CHANGE_TO_EDITION_MODE);
    }
    
    @Override
    public void setOptionEnabled(String actionCommand, boolean b) {
        MenuToolBarBasicImpl.setOptionEnabled(getJComponentActionCommand(actionCommand), b);
    }
    
    @Override
    public void setOptionSelected(String actionCommand, boolean b) {
        MenuToolBarBasicImpl.setOptionSelected(getJComponentActionCommand(actionCommand), b);
    }
    
    @Override
    public void addOptionText(String actionCommand, String text) {
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.addOptionText(component, this.defaultText.get(component), text);
    }
    
    @Override
    public void setText(String actionCommand, String text) {
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.setText(component, text);
    }

    // ── Item creation (called once) ────────────────────────────────

    private void createAllItems() {
        // File
        createItem(MenuItemNames.FILE_NEW_MENUITEM, ActionCommands.NEW_NETWORK, IconBind.NEW_ENABLED, MainMenu.ctrl(KeyEvent.VK_N));
        createItem(MenuItemNames.FILE_OPEN_MENUITEM, ActionCommands.OPEN_NETWORK, IconBind.OPEN_ENABLED, MainMenu.ctrl(KeyEvent.VK_O));
        createItem(MenuItemNames.FILE_OPEN_URL_MENUITEM, ActionCommands.OPEN_NETWORK_URL, IconBind.OPEN_URL_ENABLED, MainMenu.ctrlAlt(KeyEvent.VK_O));
        createItem(MenuItemNames.FILE_SAVE_MENUITEM, ActionCommands.SAVE_NETWORK, IconBind.SAVE_ENABLED, MainMenu.ctrl(KeyEvent.VK_S));
        createItem(MenuItemNames.FILE_SAVE_OPEN_MENUITEM, ActionCommands.SAVE_OPEN_NETWORK, IconBind.SAVE_ENABLED, MainMenu.ctrlAlt(KeyEvent.VK_S));
        createItem(MenuItemNames.FILE_SAVEAS_MENUITEM, ActionCommands.SAVEAS_NETWORK, IconBind.SAVE_ENABLED, MainMenu.ctrlShift(KeyEvent.VK_S));
        createItem(MenuItemNames.FILE_CLOSE_MENUITEM, ActionCommands.CLOSE_TAB, null, MainMenu.ctrl(KeyEvent.VK_W));
        createItem(MenuItemNames.FILE_NETWORKPROPERTIES_MENUITEM, ActionCommands.NETWORK_PROPERTIES, null, MainMenu.ctrl(KeyEvent.VK_D));
        createItem(MenuItemNames.FILE_LOAD_EVIDENCE_MENUITEM, ActionCommands.LOAD_EVIDENCE);
        createItem(MenuItemNames.FILE_SAVE_EVIDENCE_MENUITEM, ActionCommands.SAVE_EVIDENCE);
        createItem(MenuItemNames.FILE_EXIT_MENUITEM, ActionCommands.EXIT_APPLICATION, null, MainMenu.ctrl(KeyEvent.VK_Q));

        // Edit
        createItem(MenuItemNames.EDIT_CUT_MENUITEM, ActionCommands.CLIPBOARD_CUT, IconBind.CUT_ENABLED, MainMenu.ctrl(KeyEvent.VK_X));
        createItem(MenuItemNames.EDIT_COPY_MENUITEM, ActionCommands.CLIPBOARD_COPY, IconBind.COPY_ENABLED, MainMenu.ctrl(KeyEvent.VK_C));
        createItem(MenuItemNames.EDIT_PASTE_MENUITEM, ActionCommands.CLIPBOARD_PASTE, IconBind.PASTE_ENABLED, MainMenu.ctrl(KeyEvent.VK_V));
        createItem(MenuItemNames.EDIT_REMOVE_MENUITEM, ActionCommands.OBJECT_REMOVAL, IconBind.REMOVE_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        createItem(MenuItemNames.EDIT_UNDO_MENUITEM, ActionCommands.UNDO, IconBind.UNDO_ENABLED, MainMenu.ctrl(KeyEvent.VK_Z));
        createItem(MenuItemNames.EDIT_REDO_MENUITEM, ActionCommands.REDO, IconBind.REDO_ENABLED, MainMenu.ctrl(KeyEvent.VK_Y));
        createItem(MenuItemNames.EDIT_SELECTALL_MENUITEM, ActionCommands.SELECT_ALL, null, MainMenu.ctrl(KeyEvent.VK_E));
        createItem(MenuItemNames.EDIT_AUTOARRANGE_MENUITEM, ActionCommands.AUTO_ARRANGE, null, null);
        createCheckBox(MenuItemNames.EDIT_MODE_SELECTION_MENUITEM, ActionCommands.OBJECT_SELECTION, IconBind.SELECTION_ENABLED, this.groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_CHANCE_MENUITEM, ActionCommands.CHANCE_CREATION, IconBind.CHANCE_ENABLED, this.groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_DECISION_MENUITEM, ActionCommands.DECISION_CREATION, IconBind.DECISION_ENABLED, this.groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_UTILITY_MENUITEM, ActionCommands.UTILITY_CREATION, IconBind.UTILITY_ENABLED, this.groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_LINK_MENUITEM, ActionCommands.LINK_CREATION, IconBind.LINK_PARENT_ENABLED, this.groupEditOptions);
        createItem(MenuItemNames.EDIT_NODEPROPERTIES_MENUITEM, ActionCommands.NODE_PROPERTIES);
        createItem(MenuItemNames.EDIT_NODERELATION_MENUITEM, ActionCommands.EDIT_POTENTIAL);
        createItem(MenuItemNames.EDIT_LINKPROPERTIES_MENUITEM, ActionCommands.LINK_PROPERTIES);
        createItem(MenuItemNames.EDIT_OPENSETTINGS_MENUITEM, ActionCommands.OPEN_SETTINGS, IconBind.SETTINGS_ENABLED, null);

        // Inference
        createItem(MenuItemNames.INFERENCE_SWITCH_TO_EDITION_MODE_MENUITEM, ActionCommands.CHANGE_TO_EDITION_MODE, null, MainMenu.ctrl(KeyEvent.VK_I));
        createItem(MenuItemNames.PROPAGATION_OPTIONS_MENUITEM, ActionCommands.PROPAGATION_OPTIONS);
        createItem(MenuItemNames.INFERENCE_OPTIONS_MENUITEM, ActionCommands.INFERENCE_OPTIONS);
        createItem(MenuItemNames.INFERENCE_CREATE_NEW_EVIDENCE_CASE_MENUITEM, ActionCommands.CREATE_NEW_EVIDENCE_CASE, IconBind.CREATE_NEW_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_CLEAR_OUT_ALL_EVIDENCE_CASES_MENUITEM, ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES, IconBind.CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_FIRST_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_FIRST_EVIDENCE_CASE, IconBind.GO_TO_FIRST_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_PREVIOUS_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE, IconBind.GO_TO_PREVIOUS_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_NEXT_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_NEXT_EVIDENCE_CASE, IconBind.GO_TO_NEXT_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_LAST_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_LAST_EVIDENCE_CASE, IconBind.GO_TO_LAST_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_PROPAGATE_EVIDENCE_MENUITEM, ActionCommands.PROPAGATE_EVIDENCE, IconBind.PROPAGATE_EVIDENCE_ENABLED, MainMenu.ctrl(KeyEvent.VK_F));
        createItem(MenuItemNames.INFERENCE_EXPAND_NODE_MENUITEM, ActionCommands.NODE_EXPANSION);
        createItem(MenuItemNames.INFERENCE_CONTRACT_NODE_MENUITEM, ActionCommands.NODE_CONTRACTION);
        createItem(MenuItemNames.INFERENCE_REMOVE_ALL_FINDINGS_MENUITEM, ActionCommands.NODE_REMOVE_ALL_FINDINGS);

        // View
        createCheckBox(MenuItemNames.VIEW_NODES_BYNAME_MENUITEM, ActionCommands.BYNAME_NODES, null, this.groupByNameByTitle);
        createCheckBox(MenuItemNames.VIEW_NODES_BYTITLE_MENUITEM, ActionCommands.BYTITLE_NODES, null, this.groupByNameByTitle);

        // Tools
        createItem(MenuItemNames.CONFIGURATION_MENUITEM, ActionCommands.CONFIGURATION);

        // Help
        createItem(MenuItemNames.HELP_SHORTCUTS_MENUITEM, ActionCommands.HELP_SHORTCUTS);
        createItem(MenuItemNames.HELP_ABOUT_MENUITEM, ActionCommands.HELP_ABOUT);
        createItem(MenuItemNames.HELP_CHANGELANGUAGE_MENUITEM, ActionCommands.HELP_CHANGE_LANGUAGE);
    }

    // ── Menu builders ──────────────────────────────────────────────

    private JMenu buildFileMenu() {
        this.fileMenu = MainMenu.createMenu(MenuItemNames.FILE_MENU);
        rechargeFileMenu();
        return this.fileMenu;
    }

    private JMenu buildEditMenu() {
        this.editMenu = MainMenu.createMenu(MenuItemNames.EDIT_MENU);
        this.editMenu.add(this.items.get(ActionCommands.CLIPBOARD_CUT));
        this.editMenu.add(this.items.get(ActionCommands.CLIPBOARD_COPY));
        this.editMenu.add(this.items.get(ActionCommands.CLIPBOARD_PASTE));
        this.editMenu.add(this.items.get(ActionCommands.OBJECT_REMOVAL));
        this.editMenu.addSeparator();
        this.editMenu.add(this.items.get(ActionCommands.UNDO));
        this.editMenu.add(this.items.get(ActionCommands.REDO));
        this.editMenu.addSeparator();
        this.editMenu.add(this.items.get(ActionCommands.SELECT_ALL));
        this.editMenu.addSeparator();
        this.editMenu.add(this.items.get(ActionCommands.AUTO_ARRANGE));
//        editMenu.addSeparator();
//        editMenu.add(items.get(ActionCommands.OBJECT_SELECTION));
//        editMenu.add(items.get(ActionCommands.CHANCE_CREATION));
//        editMenu.add(items.get(ActionCommands.DECISION_CREATION));
//        editMenu.add(items.get(ActionCommands.UTILITY_CREATION));
//        editMenu.add(items.get(ActionCommands.LINK_CREATION));
        this.editMenu.addSeparator();
        this.editMenu.add(this.items.get(ActionCommands.NODE_PROPERTIES));
        this.editMenu.add(this.items.get(ActionCommands.EDIT_POTENTIAL));
        this.editMenu.addSeparator();
        this.editMenu.add(this.items.get(ActionCommands.CHANGE_TO_EDITION_MODE));
        this.editMenu.add(this.items.get(ActionCommands.PROPAGATION_OPTIONS));
        this.editMenu.add(this.items.get(ActionCommands.INFERENCE_OPTIONS));
        this.editMenu.addSeparator();
        this.editMenu.add(this.items.get(ActionCommands.OPEN_SETTINGS));
        return this.editMenu;
    }

    private JMenu buildInferenceMenu() {
        this.inferenceMenu = MainMenu.createMenu(MenuItemNames.INFERENCE_MENU);
        rebuildInferenceMenu(false);
        return this.inferenceMenu;
    }
    
    private void rebuildInferenceMenu(boolean withPropagate) {
        this.inferenceMenu.removeAll();
        this.inferenceMenu.add(this.items.get(ActionCommands.CREATE_NEW_EVIDENCE_CASE));
        this.inferenceMenu.add(this.items.get(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES));
        this.inferenceMenu.addSeparator();
        this.inferenceMenu.add(this.items.get(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE));
        this.inferenceMenu.add(this.items.get(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE));
        this.inferenceMenu.add(this.items.get(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE));
        this.inferenceMenu.add(this.items.get(ActionCommands.GO_TO_LAST_EVIDENCE_CASE));
        if (withPropagate) {
            this.inferenceMenu.addSeparator();
            this.inferenceMenu.add(this.items.get(ActionCommands.PROPAGATE_EVIDENCE));
        }
        this.inferenceMenu.addSeparator();
        this.inferenceMenu.add(this.items.get(ActionCommands.NODE_EXPANSION));
        this.inferenceMenu.add(this.items.get(ActionCommands.NODE_CONTRACTION));
        this.inferenceMenu.addSeparator();
        this.inferenceMenu.add(this.items.get(ActionCommands.NODE_REMOVE_ALL_FINDINGS));
    }
    
    private JMenu buildViewMenu() {
        if (this.viewMenu == null) {
            this.viewMenu = new JMenu();
            this.viewMenu.setName(MenuItemNames.VIEW_MENU);
            this.viewMenu.setText(MenuLocalizer.getLabel(MenuItemNames.VIEW_MENU));
            this.viewMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.VIEW_MENU).charAt(0));

            LocalizedMenuItem goNextTab = new LocalizedMenuItem(MenuItemNames.VIEW_GO_NEXT_TAB, null,
                                                                null, MainMenu.ctrlShift(KeyEvent.VK_RIGHT));
            this.viewMenu.add(goNextTab);
            goNextTab.addActionListener(e -> {
                var networksTabPanel = this.mainPanel.getNetworksTabPanel();
                if (networksTabPanel.getTabCount() <= 1) return;
                int nextIndex = networksTabPanel.getSelectedIndex() + 1;
                if (nextIndex >= networksTabPanel.getTabCount()) nextIndex = 0;
                networksTabPanel.setSelectedIndex(nextIndex);
            });
            
            LocalizedMenuItem goPreviousTab = new LocalizedMenuItem(MenuItemNames.VIEW_GO_PREVIOUS_TAB, null,
                                                                    null, MainMenu.ctrlShift(KeyEvent.VK_LEFT));
            this.viewMenu.add(goPreviousTab);
            goPreviousTab.addActionListener(e -> {
                var networksTabPanel = this.mainPanel.getNetworksTabPanel();
                if (networksTabPanel.getTabCount() <= 1) return;
                int previous = networksTabPanel.getSelectedIndex() - 1;
                if (previous == -1) previous = networksTabPanel.getTabCount() - 1;
                networksTabPanel.setSelectedIndex(previous);
            });
        }
        return this.viewMenu;
    }
    
    private JMenu buildToolsMenu() {
        if (this.toolsMenu == null) {
            this.toolsMenu = MainMenu.createMenu(MenuItemNames.TOOLS_MENU);
            ToolPluginManager toolsMenuManager = ToolPluginManager.getInstance();
            var pluginsByGroupIterator
                    = new TreeMap<>(toolsMenuManager.getAllToolPlugins().stream()
                                                    .collect(Collectors.groupingBy(ToolPlugin::pluginGroup)))
                    .entrySet().iterator();
            while (pluginsByGroupIterator.hasNext()) {
                var plugins = pluginsByGroupIterator.next().getValue();
                plugins.sort(Comparator.comparing(ToolPlugin::priorityInGroup));
                for (ToolPlugin plugin : plugins) {
                    this.toolsMenu.add(plugin.toMenuItem());
                }
                if (pluginsByGroupIterator.hasNext()) {
                    this.toolsMenu.addSeparator();
                }
            }
            this.toolsMenu.addSeparator();
            this.toolsMenu.add(this.items.get(ActionCommands.CONFIGURATION));
        }
        return this.toolsMenu;
    }
    
    private JMenu buildHelpMenu() {
        this.helpMenu = MainMenu.createMenu(MenuItemNames.HELP_MENU);
        this.helpMenu.add(this.items.get(ActionCommands.HELP_SHORTCUTS));
        this.helpMenu.add(this.items.get(ActionCommands.HELP_ABOUT));
        var tours = TourManager.availableProductTours();
        if (!tours.isEmpty()) {
            var productToursMenu = new JMenuItemBuilder("Product tours");
            for (var tourProviderAndTours : tours.entrySet()) {
                var tourProvider = tourProviderAndTours.getKey();
                var providerMenu = new JMenuItemBuilder(tourProvider.name());
                for (var productTour : tourProviderAndTours.getValue()) {
                    providerMenu.withItem(
                            new JMenuItemBuilder(productTour.getName())
                                    .onClick(() -> {
                                        new Thread(() -> {
                                            productTour.launch(new UserActionRequester(productTour), MainGUI.INSTANCE);
                                        }).start();
                                    })
                                    .build());
                }
                productToursMenu.withItem(providerMenu.build());
            }
            this.helpMenu.add(productToursMenu.build());
        }
        this.helpMenu.add(new JSeparator());
        
        this.helpMenu.add(new JMenuItemBuilder("License")
                                  .onClick(() -> {
                                      ArrayList<License> omLicenseList = new ArrayList<>();
                                      var OpenMarkovHolder = new LicenseHolder("org.openmarkov", "OpenMarkov", "", omLicenseList);
                                      omLicenseList.add(new License("GNU GENERAL PUBLIC LICENSE", "https://github.com/OpenMarkov/OpenMarkov/blob/development/LICENSE", null, "/OPENMARKOV_LICENSE", null));
                                      omLicenseList.getFirst().setHolder(OpenMarkovHolder);
                                      
                                      LicenseDialog dialog = new LicenseDialog(MainGUI.INSTANCE, omLicenseList.getFirst(), null);
                                      dialog.setTitle("OpenMarkov's license");
                                      GUIUtils.showDialog(dialog);
                                  })
                                  .build());
        
        var licenses = LicenseHolder.LICENSE_HOLDERS.stream()
                                                    .flatMap(licenseHolder -> licenseHolder.licenses().stream())
                                                    .toList();
        
        if (!LicenseHolder.LICENSE_HOLDERS.isEmpty()) {
            Stream<JMenuItem> licenseMenuItems = LicenseHolder.LICENSE_HOLDERS.stream()
                                                                              .flatMap(licenseHolder -> licenseHolder.licenses()
                                                                                                                     .stream()
                                                                                                                     .map(license -> Tuples.record(licenseHolder, license)))
                                                                              .map(licenseHolderAndLicense -> {
                                                                                  var holder = licenseHolderAndLicense.v0();
                                                                                  var license = licenseHolderAndLicense.v1();
                                                                                  boolean holderHasMultipleLicenses = holder.licenses()
                                                                                                                            .size() > 1;
                                                                                  int licenseIndex = holderHasMultipleLicenses ? holder.licenses()
                                                                                                                                       .indexOf(license) : 0;
                                                                                  String title = holder.descriptor() + (!holderHasMultipleLicenses ? "" : " - License " + (licenseIndex + 1));
                                                                                  return new JMenuItemBuilder(title)
                                                                                          .onClick(_ -> GUIUtils.showDialog(new LicenseDialog(MainGUI.INSTANCE, license, licenses)))
                                                                                          .build();
                                                                              });
            this.helpMenu.add(new JMenuItemBuilder("Third-party licenses").withItems(licenseMenuItems).build());
        }
        
        return this.helpMenu;
    }
    
    
    private static final Pattern CLEAN_DOCTYPE = Pattern.compile("(?s)<!DOCTYPE[^>]*>");
    private static final Pattern CLEAN_HTML = Pattern.compile("(?i)<html[^>]*>");
    private static final Pattern CLEAN_METADATA = Pattern.compile("(?i)<meta[^>]*>");
    
    private static String prepareHtmlForSwing(String rawHtml) {
        // 1. Remove DOCTYPE declaration entirely
        // 2. Strip XML namespaces and attributes from <html> tag (e.g. <html xmlns=... xml:lang=...>)
        // 3. Remove <meta> tags which break Swing's character set parser
        return MainMenu.CLEAN_METADATA.matcher(
                               MainMenu.CLEAN_HTML.matcher(
                                       MainMenu.CLEAN_DOCTYPE.matcher(rawHtml).replaceAll("")
                               ).replaceAll("<html>")
                       )
                                      .replaceAll("")
                                      // 4. Replace non-standard HTML entities with standard quotes/apostrophes
                                      .replace("&ldquo;", "\"")
                                      .replace("&rdquo;", "\"")
                                      .replace("&lsquo;", "'")
                                      .replace("&rsquo;", "'")
                                      .replace("&#039;", "'");
    }
    
    
    private static void generateLicenseDialog(String licenseContent, DialogBase licenseDialog) {
        JEditorPane editorPane = new JTextPane();
        editorPane.setText(licenseContent);
        if (licenseContent.contains("</html>")) {
            editorPane.setContentType("text/html");
            editorPane.setText(MainMenu.prepareHtmlForSwing(licenseContent));
        }
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        JScrollPane jScrollPane = new JScrollPane(editorPane);
        licenseDialog.getContentPane().setLayout(new BorderLayout());
        licenseDialog.getContentPane().add(jScrollPane, BorderLayout.CENTER);
        JButton cancelButton = new JButton();
        cancelButton.addActionListener(_ -> licenseDialog.dispose());
        licenseDialog.setCancelButton(cancelButton);
        licenseDialog.pack();
        int width = licenseDialog.getWidth();
        int height = licenseDialog.getHeight();
        Dimension size = new Dimension(
                Math.clamp(width, 200, 600),
                Math.clamp(height, 200, 800)
        );
        licenseDialog.setSize(size);
    }
    
    private static class LicenseDialog extends DialogBase {
        
        private static final boolean CAN_DISPLAY_LICENSE_IN_BROWSER = Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                                                                             .isSupported(Desktop.Action.BROWSE);
        
        private final JEditorPane editorPane;
        private final JButton showInBrowserButton;
        private License displayedLicense;
        
        LicenseDialog(Window owner, License initialLicense, @Nullable List<License> licenses) {
            super(owner);
            this.editorPane = new JTextPane();
            this.editorPane.setEditable(false);
            
            this.showInBrowserButton = new JButton("Show in browser");
            this.showInBrowserButton
                    .addActionListener(e -> {
                        try {
                            Desktop.getDesktop().browse(new URI(this.displayedLicense.URL));
                        } catch (IOException | URISyntaxException ex) {
                            throw new UnrecoverableException(ex);
                        }
                    });
            
            this.displayedLicense = initialLicense;
            showLicense(this.displayedLicense);
            
            JScrollPane jScrollPane = new JScrollPane(this.editorPane);
            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(jScrollPane, BorderLayout.CENTER);
            JButton cancelButton = new JButton();
            cancelButton.addActionListener(_ -> this.dispose());
            this.setCancelButton(cancelButton);
            
            JPanel buttonsPanel = new JPanel();
            this.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
            boolean hasMultipleLicenses = licenses != null && licenses.size() > 1;
            if (hasMultipleLicenses) {
                JButton goToPreviousLicense = new JButton("Go to previous license");
                goToPreviousLicense.addActionListener(_ -> {
                    var indexToShow = licenses.indexOf(this.displayedLicense);
                    indexToShow = (indexToShow == 0 ? licenses.size() : indexToShow) - 1;
                    this.showLicense(licenses.get(indexToShow));
                });
                buttonsPanel.add(goToPreviousLicense);
            }
            buttonsPanel.add(this.showInBrowserButton);
            if (hasMultipleLicenses) {
                JButton goToNextLicense = new JButton("Go to next license");
                goToNextLicense.addActionListener(_ -> {
                    var indexToShow = licenses.indexOf(this.displayedLicense);
                    indexToShow = indexToShow + 1 == licenses.size() ? 0 : indexToShow + 1;
                    this.showLicense(licenses.get(indexToShow));
                });
                buttonsPanel.add(goToNextLicense);
            }
            this.setMinimumSize(new Dimension(450, 0));
        }
        
        private void showLicense(License license) {
            this.displayedLicense = license;
            String licenseContent = "This license cannot be displayed";
            try {
                licenseContent = new String(LicenseHolder.RESOURCE_RESOLVER.getResourceAsStream(this.displayedLicense.resource)
                                                                           .readAllBytes());
            } catch (IOException e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
            boolean isHTMLContent = licenseContent.trim().endsWith("</html>");
            this.editorPane.setContentType(isHTMLContent ? "text/html" : "text/plain");
            try {
                this.editorPane.setText(isHTMLContent ? MainMenu.prepareHtmlForSwing(licenseContent) : licenseContent);
            } catch (RuntimeException e) {
                this.editorPane.setContentType("text/plain");
                this.editorPane.setText(licenseContent);
            }
            this.editorPane.setCaretPosition(0);
            SwingUtilities.invokeLater(() -> {
                this.pack();
                int width = this.getWidth();
                int height = this.getHeight();
                Dimension size = new Dimension(
                        Math.clamp(width, 200, 600),
                        Math.clamp(height, 200, 800)
                );
                this.setSize(size);
                GUIUtils.centerDialogToParent(this);
            });
            
            boolean holderHasMultipleLicenses = this.displayedLicense.holder().licenses().size() > 1;
            this.setTitle(this.displayedLicense.holder().descriptor()
                                  + (!holderHasMultipleLicenses ? "" : " - License " + (this.displayedLicense.holder()
                                                                                                             .licenses()
                                                                                                             .indexOf(this.displayedLicense) + 1)));
            
            if (!LicenseDialog.CAN_DISPLAY_LICENSE_IN_BROWSER) {
                this.showInBrowserButton.setEnabled(false);
                this.showInBrowserButton.setToolTipText("This license is only available while online at " + this.displayedLicense.URL
                                                                + System.lineSeparator() + "But your operating system does not allow to open URLs in your preferred web browser.");
            } else {
                this.showInBrowserButton.setEnabled(true);
                this.showInBrowserButton.setToolTipText("Opens this license from it's source URL, which is " + this.displayedLicense.URL);
            }
        }
    }


    // ── Action command lookup ──────────────────────────────────────
    
    private JComponent getJComponentActionCommand(String actionCommand) {
        ActionCommands cmd = ActionCommands.of(actionCommand);
        if (cmd == null) return null;
        // CHANGE_WORKING_MODE and CHANGE_TO_INFERENCE_MODE map to the same switch item
        if (cmd == ActionCommands.CHANGE_WORKING_MODE || cmd == ActionCommands.CHANGE_TO_INFERENCE_MODE) {
            return this.items.get(ActionCommands.CHANGE_TO_EDITION_MODE);
        }
        if (cmd == ActionCommands.NODES) return this.viewNodesMenu;
        return this.items.get(cmd);
    }
    
    // ── Recent files ───────────────────────────────────────────────
    
    private List<LastRecentFilesMenuItem> getLastOpenFiles() {
        var lastOpenFilesItems = new ArrayList<LastRecentFilesMenuItem>();
        int index = 0;
        for (String recentFile : UserPreferences.LAST_OPEN_NETWORKS_FILES.get()) {
            LastRecentFilesMenuItem item = new LastRecentFilesMenuItem();
            item.setName("lastRecentFilesMenuItem" + index);
            item.setText(recentFile);
            ActionCommands command = ActionCommands.openLastFileCommandAt(index);
            if (command != null) {
                item.setActionCommand(command.getCommandName());
            }
            item.addActionListener(this.listener);
            lastOpenFilesItems.add(item);
            index += 1;
        }
        return lastOpenFilesItems;
    }
    
    // ── Factory methods ────────────────────────────────────────────
    
    private void createItem(String name, ActionCommands action) {
        createItem(name, action, null, null);
    }
    
    private void createItem(String name, ActionCommands action, IconBind icon, KeyStroke key) {
        var item = new LocalizedMenuItem(name, action.getCommandName(), icon, key);
        item.addActionListener(this.listener);
        this.items.put(action, item);
    }
    
    private void createCheckBox(String name, ActionCommands action, IconBind icon, ButtonGroup group) {
        var item = icon != null
                ? new LocalizedCheckBoxMenuItem(name, action.getCommandName(), icon)
                : new LocalizedCheckBoxMenuItem(name, action.getCommandName());
        item.addActionListener(this.listener);
        if (group != null) group.add(item);
        this.items.put(action, item);
    }
    
    private static JMenu createMenu(String menuItemName) {
        var menu = new JMenu();
        menu.setName(menuItemName);
        menu.setText(MenuLocalizer.getLabel(menuItemName));
        menu.setMnemonic(MenuLocalizer.getMnemonic(menuItemName).charAt(0));
        return menu;
    }
    
    private static KeyStroke ctrl(int key) {
        return KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK);
    }
    
    private static KeyStroke ctrlShift(int key) {
        return KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
    }
    
    private static KeyStroke ctrlAlt(int key) {
        return KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    }
    
    private static boolean stringIsDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
