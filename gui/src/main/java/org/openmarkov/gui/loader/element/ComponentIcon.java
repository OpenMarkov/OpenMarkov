package org.openmarkov.gui.loader.element;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/// This creates an icon out of a {@link Component}.
public final class ComponentIcon implements Icon {
    private final BufferedImage image;
    private final int width;
    private final int height;

    public ComponentIcon(Component component) {
        // This forces components that haven't been rendered to render. This is useful when creating Components that you
        // want turn into an Icon, but the component themselves are never rendered in the UI.
        if (component.getWidth() <= 0 || component.getHeight() <= 0) {
            Dimension preferredSize = component.getPreferredSize();
            component.setSize(preferredSize);
            component.doLayout();
        }
        this.width = component.getWidth();
        this.height = component.getHeight();
        this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        component.paint(g2d);
        g2d.dispose();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(this.image, x, y, null);
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }
}