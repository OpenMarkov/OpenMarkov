package org.openmarkov.java.swing;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;

public class GraphicsUtilities {
    public static boolean shapesIntersect(Shape r1, Shape r2) {
        // Calculating intersection between shapes is quite costy, but if we consider them as rectangles, it is very
        // cheap, and in 2D spaces if both Shapes bounds (rectangles) don't intersect, then it is impossible for the
        // real shapes to intersect, so this optimization allows to prevent calculations.
        if (!r1.getBounds2D().intersects(r2.getBounds2D())) {
            return false;
        }
        Area unionOfShapes = new Area(r1);
        unionOfShapes.intersect(new Area(r2));
        return !unionOfShapes.isEmpty();
    }
    
    public static Shape inflateShape(Shape originalShape, double padding) {
        float strokeWidth = (float) (padding * 2);
        BasicStroke thickStroke = new BasicStroke(
                strokeWidth,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        );
        Shape outline = thickStroke.createStrokedShape(originalShape);
        Area inflatedArea = new Area(originalShape);
        inflatedArea.add(new Area(outline));
        return inflatedArea;
    }
}
