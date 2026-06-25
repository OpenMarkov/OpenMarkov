/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.DistinctLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DistinctLinksRelatedEditsTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    @Disabled
    @Test
    public void testUndoableEditWillHappen() throws DoEditException {
        PNESupport pNESupport = influenceDiagram.getPNESupport();
        PNConstraint constraint = new DistinctLinks();
        
        influenceDiagram.addConstraint(constraint);
        pNESupport.addListener(constraint);
        
        // do legal AddLink: add an directed link between U and A
        Variable vU = influenceDiagram.getVariable("U");
        Variable vA = influenceDiagram.getVariable("A");
        // creates an undirected link from node A to D
        AddLinkEdit legalEdit = new AddLinkEdit(influenceDiagram, vU, vA, true);
        legalEdit.executeEdit();
        
        // do ilegal LinkAdd. Add an directed link between A and U
        AddLinkEdit ilegalAdd = new AddLinkEdit(influenceDiagram, vA, vU, true);
        try {
            ilegalAdd.executeEdit();
            fail();
        } catch (DoEditException e) {
            // The constraint should have faild
        }
        
        Variable vD = influenceDiagram.getVariable("D");
        // do ilegal InvertLink. Add an directed link between D and U
        InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vD, true);
        try {
            ilegalAdd.executeEdit();
            fail();
        } catch (DoEditException e) {
            // The constraint should have failed
        }
        
        // do legal invert link: create undirected link between U and D
        InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vD, false);
        legalInvertLinkEdit.executeEdit();
        
        
        // do ilegal LinkEdit. Add an undirected link between U and D
        AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("U"),
                                                     influenceDiagram.getVariable("D"), false);
        try {
            ilegalLinkEdit.executeEdit();
            fail();
        } catch (DoEditException e) {
            // The constraint should have failed
        }
        
    }
    
}
