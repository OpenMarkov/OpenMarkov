package org.openmarkov.gui.exception;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;

/**
 * A property that the user gives to a node has a name under which the files keep data of the program itself.
 *
 * @author Manuel Arias
 */
public class OtherPropertyNameIsUsedByTheProgramException extends DoEditException {

    public final String propertyName;

    public OtherPropertyNameIsUsedByTheProgramException(String propertyName, PNEdit failedEdit) {
        super(failedEdit);
        this.propertyName = propertyName;
    }
}
