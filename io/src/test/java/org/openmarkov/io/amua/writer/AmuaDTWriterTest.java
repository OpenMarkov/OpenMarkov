package org.openmarkov.io.amua.writer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;
import org.openmarkov.io.amua.model.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmuaDTWriterTest {

    private static final int DECISION = 0;
    private static final int CHANCE = 1;
    private static final int UTILITY = 2;

    private static final AmuaDimensionInfo dimUtility = new AmuaDimensionInfo("Utility", "U", 4);
    private static final AmuaDimensionInfo dimCost = new AmuaDimensionInfo("Cost", "C", 4);
    private static final AmuaDimensionInfo dimEffectiveness = new AmuaDimensionInfo("Effectiveness", "E", 4);

    @Test
    void writeToInvalidFileThrowsIOException() {
        AmuaDTUnicriteriaNode node = unicriteriaNode("Root", DECISION, 0);
        File file = new File("/invalid_path/tree.amua");
        assertThrows(IOException.class, () -> writeTree(node, unicriteriaDimensions(), file));
    }

    @Test
    void unicriteriaSingleNodeTest() throws Exception {
        AmuaDTUnicriteriaNode root = unicriteriaNode("RootNode", DECISION, 0);
        root.setCost(15.0);
        root.setPayoff(11.0);

        File file = File.createTempFile("UnicriteriaSingleNodeTest", ".amua");
        file.deleteOnExit();
        writeTree(root, unicriteriaDimensions(), file);
        Document doc = loadXML(file);

        Element rootElement = doc.getRootElement();

        assertEquals("Model", rootElement.getName());
        assertNotNull(rootElement.getChild("name"));
        assertNotNull(rootElement.getChild("tree"));

        List<Element> nodes = rootElement.getChild("tree").getChildren("Node");
        assertEquals(1, nodes.size());

        Element node = nodes.get(0);
        assertEquals("RootNode", node.getChildText("name"));
    }


    @Test
    void unicriteriaTreeTest() throws Exception {
        AmuaDTUnicriteriaNode root = unicriteriaNode("RootDecision", DECISION, 0);

        AmuaDTUnicriteriaNode child1 = unicriteriaNode("Chance1", CHANCE, 1);
        child1.setCost(40.0);

        AmuaDTUnicriteriaNode child2 = unicriteriaNode("Chance2", CHANCE, 3);
        child2.setCost(15.0);

        AmuaDTUnicriteriaNode terminal1 = terminalUnicriteria("Terminal1", 2, child1, 1, 0, 50);
        AmuaDTUnicriteriaNode terminal2 = terminalUnicriteria("Terminal2", 4, child2, 1, 0, 75);

        child1.setChildNodes(List.of(terminal1));
        child2.setChildNodes(List.of(terminal2));
        root.setChildNodes(List.of(child1, child2));

        File file = File.createTempFile("UnicriteriaSingleNodeTest", ".amua");
        file.deleteOnExit();
        writeTree(root, unicriteriaDimensions(), file);

        Document doc = loadXML(file);

        List<Element> nodes = doc.getRootElement().getChild("tree").getChildren("Node");
        assertEquals(5, nodes.size());

        Element rootNode = findNode(doc, "RootDecision");
        Element chance1Node = findNode(doc, "Chance1");
        Element chance2Node = findNode(doc, "Chance2");
        Element terminalNode1 = findNode(doc, "Terminal1");
        Element terminalNode2 = findNode(doc, "Terminal2");

        assertNotNull(rootNode);
        assertNotNull(chance1Node);
        assertNotNull(chance2Node);
        assertNotNull(terminalNode1);
        assertNotNull(terminalNode2);

        assertEquals("Chance1", chance1Node.getChildText("name"));
        assertEquals("Chance2", chance2Node.getChildText("name"));

        assertEquals("Terminal1", terminalNode1.getChildText("name"));
        assertEquals("Terminal2", terminalNode2.getChildText("name"));
    }


    @Test
    void ceSingleNodeTest() throws Exception {
        AmuaDTCENode root = ceNode("RootNode", DECISION, 0);
        root.setCost(new AmuaCEvalue(1000, 5));
        root.setPayoff(new AmuaCEvalue(2000, 10));

        File file = File.createTempFile("CESingleNodeTest", ".amua");
        file.deleteOnExit();

        writeTree(root, ceDimensions(), file);

        Document doc = loadXML(file);

        Element rootElement = doc.getRootElement();

        assertEquals("Model", rootElement.getName());
        assertNotNull(rootElement.getChild("name"));
        assertNotNull(rootElement.getChild("tree"));

        List<Element> nodes = rootElement.getChild("tree").getChildren("Node");
        assertEquals(1, nodes.size());

        Element node = nodes.get(0);
        assertEquals("RootNode", node.getChildText("name"));
    }


    @Test
    void ceTreeTest() throws Exception {
        AmuaDTCENode root = ceNode("RootNode", DECISION, 0);

        AmuaDTCENode child1 = ceNode("Chance1", CHANCE, 1);
        child1.setCost(new AmuaCEvalue(40, 2));

        AmuaDTCENode child2 = ceNode("Chance2", CHANCE, 3);
        child2.setCost(new AmuaCEvalue(15, 1));

        AmuaDTCENode terminal1 = terminalCE("Terminal1", 2, child1, 1, 0, 50, 5);
        AmuaDTCENode terminal2 = terminalCE("Terminal2", 4, child2, 1, 0, 75, 8);

        child1.setChildNodes(List.of(terminal1));
        child2.setChildNodes(List.of(terminal2));
        root.setChildNodes(List.of(child1, child2));

        File file = File.createTempFile("CETreeTest", ".amua");
        file.deleteOnExit();

        writeTree(root, ceDimensions(), file);

        Document doc = loadXML(file);

        List<Element> nodes = doc.getRootElement().getChild("tree").getChildren("Node");
        assertEquals(5, nodes.size());

        Element chance1Node = findNode(doc, "Chance1");
        Element chance2Node = findNode(doc, "Chance2");
        Element terminalNode1 = findNode(doc, "Terminal1");
        Element terminalNode2 = findNode(doc, "Terminal2");

        assertNotNull(chance1Node);
        assertNotNull(chance2Node);
        assertNotNull(terminalNode1);
        assertNotNull(terminalNode2);

        assertEquals("Chance1", chance1Node.getChildText("name"));
        assertEquals("Chance2", chance2Node.getChildText("name"));
    }


    private Element findNode(Document doc, String name) {
        return doc.getRootElement().getChild("tree").getChildren("Node").stream().filter(n -> name.equals(n.getChildText("name"))).findFirst().orElseThrow();
    }


    private Document loadXML(File file) throws Exception {
        return new SAXBuilder().build(file);
    }


    private void writeTree(AmuaDTNode<?> root, AmuaDTDimensions dimensions, File file) throws Exception {
        AmuaModel type = root instanceof AmuaDTUnicriteriaNode ? AmuaModel.UNICRITERIA_DT : AmuaModel.COST_EFFECTIVENESS_DT;
        AmuaDTWriter writer = new AmuaDTWriter(root, dimensions, type, file);
        writer.writeDT();

        assertTrue(file.exists());
    }


    private AmuaDTDimensions unicriteriaDimensions() {
        return new AmuaDTDimensions(List.of(dimUtility), 0, 0, 0, 0, 0, null, 0, 0);
    }


    private AmuaDTDimensions ceDimensions() {
        return new AmuaDTDimensions(List.of(dimCost, dimEffectiveness), 1, 0, 0, 0, 1, null, 10000, 0);
    }


    private AmuaDTUnicriteriaNode unicriteriaNode(String name, int type, int index) {
        AmuaDTUnicriteriaNode node = new AmuaDTUnicriteriaNode();

        node.setName(name);
        node.setType(type);
        node.setIndex(index);
        node.setLevel(0);

        node.setXPos(50);
        node.setYPos(50);
        node.setWidth(24);
        node.setHeight(24);

        node.setVisible(true);
        node.setCollapsed(false);

        return node;
    }


    private AmuaDTUnicriteriaNode terminalUnicriteria(String name, int index, AmuaDTUnicriteriaNode parent, double prob, double cost, double payoff) {
        AmuaDTUnicriteriaNode node = unicriteriaNode(name, UTILITY, index);

        node.setParentNode(parent);
        node.setProb(prob);
        node.setCost(cost);
        node.setPayoff(payoff);

        return node;
    }


    private AmuaDTCENode ceNode(String name, int type, int index) {
        AmuaDTCENode node = new AmuaDTCENode();

        node.setName(name);
        node.setType(type);
        node.setIndex(index);
        node.setLevel(0);

        node.setXPos(50);
        node.setYPos(50);
        node.setWidth(24);
        node.setHeight(24);

        node.setVisible(true);
        node.setCollapsed(false);
        node.setCost(new AmuaCEvalue(0, 0));
        node.setPayoff(new AmuaCEvalue(0, 0));

        return node;
    }


    private AmuaDTCENode terminalCE(String name, int index, AmuaDTCENode parent, double prob, double cost, double payoffCost, double payoffEffect) {
        AmuaDTCENode node = ceNode(name, UTILITY, index);

        node.setParentNode(parent);
        node.setProb(prob);

        node.setCost(new AmuaCEvalue(cost, 0));
        node.setPayoff(new AmuaCEvalue(payoffCost, payoffEffect));

        return node;
    }
}