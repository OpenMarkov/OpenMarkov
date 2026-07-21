package org.openmarkov.core.action.base;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompoundEditIsRedoneOnFail {
    
    @Test
    void test() {
        ProbNet probNet = new ProbNet();
        probNet.setName("Test probnet");
        
        List<FailableEdit> failableEdits = List.of(
                new FailableEdit(probNet, "A", false),
                new FailableEdit(probNet, "B", false),
                new FailableEdit(probNet, "C", true),
                new FailableEdit(probNet, "D", false),
                new FailableEdit(probNet, "E", false)
        );
        CompoundFailableEdit compoundEdit = new CompoundFailableEdit(probNet, failableEdits);
        for (FailableEdit edit : failableEdits) {
            assertTrue(!probNet.containsVariable(edit.name));
        }
        try {
            compoundEdit.executeEdit();
            fail("An exception should have happened");
        } catch (DoEditException e) {
            for (FailableEdit edit : failableEdits) {
                assertTrue(!probNet.containsVariable(edit.name));
            }
        }
    }
    
    
    static class CompoundFailableEdit extends CompoundPNEdit {
        
        private final List<FailableEdit> edits;
        
        private CompoundFailableEdit(ProbNet probNet, List<FailableEdit> edits) {
            super(probNet);
            this.edits = edits;
        }
        
        @Override protected ArrayList<PNEdit> generateEdits() {
            return new ArrayList<>(this.edits);
        }
    }
    
    static class FailableEdit extends PNEdit {
        @Override protected void doEdit() throws DoEditException {
            if (fails) {
                throw new DoEditException.NodeIsNull(null, this);
            }
            this.probNet.addNode(new Variable(this.name), NodeType.CHANCE);
        }
        
        @Override public void undo() {
            this.probNet.removeNode(this.probNet.getNode(this.name));
        }
        
        final String name;
        final boolean fails;
        
        public FailableEdit(ProbNet probNet, String name, boolean fails) {
            super(probNet);
            this.name = name;
            this.fails = fails;
        }
        
        @Override public String toString() {
            return "Edit named " + name + ", which " + (fails ? "fails" : "does not fail");
        }
    }
}
