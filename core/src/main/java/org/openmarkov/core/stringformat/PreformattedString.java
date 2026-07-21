package org.openmarkov.core.stringformat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

/**
 * Similar to {@link MessageFormat}, this represents a message with its {@code pattern} and also the {@code arguments}
 * to apply on that pattern, and when calling {@link PreformattedString#toString()}, it applies the {@code pattern} with
 * the {@code arguments}.
 * <p><br>
 * This example:
 * <pre>{@code
 * PreformattedString header = new PreformattedString("Good morning, I am {Name}, {LastName},",
 *                                                    Map.of("Name", "Jorge", "LastName", "Rico Vivas"));
 * String content = "I write to you to inform you on how to use PreformattedString.";
 * PreformattedString footer = new PreformattedString("Best regards, {Name}.",
 *                                                    Map.of("Name", "Jorge"));
 * PreformattedString message = new PreformattedString("{header}\n\n{content}\n\n\n{footer}",
 *                                      Map.of("header", header, "content", content, "footer", footer));
 * System.out.println(message);
 * }</pre>
 *
 * <br>
 * ... prints:
 *
 * <pre>{@code
 * Good morning, I am Jorge, Rico Vivas,
 *
 * I write to you to inform you on how to use PreformattedString.
 *
 *
 * Best regards, Jorge.
 * }</pre>
 *
 * @see StringFormat StringFormat for more information about how messages are written.
 * @author jrico
 */
public class PreformattedString {
    
    /**
     * The pattern of the message, its pattern is the same as {@link MessageFormat}, but instead of using indexes, it
     * uses named parameters.
     */
    private final @NotNull String pattern;
    private final @NotNull Map<String, Object> namedArguments;
    private @Nullable String result;
    
    /**
     * Creates a PreformattedString with its {@code pattern} and its {@code arguments}.
     */
    public PreformattedString(@Nullable final String pattern, @Nullable final Map<String, Object> namedArguments) {
        if (pattern == null) {
            this.pattern = "";
            this.namedArguments = Map.of();
            return;
        }
        this.pattern = pattern;
        this.namedArguments = Collections.unmodifiableMap(namedArguments == null ? Map.of() : namedArguments);
    }
    
    /**
     * Returns the application of the {@code pattern} and {@code args} on {@link StringFormat#apply(CharSequence, Map)}.
     * <p>
     * The computation of {@link StringFormat#apply(CharSequence, Map)} will happen only once.
     *
     * @return The result of applying {@link StringFormat#apply(CharSequence, Map)} on {@code pattern} and {@code args}.
     */
    @Override
    public final @NotNull String toString() {
        if (this.result == null)
            this.result = StringFormat.apply(this.pattern, this.namedArguments);
        return this.result;
    }
    
}
