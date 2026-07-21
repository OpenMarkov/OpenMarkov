package org.openmarkov.gui.graphics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PopupTextbox implements Paintable {
    
    private @NotNull List<String> lines;
    private @NotNull Align align;
    private @Nullable PopupTextbox button;
    
    private @NotNull Color foreground;
    private @NotNull Color background;
    private @NotNull Color border;
    
    private int horizontalMargin;
    private int verticalMargin;
    private static final int VERTICAL_GAP_FOR_BUTTON = 1;
    
    public enum Align {
        LEFT, CENTER, RIGHT,
    }
    
    public PopupTextbox(String content) {
        this.withContent(content)
            .withAlign(Align.LEFT)
            .withMargins(3, 3)
            .withColors(Color.BLACK, Color.WHITE, Color.BLACK);
    }
    
    public PopupTextbox withAlign(Align align) {
        this.align = align;
        return this;
    }
    
    public PopupTextbox withContent(String content) {
        this.lines = PopupTextbox.extractLines(content).toList();
        return this;
    }
    
    public PopupTextbox withMargins(int horizontalMargin, int verticalMargin) {
        this.horizontalMargin = horizontalMargin;
        this.verticalMargin = verticalMargin;
        return this;
    }
    
    public PopupTextbox withButton(PopupTextbox button) {
        this.button = button;
        return this;
    }
    
    public PopupTextbox withColors(Color foreground, Color background, Color border) {
        this.foreground = foreground;
        this.background = background;
        this.border = border;
        return this;
    }
    
    
    public Rectangle paint(Graphics g, int x, int y) {
        g = g.create();
        g.setColor(this.background);
        g.fillRoundRect(x, y, this.dimensions(g).width, this.dimensions(g).height, 10, 10);
        g.setColor(this.border);
        g.drawRoundRect(x, y, this.dimensions(g).width, this.dimensions(g).height, 10, 10);
        g.setColor(this.foreground);
        if (this.lines.isEmpty()) {
            return new Rectangle(x, y, 0, 0);
        }
        var fontMetrics = g.getFontMetrics();
        var linesWidth = this.lines.stream().mapToInt(fontMetrics::stringWidth).max().orElse(0);
        for (int i = 0; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            int lineY = y + this.verticalMargin + fontMetrics.getHeight() * i + fontMetrics.getLeading() + fontMetrics.getAscent();
            int lineX = x + this.horizontalMargin + switch (this.align) {
                case LEFT -> 0;
                case CENTER -> (linesWidth - fontMetrics.stringWidth(line)) / 2;
                case RIGHT -> linesWidth - fontMetrics.stringWidth(line);
            };
            g.drawString(line, lineX, lineY);
        }
        var dimensions = this.dimensions(g);
        if (this.button != null) {
            this.button.paint(g,
                              x + dimensions.width - this.horizontalMargin - this.button.dimensions(g).width,
                              y + this.verticalMargin + fontMetrics.getHeight() * lines.size()
            );
        }
        g.dispose();
        return new Rectangle(x, y, dimensions.width, dimensions.height);
    }
    
    public @Nullable Rectangle buttonRect(Graphics g, int x, int y) {
        if(this.button == null) {
            return null;
        }
        var dimensions = this.dimensions(g);
        var fontMetrics = g.getFontMetrics();
        
        return new Rectangle(new Point(x + dimensions.width - this.horizontalMargin - this.button.dimensions(g).width,
                                       y + this.verticalMargin + fontMetrics.getHeight() * lines.size()),
                             this.button.dimensions(g));
        
    }
    
    public Dimension dimensions(Graphics g) {
        if (this.lines.isEmpty()) {
            return new Dimension(0, 0);
        }
        var fontMetrics = g.getFontMetrics();
        var linesWidth = this.lines.stream().mapToInt(fontMetrics::stringWidth).max().orElse(0);
        int allLinesHeight = fontMetrics.getHeight() * this.lines.size();
        
        
        int width = linesWidth;
        int height = allLinesHeight;
        if (this.button != null) {
            var buttonDimensions = this.button.dimensions(g);
            width = Math.max(width, buttonDimensions.width);
            height += buttonDimensions.height + (PopupTextbox.VERTICAL_GAP_FOR_BUTTON * 2);
        }
        width += (this.horizontalMargin * 2);
        height += (this.verticalMargin * 2);
        
        return new Dimension(width, height);
    }
    
    
    private static final List<Pattern> SPLITTERS = List.of(Pattern.compile(System.lineSeparator()), Pattern.compile("\n"), Pattern.compile("<br>"));
    
    private static @NotNull Stream<String> extractLines(String content) {
        var lines = Stream.of(content);
        for (var splitter : PopupTextbox.SPLITTERS) {
            lines = lines.flatMap(splitter::splitAsStream);
        }
        return lines;
    }
    
}
