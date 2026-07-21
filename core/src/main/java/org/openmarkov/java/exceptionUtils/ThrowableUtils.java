package org.openmarkov.java.exceptionUtils;

import org.openmarkov.java.collectionsUtils.arrayUtils.Slice;

import java.util.List;

public class ThrowableUtils {
    
    /**
     * Transfers the stack trace from the source to the target {@link Throwable}. This still keeps the target
     * {@link Throwable}'s stacktrace.
     * <p>
     * If the target {@link Throwable} is a cause of the source {@link Throwable}, then their stacktraces will be
     * merged.
     */
    public static void transferStackTrace(Throwable from, Throwable to) {
        if(to==null){
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
        while (throwable.getCause() != null) {
            var cause = throwable.getCause();
            ThrowableUtils.transferStackTrace(throwable, cause);
            throwable = cause;
        }
        return throwable;
    }
    
}
