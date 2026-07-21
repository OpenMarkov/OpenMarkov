/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.gui.loader.element.ImageLoader;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.util.Arrays;

/**
 * SplashScreenOpenMarkov Splash Screen Loader in OpenMarkov to prevent impatient user
 * and to show the progress of loading elements in the Main Program
 *
 * @author jlgozalo
 * @version 1.1 - jrico: Reduced most of the complexity of this class while moving it to a {@link SwingWorker} to allow safe
 * load of the splash screen.
 */
public class SplashScreenLoader {
    
    /**
     * the logo file
     */
    private static final String LOGO_FILE = "/images/OpenMarkov33.jpg";
    
    
    private record Operation(String description, int progress, Runnable action) {
    }
    
    public static void asyncLoadWithSplash(final Runnable onLoadFinishes) {
        SwingUtilities.invokeLater(() -> {
            var splashWindow = new JWindow();
            var content = (JPanel) splashWindow.getContentPane();
            content.setLayout(new BorderLayout());
            
            content.add(
                    new JLabel("", ImageLoader.load(SplashScreenLoader.class.getResource(LOGO_FILE), ImageLoader.ImageOptions.SkipAutoScale), SwingConstants.CENTER),
                    BorderLayout.CENTER);
            
            var progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            content.add(progressBar, BorderLayout.SOUTH);
            
            splashWindow.pack();
            splashWindow.setLocationRelativeTo(null);
            splashWindow.setVisible(true);
            
            var operations = Arrays.asList(
                    new Operation("Loading texts", 0, StringDatabase::getUniqueInstance),
                    new Operation("Loading resources", 30, () -> PluginSearch.init().stream().forEach(ignored -> {
                    })),
                    new Operation("Loading preferences", 70, MainGUI::doReadPreferences),
                    new Operation("Finished", 100, () -> {
                    })
            );
            
            var worker = new SwingWorker<Void, Operation>() {
                @Override
                protected Void doInBackground() {
                    OpenMarkovLogger.LOGGER.debug("Start operations");
                    for (var operation : operations) {
                        publish(operation);
                        operation.action.run();
                        OpenMarkovLogger.LOGGER.debug("{} finished", operation.description);
                    }
                    return null;
                }
                
                @Override
                protected void process(java.util.List<Operation> chunks) {
                    Operation lastOperation = chunks.getLast();
                    progressBar.setString(lastOperation.description);
                    progressBar.setValue(lastOperation.progress);
                }
                
                @Override
                protected void done() {
                    splashWindow.dispose(); // Destroys loading frame cleanly
                    SwingUtilities.invokeLater(onLoadFinishes);
                }
            };
            worker.execute();
        });
    }
}
