/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.localize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.localize.StringBundle;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.MissingResourceException;

/**
 * This class tests the classes
 * {@link StringDatabase} and
 * {@link StringBundle}.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.1 jlgozalo. modified as MissingErrorExpectedException is not longer
 * required
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class StringDatabaseTests {

	StringDatabase stringDatabase = null;

	@BeforeEach public void setUp() {
		stringDatabase = StringDatabase.getUniqueInstance();
	}

	/**
	 * This method gets a correct string identified by its key from a string
	 * resource.
	 *
	 * @param stringDatabase string resource from which the string is loaded.
	 * @param key            key of the string.
	 * @throws MissingResourceException if the string can't be loaded from the
	 *                                  string resource.
	 */
	private void getCorrectString(StringDatabase stringDatabase, String key) throws MissingResourceException {
		assertNotNull(stringDatabase.getString(key));
	}

	/**
	 * This method gets a string identified by its key from the buttons resorce
	 * bundle.
	 *
	 * @throws MissingResourceException if any string doesn't exist.
	 */
	private void getStringButtons() throws MissingResourceException {
        
        getCorrectString(stringDatabase, "Add.Text");
        getCorrectString(stringDatabase, "Cancel.Text");
        getCorrectString(stringDatabase, "Clear.Text");
        getCorrectString(stringDatabase, "Copy.Text");
        getCorrectString(stringDatabase, "Delete.Text");
        getCorrectString(stringDatabase, "Down.Text");
        getCorrectString(stringDatabase, "Ok.Text");
	}

	/**
	 * This method tests the method getBundleButtons and setLanguage loading
	 * various strings in English and Spanish.
	 *
	 * @throws MissingResourceException if any of the strings doesn't exist.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleButtons() throws MissingResourceException {
		StringDatabase.getUniqueInstance().setLanguage("en");
		getStringButtons();
		StringDatabase.getUniqueInstance().setLanguage("es");
		getStringButtons();
	}

	/**
	 * This method tests the method getBundleButtons loading a wrong key.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleButtonsWrong() {
		stringDatabase.setLanguage("en");
		String string = stringDatabase.getString("incorrect");
		assertEquals(string, ">>> incorrect <<<");
	}

	/**
	 * This method gets a string identified by its key from the dialogs resorce
	 * bundle.
	 *
	 * @throws MissingResourceException if any string doesn't exist.
	 */
	private void getStringDialogs() throws MissingResourceException {
        getCorrectString(stringDatabase, "Author.Text");
		getCorrectString(stringDatabase, "ChainGraph.Text.Mnemonic");
        getCorrectString(stringDatabase, "Continuous.Text");
        getCorrectString(stringDatabase, "Defaults.Title");
        getCorrectString(stringDatabase, "Information.Title");
        getCorrectString(stringDatabase, "NetworkProperties.Title");
		getCorrectString(stringDatabase, "Values.Text.Mnemonic");
	}

	/**
	 * This method tests the method getBundleDialogs and setLanguage loading
	 * various strings in English and Spanish.
	 *
	 * @throws MissingResourceException if any of the strings doesn't exist.
	 */
    @Test
    @Tag(TestSpeed.MEDIUM)
    public final void testGetBundleDialogs() throws MissingResourceException {
		StringDatabase.getUniqueInstance().setLanguage("en");
		getStringDialogs();
		StringDatabase.getUniqueInstance().setLanguage("es");
		getStringDialogs();
	}

	/**
	 * This method tests the method getBundleDialogs loading a wrong key.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleDialogsWrong() {

		stringDatabase.setLanguage("en");
		String string = stringDatabase.getString("incorrect");
		assertEquals(string, ">>> incorrect <<<");

	}

	/**
	 * This method gets a string identified by its key from the menus resorce
	 * bundle.
	 *
	 * @throws MissingResourceException if any string doesn't exist.
	 */
	private void getStringMenus() throws MissingResourceException {
        
        getCorrectString(stringDatabase, "Edit.ChanceCreation");
        getCorrectString(stringDatabase, "Edit.Copy");
		getCorrectString(stringDatabase, "Edit.NodeProperties.Mnemonic");
		getCorrectString(stringDatabase, "Edit.Paste.Mnemonic");
        getCorrectString(stringDatabase, "File.Close");
		getCorrectString(stringDatabase, "File.Mnemonic");
        getCorrectString(stringDatabase, "View");
	}

	/**
	 * This method tests the method getBundleMenus and setLanguage loading
	 * various strings in English and Spanish.
	 *
	 * @throws MissingResourceException if any of the strings doesn't exist.
	 */
    @Test
    @Tag(TestSpeed.MEDIUM)
    public final void testGetBundleMenus() throws MissingResourceException {
		StringDatabase.getUniqueInstance().setLanguage("en");
		getStringMenus();
		StringDatabase.getUniqueInstance().setLanguage("es");
		getStringMenus();
	}

	/**
	 * This method tests the method getBundleMenus loading a wrong key.
	 */
	@Test public final void testGetBundleMenusWrong() {

		stringDatabase.setLanguage("en");
		String string = stringDatabase.getString("incorrect");
		assertEquals(string, ">>> incorrect <<<");
	}

	/**
	 * This method gets a string identified by its key from the messages resorce
	 * bundle.
	 *
	 * @throws MissingResourceException if any string doesn't exist.
	 */
	private void getStringMessages() throws MissingResourceException {
        
        getCorrectString(stringDatabase, "Action.MoveNodes");
        getCorrectString(stringDatabase, "ClipboardNotSet.Text");
        getCorrectString(stringDatabase, "EmptyState.Text");
        getCorrectString(stringDatabase, "IconificationVetoed.Text");
        getCorrectString(stringDatabase, "LoadingNetwork.Text");
        getCorrectString(stringDatabase, "NodeNotCreated.Text");
        getCorrectString(stringDatabase, "SelectionVetoed.Text");
	}

	/**
	 * This method tests the method getBundleMessages and setLanguage loading
	 * various strings in English and Spanish.
	 *
	 * @throws MissingResourceException if any of the strings doesn't exist.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleMessages() throws MissingResourceException {
		stringDatabase.setLanguage("en");
		getStringMessages();
		stringDatabase.setLanguage("es");
		getStringMessages();
	}

	/**
	 * This method tests the method getBundleMessages loading a wrong key.
	 */
	@Test public final void testGetBundleMessagesWrong() {
		stringDatabase.setLanguage("en");
		String string = stringDatabase.getString("incorrect");
		assertEquals(string, ">>> incorrect <<<");
	}

	/**
	 * This method gets a string identified by its key from the selectables
	 * resorce bundle.
	 *
	 * @throws MissingResourceException if any string doesn't exist.
	 */
	private void getStringSelectables() throws MissingResourceException {
        
        getCorrectString(stringDatabase, "absent.Text");
        getCorrectString(stringDatabase, "high.Text");
        getCorrectString(stringDatabase, "mild.Text");
        getCorrectString(stringDatabase, "other.Text");
        getCorrectString(stringDatabase, "present.Text");
        getCorrectString(stringDatabase, "sign.Text");
        getCorrectString(stringDatabase, "yes.Text");
	}

	/**
	 * This method tests the method getBundleSelectables and setLanguage loading
	 * various strings in English and Spanish.
	 *
	 * @throws MissingResourceException if any of the strings doesn't exist.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleSelectables() throws MissingResourceException {
		stringDatabase.setLanguage("en");
		getStringSelectables();
		stringDatabase.setLanguage("es");
		getStringSelectables();
	}

	/**
	 * This method tests the method getBundleSelectables loading a wrong key.
	 */
	@Test public final void testGetBundleSelectablesWrong() {

		stringDatabase.setLanguage("en");
		String string = stringDatabase.getString("incorrect");
		assertEquals(string, ">>> incorrect <<<");
	}

	/**
	 * This method gets a string identified by its key from the toolbars resorce
	 * bundle.
	 *
	 * @throws MissingResourceException if any string doesn't exist.
	 */
	private void getStringToolBars() throws MissingResourceException {
        
        getCorrectString(stringDatabase, "ChanceCreation.ToolTip");
        getCorrectString(stringDatabase, "ClipboardCut.ToolTip");
        getCorrectString(stringDatabase, "DecisionCreation.ToolTip");
        getCorrectString(stringDatabase, "NewNetwork.ToolTip");
        getCorrectString(stringDatabase, "ObjectSelection.ToolTip");
        getCorrectString(stringDatabase, "Redo.ToolTip");
        getCorrectString(stringDatabase, "UtilityCreation.ToolTip");
	}

	/**
	 * This method tests the method getBundleToolBars and setLanguage loading
	 * various strings in English and Spanish.
	 *
	 * @throws MissingResourceException if any of the strings doesn't exist.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleToolBars() throws MissingResourceException {
		stringDatabase.setLanguage("en");
		getStringToolBars();
		stringDatabase.setLanguage("es");
		getStringToolBars();
	}

	/**
	 * This method tests the method getBundleToolBars loading a wrong key.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void testGetBundleToolBarsWrong() {
		stringDatabase.setLanguage("en");
		String string = stringDatabase.getString("incorrect");
		assertEquals(string, ">>> incorrect <<<");
	}
}
