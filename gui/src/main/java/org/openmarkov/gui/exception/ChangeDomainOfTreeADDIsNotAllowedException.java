package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.OpenMarkovException;

public class ChangeDomainOfTreeADDIsNotAllowedException extends OpenMarkovException {
    
    @Override public String toString() {
        return this.localize();
    }
    
}
