/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.menu;

import org.openmarkov.core.exception.UnreachableException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

/**
 * The table stores the defined menu handlers associated to the menu items. Even
 * if there is any element that has not an already proper element (default
 * settings), try to find the corresponding class. If not, there will be an
 * error
 *
 * @author jlgozalo
 * @version 1.0 06/12/2008
 */
public class MenuHandlersTable {
    

    /**
     * htMenuItemHandlers table to store the set of menu handlers
     */
    private static Hashtable<Object, MenuItemHandler> htMenuItemHandlers;
    
    /**
     * constructor
     */
    
    private MenuHandlersTable() {
        
        htMenuItemHandlers = new Hashtable<Object, MenuItemHandler>();
    }
    
    /**
     * Register a menu item handler in the handler table by name
     *
     * @param sHandlerName the name of the handler for the element
     * @param mih          the MenuItem handler
     */
    public static void registerMenuItemHandler(String sHandlerName, MenuItemHandler mih) {
        
        htMenuItemHandlers.put(sHandlerName, mih);
    }
    
    /**
     * Register a menu item handler in the handler table by menu item
     *
     * @param mi  the menu item
     * @param mih the handler for the menu item
     */
    public static void registerMenuItemHandler(JMenuItem mi, MenuItemHandler mih) {
        
        htMenuItemHandlers.put(mi, mih);
    }
    
    /**
     * Given a MenuItem, return its handler
     *
     * @param mi the menu item which handler we are looking for
     * @return the menu item handler
     */
    public static MenuItemHandler menuitemhandlerFind(JMenuItem mi) {
        return htMenuItemHandlers.get(mi);
    }
    
    /**
     * Get a MenuItemHandler by name
     *
     * @param sName the name of the menu item handler we are looking for
     * @return the menu item handler
     */
    public static MenuItemHandler menuitemhandlerFind(String sName) {
        
        if (sName == null) {
            return null;
        }
        MenuItemHandler mih = htMenuItemHandlers.get(sName);
        
        if (mih != null) return mih;
        
        // Not registered. See if it's a class name, and if it is, create an
        // instance of that class and register it.
        try {
            Class<?> classOfHandler = Class.forName(sName);
            MenuItemHandler newHandler = (MenuItemHandler) classOfHandler.getDeclaredConstructor().newInstance();
            registerMenuItemHandler(sName, newHandler);
            return newHandler;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new UnreachableException(e);
        }
    }
}
