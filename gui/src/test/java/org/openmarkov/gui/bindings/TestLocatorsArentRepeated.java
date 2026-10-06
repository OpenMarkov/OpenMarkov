package org.openmarkov.gui.bindings;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

class TestLocatorsArentRepeated {
    
    @Test
    void TestLocatorsArentRepeated() {
        var bindingsByLocator = Bindings.ALL_BINDINGS.stream()
                                                     .filter(binding -> binding.locator != null)
                                                     .collect(Collectors.groupingBy(binding -> binding.locator));
        
        var repeatedBindingsByLocator = new TreeMap<>(bindingsByLocator.entrySet()
                                                                       .stream()
                                                                       .filter(stringListEntry -> stringListEntry.getValue()
                                                                                                                 .size() > 1)
                                                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        
        if (repeatedBindingsByLocator.isEmpty()) {
            return;
        }
        fail("There are multiple bindings by the same locator: " + System.lineSeparator() + repeatedBindingsByLocator);
        
        
    }
    
}