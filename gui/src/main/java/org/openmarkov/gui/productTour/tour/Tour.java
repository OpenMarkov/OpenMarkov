package org.openmarkov.gui.productTour.tour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.productTour.tour.action.ActionRequester;
import org.openmarkov.java.initialization.Lazy;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.RootPaneContainer;
import javax.swing.event.ChangeListener;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Component;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Tour {
    
    private final @NotNull String name;
    
    private @Nullable WindowAndGlass<? extends Window> activeWindow;
    private final List<WindowAndGlass<? extends Window>> openWindows;
    
    private final @NotNull List<@NotNull OverrideInput> overrideInput;
    
    private static final long MAX_TIME_TO_SEARCH_COMPONENTS = 1000;
    
    private static final int MAX_RETRIES_TO_SEARCH_COMPONENTS = 1000;
    
    
    private record WindowAndGlass<T extends Window & RootPaneContainer>
            (T frame, TourGlassPane<? extends T> tourGlassPane, Component originalGlassPane) {
    }
    
    public Tour(@NotNull String name) {
        this.name = name;
        this.openWindows = Collections.synchronizedList(new ArrayList<>());
        this.overrideInput = Collections.synchronizedList(new ArrayList<>());
        this.effects = Collections.synchronizedList(new ArrayList<>());
        this.deniedEffects = Lazy.of(() -> Collections.synchronizedList(this.effects.stream()
                                                                                    .filter(TourEffects.Deny.class::isInstance)
                                                                                    .map(TourEffects.Deny.class::cast)
                                                                                    .map(TourEffects.Deny::tourEffect)
                                                                                    .toList()));
    }
    
    public final @NotNull String getName() {
        return this.name;
    }
    
    public final <StartingWindow extends Window & RootPaneContainer> void launch(ActionRequester actionRequester, StartingWindow startingWindow) {
        AWTEventListener onWindowOpens = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED || event.getID() == WindowEvent.WINDOW_GAINED_FOCUS || event.getID() == WindowEvent.WINDOW_ACTIVATED) {
                if (event.getSource() instanceof Window window && window instanceof RootPaneContainer) {
                    this.addWindow((Window & RootPaneContainer) window);
                    this.repaintWindows();
                }
            }
            if (event.getID() == WindowEvent.WINDOW_CLOSED) {
                if (event.getSource() instanceof Window window && window instanceof RootPaneContainer) {
                    this.removeWindow((Window & RootPaneContainer) window);
                    this.repaintWindows();
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(onWindowOpens, AWTEvent.WINDOW_EVENT_MASK
                | AWTEvent.WINDOW_FOCUS_EVENT_MASK
                | AWTEvent.WINDOW_STATE_EVENT_MASK);
        
        ChangeListener detectPopups = e -> {
            MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
            if (path.length > 0 && path[path.length - 1] instanceof JPopupMenu popup) {
                ComponentUtilities.parents(popup).stream().forEach(parent -> {
                    if (parent instanceof Window window && window instanceof RootPaneContainer) {
                        this.addWindow((Window & RootPaneContainer) window);
                        this.repaintWindows();
                    }
                });
            }
        };
        MenuSelectionManager.defaultManager().addChangeListener(detectPopups);
        
        this.setActiveWindow(startingWindow);
        try {
            this.execute(actionRequester);
        } finally {
            this.generalCleanUp();
            Toolkit.getDefaultToolkit().removeAWTEventListener(onWindowOpens);
            MenuSelectionManager.defaultManager().removeChangeListener(detectPopups);
            this.cleanUp();
        }
    }
    
    protected abstract void execute(ActionRequester actionRequester);
    
    protected abstract void cleanUp();
    
    private void generalCleanUp() {
        while (!this.openWindows.isEmpty()) {
            var openWindow = this.openWindows.removeLast();
            openWindow.frame.setGlassPane(openWindow.originalGlassPane);
        }
        this.activeWindow = null;
        this.effects.clear();
    }
    
    
    //It should always return a non null value, otherwise, something is wrong with the code of the caller, not here.
    @SuppressWarnings("DataFlowIssue")
    protected  final <T extends Component> @NotNull T findComponent(Class<? extends T> componentClass, Predicate<? super T> predicate) {
        var start = System.currentTimeMillis();
        int tryIndex = 0;
        while (true) {
            tryIndex += 1;
            ArrayList<Window> windowsToSearch;
            synchronized(this){
                windowsToSearch = new ArrayList<Window>(this.openWindows.stream().map(WindowAndGlass::frame).toList());
            }
            windowsToSearch.addAll(windowsToSearch.stream().flatMap(window -> ComponentUtilities
                    .findComponents(window, Window.class, (subWindow) ->
                            subWindow.getClass()
                                     .getName()
                                     .equalsIgnoreCase("javax.swing.Popup$HeavyWeightWindow")
                    )).toList());
            
            for (var window : windowsToSearch) {
                List<ComponentUtilities.ComponentSearchOptions> searchOptions = new ArrayList<>();
                if (!Window.class.isAssignableFrom(componentClass)) {
                    searchOptions.add(ComponentUtilities.ComponentSearchOptions.DO_NOT_SEARCH_OWNED_WINDOWS);
                }
                var component = ComponentUtilities.findComponent(window, componentClass, predicate, searchOptions.toArray(ComponentUtilities.ComponentSearchOptions[]::new));
                if (component != null) {
                    return component;
                }
            }
            long elapsedTime = System.currentTimeMillis() - start;
            if (elapsedTime >= MAX_TIME_TO_SEARCH_COMPONENTS && tryIndex >= MAX_RETRIES_TO_SEARCH_COMPONENTS) {
                return null;
            }
        }
    }
    
    protected final <T extends Component> @NotNull T findComponent(Class<? extends T> componentClass, String name, Predicate<? super T> predicate) {
        return this.findComponent(componentClass, (c) -> name.equals(c.getName()) && predicate.test(c));
    }
    
    protected final <T extends Component> @NotNull T findComponent(Class<? extends T> componentClass, String name) {
        return this.findComponent(componentClass, (c) -> name.equals(c.getName()));
    }
    
    protected final @NotNull Component findComponent(String name, Predicate<? super Component> predicate) {
        return this.findComponent(Component.class, (c) -> name.equals(c.getName()) && predicate.test(c));
    }
    
    protected final @NotNull Component findComponent(String name) {
        return this.findComponent(Component.class, (c) -> name.equals(c.getName()));
    }
    
    protected final <T extends Component> @NotNull T findComponent(Class<? extends T> componentClass) {
        return this.findComponent(componentClass, ignored -> true);
    }
    
    protected final @NotNull Component findComponent(Predicate<? super Component> predicate) {
        return this.findComponent(Component.class, predicate);
    }
    
    protected synchronized final <T extends Window & RootPaneContainer> WindowAndGlass<T> addWindow(T jWindow) {
        var alreadyAddedWindow = this.openWindows.stream()
                                                 .filter(openWindow -> openWindow.frame.equals(jWindow))
                                                 .findFirst();
        if (alreadyAddedWindow.isPresent()) {
            return (WindowAndGlass<T>) alreadyAddedWindow.get();
        }
        TourGlassPane<T> tourGlassPane = new TourGlassPane<>(this, jWindow);
        WindowAndGlass<T> frameAndGlass = new WindowAndGlass<>(jWindow, tourGlassPane, jWindow.getGlassPane());
        this.openWindows.add(frameAndGlass);
        this.activeWindow = frameAndGlass;
        jWindow.setGlassPane(tourGlassPane);
        tourGlassPane.setVisible(true);
        tourGlassPane.resetBoundsAndRepaint();
        return frameAndGlass;
    }
    
    protected final <T extends Window & RootPaneContainer> void setActiveWindow(T jWindow) {
        this.activeWindow = this.addWindow(jWindow);
    }
    
    protected synchronized final <T extends Window & RootPaneContainer> void removeWindow(T jWindow) {
        var iter = this.openWindows.iterator();
        while (iter.hasNext()) {
            var openWindow = iter.next();
            if (openWindow.frame == jWindow) {
                openWindow.frame.setGlassPane(openWindow.originalGlassPane);
                iter.remove();
                if (openWindow == this.activeWindow) {
                    this.activeWindow = this.openWindows.isEmpty() ? null : this.openWindows.getLast();
                }
            }
        }
    }
    
    public void repaintWindows() {
        this.openWindows.stream().forEach(windowAndGlass -> {
            windowAndGlass.tourGlassPane.resetRepaintCount();
            windowAndGlass.frame.repaint();
        });
    }
    
    public Stream<OverrideInput> overrideInputsOf(Component target) {
        return this.overrideInput.stream()
                                 .filter(overrideInput1 -> ComponentUtilities.parentsWithSelfUpToWindow(overrideInput1.getTarget())
                                                                             .contains(target));
    }
    
    public void addOverrideInput(@NotNull OverrideInput overrideInput) {
        this.overrideInput.add(overrideInput);
    }
    
    public void removeOverrideInput(OverrideInput overrideInput) {
        this.overrideInput.remove(overrideInput);
    }
    
    public List<OverrideInput> getAllOverrideInputs() {
        return new ArrayList<>(this.overrideInput);
    }
    
    private final List<TourEffects.TourEffect> effects;
    
    private final Lazy<List<TourEffects.TourEffect>> deniedEffects;
    
    public void applyEffects(List<TourEffects.TourEffect> effects) {
        this.effects.addAll(effects);
        this.deniedEffects.reset();
        this.repaintWindows();
    }
    
    public void removeEffects(List<TourEffects.TourEffect> effects) {
        this.effects.removeAll(effects);
        this.deniedEffects.reset();
        this.repaintWindows();
    }
    
    public void applyEffects(TourEffects.TourEffect... effects) {
        if (effects.length == 0) {
            return;
        }
        Collections.addAll(this.effects, effects);
        this.deniedEffects.reset();
        this.repaintWindows();
    }
    
    public void removeEffects(TourEffects.TourEffect... effects) {
        if (effects.length == 0) {
            return;
        }
        for (var effect : effects) {
            this.effects.remove(effect);
        }
        this.deniedEffects.reset();
        this.repaintWindows();
    }
    
    <T extends TourEffects.TourEffect> @NotNull Optional<T> getEffect(Class<T> effectType) {
        try {
            return Optional.of(getEffects(effectType).toList().getLast());
        } catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }
    
    <T extends TourEffects.TourEffect> @NotNull Stream<T> getEffects(Class<T> effectType) {
        return this.effects.stream()
                           .filter(effectType::isInstance)
                           .map(effectType::cast)
                           .filter(effect -> !this.deniedEffects.get().contains(effect));
    }
    
    public Window getActiveWindow() {
        return this.activeWindow.frame;
    }
    
    public TourGlassPane<? extends Window> getWindowTourGlassPaneOf(Component component) {
        var parents = ComponentUtilities.parentsWithSelfUpToWindow(component);
        for (var openWindows : this.openWindows) {
            if (parents.contains(openWindows.frame)) {
                return openWindows.tourGlassPane;
            }
        }
        return null;
    }
    
}
