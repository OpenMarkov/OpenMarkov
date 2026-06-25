/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.menutoolbar.common.ZoomMenuToolBar;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.menutoolbar.menu.MainMenu;
import org.openmarkov.gui.menutoolbar.toolbar.plugin.ToolbarManager;
import org.openmarkov.gui.menutoolbar.toolbar.EditionToolBar;
import org.openmarkov.gui.menutoolbar.toolbar.InferenceToolBar;
import org.openmarkov.gui.menutoolbar.toolbar.StandardToolBar;
import org.openmarkov.gui.window.decisiontree.DecisionTreeEditor;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This is the main panel of the OpenMarkov interface. It contains all the menu
 * items, toolbars, listeners, etc. and manages all the network frames of
 * OepnMarkov.
 *
 * @author jmendoza
 * @version 1.2 asaez	Added InferenceToolBar. Layout changed for having the
 * main and the secondary toolbar in the same line
 */
public class MainPanel extends JPanel {
    
    private static final long serialVersionUID = -7852474978327911654L;
    /**
     * Main menu.
     */
    private final MainMenu mainMenu;
    /**
     * Panel that contains the toolbars.
     */
    private JPanel toolBarPanel = null;
    /**
     * Standard toolbar.
     */
    private StandardToolBar standardToolBar = null;
    /**
     * Edition toolbar.
     */
    private EditionToolBar editionToolBar = null;
    /**
     * Inference toolbar.
     */
    private InferenceToolBar inferenceToolBar = null;
    /**
     * Object that supplies the contextual menus.
     */
    private ContextualMenuFactory contextualMenuFactory = null;
    /**
     * Object that assists this in the management of the menus and toolbars.
     */
    private MainPanelMenuAssistant mainPanelMenuAssistant = null;
    /**
     * Object that listens and manages the user's actions on the menus, contextual
     * menus and toolbars. This object also listens and manages the mdi events.
     */
    private final MainPanelListenerAssistant mainPanelListenerAssistant;
    /**
     * The frame where this panel belongs to.
     */
    private final JFrame mainFrame;
    
    
    public final MainGUI mainGUI;
    
    public AutoScrollableTabbedPane getNetworksTabPanel() {
        return this.networksTabPanel;
    }
    
    
    /**
     * Networks tabs come from here.
     */
    private final AutoScrollableTabbedPane networksTabPanel;
    
    private final ToolbarManager toolbarManager;
    
    /**
     * Creates a new instance with a clear declared parent.
     *
     * @param mainGUI the parent Frame of the Main Panel
     */
    public MainPanel(MainGUI mainGUI) {
        this.setName("MainPanel");
        this.mainGUI = mainGUI;
        mainFrame = mainGUI;
        mainFrame.setName(mainGUI.getName());
        toolbarManager = new ToolbarManager(this);
        this.networksTabPanel = new AutoScrollableTabbedPane(new JTabbedPane());
        
        //Movement for right and left.
        InputMap inputMap = this.networksTabPanel.getjTabbedPane()
                                                 .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(
                KeyEvent.VK_RIGHT,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
        ), "navigateNext");
        inputMap.put(KeyStroke.getKeyStroke(
                KeyEvent.VK_LEFT,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
        ), "navigatePrevious");
        
        this.mainPanelListenerAssistant = new MainPanelListenerAssistant(this);
        this.mainMenu = new MainMenu(this, mainPanelListenerAssistant);
        
        this.networksTabPanel.getjTabbedPane().addChangeListener(e -> {
            var selectedComponent = this.networksTabPanel.getSelectedComponent();
            switch (selectedComponent) {
                case NetworkEditorPanel networkPanel -> {
                    this.getMainPanelMenuAssistant().updateOptionsNetworkDependent(networkPanel);
                    this.getInferenceToolBar().setCurrentEvidenceCaseName(networkPanel.getCurrentCase());
                    this.getMainPanelMenuAssistant().updateOptionsWindowSelected(true);
                    this.getMainPanelMenuAssistant().setOptionEnabled(ActionCommands.CLOSE_TAB, true);
                }
                case DecisionTreeEditor decisionTreeEditor -> {
                    this.getMainPanelMenuAssistant().updateOptionsWindowSelected(false);
                    this.getMainPanelMenuAssistant().updateOptionsDecisionTree(decisionTreeEditor);
                    this.getMainPanelMenuAssistant().setOptionEnabled(ActionCommands.CLOSE_TAB, true);
                }
                case null, default -> {
                }
            }
            this.mainMenu.reInitialize();
        });
        this.initialize();
    }
    
    /**
     * Convenience method to avoid writing
     * {@code MainPanel.getUniqueInstance().getMainPanelListenerAssistant().getCurrentNetworkEditorPanel().getProbNet()}.
     * <p>
     * This method cannot throw NullPointerException.
     *
     * @return The current ProbNet opened in the Main Panel.
     */
    public static @Nullable ProbNet getCurrentProbNet() {
        NetworkEditorPanel currentNetworkEditorPanel = MainPanel.getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel == null) {
            return null;
        }
        return currentNetworkEditorPanel.getProbNet();
    }
    
    /**
     * Convenience method to avoid writing
     * {@code MainPanel.getUniqueInstance().getMainPanelListenerAssistant().getCurrentNetworkEditorPanel()}.
     * <p>
     * This method cannot throw NullPointerException.
     *
     * @return The current NetworkEditorPanel opened in the Main Panel.
     */
    public static @Nullable NetworkEditorPanel getCurrentNetworkEditorPanel() {
        if (MainGUI.INSTANCE == null) {
            return null;
        }
        MainPanel panelInstance = MainGUI.INSTANCE.mainPanel;
        if (panelInstance == null) {
            return null;
        }
        MainPanelListenerAssistant listenerAssistant = panelInstance.getMainPanelListenerAssistant();
        if (listenerAssistant == null) {
            return null;
        }
        return listenerAssistant.getCurrentNetworkEditorPanel();
    }
    
    /**
     * This method initialises this instance, changing the default values and
     * assigning the listeners of the window.
     */
    private void initialize() {
        this.getMainPanelListenerAssistant();
        this.getMainMenu();
        this.getContextualMenuFactory();
        this.setLayout(new BorderLayout());
        int previousWidth = this.getWidth();
        int previousHeight = this.getHeight();
        //Setting the dimensions to 0 causes a refresh when setting them back
        this.setSize(new Dimension(0, 0));
        this.setSize(new Dimension(Math.max(600, previousWidth), Math.max(500, previousHeight)));
        this.add(this.getToolBarPanel(), BorderLayout.NORTH);
        this.getMainPanelMenuAssistant();
        this.add(networksTabPanel.getjTabbedPane(), BorderLayout.CENTER);
    }
    
    /**
     * When this panel is added to a container, it tries to set the menubar of
     * its top level ancestor that must be an instance of the classes JFrame or
     * JApplet.
     */
    @Override public void addNotify() {
        
        JFrame frame;
        
        super.addNotify();
        Component container = this.getTopLevelAncestor();
        if (container != null) {
            if (container instanceof JFrame) {
                frame = (JFrame) container;
                frame.setJMenuBar(this.getMainMenu());
                frame.addWindowListener(mainPanelListenerAssistant);
                frame.addComponentListener(mainPanelListenerAssistant);
//            } else if (container instanceof JApplet) {
//                ((JApplet) container).setJMenuBar(this.getMainMenu());
            }
        }
        
    }
    
    /**
     * This method initialises mainMenu.
     *
     * @return a new menubar.
     */
    public MainMenu getMainMenu() {
        return mainMenu;
    }
    
    /**
     * This method initialises contextualMenuFactory.
     *
     * @return a new contextual menu factory.
     */
    ContextualMenuFactory getContextualMenuFactory() {
        
        if (contextualMenuFactory == null) {
            contextualMenuFactory = new ContextualMenuFactory(mainPanelListenerAssistant);
        }
        
        return contextualMenuFactory;
        
    }
    
    /**
     * This method initialises toolBarPanel.
     *
     * @return a new toolbar panel.
     */
    public JPanel getToolBarPanel() {
        if (toolBarPanel == null) {
            toolBarPanel = new JPanel();
				/* This way, the main toolbar and the secondary are in different lines
				toolBarPanel.setLayout(new BoxLayout(getToolBarPanel(),
					BoxLayout.Y_AXIS));
				toolBarPanel.add(getStandardToolBar());
				toolBarPanel.add(getEditionToolBar());
				*/
            // This way, the main toolbar and the secondary are in the same line
            toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            toolBarPanel.add(this.getStandardToolBar());
            toolBarPanel.add(this.getEditionToolBar());
        }
        return toolBarPanel;
        
    }
    
    /**
     * This method establishes the type of tool bar (Edition or Inference) to be
     * set in the panel.
     *
     * @param barType new type of tool bar to be set in the panel
     */
    protected void setToolBarPanel(NetworkEditorPanel.WorkingMode barType) {
        switch (barType) {
            case EDITION -> {
                getToolBarPanel().remove(getInferenceToolBar());
                getToolBarPanel().add(getEditionToolBar(), 1);
            }
            case INFERENCE -> {
                getToolBarPanel().remove(getEditionToolBar());
                getInferenceToolBar().setExpansionThreshold(this.getMainPanelListenerAssistant().
                                                                getCurrentNetworkEditorPanel()
                                                                .getExpansionThreshold());
                getToolBarPanel().add(getInferenceToolBar(), 1);
            }
        }
        initialize();
        
    }
    
    /**
     * This method sets the button for switching between Edition/inference to
     * the pertinent value (pressed or not)
     *
     * @param workingMode the working mode of the currently selected NetworkEditorPanel.
     *                    Depending on this value, the button will be set pressed or not.
     */
    public void changeWorkingModeButton(NetworkEditorPanel.WorkingMode workingMode) {
        this.getStandardToolBar().changeWorkingModeButton(workingMode);
    }
    
    // 09/09/2019
    /**
     *
     */
    public void selecMonteCarloButton(boolean select){
        this.getStandardToolBar().selectMonteCarloButton(select);
    }
    
    /**
     * This method initialises standardToolBar.
     *
     * @return a new standard toolbar.
     */
    public StandardToolBar getStandardToolBar() {
        
        if (standardToolBar == null) {
            standardToolBar = new StandardToolBar(mainPanelListenerAssistant);
        }
        
        return standardToolBar;
        
    }
    
    /**
     * This method initialises editionToolBar.
     *
     * @return a new edition toolbar.
     */
    public EditionToolBar getEditionToolBar() {
        
        if (editionToolBar == null) {
            editionToolBar = new EditionToolBar(mainPanelListenerAssistant);
        }
        
        return editionToolBar;
        
    }
    
    /**
     * This method initialises and returns the inferenceToolBar.
     *
     * @return a new inference toolbar.
     */
    public InferenceToolBar getInferenceToolBar() {
        if (inferenceToolBar == null) {
            inferenceToolBar = new InferenceToolBar(mainPanelListenerAssistant);
        }
        return inferenceToolBar;
    }
    
    /**
     * This method initialises menuAssistant.
     *
     * @return a new menu assistant.
     */
    public MainPanelMenuAssistant getMainPanelMenuAssistant() {
        
        if (mainPanelMenuAssistant == null) {
            mainPanelMenuAssistant = new MainPanelMenuAssistant(
                    new MenuToolBarBasic[]{mainMenu, standardToolBar, editionToolBar, this.getInferenceToolBar(),
                            contextualMenuFactory}, new ZoomMenuToolBar[]{standardToolBar}, this);
            mainPanelMenuAssistant.updateOptionsAllNetworkClosed();
        }
        
        return mainPanelMenuAssistant;
        
    }
    
    /**
     * This method initialises mainPanelListenerAssistant.
     *
     * @return a new main panel listener assistant.
     */
    public MainPanelListenerAssistant getMainPanelListenerAssistant() {
        return mainPanelListenerAssistant;
    }
    
    /**
     * @return the mainFrame
     */
    public JFrame getMainFrame() {
        
        return mainFrame;
    }
    
    /**
     * Opens a prob net
     *
     * @param fileName the file name
     */
    public void openNetwork(String fileName) throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        this.getMainPanelListenerAssistant().openNetwork(fileName);
    }
    
    /**
     * Returns instance of toolbarManager
     *
     * @return the toolbar manager
     */
    public ToolbarManager getToolbarManager() {
        return toolbarManager;
    }
    
    /**
     * This method checks the size of the components present in the toolbar panel and adapts its size if necessary
     */
    public void adaptToolBarSize() {
        // Variables to store different measures
        int toolBarComponentsWidth;
        int currentNetworkEditorPanelMaxWidth = 600;
        // Variables to adapt the size of the toolbar
        int safetyWidth = 11;
        int safetyHeight = 15;
        // When changing the working mode, sometimes the values of the size of the window are not accurate
        int currentNetworkEditorPanelWidth = Integer.MAX_VALUE;
        if (this.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel() != null) {
            currentNetworkEditorPanelWidth = this.getMainPanelListenerAssistant()
                                                 .getCurrentNetworkEditorPanel()
                                                 .getWidth();
        }
        // We sum the width and height of every component present in the toolbar
        toolBarComponentsWidth = Arrays.stream(this.getToolBarPanel().getComponents())
                                       .mapToInt(Component::getWidth)
                                       .sum();
        
        int maxToolbarComponentHeight = Arrays.stream(this.getToolBarPanel().getComponents())
                                              .mapToInt(Component::getHeight).max()
                                              .getAsInt();
        
        // If the toolbar cannot show them in one single line
        if ((this.getToolBarPanel().getWidth() < toolBarComponentsWidth + safetyWidth) || (
                currentNetworkEditorPanelWidth < currentNetworkEditorPanelMaxWidth
        )) {
            // we increase the height of the toolbar accordingly
            this.getToolBarPanel()
                .setPreferredSize(new Dimension(this.getWidth() + safetyWidth, maxToolbarComponentHeight + safetyHeight));
        }
        // and if the toolbar can show them in one line
        else {
            // we request the Layout manager to choose the preferred size
            this.getToolBarPanel().setPreferredSize(null);
        }
    }
    
    private static class TabHeader extends JPanel {
        
        private final JLabel titleLabel;
        private final JButton closeButton;
        
        TabHeader(String title) {
            super(new FlowLayout());
            this.setFocusable(false);
            this.setOpaque(false);
            
            this.titleLabel = new JLabel(title);
            this.add(titleLabel);
            
            this.closeButton = new JButton("✖");
            closeButton.setFocusable(false);
            closeButton.setMargin(new Insets(0, 0, 0, 0));
            this.add(closeButton);
        }
    }
    
    public void forceClose(NetworkEditorPanel networkPanel){
        this.networksTabPanel.remove(networkPanel);
    }
    
    public void addCloseableTab(String title, EditorPanel component) {
        var uniqueTitle = getUniqueTitle(title, null);
        this.networksTabPanel.addTab(uniqueTitle, component.getScrollPanel());
        
        TabHeader header = new TabHeader(uniqueTitle);
        header.closeButton.addActionListener(e -> component.close());
        this.networksTabPanel.setTabComponentAt(this.networksTabPanel.getTabCount() - 1, header);
        Component tabComponent = this.networksTabPanel.getTabComponentAt(this.networksTabPanel.getTabCount() - 1);
        
        if (component instanceof NetworkEditorPanel networkPanel) {
            Consumer<NetworkEditorPanel> reloadNamesAndColor = networkP -> {
                if (networkP.getModified()) {
                    header.titleLabel.setForeground(GUIColors.General.ATTENTION.getColor());
                } else {
                    header.titleLabel.setForeground(null);
                }
                String uniqueTitleOnChange = this.getUniqueTitle(networkPanel.getProbNet()
                                                                             .getName(), Set.of(this.networksTabPanel.indexOfTabComponent(tabComponent)));
                header.titleLabel.setText(uniqueTitleOnChange);
                networkP.getEditorPanel().updateName(uniqueTitleOnChange);
            };
            networkPanel.addOnModification(reloadNamesAndColor);
            reloadNamesAndColor.accept(networkPanel);
        }
        
        tabComponent.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
            
            }
            
            @Override public void mousePressed(MouseEvent e) {
                MainPanel mainPanel = MainPanel.this;
                int tabIndex = mainPanel.networksTabPanel.indexOfTabComponent(tabComponent);
                switch (e.getButton()) {
                    //LEFT_CLICK
                    case 1 -> {
                        mainPanel.networksTabPanel.setSelectedIndex(tabIndex);
                    }
                    //RIGHT_CLICK
                    case 3 -> {
                        JPopupMenu tabContextMenu = new JPopupMenu();
                        
                        if (component instanceof NetworkEditorPanel networkPanel) {
                            JMenuItem saveButton = new JMenuItem("Save");
                            saveButton.addActionListener(e1 -> {
                                try {
                                    MainPanel.this.mainPanelListenerAssistant.saveNetwork(networkPanel);
                                } catch (WriterException ex) {
                                    throw new UnrecoverableException(ex);
                                }
                            });
                            tabContextMenu.add(saveButton);
                            JMenuItem saveAsButton = new JMenuItem("Save as");
                            saveAsButton.addActionListener(e1 -> {
                                try {
                                    MainPanel.this.mainPanelListenerAssistant.saveNetworkAs(networkPanel);
                                } catch (WriterException ex) {
                                    throw new UnrecoverableException(ex);
                                }
                            });
                            tabContextMenu.add(saveAsButton);
                            
                        }
                        
                        JMenuItem closeThisTab = new JMenuItem("Close this tab");
                        closeThisTab.addActionListener(e2 -> multiClose(List.of(tabIndex)));
                        tabContextMenu.add(closeThisTab);
                        
                        
                        tabContextMenu.add(new JSeparator());
                        JMenuItem closeAllTab = new JMenuItem("Close all tabs");
                        closeAllTab.addActionListener(e2 -> closeAllTabs());
                        tabContextMenu.add(closeAllTab);
                        
                        JMenuItem closeAllTabsButThis = new JMenuItem("Close all tabs but this");
                        closeAllTabsButThis.addActionListener(e2 -> multiClose(Stream.concat(
                                IntStream.range(0, tabIndex).boxed(),
                                IntStream.range(tabIndex + 1, MainPanel.this.networksTabPanel.getTabCount()).boxed()
                        ).toList()));
                        tabContextMenu.add(closeAllTabsButThis);
                        JMenuItem closeTabsToTheLeft = new JMenuItem("Close tabs to the left");
                        closeTabsToTheLeft.addActionListener(e2 -> {
                            multiClose(IntStream.range(0, tabIndex).boxed().toList());
                        });
                        tabContextMenu.add(closeTabsToTheLeft);
                        JMenuItem closeTabsToTheRight = new JMenuItem("Close tabs to the right");
                        closeTabsToTheRight.addActionListener(e2 -> {
                            multiClose(IntStream.range(tabIndex + 1, MainPanel.this.networksTabPanel.getTabCount())
                                                .boxed()
                                                .toList());
                        });
                        tabContextMenu.add(closeTabsToTheRight);
                        tabContextMenu.show(tabComponent, e.getX(), e.getY());
                    }
                }
            }
            
            @Override public void mouseReleased(MouseEvent e) {
            
            }
            
            @Override public void mouseEntered(MouseEvent e) {
            
            }
            
            @Override public void mouseExited(MouseEvent e) {
            
            }
        });
    }
    
    public boolean closeAllTabs() {
        List<Integer> tabsToClose = IntStream.range(0, MainPanel.this.networksTabPanel.getTabCount())
                                             .boxed()
                                             .toList();
        var closedTabs = multiClose(tabsToClose);
        return tabsToClose.size() == closedTabs.size();
    }
    
    public ArrayList<Integer> multiClose(List<Integer> tabIndexesToClose) {
        tabIndexesToClose = tabIndexesToClose.stream().distinct().sorted(Comparator.reverseOrder()).toList();
        ArrayList<Integer> closedTabs = new ArrayList<>(tabIndexesToClose.size());
        int initialTab = MainPanel.this.networksTabPanel.getSelectedIndex();
        boolean initialTabClosed = false;
        for (int tabIndexToClose : tabIndexesToClose) {
            MainPanel.this.networksTabPanel.setSelectedIndex(tabIndexToClose);
            if (!((EditorPanel) MainPanel.this.networksTabPanel.getSelectedComponent()).close()) {
                return closedTabs;
            }
            closedTabs.add(tabIndexToClose);
            initialTabClosed = initialTabClosed || initialTab == tabIndexToClose;
        }
        if (!initialTabClosed) {
            MainPanel.this.networksTabPanel.setSelectedIndex(initialTab);
        }
        return closedTabs;
    }
    
    private String getUniqueTitle(String title, @Nullable Set<Integer> tabIndexesToSkip) {
        String uniqueTitle = title;
        IntStream indexes = IntStream.range(0, this.networksTabPanel.getTabCount());
        if (tabIndexesToSkip != null && !tabIndexesToSkip.isEmpty()) {
            indexes = indexes.filter(index -> !tabIndexesToSkip.contains(index));
        }
        var presentNames = indexes
                .mapToObj(index -> ((TabHeader) this.networksTabPanel.getTabComponentAt(index)).titleLabel.getText())
                .collect(Collectors.toSet());
        int appendedIndex = 2;
        while (presentNames.contains(uniqueTitle)) {
            uniqueTitle = title + " (" + appendedIndex + ")";
            appendedIndex++;
        }
        return uniqueTitle;
    }
    
}