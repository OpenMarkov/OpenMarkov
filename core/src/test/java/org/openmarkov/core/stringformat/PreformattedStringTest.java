package org.openmarkov.core.stringformat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author jrico
 */
@SuppressWarnings({"DuplicateStringLiteralInspection", "ConstantExpression", "TextBlockMigration"})
class PreformattedStringTest {
    
    private static final String EXPECTED = "Good morning, I am Jorge, Rico Vivas,\n\n" +
            "I write to you to inform you on how to use PreformattedString.\n\n\n" +
            "Best regards, Jorge.";
    
    /**
     * Tests the generated String is the same as {@link PreformattedStringTest#EXPECTED}.
     */
    @Test final void testToString() {
        PreformattedString header = new PreformattedString("Good morning, I am {Name}, {LastName},",
                                                           Map.of("Name", "Jorge", "LastName", "Rico Vivas"));
        String content = "I write to you to inform you on how to use PreformattedString.";
        PreformattedString footer = new PreformattedString("Best regards, {Name}.",
                                                           Map.of("Name", "Jorge"));
        PreformattedString message = new PreformattedString("{header}\n\n{content}\n\n\n{footer}",
                                                            Map.of("header", header, "content", content, "footer", footer));
        Assertions.assertEquals(PreformattedStringTest.EXPECTED, message.toString());
    }
}