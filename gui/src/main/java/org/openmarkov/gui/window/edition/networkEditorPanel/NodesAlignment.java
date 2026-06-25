package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.action.MoveNodeEdit;
import org.openmarkov.gui.graphic.VisualNode;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class NodesAlignment {
    
    
    private static final int HORIZONTAL_SPACING_IN_VERTICAL_ALIGN = 50;
    private static final int VERTICAL_SPACING_IN_HORIZONTAL_ALIGN = 30;
    
    public static void verticalAlign(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = new ArrayList<>(networkEditorPanel.getVisualNetwork().getSelectedNodes());
        var posX = networkEditorPanel.getVisualNetwork().getLastSelectedNode().getPosition().getX();
        var maxWidth = selectedNodes.stream()
                                    .mapToDouble(node -> node.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                                             .getBounds2D()
                                                             .getWidth())
                                    .max()
                                    .getAsDouble();
        
        selectedNodes.forEach(selectedNode -> {
            var nodeWidth = selectedNode.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                        .getBounds2D()
                                        .getWidth();
            
            selectedNode.setTemporalPosition(new Point2D.Double(posX + ((maxWidth - nodeWidth) / 2), selectedNode.getPosition()
                                                                                                                 .getY()));
        });
        try {
            new MoveNodeEdit(selectedNodes).executeEdit();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
    }
    
    public static boolean canVerticalAlign(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = networkEditorPanel.getVisualNetwork().getSelectedNodes();
        var posStatistics = selectedNodes.stream()
                                         .mapToDouble(selectedNode -> selectedNode.getShape((Graphics2D) networkEditorPanel.getGraphics()).getBounds2D().getCenterX())
                                         .summaryStatistics();
        return posStatistics.getMin() != posStatistics.getMax();
    }
    
    public static void horizontalAlign(NetworkEditorPanel networkEditorPanel) {
        
        
        var selectedNodes = new ArrayList<>(networkEditorPanel.getVisualNetwork().getSelectedNodes());
        var posY = networkEditorPanel.getVisualNetwork().getLastSelectedNode().getPosition().getY();
        
        var maxHeight = selectedNodes.stream()
                                     .mapToDouble(node -> node.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                                              .getBounds2D()
                                                              .getHeight())
                                     .max()
                                     .getAsDouble();
        
        selectedNodes.forEach(selectedNode -> {
            var nodeHeight = selectedNode.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                         .getBounds2D()
                                         .getHeight();
            
            selectedNode.setTemporalPosition(new Point2D.Double(selectedNode.getPosition()
                                                                            .getX(), posY + ((maxHeight - nodeHeight) / 2)));
        });
        try {
            new MoveNodeEdit(selectedNodes).executeEdit();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
    }
    
    public static boolean canHorizontalAlign(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = networkEditorPanel.getVisualNetwork().getSelectedNodes();
        var posStatistics = selectedNodes.stream()
                                         .mapToDouble(selectedNode -> selectedNode.getShape((Graphics2D) networkEditorPanel.getGraphics()).getBounds2D().getCenterY())
                                         .summaryStatistics();
        return posStatistics.getMin() != posStatistics.getMax();
    }
    
    public static void verticalSparse(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = networkEditorPanel.getVisualNetwork().getSelectedNodes();
        if (selectedNodes.size() < 2) {
            return;
        }
        sparseInAxis(networkEditorPanel, selectedNodes, 20.0,
                     VisualNode::getTemporalCoordinateY,
                     VisualNode::setTemporalCoordinateY,
                     visualNode -> visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                             .getBounds2D()
                                             .getHeight()
        );
    }
    
    public static boolean canVerticalSparse(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = networkEditorPanel.getVisualNetwork().getSelectedNodes();
        return sparsionOn(networkEditorPanel, selectedNodes, 20.0,
                          VisualNode::getTemporalCoordinateY,
                          visualNode -> visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                                  .getBounds2D()
                                                  .getHeight()
        ).sparsionProducesAChange;
    }
    
    public static void horizontalSparse(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = networkEditorPanel.getVisualNetwork().getSelectedNodes();
        if (selectedNodes.size() < 2) {
            return;
        }
        sparseInAxis(networkEditorPanel, selectedNodes, 50.0,
                     VisualNode::getTemporalCoordinateX,
                     VisualNode::setTemporalCoordinateX,
                     visualNode -> visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                             .getBounds2D()
                                             .getWidth()
        );
    }
    
    public static boolean canHorizontalSparse(NetworkEditorPanel networkEditorPanel) {
        var selectedNodes = networkEditorPanel.getVisualNetwork().getSelectedNodes();
        return sparsionOn(networkEditorPanel, selectedNodes, 50.0,
                            VisualNode::getTemporalCoordinateX,
                            visualNode -> visualNode.getShape((Graphics2D) networkEditorPanel.getGraphics())
                                                    .getBounds2D()
                                                    .getWidth()
        ).sparsionProducesAChange;
    }
    
    private static void sparseInAxis(NetworkEditorPanel networkEditorPanel, List<VisualNode> selectedNodes,
                                     double minSpaceBetweenNodes, ToDoubleFunction<? super VisualNode> getCoordinate,
                                     BiConsumer<VisualNode, Double> setCoordinate,
                                     ToDoubleFunction<? super VisualNode> getSize) {
        SparsionResult result = sparsionOn(networkEditorPanel, selectedNodes, minSpaceBetweenNodes, getCoordinate, getSize);
        for (int i = 0; i < result.nodesByPos().size(); i++) {
            VisualNode node = result.nodesByPos().get(i);
            Double newPos = result.nodesPos().get(i);
            setCoordinate.accept(node, newPos);
        }
        try {
            new MoveNodeEdit(selectedNodes).executeEdit();
        } catch (DoEditException e) {
            throw new UnreachableException(e);
        }
    }
    
    private static @NotNull SparsionResult sparsionOn(NetworkEditorPanel networkEditorPanel, List<VisualNode> selectedNodes, double minSpaceBetweenNodes, ToDoubleFunction<? super VisualNode> getCoordinate, ToDoubleFunction<? super VisualNode> getSize) {
        var originalRectStart = selectedNodes.stream()
                                             .mapToDouble(getCoordinate)
                                             .min()
                                             .getAsDouble();
        var originalRectEnd = selectedNodes.stream()
                                           .mapToDouble(visualNode -> getCoordinate.applyAsDouble(visualNode)
                                                   + getSize.applyAsDouble(visualNode))
                                           .max()
                                           .getAsDouble();
        var originalRectWidth = originalRectEnd - originalRectStart;
        var nodesByPos = selectedNodes.stream()
                                      .sorted(
                                              Comparator.comparingDouble((VisualNode visualNode) -> getCoordinate.applyAsDouble(visualNode))
                                                        .thenComparingDouble(VisualNode::getTemporalCoordinateX)
                                                        .thenComparingDouble(VisualNode::getTemporalCoordinateY)
                                      )
                                      .toList();
        var indexOfSelectedNode = nodesByPos.indexOf(networkEditorPanel.getVisualNetwork()
                                                                       .getLastSelectedNode());
        var originalCoordinateOfSelectedNode = getCoordinate.applyAsDouble(networkEditorPanel.getVisualNetwork()
                                                                                             .getLastSelectedNode());
        var newRectWidth = nodesByPos.stream()
                                     .mapToDouble(getSize::applyAsDouble)
                                     .sum() + ((nodesByPos.size() - 1) * minSpaceBetweenNodes);
        var newRectDiff = newRectWidth - originalRectWidth;
        double extraSpace = Math.max(0, -newRectDiff / (nodesByPos.size() - 1));
        List<Double> nodesPos = new ArrayList<>();
        nodesPos.add(getCoordinate.applyAsDouble(nodesByPos.getFirst()));
        for (int i = 0; i < nodesByPos.size() - 1; i++) {
            var prevNode = nodesByPos.get(i);
            nodesPos.add(
                    nodesPos.getLast()
                            + getSize.applyAsDouble(prevNode)
                            + minSpaceBetweenNodes
                            + extraSpace
            );
        }
        boolean sparsionProducesAChange = IntStream.range(0, nodesByPos.size())
                                                   .anyMatch(nodeIndex -> getCoordinate.applyAsDouble(nodesByPos.get(nodeIndex)) != nodesPos.get(nodeIndex));
        var diffOfPosOnSelectedNode = nodesPos.get(indexOfSelectedNode) - originalCoordinateOfSelectedNode;
        nodesPos.replaceAll(position -> position - diffOfPosOnSelectedNode);
        var minCoordinate = nodesPos.stream().mapToDouble(value -> value).min().getAsDouble();
        if (minCoordinate < 0) {
            nodesPos.replaceAll(position -> position + Math.abs(minCoordinate));
        }
        
        SparsionResult result = new SparsionResult(nodesByPos, nodesPos, sparsionProducesAChange);
        return result;
    }
    
    private record SparsionResult(List<VisualNode> nodesByPos, List<Double> nodesPos, boolean sparsionProducesAChange) {
    }
    

    
}
