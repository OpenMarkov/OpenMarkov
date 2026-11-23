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
     * A line of a file of comma-separated values has more or fewer values than the first one names columns: the
     * missing values used to be taken as the first state seen in their column, and one value too many stopped the
     * reading without saying where (error 124 of the pending list).
     */
    public static final class WrongNumberOfValues extends ParsingSourceException {
        public final String source;
        public final int line;
        public final int values;
        public final int columns;

        public WrongNumberOfValues(String source, int line, int values, int columns) {
            this.source = source;
            this.line = line;
            this.values = values;
            this.columns = columns;
        }
    }

    /** A file with the extension of a spreadsheet that is not one of that format. */
    public static final class NotASheet extends ParsingSourceException {
        public final String source;

        public NotASheet(String source) {
            this.source = source;
        }
    }

    /** A column of a spreadsheet that has values and no name in the first row. */
    public static final class ColumnWithoutName extends ParsingSourceException {
        public final String source;
        public final String cell;

        public ColumnWithoutName(String source, String cell) {
            this.source = source;
            this.cell = cell;
        }
    }

    /** The first line of a file of separated values uses both the comma and the semicolon to separate its names. */
    public static final class TwoSeparators extends ParsingSourceException {
        public final String source;

        public TwoSeparators(String source) {
            this.source = source;
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
