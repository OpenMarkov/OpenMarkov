package org.openmarkov.java.collectionsUtils;

import java.util.Collection;
import java.util.function.Predicate;

public class CollectionsUtils {
    
    public static <T> void retainIf(Collection<T> collection, Predicate<? super T> predicate) {
        collection.removeIf(element -> !predicate.test(element));
    }
    
}
