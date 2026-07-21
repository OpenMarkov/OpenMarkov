package org.openmarkov.core.stringformat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
        format = format.toLowerCase();
        String finalFormat = format;
        var desiredLength = Arrays.stream(LocalizationFormatterLength.values())
                                  .filter(length -> finalFormat.contains(length.toString().toLowerCase()))
                                  .findFirst()
                                  .orElse(LocalizationFormatter.DEFAULT.desiredLength);
        var listFormat = Arrays.stream(ListFormat.values())
                               .filter(listF -> {
                                   return finalFormat.contains(listF.toString().toLowerCase());
                               })
                               .findFirst()
                               .orElse(LocalizationFormatter.DEFAULT.listSeparator);
        return new LocalizationFormatter(desiredLength, listFormat);
    }
    
    /**
     * Represents multiples levels of formatting detail.
     */
    public enum LocalizationFormatterLength {
        UNSPECIFIED, SHORT, MEDIUM, LONG, VERBOSE
    }
    
}
