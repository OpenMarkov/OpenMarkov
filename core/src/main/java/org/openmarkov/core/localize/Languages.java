/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

/**
 * This class is used to encapsulate the languages supported by OPENMARKOV
 *
 * @author jlgozalo
 * @version 1.0
 */
public class Languages {
    
    /**
     * Internal names of the different languages
     */
    private static final List<String> LIST = Arrays.asList("Languages.English", "Languages.Spanish");
    
    /**
     * It retrieves the dependent-language string of the desired language. If
     * the language hasn't a dependent-language string (because this language
     * isn't a registered one), the returned string is the language itself.
     *
     * @param element name of the language
     *
     * @return a string that represents the language name in the actual language
     * bundle
     */
    @ToCheck(reasonKind = ToCheck.ReasonKind.CODE_QUALITY,
            reasonDescription = "The return value of the try catch block seems suspicious")
    public static String getString(String element) {
        if (element.isEmpty()) {
            return element;
        }
        try {
            return StringDatabase.getUniqueInstance().getString(element + ".LongName.Text");
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
    public static String[] getStringList() {
        int i;
        int l = LIST.size();
        String[] strings = new String[l];
        for (i = 0; i < l; i++) {
            strings[i] = getString(LIST.get(i));
        }
        return strings;
    }
    
    /**
     * This method returns the language sited in the specified index in the
     * list. If the index is out of range (index &lt; 0 || index &gt; list size) the
     * null is returned.
     *
     * @param index element of the list of language.
     *
     * @return a string that contains the name of the specified language.
     */
    public static String getByIndex(int index) {
        return LIST.get(index);
    }
    
    /**
     * This method returns the index in the list of the language passed as
     * parameter. If the parameter doesn't match any element of the list, then
     * the last index is returned.
     *
     * @param element name of the language to search.
     *
     * @return the index in the list of the language.
     */
    public static int getIndex(String element) {
        int index = LIST.indexOf(element);
        if (index == -1) {
            index = LIST.size() - 1;
        }
        return index;
    }
    
    /**
     * It retrieves the dependent-language short string of the desired language.
     * If the language hasn't a dependent-language string (because this language
     * isn't a registered one), the returned string is the language itself.
     *
     * @param element name of the language
     *
     * @return a short string that represents the language name in the actual
     * language bundle
     */
    public static String getShortString(String element) {
        if (element.isEmpty()) {
            return element;
        }
        try {
            return StringDatabase.getUniqueInstance().getString(element + ".ShortName.Text");
        } catch (MissingResourceException e) {
            return element;
        }
    }
    
    /**
     * This method returns the language sited in the specified index in the
     * short name list. If the index is out of range (index &lt; 0 || index &gt; list
     * size) the null is returned.
     *
     * @param index element of the short name list of language.
     *
     * @return a string that contains the short name of the specified language.
     */
    public static String getShortNameByIndex(int index) {
        return StringDatabase.getUniqueInstance().getString(LIST.get(index) + ".ShortName.Text");
    }
    
    /**
     * This method prints an array of strings, each one has the
     * dependent-language string of each language
     *
     * @return an array that contains a list of string that contains the
     * different purposes.
     */
    public String toString() {
        int i;
        int l = LIST.size();
        String strings = "Languages = ";
        for (i = 0; i < l; i++) {
            strings = strings + "\n" + getString(LIST.get(i));
        }
        return strings + "\n";
    }
}