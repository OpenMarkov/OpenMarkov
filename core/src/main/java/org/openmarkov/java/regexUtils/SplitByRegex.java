package org.openmarkov.java.regexUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SplitByRegex {
    
    /**
     * Splits the input string into substrings where the pattern matches and where it does not.
     */
    public static @NotNull List<String> splitAll(Pattern pattern, String input) {
        var matchesRanges = pattern.matcher(input).results()
                                   .map(matchResult -> new Range(matchResult.start(), matchResult.end()))
                                   .toList();
        
        var unmatchesRanges = new ArrayList<Range>();
        if (matchesRanges.isEmpty()) {
            unmatchesRanges.add(new Range(0, input.length()));
        }
        if (!matchesRanges.isEmpty() && matchesRanges.getFirst().start() != 0) {
            unmatchesRanges.add(new Range(0, matchesRanges.getFirst().start()));
        }
        if (!matchesRanges.isEmpty() && matchesRanges.getLast().end() != input.length()) {
            unmatchesRanges.add(new Range(matchesRanges.getLast().end(), input.length()));
        }
        IntStream.range(0, matchesRanges.size() - 1).forEach(i -> {
            unmatchesRanges.add(new Range(matchesRanges.get(i).end(), matchesRanges.get(i + 1).start()));
        });
        return Stream.concat(unmatchesRanges.stream(), matchesRanges.stream())
                     .sorted(Comparator.comparing(Range::start))
                     .map(range -> input.substring(range.start(), range.end()))
                     .toList();
    }
    
    record Range(int start, int end) {
    }
}
