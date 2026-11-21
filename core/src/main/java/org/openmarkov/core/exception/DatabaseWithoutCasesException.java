package org.openmarkov.core.exception;

/**
 * A dataset that has the names of its variables and no cases.
 *
 * @author Manuel Arias
 */
public class DatabaseWithoutCasesException extends EmptyDatabaseException {

    public DatabaseWithoutCasesException(String source) {
        super(source);
    }
}
