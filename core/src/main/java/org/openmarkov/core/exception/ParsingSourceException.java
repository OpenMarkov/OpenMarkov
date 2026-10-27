package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

//TODO: Catches of this class just show the exception and ignore it, leading to further bugs.
public abstract sealed class ParsingSourceException extends UserInputException {
    
    public static final class CouldNotParseSourceException extends ParsingSourceException {
        public final Exception originException;

        public CouldNotParseSourceException(Exception originException) {
            this.originException = originException;
        }
    }

    /** Two columns of a case database carry the same variable name. */
    public static final class RepeatedVariableNames extends ParsingSourceException {
        public final String source;
        public final String variableName;

        public RepeatedVariableNames(String source, String variableName) {
            this.source = source;
            this.variableName = variableName;
        }
    }

    /**
     * A file of text starts with the byte order mark of UTF-8, three bytes that some programs put before the text. The
     * formats of case databases do not allow it, and Weka refuses it too (error 250 of the pending list).
     */
    public static final class StartsWithAByteOrderMark extends ParsingSourceException {
        public final String source;

        public StartsWithAByteOrderMark(String source) {
            this.source = source;
        }
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    
}
