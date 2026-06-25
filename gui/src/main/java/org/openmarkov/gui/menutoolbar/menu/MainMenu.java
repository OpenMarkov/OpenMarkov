/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.component.LastRecentFilesMenuItem;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.common.RequestDialogger;
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
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;

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
        fileMenu.removeAll();
        fileMenu.add(items.get(ActionCommands.NEW_NETWORK));
        fileMenu.add(items.get(ActionCommands.OPEN_NETWORK));
        fileMenu.add(items.get(ActionCommands.OPEN_NETWORK_URL));
        fileMenu.addSeparator();
        fileMenu.add(items.get(ActionCommands.SAVE_NETWORK));
        fileMenu.add(items.get(ActionCommands.SAVE_OPEN_NETWORK));
        fileMenu.add(items.get(ActionCommands.SAVEAS_NETWORK));
        fileMenu.add(items.get(ActionCommands.CLOSE_TAB));
        fileMenu.addSeparator();
        fileMenu.add(items.get(ActionCommands.NETWORK_PROPERTIES));
        fileMenu.add(items.get(ActionCommands.LOAD_EVIDENCE));
        if (LastOpenFiles.existLastOpenFiles()) {
            fileMenu.addSeparator();
            getLastOpenFiles().forEach(fileMenu::add);
        }
        fileMenu.addSeparator();
        fileMenu.add(items.get(ActionCommands.EXIT_APPLICATION));
        fileMenu.repaint();
    }
    
    public void addPropagateNowItem() {
        if (inferenceMenu != null) rebuildInferenceMenu(true);
    }
    
    public void removePropagateNowItem() {
        if (inferenceMenu != null) rebuildInferenceMenu(false);
    }
    
    public JMenuItem getSwitchWorkingMode() {
        return (JMenuItem) items.get(ActionCommands.CHANGE_TO_EDITION_MODE);
    }
    
    @Override public void setOptionEnabled(String actionCommand, boolean b) {
        MenuToolBarBasicImpl.setOptionEnabled(getJComponentActionCommand(actionCommand), b);
    }
    
    @Override public void setOptionSelected(String actionCommand, boolean b) {
        MenuToolBarBasicImpl.setOptionSelected(getJComponentActionCommand(actionCommand), b);
    }
    
    @Override public void addOptionText(String actionCommand, String text) {
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.addOptionText(component, defaultText.get(component), text);
    }
    
    @Override public void setText(String actionCommand, String text) {
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.setText(component, text);
    }
    
    // ── Item creation (called once) ────────────────────────────────
    
    private void createAllItems() {
        // File
        createItem(MenuItemNames.FILE_NEW_MENUITEM, ActionCommands.NEW_NETWORK, IconBind.NEW_ENABLED, ctrl(KeyEvent.VK_N));
        createItem(MenuItemNames.FILE_OPEN_MENUITEM, ActionCommands.OPEN_NETWORK, IconBind.OPEN_ENABLED, ctrl(KeyEvent.VK_O));
        createItem(MenuItemNames.FILE_OPEN_URL_MENUITEM, ActionCommands.OPEN_NETWORK_URL, IconBind.OPEN_URL_ENABLED, ctrlAlt(KeyEvent.VK_O));
        createItem(MenuItemNames.FILE_SAVE_MENUITEM, ActionCommands.SAVE_NETWORK, IconBind.SAVE_ENABLED, ctrl(KeyEvent.VK_S));
        createItem(MenuItemNames.FILE_SAVE_OPEN_MENUITEM, ActionCommands.SAVE_OPEN_NETWORK, IconBind.SAVE_ENABLED, ctrlAlt(KeyEvent.VK_S));
        createItem(MenuItemNames.FILE_SAVEAS_MENUITEM, ActionCommands.SAVEAS_NETWORK, IconBind.SAVE_ENABLED, ctrlShift(KeyEvent.VK_S));
        createItem(MenuItemNames.FILE_CLOSE_MENUITEM, ActionCommands.CLOSE_TAB, null, ctrl(KeyEvent.VK_W));
        createItem(MenuItemNames.FILE_NETWORKPROPERTIES_MENUITEM, ActionCommands.NETWORK_PROPERTIES, null, ctrl(KeyEvent.VK_D));
        createItem(MenuItemNames.FILE_LOAD_EVIDENCE_MENUITEM, ActionCommands.LOAD_EVIDENCE);
        createItem(MenuItemNames.FILE_SAVE_EVIDENCE_MENUITEM, ActionCommands.SAVE_EVIDENCE);
        createItem(MenuItemNames.FILE_EXIT_MENUITEM, ActionCommands.EXIT_APPLICATION, null, ctrl(KeyEvent.VK_Q));
        
        // Edit
        createItem(MenuItemNames.EDIT_CUT_MENUITEM, ActionCommands.CLIPBOARD_CUT, IconBind.CUT_ENABLED, ctrl(KeyEvent.VK_X));
        createItem(MenuItemNames.EDIT_COPY_MENUITEM, ActionCommands.CLIPBOARD_COPY, IconBind.COPY_ENABLED, ctrl(KeyEvent.VK_C));
        createItem(MenuItemNames.EDIT_PASTE_MENUITEM, ActionCommands.CLIPBOARD_PASTE, IconBind.PASTE_ENABLED, ctrl(KeyEvent.VK_V));
        createItem(MenuItemNames.EDIT_REMOVE_MENUITEM, ActionCommands.OBJECT_REMOVAL, IconBind.REMOVE_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        createItem(MenuItemNames.EDIT_UNDO_MENUITEM, ActionCommands.UNDO, IconBind.UNDO_ENABLED, ctrl(KeyEvent.VK_Z));
        createItem(MenuItemNames.EDIT_REDO_MENUITEM, ActionCommands.REDO, IconBind.REDO_ENABLED, ctrl(KeyEvent.VK_Y));
        createItem(MenuItemNames.EDIT_SELECTALL_MENUITEM, ActionCommands.SELECT_ALL, null, ctrl(KeyEvent.VK_E));
        createItem(MenuItemNames.EDIT_AUTOARRANGE_MENUITEM, ActionCommands.AUTO_ARRANGE, null, null);
        createCheckBox(MenuItemNames.EDIT_MODE_SELECTION_MENUITEM, ActionCommands.OBJECT_SELECTION, IconBind.SELECTION_ENABLED, groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_CHANCE_MENUITEM, ActionCommands.CHANCE_CREATION, IconBind.CHANCE_ENABLED, groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_DECISION_MENUITEM, ActionCommands.DECISION_CREATION, IconBind.DECISION_ENABLED, groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_UTILITY_MENUITEM, ActionCommands.UTILITY_CREATION, IconBind.UTILITY_ENABLED, groupEditOptions);
        createCheckBox(MenuItemNames.EDIT_MODE_LINK_MENUITEM, ActionCommands.LINK_CREATION, IconBind.LINK_PARENT_ENABLED, groupEditOptions);
        createItem(MenuItemNames.EDIT_NODEPROPERTIES_MENUITEM, ActionCommands.NODE_PROPERTIES);
        createItem(MenuItemNames.EDIT_NODERELATION_MENUITEM, ActionCommands.EDIT_POTENTIAL);
        createItem(MenuItemNames.EDIT_LINKPROPERTIES_MENUITEM, ActionCommands.LINK_PROPERTIES);
        
        // Inference
        createItem(MenuItemNames.INFERENCE_SWITCH_TO_EDITION_MODE_MENUITEM, ActionCommands.CHANGE_TO_EDITION_MODE, null, ctrl(KeyEvent.VK_I));
        createItem(MenuItemNames.PROPAGATION_OPTIONS_MENUITEM, ActionCommands.PROPAGATION_OPTIONS);
        createItem(MenuItemNames.INFERENCE_OPTIONS_MENUITEM, ActionCommands.INFERENCE_OPTIONS);
        createItem(MenuItemNames.INFERENCE_CREATE_NEW_EVIDENCE_CASE_MENUITEM, ActionCommands.CREATE_NEW_EVIDENCE_CASE, IconBind.CREATE_NEW_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_CLEAR_OUT_ALL_EVIDENCE_CASES_MENUITEM, ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES, IconBind.CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_FIRST_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_FIRST_EVIDENCE_CASE, IconBind.GO_TO_FIRST_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_PREVIOUS_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE, IconBind.GO_TO_PREVIOUS_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_NEXT_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_NEXT_EVIDENCE_CASE, IconBind.GO_TO_NEXT_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_GO_TO_LAST_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_LAST_EVIDENCE_CASE, IconBind.GO_TO_LAST_EVIDENCE_CASE_ENABLED, null);
        createItem(MenuItemNames.INFERENCE_PROPAGATE_EVIDENCE_MENUITEM, ActionCommands.PROPAGATE_EVIDENCE, IconBind.PROPAGATE_EVIDENCE_ENABLED, ctrl(KeyEvent.VK_F));
        createItem(MenuItemNames.INFERENCE_EXPAND_NODE_MENUITEM, ActionCommands.NODE_EXPANSION);
        createItem(MenuItemNames.INFERENCE_CONTRACT_NODE_MENUITEM, ActionCommands.NODE_CONTRACTION);
        createItem(MenuItemNames.INFERENCE_REMOVE_ALL_FINDINGS_MENUITEM, ActionCommands.NODE_REMOVE_ALL_FINDINGS);
        
        // View
        createCheckBox(MenuItemNames.VIEW_NODES_BYNAME_MENUITEM, ActionCommands.BYNAME_NODES, null, groupByNameByTitle);
        createCheckBox(MenuItemNames.VIEW_NODES_BYTITLE_MENUITEM, ActionCommands.BYTITLE_NODES, null, groupByNameByTitle);
        
        // Tools
        createItem(MenuItemNames.CONFIGURATION_MENUITEM, ActionCommands.CONFIGURATION);
        
        // Help
        createItem(MenuItemNames.HELP_SHORTCUTS_MENUITEM, ActionCommands.HELP_SHORTCUTS);
        createItem(MenuItemNames.HELP_ABOUT_MENUITEM, ActionCommands.HELP_ABOUT);
        createItem(MenuItemNames.HELP_CHANGELANGUAGE_MENUITEM, ActionCommands.HELP_CHANGE_LANGUAGE);
    }
    
    // ── Menu builders ──────────────────────────────────────────────
    
    private JMenu buildFileMenu() {
        fileMenu = createMenu(MenuItemNames.FILE_MENU);
        rechargeFileMenu();
        return fileMenu;
    }
    
    private JMenu buildEditMenu() {
        editMenu = createMenu(MenuItemNames.EDIT_MENU);
        editMenu.add(items.get(ActionCommands.CLIPBOARD_CUT));
        editMenu.add(items.get(ActionCommands.CLIPBOARD_COPY));
        editMenu.add(items.get(ActionCommands.CLIPBOARD_PASTE));
        editMenu.add(items.get(ActionCommands.OBJECT_REMOVAL));
        editMenu.addSeparator();
        editMenu.add(items.get(ActionCommands.UNDO));
        editMenu.add(items.get(ActionCommands.REDO));
        editMenu.addSeparator();
        editMenu.add(items.get(ActionCommands.SELECT_ALL));
        editMenu.addSeparator();
        editMenu.add(items.get(ActionCommands.AUTO_ARRANGE));
        editMenu.addSeparator();
        editMenu.add(items.get(ActionCommands.OBJECT_SELECTION));
        editMenu.add(items.get(ActionCommands.CHANCE_CREATION));
        editMenu.add(items.get(ActionCommands.DECISION_CREATION));
        editMenu.add(items.get(ActionCommands.UTILITY_CREATION));
        editMenu.add(items.get(ActionCommands.LINK_CREATION));
        editMenu.addSeparator();
        editMenu.add(items.get(ActionCommands.NODE_PROPERTIES));
        editMenu.add(items.get(ActionCommands.EDIT_POTENTIAL));
        editMenu.addSeparator();
        editMenu.add(items.get(ActionCommands.CHANGE_TO_EDITION_MODE));
        editMenu.add(items.get(ActionCommands.PROPAGATION_OPTIONS));
        editMenu.add(items.get(ActionCommands.INFERENCE_OPTIONS));
        return editMenu;
    }
    
    private JMenu buildInferenceMenu() {
        inferenceMenu = createMenu(MenuItemNames.INFERENCE_MENU);
        rebuildInferenceMenu(false);
        return inferenceMenu;
    }
    
    private void rebuildInferenceMenu(boolean withPropagate) {
        inferenceMenu.removeAll();
        inferenceMenu.add(items.get(ActionCommands.CREATE_NEW_EVIDENCE_CASE));
        inferenceMenu.add(items.get(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES));
        inferenceMenu.addSeparator();
        inferenceMenu.add(items.get(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE));
        inferenceMenu.add(items.get(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE));
        inferenceMenu.add(items.get(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE));
        inferenceMenu.add(items.get(ActionCommands.GO_TO_LAST_EVIDENCE_CASE));
        if (withPropagate) {
            inferenceMenu.addSeparator();
            inferenceMenu.add(items.get(ActionCommands.PROPAGATE_EVIDENCE));
        }
        inferenceMenu.addSeparator();
        inferenceMenu.add(items.get(ActionCommands.NODE_EXPANSION));
        inferenceMenu.add(items.get(ActionCommands.NODE_CONTRACTION));
        inferenceMenu.addSeparator();
        inferenceMenu.add(items.get(ActionCommands.NODE_REMOVE_ALL_FINDINGS));
    }
    
    private JMenu buildViewMenu() {
        if (viewMenu == null) {
            viewMenu = new JMenu();
            viewMenu.setName(MenuItemNames.VIEW_MENU);
            viewMenu.setText(MenuLocalizer.getLabel(MenuItemNames.VIEW_MENU));
            viewMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.VIEW_MENU).charAt(0));
            
            LocalizedMenuItem goNextTab = new LocalizedMenuItem(MenuItemNames.VIEW_GO_NEXT_TAB, null,
                                                                null, ctrlShift(KeyEvent.VK_RIGHT));
            viewMenu.add(goNextTab);
            goNextTab.addActionListener(e -> {
                var networksTabPanel = this.mainPanel.getNetworksTabPanel();
                if (networksTabPanel.getTabCount() <= 1) return;
                int nextIndex = networksTabPanel.getSelectedIndex() + 1;
                if (nextIndex >= networksTabPanel.getTabCount()) nextIndex = 0;
                networksTabPanel.setSelectedIndex(nextIndex);
            });
            
            LocalizedMenuItem goPreviousTab = new LocalizedMenuItem(MenuItemNames.VIEW_GO_PREVIOUS_TAB, null,
                                                                    null, ctrlShift(KeyEvent.VK_LEFT));
            viewMenu.add(goPreviousTab);
            goPreviousTab.addActionListener(e -> {
                var networksTabPanel = this.mainPanel.getNetworksTabPanel();
                if (networksTabPanel.getTabCount() <= 1) return;
                int previous = networksTabPanel.getSelectedIndex() - 1;
                if (previous == -1) previous = networksTabPanel.getTabCount() - 1;
                networksTabPanel.setSelectedIndex(previous);
            });
            
            LocalizedMenuItem changeScale = new LocalizedMenuItem(MenuItemNames.VIEW_CHANGE_SCALE, null);
            viewMenu.add(changeScale);
            changeScale.addActionListener(e -> RequestDialogger
                    .of(this.mainPanel.mainGUI, new JTextField(LocalPreferences.UI_SCALE.get().toString()))
                    .validating((textField, validator) -> {
                        if (!validator.addErrorWhen(!stringIsDouble(textField.getText()), "The value must be a number")) {
                            validator.addErrorWhen(Double.parseDouble(textField.getText()) > UI_SCALE_MAX, "Scale should not be greater than " + UI_SCALE_MAX);
                            validator.addErrorWhen(Double.parseDouble(textField.getText()) < UI_SCALE_MIN, "Scale should not be lower than " + UI_SCALE_MIN);
                        }
                    })
                    .mapInputAs(new TypeToken<>() {
                    }, textField -> Double.parseDouble(textField.getText()))
                    .withTitle("Change scale")
                    .onOk(num -> {
                        LocalPreferences.UI_SCALE.set(num);
                        JOptionPane.showMessageDialog(this.mainPanel.mainGUI, "Scale changed to " + num + "." + System.lineSeparator() + "Your changes will be applied in the next reset.",
                                                      "Changes accepted", JOptionPane.INFORMATION_MESSAGE, IconBind.OPENMARKOV_LOGO_16.icon());
                    })
                    .request());
        }
        return viewMenu;
    }
    
    private JMenu buildToolsMenu() {
        if (toolsMenu == null) {
            toolsMenu = createMenu(MenuItemNames.TOOLS_MENU);
            ToolPluginManager toolsMenuManager = ToolPluginManager.getInstance();
            var pluginsByGroupIterator
                    = new TreeMap<>(toolsMenuManager.getAllToolPlugins().stream()
                                                    .collect(Collectors.groupingBy(ToolPlugin::pluginGroup)))
                    .entrySet().iterator();
            while (pluginsByGroupIterator.hasNext()) {
                var plugins = pluginsByGroupIterator.next().getValue();
                plugins.sort(Comparator.comparing(ToolPlugin::priorityInGroup));
                for (ToolPlugin plugin : plugins) {
                    toolsMenu.add(plugin.toMenuItem());
                }
                if (pluginsByGroupIterator.hasNext()) {
                    toolsMenu.addSeparator();
                }
            }
            toolsMenu.addSeparator();
            toolsMenu.add(items.get(ActionCommands.CONFIGURATION));
        }
        return toolsMenu;
    }
    
    private JMenu buildHelpMenu() {
        helpMenu = createMenu(MenuItemNames.HELP_MENU);
        helpMenu.add(items.get(ActionCommands.HELP_SHORTCUTS));
        helpMenu.add(items.get(ActionCommands.HELP_ABOUT));
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
            helpMenu.add(productToursMenu.build());
        }
        return helpMenu;
    }
    
    // ── Action command lookup ──────────────────────────────────────
    
    private JComponent getJComponentActionCommand(String actionCommand) {
        ActionCommands cmd = ActionCommands.of(actionCommand);
        if (cmd == null) return null;
        // CHANGE_WORKING_MODE and CHANGE_TO_INFERENCE_MODE map to the same switch item
        if (cmd == ActionCommands.CHANGE_WORKING_MODE || cmd == ActionCommands.CHANGE_TO_INFERENCE_MODE) {
            return items.get(ActionCommands.CHANGE_TO_EDITION_MODE);
        }
        if (cmd == ActionCommands.NODES) return viewNodesMenu;
        return items.get(cmd);
    }
    
    // ── Recent files ───────────────────────────────────────────────
    
    private List<LastRecentFilesMenuItem> getLastOpenFiles() {
        var lastOpenFilesItems = new ArrayList<LastRecentFilesMenuItem>();
        int index = 0;
        for (String recentFile : LocalPreferences.LAST_OPEN_NETWORKS_FILES.get()) {
            LastRecentFilesMenuItem item = new LastRecentFilesMenuItem();
            item.setName("lastRecentFilesMenuItem" + index);
            item.setText(recentFile);
            ActionCommands command = ActionCommands.openLastFileCommandAt(index);
            if (command != null) {
                item.setActionCommand(command.getCommandName());
            }
            item.addActionListener(listener);
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
        item.addActionListener(listener);
        items.put(action, item);
    }
    
    private void createCheckBox(String name, ActionCommands action, IconBind icon, ButtonGroup group) {
        var item = icon != null
                ? new LocalizedCheckBoxMenuItem(name, action.getCommandName(), icon)
                : new LocalizedCheckBoxMenuItem(name, action.getCommandName());
        item.addActionListener(listener);
        if (group != null) group.add(item);
        items.put(action, item);
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
