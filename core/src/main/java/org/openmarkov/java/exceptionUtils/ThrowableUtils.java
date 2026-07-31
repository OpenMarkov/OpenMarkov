package org.openmarkov.java.exceptionUtils;

import org.openmarkov.java.collectionsUtils.arrayUtils.Slice;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class ThrowableUtils {
    
    /**
     * Moves the stack trace from the source to the target {@link Throwable}: the target ends up
     * with the merged trace ---its own frames first, then the source's--- and <strong>the source is
     * left with no trace at all</strong>. A transfer moves, it does not copy: the wrapper
     * exceptions of the project push their trace down into their cause with this, so that a
     * printed error carries its information in one place instead of repeated in every layer.
     * <p>
     * If the target {@link Throwable} is a cause of the source {@link Throwable}, then their
     * stacktraces will be merged.
     * <p>
     * If either of the two is missing, nothing is done.
     */
    public static void transferStackTrace(Throwable from, Throwable to) {
        if (from == null || to == null) {
            return;
        }
        Throwable source = from;
        Throwable target = to;
        var sourceStackTrace = source.getStackTrace();
        var targetStackTrace = target.getStackTrace();
        var sourceStackTraceIndex = sourceStackTrace.length - 1;
        var targetStackTraceIndex = targetStackTrace.length - 1;
        while (true) {
            if (sourceStackTraceIndex == -1 || targetStackTraceIndex == -1) {
                break;
            }
            StackTraceElement currentSourceTrace = sourceStackTrace[sourceStackTraceIndex];
            StackTraceElement currentTargetTrace = targetStackTrace[targetStackTraceIndex];
            if (!currentSourceTrace.equals(currentTargetTrace)) {
                break;
            }
            sourceStackTraceIndex--;
            targetStackTraceIndex--;
        }
        /*
        //This was the old way before using the Slice<T> class
        int newArraySize = (targetStackTraceIndex + 1) + sourceStackTrace.length;
        var joinedStackTrace = new StackTraceElement[newArraySize];
        System.arraycopy(targetStackTrace, 0, joinedStackTrace, 0, targetStackTraceIndex + 1);
        System.arraycopy(sourceStackTrace, 0, joinedStackTrace, (targetStackTraceIndex + 1), sourceStackTrace.length);
        source.setStackTrace(new StackTraceElement[0]);
        target.setStackTrace(joinedStackTrace);
         */
        var targetUniqueTrace = new Slice<>(StackTraceElement.class, targetStackTrace, 0, targetStackTraceIndex + 1);
        var sourceUniqueTrace = new Slice<>(StackTraceElement.class, sourceStackTrace, 0, sourceStackTraceIndex + 1);
        var commonTrace = new Slice<>(StackTraceElement.class, sourceStackTrace, sourceStackTraceIndex + 1, sourceStackTrace.length);
        var joinedTraces = Slice.slicesToArray(List.of(targetUniqueTrace, sourceUniqueTrace, commonTrace));
        source.setStackTrace(new StackTraceElement[0]);
        target.setStackTrace(joinedTraces);
    }
    
    /**
     * Gets the root cause of a {@link Throwable}, while also transferring the stack trace from the {@link Throwable}
     * and the intermediary {@link Throwable}s to the root cause.
     *
     * @see ThrowableUtils#transferStackTrace(Throwable, Throwable)
     */
    public static Throwable flatten(Throwable throwable) {
        // The language forbids an error being its own cause, but not a cycle of two; walking one
        // would never end. The walk closes on the first already-visited link, and the last new
        // one plays the root.
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visited.add(throwable);
        while (throwable.getCause() != null && visited.add(throwable.getCause())) {
            var cause = throwable.getCause();
            ThrowableUtils.transferStackTrace(throwable, cause);
            throwable = cause;
        }
        return throwable;
    }
    
}
