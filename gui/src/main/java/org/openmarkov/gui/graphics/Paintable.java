package org.openmarkov.gui.graphics;

import java.awt.*;

public interface Paintable {
    
    Rectangle paint(Graphics g, int x, int y);
    
    Dimension dimensions(Graphics g);
    
}
