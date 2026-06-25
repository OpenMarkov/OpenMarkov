package org.openmarkov.gui.exception;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;

public class OtherPropertyAlreadyExistsException extends DoEditException {
    
    public final String propertyName;
    
    public OtherPropertyAlreadyExistsException(String propertyName, PNEdit failedEdit) {
        super(failedEdit);
        this.propertyName = propertyName;
    }
}
