package org.openmarkov.gui.window;

import javax.swing.*;
import java.awt.*;

public class AutoScrollableTabbedPane {
    
    private final JTabbedPane jTabbedPane;
    
    public AutoScrollableTabbedPane(JTabbedPane jTabbedPane) {
        this.jTabbedPane = jTabbedPane;
    }
    
    public JTabbedPane getjTabbedPane() {
        return this.jTabbedPane;
    }
    
    public Dimension getSize() {
        return this.jTabbedPane.getSize();
    }
    
    public Component getSelectedComponent() {
        return AutoScrollableTabbedPane.unwrapIfNeccesary(jTabbedPane.getSelectedComponent());
    }
    
    public void addTab(String uniqueTitle, Component component) {
        this.jTabbedPane.addTab(uniqueTitle, AutoScrollableTabbedPane.wrapIfNeccesary(component));
    }
    
    public int getTabCount() {
        return this.jTabbedPane.getTabCount();
    }
    
    public void setTabComponentAt(int i, Component component) {
        this.jTabbedPane.setTabComponentAt(i, component);
    }
    
    public Component getTabComponentAt(int i) {
        return this.jTabbedPane.getTabComponentAt(i);
    }
    
    public int indexOfTabComponent(Component tabComponent) {
        return this.jTabbedPane.indexOfTabComponent(tabComponent);
    }
    
    public void setSelectedIndex(int tabIndex) {
        this.jTabbedPane.setSelectedIndex(tabIndex);
    }
    
    public int getSelectedIndex() {
        return this.jTabbedPane.getSelectedIndex();
    }
    
    public void setSelectedComponent(Component component) {
        this.jTabbedPane.setSelectedIndex(this.indexOfComponent(component));
    }
    
    public int indexOfComponent(Component component) {
        for (int tabIndex = 0; tabIndex < this.jTabbedPane.getTabCount(); tabIndex++) {
            Component componentAt = this.jTabbedPane.getComponentAt(tabIndex);
            if(componentAt==component || AutoScrollableTabbedPane.unwrapIfNeccesary(componentAt) == component) {
                return tabIndex;
            }
        }
        return -1;
    }
    
    public Component getComponentAt(int tabIndex) {
        return AutoScrollableTabbedPane.unwrapIfNeccesary(this.jTabbedPane.getComponentAt(tabIndex));
    }
    
    public void remove(Component component) {
        int index = this.indexOfComponent(component);
        if(index==-1){
            return;
        }
        this.jTabbedPane.remove(this.jTabbedPane.getComponentAt(index));
    }
    
    public static Component unwrapIfNeccesary(Component component) {
        if (component instanceof JScrollPane scrollPane) {
            return scrollPane.getViewport().getView();
        }
        return component;
    }
    
    public static Component wrapIfNeccesary(Component component) {
        if (component instanceof JScrollPane scrollPane) {
            return scrollPane;
        }
        return new JScrollPane(component);
    }

}