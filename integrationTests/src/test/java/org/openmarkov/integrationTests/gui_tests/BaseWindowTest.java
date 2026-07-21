package org.openmarkov.integrationTests.gui_tests;

import org.assertj.swing.fixture.AbstractWindowFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.awt.*;

/**
 * Common class to test a Window of the GUI (Such as JDialogs or JFrames)
 *
 * @param <TWindowFixture> The type of Fixture. Usually {@link org.assertj.swing.fixture.DialogFixture} or
 * {@link org.assertj.swing.fixture.FrameFixture}.
 *
 * @author jrico
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class BaseWindowTest<TWindowFixture extends AbstractWindowFixture<TWindowFixture, TWindow, ?>, TWindow extends Window> {
    
    /**
     * The generated window on the {@link BaseWindowTest#setUpWindow()} method.
     * <p>
     * Since {@link BaseWindowTest#beforeTest()} method is {@link BeforeEach}, the window gets regenerated for every
     * test in the class.
     */
    protected TWindowFixture window;
    
    /**
     * Specifies how to generate the window to test. This method is executed in {@link BaseWindowTest#beforeTest()}
     * to regenerate the window.
     *
     * @return The re-generated window to test.
     */
    protected abstract TWindowFixture setUpWindow();
    
    /**
     * Initializes the test by re-generating the window and showing it to the front.
     * <p>
     * This gets ignored if the machine has no Graphic Environment.
     *
     * @see GraphicsEnvironment#isHeadless()
     */
    @BeforeEach
    public void beforeTest() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        this.window = this.setUpWindow();
        this.window.show();
        this.window.target().toFront();
        this.window.focus();
    }
    
    /**
     * Cleanups the test by releasing the resources taken by the window.
     * <p>
     * This gets ignored if the machine has no Graphic Environment.
     *
     * @see GraphicsEnvironment#isHeadless()
     */
    @AfterEach
    public void afterTest() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        this.window.cleanUp();
        this.window.target().dispose();
    }
    
}
