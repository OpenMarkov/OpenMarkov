/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar.plugin;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.menutoolbar.toolbar.ToolBarBasic;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.plugin.PluginSearch;

import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Discovers and manages toolbar plugins annotated with {@link Toolbar}.
 * Toolbars are instantiated reflectively and added to the main panel's toolbar area.
 */
public class ToolbarManager {
    private final MainPanel mainPanel;
    private final Map<String, Class<? extends ToolBarBasic>> toolbarClasses;
    private final List<String> activeToolbars = new ArrayList<>();

    /**
     * Creates a new toolbar manager and discovers all available toolbar plugins.
     *
     * @param mainPanel the main panel to which toolbars will be added
     */
    public ToolbarManager(MainPanel mainPanel) {
        toolbarClasses = new HashMap<>();
        this.mainPanel = mainPanel;
        findAllToolbars().forEach(toolbarClass->{
            Toolbar toolbar = toolbarClass.getAnnotation(Toolbar.class);
            this.toolbarClasses.put(toolbar.name(), toolbarClass);
        });
    }
    
    /**
     * Activates a toolbar by name. If the toolbar is not already active,
     * it is instantiated and added to the main panel.
     *
     * @param name the name of the toolbar plugin to activate
     */
    public void addToolbar(String name) {
        ToolBarBasic instance = null;
        if (!activeToolbars.contains(name)) {
            if (toolbarClasses.containsKey(name)) {
                try {
                    Constructor<?> constructor = toolbarClasses.get(name).getConstructor(ActionListener.class);
                    instance = (ToolBarBasic) constructor.newInstance(mainPanel.getMainPanelListenerAssistant());
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new UnreachableException(e);
                }
            }
            mainPanel.getToolBarPanel().add(instance);
        }
        activeToolbars.add(name);
    }
    
    /**
     * This method gets all the plugins with Toolbar annotations
     *
     * @return a list with the plugins detected with Toolbar annotations.
     */
    private static @NotNull Stream<Class<? extends ToolBarBasic>> findAllToolbars() {
        return PluginSearch.init()
                           .annotatedWith(Toolbar.class)
                           .childrenOf(ToolBarBasic.class)
                           .stream();
    }
}
