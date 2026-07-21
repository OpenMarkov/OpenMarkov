/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.CRemoveNodeEdit;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Compound edit that removes all currently selected nodes and links from the network.
 * Links connected to removed nodes are also removed.
 *
 * @author Iñigo
 */
@SuppressWarnings("serial")
public class RemoveSelectedEdit extends CompoundPNEdit {
	private final List<VisualNode> nodesToRemove;
	private final List<VisualLink> linksToRemove;
    
    // TODO OOPN end

	/**
	 * Creates a new edit that will remove all selected nodes and links.
	 *
	 * @param visualNetwork the visual network from which selected elements will be removed
	 */
	public RemoveSelectedEdit(VisualNetwork visualNetwork) {
		super(visualNetwork.getNetwork());
		this.nodesToRemove = visualNetwork.getSelectedNodes();
		this.linksToRemove = union(visualNetwork.getSelectedLinks(), visualNetwork.getLinksOfNodes(this.nodesToRemove));
        System.out.println();
	}
    
    @Override public ArrayList<PNEdit> generateEdits() {
        ArrayList<PNEdit> edits = new ArrayList<>();
		for (VisualLink link : linksToRemove) {
            edits.add(new RemoveLinkEdit(probNet, probNet.getVariable(link.getSourceNode().getNode().getName()),
                                         probNet.getVariable(link.getDestinationNode().getNode().getName()),
                                         link.getLink().isDirected()));
        }
		for (VisualNode node : nodesToRemove) {
			edits.add(new CRemoveNodeEdit(probNet, node.getNode()));
		}
		return edits;
	}

	/**
	 * Produces the union of two lists of visual links without duplicates.
	 *
	 * @param list1 first list
	 * @param list2 second list
	 * @return a new list containing all unique links from both lists
	 */
    private static List<VisualLink> union(List<VisualLink> list1, List<VisualLink> list2) {
        List<VisualLink> result = new ArrayList<VisualLink>(list1);
		for (VisualLink o : list2) {
			if (!result.contains(o)) {
				result.add(o);
			}
		}
		return result;
	}
}
