package org.openmarkov.core.action.base;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;

import static org.junit.jupiter.api.Assertions.*;

class EditsHistoryTest {
    
    @Test
    public void test() {
        ProbNet probNet = new ProbNet();
        probNet.setName("Test probnet");
        
        var editA = new NamedEdit(probNet, "A");
        var editB = new NamedEdit(probNet, "B");
        var editC = new NamedEdit(probNet, "C");
        var editD = new NamedEdit(probNet, "D");
        var editE = new NamedEdit(probNet, "E");
        
        
        var manager = new EditsHistory();
        //Done edits: {}
        //Undone edits: {}
        assertFalse(manager.canRedo());
        assertFalse(manager.canUndo());
        
        //Done edits: {A}
        //Undone edits: {}
        manager.addEdit(editA);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        
        //Done edits: {}
        //Undone edits: {A}
        assertEquals(editA, manager.undo());
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        
        
        //Done edits: {A, B}
        //Undone edits: {}
        manager.removeUndoneEdits();
        manager.addEdit(editA);
        manager.addEdit(editB);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        
        
        //Done edits: {A}
        //Undone edits: {B}
        assertEquals(editB, manager.undo());
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        
        
        //Done edits: {}
        //Undone edits: {A, B}
        assertEquals(editA, manager.undo());
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        
        
        //Done edits: {A}
        //Undone edits: {B}
        assertEquals(editA, manager.redo());
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        
        
        //Done edits: {A, B}
        //Undone edits: {}
        assertEquals(editB, manager.redo());
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        
        
        //Done edits: {A, B, C, D}
        //Undone edits: {}
        manager.addEdit(editC);
        manager.addEdit(editD);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        
        
        //Done edits: {A, B, C}
        //Undone edits: {D}
        assertEquals(editD, manager.undo());
        
        //Done edits: {A, B}
        //Undone edits: {C, D}
        assertEquals(editC, manager.undo());
        
        //Done edits: {A, B, E}
        //Undone edits: {}
        manager.addEdit(editE);
        
        //Done edits: {A, B}
        //Undone edits: {E}
        assertEquals(editE, manager.undo());
        
        //Done edits: {A}
        //Undone edits: {B, E}
        assertEquals(editB, manager.undo());
        
        //Done edits: {}
        //Undone edits: {A, B, E}
        assertEquals(editA, manager.undo());
        
    }
    
    
    static class NamedEdit extends PNEdit {
        @Override protected void doEdit() {
        }
        
        final String name;
        
        public NamedEdit(ProbNet probNet, String name) {
            super(probNet);
            this.name = name;
        }
        
        @Override public String toString() {
            return "Edit named " + name;
        }
    }
}