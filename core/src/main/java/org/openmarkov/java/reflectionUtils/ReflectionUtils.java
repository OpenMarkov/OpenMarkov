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
                .get();
        field.setAccessible(true);
        return resType.cast(field.get(source));
    }
}
