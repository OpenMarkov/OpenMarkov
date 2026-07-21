/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.gui_tests.tours;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.gui.productTour.tour.Tour;
import org.openmarkov.gui.productTour.tour.TourManager;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.integrationTests.gui_tests.BaseOpenMarkovAppTest;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "Your machine does not have a Graphic Environment")
@Disabled("Tour playback requires interactive user attention; even on a non-headless display "
        + "the AWT Robot driving the tour crashes the surefire forked VM. Re-enable once the "
        + "tour engine can run fully unattended.")
public class TutorialTest extends BaseOpenMarkovAppTest {
    
    public static Stream<Tour> tours() {
        return TourManager.availableProductTours().values().stream().flatMap(Collection::stream);
    }
    
    @ParameterizedTest
    @MethodSource("tours")
    void test(Tour tour){
        MainGUI mainGUI = (MainGUI) this.window.target();
        tour.launch(new TestActionRequester(tour, this.window.robot()), mainGUI);
    }
    
}
