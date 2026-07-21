package org.openmarkov.gui.graphics;

import org.openmarkov.gui.configuration.GUIColors;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Stream;

public class BoxedElement<Element extends Paintable> implements Paintable {
    
    public enum BorderSide {
        LEFT, RIGHT, TOP, BOTTOM
    }
    
    public static class Border {
        public byte size;
        public byte spacing;
        public Color color;
        
        public Border() {
            this.size = 2;
            this.spacing = 2;
            this.color = GUIColors.Graphics.DEFAULT_BOX_BORDER_COLOR.getColor();
        }
        
        public Border withSize(byte size) {
            this.size = size;
            return this;
        }
        
        public Border withSpacing(byte spacing) {
            this.spacing = spacing;
            return this;
        }
        
        public Border withColor(Color color) {
            this.color = color;
            return this;
        }
    }
    
    private final Element element;
    private EnumMap<BorderSide, Border> borders;
    
    public BoxedElement(Element element) {
        this.element = element;
        this.borders = new EnumMap<>(BorderSide.class);
        this.withBorder(new Border(), BorderSide.values());
    }
    
    public static <Element extends Paintable> BoxedElement<Element> of(Element element){
        return new BoxedElement<Element>(element);
    }
    
    
    public BoxedElement withBorder(Border border, BorderSide... borderSides) {
        Arrays.stream(borderSides.length == 0 ? BorderSide.values() : borderSides)
              .forEach(borderSide -> this.borders.put(borderSide, border));
        return this;
    }
    
    @Override public Rectangle paint(Graphics g, int x, int y) {
        var dimensions = this.dimensions(g);
        var defColor = g.getColor();
        for (var side : BorderSide.values()){
            var border = this.borders.get(side);
            g.setColor(border.color);
            switch (side){
                case TOP -> {
                    for (int i = 0; i < border.size; i++) {
                        g.drawLine(x, y+i, x+dimensions.width, y+i);
                    }
                }
                case BOTTOM -> {
                    for (int i = 0; i < border.size; i++) {
                        g.drawLine(x, y+(dimensions.height)-i, x+dimensions.width, y+(dimensions.height)-i);
                    }
                }
                case LEFT -> {
                    for (int i = 0; i < border.size; i++) {
                        g.drawLine(x+i, y, x+i, y+dimensions.height);
                    }
                }
                case RIGHT -> {
                    for (int i = 0; i < border.size; i++) {
                        g.drawLine(x+(dimensions.width)-i, y, x+(dimensions.width)-i, y+dimensions.height);
                    }
                }
            }
        }
        g.setColor(defColor);
        
        this.element.paint(g,
                           x+this.borders.get(BorderSide.LEFT).size+this.borders.get(BorderSide.LEFT).spacing,
                           y+this.borders.get(BorderSide.TOP).size+this.borders.get(BorderSide.TOP).spacing);
        
        return new Rectangle(x,y, dimensions.width, dimensions.height);
    }
    
    @Override public Dimension dimensions(Graphics g) {
        var elementDimensions = this.element.dimensions(g);
        var boxBorderHeights = Stream
                .of(BorderSide.TOP, BorderSide.BOTTOM)
                .map(this.borders::get)
                .filter(border -> border.size > 0)
                .mapToInt(border -> border.size + border.spacing)
                .sum();
        var boxBorderWidths = Stream
                .of(BorderSide.LEFT, BorderSide.RIGHT)
                .map(this.borders::get)
                .filter(border -> border.size > 0)
                .mapToInt(border -> border.size + border.spacing)
                .sum();
        return new Dimension(boxBorderWidths + elementDimensions.width, boxBorderHeights + elementDimensions.height);
    }
    
}
