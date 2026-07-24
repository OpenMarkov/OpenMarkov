/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The database of texts is a singleton with global state, so these tests only ever ask it for a
 * language it does not serve: they check what it answers and, above all, what it tells its
 * listeners, without leaving it in a different state than they found it.
 *
 * @author Manuel Arias
 */
@Tag(TestConfig.DisabledInParallel)
class StringDatabaseTest {

    @Test final void theLanguageServedIsEnglish() {
        Assertions.assertEquals("en", StringDatabase.getUniqueInstance().getLanguage());
    }

    /**
     * B1: OpenMarkov is English-only, so a request for another language is refused. What must not
     * happen is what happened before: the language was silently kept in English while the listeners
     * were told it had changed to the requested one.
     */
    @Test final void askingForALanguageThatIsNotServedChangesNothingAndAnnouncesNothing() {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        List<LocaleChangeEvent> announced = new ArrayList<>();
        LocaleChangeListener listener = announced::add;
        stringDatabase.addLocaleChangeListener(listener);
        try {
            stringDatabase.setLanguage("es");

            Assertions.assertEquals("en", stringDatabase.getLanguage());
            Assertions.assertTrue(announced.isEmpty(),
                                  "A change of language was announced that did not happen: " + announced);
        } finally {
            stringDatabase.removeLocaleChangeListener(listener);
        }
    }

    /** Asking for the language already in force is a no-op, and is not announced either. */
    @Test final void askingForTheLanguageInForceAnnouncesNothing() {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        List<LocaleChangeEvent> announced = new ArrayList<>();
        LocaleChangeListener listener = announced::add;
        stringDatabase.addLocaleChangeListener(listener);
        try {
            stringDatabase.setLanguage("en");

            Assertions.assertEquals("en", stringDatabase.getLanguage());
            Assertions.assertTrue(announced.isEmpty());
        } finally {
            stringDatabase.removeLocaleChangeListener(listener);
        }
    }

    @Test final void aKeyThatDoesNotExistComesBackMarkedAsUnknown() {
        Assertions.assertEquals(">>> noSuchKey <<<", StringDatabase.getUniqueInstance().getString("noSuchKey"));
        Assertions.assertNull(StringDatabase.getUniqueInstance().getNullableString("noSuchKey"));
    }
}
