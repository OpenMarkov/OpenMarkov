/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Safety net for the exception framework, written while applying its report.
 *
 * @author Manuel Arias
 */
class ExceptionFrameworkTest {

    /**
     * The fixture the framework's own javadoc recommends: title and message in
     * a bundle, fields filled in by reflection.
     */
    private static final class WalkedFrameworkException extends Exception implements IOpenMarkovException {

        private final String program;
        private final String netName;

        private WalkedFrameworkException(String program, String netName) {
            this.program = program;
            this.netName = netName;
        }

        @Override public String getExceptionTitle() {
            return IOpenMarkovException.autoGetExceptionTitle(this);
        }

        @Override public String getExceptionMessage() {
            return IOpenMarkovException.autoGetExceptionMessage(this);
        }
    }

    /**
     * The bundled-message pattern the framework recommends works end to end:
     * the template comes from the bundle and the fields fill it in. The walk
     * that collects those fields used to compare against an interface as its
     * stop condition, which a superclass chain never reaches, so the first
     * real exception to adopt the recommended pattern died with a
     * NullPointerException past Object.
     */
    @Test
    void aBundledExceptionMessageIsFormattedFromItsFields() {
        WalkedFrameworkException exception = new WalkedFrameworkException("OpenMarkov", "Mynet");

        assertEquals("Network Mynet of OpenMarkov", exception.getExceptionMessage());
        assertEquals("Walked title", exception.getExceptionTitle());
    }

    /**
     * Wrapping an UnrecoverableException in another must flatten to the real
     * failure. The unwrap loop used to test for UnreachableException - line
     * for line its sibling's loop, a copy-paste - so the effective cause was
     * the inner wrapper instead of the failure it wrapped.
     */
    @Test
    void nestedUnrecoverableExceptionsFlattenToTheRealFailure() {
        IllegalStateException realFailure = new IllegalStateException("the real failure");

        UnrecoverableException outer = new UnrecoverableException(new UnrecoverableException(realFailure));

        assertSame(realFailure, outer.getCause());
    }
}
