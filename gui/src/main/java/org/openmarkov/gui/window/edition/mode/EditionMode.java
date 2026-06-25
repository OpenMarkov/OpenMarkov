/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 * This class the defines the behaviour of the editor panel in a certain edition
 * state such as selection, node creation, link creation, etc.
 *
 * @author ibermejo
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({NetworkEditorPanel.class, ProbNet.class}))
public abstract class EditionMode implements KeyListener, FocusListener {

	protected final NetworkEditorPanel networkEditorPanel;
	protected final VisualNetwork visualNetwork;
	protected final ProbNet probNet;

	public EditionMode(NetworkEditorPanel networkEditorPanel, ProbNet probNet) {
		this.networkEditorPanel = networkEditorPanel;
		this.visualNetwork = networkEditorPanel.getVisualNetwork();
		this.probNet = probNet;
	}
    
    public abstract void mousePressed(MouseEvent e, Point2D.Double position, Graphics2D g) throws DoEditException;

    public abstract void mouseReleased(MouseEvent e, Point2D.Double position, Graphics2D g) throws DoEditException;
	
	public abstract void mouseMoved(MouseEvent e, Point2D.Double position, double diffX, double diffY, Graphics2D g);
	
	
	public abstract void tryCancelCurrentAction(MouseEvent e, Point2D.Double position, Graphics2D g);
}
