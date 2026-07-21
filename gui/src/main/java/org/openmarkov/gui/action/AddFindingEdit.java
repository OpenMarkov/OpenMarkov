package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.gui.graphic.VisualNode;


/**
 * Edit that adds a finding (evidence) to a node, supporting undo and redo.
 * If a previous finding exists, undo restores it instead of simply removing.
 */
public class AddFindingEdit extends PNEdit {

    final VisualNode visualNode;
    final EvidenceCase evidenceCase;
    final Finding finding;
    final Finding previousFinding;

    /**
     * Creates a new edit that adds a finding to the specified node.
     *
     * @param visualNode      the visual node receiving the finding
     * @param evidenceCase    the evidence case to which the finding is added
     * @param previousFinding the previous finding for this variable, or {@code null} if none
     * @param finding         the new finding to add
     */
    public AddFindingEdit(VisualNode visualNode, EvidenceCase evidenceCase, Finding previousFinding, Finding finding) {
        super(visualNode.getNode().getProbNet());
        this.visualNode = visualNode;
        this.evidenceCase = evidenceCase;
        this.finding = finding;
        this.previousFinding = previousFinding;
    }
    
    @Override
    protected void doEdit() throws DoEditException.CannotDoEditException {
        try {
            evidenceCase.addFinding(finding);
            visualNode.setPreResolutionFinding(true);
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
            throw new DoEditException.CannotDoEditException(e, this);
        }
    }
    
    @Override
    public void undo() {
        super.undo();
        if (previousFinding == null) {
            evidenceCase.removeFinding(finding.getVariable());
            visualNode.setPreResolutionFinding(false);
        } else {
            try {
                evidenceCase.removeFinding(finding.getVariable());
                evidenceCase.addFinding(previousFinding);
            } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
                throw new UnreachableException(e);
            }
        }
        
    }
    
    
    @Override
    public void redo() {
        super.redo();
    }
    
    
}
