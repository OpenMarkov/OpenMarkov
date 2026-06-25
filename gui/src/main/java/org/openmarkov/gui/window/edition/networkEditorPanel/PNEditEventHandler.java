package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listens for {@link PNEdit} undo/redo events and updates the visual network
 * accordingly (e.g. refreshing pre-resolution finding markers and repainting).
 */
class PNEditEventHandler implements PNEditListener {
    private final NetworkEditorPanel networkEditorPanel;
    
    PNEditEventHandler(NetworkEditorPanel networkEditorPanel) {
        this.networkEditorPanel = networkEditorPanel;
    }
    
    @Override
    public void afterUndoingEdit(PNEdit edit) {
        List<Finding> findings = this.networkEditorPanel.getEvidenceManager().getPreResolutionEvidence().getFindings();
        Set<Variable> findingVariables = findings.stream()
                                                 .map(Finding::getVariable)
                                                 .collect(Collectors.toSet());
        List<VisualNode> allVisualNodes = this.networkEditorPanel.getVisualNetwork().getAllNodes();
        for (VisualNode visualNode : allVisualNodes) {
            Variable nodeVariable = visualNode.getNode().getVariable();
            boolean isPreResolution = findingVariables.contains(nodeVariable);
            visualNode.setPreResolutionFinding(isPreResolution);
        }
        this.networkEditorPanel.readjustAndRepaint();
    }
    
    @Override
    public void afterEditExecutes(PNEdit edit) {
        for (Finding finding : this.networkEditorPanel.getEvidenceManager().getPreResolutionEvidence().getFindings()) {
            Variable variable = finding.getVariable();
            for (VisualNode visualNode : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
                if (variable.getName().equals(visualNode.getNode().getName())) {
                    visualNode.setPreResolutionFinding(true);
                }
            }
        }
        if (edit instanceof PasteEdit pasteEdit) {
            this.networkEditorPanel.getVisualNetwork().setSelectedAllObjects(false);
            SelectedContent pastedContent = pasteEdit.getPastedContent();
            for (Node node : pastedContent.nodes()) {
                this.networkEditorPanel.getVisualNetwork().setSelectedNode(node.getName(), true);
            }
            for (Link<Node> link : pastedContent.links()) {
                this.networkEditorPanel.getVisualNetwork().setSelectedLink(link, true);
            }
        }
        this.networkEditorPanel.readjustAndRepaint();
    }
    
    @Override public void onEditFailed(PNEdit edit, DoEditException exception) {
        this.networkEditorPanel.readjustAndRepaint();
    }
    
    @Override
    public void onEditViolatesConstraints(PNEdit edit, ConstraintViolatedException ex) {
        this.networkEditorPanel.readjustAndRepaint();
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        this.networkEditorPanel.readjustAndRepaint();
    }
}
