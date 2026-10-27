/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tables round their values before showing them, to 10 decimals and fewer. Above some 922 million, the value
 * times 10^10 did not fit in a long, and Math.round gave the largest one: the table of a utility showed
 * 922337203.685478 for any larger value, such as a cost in euros of a programme (error 156 of the pending list).
 *
 * @author Manuel Arias
 */
class LargeValuesAreRoundedAsTheyAreTest {

    @Test void largeValuesKeepTheirValue() {
        assertEquals(1e9, Util.roundAndReduce(1e9, 1e-12, 10));
        assertEquals(1_234_567_890, Util.roundAndReduce(1_234_567_890, 1e-12, 10));
        assertEquals(1e15, Util.roundAndReduce(1e15, 1e-12, 10));
        assertEquals(-1e15, Util.roundAndReduce(-1e15, 1e-12, 10));
        assertEquals(2_000_000_000.25, Util.roundAndReduce(2_000_000_000.25, 1e-12, 10));
    }

    @Test void smallValuesAreRoundedAsBefore() {
        assertEquals(0.3, Util.roundAndReduce(0.30000000000000004, 1e-12, 10));
        assertEquals(0.12345, Util.roundWithPrecision(0.123454, 5));
        assertEquals(922_337_203.0, Util.roundWithPrecision(922_337_203.4, 0));
    }
}
