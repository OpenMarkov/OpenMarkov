package org.openmarkov.core.stringformat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @author jrico
 */
@SuppressWarnings({"ConstantExpression", "DuplicateStringLiteralInspection"})
class StringFormatListTest {
    
    
    @Test final void testFields() {
        var valueToLocalize = Map.of(
                1, List.of(11, 12, 13),
                2, List.of(21, 22, 23),
                3, List.of(31, 32, 33)
        );
        var line = StringFormat.apply("{value}", Map.of("value", valueToLocalize));
        System.out.println(line);
    }
    
    static class NamedListOfNames {
        private final List<String> listOfNames;
        
        NamedListOfNames(List<String> listOfNames) {
            this.listOfNames = listOfNames;
        }
    }
    
    static class Names {
        private final String name;
        
        Names(String name) {
            this.name = name;
        }
    }
    
}