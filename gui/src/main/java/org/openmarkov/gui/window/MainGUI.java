/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.component.FrameMirror;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.SplashScreenLoader;
import org.openmarkov.gui.dialog.common.WindowDimensions;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serial;

/**
 * This class constructs the main GUI in a frame with a splash screen during the
 * loading and reading configuration from external preferences.
 *
 * @author mendoza
 * @author jlgozalo
 * @version 1.3 jlgozalo - replacing System.err with JOptionPane
 */
public class MainGUI extends JFrame {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final MainGUI INSTANCE = new MainGUI();
    
    public final MainPanel mainPanel;
    
    private final FrameMirror frameMirror;
    
    /**
     * Launch the MainGUIInit runnable process
     */
    private MainGUI() {
        UIManager.put("MenuItem.disabledAreNavigable", Boolean.FALSE);
        setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        setSize(screenPortionSize(screenInsets));
        setLocation(screenInsets.left, screenInsets.top);
        this.mainPanel = new MainPanel(this);
        setContentPane(this.mainPanel);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("OpenMarkov");
        setName("MainGUI");
        setMinimumSize(new Dimension(700, 250));
        this.frameMirror = new FrameMirror(this);
        if (LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.isSet()) {
            LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.get().set(this);
        }
        addComponentListener(new ComponentListener() {
            
            @Override public void componentResized(ComponentEvent e) {
                updatePreferenceDimensions();
            }
            
            @Override public void componentMoved(ComponentEvent e) {
                updatePreferenceDimensions();
            }
            
            @Override public void componentShown(ComponentEvent e) {
            
            }
            
            @Override public void componentHidden(ComponentEvent e) {
            
            }
        });
    }
    
    public static void loadWithSplash() {
        SplashScreenLoader splash = new SplashScreenLoader();
        splash.splashScreenInit();
        splash.getSplash().setProgress("Loading preferences", 0);
        MainGUI.doReadPreferences();
        splash.getSplash().setProgress("Loading resources", 50);
        StringDatabase.getUniqueInstance();
        // This forces static loading of every OpenMarkov's class. We might want to change into 'full' instead of 'init'
        // to preload every class available.
        PluginSearch.init().stream().forEach(ignored -> {
        });
        splash.getSplash().setProgress("Completed", 100);
        // loading the application
        splash.splashScreenDestroy();
    }
    
    private void updatePreferenceDimensions() {
        /*
        var isMaximized = getExtendedState() == Frame.MAXIMIZED_BOTH;
        var originalDimensions = LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.get();
        Point location = isMaximized ? originalDimensions.location() : getLocation();
        Dimension size = isMaximized ? originalDimensions.size() : getSize();
        int extendedState = getExtendedState();
        WindowDimensions newDimensions = new WindowDimensions(location, size, extendedState);
        LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.set(newDimensions);
        */
        LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.set(
                LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.isSet() ?
                        WindowDimensions.of(this, LocalPreferences.LATEST_MAIN_GUI_DIMENSIONS.get()) :
                        WindowDimensions.of(this)
        );
    }
    
    //Conditionally disabled
    public void freeze() {
        if (true) return;
        this.frameMirror.freeze();
    }
    
    //Conditionally disabled
    public void unfreeze() {
        if (true) return;
        this.frameMirror.unfreeze();
    }
    
    /**
     * read the {@code OpenMarkovPreferences} configuration, and set the
     * LastConnection preference to current Time
     */
    private static void doReadPreferences() {
        LocalPreferences.initializeAllPreferences();
    }
    
    /**
     * This method returns a dimension that represents the 3/4 size of the
     * screen.
     *
     * @return new dimensions of the window.
     */
    private static Dimension screenPortionSize(Insets screenInsets) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screen.width - screenInsets.right - screenInsets.left;
        int height = screen.height - screenInsets.top - screenInsets.bottom;
        return new Dimension(width, height);
    }
    
    /**
     * Opens net from file
     *
     * @param fileName File name
     */
    public void openNetwork(String fileName) throws ParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        mainPanel.openNetwork(fileName);
    }
    
}
