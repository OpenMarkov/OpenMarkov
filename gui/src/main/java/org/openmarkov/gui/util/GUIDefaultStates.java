/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.openmarkov.core.model.network.DefaultStates;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.localize.StringDatabase;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is used to encapsulate the default states of the nodes and their
 * dependent-language strings.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - fix javadoc and initial values for fields
 */
public class GUIDefaultStates extends DefaultStates {
	/**
	 * It retrieves the dependent-language string of the desired state. If the
	 * state hasn't a dependent-language string (because this state isn't a
	 * default one), the returned string is the state itself.
	 *
	 * @param element name of the state.
	 * @return a string that represents the state in the actual language.
	 */
	public static String getString(String element) {
		// try {
		String newKey = "defaultStates." + element + ".Text";
		if (StringDatabase.getUniqueInstance().getString(newKey).equals(StringDatabase.surrondAsUnknown(newKey)))
			return element;
        return StringDatabase.getUniqueInstance().getString(newKey);
        // } catch (MissingResourceException e) {
		// return element;
		// }
	}

	/**
	 * Returns a string concatenating all the states language-dependent string
	 * separated by dashes. If the array of states passed as parameter is empty,
	 * then a string representing others states is returned.
	 *
	 * @param elements states whose language-dependent strings are concatenated.
	 * @return a string formed by the language-dependent strings of the states.
	 */
	public static String getString(String[] elements) {
        int l = elements.length;
		String result = "";
		if ((l == 0) || ((l == 1) && elements[0].equals("nonamed"))) {
			result = StringDatabase.getUniqueInstance().getString("defaultStates.others.Text");
		} else {
			l--;
            for (int i = 0; i < l; i++) {
				result += getString(elements[i]) + " - ";
			}
			result += getString(elements[l]);
		}
		return result;
	}

	/**
	 * It retrieves the dependent-language strings of the desired states. If a
	 * state hasn't a dependent-language string (because this state isn't a
	 * default one), the returned string is the state itself.
	 *
	 * @param elements names of the states.
	 * @return an array that contains the strings that represent the states in
	 * the actual language.
	 */
	public static String[] getStrings(State[] elements) {
        int l = elements.length;
		String[] result = new String[l];
        for (int i = 0; i < l; i++) {
			result[i] = getString(elements[i].getName());
		}
		return result;
	}

	/**
	 * Returns the state corresponding to its language-dependent string.
	 *
	 * @param languageDependentString language-dependent string corresponding to
	 *                                the state.
	 * @return the name of the state or the string passed as parameter if any
	 * state corresponds to the language-dependent string.
	 */
	public static String getStringLanguageDependent(String languageDependentString) {
		boolean found = false;
        String result = null;
        int i1 = 0;
        int length1 = LIST.size();
        while (!found && (i1 < length1)) {
            ArrayList<String> elements = LIST.get(i1);
            int i2 = 0;
            int length2 = elements.size();
            while (!found && (i2 < length2)) {
				if (languageDependentString.equals(getString(elements.get(i2)))) {
					result = elements.get(i2);
					found = true;
				} else {
					i2++;
				}
			}
			i1++;
		}
		return (found) ? result : languageDependentString;
	}

	/**
	 * Returns the states corresponding to their language-dependent strings.
	 *
	 * @param languageDependentStrings language-dependent strings corresponding
	 *                                 to the states.
	 * @return the name of each state if its language-dependent string is
	 * correct or the string that is passed as parameter.
	 */
	public static String[] getStringsLanguageDependent(String[] languageDependentStrings) {
        int l = languageDependentStrings.length;
		String[] elements = new String[l];
        for (int i = 0; i < l; i++) {
			elements[i] = getStringLanguageDependent(languageDependentStrings[i]);
		}
		return elements;
	}

	/**
	 * This method returns an array of strings, each one has the default states
	 * contained in one element of the whole list separated by dashes.
	 *
	 * @return an array that contains a list of string that contains the
	 * different states separated by dashes.
	 */
	public static String[] getListStrings() {
        int length = LIST.size();
        String[] strings = new String[length];
        for (int i = 0; i < length; i++) {
            ArrayList<String> states = LIST.get(i);
            strings[i] = getString(states.toArray(new String[states.size()]));
		}
		return strings;
	}

	/**
	 * This method returns the index in the list of the states passed as
	 * parameter. A states set matches an element of the list if has the same
	 * size and the same elements in the same order. The elements of the list
	 * are the language-dependent strings, not the names of the states. If the
	 * parameter doesn't match any element of the list, then the last index is
	 * returned.
	 *
	 * @param elements array that contains the names of the states.
	 * @return the index in the list of the states set
	 */
	public static int getIndexLanguageDependent(String[] elements) {
		ArrayList<String> statesAsList = new ArrayList<String>();
        Collections.addAll(statesAsList, elements);
        int length = LIST.size();
        boolean found = false;
        int i = 0;
        while (!found && (i < length)) {
            ArrayList<String> languageStrings = new ArrayList<String>();
            for (String languageString : LIST.get(i)) {
				languageStrings.add(getString(languageString));
			}
			if (languageStrings.equals(statesAsList)) {
				found = true;
			} else {
				i++;
			}
		}
		return (found) ? i : -1;
	}
}
