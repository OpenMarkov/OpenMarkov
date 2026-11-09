package org.openmarkov.java.debugTools;

import java.util.Arrays;
import java.util.function.Predicate;

public class BreakpointTools {
    
    private static boolean isMet(Predicate<StackTraceElement> condition) {
        return Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(condition);
    }
    
    public static boolean stackContainsMethod(String methodName) {
        return isMet(stack -> stack.getMethodName().equals(methodName));
    }
    
}
