package org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.java.exceptionUtils.ThrowableUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class testUnreachableExceptionStacktrace {
    
    /**
     * Tests {@link UnreachableException} always contains just the target exception, meaning an
     * {@link UnreachableException} will never contain another {@link UnreachableException}.
     * <p>
     * It also tests the operation {@link ThrowableUtils#flatten(Throwable)}.
     */
    @Test
    public void testFlattening() {
        UnreachableException exception;
        try {
            callerA();
            return;
        } catch (UnreachableException e) {
            exception = e;
        }
        Throwable flat = ThrowableUtils.flatten(exception);
        assertEquals(flat.getClass(), EmptyDatabaseException.class);
        var stackTraceInOrder = List.of(
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.thrower",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.callerD",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.callerC",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.callerC",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.callerB",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.callerA",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.testUnreachableExceptionStacktrace.testFlattening"
        );
        for (int i = 0; i < stackTraceInOrder.size(); i++) {
            var expectedMethodInStackTrace = stackTraceInOrder.get(i);
            var stackTraceElement = flat.getStackTrace()[i];
            assertEquals(expectedMethodInStackTrace, stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
        }
    }
    
    static void callerA() {
        callerB();
    }
    
    static void callerB() {
        callerC();
    }
    
    static void callerC() {
        try {
            callerD();
        } catch (EmptyDatabaseException e) {
            throw new UnreachableException(e);
        }
        
    }
    
    static void callerD() throws EmptyDatabaseException {
        thrower();
    }
    
    static void thrower() throws EmptyDatabaseException {
        throw new EmptyDatabaseException("DB.file");
    }
    
}
