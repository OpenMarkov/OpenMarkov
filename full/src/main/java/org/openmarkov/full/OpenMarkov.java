/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.full;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.core.model.network.NetworkMetadata;
import org.openmarkov.gui.configuration.JavaSerializationUtils;
import org.openmarkov.gui.configuration.StartupAction;
import org.openmarkov.gui.configuration.Theme;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.OMExceptionHandler;
import org.openmarkov.gui.dialog.SplashScreenLoader;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class stores a set of additionalProperties and the {@code main}
 * method.
 * <p>
 * If there is some other main method in other class is only for test.
 * <p>
 *
 * @author manuel
 * @author fjdiez
 * @author jmendoza
 * @version 1.1 jlgozalo - Suppress public modifier in the configuration
 * attributes (not required); add explicit initial value and fix bug in
 * the getUniqueInstance with the mainGui starting inside the singleton
 * (not outside) to prevent double GUI initialization
 * @since OpenMarkov 1.0
 */
public class OpenMarkov {
    
    private static final String JUNIQUE_ID = "org.openmarkov.OpenMarkov";
    
    private static boolean STARTED_AS_MAIN_INSTANCE = true;
    
    /**
     * OpenMarkov main class
     *
     * @param baseArgs Arguments
     */
    public static void main(String[] baseArgs) {
        try {
            OpenMarkov.onSingleInstance(baseArgs);
        } catch (AlreadyLockedException e) {
            OpenMarkov.onInstanceRejected(baseArgs);
        }
    }
    
    
    private static void onSingleInstance(String[] baseArgs) throws AlreadyLockedException {
        OpenMarkovLogger.LOGGER.debug("Start");
        AtomicBoolean appLoaded = new AtomicBoolean(false);
        MessageHandler instanceMessageHandler = new MessageHandler() {
            public @NotNull String handle(String message) {
                return OpenMarkov.readSingleInstanceMessage(message, appLoaded);
            }
        };
        boolean canBeStandAloneInstance = OpenMarkov.readArguments(baseArgs).isEmpty();
        STARTED_AS_MAIN_INSTANCE = true;
        try {
            JUnique.acquireLock(OpenMarkov.JUNIQUE_ID, instanceMessageHandler);
        } catch (AlreadyLockedException e) {
            STARTED_AS_MAIN_INSTANCE = false;
            if (!canBeStandAloneInstance) {
                throw e;
            }
            new Thread(() -> {
                while (true) {
                    try {
                        JUnique.acquireLock(OpenMarkov.JUNIQUE_ID, instanceMessageHandler);
                        return;
                    } catch (AlreadyLockedException ex) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException exc) {
                            throw new RuntimeException(exc);
                        }
                    }
                }
            }).start();
        }

        if (UserPreferences.UI_SCALE.isSet()) {
            System.setProperty("sun.java2d.uiScale", UserPreferences.UI_SCALE.get().toString());
        }
        System.setProperty("flatlaf.uiScale", String.valueOf(UserPreferences.UI_SCALE.get()));
        NetworkMetadata.USERS_LOCALE = Locale.getDefault(Locale.Category.FORMAT);
        // Numbers and dates are written in English's format way, such as using the decimal point instead of the
        // Spanish's decimal comma. This avoids multiple problems related to localization.
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        Thread.setDefaultUncaughtExceptionHandler(new OMExceptionHandler());
        try {
            Theme.updateInterfaceToLook();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new UnreachableException(e);
        }
        
        SplashScreenLoader.asyncLoadWithSplash(() -> {
            MainGUI.INSTANCE.setVisible(true);
            if (OpenMarkov.STARTED_AS_MAIN_INSTANCE &&
                    UserPreferences.STARTUP_ACTIONS.get().contains(StartupAction.RESTORE_LAST_SESSION)) {
                for (var lastSessionFile : UserPreferences.LAST_SESSION_NETWORK_FILES.get()) {
                    try {
                        MainGUI.INSTANCE.openNetwork(lastSessionFile);
                    } catch (ProbNetParserException | IOException | NoReaderForFileException | CorruptNetworkFile e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                    }
                }
            }
            OpenMarkov.executeArguments(OpenMarkov.readArguments(baseArgs));
            if (MainGUI.INSTANCE.mainPanel.getNetworksTabPanel().getTabCount() == 0 &&
                    UserPreferences.STARTUP_ACTIONS.get().contains(StartupAction.SHOW_CREATE_NEW_NETWORK)) {
                MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getFileHandler().createNewNetwork();
            }
            appLoaded.set(true);
        });
        

        /*
        var developmentTheme = new File("development.theme.json").getAbsoluteFile();
        System.out.println("Reading changes at " + developmentTheme);
        long lastModified;
        while (true) {
            lastModified = developmentTheme.lastModified();
            System.out.println("Loading development theme from " + developmentTheme);
            try {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        UIManager.setLookAndFeel(new IntelliJTheme.ThemeLaf(new IntelliJTheme(new FileInputStream(developmentTheme))));
                        SwingUtilities.updateComponentTreeUI(MainGUI.INSTANCE);
                    } catch (UnsupportedLookAndFeelException | IOException e) {
                        System.out.println(e);
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            while (lastModified == developmentTheme.lastModified()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        */
    }
    
    private static @NotNull String readSingleInstanceMessage(String message, AtomicBoolean appLoaded) {
        while (!appLoaded.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        var childWindowsOfMainGUI = new ArrayList<Window>();
        var toVisit = new ArrayDeque<Window>();
        toVisit.add(MainGUI.INSTANCE);
        while (!toVisit.isEmpty()) {
            Window visitingWindow = toVisit.removeFirst();
            childWindowsOfMainGUI.add(visitingWindow);
            toVisit.addAll(Arrays.stream(visitingWindow.getOwnedWindows()).toList());
        }
        //The first windows is the MainGUI, so we have to remove it.
        childWindowsOfMainGUI.removeFirst();
        var isAModalOpen = childWindowsOfMainGUI.stream()
                                                .filter(Component::isVisible)
                                                .anyMatch(window -> window instanceof JDialog dialog && dialog.isModal());
        if (isAModalOpen) {
            return "-1";
        }
        OpenMarkov.executeArguments(OpenMarkov.readArguments(JavaSerializationUtils.deserialize(message)));
        return "0";
    }
    
    private static void onInstanceRejected(String[] baseArgs) {
        var res = JUnique.sendMessage(OpenMarkov.JUNIQUE_ID, JavaSerializationUtils.serialize(baseArgs));
        if ("-1".equals(res)) {
            if (UserPreferences.UI_SCALE.isSet()) {
                System.setProperty("sun.java2d.uiScale", UserPreferences.UI_SCALE.get().toString());
            }
            System.setProperty("flatlaf.uiScale", String.valueOf(UserPreferences.UI_SCALE.get()));
            try {
                Theme.updateInterfaceToLook();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException _) {
            }
            JOptionPane.showMessageDialog(MainGUI.INSTANCE, "<html>Make yourself sure you don't have any dialogs open in OpenMarkov.</html>", "OpenMarkov is busy", JOptionPane.ERROR_MESSAGE);
        }
        //Ensure no leftover of the instance rejection is left. When showing the error message a leftover might be left,
        //so we need to ensure the closing of this instance.
        System.exit(0);
    }
    
    public sealed interface OMArgument extends Serializable {
        
        record SetLanguage(String language) implements OMArgument {
        }
        
        record OpenFile(File file) implements OMArgument {
        }
        
    }
    
    private static @NotNull ArrayList<OMArgument> readArguments(String[] args) {
        String languageToSet = null;
        Collection<File> filesToOpen = new ArrayList<>();
        for (int i = 0; i < args.length; ++i) {
            if ("-l".equals(args[i]) || "-language".equals(args[i])) {
                if (i + 1 < args.length) {
                    languageToSet = args[i + 1];
                    ++i;
                }
            } else if (new File(args[i]).exists()) {
                filesToOpen.add(new File(args[i]));
            }
        }
        ArrayList<OMArgument> arguments = new ArrayList<>();
        if (languageToSet != null) {
            arguments.add(new OMArgument.SetLanguage(languageToSet));
        }
        filesToOpen.stream().map(OMArgument.OpenFile::new).forEach(arguments::add);
        return arguments;
    }
    
    private static void executeArguments(Iterable<? extends OMArgument> args) {
        for (var arg : args) {
            switch (arg) {
                case OMArgument.SetLanguage setLanguage ->
                        StringDatabase.getUniqueInstance().setLanguage(setLanguage.language);
                case OMArgument.OpenFile openFile -> {
                    try {
                        MainGUI.INSTANCE.openNetwork(openFile.file.getAbsolutePath());
                    } catch (ProbNetParserException | IOException | NoReaderForFileException | CorruptNetworkFile e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                    }
                }
            }
        }
    }
    
}