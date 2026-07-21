package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException.exceptions;

public class SubTrialException2 extends TrialException2 {
    private final int errorCode;
    
    public SubTrialException2(String program, String netName, int errorCode) {
        super(program, netName);
        this.errorCode = errorCode;
    }
}
