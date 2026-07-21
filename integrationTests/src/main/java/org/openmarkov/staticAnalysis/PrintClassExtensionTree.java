package org.openmarkov.staticAnalysis;

import org.openmarkov.core.model.network.potential.Reorderable;
import org.openmarkov.plugin.PluginSearch;

public class PrintClassExtensionTree {
    
    public static void main(String[] args) {
        PluginSearch.full()
                    .extending(Reorderable.class)
                    .extensionTree()
                    .print();
    }
    
}
