/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.core.localize.LocaleChangeEvent;
import org.openmarkov.core.localize.LocaleChangeListener;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.layout.radial.RadialLayout;
import org.openmarkov.gui.layout.radial.RadialPanel;
import org.openmarkov.gui.localize.UpdateLocalizationInComponents;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasicImpl;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is used to set the common features of all contextual menus of the
 * application.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.1 adding StringResourceLocaleChangeListener
 */
public abstract class ContextualMenu extends JPopupMenu implements MenuToolBarBasic, LocaleChangeListener {
    
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -792738738895619891L;
    /**
     * Object that listen to the user's actions.
     */
    protected final ActionListener listener;
    
    /**
     * This method initialises this instance.
     *
     * @param newListener listener that listen to the user's actions.
     */
    ContextualMenu(ActionListener newListener) {
        
        super();
        listener = newListener;
        StringDatabase.getUniqueInstance().addLocaleChangeListener(this);
        
    }
    
    /**
     * Returns the component that correspond to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    protected abstract JComponent getJComponentActionCommand(String actionCommand);
    
    /**
     * Enables or disabled an option identified by an action command.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to enable the option, false to disable.
     */
    @Override public void setOptionEnabled(String actionCommand, boolean b) {
        MenuToolBarBasicImpl.setOptionEnabled(getJComponentActionCommand(actionCommand), b);
    }
    
    /**
     * Enables or disabled an option identified by an action command.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to enable the option, false to disable.
     */
    public void setOptionEnabled(ActionCommands actionCommand, boolean b) {
        setOptionEnabled(actionCommand.getCommandName(), b);
    }
    
    /**
     * Selects or unselects an option identified by an action command. Only
     * selects or unselects the components that are AbstractButton.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to select the option, false to unselect.
     */
    @Override public void setOptionSelected(String actionCommand, boolean b) {
        
        MenuToolBarBasicImpl.setOptionSelected(getJComponentActionCommand(actionCommand), b);
        
        if (getJComponentActionCommand(actionCommand) instanceof JCheckBoxMenuItem) {
            ((JCheckBoxMenuItem) getJComponentActionCommand(actionCommand)).setState(b);
        }
    }
    
    /**
     * Adds a foreground to the label of an option identified by an action command.
     * Only adds a foreground to the components that are AbstractButton.
     *
     * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
     *                      added.
     */
    @Override public void addOptionText(String actionCommand, String text) {
    
    }
    
    private Component invoker;
    private int relativeShownLocationX;
    private int relativeShownLocationY;
    
    @Override public Component getInvoker() {
        return this.invoker;
    }
    
    public int getRelativeShownLocationX() {
        return this.relativeShownLocationX;
    }
    
    public int getRelativeShownLocationY() {
        return this.relativeShownLocationY;
    }
    
    @Override public void show(Component invoker, int x, int y) {
        this.invoker = invoker;
        this.relativeShownLocationX = x;
        this.relativeShownLocationY = y;
        AtomicBoolean holdsRightClick = new AtomicBoolean(true);
        MouseListener rightClickListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    holdsRightClick.set(true);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    holdsRightClick.set(false);
                }
            }
        };
        invoker.addMouseListener(rightClickListener);
        Timer delayedAction = new Timer(300, e -> {
            invoker.removeMouseListener(rightClickListener);
            if (!holdsRightClick.get() || !tryShowRadialFastMenu(invoker, x, y)) {
                super.show(invoker, x, y);
            }
        });
        delayedAction.setRepeats(false);
        delayedAction.start();
    }
    
    private boolean tryShowRadialFastMenu(Component invoker, int x, int y) {
        var components = ComponentUtilities
                .flatComponentsAsStream(this, ComponentUtilities.DEFAULT_COMPONENT_SEARCH_OPTIONS)
                .filter(component -> component instanceof JMenuItem)
                .map(component -> (JMenuItem) component)
                .filter(component -> component.getIcon() != null)
                .toList();
        if (components.isEmpty()) {
            return false;
        }
        JPopupMenu horizontalMenu = new JPopupMenu();
        horizontalMenu.setLightWeightPopupEnabled(true);
        horizontalMenu.setOpaque(false);
        horizontalMenu.setBorder(null); // Optional: Borders often stay opaque otherwise
        horizontalMenu.setBackground(GUIColors.General.TRANSPARENT.getColor());
        horizontalMenu.setBorder(BorderFactory.createEmptyBorder());
        horizontalMenu.putClientProperty("Popup.dropShadowPainted", false);
        
        RadialPanel radialPanel = new RadialPanel(new RadialLayout(270, 8));
        radialPanel.setOpaque(false);
        radialPanel.setBackground(GUIColors.General.TRANSPARENT.getColor());
        
        AtomicReference<JButton> selectedButton = new AtomicReference<>(null);
        AtomicReference<Popup> tipPopup = new AtomicReference<>(null);
        AtomicReference<Timer> tipShower = new AtomicReference<>(null);
        MouseAdapter trackSelectedItem = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton button = (JButton) e.getSource();
                String tipText = button.getToolTipText(e);
                if (tipText != null) {
                    // 2. Create the actual tooltip component
                    JToolTip tip = button.createToolTip();
                    tip.setTipText(tipText);
                    
                    // 3. Calculate position (relative to screen)
                    Point p = button.getLocationOnScreen();
                    
                    // 4. Manually create and show the popup
                    PopupFactory factory = PopupFactory.getSharedInstance();
                    tipShower.set(new Timer(500, timedEvent -> {
                        tipPopup.set(factory.getPopup(button, tip, p.x + e.getX(), p.y + e.getY() + 20));
                        tipPopup.get().show();
                    }));
                    tipShower.get().setRepeats(false);
                    tipShower.get().start();
                }
                selectedButton.set(button);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                selectedButton.set(null);
                if (tipShower.get() != null) {
                    tipShower.get().stop();
                    tipShower.set(null);
                }
                if (tipPopup.get() != null) {
                    tipPopup.get().hide();
                }
            }
        };
        
        components.stream().map(menuItem -> {
            var button = new JButton(menuItem.getIcon());
            button.setMargin(new Insets(2, 2, 2, 2));
            button.setFocusable(false);
            button.addActionListener((ev) -> menuItem.doClick());
            button.setEnabled(menuItem.isEnabled());
            button.setActionCommand(menuItem.getActionCommand());
            button.setName(menuItem.getName());
            button.addMouseListener(trackSelectedItem);
            button.addMouseMotionListener(trackSelectedItem);
            button.setBackground(GUIColors.FastMenu.OPTION_BACKGROUND.getColor());
            button.setToolTipText(menuItem.getToolTipText() != null ? menuItem.getToolTipText() : menuItem.getText());
            return button;
        }).forEach(radialPanel::add);
        
        AtomicReference<MouseAdapter> action = new AtomicReference<>();
        action.set(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (tipShower.get() != null) {
                        tipShower.get().stop();
                        tipShower.set(null);
                    }
                    if (tipPopup.get() != null) {
                        tipPopup.get().hide();
                    }
                    if (selectedButton.get() != null) {
                        selectedButton.get().doClick();
                    }
                    if (horizontalMenu.isVisible()) {
                        horizontalMenu.setVisible(false);
                    }
                    invoker.removeMouseListener(action.get());
                    invoker.removeMouseMotionListener(action.get());
                }
            }
        });
        invoker.addMouseListener(action.get());
        invoker.addMouseMotionListener(action.get());
        
        horizontalMenu.add(radialPanel);
        horizontalMenu.show(invoker, x - (horizontalMenu.getPreferredSize().width / 2), y - (horizontalMenu.getPreferredSize().height / 2));
        return true;
    }
    
    /**
     * process a change in the String Resource Locale, settings all the labels
     * menus, and strings in the component to the new selected language
     */
    @Override public void processLocaleChange(LocaleChangeEvent event) {
        UpdateLocalizationInComponents.allComponentsUpdateSetText(this);
        repaint();
    }
    
    /**
     * Changes the foreground of menu item
     *
     * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label.
     */
    @Override public void setText(String actionCommand, String text) {
        
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.setText(component, text);
        
    }
    
}
