package org.openmarkov.core.exception;

/**
 * Base class for exceptions caused by invalid or unsupported user input.
 * <p>
 * Subtypes represent conditions where the user provided something the system
 * cannot handle (malformed file, conflicting evidence, unsupported format,
 * invalid expression syntax, empty dataset, etc.). The GUI can catch this
 * single type to display a user-friendly message for any such error without
 * knowing the specific subtype.
 * <p>
 * Direct subtypes: {@link IncompatibleEvidenceException}, {@link ProbNetParserException},
 * {@link WriterException}, {@link EmptyDatabaseException},
 * {@link ParsingSourceException}.
 * Additional subtypes live in {@code org.openmarkov.core.io.exception} and
 * {@code org.openmarkov.core.io.format.annotation}.
 */
public abstract class UserInputException extends OpenMarkovException {
    
    protected UserInputException() {}
    
    
}
