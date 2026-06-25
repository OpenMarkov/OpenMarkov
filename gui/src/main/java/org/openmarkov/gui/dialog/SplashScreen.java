/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * SplashScreen shows the OpenMarkov logo and the progress bar meantime OpenMarkov is
 * loaded.
 *
 * @author jlgozalo
 * @version 1.0 22/11/2008
 */
public class SplashScreen extends JFrame {
    
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -6227174335233774982L;
    /**
     * Component to store the image to splash
     */
    final JLabel imageLabel = new JLabel();
    /**
     * Component to present the progress of the loading
     */
    final JProgressBar progressBar = new JProgressBar();
    /**
     * Image to be displayed
     */
    final ImageIcon imageIcon;
    
    /**
     * Constructor
     *
     * @param imageIcon The image to be used as Splash Screen
     */
    public SplashScreen(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
        jbInit();
    }
    
    /**
     * Main initialization method to display visual components
     *
     */
    void jbInit() {
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        this.setUndecorated(true);
        this.getContentPane().setLayout(new BorderLayout());
        
        progressBar.setStringPainted(true);
        progressBar.setForeground(GUIColors.SplashScreen.PROGRESS_BAR_FOREGROUND.getColor());
        progressBar.setBackground(GUIColors.SplashScreen.PROGRESS_BAR_BACKGROUND.getColor());
        this.getContentPane().add(progressBar, BorderLayout.SOUTH);
        
        imageLabel.setIcon(imageIcon);
        this.getContentPane().add(imageLabel, BorderLayout.CENTER);
        
        this.pack();
    }
    
    /**
     * States which will be the maximum progress to be displayed
     *
     * @param maxProgress the max progress
     */
    public void setProgressMax(int maxProgress) {
        
        progressBar.setMaximum(maxProgress);
    }
    
    /**
     * Update the progress of the loading of the main program
     *
     * @param progress the progress
     */
    public void setProgress(int progress) {
        
        final int theProgress = progress;
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override public void run() {
                
                progressBar.setValue(theProgress);
            }
        });
    }
    
    /**
     * Display the progress of the loading in a Progress Bar
     *
     * @param message  The underlying message with the progress
     * @param progress The graphical bar with the progress
     */
    public void setProgress(String message, int progress) {
        
        final int theProgress = progress;
        final String theMessage = message;
        setProgress(progress);
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override public void run() {
                
                progressBar.setValue(theProgress);
                progressBar.setString(theMessage);
            }
        });
    }
    
    /**
     * Show SplashScreen
     *
     * @param b True to put SplashScreen visible, false otherwise
     */
    public void setScreenVisible(boolean b) {
        
        final boolean boo = b;
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override public void run() {
                
                setVisible(boo);
            }
        });
    }
    
}