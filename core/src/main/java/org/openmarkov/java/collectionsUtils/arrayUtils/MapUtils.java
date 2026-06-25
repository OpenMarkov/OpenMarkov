package org.openmarkov.java.collectionsUtils.arrayUtils;

import java.util.Map;

public class MapUtils {
    public static <K,V> Map.Entry<K,V>[] mapToArray(Map<K, V> map){
        Map.Entry<K,V>[] array = new Map.Entry[map.size()];
        var index = 0;
        for (Map.Entry<K,V> stringStringEntry : map.entrySet()) {
            array[index] = stringStringEntry;
            index += 1;
        }
        return array;
    }
}
