package org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.java.exceptionUtils.ThrowableUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnreachableExceptionStacktraceTest {
    
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
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.thrower",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.callerD",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.callerC",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.callerC",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.callerB",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.callerA",
                "org.openmarkov.integrationTests.integrationTests.testRuntimeExceptions.UnreachableExceptionStacktraceTest.testFlattening"
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
