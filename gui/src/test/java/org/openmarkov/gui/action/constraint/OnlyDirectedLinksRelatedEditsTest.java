/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyDirectedLinksRelatedEditsTest {
    
    private ProbNet probNetMixed;
    
    private ProbNet probNetUndirected;
    
    private ProbNet probNetDirected;
    
    @BeforeEach public void setUp() {
        probNetMixed = ConstraintsTests.getTestProbNetMixed();
        probNetUndirected = ConstraintsTests.getTestProbNetUndirected();
        probNetDirected = ConstraintsTests.getTestProbNetDirected();
    }
    
    /**
     * Checks veto
     */
    @Test public void testUndoableEditWillHappen() throws DoEditException {
        
        // Add constraints as listeners.
        PNESupport pNESupport = probNetDirected.getPNESupport();
        probNetDirected.addConstraint(new OnlyDirectedLinks());
        List<PNConstraint> constraints = probNetDirected.getConstraints();
        for (PNConstraint constraint : constraints) { // sets listeners
            pNESupport.addListener(constraint);
        }
        // Create edits
        Variable va = probNetDirected.getVariable("A");
        Variable vc = probNetDirected.getVariable("C");
        
        // test no exception in legal edit
        AddLinkEdit cEdit = new AddLinkEdit(probNetDirected, va, vc, true);
        cEdit.executeEdit();
        
        // test exception in no legal edit
        AddLinkEdit iEdit;
        try {
            iEdit = new AddLinkEdit(probNetDirected, va, vc, false);
            iEdit.executeEdit();
            fail();
        } catch (DoEditException cve) {
            // It should have thrown an exception.
        }
    }
    
}
