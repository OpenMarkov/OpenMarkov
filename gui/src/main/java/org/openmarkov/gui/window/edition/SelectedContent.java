/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition;

import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable snapshot of the currently selected nodes and links in the editor,
 * used for clipboard operations (copy/paste) and selection-based actions.
 */
public record SelectedContent(List<Node> nodes, List<Link<Node>> links) implements ClassLocalizable {
 
	/**
	 * Copy constructor for ClipboardContent.
	 *
	 * @param content selected nodes and links
	 */
	public SelectedContent(SelectedContent content) {
        this(new ArrayList<>(content.nodes()), new ArrayList<>(content.links()));
	}
    
    /**
	 * returns whether the object is empty
	 * @return true iff the selected content is empty
	 */
	public boolean isEmpty() {
        return this.nodes.isEmpty() && this.links.isEmpty();
	}

}