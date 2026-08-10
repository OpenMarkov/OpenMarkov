package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.OpenMarkovException;

public class AlreadyExistingAgentException extends OpenMarkovException {
    
    public final String agentName;
    
    public AlreadyExistingAgentException(String agentName) {
        this.agentName = agentName;
    }
    
}
