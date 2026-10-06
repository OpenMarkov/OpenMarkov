package org.openmarkov.gui.window.settings;

import com.google.gson.reflect.TypeToken;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.bindings.Binding;
import org.openmarkov.gui.bindings.BindingPanel;
import org.openmarkov.gui.bindings.Bindings;
import org.openmarkov.gui.bindings.Input;
import org.openmarkov.gui.bindings.InputCombination;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.component.NumericSpinner;
import org.openmarkov.gui.configuration.GUIColor;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.configuration.OperatingSystem;
import org.openmarkov.gui.configuration.StartupAction;
import org.openmarkov.gui.configuration.Theme;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.configuration.gson.GsonCommon;
import org.openmarkov.gui.dialog.common.CommonOptions;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.common.OptionDialog;
import org.openmarkov.gui.dialog.io.FileFilterByExtension;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.OMTabbedPane;
import org.openmarkov.java.collectionsUtils.streamUtils.StreamUtils;
import org.openmarkov.java.langUtils.SwitchUtils;
import org.openmarkov.java.swing.ComponentUtilities;
import org.openmarkov.java.swing.SimplifiedGridBagConstraint;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SettingsDialog extends JDialog {
    
    private final JPanel sectionsPanel;
    private final JPanel currentSectionPanel;
    private @Nullable Section currentSelectedSection;
    
    public SettingsDialog(Frame owner) {
        super(owner);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setSize(800, 600);
        this.setTitle("OpenMarkov's Settings");
        this.sectionsPanel = new JPanel();
        this.sectionsPanel.setLayout(new VerticalLayout());
        this.currentSectionPanel = new JPanel();
        this.sectionsPanel.setLayout(new VerticalLayout());
        
        
        JScrollPane sectionsScrollPanel = new JScrollPane(this.sectionsPanel);
        JScrollPane currentSectionScrollPanel = new JScrollPane(this.currentSectionPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              sectionsScrollPanel,
                                              currentSectionScrollPanel);
        splitPane.setDividerLocation(Math.max(splitPane.getDividerLocation(), 150));
        this.add(splitPane);
        
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Arrays.stream(Section.values())
              .filter(section -> section.belongsTo() == null)
              .forEach(section -> root.add(new DefaultMutableTreeNode(section)));
        ArrayDeque<DefaultMutableTreeNode> sectionsToVisit = new ArrayDeque<DefaultMutableTreeNode>();
        sectionsToVisit.addAll(StreamUtils.of(root.getChildCount(), i -> (DefaultMutableTreeNode) root.getChildAt(i))
                                          .toList());
        while (!sectionsToVisit.isEmpty()) {
            var node = sectionsToVisit.pop();
            var section = (Section) node.getUserObject();
            section.children().forEach(child -> node.add(new DefaultMutableTreeNode(child)));
            sectionsToVisit.addAll(StreamUtils.of(node.getChildCount(), i -> (DefaultMutableTreeNode) node.getChildAt(i))
                                              .toList());
        }
        
        
        JTree sectionsTree = new JTree(new DefaultTreeModel(root));
        sectionsTree.addTreeSelectionListener(e -> {
            Object lastSelectedPathComponent = sectionsTree.getLastSelectedPathComponent();
            if (!(lastSelectedPathComponent instanceof DefaultMutableTreeNode node)) {
                return;
            }
            if (!(node.getUserObject() instanceof Section selectedSection)) {
                return;
            }
            currentSectionScrollPanel.setViewportView(this.generateConfigurationSection(selectedSection));
        });
        this.sectionsPanel.add(sectionsTree);
    }
    
    private Component generateConfigurationSection(Section selectedSection) {
        this.currentSelectedSection = selectedSection;
        var gridpanel = new JPanel(new GridBagLayout());
        gridpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        Component res = switch (selectedSection) {
            case UI -> {
                SettingsDialog.generateUIVisualSection(gridpanel);
                yield gridpanel;
            }
            case NETWORKS -> {
                SettingsDialog.generateNetworkVisualSection(gridpanel);
                yield gridpanel;
            }
            case BINDINGS -> {
                SettingsDialog.generateBindingsVisualSection(this, gridpanel);
                yield gridpanel;
            }
            case SETTINGS_BACKUP -> {
                this.generateSettingsBackupVisualSection(gridpanel);
                yield gridpanel;
            }
        };
        //noinspection ConstantValue
        if (res == gridpanel) {
            var resultingPanel = new JPanel(new BorderLayout());
            resultingPanel.add(gridpanel, BorderLayout.NORTH);
            return resultingPanel;
        }
        return res;
    }
    
    record BindingRowPanel(JPanel panel, JLabel bindingName, JLabel bindingHelp, JLabel bindingError,
                           JLabel bindingCombination) {
    }
    
    private static void generateBindingsVisualSection(SettingsDialog settingsDialog, JPanel panel) {
        var gridbag = new SimplifiedGridBagConstraint(panel, new GridBagConstraints(), 1);
        
        //Guide
        {
            var assignLabel = new JLabel("Assign actions to key+mouse combinations  ");
            var helpToolTip = GUIUtils.generateTooltipElement("""
                                                                      <html>
                                                                      </html>
                                                                      """);
            
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, assignLabel, helpToolTip));
        }
        //Tabs
        {
            HashMap<Binding, BindingRowPanel> bindingsToPanels = new HashMap<>();
            Consumer<Binding> assignPanelColor = binding -> {
                GUIColor borderColor;
                if (binding.isInvalid()) {
                    borderColor = GUIColors.Bindings.INVALID;
                } else if (binding.isSetByUser()) {
                    borderColor = GUIColors.Bindings.BOUND;
                } else {
                    borderColor = GUIColors.General.TRANSPARENT;
                }
                bindingsToPanels.get(binding).panel.setBorder(BorderFactory.createLineBorder(borderColor.getColor(), 2));
            };
            Runnable checkBindings = () -> {
                Binding.checkValidity(null);
                for (var binding : Bindings.ALL_BINDINGS) {
                    var bindingPanel = bindingsToPanels.get(binding);
                    var invalidityReasons = binding.invalidReasonsAsString();
                    bindingPanel.bindingError.setVisible(!invalidityReasons.isEmpty());
                    bindingPanel.bindingError.setToolTipText(invalidityReasons.orElse("<html></html>"));
                    bindingPanel.bindingCombination.setText(binding.inputCombination().toString());
                    assignPanelColor.accept(binding);
                }
            };
            
            for (var binding : Bindings.ALL_BINDINGS) {
                var keyBoardRepresentation = switch (binding.keyAcceptance) {
                    case ALLOWED -> "⌨";
                    case REQUIRES_AT_LEAST_ONE -> "\uD83D\uDFB5⌨";
                    case FORBIDDEN -> "";
                };
                var mouseRepresentation = switch (binding.mouseAcceptance) {
                    case REQUIRED -> "\uD83D\uDFB5\uD83D\uDDAF";
                    case REQUIRED_WITH_JUST_ONE_MOUSE -> "\uD83D\uDFB5⒈\uD83D\uDDAF";
                    case REQUIRED_WITH_SINGLE_CLICK -> "\uD83D\uDFB5①\uD83D\uDDAF";
                    case ALLOWED -> "\uD83D\uDDAF";
                    case ALLOWED_WITH_JUST_ONE_MOUSE -> "⒈\uD83D\uDDAF";
                    case ALLOWED_WITH_SINGLE_CLICK -> "①\uD83D\uDDAF";
                    case FORBIDDEN -> "";
                };
                
                var bindingRowPanel = new BindingRowPanel(new JPanel(new BorderLayout()),
                                                          new JLabel("  - " + binding.name + "  "),
                                                          GUIUtils.generateTooltipElement(binding.description),
                                                          GUIUtils.generateIconTooltip("◬", "<html></html>"),
                                                          new JLabel(binding.inputCombination().toString()));
                
                
                bindingRowPanel.panel
                        .add(ComponentUtilities
                                     .joinComponents(FlowLayout.LEFT,
                                                     bindingRowPanel.bindingName,
                                                     bindingRowPanel.bindingHelp,
                                                     bindingRowPanel.bindingError,
                                                     new JLabel("   "),
                                                     GUIUtils.generateIconTooltip(keyBoardRepresentation, binding.keyAcceptance.toString()),
                                                     new JLabel(!keyBoardRepresentation.isBlank() && !mouseRepresentation.isBlank() ? " + " : ""),
                                                     GUIUtils.generateIconTooltip(mouseRepresentation, binding.mouseAcceptance.toString())
                                     )
                                , BorderLayout.WEST);
                bindingRowPanel.panel.add(bindingRowPanel.bindingCombination, BorderLayout.EAST);
                
                final AtomicBoolean isModifyingInput = new AtomicBoolean(false);
                
                Runnable stopModification = () -> {
                    if (!isModifyingInput.get()) {
                        return;
                    }
                    isModifyingInput.set(false);
                    bindingRowPanel.panel.setFocusable(false);
                    bindingRowPanel.bindingCombination.setText(binding.inputCombination().toString());
                    checkBindings.run();
                };
                Consumer<Input> addInput = input -> {
                    Binding.tryUpdateBinding(binding, input, true, (_) -> {
                    });
                    bindingRowPanel.bindingCombination.setText(binding.inputCombination().toString());
                };
                
                final Collection<Integer> pressedKeys = new HashSet<>();
                MouseAdapter l = new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        if (!isModifyingInput.get()) {
                            return;
                        }
                        addInput.accept(new Input.Click(e.getButton(), e.getClickCount()));
                    }
                    
                    @Override public void mouseReleased(MouseEvent e) {
                        if (!binding.isModifiable()) {
                            return;
                        }
                        if (!isModifyingInput.get()) {
                            switch (e.getButton()) {
                                case MouseEvent.BUTTON1 -> {
                                    isModifyingInput.set(true);
                                    binding.setInputCombination(new InputCombination());
                                    bindingRowPanel.bindingCombination.setText("Reading new combination...");
                                    bindingRowPanel.panel.setBorder(BorderFactory.createLineBorder(GUIColors.Bindings.MODIFYING.getColor(), 2));
                                    bindingRowPanel.panel.setFocusable(true);
                                    bindingRowPanel.panel.requestFocus();
                                    pressedKeys.clear();
                                }
                                case MouseEvent.BUTTON2 -> {
                                    binding.resetInputCombination(new InputCombination());
                                    checkBindings.run();
                                }
                                case MouseEvent.BUTTON3 -> {
                                    JDialog bindingDialog = new JDialog(settingsDialog);
                                    bindingDialog.setTitle("Modifying binding for: " + binding.name);
                                    bindingDialog.setModal(true);
                                    bindingDialog.setLayout(new BorderLayout());
                                    var mouseAndPanel = new BindingPanel(binding);
                                    bindingDialog.add(mouseAndPanel, BorderLayout.CENTER);
                                    bindingDialog.pack();
                                    bindingDialog.setLocationRelativeTo(null);
                                    ComponentUtilities.showDialog(bindingDialog);
                                    checkBindings.run();
                                }
                            }
                        } else {
                            stopModification.run();
                        }
                    }
                    
                    @Override public void mouseEntered(MouseEvent e) {
                        if (!binding.isModifiable()) {
                            return;
                        }
                        bindingRowPanel.panel.setBorder(BorderFactory.createLineBorder(GUIColors.Bindings.HOVER.getColor(), 2));
                    }
                    
                    @Override public void mouseExited(MouseEvent e) {
                        if (!isModifyingInput.get()) {
                            assignPanelColor.accept(binding);
                            return;
                        }
                        stopModification.run();
                    }
                };
                bindingRowPanel.panel.addMouseListener(l);
                bindingRowPanel.panel.addMouseMotionListener(l);
                bindingRowPanel.panel.addMouseWheelListener(e -> {
                    if (!isModifyingInput.get()) {
                        GUIUtils.redispatchMouseEventToParent(e, bindingRowPanel.panel);
                        return;
                    }
                    addInput.accept(new Input.MouseWheel(e.getWheelRotation() < 0));
                    stopModification.run();
                });
                bindingRowPanel.panel.addKeyListener(new KeyListener() {
                    
                    
                    @Override public void keyTyped(KeyEvent e) {
                    }
                    
                    @Override public void keyPressed(KeyEvent e) {
                        if (!pressedKeys.add(e.getKeyCode())) {
                            return;
                        }
                        if (!isModifyingInput.get()) {
                            return;
                        }
                        if (binding.isModifiable() && e.getKeyCode() == KeyEvent.VK_DELETE) {
                            binding.setInputCombination(new InputCombination());
                            stopModification.run();
                            return;
                        }
                        addInput.accept(new Input.Key(e.getKeyCode()));
                    }
                    
                    @Override public void keyReleased(KeyEvent e) {
                        if (!isModifyingInput.get()) {
                            return;
                        }
                        if (!pressedKeys.remove(e.getKeyCode())) {
                            return;
                        }
                        stopModification.run();
                    }
                });
                bindingsToPanels.put(binding, bindingRowPanel);
            }
            
            var tabs = new OMTabbedPane();
            var tabsAndBindings = Bindings.ALL_BINDINGS.stream()
                                                       .collect(Collectors.groupingBy(binding -> binding.tab, LinkedHashMap::new, Collectors.toList()));
            for (var tabAndBinding : tabsAndBindings.entrySet()) {
                var tabPanel = new JPanel(new GridBagLayout());
                var tabConstraints = new SimplifiedGridBagConstraint(tabPanel, new GridBagConstraints(), 1);
                
                var tabName = tabAndBinding.getKey();
                var sectionAndBindings = tabAndBinding.getValue()
                                                      .stream()
                                                      .collect(Collectors.groupingBy(binding -> binding.sectionInTab, LinkedHashMap::new, Collectors.toList()));
                for (var sectionAndBinding : sectionAndBindings.entrySet()) {
                    var sectionName = sectionAndBinding.getKey();
                    if (!sectionName.isBlank()) {
                        tabConstraints
                                .anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                                .weightx(0)
                                .globalFill(SimplifiedGridBagConstraint.Fill.NONE)
                                .add(new JLabel(sectionName));
                    }
                    for (var binding : sectionAndBinding.getValue()) {
                        tabConstraints
                                .anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                                .weightx(1)
                                .globalFill(SimplifiedGridBagConstraint.Fill.HORIZONTAL)
                                .add(bindingsToPanels.get(binding).panel);
                    }
                }
                tabs.addTab(tabName, tabPanel);
            }
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .weightx(1)
                   .weighty(1)
                   .globalFill(SimplifiedGridBagConstraint.Fill.BOTH)
                   .add(tabs);
            checkBindings.run();
        }
        gridbag.addCorrectionGlue();
    }
    
    private static void generateUIVisualSection(JPanel panel) {
        var gridbag = new SimplifiedGridBagConstraint(panel, new GridBagConstraints(), 2);
        
        //UI Scale
        {
            var uiScaleSpinner = new NumericSpinner<>(Double.class);
            uiScaleSpinner.setMinimum(0.5);
            uiScaleSpinner.setMaximum(2.0);
            uiScaleSpinner.setCurrentValueNoListener(UserPreferences.UI_SCALE.get());
            ((JSpinner.DefaultEditor) uiScaleSpinner.getEditor()).getTextField().setColumns(4);
            uiScaleSpinner.setStepSize(0.1);
            
            var uiScaleLabel = new JLabel("UI scale  ");
            var helpToolTip = GUIUtils.generateTooltipElement("<html>Makes UI elements larger/smaller.<br><br>For the scale to take effect, OpenMarkov has to be restarted.</html>");
            
            uiScaleSpinner.addChangeListener(e -> {
                UserPreferences.UI_SCALE.set(uiScaleSpinner.getCurrentValue());
            });
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, uiScaleLabel, helpToolTip))
                   .weighty(0)
                   .weightx(0)
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(uiScaleSpinner);
        }
        
        //UI Theme
        {
            JComboBox<Theme> themeSpinner = new JComboBox<>();
            Arrays.stream(Theme.values()).forEach(themeSpinner::addItem);
            themeSpinner.setSelectedItem(UserPreferences.PREFERRED_THEME.get());
            themeSpinner.setRenderer(new JComboBoxFunctionRender<>(Theme::toUIString));
            
            var themeLabel = new JLabel("UI theme  ");
            var themeToolTip = GUIUtils.generateTooltipElement("""
                                                                       <html>Changes the GUI look and feel to one of the following: <ul>
                                                                       <li>Sync with OS: Uses the Light theme when the OS is on light, and Dark when the OS is on dark.</li>
                                                                       <li>Light: The base light theme.</li>
                                                                       <li>Dark: The base dark theme.</li>
                                                                       <li>System: Takes the look and feel of the OS. Warning: This can cause visual bugs on some OS, and therefore is not recommended.</li>
                                                                       </ul></html>""");
            
            themeSpinner.addItemListener(new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    if (themeSpinner.getSelectedItem() instanceof Theme theme) {
                        UserPreferences.PREFERRED_THEME.set(theme);
                        try {
                            Theme.updateInterfaceToLook();
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                                 UnsupportedLookAndFeelException ex) {
                            throw new UnreachableException(ex);
                        }
                    }
                    
                }
            });
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, themeLabel, themeToolTip))
                   .weighty(0)
                   .weightx(0)
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(themeSpinner);
        }
        
        //Start-up actions
        {
            var restoreDimensionsLabel = new JLabel("Start up actions  ");
            var helpToolTip = GUIUtils.generateTooltipElement("Causes OpenMarkov to take certain behavior when starting the app.");
            
            var optionsPanel = new JPanel();
            optionsPanel.setLayout(new GridBagLayout());
            var optionsLayout = new SimplifiedGridBagConstraint(optionsPanel, new GridBagConstraints(), 1);
            
            for (StartupAction startupAction : StartupAction.values()) {
                var startUpActionCheckBox = new JCheckBox();
                startUpActionCheckBox.setText(startupAction.checkBoxText());
                var startUpActionHelpToolTip = GUIUtils.generateTooltipElement(startupAction.toolTipText());
                startUpActionCheckBox.setSelected(UserPreferences.STARTUP_ACTIONS.get().contains(startupAction));
                startUpActionCheckBox.addItemListener(e -> UserPreferences.STARTUP_ACTIONS.use(startupActions -> {
                    if (startUpActionCheckBox.isSelected()) {
                        startupActions.add(startupAction);
                    } else {
                        startupActions.remove(startupAction);
                    }
                }));
                optionsLayout.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                             .add(ComponentUtilities.joinComponents(FlowLayout.LEADING, startUpActionCheckBox, startUpActionHelpToolTip));
            }
            
            
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, restoreDimensionsLabel, helpToolTip))
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(optionsPanel);
        }
        gridbag.addCorrectionGlue();
    }
    
    private static void generateNetworkVisualSection(JPanel panel) {
        var gridbag = new SimplifiedGridBagConstraint(panel, new GridBagConstraints(), 2);
        
        //Add to recents
        {
            var addToRecentsCheckBox = new JCheckBox();
            var addToRecentsLabel = new JLabel("Add network to recent files when opened  ");
            var helpToolTip = GUIUtils.generateTooltipElement("""
                                                                      <html>
                                                                      When opening a network, it is added to the Recent files list of the OS.<br>
                                                                      This is currently limited to Windows systems.
                                                                      </html>
                                                                      """);
            if (OperatingSystem.CURRENT_OS != OperatingSystem.WINDOWS) {
                addToRecentsCheckBox.setEnabled(false);
                addToRecentsCheckBox.setToolTipText("This option is only available on Windows systems.");
            }
            addToRecentsCheckBox.setSelected(UserPreferences.UPDATE_RECENTS_ON_OPEN_NETWORK.get());
            addToRecentsCheckBox.addItemListener(e -> UserPreferences.UPDATE_RECENTS_ON_OPEN_NETWORK.set(addToRecentsCheckBox.isSelected()));
            
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, addToRecentsLabel, helpToolTip))
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(addToRecentsCheckBox);
        }
        
        //Custom domain
        {
            var customDomainArea = new JTextArea(4, 30);
            
            var restoreDimensionsLabel = new JLabel("Custom domains:  ");
            var helpToolTip = GUIUtils.generateTooltipElement("""
                                                                      <html>
                                                                      Each line represents a custom domain, where states are separated by "-".<br>
                                                                      This allows you to set standard domains that can be assigned in<br>
                                                                      "Network properties -> Variables -> Default states"<br>
                                                                      
                                                                      For example, this is domain "Yes/No" and "Absent/Mild/Moderate/Severe":<br><br>
                                                                      Yes - No<br>
                                                                      Absent - Mild - Moderate - Severe
                                                                      </html>""");
            
            Runnable reloadDomains = () -> customDomainArea.setText(UserPreferences.CUSTOM_DOMAINS.get()
                                                                                                  .stream()
                                                                                                  .map(states -> String.join(" - ", states))
                                                                                                  .collect(Collectors.joining(System.lineSeparator())));
            
            customDomainArea.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                
                }
                
                @Override public void focusLost(FocusEvent e) {
                    var lines = customDomainArea.getText().split("\n");
                    var newDomains = new ArrayList<>(Arrays.stream(lines).map(String::trim)
                                                           .filter(line -> !line.isEmpty())
                                                           .filter(line -> !line.equals("-"))
                                                           .map(line -> new ArrayList<>(Arrays.stream(line.split("-"))
                                                                                              .map(String::trim)
                                                                                              .filter(stateName -> !stateName.isEmpty())
                                                                                              .distinct()
                                                                                              .toList())).toList());
                    UserPreferences.CUSTOM_DOMAINS.set(newDomains);
                    reloadDomains.run();
                }
            });
            reloadDomains.run();
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, restoreDimensionsLabel, helpToolTip))
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(new JScrollPane(customDomainArea));
        }
        gridbag.addCorrectionGlue();
    }
    
    private void generateSettingsBackupVisualSection(JPanel panel) {
        var gridbag = new SimplifiedGridBagConstraint(panel, new GridBagConstraints(), 2);
        
        //Create backup
        {
            var createBackupButton = new JButton("Create config backup file...");
            var createBackupLabel = new JLabel("Create backup  ");
            var toolTip = GUIUtils.generateTooltipElement("""
                                                                  <html>
                                                                  Creates a file that allows to restore your configuration.
                                                                  </html>
                                                                  """);
            
            createBackupButton.addActionListener(e -> {
                var selectedPreferences = new HashSet<UserPreference<?>>();
                if (showPreferenceChooser(gridbag, selectedPreferences, "Preferences to restore", null) != OkCancelDialog.ChosenOption.Ok) {
                    return;
                }
                
                var fileChooser = new OMFileChooser();
                FileFilterByExtension<Void> filter = new FileFilterByExtension<>(null, List.of("json"), "JSON");
                while (fileChooser.getChoosableFileFilters().length > 0) {
                    fileChooser.removeChoosableFileFilter(fileChooser.getChoosableFileFilters()[0]);
                }
                fileChooser.setFileFilter(filter);
                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                var rawPreferences = UserPreferences.getAllPreferences()
                                                    .stream()
                                                    .filter(selectedPreferences::contains)
                                                    .collect(Collectors.toMap(
                                                            UserPreference::getPreferencePath,
                                                            UserPreference::getSerialized)
                                                    );
                try {
                    Files.write(fileChooser.getSelectedFile().toPath(),
                                GsonCommon.GSON.toJson(rawPreferences).getBytes());
                } catch (IOException ex) {
                    throw new UnrecoverableException(ex);
                }
            });
            
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, createBackupLabel, toolTip))
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(createBackupButton);
        }
        
        //Restore backup
        {
            var restoreBackupButton = new JButton("Restore config backup file...");
            var restoreBackupLabel = new JLabel("Restore backup  ");
            var toolTip = GUIUtils.generateTooltipElement("""
                                                                  <html>
                                                                  Restores your configuration from a configuration file.
                                                                  </html>
                                                                  """);
            
            restoreBackupButton.addActionListener(e -> {
                var fileChooser = new OMFileChooser();
                FileFilterByExtension<Void> filter = new FileFilterByExtension<>(null, List.of("json"), "JSON");
                while (fileChooser.getChoosableFileFilters().length > 0) {
                    fileChooser.removeChoosableFileFilter(fileChooser.getChoosableFileFilters()[0]);
                }
                fileChooser.setFileFilter(filter);
                if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                try {
                    Map<List<String>, String> rawPreferences = GsonCommon.GSON.fromJson(
                            Files.readString(fileChooser.getSelectedFile().toPath()),
                            new TypeToken<Map<List<String>, String>>() {
                            }.getType());
                    
                    var selectedPreferences = new HashSet<UserPreference<?>>();
                    if (showPreferenceChooser(gridbag, selectedPreferences, "Preferences to restore", rawPreferences.keySet()
                                                                                                                    .stream()
                                                                                                                    .toList()) != OkCancelDialog.ChosenOption.Ok) {
                        return;
                    }
                    
                    
                    UserPreferences.getAllPreferences()
                                   .stream()
                                   .filter(selectedPreferences::contains)
                                   .forEach(preference -> {
                                       String rawSavedValue = rawPreferences.get(preference.getPreferencePath());
                                       if (rawSavedValue == null) {
                                           return;
                                       }
                                       boolean preferenceChanged = !Objects.equals(preference.getSerialized(), rawSavedValue);
                                       preference.setSerialized(rawSavedValue);
                                       if (preferenceChanged) {
                                           preference.backupInfo().onBackup().run();
                                       }
                                   });
                } catch (IOException ex) {
                    throw new UnrecoverableException(ex);
                }
            });
            
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, restoreBackupLabel, toolTip))
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(restoreBackupButton);
        }
        
        //Restore settings
        {
            var restoreBackupButton = new JButton("Restore default configuration");
            restoreBackupButton.setBackground(GUIColors.General.ATTENTION_BG.getColor());
            restoreBackupButton.setForeground(GUIColors.General.ATTENTION_FG.getColor());
            var restoreBackupLabel = new JLabel("Restore default configuration  ");
            var toolTip = GUIUtils.generateTooltipElement("""
                                                                  <html>
                                                                  Restores your configuration to the default values.
                                                                  </html>
                                                                  """);
            
            restoreBackupButton.addActionListener(e -> {
                OptionDialog<CommonOptions.YesNo> dialog = new OptionDialog<>(MainGUI.INSTANCE,
                                                                              "Non-recoverable operation",
                                                                              """
                                                                                      This operation cannot be undone.
                                                                                      Are you sure you want to restore your configuration to default values?
                                                                                      This will restore more than just the values that you can export and re-import.
                                                                                      """,
                                                                              CommonOptions.YesNo.class);
                if (dialog.request(CommonOptions.YesNo.NO) != CommonOptions.YesNo.YES) {
                    return;
                }
                UserPreferences.getAllPreferences().stream()
                               .filter(UserPreference::isSet)
                               .filter(pref -> pref != UserPreferences.LATEST_MAIN_GUI_DIMENSIONS)
                               .filter(pref -> pref != UserPreferences.UI_SCALE)
                               .forEach(localPreference -> {
                                   localPreference.clear();
                                   if (localPreference.isBackupable()) {
                                       localPreference.backupInfo().onBackup().run();
                                   }
                               });
                MainGUI.INSTANCE.mainPanel.getMainMenu().rechargeFileMenu();
            });
            
            gridbag.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                   .add(ComponentUtilities.joinComponents(FlowLayout.CENTER, restoreBackupLabel, toolTip))
                   .anchor(SimplifiedGridBagConstraint.Anchor.EAST)
                   .add(restoreBackupButton);
        }
        gridbag.addCorrectionGlue();
    }
    
    private OkCancelDialog.ChosenOption showPreferenceChooser(SimplifiedGridBagConstraint gridbag, HashSet<UserPreference<?>> selectedPreferences, String preferencesToRestore, List<List<String>> onlyShowThoseOfPath) {
        var preferenceChooser = new OkCancelDialog(this);
        preferenceChooser.setTitle(preferencesToRestore);
        JPanel preferencesPanel = new JPanel();
        preferencesPanel.setLayout(new GridBagLayout());
        var gridPanel = new SimplifiedGridBagConstraint(preferencesPanel, new GridBagConstraints(), 1);
        
        
        UserPreferences.getAllPreferences()
                       .stream()
                       .filter(UserPreference::isBackupable)
                       .filter(pref -> onlyShowThoseOfPath == null || onlyShowThoseOfPath.contains(pref.getPreferencePath()))
                       .forEach(userPreference -> {
                           var checkbox = new JCheckBox(userPreference.backupInfo().preferenceTitle() + "  ");
                           checkbox.addItemListener(e1 -> {
                               if (checkbox.isSelected()) {
                                   selectedPreferences.add(userPreference);
                               } else {
                                   selectedPreferences.remove(userPreference);
                               }
                               preferenceChooser.getOKButton().setEnabled(!selectedPreferences.isEmpty());
                           });
                           boolean selected = userPreference.backupInfo()
                                                            .backupByDefault() && (userPreference.isSet() || onlyShowThoseOfPath != null);
                           checkbox.setSelected(selected);
                           if (selected) {
                               selectedPreferences.add(userPreference);
                           }
                           gridPanel.anchor(SimplifiedGridBagConstraint.Anchor.WEST)
                                    .add(ComponentUtilities.joinComponents(FlowLayout.LEFT, checkbox, GUIUtils.generateTooltipElement(userPreference.backupInfo()
                                                                                                                                                    .preferenceDescription())));
                       });
        preferenceChooser.getOKButton().setEnabled(!selectedPreferences.isEmpty());
        gridbag.addCorrectionGlue();
        preferenceChooser.add(new JScrollPane(preferencesPanel), BorderLayout.NORTH);
        preferenceChooser.pack();
        preferenceChooser.setMaximumSize(new Dimension(300, 400));
        ComponentUtilities.showDialog(preferenceChooser);
        return preferenceChooser.getSelectedOption();
    }
    
    private static void reloadPreference(UserPreference<?> preference) {
        SwitchUtils.switchInstance(
                preference,
                SwitchUtils.Case.of(UserPreferences.PREFERRED_THEME, (_) -> {
                    try {
                        Theme.updateInterfaceToLook();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                             UnsupportedLookAndFeelException e) {
                    }
                }),
                SwitchUtils.Case.of(UserPreferences.LAST_OPEN_NETWORKS_FILES, (lastOpenNetworksPreference) -> {
                    MainGUI.INSTANCE.mainPanel.getMainMenu().rechargeFileMenu();
                })
        );
        
    }
    
    enum Section {
        UI, NETWORKS, BINDINGS, SETTINGS_BACKUP;
        
        @Nullable SettingsDialog.Section belongsTo() {
            return switch (this) {
                case UI, NETWORKS, BINDINGS, SETTINGS_BACKUP -> null;
            };
        }
        
        @Override public String toString() {
            return switch (this) {
                case UI -> "User Interface";
                case NETWORKS -> "Networks";
                case BINDINGS -> "Bindings";
                case SETTINGS_BACKUP -> "Settings backup";
            };
        }
        
        @NotNull List<Section> children() {
            return Section.CHILDREN_OF_SECTION.get(this);
        }
        
        private static final Map<Section, List<Section>> CHILDREN_OF_SECTION;
        
        static {
            LinkedHashMap<Section, List<Section>> childrenOfSection = new LinkedHashMap<>();
            Arrays.stream(Section.values()).forEach(section -> childrenOfSection.put(section, new ArrayList<>()));
            Arrays.stream(Section.values())
                  .filter(section -> section.belongsTo() != null)
                  .forEach(section -> childrenOfSection.get(section.belongsTo()).add(section));
            CHILDREN_OF_SECTION = Collections.unmodifiableMap(childrenOfSection);
        }
        
    }
    
    
}
