package org.openmarkov.java.classUtils;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A series of methods to work with classes using reflections.
 *
 * @author jrico
 */
public class ClassUtils {
    
    /**
     * Gets a sorted list of all superclasses and interfaces of a class.
     * <p>
     * In the resulting list, all interfaces appear before the classes, and both interfaces and classes are ordered by
     * inheritance. This means a class in the position {@code n} can be a child of all classes {@code n-1},
     * {@code n-2}... but it cannot be a child of classes in positions {@code n+1}, {@code n+2}...
     */
    public static ArrayList<Class<?>> extensionClassesOf(Class<?> aClass) {
        var superClasses = superClassesOf(aClass);
        //Adds all the interfaces of the class and its superclasses
        superClasses.addAll(
                Stream.concat(superClasses.stream(), Stream.of(aClass))
                      .flatMap(anotherSuperClass -> Arrays.stream(anotherSuperClass.getInterfaces()))
                      .collect(Collectors.toSet()));
        Comparator<Class<?>> compareExtension = (class1, class2) -> {
            if (class2.isAssignableFrom(class1)) {
                return 1;
            }
            if (class1.isAssignableFrom(class2)) {
                return -1;
            }
            return 0;
        };
        Comparator<Class<?>> compareIsInterface = Comparator.comparing(Class::isInterface);
        //Interfaces are before classes, and then they are ordered by inheritance
        superClasses.sort(compareIsInterface.reversed().thenComparing(compareExtension));
        return superClasses;
    }
    
    public static ArrayList<Class<?>> superClassesOf(Class<?> aClass) {
        var superClasses = new ArrayList<Class<?>>();
        var superClass = aClass.getSuperclass();
        //Adds every superclass
        while (superClass != null) {
            superClasses.add(superClass);
            superClass = superClass.getSuperclass();
        }
        return superClasses;
    }
    
    //Only usable on testing
    public static File getResourceAsFile(Class<?> sourceClass, String resource){
        var is = sourceClass.getResource(resource).getFile();
        // On Windows, resource URLs start with /C:/ — strip only the leading slash before a drive letter
        if (is.length() > 2 && is.charAt(0) == '/' && is.charAt(2) == ':') {
            is = is.substring(1);
        }
        is=is.replaceFirst("target/test-classes/", "src/test/resources/")
             .replaceFirst("target/classes/", "src/main/resources/");
        return new File(is);
    }
    
    public static HashSet<Class<?>> allInterfacesOf(Class<?> theClass) {
        HashSet<Class<?>> interfacesToVisit = extensionClassesOf(theClass).stream()
                                                                          .flatMap(aClass -> Arrays.stream(aClass.getInterfaces()))
                                                                          .collect(Collectors.toCollection(HashSet::new));
        interfacesToVisit.addAll(List.of(theClass.getInterfaces()));
        HashSet<Class<?>> visitedInterfaces = new HashSet<>();
        while (interfacesToVisit.size() > 0) {
            Class<?> currentInterface = interfacesToVisit.stream().findFirst().get();
            visitedInterfaces.add(currentInterface);
            interfacesToVisit.remove(currentInterface);
            Arrays.stream(currentInterface.getInterfaces())
                  .filter(interfaceToVisit -> !visitedInterfaces.contains(interfaceToVisit))
                  .forEach(interfacesToVisit::add);
        }
        return visitedInterfaces;
    }
    
    public static boolean isConcrete(Class<?> aClass) {
        return !aClass.isInterface() && !aClass.isAnnotation() && !Modifier.isAbstract(aClass.getModifiers());
    }
    
    public static Stream<Class<?>> streamOfInstanciableClassOf(Class<?> clazz) {
        var classes = new ArrayList<Class<?>>();
        while (clazz != null) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return classes.stream();
    }
    
    public static @Nullable URL rawFileOfClass(Class<?> theClass) {
        String simpleName = theClass.getSimpleName();
        URL classUrl = theClass.getResource(simpleName + ".class");
        if (classUrl == null) {
            return null;
        }
        return classUrl;
    }
    
    public static boolean isProductionClass(Class<?> theClass) {
        return !isTestClass(theClass);
    }
    
    public static boolean isTestClass(Class<?> theClass) {
        return theClass.getProtectionDomain().getCodeSource().getLocation().toString().contains("test-classes");
    }
    
    
    /**
     * Gets the file location of a class.
     *
     * @param theClass The class to extract its file location from
     *
     * @return the file location of a class.
     */
    public static @Nullable File fileOfClass(Class<?> theClass) {
        URL classUrl = theClass.getResource(theClass.getSimpleName() + ".class");
        if (classUrl == null) {
            return null;
        }
        String path = new File(classUrl.getFile()).getAbsolutePath();
        path = path.replace("target\\classes", "src\\main\\java");
        path = path.substring(0, path.length() - ".class".length());
        path += ".java";
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return file;
    }
}
