package org.openmarkov.gui.window;

import org.openmarkov.java.staticAnalysis.SubstitutesClass;

import javax.swing.JTabbedPane;

@SubstitutesClass(value = JTabbedPane.class, includesInheritance = true)
public class OMTabbedPane extends JTabbedPane {
    
    public OMTabbedPane() {
        super();
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    
}