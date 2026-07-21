/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.element;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple3Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.configuration.LocalPreferences;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;

/**
 * This class is used to load icons from a folder.
 *
 * @author jlgozalo
 * @version 1.0 jlgozalo 25/08 based on IconLoader
 */
public class ImageLoader {
    
    private static class OMImageIcon extends ImageIcon {
        
        public OMImageIcon(Image image) {
            super(image);
        }
        
        @Override public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, ImageLoader.generateGraphicsWithIconHints(g), x, y);
        }
        
        
    }
    
    /**
     * This method loads an image resource.
     *
     * @param imageName name of the image to load.
     *
     * @return a reference to the image resource.
     *
     * @throws MissingResourceException if the resource doesn't exist.
     */
    public static ImageIcon load(String imageName) throws MissingResourceException {
        return ImageLoader.createImage(ImageLoader.class.getResource(imageName));
    }
    
    /**
     * This method loads an image resource.
     *
     * @param imageName name of the image to load.
     *
     * @return a reference to the image resource.
     *
     * @throws MissingResourceException if the resource doesn't exist.
     */
    public static ImageIcon load(URL location) throws MissingResourceException {
        return ImageLoader.createImage(location);
    }
    
    
    public static Icon of(Image image) {
        return createImage(image);
    }
    
    private static Graphics2D generateGraphicsWithIconHints(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2d;
    }
    
    public static ImageIcon createImage(URL url) {
        try {
            return createImage(ImageIO.read(url));
        } catch (IOException e) {
            throw new UnreachableException(e);
        }
    }
    
    public static ImageIcon createImage(Image source) {
        Double uiScale = LocalPreferences.UI_SCALE.get();
        var desiredScale = ImageLoader.SCALES[ImageLoader.SCALES.length - 1];
        for (var scale : ImageLoader.SCALES) {
            if (uiScale >= scale.v0() && uiScale < scale.v1()) {
                desiredScale = scale;
                break;
            }
        }
        return new OMImageIcon(source.getScaledInstance(desiredScale.v2(), desiredScale.v2(), Image.SCALE_SMOOTH));
    }
    
    private static final Tuple3Record<Double, Double, Integer>[] SCALES = new Tuple3Record[]{
            Tuples.record(Double.MIN_VALUE, 0.1, 1),
            Tuples.record(0.1, 0.2, 2),
            Tuples.record(0.2, 0.5, 4),
            Tuples.record(0.5, 0.7, 8),
            Tuples.record(0.7, 1.2, 20),
            Tuples.record(1.2, 1.5, 32),
            Tuples.record(1.5, Double.MAX_VALUE, 64)
    };
}
