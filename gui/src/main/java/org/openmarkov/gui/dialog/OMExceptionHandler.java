package org.openmarkov.gui.dialog;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
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
        ExceptionType exceptionType = null;
        // We get the root cause of the exception, as UnrecoverableException and UnreachableException aren't exceptions
        // themselves, but holders of other exceptions.
        while (throwable instanceof UnrecoverableException || throwable instanceof UnreachableException) {
            if (throwable instanceof UnreachableException) {
                exceptionType = ExceptionType.UNREACHABLE;
            } else if (throwable instanceof UnrecoverableException && exceptionType == null) {
                exceptionType = ExceptionType.EXPECTED;
            }
            ThrowableUtils.transferStackTrace(throwable, throwable.getCause());
            throwable = throwable.getCause();
        }
        if (throwable instanceof RuntimeException) {
            exceptionType = ExceptionType.RUNTIME;
        } else if (exceptionType == null) {
            exceptionType = ExceptionType.EXPECTED;
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
        throwable.printStackTrace();
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
    
    enum ExceptionType {
        UNREACHABLE,
        EXPECTED,
        RUNTIME,
    }
}
