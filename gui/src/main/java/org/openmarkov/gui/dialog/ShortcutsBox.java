/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class that shows a window with OpenMarkov shortcuts
 *
 * @author IagoParis - 29/10/2018
 * @version 1.0
 */
public class ShortcutsBox extends JDialog implements ActionListener {

	/** ID for serialization checks **/
	private static final long serialVersionUID = -2926600957370532010L;

	/** String database **/
	private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	/* List of known shortcuts */
	//-------------------------//
	/* They are listed by its token in the string database.
		To add a new shortcut, replicate the structure of any token
		in the localization xml files and add said token to the
		corresponding list below. */

	private final String[] specificShortcuts = new String[]{
			"OpenTable",
			"OpenTableNote",
			"OpenProperties",
			"MoveNodes",
			"ToggleEditionInference",
			"PreviousNext",
			"SaveReopen"};


	private final String[] commonShortcuts = new String[]{
			"New",
			"Open",
			"Save",
			"SelectAll",
			"Cut",
			"Copy",
			"Paste",
			"Undo",
			"Redo",
			"MacNote"};


	/**
	 * Constructor on a parent JFrame
	 */
	public ShortcutsBox(JFrame parent) {
		super(parent, "", true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		createAndSetPanels();

		// Position and dimension of the components
		pack();
		int halfHeight = getHeight()/2;
		int halfWidth = getWidth()/2;
		int halfParentHeight = getParent().getHeight()/2;
		int halfParentWidth = getParent().getWidth()/2;
		setLocation(halfParentWidth - halfWidth, halfParentHeight - halfHeight);
		this.setVisible(true);
	}


	// This functions create all the swing components (the shortcut tables)
	private void createAndSetPanels() {
		// Title
		setTitle(stringDatabase.getString("ShortcutsBox.Title"));

		// Create the main panel: contains two tables
		JPanel mainPanel = new JPanel();
		GridLayout mainLayout = new GridLayout(2, 1, 1, 20);
		mainPanel.setLayout(mainLayout);

		// Create fonts
		Font bigBold = new Font(Font.SERIF, Font.BOLD, 15);
		Font bold = new Font(Font.SERIF, Font.BOLD, 13);


		//---------------------------------//
		// First table: specific shortcuts //
		//---------------------------------//
		JPanel specificPanel = new JPanel();
		GridLayout specificLayout = new GridLayout(specificShortcuts.length + 2, 2, 10, 1);
		specificPanel.setLayout(specificLayout);

		// First row
		JLabel specificTitle = new JLabel(stringDatabase.getString("ShortcutsBox.SpecificTableTitle"));
		specificTitle.setFont(bigBold);
		specificPanel.add(specificTitle);
		specificPanel.add(new JLabel(""));

		// Second row
		JLabel shortcutTitle = new JLabel(stringDatabase.getString("ShortcutsBox.ShortcutRowTitle"));
		shortcutTitle.setFont(bold);
		JLabel functionTitle = new JLabel(stringDatabase.getString("ShortcutsBox.FunctionRowTitle"));
		functionTitle.setFont(bold);
		specificPanel.add(shortcutTitle);
		specificPanel.add(functionTitle);

		// Specific shortcut rows
		for (String entry : specificShortcuts) {
			String shortcut = stringDatabase.getString("ShortcutsBox.Specific." + entry + ".Shortcut");
			String function = stringDatabase.getString("ShortcutsBox.Specific."  + entry + ".Function");
			addRow(specificPanel, shortcut, function);
		}

		mainPanel.add(specificPanel);


		//--------------------------------//
		// Second table: common shortcuts //
		//--------------------------------//

		JPanel commonPanel = new JPanel();
		GridLayout commonLayout = new GridLayout(commonShortcuts.length + 2, 2, 10, 5);
		commonPanel.setLayout(commonLayout);

		// First row
		JLabel commonTitle = new JLabel(stringDatabase.getString("ShortcutsBox.CommonTableTitle"));
		commonTitle.setFont(bigBold);
		commonPanel.add(commonTitle);
		commonPanel.add(new JLabel(""));

		// Common shortcut rows
		for (String entry : commonShortcuts) {
			String shortcut = stringDatabase.getString("ShortcutsBox.Common." + entry + ".Shortcut");
			String function = stringDatabase.getString("ShortcutsBox.Common." + entry + ".Function");
			addRow(commonPanel, shortcut, function);
		}

		mainPanel.setBorder( BorderFactory.createEmptyBorder(20, 20, 20, 20) );
		mainPanel.add(commonPanel);

		//--------------------------------------------------------------
		setContentPane(mainPanel);

	}

	// Auxiliary function to iteratively create a row with shortcut info
	private void addRow(JPanel specificPanel, String shortcut, String function) {
		// If the row is a note use a smaller font
		if (shortcut.equalsIgnoreCase(stringDatabase.getString("ShortcutsBox.NoteIndicator"))) {
			specificPanel.add(buildNoteLabel(function));
			specificPanel.add(new JLabel(""));
		} else {
			specificPanel.add(new JLabel(shortcut));
			specificPanel.add(new JLabel(function));
		}
	}

	/* Adding the same component (by reference) two times to different swing components (or the same) renders the first
		an empty component. To avoid that the object should be constructed on addition. But a font can't be added to
		a JLabel constructed on the fly, so this factory function is needed: */
    private static JLabel buildNoteLabel(String content) {
		Font noteFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
		JLabel note = new JLabel(content);
		note.setFont(noteFont);
		return note;
	}

	/**
	 * Close the dialog on a button event.
	 * @param actionEvent A possible OK click
	 */
	@Override public void actionPerformed(ActionEvent actionEvent) {
        // Reusing about box foreground
		if (actionEvent.getActionCommand().equals(stringDatabase.getString("AboutBox.OK.Text"))) {
			this.dispose();
		}
	}
}
