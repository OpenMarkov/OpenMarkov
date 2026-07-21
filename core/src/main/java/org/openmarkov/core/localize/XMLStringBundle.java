/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.MissingResourceException;
import java.util.Set;

/**
 * This class contains a ResourceBundle object and limits the access to this
 * object.
 *
 * @author jmendoza 1.0
 * @author jlgozalo 1.1
 * @author jlgozalo jrico 1.2
 * @version 1.2 always return a String. If the key is not found, return a blank
 * string to avoid stopping OPENMARKOV
 */
public class XMLStringBundle implements StringBundle {
    
    /**
     * Underlying resource.
     */
    private final XMLResourceBundle resourceBundle;
    
    /**
     * Default constructor. It saves the reference to a resource bundle.
     *
     * @param newResourceBundle underlying resource bundle.
     */
    public XMLStringBundle(XMLResourceBundle newResourceBundle) {
        this.resourceBundle = newResourceBundle;
    }
    
    /**
     * This method returns the requested string resource. If the key does not
     * exist then a "virtual" string resource is returned to avoid the program
     * to be stopped
     *
     * @param key the key of the desired string.
     * @return the string associated with the key. if the resource doesn't
     * exist, then a special string is returned.
     */
    @Override public @Nullable String getString(String key) {
        return this.resourceBundle.containsKey(key)?this.resourceBundle.getString(key):null;
    }
    
    @Override public @NotNull Set<String> getKeys() {
        return this.resourceBundle.getStringKeys();
    }
    
    /**
     * Print the information of this object
     *
     * @return the information of the object
     */
    @Override public String toString() {
        
        return "["
                + this.getClass().getName()
                + ":\nResourcebundle ="
                + this.resourceBundle.toString() +
                "]";
    }
    
}
