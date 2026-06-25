package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;

/**
 * Simple edit that allow to modify the always append property of an event variable
 * @author cmyago
 * @version 1.0 25/10/2020
 */

@SuppressWarnings("serial") public class EventNodeAlwaysAppendEdit extends PNEdit {

	/**
	 * The last 'alwaysAppend' before the edition
	 */
	private boolean lastAlwaysAppend;
	/**
	 * The new 'alwaysAppend' after the edition
	 */
	private boolean newAlwaysAppend;
	/**
	 * The edited node
	 */
	private Node node;

	/**
	 * Creates a new <code>AlwaysAppendEdit</code> with the node and new 'alwaysObserved'
	 * specified.
	 *
	 * @param node              the node that will be edited
	 * @param newAlwaysAppend the new alwaysObserved
	 */
	public EventNodeAlwaysAppendEdit(Node node, boolean newAlwaysAppend) {
		super(node.getProbNet());
		this.lastAlwaysAppend = node.isAlwaysObserved();
		this.newAlwaysAppend = newAlwaysAppend;
		this.node = node;
	}

	/**
	 * Updates the event node with the new value for alwaysAppend
	 * @throws DoEditException if edit cannot be done
	 */
	@Override public void doEdit() throws DoEditException {
		node.setAlwaysAppend(newAlwaysAppend);
	}

	/**
	 * Undoes the alwaysAppend change
	 */
	@Override public void undo() {
		super.undo();
		node.setAlwaysAppend(lastAlwaysAppend);
	}


}
