package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException.exceptions;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IOpenMarkovException;


@SuppressWarnings("ALL")
public class TrialException2 extends Exception implements IOpenMarkovException {
    
    private final String program;
    private final String netName;
    
    public TrialException2(String program, String netName) {
        this.program = program;
        this.netName = netName;
    }
    
    @Override @Nullable public String getExceptionTitle() {
        return IOpenMarkovException.autoGetExceptionTitle(this);
    }
    
    @Override @Nullable public String getExceptionMessage() {
        return IOpenMarkovException.autoGetExceptionMessage(this);
    }
    
    @Override public String toString() {
        return IOpenMarkovException.toString(this);
    }
}
