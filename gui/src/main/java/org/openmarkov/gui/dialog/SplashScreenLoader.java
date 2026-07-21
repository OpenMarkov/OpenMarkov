/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.gui.loader.element.ImageLoader;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;

import javax.swing.*;
import java.net.URL;

/**
 * SplashScreenOpenMarkov Splash Screen Loader in OpenMarkov to prevent impatient user
 * and to show the progress of loading elements in the Main Program
 *
 * @author jlgozalo
 * @version 1.0 16/11/2008
 */
public class SplashScreenLoader {
    
    /**
     * the logo file
     */
    //private static final String logoFile = "/images/OpenMarkovSplash.jpg";
    private static final String logoFile = "/images/OpenMarkov33.jpg";
    private SplashScreen splash;
    
    public void splashScreenInit() {
        // TODO externalize to OpenMarkov Properties the string for the icon
        URL url = this.getClass().getResource(logoFile);
        ImageIcon myImage = ImageLoader.load(url);
        splash = new SplashScreen(myImage);
        splash.setLocationRelativeTo(null);
        splash.setProgressMax(100);
        splash.setVisible(true);
        splash.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
    }
    
    
    /**
     * destroy the splash Screen turning not visible
     */
    public void splashScreenDestroy() {
        SwingUtilities.invokeLater(splash::dispose);
    }
    
    /**
     * get splash
     *
     * @return aSplash The real splash screen
     */
    public SplashScreen getSplash() {
        return splash;
    }
    
}
