package org.openmarkov.java.function;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PriorityComparator<T> implements Comparator<T> {
    
    private final List<T> priorityItems;
    
    @SafeVarargs
    public PriorityComparator(T... priorityItems) {
        this.priorityItems = Arrays.stream(priorityItems).toList();
    }
    
    @Override
    public int compare(T value1, T value2) {
        int index1 = this.priorityItems.indexOf(value1);
        int index2 = this.priorityItems.indexOf(value2);
        if (index1 == -1) {
            index1 = Integer.MAX_VALUE;
        }
        if (index2 == -1) {
            index2 = Integer.MAX_VALUE;
        }
        return Integer.compare(index1, index2);
    }
}
