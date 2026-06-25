package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

public class NoRowSelectedException extends OpenMarkovException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
