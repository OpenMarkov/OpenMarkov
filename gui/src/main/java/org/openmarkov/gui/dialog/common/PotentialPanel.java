/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class for panels that edit a node's {@link org.openmarkov.core.model.network.potential.Potential}.
 * Concrete implementations are discovered at runtime via the {@link PotentialPanelPlugin} annotation
 * and instantiated by {@link PotentialPanelManager}.
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor(Node.class))
@SuppressWarnings("serial") public abstract class PotentialPanel extends JPanel {
	private final List<PanelResizeEventListener> listeners;
	/**
	 * If true, values inside the panel will not be editable
	 */
	private boolean readOnly;

	public PotentialPanel() {
		listeners = new ArrayList<>();
	}

	/**
	 * Fill the panel with the data from the node.
	 *
	 * @param node the node whose potential data should be displayed
	 */
    public abstract void setData(Node node) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException;

	/**
	 * Modify the node according to the changes entered by the user in the panel
	 */
    public boolean saveChanges() throws BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong, org.openmarkov.core.exception.DoEditException {
		close();
		return true;
	}

	/**
	 * Releases resources or performs cleanup when the panel is closed.
	 */
	public abstract void close();

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Registers a listener to be notified when this panel is resized.
	 *
	 * @param listener the listener to register
	 */
	public void suscribePanelResizeEventListener(PanelResizeEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a previously registered resize listener.
	 *
	 * @param listener the listener to remove
	 * @return {@code true} if the listener was found and removed
	 */
	public boolean unsuscribePanelResizeEventListener(PanelResizeEventListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * Notifies all registered listeners that this panel has been resized.
	 */
	public void notifyPanelResizeEventListeners() {
		PanelResizeEvent event = new PanelResizeEvent(this, getSize());
		for (PanelResizeEventListener listener : listeners) {
			listener.panelSizeChanged(event);
		}
	}

}
