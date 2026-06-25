package org.openmarkov.java.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseListenerUtils {
    
    public static MouseAdapter mouseListenerToMouseAdapter(MouseListener sourceMouseListener) {
        return new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e) {
                sourceMouseListener.mouseClicked(e);
            }
            
            public void mousePressed(MouseEvent e) {
                sourceMouseListener.mousePressed(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                sourceMouseListener.mouseReleased(e);
            }
            
            public void mouseEntered(MouseEvent e) {
                sourceMouseListener.mouseEntered(e);
            }
            
            public void mouseExited(MouseEvent e) {
                sourceMouseListener.mouseExited(e);
            }
        };
    }
    
}
