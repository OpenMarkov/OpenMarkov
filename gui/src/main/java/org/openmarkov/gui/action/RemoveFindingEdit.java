package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.*;
import org.openmarkov.gui.graphic.VisualNode;

/**
 * Edit that removes a finding (evidence) from a node, supporting undo and redo.
 */
public class RemoveFindingEdit extends PNEdit {

    private final EvidenceCase evidenceCase;
    private final Variable variable;
    private Finding finding;
    private final VisualNode visualNode;

    /**
     * Creates a new edit that removes the finding for the specified variable.
     *
     * @param visualNode   the visual node from which the finding is removed
     * @param evidenceCase the evidence case containing the finding
     * @param variable     the variable whose finding will be removed
     */
    public RemoveFindingEdit(VisualNode visualNode, EvidenceCase evidenceCase, Variable variable) {
        super(visualNode.getNode().getProbNet());
        this.visualNode = visualNode;
        this.evidenceCase = evidenceCase;
        this.variable = variable;
    }
    
    @Override protected void doEdit() {
        finding = evidenceCase.getFinding(variable);
        evidenceCase.removeFinding(variable);
        visualNode.setPreResolutionFinding(false);
    }
    
    @Override public void undo() {
        super.undo();
        try {
            evidenceCase.addFinding(finding);
            visualNode.setPreResolutionFinding(true);
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
            throw new UnreachableException(e);
        }

    }
    
    @Override
    public void redo() {
        super.redo();
        evidenceCase.removeFinding(variable);
        visualNode.setPreResolutionFinding(false);
    }
    
}
