package org.openmarkov.core.stringformat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * Represents information about formatting options indicated by a user. Said information is read for the 'style' field
 * when processing a String in {@link StringFormat#apply(CharSequence, Map)}.
 */
public final class LocalizationFormatter {
    
    public static final LocalizationFormatter DEFAULT = new LocalizationFormatter(
            LocalizationFormatterLength.UNSPECIFIED,
            ListFormat.INLINE);
    
    public final @NotNull LocalizationFormatterLength desiredLength;
    public final @NotNull ListFormat listSeparator;
    
    /**
     * How a collection or a map is written out.
     * <p>
     * {@link #INLINE}, the default, writes it on one line between brackets: {@code [a, b, c]}.
     * {@link #DETAIL} writes one item per line, each preceded by a dash. No bundle asks for
     * {@code detail} today, but any of them can: it is enough to write the style, as in
     * {@code {nodes, om, detail}}.
     */
    public enum ListFormat {
        INLINE, DETAIL;

        public @NotNull String globalPrefix() {
            return switch (this) {
                case INLINE -> "[";
                case DETAIL -> "";
            };
        }
        
        public @NotNull String globalSuffix() {
            return switch (this) {
                case INLINE -> "]";
                case DETAIL -> "";
            };
        }
        
        public @NotNull String itemPrefix() {
            return switch (this) {
                case INLINE -> "";
                case DETAIL -> "- ";
            };
        }
        
        /**
         * @return what closes each item. Empty for both shapes today; it exists so that the item is
         * wrapped symmetrically —{@link #itemPrefix()} … {@code itemSuffix()}— and a shape that needs
         * to close its items can be added without touching the code that writes the lists.
         */
        public @NotNull String itemSuffix() {
            return "";
        }
        
        
        public @NotNull String separator() {
            return switch (this) {
                case INLINE -> ", ";
                case DETAIL -> System.lineSeparator();
            };
        }
    }
    
    /**
     * Constructs a new instance of {@code LocalizationFormatter} with the specified formatting length.
     *
     * @param desiredLength The desired length
     * @param listSeparator the list separator
     */
    private LocalizationFormatter(@NotNull LocalizationFormatterLength desiredLength, @NotNull ListFormat listSeparator) {
        this.desiredLength = desiredLength;
        this.listSeparator = listSeparator;
    }
    
    /**
     * Parses the {@code format} String to create an instance of {@code LocalizationFormatter}.
     * <p>
     * If the {@code format} parameter is null, then it returns {@link LocalizationFormatter#DEFAULT}.
     *
     * @param format The textual representation of the desired format.
     * @return The result of parsing the input as a {@link LocalizationFormatter}.
     */
    public static LocalizationFormatter of(@Nullable String format) {
        if (format == null || format.isBlank()) {
            return LocalizationFormatter.DEFAULT;
        }
        // Locale.ROOT, not the default locale: the style comes from a resource bundle and is matched
        // against the names of these enums, so the conversion must not depend on where the program
        // runs. With a Turkish locale, "INLINE".toLowerCase() gives "ınlıne" (dotless i) and the
        // style would stop being recognised.
        String finalFormat = format.toLowerCase(Locale.ROOT);
        var desiredLength = Arrays.stream(LocalizationFormatterLength.values())
                                  .filter(length -> finalFormat.contains(length.toString().toLowerCase(Locale.ROOT)))
                                  .findFirst()
                                  .orElse(LocalizationFormatter.DEFAULT.desiredLength);
        var listFormat = Arrays.stream(ListFormat.values())
                               .filter(listF -> finalFormat.contains(listF.toString().toLowerCase(Locale.ROOT)))
                               .findFirst()
                               .orElse(LocalizationFormatter.DEFAULT.listSeparator);
        return new LocalizationFormatter(desiredLength, listFormat);
    }
    
    /**
     * Represents multiples levels of formatting detail.
     * <p>
     * Careful: the length does <strong>not</strong> shorten anything inside
     * {@link StringFormat#apply(CharSequence, Map)} — {@code {nodes, om, short}} writes the same list
     * as {@code {nodes, om}}. It is read further up, by {@code Localizable.findLocalizedString}, to
     * choose <em>which</em> entry of the bundle is used; the shortening, if any, is the one the author
     * of the bundle wrote in that entry.
     */
    public enum LocalizationFormatterLength {
        UNSPECIFIED, SHORT, MEDIUM, LONG, VERBOSE
    }
    
}
