package org.openmarkov.java.collectionsUtils;

import org.openmarkov.java.collectionsUtils.streamUtils.StreamUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ListUtils {
    
    public static <T> List<T> join(List<? extends T>... lists) {
        Stream<T>[] streams = Arrays.stream(lists).map(Collection::stream).toArray(Stream[]::new);
        return StreamUtils.concat(streams).toList();
    }
    
}
