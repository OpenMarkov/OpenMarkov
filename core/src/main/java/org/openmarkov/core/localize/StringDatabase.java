/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.java.initialization.Lazy;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * This class creates new string resources with the recorded language.
 *
 * @author jmendoza
 * @version 1.3. ibermejo challenge everything
 */
public class StringDatabase {
    
    /**
     * Default language.
     */
    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * The languages this database serves.
     * <p>
     * Only English: the team decided that the whole application is in English. The Spanish files are
     * kept in the repository in case that decision is ever revisited — adding {@code "es"} here is
     * what would bring them back, since the rest of the machinery (locale, bundles, caches and the
     * change event) does honour whatever this set allows.
     */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(StringDatabase.DEFAULT_LANGUAGE);
    /**
     * Unique instance of this class.
     */
    public static final StringDatabase INSTANCE = new StringDatabase();
    
    /**
     * Language to use.
     */
    private String language = DEFAULT_LANGUAGE;
    
    /**
     * Locale to use
     */
    private Locale locale = null;
    /**
     * Map containing all the bundles
     */
    private Map<String, StringBundle> bundles = null;
    
    
    private final Lazy<HashMap<String, String>> keysToValues = new Lazy<>(() -> {
        var keysToValues = new HashMap<String, String>();
        for (var bundle : getAllBundles().values()) {
            for (var key : bundle.getKeys()) {
                keysToValues.put(key, bundle.getString(key));
            }
        }
        return keysToValues;
    });
    
    // Create the listener list
    private final List<LocaleChangeListener> listenerList = new CopyOnWriteArrayList<>();
    
    
    /**
     * Sets the locale of the language in force. The bundles are not read here: they are read the
     * first time a text is asked for. (This comment used to claim that the constructor loaded every
     * bundle to check the language was available in all of them, and it never did.)
     */
    private StringDatabase() {
        setLocale(StringDatabase.getLocaleByLanguage(language));
    }
    
    
    /**
     * @return the unique instance, which is created when the class is loaded (there is nothing to
     * initialise on demand, whatever the old wording of this comment said).
     */
    public static StringDatabase getUniqueInstance() {
        return StringDatabase.INSTANCE;
    }
    
    public static String surrondAsUnknown(String string) {
        if (string == null) {
            return ">>> null <<<";
        }
        return ">>> " + string + " <<<";
    }
    
    private static Locale getLocaleByLanguage(String language) {
        return Locale.of(language);
    }
    
    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Sets the language to a new one, if it is one of the languages this database serves.
     * <p>
     * A language that is not served is <strong>refused</strong>: a warning is logged, the language in
     * force does not change and no {@link LocaleChangeEvent} is fired. That last part is the point —
     * this method used to overwrite the language with {@code "en"} whatever it was asked for, and then
     * announce the <em>requested</em> language to its listeners, leaving them believing in a change
     * that had not happened.
     *
     * @param newLanguage new language.
     *
     * @see #SUPPORTED_LANGUAGES
     */
    public void setLanguage(String newLanguage) {
        if (newLanguage == null || newLanguage.equals(language)) {
            return;
        }
        if (!StringDatabase.SUPPORTED_LANGUAGES.contains(newLanguage)) {
            OpenMarkovLogger.LOGGER.warn("Ignoring the request to use the language '" + newLanguage
                                                 + "': OpenMarkov serves " + StringDatabase.SUPPORTED_LANGUAGES
                                                 + " only, so the language stays '" + language + "'.");
            return;
        }
        language = newLanguage;
        setLocale(StringDatabase.getLocaleByLanguage(newLanguage));
        resetBundles();
        fireLocaleChangeEvent(new LocaleChangeEvent(this, newLanguage));
    }
    
    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * @param newLocale the locale to set
     */
    public void setLocale(Locale newLocale) {
        locale = newLocale;
    }
    
    private Map<String, StringBundle> calculateAllBundles() {
        //Iterable<LocalizeResourcesProvider> providers = ServiceLoader.load(LocalizeResourcesProvider.class);
        Iterable<? extends LocalizeResourcesProvider> providers = StringDatabase.getBundleProviders().toList();
        Map<String, StringBundle> bundlesMap = new LinkedHashMap<>();
        for (LocalizeResourcesProvider provider : providers) {
            bundlesMap.putAll(provider.getBundlesMap(this.locale));
        }
        return bundlesMap;
    }
    
    public static @NotNull Stream<? extends LocalizeResourcesProvider> getBundleProviders() {
        return PluginSearch.init()
                           .childrenOf(LocalizeResourcesProvider.class)
                           .stream()
                           .map(c -> {
                               try {
                                   return c.getDeclaredConstructor().newInstance();
                               } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                        NoSuchMethodException e) {
                                   OpenMarkovLogger.LOGGER.warn("The localization provider " + c.getName()
                                                                        + " could not be instantiated, so the texts of"
                                                                        + " its module will be missing.", e);
                                   return null;
                               }
                           })
                           .filter(Objects::nonNull);
    }
    
    public synchronized Map<String, StringBundle> getAllBundles() {
        if (bundles == null) {
            bundles = calculateAllBundles();
        }
        return bundles;
    }
    
    
    // This methods allows classes to register for LocaleChangeEvent
    public void addLocaleChangeListener(LocaleChangeListener listener) {
        listenerList.add(listener);
    }
    
    // This methods allows classes to unregister for LocaleChangeEvent
    public void removeLocaleChangeListener(LocaleChangeListener listener) {
        listenerList.remove(listener);
    }
    
    /**
     * This private class is used to fire LocaleChangeEvent
     *
     * @param evt - event to manage for locale change
     */
    protected void fireLocaleChangeEvent(LocaleChangeEvent evt) {
        for (LocaleChangeListener listener : listenerList) {
            listener.processLocaleChange(evt);
        }
    }
    
    /**
     * Drops everything that was computed for the language in force, so that it is computed again for
     * the new one.
     * <p>
     * There are two caches in a row, and only the first one used to be dropped: {@code bundles} holds
     * the bundles read from the XML files, and {@code keysToValues} the flat key → text map built out
     * of them, which is what {@link #getNullableString} actually reads. Leaving the second one
     * standing meant the served texts stayed in the first language for ever, even once the bundles
     * had been reloaded in another one.
     */
    private synchronized void resetBundles() {
        this.bundles = null;
        this.keysToValues.reset();
    }
    
    public String getString(String key) {
        String value = this.getNullableString(key);
        return value != null ? value : StringDatabase.surrondAsUnknown(key);
    }
    
    public @Nullable String getNullableString(String key) {
        return keysToValues.get().get(key);
    }
    
    /**
     * This method returns the requested string resource, replacing each '~' by
     * an element of the array. The number of '~' replaced depends on the number
     * of elements of the array.
     *
     * @param key     the key of the desired string.
     * @param strings strings that will replace the '~'.
     *
     * @return the string associated with the key. if the resource doesn't
     * exist, then a special string is returned.
     */
    public String getFormattedString(String key, String... strings) {
        String result = getString(key);
        if (strings == null) {
            return result;
        }
        final String diacritic = "~";
        int index = 0;
        for (String string : strings) {
            index = result.indexOf(diacritic, index);
            if (index < 0) {
                break;
            }
            String parameter = string == null ? "" : string;
            // Plain concatenation, not replaceFirst: that method reads its second argument as a
            // regular-expression replacement, where $ and \ mean something. A parameter carrying one
            // — the name of a network, a file path — either came out corrupted or threw
            // IllegalArgumentException ("Illegal group reference"). See B4 of the report.
            result = result.substring(0, index) + parameter + result.substring(index + diacritic.length());
            index += parameter.length();
        }
        return result;
    }
    
}
