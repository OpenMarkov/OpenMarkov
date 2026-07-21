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
import org.openmarkov.java.initialization.Lazy;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
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

	/*
	private static final String DEFAULT_LANGUAGE = OpenMarkovPreferences
			.get(OpenMarkovPreferences.PREFERENCE_LANGUAGE, OpenMarkovPreferences.OPENMARKOV_LANGUAGES,
					System.getProperty("user.language"));
					
	OpenMarkovPreferences.set(OpenMarkovPreferences.PREFERENCE_LANGUAGE, newLanguage,
		OpenMarkovPreferences.OPENMARKOV_LANGUAGES);

	 */
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
    private List<LocaleChangeListener> listenerList = null;
    
    
    /**
     * This constructor initializes the object with the language of the class.
     * Then creates all the resource bundles to check if the language is
     * available for all of them. If this language is not available for all, the
     * default one is used.
     */
    private StringDatabase() {
        setLocale(new Locale(language));
        /* Set format locale to english (to format decimal point)*/
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        listenerList = new ArrayList<>();
    }
    
    
    /**
     * Returns the unique instance of this class. If the instance doesn't exist,
     * then a new instance is initialized.
     *
     * @return the unique instance.
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
        if (language.equals("es")) {
            return new Locale("es");
        }
        return Locale.ENGLISH;
    }
    
    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Sets the language to a new one.
     *
     * @param newLanguage new language.
     */
    public void setLanguage(String newLanguage) {
        if (!newLanguage.equals(language)) {
            language = "en";
            setLocale(getLocaleByLanguage(language));
            /* Set format locale to english (to format decimal point)*/
            Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
            resetBundles();
            fireLocaleChangeEvent(new LocaleChangeEvent(this, newLanguage));
			/*
			OpenMarkovPreferences.set(OpenMarkovPreferences.PREFERENCE_LANGUAGE, newLanguage,
					OpenMarkovPreferences.OPENMARKOV_LANGUAGES);
			*/
        }
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
                                   return null;
                               }
                           })
                           .filter(Objects::nonNull);
    }
    
    public Map<String, StringBundle> getAllBundles() {
        if (bundles == null) {
            bundles = calculateAllBundles();
            if (bundles.isEmpty() && !language.equals("en")) {
                setLanguage("en");
                bundles = calculateAllBundles();
            }
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
     * reset the StringResource to null
     */
    private void resetBundles() {
        this.bundles = null;
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
        try {
            String result = getString(key);
            if (strings == null) {
                return result;
            }
            final String diacritic = "~";
            int index = 0;
            int i = 0;
            boolean flag = true;
            while (flag && (i < strings.length)) {
                if ((index = result.indexOf(diacritic, index)) >= 0) {
                    String parameter = strings[i++];
                    if (parameter == null) {
                        parameter = "";
                    }
                    result = result.substring(0, index) + result.substring(index)
                                                                .replaceFirst(diacritic, parameter);
                    index += parameter.length();
                } else {
                    flag = false;
                }
            }
            return result;
        } catch (MissingResourceException e1) {
            return StringDatabase.surrondAsUnknown(key);
        }
    }
    
}
