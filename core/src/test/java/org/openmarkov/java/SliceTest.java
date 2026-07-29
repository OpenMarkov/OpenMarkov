package org.openmarkov.java;

import org.junit.jupiter.api.Test;
import org.openmarkov.java.collectionsUtils.arrayUtils.Slice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SliceTest {
    
    private static final String[] ABCDE_ARRAY = new String[]{"a", "b", "c", "d", "e"};
    private static final Slice<String> AB_SLICE = new Slice<>(String.class, SliceTest.ABCDE_ARRAY, 0, 2);
    private static final Slice<String> BCD_SLICE = new Slice<>(String.class, SliceTest.ABCDE_ARRAY, 1, 4);
    private static final Slice<String> DE_SLICE = new Slice<>(String.class, SliceTest.ABCDE_ARRAY, 3, 5);
    
    @Test final void testArray() {
        assertArrayEquals(new String[]{"a", "b"}, SliceTest.AB_SLICE.array());
        assertArrayEquals(new String[]{"b", "c", "d"}, SliceTest.BCD_SLICE.array());
        assertArrayEquals(new String[]{"d", "e"}, SliceTest.DE_SLICE.array());
    }
    
    @Test final void testSlicesToArray() {
        String[] joinedSlices = Slice.slicesToArray(List.of(
                SliceTest.AB_SLICE, SliceTest.BCD_SLICE, SliceTest.DE_SLICE));
        assertArrayEquals(new String[]{"a", "b", "b", "c", "d", "d", "e"}, joinedSlices);
    }

    /** The whole-array constructor used to hand out the very array of the caller. */
    @Test final void theHandedOutArrayIsNotTheCallersOne() {
        String[] original = {"a", "b", "c"};
        Slice<String> whole = new Slice<>(String.class, original);

        assertNotSame(original, whole.array());
        whole.array()[0] = "changed";
        assertArrayEquals(new String[]{"a", "b", "c"}, original,
                          "writing on what the slice hands out must not reach the caller's array");
    }

    /** Once read, the slice is settled: later changes to the origin array do not reach it. */
    @Test final void onceReadTheSliceIsSettled() {
        String[] original = {"a", "b", "c"};
        Slice<String> whole = new Slice<>(String.class, original);
        whole.array();

        original[1] = "changed";
        assertArrayEquals(new String[]{"a", "b", "c"}, whole.array());
    }

    /** Validating the start it talked about the end. */
    @Test final void theStartValidationTalksAboutTheStart() {
        IndexOutOfBoundsException error = assertThrows(IndexOutOfBoundsException.class,
                () -> new Slice<>(String.class, new String[]{"a"}, 3, 3));
        assertTrue(error.getMessage().contains("Start"),
                   () -> "the message must talk about the start; it says: " + error.getMessage());
    }

    /** Zero is a valid start, so the message must ask for zero or more, not for more than zero. */
    @Test final void aNegativeStartAsksForZeroOrMore() {
        IndexOutOfBoundsException error = assertThrows(IndexOutOfBoundsException.class,
                () -> new Slice<>(String.class, new String[]{"a"}, -1, 1));
        assertTrue(error.getMessage().contains("greater than or equal"),
                   () -> "the message must ask for zero or more; it says: " + error.getMessage());
    }
}