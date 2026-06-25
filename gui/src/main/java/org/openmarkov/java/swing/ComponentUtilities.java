package org.openmarkov.java.swing;

import org.jetbrains.annotations.Nullable;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ComponentUtilities {
    
    public static ArrayList<Component> parents(Component component) {
        var parents = new ArrayList<Component>();
        while (component.getParent() != null) {
            parents.add(component.getParent());
            component = component.getParent();
        }
        return parents;
    }
    
    public static ArrayList<Component> parentsWithSelf(Component component) {
        var parents = new ArrayList<Component>();
        parents.add(component);
        while (component.getParent() != null) {
            parents.add(component.getParent());
            component = component.getParent();
        }
        return parents;
    }
    
    public static ArrayList<Component> parentsUpToWindow(Component component) {
        var parents = new ArrayList<Component>();
        while (component.getParent() != null) {
            parents.add(component.getParent());
            if (component.getParent() instanceof Window) {
                break;
            }
            component = component.getParent();
        }
        return parents;
    }
    
    public static ArrayList<Component> parentsWithSelfUpToWindow(Component component) {
        var parents = new ArrayList<Component>();
        parents.add(component);
        while (true) {
            Container parent = component.getParent();
            if (parent == null && component instanceof JPopupMenu popupMenu) {
                if (popupMenu.getInvoker() instanceof Container popupMenuInvoker) {
                    parent = popupMenuInvoker;
                }
                ;
            }
            if (parent == null) break;
            parents.add(parent);
            if (parent instanceof Window) {
                break;
            }
            component = parent;
        }
        return parents;
    }
    
    public enum ComponentSearchOptions {
        DO_NOT_SEARCH_OWNED_WINDOWS
    }
    
    public static final EnumSet<ComponentSearchOptions> DEFAULT_COMPONENT_SEARCH_OPTIONS = EnumSet.noneOf(ComponentSearchOptions.class);
    
    public static Stream<Component> flatComponentsAsStream(Component component, EnumSet<ComponentSearchOptions> searchOptionsSet) {
        return Stream.concat(Stream.of(component),
                             ComponentUtilities.extractSubComponents(component, searchOptionsSet)
                                               .flatMap(component1 -> flatComponentsAsStream(component1, searchOptionsSet))
        );
    }
    
    public static <T> @Nullable T findComponent(Component component, Class<? extends T> componentClass, Predicate<? super T> predicate, ComponentSearchOptions... searchOptions) {
        return findComponents(component, componentClass, predicate, searchOptions)
                .findFirst()
                .orElse(null);
    }
    
    public static <T> Stream<T> findComponents(Component component, Class<? extends T> componentClass, Predicate<? super T> predicate, ComponentSearchOptions... searchOptions) {
        EnumSet<ComponentSearchOptions> searchOptionsSet = EnumSet.noneOf(ComponentSearchOptions.class);
        searchOptionsSet.addAll(Arrays.asList(searchOptions));
        return (Stream<T>) flatComponentsAsStream(component, searchOptionsSet)
                .filter(componentClass::isInstance)
                .map(componentClass::cast)
                .filter(predicate);
    }
    
    private static Stream<Component> extractSubComponents(Component component, EnumSet<ComponentSearchOptions> searchOptionsSet) {
        return switch (component) {
            case Window frame -> Stream.concat(
                    searchOptionsSet.contains(ComponentSearchOptions.DO_NOT_SEARCH_OWNED_WINDOWS)
                            ? Stream.empty() : Arrays.stream(frame.getOwnedWindows()),
                    Arrays.stream(frame.getComponents())
            );
            case JMenu menu -> Stream.concat(
                    Arrays.stream(menu.getMenuComponents()),
                    Arrays.stream(menu.getComponents())
            );
            case Container container -> Arrays.stream(container.getComponents());
            case null, default -> Stream.empty();
        };
    }
    
    public static void addMouseListenerFirst(Component component, MouseListener mouseListener) {
        addListenerAtBeggining(mouseListener, component::getMouseListeners, component::removeMouseListener, component::addMouseListener);
    }
    
    private static <T> void addListenerAtBeggining(T newListener, Supplier<T[]> getAllListener, Consumer<T> removeOneListener, Consumer<T> addOneListener) {
        var existing = getAllListener.get();
        for (T t : existing) {
            removeOneListener.accept(t);
        }
        addOneListener.accept(newListener);
        for (T t : existing) {
            addOneListener.accept(t);
        }
    }
    
    public static void removeInputsFor(Component component) {
        component.setEnabled(false);
        while (component.getMouseListeners().length > 0) {
            component.removeMouseListener(component.getMouseListeners()[0]);
        }
        while (component.getFocusListeners().length > 0) {
            component.removeFocusListener(component.getFocusListeners()[0]);
        }
        while (component.getKeyListeners().length > 0) {
            component.removeKeyListener(component.getKeyListeners()[0]);
        }
    }
}
