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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
		String value = stringDatabase.getString(key);
		assertNotNull(value);
		// getString never returns null: a key with no text comes back as ">>> the.key <<<". So the
		// null check alone passed for every key, present or missing, and none of these assertions
		// was checking anything (P6 of the report).
		assertFalse(value.startsWith(">>>"), "The key " + key + " has no text in the bundles: " + value);
	}

	/**
	 * B4: the parameters of a formatted message are user data — the name of a network, a file path —
	 * and used to be inserted as a regular-expression replacement, where {@code $} and {@code \} mean
	 * something. A name carrying one either came out corrupted or threw IllegalArgumentException.
	 */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void aParameterWithADollarSignIsInsertedAsItIs() {
		String formatted = stringDatabase.getFormattedString("MessageResourceNotExists.Text", "cost$file");

		assertEquals("Missing image resource: cost$file", formatted);
	}

	/** B4: the same for a backslash, which introduces an escape in a replacement string. */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void aParameterWithABackslashIsInsertedAsItIs() {
		String formatted = stringDatabase.getFormattedString("MessageResourceNotExists.Text", "C:\\nets\\a.pgmx");

		assertEquals("Missing image resource: C:\\nets\\a.pgmx", formatted);
	}

	/** Several placeholders are filled in order, and a null parameter empties its own. */
	@Tag(TestSpeed.MEDIUM)
	@Test public final void everyPlaceholderTakesItsOwnParameter() {
		String formatted = stringDatabase.getFormattedString("LinkMakesCycle.Text", "A", "B");

		assertEquals("The link A -> B cannot be created because it would make a cycle in the network", formatted);
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
	 * This method tests the method getBundleButtons, and that asking for a language that is not
	 * served leaves every text in place.
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
        getCorrectString(stringDatabase, "AboutBox.Authors.Text");
		getCorrectString(stringDatabase, "AddState.Title");
        getCorrectString(stringDatabase, "AddState.Message");
        getCorrectString(stringDatabase, "NetworkVariablesPanel.jLabelDefaultStates.Text");
        getCorrectString(stringDatabase, "NetworkPropertiesDialog.Advanced");
        getCorrectString(stringDatabase, "TemporalEvolutionResultDialog.Title");
		getCorrectString(stringDatabase, "TemporalEvolutionResultDialog.States");
	}

	/**
	 * This method tests the method getBundleDialogs, and that asking for a language that is not
	 * served leaves every text in place.
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
        
        getCorrectString(stringDatabase, "Edit.EditClass");
        getCorrectString(stringDatabase, "Edit.Copy");
		getCorrectString(stringDatabase, "Edit.NodeProperties.Mnemonic");
		getCorrectString(stringDatabase, "Edit.Paste.Mnemonic");
        getCorrectString(stringDatabase, "File.Close");
		getCorrectString(stringDatabase, "File.Mnemonic");
        getCorrectString(stringDatabase, "View");
	}

	/**
	 * This method tests the method getBundleMenus, and that asking for a language that is not
	 * served leaves every text in place.
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
	 * This method tests the method getBundleMessages, and that asking for a language that is not
	 * served leaves every text in place.
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
        
        getCorrectString(stringDatabase, "defaultStates.absent.Text");
        getCorrectString(stringDatabase, "defaultStates.high.Text");
        getCorrectString(stringDatabase, "defaultStates.mild.Text");
        getCorrectString(stringDatabase, "purpose.other.Text");
        getCorrectString(stringDatabase, "defaultStates.present.Text");
        getCorrectString(stringDatabase, "purpose.sign.Text");
        getCorrectString(stringDatabase, "defaultStates.yes.Text");
	}

	/**
	 * This method tests the method getBundleSelectables, and that asking for a language that is not
	 * served leaves every text in place.
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
        
        getCorrectString(stringDatabase, "Edit.Mode.Chance.ToolTip");
        getCorrectString(stringDatabase, "ClipboardCut.ToolTip");
        getCorrectString(stringDatabase, "Edit.Mode.Decision.ToolTip");
        getCorrectString(stringDatabase, "NewNetwork.ToolTip");
        getCorrectString(stringDatabase, "Edit.Mode.Selection.ToolTip");
        getCorrectString(stringDatabase, "Redo.ToolTip");
        getCorrectString(stringDatabase, "Edit.Mode.Utility.ToolTip");
	}

	/**
	 * This method tests the method getBundleToolBars, and that asking for a language that is not
	 * served leaves every text in place.
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
