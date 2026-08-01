/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.gui.configuration.GUIColors;
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
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Map;

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
    private static final String LOGO_FILE = "/images/openmarkov-splash.png";

    /**
     * Where the progress bar sits on the image: the gap between the two columns and the footer.
     */
    private static final Rectangle PROGRESS_BAR_BOUNDS = new Rectangle(130, 762, 1092, 2);

    /**
     * Where the message sits on the image: the footer, between the two texts in the corners.
     */
    private static final Rectangle MESSAGE_BOUNDS = new Rectangle(322, 786, 760, 34);

    /**
     * Italic and spaced out, like the text the image has in the other two corners of the footer.
     */
    private static final Font MESSAGE_FONT = new Font(Font.SERIF, Font.ITALIC, 23)
            .deriveFont(Map.of(TextAttribute.TRACKING, 0.11f));


    /**
     * How long each message stays on screen, so that every step of the loading can be read.
     */
    private static final long MINIMUM_MILLISECONDS_PER_MESSAGE = 250;


    private record Operation(String description, int progress, Runnable action) {
    }

    public static void asyncLoadWithSplash(final Runnable onLoadFinishes) {
        SwingUtilities.invokeLater(() -> {
            var operations = Arrays.asList(
                    new Operation("Loading texts…", 0, StringDatabase::getUniqueInstance),
                    new Operation("Loading resources…", 30, () -> PluginSearch.init().stream().forEach(ignored -> {
                    })),
                    new Operation("Loading preferences…", 70, MainGUI::doReadPreferences),
                    new Operation("Finished", 100, () -> {
                    })
            );

            var splashWindow = new JWindow();
            var content = (JPanel) splashWindow.getContentPane();
            content.setLayout(new BorderLayout());
            
            var splashImage = new JLabel("",
                    ImageLoader.load(SplashScreenLoader.class.getResource(LOGO_FILE), ImageLoader.ImageOptions.SkipAutoScale),
                    SwingConstants.CENTER);
            splashImage.setLayout(null);
            content.add(splashImage, BorderLayout.CENTER);

            var progressBar = new JProgressBar(0, 100);
            // A plain rectangle: the look and feel rounds the bar and gives it a height of its own.
            progressBar.setUI(new BasicProgressBarUI());
            progressBar.setBorderPainted(false);
            progressBar.setForeground(GUIColors.SplashScreen.PROGRESS_BAR_FOREGROUND.getColor());
            progressBar.setBackground(GUIColors.SplashScreen.PROGRESS_BAR_BACKGROUND.getColor());
            progressBar.setBounds(PROGRESS_BAR_BOUNDS);
            splashImage.add(progressBar);

            var message = new JLabel(operations.getFirst().description, SwingConstants.CENTER);
            message.setFont(MESSAGE_FONT);
            message.setForeground(GUIColors.SplashScreen.MESSAGE_FOREGROUND.getColor());
            message.setBounds(MESSAGE_BOUNDS);
            splashImage.add(message);

            splashWindow.pack();
            splashWindow.setLocationRelativeTo(null);
            splashWindow.setVisible(true);
            // On screen before the loading starts: it takes the machine, and a repaint would have to wait for it.
            splashImage.paintImmediately(splashImage.getBounds());

            var worker = new SwingWorker<Void, Operation>() {
                @Override
                protected Void doInBackground() throws InterruptedException {
                    OpenMarkovLogger.LOGGER.debug("Start operations");
                    for (var operation : operations) {
                        publish(operation);
                        long startedAt = System.nanoTime();
                        operation.action.run();
                        OpenMarkovLogger.LOGGER.debug("{} finished", operation.description);
                        long millisecondsLeft = MINIMUM_MILLISECONDS_PER_MESSAGE
                                - (System.nanoTime() - startedAt) / 1_000_000;
                        if (millisecondsLeft > 0) {
                            Thread.sleep(millisecondsLeft);
                        }
                    }
                    return null;
                }
                
                @Override
                protected void process(java.util.List<Operation> chunks) {
                    Operation lastOperation = chunks.getLast();
                    message.setText(lastOperation.description);
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
