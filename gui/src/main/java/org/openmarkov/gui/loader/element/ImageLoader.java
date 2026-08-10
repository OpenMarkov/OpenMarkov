/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.element;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.configuration.UserPreferences;

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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.MissingResourceException;

/**
 * This class is used to load icons from a folder.
 *
 * @author jlgozalo
 * @version 1.0 jlgozalo 25/08 based on IconLoader
 */
public class ImageLoader {
    
    public enum ImageOptions {
        SkipAutoScale
    }
    
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
    public static ImageIcon load(URL location, ImageOptions... imageOptions) throws MissingResourceException {
        return ImageLoader.createImage(location, imageOptions);
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
    
    public static ImageIcon createImage(URL url, ImageOptions... imageOptions) {
        try {
            return createImage(ImageIO.read(url), imageOptions);
        } catch (IOException e) {
            throw new UnreachableException(e);
        }
    }
    
    public static ImageIcon createImage(Image source, ImageOptions... imageOptions) {
        var options = EnumSet.noneOf(ImageOptions.class);
        options.addAll(Arrays.asList(imageOptions));
        if (options.contains(ImageOptions.SkipAutoScale)) {
            return new OMImageIcon(source);
        }
        Double uiScale = UserPreferences.UI_SCALE.get();
        var desiredScale = ImageLoader.SCALES[ImageLoader.SCALES.length - 1];
        for (var scale : ImageLoader.SCALES) {
            if (uiScale >= scale.minRange && uiScale < scale.maxRange) {
                desiredScale = scale;
                break;
            }
        }
        return new OMImageIcon(source.getScaledInstance(desiredScale.pixelsToUse, desiredScale.pixelsToUse, Image.SCALE_SMOOTH));
    }
    
    record Scale(double minRange, double maxRange, int pixelsToUse) {
    }
    
    private static final Scale[] SCALES = new Scale[]{
            new Scale(Double.MIN_VALUE, 0.1, 1),
            new Scale(0.1, 0.2, 2),
            new Scale(0.2, 0.5, 4),
            new Scale(0.5, 0.7, 10),
            new Scale(0.7, 1.2, 24),
            new Scale(1.2, 1.5, 38),
            new Scale(1.5, Double.MAX_VALUE, 64)
    };
}
