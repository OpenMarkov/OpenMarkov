/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The warning of a zoom out of range said «Requested zoomManager», the name of a field of the code (error 183 of the
 * pending list).
 *
 * @author Manuel Arias
 */
class TheZoomOutOfRangeIsNamedZoomTest {

    @Test void theWarningSaysZoom() {
        assertEquals("Requested zoom (500) is out of the range [10..300].",
                     new ZoomOutOfRangeException("500", 10, 300).toString());
    }
}
