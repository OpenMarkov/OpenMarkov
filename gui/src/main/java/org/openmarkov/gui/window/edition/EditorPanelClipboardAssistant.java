/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

/**
 * This class assists to a network panel in operations with the clipboard.
 *
 * @author jmendoza
 * @version 1.0
 */
public class EditorPanelClipboardAssistant {
	private SelectedContent content = null;
    
    /**
	 * @param copiedContent selected content to copy to the clipboard.
	 */
	public void copyToClipboard(SelectedContent copiedContent) {
		this.content = copiedContent;
	}

	/**
	 * This method retrieves the content of the clipboard
	 */
	public SelectedContent paste() {
		return new SelectedContent(this.content);
	}

	/**
	 * This method says if there is data stored in the clipboard.
	 *
	 * @return true if there is data stored in the clipboard; otherwise, false.
	 */
	public boolean isThereDataStored() {
		return content != null;
	}
}
