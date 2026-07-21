package org.openmarkov.staticAnalysis;

import org.openmarkov.core.localize.StringBundle;
import org.openmarkov.core.localize.StringDatabase;

public class FindInDatabase {
    
    public static void main(String[] args) {
        var bundleKeyAndValues = StringDatabase.getUniqueInstance().getAllBundles().values()
                                               .stream().flatMap(bundle -> bundle.getKeys().stream()
                                                                                 .map(bundleKey -> new BundleKeyAndValue(bundle, bundleKey, bundle.getString(bundleKey))));
        bundleKeyAndValues.filter(bundleKeyAndValue -> bundleKeyAndValue.value().equals("Learn"))
                          .forEach(System.out::println);
    }
    
    record BundleKeyAndValue(StringBundle bundle, String key, String value) {
    }
    
}
