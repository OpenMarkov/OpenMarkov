package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.java.exceptionUtils.ThrowableUtils;

import java.net.URL;

public class CorruptNetworkFile extends OpenMarkovException {
    
    public CorruptNetworkFile(URL fileToRead, Exception ex) {
        ThrowableUtils.transferStackTrace(ex, this);
        this.fileToRead = fileToRead;
    }
    
    private final URL fileToRead;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
