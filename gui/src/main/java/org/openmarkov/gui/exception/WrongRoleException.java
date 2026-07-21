package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.potential.PotentialRole;

//TODO: This should probably be a UnrecheableException instead of being wrapped on it when used.
public class WrongRoleException extends OpenMarkovException {
    public final PotentialRole expectedRole;
    public final PotentialRole foundRole;
    
    public WrongRoleException(PotentialRole expectedRole, PotentialRole foundRole) {
        this.expectedRole = expectedRole;
        this.foundRole = foundRole;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
