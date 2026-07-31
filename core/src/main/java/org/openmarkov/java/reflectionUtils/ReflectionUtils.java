package org.openmarkov.java.reflectionUtils;

import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

public class ReflectionUtils {
    public sealed interface Source {
        record StaticClass(Class<?> aClass) implements Source {
        }
        
        record Instance(Object object) implements Source {
        }
        
        private static Source of(Object object) {
            if (object instanceof Class<?> aClass) {
                return new StaticClass(aClass);
            }
            return new Instance(object);
        }
        
        private Class<?> getTargetClass() {
            return switch (this) {
                case Instance instance -> instance.object.getClass();
                case StaticClass staticClass -> staticClass.aClass;
            };
        }
        
        private Object getInstance() {
            return switch (this) {
                case Instance instance -> instance.object;
                case StaticClass staticClass -> null;
            };
        }
    }
    
    public static <T> T forceGetField(Object source, String fieldName, Class<T> resType) throws ReflectiveOperationException {
        Source sourceElement = Source.of(source);
        ArrayList<Class<?>> classesOfSource = ClassUtils.extensionClassesOf(sourceElement.getTargetClass());
        classesOfSource.add(0, sourceElement.getTargetClass());
        Field field = classesOfSource
                .stream()
                .map(subclass -> {
                    try {
                        return subclass.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                // The declared family has the exact member for this case; the bare get() of the
                // stream threw a NoSuchElementException instead, which names nothing and slips
                // past a catch written against the declaration.
                .orElseThrow(() -> new NoSuchFieldException(
                        "the class " + sourceElement.getTargetClass().getName()
                                + " does not declare a field named '" + fieldName
                                + "', nor does any of its ancestors"));
        field.setAccessible(true);
        // The instance for an object, null for a static read from a class.
        return resType.cast(field.get(sourceElement.getInstance()));
    }
}
