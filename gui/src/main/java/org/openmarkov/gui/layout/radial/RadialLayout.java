package org.openmarkov.gui.layout.radial;

import java.awt.*;

public class RadialLayout implements LayoutManager {
    
    private final double startAngleDeg;
    private final int gap;
    private int currentRadius;
    
    /**
     * @param startAngleDeg 0 = 3 o'clock, 270 = 12 o'clock
     * @param gap           minimum clear space in pixels between adjacent components
     */
    public RadialLayout(double startAngleDeg, int gap) {
        this.startAngleDeg = startAngleDeg;
        this.gap = gap;
    }
    
    int getCurrentRadius() {
        return currentRadius;
    }
    
    // ── LayoutManager contract ───────────────────────────────────────────────
    
    @Override public void addLayoutComponent(String name, Component component) {
    }
    
    @Override public void removeLayoutComponent(Component component) {
    }
    
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        int componentCount = parent.getComponentCount();
        Dimension maxCompSize = maxComponentSize(parent);
        Insets insets = parent.getInsets();
        int radius = computeRadius(300, 300, maxCompSize, componentCount);
        return new Dimension(
                radius * 2 + maxCompSize.width + insets.left + insets.right,
                radius * 2 + maxCompSize.height + insets.top + insets.bottom
        );
    }
    
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension maxCompSize = maxComponentSize(parent);
        Insets insets = parent.getInsets();
        return new Dimension(
                maxCompSize.width * 2 + insets.left + insets.right,
                maxCompSize.height * 2 + insets.top + insets.bottom
        );
    }
    
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            int componentCount = parent.getComponentCount();
            if (componentCount == 0) return;
            
            Insets insets = parent.getInsets();
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int availableHeight = parent.getHeight() - insets.top - insets.bottom;
            int centerX = insets.left + availableWidth / 2;
            int centerY = insets.top + availableHeight / 2;
            Dimension maxCompSize = maxComponentSize(parent);
            
            currentRadius = computeRadius(availableWidth, availableHeight, maxCompSize, componentCount);
            
            double angleStepDeg = 360.0 / componentCount;
            for (int index = 0; index < componentCount; index++) {
                Component component = parent.getComponent(index);
                Dimension compSize = component.getPreferredSize();
                double angleRad = Math.toRadians(startAngleDeg + index * angleStepDeg);
                int componentX = centerX + (int) Math.round(currentRadius * Math.cos(angleRad)) - compSize.width / 2;
                int componentY = centerY + (int) Math.round(currentRadius * Math.sin(angleRad)) - compSize.height / 2;
                component.setBounds(componentX, componentY, compSize.width, compSize.height);
            }
        }
    }
    
    /**
     * Computes the radius that satisfies both constraints:
     * <p>
     * 1. Fit constraint — components must not clip the panel edge:
     * radius ≤ min(availableWidth, availableHeight) / 2 − halfComponentDiagonal
     * <p>
     * 2. No-overlap constraint — adjacent components must have at least `gap` pixels
     * of clear space between them. Adjacent component centres are separated by a
     * chord of length 2·radius·sin(π/n). That chord must be ≥ componentDiagonal + gap:
     * radius ≥ (componentDiagonal + gap) / (2·sin(π/n))
     * <p>
     * We take the maximum of the two lower bounds and cap at the upper bound.
     * If the panel is too small to satisfy both, the fit constraint wins so that
     * components at least stay on-screen.
     */
    private int computeRadius(int availableWidth, int availableHeight,
                              Dimension maxCompSize, int componentCount) {
        double componentDiagonal = Math.sqrt(
                maxCompSize.width * maxCompSize.width +
                        maxCompSize.height * maxCompSize.height
        );
        int halfComponentDiagonal = (int) Math.ceil(componentDiagonal / 2.0);
        int maxRadiusToFit = Math.max(0, Math.min(availableWidth, availableHeight) / 2 - halfComponentDiagonal);
        
        
        double minRadiusToNotOverlap = (componentDiagonal + gap) / (2.0 * Math.sin(Math.PI / Math.max(2, componentCount)));
        
        return (int) Math.min(maxRadiusToFit, Math.max(minRadiusToNotOverlap, halfComponentDiagonal));
    }
    
    private Dimension maxComponentSize(Container parent) {
        int maxWidth = 0, maxHeight = 0;
        for (int index = 0; index < parent.getComponentCount(); index++) {
            Dimension size = parent.getComponent(index).getPreferredSize();
            maxWidth = Math.max(maxWidth, size.width);
            maxHeight = Math.max(maxHeight, size.height);
        }
        return new Dimension(maxWidth, maxHeight);
    }
}