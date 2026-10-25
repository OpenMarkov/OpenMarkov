/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.OMExceptionHandler.ExceptionType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A dialog rejects a mistyped value with an {@link InvalidArgumentException} wrapped in an
 * {@link UnrecoverableException}: the global handler shows it as expected, with its own title and message, while any
 * other runtime exception is still unexpected.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AMistypedValueIsShownAsExpectedTest {

    @Test void anInvalidArgumentWrappedAsExpectedIsExpected() {
        var alpha = new InvalidArgumentException(2.0, "alpha", "must be between 0 and 1");
        assertEquals(ExceptionType.EXPECTED, OMExceptionHandler.typeOf(new UnrecoverableException(alpha)));
    }

    @Test void otherRuntimeExceptionsAreUnexpected() {
        assertEquals(ExceptionType.RUNTIME,
                     OMExceptionHandler.typeOf(new UnrecoverableException(new NullPointerException())));
        assertEquals(ExceptionType.RUNTIME,
                     OMExceptionHandler.typeOf(new InvalidArgumentException(2.0, "alpha", "must be between 0 and 1")));
        assertEquals(ExceptionType.RUNTIME, OMExceptionHandler.typeOf(new UnreachableException(
                new InvalidArgumentException(2.0, "alpha", "must be between 0 and 1"))));
    }

    @Test void aCheckedExceptionIsExpected() {
        assertEquals(ExceptionType.EXPECTED, OMExceptionHandler.typeOf(new Exception("checked")));
    }
}
