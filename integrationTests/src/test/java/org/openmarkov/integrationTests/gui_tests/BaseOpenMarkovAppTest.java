package org.openmarkov.integrationTests.gui_tests;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.full.OpenMarkov;
import org.openmarkov.gui.configuration.LocalPreference;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.assertj.swing.core.BasicRobot.robotWithCurrentAwtHierarchy;

/**
 * Common class to test functionalities in the user version of OpenMarkov.
 *
 * @author jrico
 */
public abstract class BaseOpenMarkovAppTest extends BaseWindowTest<FrameFixture, Frame> {
    
    /**
     * The window is set-up by starting the {@link OpenMarkov} application and getting the {@link MainGUI} shown after
     * the splash screen.
     * <p>
     * The {@link FrameFixture} is the resulting {@link MainGUI}, which must be {@link MainGUI#INSTANCE}.
     * <p>
     * Before setting the application, the {@link org.openmarkov.full.HoverLoggerPlugin} is enabled, and storing
     * preferences are disabled in order to preserve the developer's preferences (otherwise, it would be highly possible
     * a test might override at least one {@link LocalPreference}).
     *
     * @return A {@link FrameFixture} containing the {@link MainGUI#INSTANCE}.
     */
    @Override protected FrameFixture setUpWindow() {
        LocalPreference.IGNORE_STORAGE = true;
        LocalPreferences.HOVER_LOGGER_ENABLED.set(true);
        org.assertj.swing.launcher.ApplicationLauncher.application(OpenMarkov.class).start();
        return WindowFinder.findFrame(new GenericTypeMatcher<>(Frame.class) {
            @Override protected boolean isMatching(Frame component) {
                return component.isShowing() && component instanceof MainGUI;
            }
        }).using(robotWithCurrentAwtHierarchy());
    }
    
    /**
     * Opens a network by using the mouse and keyboard on OpenMarkov's interface.
     *
     * @param netFileToOpen The network file to open.
     * @return Both the {@link NetworkEditorPanel} and the {@link ProbNet}.
     */
    public @NotNull BaseOpenMarkovAppTest.OpenNetworkResult openNetwork(File netFileToOpen) {
        // Maximize the window so the NetworkEditorPanel viewport is large enough for the robot
        // to reach nodes at any coordinate. Without this, the viewport can be as small as 16px
        // tall, causing robot clicks to land on scrollbars instead of nodes.
        GuiActionRunner.execute(() -> {
            ((Frame) window.target()).setExtendedState(Frame.MAXIMIZED_BOTH);
            return null;
        });
        Pause.pause(new Condition("Window to be maximized") {
            @Override public boolean test() {
                return GuiActionRunner.execute(() -> (((Frame) window.target()).getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);
            }
        }, Timeout.timeout(5, TimeUnit.SECONDS));
        this.window.menuItem("File").click();
        this.window.menuItem("File.Open").click();
        var openDialog = this.window.fileChooser("NetworkOMFileChooser");
        openDialog.selectFile(netFileToOpen);
        // approveButton().click() relies on the robot's mouse simulation which can fail
        // with modal dialogs on some platforms. Set the file and call approveSelection() on EDT.
        GuiActionRunner.execute(() -> {
            openDialog.target().setSelectedFile(netFileToOpen);
            openDialog.target().approveSelection();
            return null;
        });
        Pause.pause(new Condition("ZoomComboBox to become enabled after network opens") {
            @Override public boolean test() {
                return GuiActionRunner.execute(() -> window.comboBox("ZoomComboBox").target().isEnabled());
            }
        }, Timeout.timeout(10, TimeUnit.SECONDS));
        this.window.comboBox("ZoomComboBox").selectItem("100%");
        var editorPanel = this.window.panel(new GenericTypeMatcher<>(JPanel.class) {
            @Override protected boolean isMatching(JPanel component) {
                return component.isShowing() && component instanceof NetworkEditorPanel;
            }
        });
        ProbNet probNet = ((NetworkEditorPanel) editorPanel.target()).getNetworkEditorPanel().getProbNet();
        return new OpenNetworkResult(editorPanel, probNet);
    }
    
    public record OpenNetworkResult(JPanelFixture editorPanel, ProbNet probNet) {
    }
    
}











