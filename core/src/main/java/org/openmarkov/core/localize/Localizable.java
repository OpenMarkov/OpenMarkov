package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.core.stringformat.StringFormat;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents objects that could be represented by a String in a Localization Bundle file.
 *
 * @author jrico
 */
@FunctionalInterface
public interface Localizable {
    
    /**
     * Returns the key/path of the localization key in the Bundle file.
     *
     * @return The key/path of the localization key in the Bundle file.
     */
    @NotNull String path();
    
    /**
     * Returns the name of the Bundle file where the key should be localized.
     *
     * @return The name of the Bundle file where the key should be localized.
     */
    @Nullable default String bundle() {
        return null;
    }
    
    /**
     * Localizes the current object using a default {@link LocalizationFormatter}.
     * <p>
     * In the default implementation, this {@link LocalizationFormatter} is {@link LocalizationFormatter#DEFAULT}.
     *
     * @return a localized String representing this class.
     */
    @NotNull default String localize() {
        return this.localize(LocalizationFormatter.DEFAULT);
    }
    
    /**
     * Localizes the current object based on the given {@link LocalizationFormatter} in order to give a String
     * representing it.
     * <p>
     * The default implementation is just a call to {@link Localizable#localize(Object, LocalizationFormatter, String)}.
     *
     * @param formatter the {@link LocalizationFormatter} used to format the object.
     * @return the localized string if available.
     */
    @NotNull default String localize(LocalizationFormatter formatter) {
        return Localizable.localize(this, formatter, this.path());
    }
    
    /**
     * Localizes the current object based on the given {@link LocalizationFormatter} in order to give a String
     * representing it.
     * <p>
     * It retrieves a Localized String using the stringDatabaseKey to get the bundled string over the
     * {@link StringDatabase#getUniqueInstance()}. If the String is found, it will format said String using
     * {@link StringFormat#apply(CharSequence, Map)}, where the arguments are the fields of the {@link Localizable}.
     * <p>
     * If no localization String is found, it returns the name of the class surrounded by &gt;&gt;&gt;&lt;&lt;&lt; as
     * {@link StringDatabase#surrondAsUnknown(String)} does.
     *
     * @param formatter the {@link LocalizationFormatter} used to format the object.
     * @param stringDatabaseKey the key used to look up the localized string in the {@link StringDatabase}.
     * @return the localized string if available.
     */
    @NotNull
    static String localize(Object localizable, LocalizationFormatter formatter, @NotNull String stringDatabaseKey) {
        Optional<String> optionalLocalizedString = Localizable.findLocalizedString(formatter, stringDatabaseKey);
        if (optionalLocalizedString.isEmpty()) {
            return StringDatabase.surrondAsUnknown(stringDatabaseKey);
        }
        String localizedString = optionalLocalizedString.get();
        if (!StringFormat.isStringFormatUsed(localizedString)) {
            return localizedString;
        }
        return StringFormat.applyOnObject(localizedString, localizable);
    }
    
    /**
     * Searches for the first localization String in the bundles matching the {@link LocalizationFormatter}.
     *
     * @param formatter The {@link LocalizationFormatter} that specifies the desired format for the localization string.
     * @param stringDatabaseKey the string database key
     * @return An {@link Optional} containing the first matching localized string found.
     */
    @SuppressWarnings("SimplifyForEach")
    private static @NotNull Optional<String> findLocalizedString(LocalizationFormatter formatter, @NotNull String stringDatabaseKey) {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        ArrayList<Supplier<String>> localizationAccessors = new ArrayList<>();
        Localizable.lengthOrder(formatter.desiredLength)
                   .forEach(length -> localizationAccessors.add(
                           () -> stringDatabase.getNullableString(stringDatabaseKey + "." + length.toString()
                                                                                                  .toLowerCase())));
        localizationAccessors.add(() -> stringDatabase.getNullableString(stringDatabaseKey));
        return localizationAccessors.stream().map(Supplier::get).filter(Objects::nonNull).findFirst();
    }
    
    /**
     * Gets in which order the Formatters should be discovered in the
     * {@link Localizable#findLocalizedString(LocalizationFormatter, String)} function.
     *
     * @param length the specified {@link LocalizationFormatter.LocalizationFormatterLength}.
     * @return a stream of {@link LocalizationFormatter.LocalizationFormatterLength} values in a specific discovery
     * order.
     */
    private static @NotNull Stream<LocalizationFormatter.LocalizationFormatterLength>
    lengthOrder(@NotNull LocalizationFormatter.LocalizationFormatterLength length) {
        return switch (length) {
            case UNSPECIFIED -> Stream.empty();
            case SHORT -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.SHORT,
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE);
            case MEDIUM -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.SHORT,
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE);
            case LONG -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE,
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.SHORT);
            case VERBOSE -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE,
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.SHORT);
        };
    }
    
}
