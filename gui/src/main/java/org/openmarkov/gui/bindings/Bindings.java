package org.openmarkov.gui.bindings;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openmarkov.gui.bindings.Bindings.General.EXIT;
import static org.openmarkov.gui.bindings.Bindings.General.SHOW_HELP;
import static org.openmarkov.gui.bindings.Bindings.Network.CREATE_LINK_SHORTCUT;
import static org.openmarkov.gui.bindings.Bindings.Network.DEFINE_CHANCE_NODE;

public class Bindings {
    
    public static final List<Binding> ALL_BINDINGS;
    
    static {
        var classes = new ArrayList<Class<?>>();
        ArrayDeque<Class<?>> queue = new ArrayDeque<>();
        queue.add(Bindings.class);
        while (!queue.isEmpty()) {
            var classToVisit = queue.removeFirst();
            classes.add(classToVisit);
            queue.addAll(List.of(classToVisit.getDeclaredClasses()).reversed());
        }
        ALL_BINDINGS = classes.stream().flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                              .map(field -> {
                                  try {
                                      return field.get(null);
                                  } catch (IllegalAccessException e) {
                                      return null;
                                  }
                              })
                              .filter(Objects::nonNull)
                              .filter(Binding.class::isInstance)
                              .map(Binding.class::cast)
                              .toList();
    }
    
    public static final List<Set<Binding>> INCOMPATIBLE_GROUPS;
    
    static {
        var incompatibleGroups = new ArrayList<Set<Binding>>();
        incompatibleGroups.add(Set.of(CREATE_LINK_SHORTCUT, DEFINE_CHANCE_NODE));
        Stream.of(SHOW_HELP, EXIT)
              .forEach(binding -> Bindings.ALL_BINDINGS.stream()
                                                       .filter(oBinding -> binding != oBinding)
                                                       .forEach(oBinding -> incompatibleGroups.add(Set.of(binding, oBinding))));
        
        //noinspection SimplifyStreamApiCallChains
        INCOMPATIBLE_GROUPS = incompatibleGroups.stream().map(set -> set.stream().collect(Collectors.toSet())).toList();
    }
    
    static {
        Binding.checkValidity(null);
    }
    
    public class General {
        
        public static final Binding SHOW_HELP =
                new Binding(null,
                            "Show help",
                            "Opens a dialog to offer help in the current context.",
                            "General",
                            null,
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(new Input.Key(KeyEvent.VK_F1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding EXIT =
                new Binding(null,
                            "Exit",
                            """
                                    Closes the current context, such as closing the current dialog.
                                    
                                    This is not used to close the application.
                                    """,
                            "General",
                            null,
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(new Input.Key(KeyEvent.VK_ESCAPE)),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding OPEN_CONTEXTUAL_MENU =
                new Binding(null,
                            "Open contextual menu",
                            null,
                            "General",
                            null,
                            Binding.KeyAcceptance.FORBIDDEN,
                            Binding.MouseAcceptance.REQUIRED_WITH_SINGLE_CLICK,
                            new InputCombination(new Input.Click(MouseEvent.BUTTON3, 1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
    }
    
    public class Network {
        public static final Binding CREATE_LINK_SHORTCUT =
                new Binding("network/create_link_shortcut",
                            "Create link (Shortcut)",
                            "Holding this combination allows you to enter 'Link creation mode' without exiting the 'Selection mode'.",
                            "Network editor",
                            "Selection mode",
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(
                                    new Input.Key(KeyEvent.VK_CONTROL),
                                    new Input.Key(KeyEvent.VK_SHIFT)
                            ),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding SELECTION_ELEMENT =
                new Binding("network/general/selection_element",
                            "Select element",
                            "Marks the element where your cursor is as the single selected element.",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_SINGLE_CLICK,
                            new InputCombination(new Input.Click(MouseEvent.BUTTON1, 1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final BindingCommandGroup.BindingGroup ADD_ELEMENT_TO_SELECTION =
                new Binding("network/general/add_to_selection",
                            "Add to selection",
                            """
                                    Adds the element to the selection. If it was already present, it removes it.
                                    """,
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_SINGLE_CLICK,
                            new InputCombination(new Input.Key(KeyEvent.VK_CONTROL), new Input.Click(MouseEvent.BUTTON1, 1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding SELECTION_RECTANGLE =
                new Binding("network/general/selection_rectangle",
                            "Selection rectangle",
                            """
                                    Creates a rectangle from where your started to hold your cursor up to where you released.
                                    
                                    Once released, all elements within the rectangle will be selected.
                                    """,
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(new Input.Click(MouseEvent.BUTTON1, 1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding MOVE_NODE_MOUSE =
                new Binding("network/general/move_node",
                            "Move node",
                            "Moves the node(s) along the position your mouse moves to.",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_SINGLE_CLICK,
                            new InputCombination(new Input.Click(MouseEvent.BUTTON1, 1)),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        
        public static final Binding MOVE_NODE_KEY_UP =
                new Binding("network/general/move_node_up",
                            "Move node up",
                            "Moves the selected node(s) up as long as you hold this key.",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(new Input.Key(KeyEvent.VK_UP)),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding MOVE_NODE_KEY_RIGHT =
                new Binding("network/general/move_node_right",
                            "Move node right",
                            "Moves the selected node(s) right as long as you hold this key.",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(new Input.Key(KeyEvent.VK_RIGHT)),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding MOVE_NODE_KEY_DOWN =
                new Binding("network/general/move_node_down",
                            "Move node down",
                            "Moves the selected node(s) down as long as you hold this key.",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(new Input.Key(KeyEvent.VK_DOWN)),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding MOVE_NODE_KEY_LEFT =
                new Binding("network/general/move_node_left",
                            "Move node left",
                            "Moves the selected node(s) left as long as you hold this key.",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(new Input.Key(KeyEvent.VK_LEFT)),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding EDIT_POTENTIAL =
                new Binding("network/general/edit_probability",
                            "Edit relation",
                            "Shows a dialog to edit the relation of the node (probability, utility...).",
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(new Input.Key(KeyEvent.VK_ALT), new Input.Click(MouseEvent.BUTTON1, 1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding CHANGE_PROPERTIES =
                new Binding("network/general/change_properties",
                            "Change properties",
                            """
                                    Opens a dialog to change the properties of a selected element (Node or Link).
                                    
                                    If no element is selected, it will show the properties of the network.
                                    """,
                            "Network editor",
                            "General",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(new Input.Click(MouseEvent.BUTTON1, 2)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding SELECT_STATE_EVIDENCE =
                new Binding("network/inference/change_state",
                            "Select state",
                            """
                                    Sets the evidence of a node to the selected state.
                                    
                                    This cannot be applied if pre-resolution evidence is present.
                                    """,
                            "Network editor",
                            "Inference",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(new Input.Click(MouseEvent.BUTTON1, 1)),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        
        public static final Binding CREATE_NEW_NODE_IN_NODE_CREATION =
                new Binding("network/node_creation_mode/create_node",
                            "Create node",
                            "Creates a node on the place where your cursor is while in Node Creation mode.",
                            "Network editor",
                            "Node creation mode",
                            Binding.KeyAcceptance.FORBIDDEN,
                            Binding.MouseAcceptance.ALLOWED_WITH_SINGLE_CLICK,
                            new InputCombination(
                                    new Input.Click(MouseEvent.BUTTON1, 1)
                            ),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding CREATE_NEW_LINK_IN_LINK_CREATION =
                new Binding("network/link_creation_mode/create_link",
                            "Create link",
                            "Creates a link from the select node(s) to the node where the cursor is while in Link Creation mode (or while using the Link Creation shortcut).",
                            "Network editor",
                            "Link creation mode",
                            Binding.KeyAcceptance.FORBIDDEN,
                            Binding.MouseAcceptance.ALLOWED_WITH_SINGLE_CLICK,
                            new InputCombination(
                                    new Input.Click(MouseEvent.BUTTON1, 1)
                            ),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding TOGGLE_DIRECTION_IN_LINK_CREATION =
                new Binding("network/link_creation_mode/toggle_direction",
                            "Toggle link direction",
                            "Changes the direction of the link(s) while creating it.",
                            "Network editor",
                            "Link creation mode",
                            Binding.KeyAcceptance.REQUIRES_AT_LEAST_ONE,
                            Binding.MouseAcceptance.FORBIDDEN,
                            new InputCombination(
                                    new Input.Key(KeyEvent.VK_ALT)
                            ),
                            Binding.Exclusivity.INCLUSIVE
                );
        
        public static final Binding DEFINE_CHANCE_NODE =
                new Binding("network/shortcut/define_chance_node",
                            "Define chance node",
                            """
                                    Creates a chance node where your cursor is and open its properties.
                                    
                                    Canceling the properties will also delete the created node.
                                    """,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(
                                    new Input.Click(MouseEvent.BUTTON1, 2)
                            ),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding DEFINE_DECISION_NODE =
                new Binding("network/shortcut/define_decision_node",
                            "Define decision node",
                            """
                                    Creates a decision node where your cursor is and open its properties.
                                    
                                    Canceling the properties will also delete the created node.
                                    """,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding DEFINE_UTILITY_NODE =
                new Binding("network/shortcut/define_utility_node",
                            "Define utility node",
                            """
                                    Creates a utility node where your cursor is and open its properties.
                                    
                                    Canceling the properties will also delete the created node.
                                    """,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding DEFINE_EVENT_NODE =
                new Binding("network/shortcut/define_event_node",
                            "Define event node",
                            """
                                    Creates a event node where your cursor is and open its properties.
                                    
                                    Canceling the properties will also delete the created node.
                                    """,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        
        public static final Binding CREATE_CHANCE_NODE =
                new Binding("network/shortcut/create_chance_node",
                            "Create chance node",
                            null,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding CREATE_DECISION_NODE =
                new Binding("network/shortcut/create_decision_node",
                            "Create decision node",
                            null,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding CREATE_UTILITY_NODE =
                new Binding("network/shortcut/create_utility_node",
                            "Create utility node",
                            null,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
        
        public static final Binding CREATE_EVENT_NODE =
                new Binding("network/shortcut/create_event_node",
                            "Create event node",
                            null,
                            "Network editor",
                            "Shortcuts",
                            Binding.KeyAcceptance.ALLOWED,
                            Binding.MouseAcceptance.REQUIRED_WITH_JUST_ONE_MOUSE,
                            new InputCombination(),
                            Binding.Exclusivity.EXCLUSIVE
                );
    }
    
}
