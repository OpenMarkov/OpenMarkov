package org.openmarkov.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;

public class FrameMirror extends JComponent {
    
    private final JFrame frameToHide;
    private BufferedImage imageOfTheFrame;
    private Point contentPaneLocation; // Relative location of the frame to the start of the window.
    private int freezingCount;
    
    public FrameMirror(JFrame targetFrame) {
        this.frameToHide = targetFrame;
        addMouseListener(new MouseAdapter() {
        });
        addKeyListener(new KeyAdapter() {
        });
    }
    
    public void freeze() {
        //We don't re-freeze the frame if it is already frozen.
        if (this.freezingCount > 0) {
            this.freezingCount++;
            return;
        }
        this.freezingCount++;
        this.frameToHide.setGlassPane(this);
        this.captureFrameContent();
        this.setVisible(true);
    }
    
    public void unfreeze() {
        if (this.freezingCount <= 0) {
            return;
        }
        this.freezingCount--;
        this.setVisible(false);
    }
    
    
    //Prevents other mouse inputs.
    @Override
    public boolean contains(int x, int y) {
        return true;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(this.imageOfTheFrame, this.contentPaneLocation.x, this.contentPaneLocation.y, this);
    }
    
    
    public void captureFrameContent() {
        Component contentPane = frameToHide.getContentPane();
        int width = contentPane.getWidth();
        int height = contentPane.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        // Creates an image of the current frame's content.
        imageOfTheFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        contentPane.printAll(imageOfTheFrame.createGraphics());
        
        contentPaneLocation = SwingUtilities.convertPoint(contentPane, 0, 0, this);
    }
    
}