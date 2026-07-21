package org.openmarkov.java.collectionsUtils.streamUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
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
    
    
    public static <T> @NotNull Stream<T> of(int startInclusive, int endExclusive, IntFunction<T> mapIndex) {
        return IntStream.range(startInclusive, endExclusive).mapToObj(mapIndex);
    }
    
    public static <T> @NotNull Stream<T> of(int length, IntFunction<T> mapIndex) {
        return IntStream.range(0, length).mapToObj(mapIndex);
    }
    
}
