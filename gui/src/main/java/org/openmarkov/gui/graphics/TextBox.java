package org.openmarkov.gui.graphics;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TextBox implements Paintable {
    
    private List<String> lines;
    private Align align;
    
    public enum Align {
        LEFT, CENTER, RIGHT,
    }
    
    public TextBox(String content) {
        this.withContent(content).withAlign(Align.LEFT);
    }
    
    public TextBox withAlign(Align align) {
        this.align = align;
        return this;
    }
    
    public TextBox withContent(String content) {
        this.lines = TextBox.extractLines(content).toList();
        return this;
    }
    
    public Rectangle paint(Graphics g, int x, int y) {
        g=g.create();
        g.setColor(Color.BLACK);
        if (this.lines.isEmpty()) {
            return new Rectangle(x,y, 0, 0);
        }
        var fontMetrics = g.getFontMetrics();
        var linesWidth = this.lines.stream().mapToInt(fontMetrics::stringWidth).max().orElse(0);
        for (int i = 0; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            int lineY = y + fontMetrics.getHeight() * i + fontMetrics.getLeading() + fontMetrics.getAscent();
            int lineX = x + switch (this.align) {
                case LEFT -> 0;
                case CENTER -> (linesWidth - fontMetrics.stringWidth(line)) / 2;
                case RIGHT -> linesWidth - fontMetrics.stringWidth(line);
            };
            g.drawString(line, lineX, lineY);
        }
        var dimensions = this.dimensions(g);
        g.dispose();
        return new Rectangle(x,y, dimensions.width, dimensions.height);
    }
    
    public Dimension dimensions(Graphics g) {
        if (this.lines.isEmpty()) {
            return new Dimension(0, 0);
        }
        var fontMetrics = g.getFontMetrics();
        var linesWidth = this.lines.stream().mapToInt(fontMetrics::stringWidth).max().orElse(0);
        int allLinesHeight = fontMetrics.getHeight() * this.lines.size();
        return new Dimension(linesWidth, allLinesHeight);
    }
    
    private static final List<Pattern> SPLITTERS = List.of(Pattern.compile(System.lineSeparator()), Pattern.compile("\n"), Pattern.compile("<br>"));
    
    private static @NotNull Stream<String> extractLines(String content) {
        var lines = Stream.of(content);
        for (var splitter : TextBox.SPLITTERS) {
            lines = lines.flatMap(splitter::splitAsStream);
        }
        return lines;
    }
    
}
