/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.testTags.TestSpeed;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A discount written in percent in the dialog of inference options is the number that writing it in decimal gives,
 * and the dialog shows it again as it was written.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ADiscountWrittenInPercentIsTheDecimalNumberTest {

    @ParameterizedTest
    @CsvSource({ "1.4, 0.014", "0.35, 0.0035", "4.1, 0.041", "3.5555, 0.035555" })
    void theDiscountIsTheDecimalNumber(String percent, String decimal) {
        assertEquals(Double.parseDouble(decimal), InferenceOptionsDialog.fractionOf(percent));
    }

    /** Every discount from 0.1 % to 10 %, with one decimal, is kept and shown as written. */
    @Test void everyDiscountWithOneDecimalIsKeptAndShownAsWritten() {
        for (int tenths = 1; tenths <= 100; tenths++) {
            String percent = BigDecimal.valueOf(tenths, 1).stripTrailingZeros().toPlainString();
            double discount = Double.parseDouble(BigDecimal.valueOf(tenths, 3).toPlainString());

            assertEquals(discount, InferenceOptionsDialog.fractionOf(percent), percent);
            assertEquals(percent + " %", InferenceOptionsDialog.percentOf(discount));
        }
    }

    @Test void theUsualDiscountsHaveNoZerosAtTheEnd() {
        assertEquals("3 %", InferenceOptionsDialog.percentOf(0.03));
        assertEquals("3.5 %", InferenceOptionsDialog.percentOf(0.035));
        assertEquals("0 %", InferenceOptionsDialog.percentOf(0));
    }
}
