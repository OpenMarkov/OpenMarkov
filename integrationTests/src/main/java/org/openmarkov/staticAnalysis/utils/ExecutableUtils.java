package org.openmarkov.staticAnalysis.utils;

import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * A series of methods to work with {@link Executable}s ({@link Method} and {@link Constructor}) using reflections.
 *
 * @author jrico
 */
public class ExecutableUtils {
    
    /**
     * Finds the class declaring for this {@link Executable} (Either Method or Constructor), and then returns the
     * {@link Executable} from said class.
     * <p>
     * This is meant to be used for {@link Executable} that are overridden. If the {@link Executable} isn't overridden,
     * then this function will return the same {@link Executable} you sent as a parameter.
     */
    public static <Execution extends Executable> Execution getOriginalDeclarationOfExecutable(Execution executable) {
        String methodName = executable.getName();
        Class<?>[] parameterTypes = executable.getParameterTypes();
        for (var superclass : ClassUtils.extensionClassesOf(executable.getDeclaringClass())) {
            try {
                // Method declared in superclass
                if (executable instanceof Method) {
                    return (Execution) superclass.getDeclaredMethod(methodName, parameterTypes);
                }
                if (executable instanceof Constructor) {
                    return (Execution) superclass.getDeclaredConstructor(parameterTypes);
                }
                try {
                    return (Execution) superclass.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException ignored) {
                }
                try {
                    return (Execution) superclass.getDeclaredConstructor(parameterTypes);
                } catch (NoSuchMethodException ignored) {
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return executable;
    }
}
