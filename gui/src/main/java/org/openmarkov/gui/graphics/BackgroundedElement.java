package org.openmarkov.gui.graphics;

import java.awt.*;

public class BackgroundedElement<Element extends Paintable> implements Paintable {
    
    private final Element element;
    private final Color color;
    
    public BackgroundedElement(Element element, Color color) {
        this.element = element;
        this.color=color;
    }
    
    @Override public Rectangle paint(Graphics g, int x, int y) {
        var defColor = g.getColor();
        g.setColor(this.color);
        g.fillRect(x,y, this.dimensions(g).width, this.dimensions(g).height);
        g.setColor(defColor);
        return this.element.paint(g, x, y);
    }
    
    @Override public Dimension dimensions(Graphics g) {
        return this.element.dimensions(g);
    }
    
}
