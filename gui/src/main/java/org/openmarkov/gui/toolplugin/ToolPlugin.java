/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.toolplugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.*;

@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
/**
 * This interface is to be implemented to create a functionality that can be accessed in the {@code Tools} bar of
 * OpenMarkov's interface, where you'll find a menu item you can click to trigger the functionality of this {@code tool
 * plugin}.
 * <p>
 * This interface is implemented for some classes in plugin repositories, such as {@code costEffectiveness} or
 * {@code dbGenerator}.
 * Hence, the name {@code Tool Plugin}.
 * <p>
 * The methods that you can override are:
 * <ul>
 *   <li>{@link ToolPlugin#menuOptionText()} this returns the visual foreground, and it is the same foreground that will show for
 *   this {@code tool plugin}'s menu item in the {@code Tools} bar</li>
 *   <li>{@link ToolPlugin#mnemonic()} this is a shortcut for easily accessing this {@code tool plugin}. You can return
 *   {@code null} if there is no mnemonic.</li>
 *   <li>{@link ToolPlugin#showDialog(JFrame)} this is executed when the user clicks on the menu item for this
 *   {@code tool plugin}.
 *   It is where you should write how the user interacts with the plugin</li>
 * </ul>
 *
 * @author unknown
 * @version 1.1 jrico Simplified interface by removing the old of Localization. Added documentation.
 */
public interface ToolPlugin {
    
    JMenuItem toMenuItem();
    
    @NotNull ToolPluginGroup pluginGroup();
    
    int priorityInGroup();
    
    enum ToolPluginGroup {
        ANALYSIS,
        PROCESSING,
        EXPORT,
        USER_EXPERIENCE,
        UNCATEGORIZED;
    }
    
    public static @NotNull JMenuItem commonToJMenuItem(CommonToJMenuItem info) {
        
        
        JMenuItem menuItem = new JMenuItem();
        menuItem.addActionListener(e -> {
            try {
                info.onClickAction().accept(MainGUI.INSTANCE.mainPanel.getMainFrame());
            } catch (UnrecoverableException | UnreachableException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new UnrecoverableException(ex);
            }
        });
        menuItem.setText(info.title());
        var mnemonic = info.mnemonic();
        if (mnemonic != null) {
            menuItem.setMnemonic(mnemonic);
        }
        menuItem.setEnabled(info.enabled());
        return menuItem;
    }
    
    record CommonToJMenuItem(
            @NotNull String title,
            @Nullable Character mnemonic,
            boolean enabled,
            @NotNull ThrowingConsumer<JFrame, ? extends Exception> onClickAction
    ) {
    }
    
    @FunctionalInterface
    interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }
}
