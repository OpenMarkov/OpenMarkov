package org.openmarkov.io.amua.writer;

import org.jdom2.*;
import org.jdom2.output.*;
import org.openmarkov.io.amua.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;


/**
 * Responsible for writing a decision tree in Amua XML format.
 * This class takes a decision tree in Amua format, validates it,
 * initializes internal attributes if valid, and writes it to an XML file.
 *
 * @author Hugo Manuel
 * @version 3.0
 */

public class AmuaDTWriter {
    private final AmuaDTNode<?> amuaRootNode;
    private final AmuaDTDimensions amuaDimInfo;
    private final AmuaModel amuaModel;
    private final File outputFile;

    private static final DecimalFormat df = new DecimalFormat("#.####");


    /**
     * Constructs a new Amua Decision Tree Writer.
     *
     * @param amuaRootNode root node of the decision tree in Amua format.
     * @param amuaDimInfo object with information about the tree's dimensions
     * @param amuaModel yype of Amua decision tree (COST_EFFECTIVENESS or UNICRITERIA).
     * @param outputFile file to which the XML representation will be written.
     */
    public AmuaDTWriter(AmuaDTNode<?> amuaRootNode, AmuaDTDimensions amuaDimInfo, AmuaModel amuaModel, File outputFile) {
        this.amuaRootNode = amuaRootNode;
        this.amuaDimInfo = amuaDimInfo;
        this.amuaModel = amuaModel;
        this.outputFile = outputFile;
    }


    /**
     * Generates a complete Amua XML document from the decision tree and writes it to the output file.
     *
     * @throws IOException if an error occurs while writing the file.
     */
    public void writeDT() throws IOException {
        // <Model>...</Model>
        Element model = new Element("Model");

        // <name>...</name>
        model.addContent(writeAmuaName());

        // <Metadata>...</Metadata> (empty, required by Amua)
        model.addContent(new Element("Metadata"));

        // Add the dimension information
        model.addContent(writeAmuaDimInfo());

        // PARAMETER (cannot be included until they are defined separately in OpenMarkov): pending task

        // INFORMATION ABOUT THE SIMULATION: pending task

        // <tree>...</tree>
        model.addContent(writeAmuaTree());

        // build the complete XML
        Document doc = new Document(model);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            out.output(doc, fos);
        }
    }


    /**
     * This method extracts the base name of the file (without extension)
     * and returns it as an XML element with the tag "name".
     *
     * @return the XML element <name> representing the name
     */
    private Element writeAmuaName() {
        String fileName = outputFile.getName(); // retrieve the file name
        int dot = fileName.lastIndexOf('.'); // locate the last '.'

        // extract the base name (remove extension if present)
        String treeName = (dot > 0) ? fileName.substring(0, dot) : fileName;

        return new Element("name").setText(treeName);
    }


    /**
     * Writes the Amua representation of DimInfo
     *
     * @return an XML element <DimInfo> representing the tree's dimension information
     */
    private Element writeAmuaDimInfo() {
        Element dimInfo = new Element("DimInfo");

        List<AmuaDimensionInfo> dimensions = amuaDimInfo.getDimensions();

        for (AmuaDimensionInfo d : dimensions) {
            addElement(dimInfo, "dimNames", d.getName());
        }
        for (AmuaDimensionInfo d : dimensions) {
            addElement(dimInfo, "dimSymbols", d.getSymbols());
        }
        for (AmuaDimensionInfo d : dimensions) {
            addElement(dimInfo, "decimals", d.getDecimals());
        }

        addElement(dimInfo, "analysisType", amuaDimInfo.getAnalysisType());
        addElement(dimInfo, "objective", amuaDimInfo.getObjective());
        addElement(dimInfo, "objectiveDim", amuaDimInfo.getObjectiveDim());
        addElement(dimInfo, "costDim", amuaDimInfo.getCostDim());
        addElement(dimInfo, "effectDim", amuaDimInfo.getEffectDim());

        if (amuaModel == AmuaModel.COST_EFFECTIVENESS_DT){ // only required by CEA
            addElement(dimInfo, "baseScenario", amuaDimInfo.getBaseScenario());
        }

        addElement(dimInfo, "WTP", amuaDimInfo.getWTP());
        addElement(dimInfo, "extendedDim", amuaDimInfo.getExtendedDim());

        return dimInfo;
    }


    /**
     * Writes the Amua representation of a tree to an XML element
     *
     * @return an XML element <tree> containing all nodes of the decision tree.
     */
    private Element writeAmuaTree() {
        Element treeElem = new Element("tree");
        // explore the nodes (first call)
        writeAmuaNode(amuaRootNode, treeElem);
        return treeElem;
    }


    /**
     * Recursively writes the Amua representation of a tree node to an XML element.
     *
     * @param amuaNode the decision tree node to be serialized into XML
     * @param treeElem the XML element that contains all nodes
     */
    private void writeAmuaNode(AmuaDTNode<?> amuaNode, Element treeElem) {
        Element nodeElem = new Element("Node");

        addElement(nodeElem, "type", amuaNode.getType());
        addElement(nodeElem, "name", amuaNode.getName());

        addElement(nodeElem, "xPos", amuaNode.getXPos());
        addElement(nodeElem, "yPos", amuaNode.getYPos());
        addElement(nodeElem, "width", amuaNode.getWidth());
        addElement(nodeElem, "height", amuaNode.getHeight());
        addElement(nodeElem, "parentX", amuaNode.getParentX());
        addElement(nodeElem, "parentY", amuaNode.getParentY());

        AmuaDTNode<?> parent = amuaNode.getParentNode();
        if (parent != null) {
            addElement(nodeElem, "parentType", parent.getType());
        }

        List<? extends AmuaDTNode<?>> children = amuaNode.getChildNodes();
        if (children != null && !children.isEmpty()) {
            for (AmuaDTNode<?> child : children) {
                addElement(nodeElem, "childIndices", child.getIndex());
            }
        }

        addElement(nodeElem, "level", amuaNode.getLevel());

        addElement(nodeElem, "hasCost", amuaNode.isHasCost());
        addElement(nodeElem, "hasVarUpdates", amuaNode.isHasVarUpdates());
        addElement(nodeElem, "visible", amuaNode.isVisible());
        addElement(nodeElem, "collapsed", amuaNode.isCollapsed());

        if (amuaNode.getProbability() != 0) {
            addElement(nodeElem, "prob", df.format(amuaNode.getProbability()));
        }

        // delegates to write the cost and payoff
        writeNodeCost(amuaNode, nodeElem);
        writeNodePayoff(amuaNode, nodeElem);

        treeElem.addContent(nodeElem);

        if (children != null && !children.isEmpty()) {
            for (AmuaDTNode<?> childNode: children) {
                // recursive call
                writeAmuaNode(childNode, treeElem);
            }
        }
    }


    /**
     * Writes the cost information of a node into the corresponding XML element.
     *
     * @param amuaNode the node whose cost information will be exported
     * @param nodeElem the XML element representing the node
     */
    private void writeNodeCost(AmuaDTNode<?> amuaNode, Element nodeElem) {
        if (amuaNode instanceof AmuaDTUnicriteriaNode unicriteriaNode) {
            addElement(nodeElem, "cost", unicriteriaNode.getCost());
        } else if ( amuaNode instanceof AmuaDTCENode ceNode){
            AmuaCEvalue cost = ceNode.getCost();
            if(cost != null){
                addElement(nodeElem, "cost", df.format(cost.getCost()));
                addElement(nodeElem, "cost", df.format(cost.getEffectiveness()));
            }
        }
    }


    /**
     * Writes the payoff information of a node into the corresponding XML element.
     *
     * @param amuaNode the node whose payoff information will be exported
     * @param nodeElem the XML element representing the node
     */
    private void writeNodePayoff(AmuaDTNode<?> amuaNode, Element nodeElem){
        if (amuaNode instanceof AmuaDTUnicriteriaNode unicriteriaNode) {
            addElement(nodeElem, "payoff", unicriteriaNode.getPayoff());
        } else if ( amuaNode instanceof AmuaDTCENode ceNode){
            AmuaCEvalue payoff = ceNode.getPayoff();
            if (payoff != null) {
                addElement(nodeElem, "payoff", df.format(payoff.getCost()));
                addElement(nodeElem, "payoff", df.format(payoff.getEffectiveness()));
            }
        }
    }


    /**
     * Adds a child XML element to a parent element only if the provided value is not null.
     *
     * @param parent the parent XML element to which the new element will be added
     * @param name the tag name of the child element
     * @param value the value to be set as text content
     */
    private void addElement(Element parent, String name, Object value) {
        if (value != null) {
            parent.addContent(new Element(name).setText(String.valueOf(value)));
        }
    }

}
