/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.loader.element.CursorLoader;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.plugin.PluginSearch;

import java.awt.Cursor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manages the available edition modes (selection, node creation, link creation).
 * <p>
 * Non-node modes (selection, link) are discovered via the {@link EditionState}
 * plugin annotation.  Node-creation modes are registered programmatically —
 * one {@link NodeEditionMode} per {@link NodeType} that has a GUI representation.
 */
public class EditionModeManager {

    /**
     * Static metadata for node-creation edition modes, keyed by the
     * localisation name that the rest of the GUI already uses
     * (e.g.&nbsp;{@code "Edit.Mode.Chance"}).
     */
    private record NodeModeDescriptor(String name, String icon, String cursor, NodeType nodeType) {}

    private static final List<NodeModeDescriptor> NODE_MODE_DESCRIPTORS = List.of(
            new NodeModeDescriptor("Edit.Mode.Chance",   "chance.png",   "chance.png",   NodeType.CHANCE),
            new NodeModeDescriptor("Edit.Mode.Decision", "decision.png", "decision.png", NodeType.DECISION),
            new NodeModeDescriptor("Edit.Mode.Utility",  "utility.png",  "utility.png",  NodeType.UTILITY),
            new NodeModeDescriptor("Edit.Mode.Event",  "event.png",  "event.png",  NodeType.EVENT)
    );

    private final Map<String, EditionState> editionStates;
    private final Map<String, Class<? extends EditionMode>> editionModeClasses;
    private final Map<String, NodeModeDescriptor> nodeModeDescriptors;
    private final NetworkEditorPanel networkEditorPanel;
    private final ProbNet probNet;

    public EditionModeManager(NetworkEditorPanel networkEditorPanel, ProbNet probNet) {
        editionStates = new LinkedHashMap<>();
        editionModeClasses = new HashMap<>();
        nodeModeDescriptors = new HashMap<>();
        this.networkEditorPanel = networkEditorPanel;
        this.probNet = probNet;

        // 1. Discover annotation-based modes (Selection, Link, etc.)
        EditionModeManager.findAllEditionStates().forEach(editionModeClass -> {
            EditionState editionState = editionModeClass.getAnnotation(EditionState.class);
            this.editionStates.put(editionState.name(), editionState);
            this.editionModeClasses.put(editionState.name(), editionModeClass);
        });

        // 2. Register node-creation modes programmatically
        for (NodeModeDescriptor desc : NODE_MODE_DESCRIPTORS) {
            nodeModeDescriptors.put(desc.name(), desc);
            // Also create a synthetic EditionState so getEditionStates() and
            // getCursor() keep working without changes in the toolbar code.
            editionStates.put(desc.name(), syntheticEditionState(desc));
        }
    }

    public EditionMode getEditionMode(String editionMode) {
        // Node-creation modes: instantiate directly
        NodeModeDescriptor desc = nodeModeDescriptors.get(editionMode);
        if (desc != null) {
            return new NodeEditionMode(networkEditorPanel, probNet, desc.nodeType());
        }
        // Annotation-discovered modes: instantiate via reflection
        if (editionModeClasses.containsKey(editionMode)) {
            try {
                Constructor<?> constructor = editionModeClasses
                        .get(editionMode)
                        .getConstructor(NetworkEditorPanel.class, ProbNet.class);
                return (EditionMode) constructor.newInstance(networkEditorPanel, probNet);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException |
                     IllegalArgumentException | InvocationTargetException e) {
                throw new UnreachableException(e);
            }
        }
        return null;
    }

    public Collection<EditionState> getEditionStates() {
        return editionStates.values();
    }

    /**
     * This method gets all the plugins with EditionState annotations
     *
     * @return a list with the plugins detected with FormatType annotations.
     */
    private static @NotNull Stream<Class<? extends EditionMode>> findAllEditionStates() {
        return PluginSearch.init()
                           .annotatedWith(EditionState.class)
                           .childrenOf(EditionMode.class)
                           .stream();
    }

    public Cursor getCursor(String newEditionModeName) {
        return CursorLoader.load(editionStates.get(newEditionModeName).cursor());
    }

    public Cursor getDefaultCursor() {
        return CursorLoader.load(editionStates.get("Edit.Mode.Selection").cursor());
    }

    public EditionMode getDefaultEditionMode() {
        return getEditionMode("Edit.Mode.Selection");
    }

    /**
     * Creates a synthetic {@link EditionState} annotation instance so that
     * existing toolbar/menu code that iterates over {@link #getEditionStates()}
     * keeps working unchanged.
     */
    @SuppressWarnings("all")
    private static EditionState syntheticEditionState(NodeModeDescriptor desc) {
        return new EditionState() {
            @Override public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return EditionState.class;
            }
            @Override public String name()   { return desc.name();   }
            @Override public String icon()   { return desc.icon();   }
            @Override public String cursor() { return desc.cursor(); }
        };
    }
}
