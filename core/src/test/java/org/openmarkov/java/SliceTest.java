package org.openmarkov.java;

import org.junit.jupiter.api.Test;
import org.openmarkov.java.collectionsUtils.arrayUtils.Slice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
}