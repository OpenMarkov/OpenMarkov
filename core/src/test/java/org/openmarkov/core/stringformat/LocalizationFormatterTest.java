package org.openmarkov.core.stringformat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestConfig;

import java.util.Locale;

/**
 * @author Manuel Arias
 */
class LocalizationFormatterTest {

    @Test final void anAbsentStyleGivesTheDefaultFormatter() {
        Assertions.assertSame(LocalizationFormatter.DEFAULT, LocalizationFormatter.of(null));
        Assertions.assertSame(LocalizationFormatter.DEFAULT, LocalizationFormatter.of("  "));
    }

    @Test final void theStyleIsReadWhateverItsCase() {
        Assertions.assertEquals(LocalizationFormatter.LocalizationFormatterLength.SHORT,
                                LocalizationFormatter.of("SHORT").desiredLength);
        Assertions.assertEquals(LocalizationFormatter.ListFormat.DETAIL,
                                LocalizationFormatter.of("Detail").listSeparator);
    }

    /**
     * S4: the style comes from a resource bundle and is matched against the names of the enums, so
     * the conversion to lower case must not depend on where the program runs. In Turkish, an upper
     * case I becomes a dotless ı, so "DETAIL" stopped matching the style "detail".
     */
    @Tag(TestConfig.DisabledInParallel)
    @Test final void theStyleIsReadTheSameInATurkishLocale() {
        Locale previousDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.of("tr"));

            Assertions.assertEquals(LocalizationFormatter.ListFormat.DETAIL,
                                    LocalizationFormatter.of("detail").listSeparator);
        } finally {
            Locale.setDefault(previousDefault);
        }
    }
}
