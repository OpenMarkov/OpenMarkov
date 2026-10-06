package org.openmarkov.gui.bindings;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.java.initialization.Lazy;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BindingCommandReactor {
    
    private final List<BindingCommandGroup<?, ?>> bindingCommandGroups;
    
    public BindingCommandReactor(BindingCommandGroup<?, ?>... commands) {
        this.bindingCommandGroups = Collections.unmodifiableList(Arrays.asList(commands));
    }
    
    public void process(@NotNull InputEvent inputEvent) {
        var event = BindingCommandGroup.OnEvent.of(inputEvent);
        if (event == null) {
            return;
        }
        OpenMarkovLogger.LOGGER.debug("Processing event " + event);
        for (var command : this.bindingCommandGroups) {
            if (!command.eventsToReact.contains(event)) {
                continue;
            }
            boolean isInputInstance = command.inputKind.isInstance(inputEvent);
            if (isInputInstance && command.isTriggered() && command.allowsProcessing(inputEvent)) {
                OpenMarkovLogger.LOGGER.debug("Triggering command " + command.bindings.name());
                command.triggerAction(inputEvent);
                if (command.isBlocker) {
                    OpenMarkovLogger.LOGGER.debug("Command " + command.bindings.name() + " has cancelled the follow-up execution of other commands");
                    break;
                }
            }
        }
        OpenMarkovLogger.LOGGER.debug("Finished processing event ");
    }
    
    public final Lazy<MouseAdapter> mouseAdapter = new Lazy<>(() -> new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mousePressed(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mouseReleased(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mouseEntered(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mouseExited(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mouseWheelMoved(MouseWheelEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mouseDragged(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void mouseMoved(MouseEvent e) {
            BindingCommandReactor.this.process(e);
        }
    });
    public final Lazy<KeyListener> keyListener = new Lazy<>(() -> new KeyListener() {
        
        @Override public void keyTyped(KeyEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void keyPressed(KeyEvent e) {
            BindingCommandReactor.this.process(e);
        }
        
        @Override public void keyReleased(KeyEvent e) {
            BindingCommandReactor.this.process(e);
        }
    });
    
    public void addListenersTo(Component component) {
        component.removeKeyListener(this.keyListener.get());
        component.removeMouseListener(this.mouseAdapter.get());
        component.removeMouseMotionListener(this.mouseAdapter.get());
        component.removeMouseWheelListener(this.mouseAdapter.get());
        
        component.addKeyListener(this.keyListener.get());
        component.addMouseListener(this.mouseAdapter.get());
        component.addMouseMotionListener(this.mouseAdapter.get());
        component.addMouseWheelListener(this.mouseAdapter.get());
    }
    
}
