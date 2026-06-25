package org.openmarkov.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ExtensionTree<T> {

    private static final Logger logger = LogManager.getLogger(ExtensionTree.class);

    private final ExtensionTree<?> parent;
    private final Class<T> currentClass;
    private final TreeMap<Class<? extends T>, ExtensionTree<? extends T>> subClasses;
    
    
    public ExtensionTree(ExtensionTree<?> parent, Class<T> currentClass, TreeMap<Class<? extends T>, ExtensionTree<? extends T>> subClasses) {
        this.parent = parent;
        this.currentClass = currentClass;
        this.subClasses = subClasses;
    }
    
    private ExtensionTree(ExtensionTree<?> parent, Class<T> currentClass) {
        this.parent = parent;
        this.currentClass = currentClass;
        this.subClasses = new TreeMap<>(Comparator.comparing(Class::getName));
    }
    
    public static <Target> ExtensionTree<Target> start(Class<Target> rootClass, Stream<Class<? extends Target>> subClasses) {
        if (rootClass.isInterface()) {
            return ExtensionTree.startInterface(rootClass, subClasses);
        }
        ExtensionTree<Target> map = new ExtensionTree<Target>(null, rootClass);
        subClasses.forEach(map::registerClass);
        return map;
    }
    
    private static <Target> ExtensionTree<Target> startInterface(Class<Target> interfaceClass, Stream<Class<? extends Target>> subClasses) {
        ExtensionTree<Target> resultingExtensionMap = new ExtensionTree<>(null, interfaceClass);
        ExtensionTree<Object> objectExtensionTree = new ExtensionTree<Object>(null, Object.class);
        subClasses.forEach(objectExtensionTree::registerClass);
        
        ArrayDeque<ExtensionTree<?>> treesToVisit = new ArrayDeque<>();
        treesToVisit.add(objectExtensionTree);
        while (!treesToVisit.isEmpty()) {
            ExtensionTree<?> currentTree = treesToVisit.removeFirst();
            if (ClassUtils.allInterfacesOf(currentTree.currentClass).contains(interfaceClass)) {
                ExtensionTree asChildExceptionMap = new ExtensionTree(
                        resultingExtensionMap, currentTree.currentClass, currentTree.subClasses);
                resultingExtensionMap.subClasses.put((Class<? extends Target>) currentTree.currentClass, asChildExceptionMap);
            } else {
                for (var subclass : currentTree.subClasses.values()) {
                    treesToVisit.addLast(subclass);
                }
            }
        }
        return resultingExtensionMap;
    }
    
    @Override public int hashCode() {
        return this.currentClass.hashCode();
    }
    
    @Override public boolean equals(Object obj) {
        if (!(obj instanceof ExtensionTree<?> other)) {
            return false;
        }
        return this.currentClass == other.currentClass;
    }
    
    public void print() {
        print(0, new ArrayList<>());
    }
    
    private void print(int level, Collection<Class<?>> parentsShown) {
        Set<Class<?>> parentInterfaces = parentsShown
                .stream()
                .flatMap(parent -> ClassUtils.allInterfacesOf(parent).stream())
                .filter(Class::isInterface)
                .collect(Collectors.toSet());
        String exclusiveInterfacesString = ClassUtils.extensionClassesOf(this.currentClass).stream()
                                                     .filter(Class::isInterface)
                                                     .filter(interfaceClass -> !parentInterfaces.contains(interfaceClass))
                                                     .sorted(Comparator.comparing(Class::getName))
                                                     .map(Class::getName)
                                                     .collect(Collectors.joining(" + "));
        
        String abstractKeyword = Modifier.isAbstract(this.currentClass.getModifiers()) ? "abstract " : "";
        String message = "\t".repeat(level) + "- " + abstractKeyword + this.currentClass;
        if (!exclusiveInterfacesString.isBlank()) {
            message += ": " + exclusiveInterfacesString;
        }
        System.out.println(message);
        var parentsThisFar = new ArrayList<>(parentsShown);
        parentsThisFar.add(this.currentClass);
        for (var subclass : this.subClasses.values()) {
            subclass.print(level + 1, parentsThisFar);
        }
    }
    
    private ExtensionTree<? extends T> registerChild(Class<? extends T> child) {
        if (!this.subClasses.containsKey(child)) {
            this.subClasses.put(child, new ExtensionTree<>(this, child));
        }
        return this.subClasses.get(child);
    }
    
    public void registerClass(Class<?> classToRegister) {
        var classes = ExtensionTree.subclassesOf(classToRegister);
        ExtensionTree currentExtensionTree = this.findFirstAncestor(classes, true);
        for (Class<?> subclass : classes) {
            currentExtensionTree = currentExtensionTree.registerChild(subclass);
        }
    }
    
    public @Nullable ExtensionTree<T> findFirstAncestor(ArrayList<Class<?>> classes, boolean consumeList) {
        if (!consumeList) {
            classes = new ArrayList<>(classes);
        }
        while (!classes.isEmpty()) {
            var subclass = classes.remove(0);
            if (this.currentClass == subclass) {
                return this;
            }
        }
        return null;
    }
    
    
    private static @NotNull ArrayList<Class<?>> subclassesOf(Class<?> classToRegister) {
        var classes = ClassUtils.extensionClassesOf(classToRegister)
                                .stream()
                                .filter(subclass -> !subclass.isInterface())
                                .collect(Collectors.toCollection(ArrayList::new));
        classes.add(classToRegister);
        return classes;
    }
    
    
    public <Target> @Nullable ExtensionTree<Target> find(Class<Target> targetClass) {
        ExtensionTree<?> currentExtensionTree = null;
        var subclasses = subclassesOf(targetClass);
        for (Class<?> subclass : subclasses) {
            if (currentExtensionTree == null && this.currentClass == subclass) {
                currentExtensionTree = this;
                continue;
            }
            if (currentExtensionTree != null) {
                currentExtensionTree = currentExtensionTree.subClasses.get(subclass);
            }
        }
        if (targetClass == currentExtensionTree.currentClass) {
            return (ExtensionTree<Target>) currentExtensionTree;
        }
        return null;
    }
    
    /**
     * Returns a queue with this extension tree and all the sub-extension trees (recursively).
     * <p>
     * The order matches Breath first - Level order. This means the queue is sorted such as trees from level n will be
     * preceded by elements of level n+1.
     */
    public ArrayDeque<ExtensionTree<? extends T>> breathFirstLevelOrderQueue() {
        ArrayDeque<ExtensionTree<? extends T>> queue = new ArrayDeque<>();
        ArrayDeque<ExtensionTree<? extends T>> toVisit = new ArrayDeque<>();
        toVisit.add(this);
        while (!toVisit.isEmpty()) {
            var next = toVisit.removeFirst();
            queue.addLast(next);
            next.subClasses.values().forEach(toVisit::addLast);
        }
        return queue;
    }
    
    @Override public String toString() {
        return "ExtensionTree{" +
                "currentClass=" + currentClass +
                '}';
    }
    
    public ExtensionTree<?> getParent() {
        return this.parent;
    }
    
    public Class<T> getCurrentClass() {
        return this.currentClass;
    }
    
    public TreeMap<Class<? extends T>, ExtensionTree<? extends T>> getSubClasses() {
        return this.subClasses;
    }
    
}
