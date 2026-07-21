/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.toolplugin;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manager class for tool plugins in OpenMarkov.
 * <p>
 * This class is responsible for locating, loading, and managing all tool plugins
 * defined with the {@link ToolPlugin} annotation. It dynamically generates
 * corresponding menu items and executes the selected plugins at runtime.
 * </p>
 *
 * <p>This class implements the Singleton pattern.</p>
 *
 * @author unknown
 * @version 1.1 jrico. Removed the old use of {@code actionCommand}. Simplified class without changing behavior.
 * Adapted to version 1.1 of {@link ToolPlugin}.
 */
public final class ToolPluginManager {
    
    /**
     * Singleton instance of the plugin manager.
     */
    @NotNull
    private static final ToolPluginManager INSTANCE = new ToolPluginManager();
    
    /**
     * List of loaded tool plugins.
     */
    private final List<? extends ToolPlugin> plugins;
    
    /**
     * Private constructor.
     * Initializes the plugin loader and populates the plugin map
     * with all available tool plugins.
     */
    private ToolPluginManager() {
        this.plugins = ToolPluginManager
                .findAllToolPlugins()
                .map(toolPluginClass -> {
                    try {
                        Constructor<? extends ToolPlugin> noArgsConstructors = toolPluginClass.getDeclaredConstructor();
                        noArgsConstructors.setAccessible(true);
                        return noArgsConstructors.newInstance();
                    } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                             InvocationTargetException e) {
                        throw new UnreachableException(e);
                    }
                })
                //.sorted(Comparator.comparing(ToolPlugin::menuOptionText))
                .toList();
    }
    
    /**
     * Returns the singleton instance of the plugin manager.
     *
     * @return The unique instance of {@code ToolPluginManager}.
     */
    public static ToolPluginManager getInstance() {
        return ToolPluginManager.INSTANCE;
    }
    
    
    /**
     * Finds all Plugin Tools classes.
     *
     * @return a list of Plugin Tools classes.
     */
    private static Stream<Class<? extends ToolPlugin>> findAllToolPlugins() {
        return PluginSearch.init().childrenOf(ToolPlugin.class).stream();
    }
    
    public List<? extends ToolPlugin> getAllToolPlugins() {
        return this.plugins;
    }
    
}
