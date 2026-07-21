package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException.exceptions;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IOpenMarkovException;

@SuppressWarnings("ALL")
public class UnbundledTrialException2 extends Exception implements IOpenMarkovException {
    public final String program;
    public final String concept;
    
    public UnbundledTrialException2(String program, String concept) {
        this.program = program;
        this.concept = concept;
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
