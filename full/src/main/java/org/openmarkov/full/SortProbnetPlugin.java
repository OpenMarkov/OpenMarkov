package org.openmarkov.full;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.LayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

/**
 * Tool plugin that rearranges the visual layout of a probabilistic network using
 * the Fruchterman-Reingold force-directed algorithm from JGraphT.
 */
public class SortProbnetPlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.UNCATEGORIZED;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    public boolean enabled() {
        return MainPanel.getCurrentNetworkEditorPanel() != null && !MainPanel.getCurrentNetworkEditorPanel()
                                                                       .getProbNet()
                                                                       .getLinks()
                                                                       .isEmpty();
    }
    
    private static final double MIN_BOX_WIDTH = 100.0;
    private static final double MIN_BOX_HEIGHT = 100.0;
    
    private static final int TOP_LEFT_CORNER_SIZE = 30;
    private static final int BOTTOM_RIGHT_CORNER_SIZE = 50;
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Sort probnet")
                .enabled(MainPanel.getCurrentNetworkEditorPanel() != null
                                 && !MainPanel.getCurrentNetworkEditorPanel().getProbNet().getLinks().isEmpty())
                .onClick(() -> {
                    var panel = MainPanel.getCurrentNetworkEditorPanel().getEditorPanel();
                    double desiredWidth = panel.getVisibleRect().getWidth()
                            - SortProbnetPlugin.TOP_LEFT_CORNER_SIZE - SortProbnetPlugin.BOTTOM_RIGHT_CORNER_SIZE;
                    double desiredHeight = panel.getVisibleRect().getHeight()
                            - SortProbnetPlugin.TOP_LEFT_CORNER_SIZE - SortProbnetPlugin.BOTTOM_RIGHT_CORNER_SIZE;
                    SortProbnetPlugin.graphicallySortNetworkAsMoveEdit(panel, desiredWidth, desiredHeight);
                })
                .build();
    }
    
    /**
     * Applies force-directed layout to the network and directly sets node positions (no undo support).
     *
     * @param panel         the editor panel containing the visual network
     * @param desiredWidth  the target layout width in pixels
     * @param desiredHeight the target layout height in pixels
     */
    public static void graphicallySortNetwork(NetworkEditorPanel panel, double desiredWidth, double desiredHeight) {
        SortedCoordinates sortedCoordinates = SortProbnetPlugin.getSortedCoordinates(panel, desiredWidth, desiredHeight);
        for (VisualNode vertex : sortedCoordinates.graph().vertexSet()) {
            Point2D position = sortedCoordinates.model().get(vertex);
            double xPos = Math.max(0.0, position.getX() - sortedCoordinates.minX());
            double yPos = Math.max(0.0, position.getY() - sortedCoordinates.minY());
            vertex.setPosition(new org.openmarkov.core.model.network.Point2D.Double(xPos, yPos));
        }
        panel.adjustPanelDimension();
    }
    
    private static void graphicallySortNetworkAsMoveEdit(NetworkEditorPanel panel, double desiredWidth, double desiredHeight) throws DoEditException {
        SortedCoordinates sortedCoordinates = SortProbnetPlugin.getSortedCoordinates(panel, desiredWidth, desiredHeight);
        for (VisualNode vertex : sortedCoordinates.graph().vertexSet()) {
            Point2D position = sortedCoordinates.model().get(vertex);
            double xPos = Math.max(0.0, position.getX() - sortedCoordinates.minX());
            double yPos = Math.max(0.0, position.getY() - sortedCoordinates.minY());
            vertex.setTemporalPosition(new org.openmarkov.core.model.network.Point2D.Double(xPos, yPos));
        }
        new MoveNodeEdit(sortedCoordinates.graph().vertexSet().stream().toList()).executeEdit();
    }
    
    private static @NotNull SortProbnetPlugin.SortedCoordinates getSortedCoordinates(NetworkEditorPanel panel, double desiredWidth, double desiredHeight) {
        var visualNetwork = panel.getVisualNetwork();
        Graph<VisualNode, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        LayoutModel2D<VisualNode> model = new MapLayoutModel2D<>(new Box2D(
                Math.max(SortProbnetPlugin.MIN_BOX_WIDTH, desiredWidth),
                Math.max(SortProbnetPlugin.MIN_BOX_HEIGHT, desiredHeight)));
        LayoutAlgorithm2D<VisualNode, DefaultEdge> layoutAlgorithm = new FRLayoutAlgorithm2D<>(
                FRLayoutAlgorithm2D.DEFAULT_ITERATIONS,
                FRLayoutAlgorithm2D.DEFAULT_NORMALIZATION_FACTOR,
                new java.util.Random(0));
        
        visualNetwork.getAllNodes().forEach(graph::addVertex);
        for (var link : visualNetwork.getVisualLinks()) {
            graph.addEdge(link.getSourceNode(), link.getDestinationNode());
            if (!link.isDirected()) {
                graph.addEdge(link.getDestinationNode(), link.getSourceNode());
            }
        }
        layoutAlgorithm.layout(graph, model);
        double minX = graph.vertexSet().stream().map(model::get).mapToDouble(Point2D::getX).min().getAsDouble();
        double minY = graph.vertexSet().stream().map(model::get).mapToDouble(Point2D::getY).min().getAsDouble();
        minX -= SortProbnetPlugin.TOP_LEFT_CORNER_SIZE;
        minY -= SortProbnetPlugin.TOP_LEFT_CORNER_SIZE;
        return new SortedCoordinates(visualNetwork, graph, model, minX, minY);
    }
    
    /** Holds the result of a force-directed layout computation. */
    private record SortedCoordinates(org.openmarkov.gui.graphic.VisualNetwork visualNetwork,
                                     Graph<VisualNode, DefaultEdge> graph, LayoutModel2D<VisualNode> model, double minX,
                                     double minY) {
    }
    
    
}
