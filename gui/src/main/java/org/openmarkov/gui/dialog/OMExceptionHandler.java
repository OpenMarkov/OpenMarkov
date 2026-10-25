package org.openmarkov.gui.dialog;

import org.openmarkov.core.exception.IOpenMarkovException;
import org.openmarkov.core.exception.InferenceStoppedException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.java.exceptionUtils.ThrowableUtils;

import java.util.ArrayList;

/**
 * Global uncaught-exception handler for OpenMarkov. Classifies throwables into
 * {@link ExceptionType#UNREACHABLE}, {@link ExceptionType#EXPECTED}, or
 * {@link ExceptionType#RUNTIME} and routes them to the appropriate dialog
 * ({@link UnexpectedThrowableDialog} or {@link ExceptionDialog}).
 */
public class OMExceptionHandler implements Thread.UncaughtExceptionHandler {
    
    @Override public void uncaughtException(Thread thread, Throwable throwable) {
        ExceptionType exceptionType = typeOf(throwable);
        // We get the root cause of the exception, as UnrecoverableException and UnreachableException aren't exceptions
        // themselves, but holders of other exceptions.
        while (isHolder(throwable)) {
            ThrowableUtils.transferStackTrace(throwable, throwable.getCause());
            throwable = throwable.getCause();
        }
        if (throwable instanceof InferenceStoppedException) {
            return; // the user asked for it: there is nothing to tell
        }
        // We flatten the exceptions stacktrace, avoiding seeing a long stacktrace of exceptions blaming other
        // exceptions such as 'Exception X caused by Exception Y caused by Exception Z...'.
        if (throwable.getCause() != null) {
            var allThrowables = new ArrayList<Throwable>();
            allThrowables.add(throwable);
            while (allThrowables.getLast().getCause() != null) {
                allThrowables.add(allThrowables.getLast().getCause());
            }
            while (allThrowables.size() > 2) {
                var last = allThrowables.removeLast();
                ThrowableUtils.transferStackTrace(last, allThrowables.getLast());
            }
        }
        OpenMarkovLogger.LOGGER.error("", throwable);
        // Exceptions are shown differently depending on their type.
        switch (exceptionType) {
            // If an exception is unexpected (When an exception is runtime or came from an UnreachableException),
            // they are shown in a special dialog telling the user something wrong happened and that an OpenMarkov's
            // developer didn't predict.
            case UNREACHABLE, RUNTIME -> new UnexpectedThrowableDialog(throwable, exceptionType).setVisible(true);
            // When the exception is expected (This is when it is thrown directly, or it has been wrapped in an
            // UnrecoverableException), then it just shows a dialog showing a message describing the exception.
            case EXPECTED -> ExceptionDialog.show(throwable);
        }
    }
    
    /**
     * How {@code throwable} is shown. A runtime exception is unexpected, unless it is an OpenMarkov exception wrapped
     * in an {@link UnrecoverableException}, which marks it as expected.
     */
    static ExceptionType typeOf(Throwable throwable) {
        ExceptionType marked = null;
        while (isHolder(throwable)) {
            if (throwable instanceof UnreachableException) {
                marked = ExceptionType.UNREACHABLE;
            } else if (marked == null) {
                marked = ExceptionType.EXPECTED;
            }
            throwable = throwable.getCause();
        }
        if (throwable instanceof RuntimeException
                && !(marked == ExceptionType.EXPECTED && throwable instanceof IOpenMarkovException)) {
            return ExceptionType.RUNTIME;
        }
        return marked == null ? ExceptionType.EXPECTED : marked;
    }

    private static boolean isHolder(Throwable throwable) {
        return throwable instanceof UnrecoverableException || throwable instanceof UnreachableException;
    }

    enum ExceptionType {
        UNREACHABLE,
        EXPECTED,
        RUNTIME,
    }
}
