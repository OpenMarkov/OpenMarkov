/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * It is used to control the character format in Relevance ComboBox when the
 * user select the option "Other... "
 *
 * @author Alberto Manuel Ruiz Lafuente UCLM 2008
 * @version 1.1 25/07/2009 jlgozalo set variables in english language
 */
public class DocumentLimit extends PlainDocument {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * "Editor" which will control the string
	 */
	private final JTextField editor;

	/**
	 * Max number of characters to control
	 */
	private final int maxCharNumber;

	/**
	 * Constructor
	 *
     * @param editor        the foreground field to constrain
	 * @param maxCharNumber maximum allowed number of characters
	 */
	public DocumentLimit(JTextField editor, int maxCharNumber) {

		this.editor = editor;
		this.maxCharNumber = maxCharNumber;
	}

	/**
	 * Method that is called by the editor each time that the user introduce a
	 * character. The method check that is not over the limit.
	 */
	@Override public void insertString(int arg0, String arg1, javax.swing.text.AttributeSet arg2) throws BadLocationException {

		if ((editor.getText().length() + arg1.length()) > this.maxCharNumber)
			return;
		super.insertString(arg0, arg1, arg2);
	}

}
