package org.openmarkov.java.collectionsUtils.arrayUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArrayUtils {
    public static <K,V> LinkedHashMap<K, V> arrayToLinkedMap(Map.Entry<K,V>[] array){
        var map = new LinkedHashMap<K,V>(array.length);
        for (Map.Entry<K, V> entry : array){
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
    
    public static <T> void swapElements(T[] array, int i, int j) {
        var objectI = array[i];
        var objectJ = array[j];
        array[i] = objectJ;
        array[j] = objectI;
    }
}
