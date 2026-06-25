package org.openmarkov.integrationTests.integrationTests.localization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.openmarkov.integrationTests.IntegrationTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * See the method {@link LocalizeXML#keysArentRepeated()} ()}, which is the purpose of this test class.
 *
 * @author jrico
 */
public class LocalizeXML {
    
    /**
     * Ensures there are no localization keys repeated across multiple bundles given by
     * {@link LocalizeResourcesProvider}s.
     * <p>
     * This test excludes the {@link LocalizeResourcesProvider} of {@code org.openmarkov.IntegrationTests}, as that one
     * doesn't get included in the full version.
     */
    @Test
    void keysArentRepeated() {
        var keysToProviders = new HashMap<String, ArrayList<LocalizedStringDescription>>();
        StringDatabase.getBundleProviders()
                      .filter(provider -> provider.getClass().getModule() != IntegrationTest.class.getModule())
                      .forEach(provider ->
                                       provider.getBundlesMap(Locale.ENGLISH)
                                               .forEach((bundleName, bundle) -> bundle.getKeys().forEach(key -> {
                                                   if (!keysToProviders.containsKey(key)) {
                                                       keysToProviders.put(key, new ArrayList<>());
                                                   }
                                                   keysToProviders.get(key)
                                                                  .add(new LocalizedStringDescription(provider, bundleName, key));
                                               })));
        keysToProviders.remove("BUNDLEFILE.Text");
        var repeatedKeys = keysToProviders.values().stream()
                                          .filter(resolutions -> resolutions.size() > 1)
                                          .sorted(Comparator.comparing(a -> a.get(0).key))
                                          .map(repetitions -> {
                                              String repeatedKey = repetitions.get(0).key;
                                              var repetitionsLocations = repetitions.stream()
                                                                                    .map(repetition -> "\t- " + repetition.bundleName + "_en.xml of " + repetition.provider.getClass()
                                                                                                                                                                           .getModule()
                                                                                                                                                                           .getName())
                                                                                    .collect(Collectors.joining(System.lineSeparator()));
                                              return "'" + repeatedKey + "' is localized in multiple bundles: " + System.lineSeparator() + repetitionsLocations;
                                          })
                                          .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
        Assertions.assertTrue(repeatedKeys.isBlank(), System.lineSeparator() + repeatedKeys);
    }
    
    /**
     * Aggregate of all the information to represent a localization String from a Bundle file.
     *
     * @param provider   The {@link LocalizeResourcesProvider} where the localization String came from.
     * @param bundleName The name of the bundle.
     * @param key        The key of the string in the bundle.
     */
    record LocalizedStringDescription(LocalizeResourcesProvider provider, String bundleName, String key) {
    }
    
}
