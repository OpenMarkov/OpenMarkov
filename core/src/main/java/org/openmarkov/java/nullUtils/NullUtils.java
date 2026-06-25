package org.openmarkov.java.nullUtils;

import org.jetbrains.annotations.Nullable;

public class NullUtils {
    
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }
    
    @SafeVarargs public static <T> @Nullable T firstNotNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
    
}
