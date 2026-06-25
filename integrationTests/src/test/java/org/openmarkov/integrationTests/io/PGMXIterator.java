package org.openmarkov.integrationTests.io;

import org.openmarkov.core.exception.ProbNetParserException;

import java.io.IOException;

public interface PGMXIterator {
    
    PGMXCompound next() throws IOException, ProbNetParserException;
    boolean hasNext();

}
