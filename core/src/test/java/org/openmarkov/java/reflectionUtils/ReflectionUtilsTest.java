/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.reflectionUtils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ReflectionUtils#forceGetField}: what it promises to throw is what must
 * come out, and it must say what it could not find.
 *
 * @author Manuel Arias
 */
public class ReflectionUtilsTest {

    private static class Parent {
        @SuppressWarnings("unused") private final String hidden = "from the parent";
    }

    private static class Child extends Parent {
        @SuppressWarnings("unused") private final int answer = 42;
        @SuppressWarnings("unused") private static final String SHARED = "shared value";
    }

    @Test
    public void readingAPrivateFieldOfAnInstance() throws ReflectiveOperationException {
        assertEquals(42, ReflectionUtils.forceGetField(new Child(), "answer", Integer.class));
    }

    @Test
    public void readingAnInheritedFieldWalksUpTheAncestors() throws ReflectiveOperationException {
        assertEquals("from the parent", ReflectionUtils.forceGetField(new Child(), "hidden", String.class));
    }

    /** A shared (static) field is read giving the class itself as the source. */
    @Test
    public void readingASharedFieldGivenTheClass() throws ReflectiveOperationException {
        assertEquals("shared value", ReflectionUtils.forceGetField(Child.class, "SHARED", String.class));
    }

    /**
     * The method declares the reflective family of errors, whose member for this case is
     * {@code NoSuchFieldException}; that is what must escape ---so that a caller who trusts the
     * declaration catches it--- and it must name the field and the class it was looked for in.
     */
    @Test
    public void aMissingFieldIsReportedByNameAndClass() {
        ReflectiveOperationException error = assertThrows(ReflectiveOperationException.class,
                () -> ReflectionUtils.forceGetField(new Child(), "nowhere", Object.class));

        assertInstanceOf(NoSuchFieldException.class, error);
        assertTrue(error.getMessage().contains("nowhere"),
                   () -> "the error must name the field; it says: " + error.getMessage());
        assertTrue(error.getMessage().contains(Child.class.getName()),
                   () -> "the error must name the class; it says: " + error.getMessage());
    }
}
