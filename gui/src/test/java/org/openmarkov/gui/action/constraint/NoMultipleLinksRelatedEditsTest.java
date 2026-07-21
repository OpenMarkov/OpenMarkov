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
import org.openmarkov.core.model.network.constraint.NoMultipleLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;

import static org.junit.jupiter.api.Assertions.fail;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMultipleLinksRelatedEditsTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    @Disabled
    @Test
    public void testUndoableEditWillHappen() throws DoEditException {
        PNESupport pNESupport = influenceDiagram.getPNESupport();
        PNConstraint constraint = new NoMultipleLinks();
        
        influenceDiagram.addConstraint(constraint);
        pNESupport.addListener(constraint);
        
        // do legal AddLink: add an directed link between U and A
        Variable vU = influenceDiagram.getVariable("U");
        Variable vA = influenceDiagram.getVariable("A");
        // creates an undirected link from node A to D
        AddLinkEdit legalEdit = new AddLinkEdit(influenceDiagram, vU, vA, true);
        legalEdit.executeEdit();
        
        // do ilegal LinkAdd. Add an undirected link between U and A
        AddLinkEdit ilegalAdd = new AddLinkEdit(influenceDiagram, vU, vA, false);
        try {
            ilegalAdd.executeEdit();
            fail();
        } catch (DoEditException e) {
            // the ilegal edit should have thrown the exception
        }
        
        // do ilegal LinkEdit. Add an undirected link between U and A
        AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("U"),
                                                     influenceDiagram.getVariable("A"), false);
        ilegalLinkEdit.executeEdit();
        
        // do legal invert link
        InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vA, true);
        legalInvertLinkEdit.executeEdit();
        
        // do ilegal InvertLink. Add an directed link between U and A
        InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vA, false);
        try {
            ilegalInvertLinkEdit.executeEdit();
            fail();
        } catch (DoEditException e) {
            // the ilegal edit should have thrown the exception
        }
    }
    
}
