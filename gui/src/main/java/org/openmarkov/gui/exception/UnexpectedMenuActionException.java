package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

//TODO: This should probably be a UnrecheableException instead of being wrapped on it when used.
public class UnexpectedMenuActionException extends RuntimeException implements IBundledOpenMarkovException {
    public UnexpectedMenuActionException(String actionCommand) {
        this.actionCommand = actionCommand;
    }
    
    public final String actionCommand;
    
    @Override public String toString() {
        return this.localize();
    }
    
}
