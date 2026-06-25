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

		//Transitive reduction
		List<Link<Node>> linksToRemove = new ArrayList<>();
		for (Node dec : order.getNodes()) {
			List<Link<Node>> decLinks = order.getLinks(dec);
			for (int i = 0; i < decLinks.size(); i++) {
                Node nodeI = decLinks.get(i).getTo();
				for (int j = 0; j < decLinks.size(); j++) {
					Link<Node> linkJ = decLinks.get(j);
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
