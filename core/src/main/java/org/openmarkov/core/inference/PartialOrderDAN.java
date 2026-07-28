/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mluque
 * The transitive reduction of the partial temporal order among decisions induced by the DAN.
 */
public class PartialOrderDAN {

	final ProbNet order;

	public PartialOrderDAN(ProbNet probNet) {

		order = new ProbNet();

		//Only keep decision nodes
		for (Node auxNode : probNet.getNodes()) {
			NodeType auxType = auxNode.getNodeType();
			if (auxType == NodeType.DECISION) {
				order.addNode(auxNode.getVariable(), auxType);
			}
		}

		//Transitive closure among decision nodes
		for (Node nodeI : order.getNodes()) {
			for (Node nodeJ : order.getNodes()) {
				if (nodeI != nodeJ) {
					Variable variableI = nodeI.getVariable();
					Variable variableJ = nodeJ.getVariable();
					Node probNetnodeI = probNet.getNode(variableI);
					Node probNetNodeJ = probNet.getNode(variableJ);
					if (probNet.existsPath(probNetnodeI, probNetNodeJ, true, Collections.emptyList())) {
						order.addLink(order.getNode(variableI), order.getNode(variableJ), true);
					}
				}
			}
		}

		// Transitive reduction: the link dec -> nodeJ is redundant when another successor of dec,
		// nodeI, already reaches nodeJ.
		//
		// This walked the links *incident* to dec, incoming ones included, and read the end of each
		// as if it were a successor. For an incoming link that end is dec itself, so nodeI could be
		// dec, and "does nodeI reach nodeJ?" was then trivially true through the very link being
		// examined. The effect was that every outgoing link of a decision that had a predecessor got
		// deleted: in a chain of three phases of tests, the order kept only the links of the first.
		List<Link<Node>> linksToRemove = new ArrayList<>();
		for (Node dec : order.getNodes()) {
			List<Link<Node>> successorLinks = order.getLinks(dec).stream().filter(link -> link.getFrom() == dec).toList();
			for (Link<Node> linkI : successorLinks) {
				for (Link<Node> linkJ : successorLinks) {
					Node nodeI = linkI.getTo();
					Node nodeJ = linkJ.getTo();
					if ((nodeI != nodeJ) && order.existsPath(nodeI, nodeJ, true, Collections.emptyList())) {
						linksToRemove.add(linkJ);
					}
				}
			}
		}
		for (Link<Node> auxLink : linksToRemove) {
			order.removeLink(auxLink);
		}
	}

	public ProbNet getOrder() {
		return order;
	}

	public String toStringForGraphviz() {
        
        ProbNet probNet = this.getOrder();
		List<Link<Node>> links = probNet.getLinks();
        String content = "digraph G {\n";

		for (Node node : probNet.getNodes()) {
            String strType = switch (node.getNodeType()) {
                case CHANCE -> "ellipse";
                case DECISION -> "decision";
                default -> "";
            };
            content = content + getNameWithQuotes(node) + "[shape=" + strType + "]\n";
		}

		for (Link<Node> link : links) {
            Node node1 = link.getFrom();
            Node node2 = link.getTo();

			content = content + getNameWithQuotes(node1) + "-> " + getNameWithQuotes(node2) + "\n";

		}
		content = content + "}\n";

		return content;

	}
    
    private static String getNameWithQuotes(Node node) {
		return "\"" + node.getVariable().getName() + "\"";

	}

}
