package org.openmarkov.gui.layout.radial;

import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.JPanel;
import java.awt.*;

public class RadialPanel extends JPanel {
    
    private final RadialLayout radialLayout;
    
    public RadialPanel(RadialLayout layout) {
        super(layout);
        this.radialLayout = layout;
    }
    
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = radialLayout.getCurrentRadius();
        if (radius <= 0) {
            graphics2D.dispose();
            return;
        }
        
        graphics2D.setColor(GUIColors.FastMenu.Radial.CIRCLE_BACKGROUND.getColor());
        graphics2D.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        graphics2D.setColor(GUIColors.FastMenu.Radial.CIRCLE_OUTLINE.getColor());
        graphics2D.setStroke(new BasicStroke(
                1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{6, 4}, 0
        ));
        graphics2D.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        graphics2D.setColor(GUIColors.FastMenu.Radial.CIRCLE_CENTER.getColor());
        graphics2D.fillOval(centerX - 4, centerY - 4, 8, 8);
        
        graphics2D.dispose();
    }
}