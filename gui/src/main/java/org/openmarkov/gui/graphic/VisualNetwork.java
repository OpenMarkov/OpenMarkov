/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.base.linkEdits.MultiAddLinkEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;
import org.openmarkov.gui.action.RemoveSelectedEdit;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.util.MovedNodeInfo;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;
import org.openmarkov.gui.window.edition.SelectedContent;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class implements the visual representation of a network.
 *
 * @author jmendoza 1.0
 * @author jlgozalo 1.1
 * @version 1.2 asaez - modified the constructor, the creation of
 * visual nodes and the order of painting nodes
 */
public class VisualNetwork implements PNEditListener {
    
    /**
     * Network whose visual representation is managed by this object.
     */
    private final ProbNet probNet;
    private final NetworkEditorPanel networkEditorPanel;
    
    /**
     * This variable indicates if nodes must be drawn by title.
     */
    // TODO este valor debe asignarse usando las preferencias de usuario de
    // visualización de redes
    private boolean byTitle = false;
    
    /**
     * List of visual nodes.
     */
    private List<VisualNode> visualNodes = new ArrayList<VisualNode>();
    
    /**
     * List of visual links.
     */
    private final List<VisualLink> visualLinks = new ArrayList<VisualLink>();
    
    /**
     * Set of selected nodes.
     */
    private final LinkedHashSet<VisualElement> selectedElements = new LinkedHashSet<>();
    
    
    record NewLinkInfo(VisualArrow arrow, VisualNode source) {
    }
    
    /**
     * This object represents the arrow that is painted when a new link is being
     * created.
     */
    private @Nullable List<NewLinkInfo> newLinks = new ArrayList<>();
    private LinkCreationSourceDirection newLinksSourceDirection;
    
    public Stream<VisualArrow> getNewLinksArrows() {
        return this.newLinks.stream().map(NewLinkInfo::arrow);
    }
    
    
    /**
     * Rectangle used to select various nodes.
     */
    protected SelectionRectangle selection = null;
    
    public SelectionRectangle getSelection() {
        return this.selection;
    }
    
    private boolean isPropagationActive = true;
    
    private NetworkEditorPanel.WorkingMode workingMode = NetworkEditorPanel.WorkingMode.EDITION;
    
    /**
     * Listener to the selection.
     */
    private final Set<SelectionListener> selectionListeners = new HashSet<SelectionListener>();
    
    
    //private LinkWrapper linkWrapper;
    /**
     * Position of the mouse cursor when it is pressed.
     */
    
    /**
     * Creates a new visual network.
     *
     * @param probNet            object that has the information of the network.
     * @param networkEditorPanel
     */
    public VisualNetwork(ProbNet probNet, NetworkEditorPanel networkEditorPanel) {
        this.probNet = probNet;
        this.networkEditorPanel = networkEditorPanel;
        this.probNet.getPNESupport().addListener(this);
        
        //network.addNetworkChangeListener(this);
        //changed by mpalacios
        constructVisualInfo();
    }
    
    public ProbNet getProbNet() {
        return this.probNet;
    }
    
    /**
     * Calculates the width and height of the panel according to the position of
     * the left-most and bottom-most nodes.
     *
     * @param g graphics where the network is painted.
     *
     * @return an array that contains the lowest and highest X coordinate and
     * the lowest and highest Y coordinate.
     */
    public double[] getNetworkBounds(Graphics2D g) {
        
        double[] networkBounds = {Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE};
        
        for (VisualNode node : this.visualNodes) {
            Rectangle2D nodeBounds = node.getShape(g).getBounds2D();
            networkBounds[0] = Math.min(nodeBounds.getMinX(), networkBounds[0]);
            networkBounds[1] = Math.max(nodeBounds.getMaxX(), networkBounds[1]);
            networkBounds[2] = Math.min(nodeBounds.getMinY(), networkBounds[2]);
            networkBounds[3] = Math.max(nodeBounds.getMaxY(), networkBounds[3]);
        }
        networkBounds[0] -= 2;
        networkBounds[1] += 2;
        networkBounds[2] -= 2;
        networkBounds[3] += 2;
        
        return networkBounds;
        
    }
    
    @FunctionalInterface
    interface NodeIsToKeep {
        boolean nodeIsToKeep(VisualNode visualNode, List<Node> currentNodesToAdd);
    }
    
    /**
     * This method constructs the lists of the visual nodes and visual links. It
     * only creates visual information for the new nodes and links and delete
     * the visual representation of the nodes and links that don't exist.
     */
    private void constructVisualInfo() {
        reconstructVisualInfo((visualNode, currentNodesToAdd) ->
                                      currentNodesToAdd.contains(visualNode.getNode())
                                              && visualNode.getTemporalPosition().getX()
                                              == currentNodesToAdd.get(currentNodesToAdd.indexOf(visualNode.getNode()))
                                                                  .getCoordinateX()
                                              && visualNode.getTemporalPosition().getX()
                                              == currentNodesToAdd.get(currentNodesToAdd.indexOf(visualNode.getNode()))
                                                                  .getCoordinateX());
    }
    
    private void reconstructVisualInfo(NodeIsToKeep nodeIsToKeep) {
        List<VisualNode> visualNodesToDelete = new ArrayList<VisualNode>();
        List<Node> nodesToAdd = this.probNet.getNodes();
        for (VisualNode visualNode : this.visualNodes) {
            if (nodeIsToKeep.nodeIsToKeep(visualNode, Collections.unmodifiableList(nodesToAdd))) {
                nodesToAdd.remove(visualNode.getNode());
            } else {
                visualNodesToDelete.add(visualNode);
            }
        }
        this.visualNodes.removeAll(visualNodesToDelete);
        for (Node node : nodesToAdd) {
            VisualNode visualNode = createVisualNode(node);
            this.visualNodes.add(visualNode);
            visualNode.setByTitle(this.byTitle);
        }
        List<Link<Node>> links = this.probNet.getLinks();
        List<VisualLink> vLinksToDelete = new ArrayList<VisualLink>();
        for (VisualLink vLink : this.visualLinks) {
            Link<Node> linkToCheck = vLink.getLink();
            if (links.contains(linkToCheck) && !containsNodeToDelete(linkToCheck, visualNodesToDelete)) {
                links.remove(linkToCheck);
            } else {
                vLinksToDelete.add(vLink);
            }
        }
        this.visualLinks.removeAll(vLinksToDelete);
        int visualNodesCount = this.visualNodes.size();
        for (Link<Node> link : links) {
            VisualNode vNode1 = null;
            VisualNode vNode2 = null;
            int i = 0;
            while ((i < visualNodesCount) && ((vNode1 == null) || (vNode2 == null))) {
                if (vNode1 == null) {
                    if (link.getFrom().equals(this.visualNodes.get(i).getNode())) {
                        vNode1 = this.visualNodes.get(i);
                    }
                }
                if (vNode2 == null) {
                    if (link.getTo().equals(this.visualNodes.get(i).getNode())) {
                        vNode2 = this.visualNodes.get(i);
                    }
                }
                i++;
            }
            if ((vNode1 != null) && (vNode2 != null)) {
                this.visualLinks.add(new VisualLink(link, vNode1, vNode2));
            }
        }
    }
    
    /**
     * Returns whether the link contains nodes to delete
     *
     * @param linkToCheck    the link to check
     * @param vNodesToDelete the v nodes to delete
     *
     * @return True iff the link contains the node to delete
     */
    private static boolean containsNodeToDelete(Link<Node> linkToCheck, List<VisualNode> vNodesToDelete) {
        
        for (VisualNode vNode : vNodesToDelete)
            if (linkToCheck.contains(vNode.getNode()))
                return true;
        
        return false;
    }
    
    /**
     * Returns the presentation mode of the foreground of the nodes.
     *
     * @return true if the title of the nodes is the name or false if it is the
     * name.
     */
    public boolean getByTitle() {
        return this.byTitle;
    }
    
    /**
     * Changes the presentation mode of the foreground of the nodes.
     *
     * @param value new value of the presentation mode of the foreground of the nodes.
     */
    public void setByTitle(boolean value) {
        
        if (this.byTitle != value) {
            this.byTitle = value;
        }
        for (VisualNode node : this.visualNodes) {
            node.setByTitle(value);
        }
        
    }
    
    /**
     * Creates a new list of visual nodes reordering them following
     * this criteria:
     * - first criteria: selection state -&gt; the selected nodes are in
     * the first places of the array.
     * - second criteria: relevance -&gt; the higher the relevance
     * the nearer to the start of the array.
     *
     * @return a new ordered array (first, selected nodes, and last,
     * non selected nodes; each group is ordered in
     * descending relevance criteria).
     */
    public void reorderVisualNodes() {
        ArrayList<VisualNode> nodesSelected = new ArrayList<VisualNode>();
        ArrayList<VisualNode> nodesUnselected = new ArrayList<VisualNode>();
        
        for (VisualNode node : this.visualNodes) {
            if (node.isSelected()) {
                nodesSelected.add(node);
            } else {
                nodesUnselected.add(node);
            }
        }
        
        int selected = nodesSelected.size();
        int counter1 = 0;
        ArrayList<VisualNode> newList = new ArrayList<VisualNode>();
        int selPos = 0;
        while (counter1 < selected) {
            VisualNode candidate = null;
            double highestRelevance = -1;
            for (int i = 0; i < nodesSelected.size(); i++) {
                double relevance = nodesSelected.get(i).getNode().getRelevance();
                if (relevance > highestRelevance) {
                    highestRelevance = relevance;
                    candidate = nodesSelected.get(i);
                }
            }
            newList.add(selPos, candidate);
            selPos++;
            nodesSelected.remove(candidate);
            counter1++;
        }
        
        int unselected = nodesUnselected.size();
        int counter2 = 0;
        while (counter2 < unselected) {
            VisualNode candidate = null;
            double highestRelevance = -1;
            for (int i = 0; i < nodesUnselected.size(); i++) {
                double relevance = nodesUnselected.get(i).getNode().getRelevance();
                if (relevance > highestRelevance) {
                    highestRelevance = relevance;
                    candidate = nodesUnselected.get(i);
                }
            }
            newList.add(selPos, candidate);
            selPos++;
            nodesUnselected.remove(candidate);
            counter2++;
        }
        this.visualNodes = newList;
    }
    
    /**
     * Checks if is there a node in a position. You must specify if the node
     * must be selected or not.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a (selected or not) node in the position, returns it,
     * else, returns null.
     */
    public VisualNode whatNodeInPosition(Point2D.Double position, Graphics2D g) {
        
        VisualNode nodeFound = null;
        int index = 0, length = this.visualNodes.size();
        
        while ((nodeFound == null) && (index < length)) {
            VisualNode node = this.visualNodes.get(index++);
            if (node.pointIsInsideShape(position, g)) {
                nodeFound = node;
            }
        }
        
        return nodeFound;
        
    }
    
    /**
     * Checks if is there a inner box in a position.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a inner box in the position, returns it,
     * else, returns null.
     */
    public InnerBox whatInnerBoxInPosition(Point2D.Double position, Graphics2D g) {
        
        InnerBox innerBoxFound = null;
        int index = 0;
        int nodesLength = this.visualNodes.size();
        while ((innerBoxFound == null) && (index < nodesLength)) {
            VisualNode node = this.visualNodes.get(index++);
            if (node.pointIsInsideShape(position, g)) {
                InnerBox innerBox = node.getInnerBox();
                if (innerBox.pointIsInsideShape(position, g)) {
                    innerBoxFound = innerBox;
                }
            }
        }
        return innerBoxFound;
    }
    
    /**
     * Checks if is there a visual state in a position.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a visual state in the position, returns it,
     * else, returns null.
     */
    public @Nullable VisualState whatStateInPosition(Point2D.Double position, Graphics2D g) {
        for (VisualNode node : this.visualNodes) {
            if (node.pointIsInsideShape(position, g) && node.getInnerBox() instanceof FSVariableBox fsVariableBox) {
                int numStates = node.getInnerBox().getNumStates();
                for (int i = 0; i < numStates; i++) {
                    VisualState state = fsVariableBox.getVisualState(i);
                    if (state.pointIsInsideShape(position, g)) {
                        return state;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if is there a link in a position. You must specify if the link
     * must be selected or not.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if there is a (selected or not) link in the position, returns it,
     * else, returns null.
     */
    public @Nullable VisualLink whatLinkInPosition(Point2D.Double position, Graphics2D g) {
        int index = 0;
        int length = this.visualLinks.size();
        while (index < length) {
            VisualLink link = this.visualLinks.get(index++);
            if (link.pointIsInsideShape(position, g)) {
                return link;
            }
        }
        return null;
        
    }
    
    /**
     * Checks if is there any selected element in a position.
     *
     * @param position position to be checked.
     * @param g        graphics where the network is painted.
     *
     * @return if a selected element is there in the position, returns the
     * element, else returns null.
     */
    public VisualElement getElementInPosition(Point2D.Double position, Graphics2D g) {
        VisualElement elementSelected;
        if ((elementSelected = whatNodeInPosition(position, g)) == null) {
            elementSelected = whatLinkInPosition(position, g);
        }
        return elementSelected;
    }
    
    /**
     * Sets the selection state of an element.
     *
     * @param element  element to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectionOfElement(VisualElement element, boolean selected) {
        boolean selectionWasAlreadyDone = selected == element.isSelected();
        if (selected) {
            this.selectedElements.addLast(element);
        } else {
            this.selectedElements.remove(element);
        }
        if (selectionWasAlreadyDone) {
            return;
        }
        notifyObjectsSelected();
        element.setSelected(selected);
    }
    
    /**
     * Sets the selection state of a node.
     *
     * @param node     node to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectedNode(VisualNode node, boolean selected) {
        
        setSelectionOfElement(node, selected);
        
    }
    
    /**
     * Sets the selection state of a node identified by its name.
     *
     * @param name     name of the node to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectedNode(String name, boolean selected) {
        
        boolean found = false;
        int i = 0, l = this.visualNodes.size();
        
        while (!found && (i < l)) {
            if (this.visualNodes.get(i).getNode().getName().equals(name)) {
                setSelectionOfElement(this.visualNodes.get(i), selected);
                found = true;
            } else {
                i++;
            }
        }
        
    }
    
    /**
     * Sets the selection state of a link.
     *
     * @param link     link to be selected/deselected.
     * @param selected new selection state.
     */
    private void setSelectedLink(VisualLink link, boolean selected) {
        
        setSelectionOfElement(link, selected);
        
    }
    
    /**
     * Sets the selection state of a link.
     *
     * @param link     link to be selected/deselected.
     * @param selected new selection state.
     */
    public void setSelectedLink(Link<Node> link, boolean selected) {
        int i = 0;
        VisualLink visualLink = null;
        while (visualLink == null && i < this.visualLinks.size()) {
            if (this.visualLinks.get(i).getLink().equals(link)) {
                visualLink = this.visualLinks.get(i);
            }
            ++i;
        }
        if (visualLink != null) {
            setSelectionOfElement(visualLink, selected);
        }
    }
    
    /**
     * Selects all nodes.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllNodes(boolean selected) {
        for (VisualNode node : this.visualNodes) {
            setSelectionOfElement(node, selected);
        }
        if (!selected) {
            this.selectedElements.removeIf(VisualNode.class::isInstance);
        }
        
    }
    
    /**
     * Selects all links.
     *
     * @param selected new selection state.
     */
    private void setSelectedAllLinks(boolean selected) {
        for (VisualLink link : this.visualLinks) {
            setSelectionOfElement(link, selected);
        }
        if (!selected) {
            this.selectedElements.removeIf(VisualLink.class::isInstance);
        }
    }
    
    /**
     * Selects all nodes and links.
     *
     * @param selected new selection state.
     */
    public void setSelectedAllObjects(boolean selected) {
        setSelectedAllNodes(selected);
        setSelectedAllLinks(selected);
    }
    
    /**
     * Move some nodes an amount in both axis. The parameter 'selected'
     * indicates if the nodes must be selected or it doesn't mind.
     *
     * @param diffX    X-axis movement.
     * @param diffY    Y-axis movement.
     * @param selected if true, only the selected nodes are moved; if false, all
     *                 nodes are moved.
     */
    private void moveNodes(double diffX, double diffY, boolean selected) {
        for (VisualNode node : this.visualNodes) {
            if (!selected || (node.isSelected())) {
                Point2D.Double originalPosition = node.getTemporalPosition();
                double newPosX = originalPosition.getX() + diffX;
                double newPosY = originalPosition.getY() + diffY;
                boolean isValidPlace = newPosX >= Math.min(originalPosition.getX(), 0) && newPosY >= Math.min(originalPosition.getY(), 0);
                if (isValidPlace) {
                    node.setTemporalPosition(new Point2D.Double(newPosX, newPosY));
                    this.networkEditorPanel.repaint();
                }
            }
        }
        
    }
    
    /**
     * Move the selected elements an amount in both axis.
     *
     * @param diffX X-axis movement.
     * @param diffY Y-axis movement.
     */
    public void moveSelectedElements(double diffX, double diffY) {
        moveSelectedNodes(diffX, diffY);
    }
    
    /**
     * Move the selected nodes an amount in both axis.
     *
     * @param diffX X-axis movement.
     * @param diffY Y-axis movement.
     */
    private void moveSelectedNodes(double diffX, double diffY) {
        
        moveNodes(diffX, diffY, true);
        
    }
    
    /**
     * Move all the nodes an amount in both axis.
     *
     * @param diffX X-axis movement.
     * @param diffY Y-axis movement.
     */
    protected void moveAllNodes(double diffX, double diffY) {
        
        moveNodes(diffX, diffY, false);
        
    }
    
    /**
     * Selects the nodes and links that are inside the selection rectangle and deselects
     * the ones that are outside.
     *
     * @param selection object that manages the selection.
     */
    private void selectElementsInsideSelection(SelectionRectangle selection) {
        
        setSelectedAllNodes(false);
        setSelectedAllLinks(false);
        // Select nodes
        ArrayList<VisualNode> selectedVisualNodes = new ArrayList<VisualNode>();
        for (VisualNode node : this.visualNodes) {
            if (selection.containsNode(node)) {
                setSelectionOfElement(node, true);
                selectedVisualNodes.add(node);
            }
        }
        // Select links
        for (VisualLink selectedLink : getLinksOfNodes(selectedVisualNodes, true)) {
            setSelectionOfElement(selectedLink, true);
        }
    }
    
    /**
     * Fills the array of information of the selected nodes and their actual
     * state.
     *
     * @return list where are the moved nodes information.
     */
    public List<MovedNodeInfo> fillActualNodesMovedInfo() {
        
        List<MovedNodeInfo> movedNodes = new ArrayList<MovedNodeInfo>();
        
        for (VisualNode node : this.visualNodes) {
            if (node.isSelected()) {
                movedNodes.add(new MovedNodeInfo(node.getNode(), node.getPosition()));
            }
        }
        
        return movedNodes;
        
    }
    
    /**
     * Fills the array of information of the selected nodes and the differences
     * of their state.
     *
     * @param movedNodes list where is saved the moved nodes information.
     */
    public static void fillDifferencesNodesMovedInfo(List<MovedNodeInfo> movedNodes) {
        
        for (MovedNodeInfo movedNode : movedNodes) {
            Node node = movedNode.getNode();
            movedNode.setDiffPosition(new Point2D.Double(node.getCoordinateX() - movedNode.getDiffPosition().getX(),
                                                         node.getCoordinateY() - movedNode.getDiffPosition().getY()));
        }
        
    }
    
    /**
     * Fills the array of information of the selected nodes and their actual
     * state.
     *
     * @return list where are the moved nodes information.
     */
    public List<VisualNode> fillVisualNodesSelected() {
        return this.visualNodes.stream()
                               .filter(VisualElement::isSelected)
                               .collect(Collectors.toList());
        
    }
    
    /**
     * This method returns a list containing all the nodes in the network.
     *
     * @return a list containing all the nodes in the network.
     */
    public List<VisualNode> getAllNodes() {
        return this.visualNodes;
    }
    
    /**
     * This method returns a list that contains all the links that leave of or
     * arrive in one node of the list of nodes passed as parameter.
     *
     * @param nodes        list of nodes whose links are returned.
     * @param onlyBothEnds returns only those links whose two ends are selected
     *
     * @return a list of links related to the nodes.
     */
    private List<VisualLink> getLinksOfNodes(List<VisualNode> nodes, boolean onlyBothEnds) {
        ArrayList<VisualLink> links = new ArrayList<VisualLink>();
        int l = nodes.size();
        for (VisualLink visualLink : this.visualLinks) {
            boolean found = false;
            boolean foundSource = false;
            boolean foundDestination = false;
            int i = 0;
            while (!found && (i < l)) {
                foundSource |= visualLink.getSourceNode().equals(nodes.get(i));
                foundDestination |= visualLink.getDestinationNode().equals(nodes.get(i));
                found = (onlyBothEnds) ? foundSource && foundDestination : foundSource || foundDestination;
                if (found) {
                    links.add(visualLink);
                } else {
                    i++;
                }
            }
        }
        return links;
    }
    
    public List<VisualLink> getVisualLinks() {
        return this.visualLinks;
    }
    
    /**
     * This method returns a list that contains all the links that leave of or
     * arrive in one node of the list of nodes passed as parameter.
     *
     * @param nodes list of nodes whose links are returned.
     *
     * @return a list of links related to the nodes.
     */
    public List<VisualLink> getLinksOfNodes(List<VisualNode> nodes) {
        return getLinksOfNodes(nodes, false);
    }
    
    /**
     * Sets a new selection listener.
     *
     * @param listener listener to be set.
     */
    public void addSelectionListener(SelectionListener listener) {
        this.selectionListeners.add(listener);
    }
    
    /**
     * This method returns a list containing the selected nodes.
     *
     * @return a list containing the selected nodes.
     */
    public List<VisualNode> getSelectedNodes() {
        return new ArrayList<>(getSelectedElementsOf(VisualNode.class).toList());
    }
    
    public VisualNode getLastSelectedNode() {
        return getSelectedElementOf(VisualNode.class);
    }
    
    private <T extends VisualElement> Stream<T> getSelectedElementsOf(Class<? extends T> type) {
        return this.selectedElements.stream()
                                    .filter(type::isInstance)
                                    .map(type::cast);
    }
    
    private <T extends VisualElement> @Nullable T getSelectedElementOf(Class<T> type) {
        var selectedElements = this.getSelectedElementsOf(type).toList();
        if (selectedElements.isEmpty()) {
            return null;
        }
        return selectedElements.getLast();
    }
    
    /**
     * This method returns a list containing the selected links.
     *
     * @return a list containing the selected links.
     */
    public List<VisualLink> getSelectedLinks() {
        return new ArrayList<VisualLink>(getSelectedElementsOf(VisualLink.class).toList());
    }
    
    public VisualLink getLastSelectedLink() {
        return getSelectedElementOf(VisualLink.class);
    }
    
    /**
     * Notifies to the registered selection listener how many nodes and links
     * are selected, and which are the especific selected nodes.
     * Also notifies this situation to the menu assistant.
     */
    private void notifyObjectsSelected() {
        
        for (SelectionListener listener : this.selectionListeners) {
            listener.objectsSelected();
        }
    }
    
    /**
     * Returns the network that is painted by this object.
     *
     * @return network which is painted.
     */
    public ProbNet getNetwork() {
        return this.probNet;
    }
    
    public PNESupport getpNESupport() {
        //review method
        return this.probNet.getPNESupport();
        
    }
    
    @Override public void afterEditExecutes(PNEdit edit) {
        constructVisualInfo();
    }
    
    private void visualDecisionNodeRefresh() {
        reconstructVisualInfo((visualNode, currentNodesToAdd)
                                      -> visualNode.getNode().getNodeType() != NodeType.DECISION);
    }
    
    /**
     * Returns different types of visual nodes according to the supplied node.
     *
     * @param node node whose visual representation is going to be returned.
     *
     * @return the visual representation of the node.
     */
    private VisualNode createVisualNode(Node node) {
        return switch (node.getNodeType()) {
            case CHANCE, DECISION, UTILITY, EVENT -> new VisualNode(node, this);
            default -> null;
        };
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        refreshUI();
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        refreshUI();
    }
    
    private void refreshUI() {
        constructVisualInfo();
        if (getWorkingMode() != NetworkEditorPanel.WorkingMode.INFERENCE) {
            visualDecisionNodeRefresh();
        }
    }
    
    /**
     * Adds whatever is in that position to the selection
     *
     * @param cursorPosition the cursor position
     * @param g              the g
     */
    public void addToSelection(Point2D.Double cursorPosition, Graphics2D g) {
        VisualNode node;
        VisualLink link;
        
        if ((node = whatNodeInPosition(cursorPosition, g)) != null) {
            setSelectedNode(node, !node.isSelected());
        } else if ((link = whatLinkInPosition(cursorPosition, g)) != null) {
            setSelectedLink(link, !link.isSelected());
        }
    }
    
    /**
     * Cleans selection and sets it to whatever is in the cursorPosition
     *
     * @param cursorPosition the cursor position
     * @param g              the g
     *
     * @return true if there is an element in the position
     */
    public @Nullable VisualElement selectElementInPosition(Point2D.Double cursorPosition, Graphics2D g) {
        VisualNode node = whatNodeInPosition(cursorPosition, g);
        if (node != null) {
            if (!node.isSelected()) {
                setSelectedAllObjects(false);
                setSelectedNode(node, true);
            }
            return node;
        }
        VisualLink link = whatLinkInPosition(cursorPosition, g);
        if (link != null) {
            if (!link.isSelected()) {
                setSelectedAllObjects(false);
                setSelectedLink(link, true);
            }
            return link;
        }
        setSelectedAllObjects(false);
        return null;
    }
    
    public VisualNode getVisualNodeOf(Node node) {
        return this.visualNodes.stream().filter(visualNode -> visualNode.getNode() == node).findFirst().orElse(null);
    }
    
    public enum LinkCreationSourceDirection {
        PARENT, CHILD;
    }
    
    /**
     * Starts link creation
     *
     * @param selectedNodes
     * @param cursorPosition the cursor position
     * @param g              the g
     */
    public void startLinkCreation(Point2D.Double cursorPosition, Graphics2D g, LinkCreationSourceDirection linkSourceDirection, boolean preserveLinkSourceDirection, List<VisualNode> selectedNodes) {
        if (!this.newLinks.isEmpty()) {
            return;
        }
        if(!preserveLinkSourceDirection){
            newLinksSourceDirection = linkSourceDirection;
        }
        selectedNodes.stream().map(node -> {
            Rectangle2D nodeBounds = node.getShape(g).getBounds2D();
            Point2D.Double nodePoint = new Point2D.Double(nodeBounds.getCenterX(), nodeBounds.getCenterY());
            return new NewLinkInfo(
                    new VisualArrow(switch (this.newLinksSourceDirection) {
                        case PARENT -> nodePoint;
                        case CHILD -> cursorPosition;
                    }, switch (this.newLinksSourceDirection) {
                        case PARENT -> cursorPosition;
                        case CHILD -> nodePoint;
                    }, true),
                    node
            );
        }).forEach(this.newLinks::add);
        updateLinkCreation(cursorPosition, g);
    }
    
    public void updateLinkCreation(Point2D.Double position, Graphics2D g) {
        if (this.newLinks.isEmpty()) {
            return;
        }
        @Nullable VisualNode destinationNode = whatNodeInPosition(position, g);
        var multiAddLinkEdit = generateMultiAddLinkInCreation(destinationNode);
        var addLinks = multiAddLinkEdit.getEdits()
                                       .filter(AddLinkEdit.class::isInstance)
                                       .map(AddLinkEdit.class::cast)
                                       .toList();
        for (NewLinkInfo newLink : this.newLinks) {
            var linkSource = newLink.source;
            var linkArrow = newLink.arrow;
            if (linkSource == destinationNode && probNet.hasConstraintOfClass(NoSelfLoop.class)) {
                linkArrow.setLinkColor(GUIColors.General.TRANSPARENT);
                continue;
            }
            try {
                Tuple2Record<Point2D.Double, Point2D.Double> points = VisualLink.shortenedPoints(g, linkSource, destinationNode, position);
                switch (this.newLinksSourceDirection) {
                    case PARENT -> {
                        linkArrow.setStartPoint(points.v0());
                        linkArrow.setEndPoint(points.v1());
                    }
                    case CHILD -> {
                        linkArrow.setStartPoint(points.v1());
                        linkArrow.setEndPoint(points.v0());
                    }
                }
                linkArrow.setSelfLoop(linkSource == destinationNode);
            } catch (VisualLink.LinkCannotBePaintedException e) {
                //Do not update positions
            }
            var addLinkEdit = addLinks.stream()
                                      .filter(switch (this.newLinksSourceDirection){
                                          case PARENT -> (Predicate<AddLinkEdit>) linkEdit ->  linkEdit.getNodeFrom() == newLink.source.getNode();
                                          case CHILD -> (Predicate<AddLinkEdit>)linkEdit ->  linkEdit.getNodeTo() == newLink.source.getNode();
                                      })
                                      .findFirst()
                                      .orElse(null);
            linkArrow.setLinkColor(addLinkEdit == null ? GUIColors.Network.Link.Creation.FOREGROUND_ON_SELECTS_NOTHING : addLinkEdit.constraintsWillBeMet() ? GUIColors.Network.Link.Creation.FOREGROUND_ON_SELECTS_SUCCESS : GUIColors.Network.Link.Creation.FOREGROUND_ON_SELECTS_FAILURE);
        }
        
        
    }
    
    /**
     * Finishes link creation and returns edit for the new link
     *
     * @param point the point
     * @param g     the g
     *
     * @return The edit for the new link created
     */
    public void finishLinkCreation(Point2D.Double point, Graphics2D g) throws DoEditException {
        @Nullable VisualNode newLinkDestination = whatNodeInPosition(point, g);
        if (this.newLinks.isEmpty() || newLinkDestination == null) {
            cancelLinkCreation();
            return;
        }
        try {
            MultiAddLinkEdit multiAddLinkEdit = generateMultiAddLinkInCreation(newLinkDestination);
            if(multiAddLinkEdit.getEdits().findFirst().isPresent()){
                multiAddLinkEdit.executeEdit();
            }
        } catch (DoEditException e) {
            if (e.failedEdit instanceof AddLinkEdit failedAddLinkEdit) {
                var failedLink = this.newLinks.stream()
                                              .filter(newLinkInfo -> newLinkInfo.source.getNode() == failedAddLinkEdit.getNodeFrom())
                                              .findFirst()
                                              .get();
                failedLink.arrow.setLinkColor(GUIColors.Network.Link.Creation.FOREGROUND_ON_SELECTS_FAILURE);
            }
            throw e;
        }
        cancelLinkCreation();
    }
    
    private @NotNull MultiAddLinkEdit generateMultiAddLinkInCreation(@Nullable VisualNode destinationNode) {
        List<Node> sources = this.newLinks.stream()
                                          .map(newLinkInfo -> newLinkInfo.source()
                                                                         .getNode())
                                          .toList();
        List<Node> destinations = destinationNode == null ? Collections.emptyList() : List.of(destinationNode.getNode());
        var multiAddLinkEdit = new MultiAddLinkEdit(this.probNet, switch (this.newLinksSourceDirection) {
            case PARENT -> sources;
            case CHILD -> destinations;
        }, switch (this.newLinksSourceDirection){
            case PARENT -> destinations;
            case CHILD -> sources;
        }, true);
        return multiAddLinkEdit;
    }
    
    public void toggleLinkCreationSource(Point2D.Double point) {
        this.newLinksSourceDirection = switch (this.newLinksSourceDirection){
            case PARENT -> LinkCreationSourceDirection.CHILD;
            case CHILD -> LinkCreationSourceDirection.PARENT;
        };
        updateLinkCreation(point, (Graphics2D) this.networkEditorPanel.getGraphics());
    }
    
    public void cancelLinkCreation() {
        this.newLinks.clear();
        this.networkEditorPanel.repaint();
    }
    
    
    public void startSelectionRectangle(Point2D.Double position) {
        this.selection = new SelectionRectangle();
        this.selection.initSelection(position, 0, 0);
    }
    
    public void finishSelectionRectangle(Point2D.Double position) {
        this.selection.clearSelectionSquare();
    }
    
    public void updateSelectionRectangle(double diffX, double diffY) {
        this.selection.setSize(this.selection.getWidth() + diffX, this.selection.getHeight() + diffY);
        selectElementsInsideSelection(this.selection);
    }
    
    /**
     * Returns the isPropagationActive.
     *
     * @return the isPropagationActive.
     */
    boolean isPropagationActive() {
        return this.isPropagationActive;
    }
    
    /**
     * Sets the isPropagationActive.
     *
     * @param isPropagationActive the isPropagationActive to set.
     */
    public void setPropagationActive(boolean isPropagationActive) {
        this.isPropagationActive = isPropagationActive;
    }
    
    public NetworkEditorPanel.WorkingMode getWorkingMode() {
        return this.workingMode;
    }
    
    public void setWorkingMode(NetworkEditorPanel.WorkingMode workingMode) {
        this.workingMode = workingMode;
    }
    
    //TODO OOPN end
    
    public boolean isSelected(VisualElement element) {
        return this.selectedElements.contains(element);
    }
    
    /**
     * This method copies the selected nodes to the clipboard.
     *
     * @param cut if true, the nodes copied to the clipboard are also removed.
     */
    public void exportToClipboard(boolean cut, EditorPanelClipboardAssistant clipboardAssistant) {
        List<Node> selectedNodes = this
                .getSelectedNodes().stream().map(VisualNode::getNode).toList();
        List<Link<Node>> selectedLinks = this
                .getSelectedLinks().stream().map(VisualLink::getLink).toList();
        SelectedContent copiedContent = new SelectedContent(selectedNodes, selectedLinks);
        if (!copiedContent.isEmpty()) {
            clipboardAssistant.copyToClipboard(copiedContent);
            if (cut) {
                this.removeSelectedObjects();
            }
        }
    }
    
    /**
     * Removes selected objects
     */
    public void removeSelectedObjects() {
        try {
            new RemoveSelectedEdit(this).executeEdit();
            this.setSelectedAllObjects(false);
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
    }
    
}
