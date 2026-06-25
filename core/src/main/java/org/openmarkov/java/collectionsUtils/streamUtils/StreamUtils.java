package org.openmarkov.java.collectionsUtils.streamUtils;

import java.util.Arrays;
import java.util.stream.Stream;

public class StreamUtils {
    
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<? extends T>... streams) {
        if (streams.length == 0) {
            return Stream.empty();
        }
        var streamsIterator = Arrays.stream(streams).iterator();
        Stream<? extends T> resStream = streamsIterator.next();
        while (streamsIterator.hasNext()) {
            resStream = Stream.concat(resStream, streamsIterator.next());
        }
        return (Stream<T>) resStream;
    }
    
    
}
