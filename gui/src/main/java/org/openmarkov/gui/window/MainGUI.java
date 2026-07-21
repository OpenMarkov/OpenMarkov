/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.gui.component.FrameMirror;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.common.WindowDimensions;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
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
        if (UserPreferences.RESTORE_LATEST_MAIN_GUI_DIMENSIONS.get() && UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.isSet()) {
            UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.get().set(this);
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
    
    private void updatePreferenceDimensions() {
        /*
        var isMaximized = getExtendedState() == Frame.MAXIMIZED_BOTH;
        var originalDimensions = UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.get();
        Point location = isMaximized ? originalDimensions.location() : getLocation();
        Dimension size = isMaximized ? originalDimensions.size() : getSize();
        int extendedState = getExtendedState();
        WindowDimensions newDimensions = new WindowDimensions(location, size, extendedState);
        UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.set(newDimensions);
        */
        UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.set(
                UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.isSet() ?
                        WindowDimensions.of(this, UserPreferences.LATEST_MAIN_GUI_DIMENSIONS.get()) :
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
    public static void doReadPreferences() {
        UserPreferences.initializeAllPreferences();
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
    public void openNetwork(String fileName) throws ProbNetParserException, IOException, NoReaderForFileException, CorruptNetworkFile {
        mainPanel.openNetwork(fileName);
    }
    
}
