package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;

public class LinkInversionRequiresChanceVariablesWithPotential extends OpenMarkovException {
    
    public final Node wrongNode;
    
    public LinkInversionRequiresChanceVariablesWithPotential(Node wrongNode) {
        super();
        this.wrongNode = wrongNode;
    }
}
