package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.OpenMarkovException;

//TODO: This should probably be a UnrecheableException instead of being wrapped on it when used.
public class UnexpectedMenuActionException extends OpenMarkovException {
    public UnexpectedMenuActionException(String actionCommand) {
        this.actionCommand = actionCommand;
    }
    
    public final String actionCommand;
    
    @Override public String toString() {
        return this.localize();
    }
    
}
