package org.openmarkov.gui.util;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.gui.dialog.io.OMFileChooser;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a {@link DecisionTreeNode} hierarchy into a Graphviz DOT format file.
 * Nodes are colored by type (chance, decision, utility) and edges carry
 * branch state names and probabilities.
 */
public class TreeNodeToDot {
    
    private static final String C_decisionColor = "#cfe3fd";
    private static final String C_chanceColor = "#fbf999";
    private static final String C_utilityColor = "#d0e6b2";
    
    private class DotNode {
        private String nodeName;
        private int number;
        private NodeType type;
        private final double computedUtility;
        
        public DotNode(int number, String nodeName, double computedUtility, NodeType type) {
            this.nodeName = nodeName;
            this.number = number;
            this.type = type;
            this.computedUtility = computedUtility;
        }
        
        public String getNodeName() {
            return nodeName;
        }
        
        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }
        
        public int getNumber() {
            return number;
        }
        
        public void setNumber(int number) {
            this.number = number;
        }
        
        public NodeType getType() {
            return type;
        }
        
        public void setType(NodeType type) {
            this.type = type;
        }
        
        private String buildStyle() {
            if (this.type == NodeType.CHANCE) {
                return "shape = \"oval\", fillcolor=\"" + C_chanceColor + "\"";
            }
            if (this.type == NodeType.DECISION) {
                return "shape = \"box\", fillcolor=\"" + C_decisionColor + "\"";
            }
            return "shape = \"hexagon\", fillcolor=\"" + C_utilityColor + "\"";
        }
        
        private String buildLabel() {
            return "<b>" + this.nodeName + "</b><br/><font color=\"red\"> U=" + df.format(this.computedUtility) + "</font>";
        }
        
        @Override
        public String toString() {
            return this.number + " [label=<" + buildLabel() + ">, " + buildStyle() + "];";
        }
    }
    
    private class DotLink {
        private DotNode sourceNode;
        private DotNode destinationNode;
        private final String branchState;
        private final double probability;
        
        public DotLink(DotNode sourceNode, DotNode destinationNode, String branchState, double probability) {
            this.sourceNode = sourceNode;
            this.destinationNode = destinationNode;
            this.probability = probability;
            this.branchState = branchState;
            
        }
        
        public DotNode getSourceNode() {
            return sourceNode;
        }
        
        public void setSourceNode(DotNode sourceNode) {
            this.sourceNode = sourceNode;
        }
        
        public DotNode getDestinationNode() {
            return destinationNode;
        }
        
        public void setDestinationNode(DotNode destinationNode) {
            this.destinationNode = destinationNode;
        }
        
        private String buildLabel() {
            if (this.sourceNode.getType() == NodeType.CHANCE) {
                return "<b>" + this.branchState + "</b><br/>P=" + df.format(this.probability);
            }
            return "<b>" + this.branchState + "</b>";
        }
        
        @Override
        public String toString() {
            return this.sourceNode.getNumber() + " -> " + this.destinationNode.getNumber() + " [label=<" + buildLabel() + ">];";
        }
    }
    
    final List<DotNode> dotNodes = new ArrayList<>();
    final List<DotLink> dotLinks = new ArrayList<>();
    
    private int numNode = 0;
    private final DecimalFormat df = new DecimalFormat();
    private int graphDPI;
    
    public TreeNodeToDot() {
        graphDPI = 300;
        setNumDecimals(4);
    }
    
    /**
     * Generates a DOT graph from the given decision tree root node and prompts the user
     * to save it as a {@code .gv} file.
     *
     * @param treeNode the root node of the decision tree
     * @throws IOException if writing the file fails
     */
    public void paintDTNode(DecisionTreeNode treeNode) throws IOException {
        List<DecisionTreeNode> children = new ArrayList<>();
        children.add(treeNode);
        
        DotNode sourceNode = new DotNode(numNode, treeNode.getVariable()
                                                          .getName(), (double) treeNode.getUtility(), treeNode.getNodeType());
        numNode += 1;
        dotNodes.add(sourceNode);
        
        
        parseTreeNode(sourceNode, treeNode);
        
        String graph = "digraph G {" + "\n\tgraph [dpi = " + graphDPI + "];\n\trankdir=LR;\n\tnode [style=\"filled\"]; \n";
        for (DotNode node : dotNodes) {
            graph += "\t" + node.toString() + "\n";
        }
        
        for (DotLink dotLink : dotLinks) {
            graph += "\t" + dotLink + "\n";
        }
        graph += "}";
        
        System.out.println(graph.toString());
        
        OMFileChooser chooser = new OMFileChooser();
        int retrival = chooser.showSaveDialog(null);
        if (retrival != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".gv")) {
            fw.write(graph.toString());
        }
    }
    
    private void parseTreeNode(DotNode sourceNode, DecisionTreeNode treeNode) {
        
        // Analyze the branches of that node
        for (Object elements : treeNode.getChildren()) {
            DecisionTreeBranch branch = (DecisionTreeBranch) elements;
            String branchState = branch.getBranchState().getName();
            
            DecisionTreeNode childNode = branch.getChild();
            DotNode destinationNode = new DotNode(numNode, childNode.getVariable()
                                                                    .getName(), (double) childNode.getUtility(), childNode.getNodeType());
            numNode += 1;
            dotNodes.add(destinationNode);
            dotLinks.add(new DotLink(sourceNode, destinationNode, branchState, branch.getBranchProbability()));
            
            parseTreeNode(destinationNode, childNode);
        }
        
    }
    
    /**
     * Sets the number of decimal places used when formatting numeric values.
     *
     * @param numDecimals the maximum number of fraction digits
     */
    public void setNumDecimals(int numDecimals) {
        df.setMaximumFractionDigits(numDecimals);
    }
    
    /**
     * Sets the DPI (dots per inch) for the generated graph.
     *
     * @param graphDPI the resolution in DPI
     */
    public void setGraphDPI(int graphDPI) {
        this.graphDPI = graphDPI;
    }
}
