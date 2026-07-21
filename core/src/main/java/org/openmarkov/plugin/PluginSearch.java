package org.openmarkov.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to search for classes matching certain conditions.
 * <p>
 * For example, this code finds all classes in OpenMarkov extending
 * {@link org.openmarkov.core.model.network.type.NetworkType}:
 * <pre>
 * {@code PluginSearch.init().extending(NetworkType.class).list()}
 * </pre>
 *
 * @param <T> the <t>
 *
 * @author jrico
 */
public final class PluginSearch<T> {
    
    /**
     * Stream containing classes that are filtered.
     * <p>
     * It initially contains classes from the OpenMarkov project when calling {@link PluginSearch#init()}
     */
    private final Stream<Class<T>> classStream;
    private final Class<T> currentClass;
    
    /**
     * Initializes a new search over OpenMarkov's classes.
     */
    public static PluginSearch<Object> init() {
        return new PluginSearch<>(PluginLoader.pluginsStream(PluginClassCategory.OPENMARKOV), Object.class);
    }
    
    /**
     * Initializes a new search over every class.
     */
    public static PluginSearch<Object> full() {
        return new PluginSearch<>(
                Arrays.stream(PluginClassCategory.values()).flatMap(PluginLoader::pluginsStream), Object.class);
    }
    
    /**
     * Initializes a new search over classes of specific categories.
     */
    public static PluginSearch<Object> init(List<PluginClassCategory> categories) {
        categories = new ArrayList<>(categories);
        categories.removeIf(Objects::isNull);
        if (categories.isEmpty()) {
            return new PluginSearch<>(Stream.empty(), Object.class);
        }
        categories = categories.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        Stream<Class<Object>> pluginsStream = PluginLoader.pluginsStream(categories.remove(0));
        while (!categories.isEmpty()) {
            pluginsStream = Stream.concat(pluginsStream, PluginLoader.pluginsStream(categories.remove(0)));
        }
        return new PluginSearch<>(pluginsStream, Object.class);
    }
    
    /**
     * Creates a new search with an initial stream.
     */
    private PluginSearch(Stream<Class<T>> classStream, Class<T> currentClass) {
        this.classStream = classStream;
        this.currentClass = currentClass;
    }
    
    /**
     * Filters the search to classes matching this predicate.
     */
    public PluginSearch<T> filter(Predicate<? super Class<T>> predicate) {
        return new PluginSearch<>(this.classStream.filter(predicate), this.currentClass);
    }
    
    /**
     * Filters the search to classes extending {@code ExtendingClass}.
     * <p>
     * The class {@code ExtendingClass} might be present in the results of the search.
     */
    public <ExtendingClass> PluginSearch<ExtendingClass> extending(Class<ExtendingClass> extendingClass) {
        return new PluginSearch<>(
                this.classStream
                        .filter(extendingClass::isAssignableFrom)
                        .map(baseClass -> (Class<ExtendingClass>) baseClass),
                extendingClass
        );
    }
    
    /**
     * Filters the search to classes extending {@code ExtendingClass}.
     * <p>
     * The class {@code ExtendingClass} will NOT be present in the results of the search.
     */
    public <ExtendingClass> PluginSearch<ExtendingClass> childrenOf(Class<ExtendingClass> extendingClass) {
        return this.extending(extendingClass).filter(baseClass -> baseClass != extendingClass);
    }
    
    /**
     * Filters the search to classes annotated with {@code AnnotationClass}.
     */
    public <AnnotationClass> PluginSearch<T> annotatedWith(Class<AnnotationClass> annotationClass) {
        return this.filter(baseClass ->
                                   Arrays.stream(baseClass.getAnnotations())
                                         .anyMatch(annotation -> annotation.annotationType() == annotationClass));
    }
    
    /**
     * Returns the stream containing the filtered classes.
     */
    public Stream<Class<? extends T>> stream() {
        return this.classStream.map(aClass -> aClass);
    }
    
    /**
     * Returns an extension tree. The class starts from the base class for the parameter {@code T}.
     */
    public ExtensionTree<T> extensionTree() {
        return ExtensionTree.start(this.currentClass, this.stream());
    }
    
    /**
     * Returns a list with all the filtered classes.
     */
    public List<Class<T>> list() {
        return this.classStream.toList();
    }
    
}
