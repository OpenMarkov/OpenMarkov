package org.openmarkov.java.swing;

import org.openmarkov.gui.graphic.VisualNode;

import java.awt.Point;
import java.util.Arrays;
import java.util.stream.Stream;

public class PointUtils {
    
    public static Point sumPoints(Point point, Point... points) {
        return new Point(
                (int) Stream.concat(Stream.of(points), Arrays.stream(points)).mapToDouble(Point::getX).sum(),
                (int) Stream.concat(Stream.of(points), Arrays.stream(points)).mapToDouble(Point::getY).sum()
        );
    }
    
}
