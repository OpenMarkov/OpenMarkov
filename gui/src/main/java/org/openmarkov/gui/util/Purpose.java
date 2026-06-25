/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.PurposeType;

import java.util.List;
import java.util.MissingResourceException;

/**
 * This class is used to encapsulate the purpose of the nodes and their
 * dependent-language strings.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - fix initial values for fields
 */
public class Purpose {
    /**
     * Internal names of the different purposes.
     */
    private static final List<String> LIST = PurposeType.purposeList();
    
    /**
     * It retrieves the dependent-language string of the desired purpose. If the
     * purpose hasn't a dependent-language string (because this purpose isn't a
     * registered one), the returned string is the purpose itself.
     *
     * @param element name of the purpose.
     *
     * @return a string that represents the purpose in the actual language.
     */
    public static String getString(String element) {
        if (element.isEmpty()) {
            return element;
        }
        try {
            return StringDatabase.getUniqueInstance().getString("purpose." + element + ".Text");
        } catch (MissingResourceException e) {
            return element;
        }
    }
    
    /**
     * This method returns an array of strings, each one has the
     * dependent-language string of each purpose.
     *
     * @return an array that contains a list of string that contains the
     * different purposes.
     */
    public static String[] getListStrings(boolean original) {
        if (!original) {
            return Purpose.LIST.stream().map(Purpose::getString).toArray(String[]::new);
        }
        String[] strings = new String[Purpose.LIST.size()];
        Purpose.LIST.toArray(strings);
        return strings;
    }
    
    /**
     * This method returns the index in the list of the purpose passed as
     * parameter. If the parameter doesn't match any element of the list, then
     * the last index is returned.
     *
     * @param element name of the purpose to search.
     *
     * @return the index in the list of the purpose.
     */
    public static int getIndex(String element) {
        int index = Purpose.LIST.indexOf(element);
        if (index == -1) {
            return Purpose.LIST.size() - 1;
        }
        return index;
    }
}
