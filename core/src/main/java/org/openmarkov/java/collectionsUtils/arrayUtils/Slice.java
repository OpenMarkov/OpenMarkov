package org.openmarkov.java.collectionsUtils.arrayUtils;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;

public class Slice<T> {
    
    private final Class<T> elementsClass;
    private final T[] elements;
    private final int start;
    private final int end;
    private final T[] solved;
    private boolean isSolved = false;
    
    public Slice(Class<T> elementsClass, T[] elements, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("End must be greater than start");
        }
        if (start < 0) {
            throw new IndexOutOfBoundsException("Start must be greater than 0");
        }
        if (end < 0) {
            throw new IndexOutOfBoundsException("End must be greater than 0");
        }
        if (start > elements.length) {
            throw new IndexOutOfBoundsException("End must be less than the length of the array");
        }
        if (end > elements.length) {
            throw new IndexOutOfBoundsException("End must be less than the length of the array");
        }
        this.elementsClass = elementsClass;
        this.solved = (T[]) Array.newInstance(this.elementsClass, end - start);
        this.elements = elements;
        this.start = start;
        this.end = end;
    }
    
    public Slice(Class<T> elementsClass, T[] elements) {
        this.elementsClass = elementsClass;
        this.solved = elements;
        this.elements = elements;
        this.start = 0;
        this.end = elements.length;
        this.isSolved = true;
    }
    
    private void solve() {
        if (isSolved) {
            return;
        }
        for (int i = start; i < end; i++) {
            solved[i - start] = elements[i];
        }
        isSolved = true;
    }
    
    public T[] array() {
        this.solve();
        return solved;
    }
    
    public static <T> T @Nullable [] slicesToArray(Collection<Slice<T>> slices) {
        if (slices.isEmpty()) {
            return null;
        }
        if (slices.size() == 1) {
            return slices.iterator().next().array();
        }
        var size = 0;
        var slicesIterator = slices.iterator();
        while (slicesIterator.hasNext()) {
            size += slicesIterator.next().array().length;
        }
        var array = (T[]) Array.newInstance(slices.iterator().next().elementsClass, size);
        slicesIterator = slices.iterator();
        var index = 0;
        while (slicesIterator.hasNext()) {
            var slice = slicesIterator.next();
            System.arraycopy(slice.array(), 0, array, index, slice.array().length);
            index += slice.array().length;
        }
        return array;
        
    }
    
}
