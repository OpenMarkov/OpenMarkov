package org.openmarkov.integrationTests.gui;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.dialog.common.PotentialPanelManager;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import java.util.stream.Stream;

@Disabled("36 of the 39 potential types do have an editing panel; ExternalPotential, "
        + "IncrementPotential and TableWithEvents do not, so the user cannot edit them. "
        + "Re-enable once those three have a panel. (The previous reason, \"on hold until "
        + "July 2026\", had expired and did not say what was wrong.)")
public class AllPotentialsHaveAPanelTest {
    
    public static Stream<Class<? extends Potential>> toolPotentialClasses() {
        return PluginSearch.init().childrenOf(Potential.class).filter(ClassUtils::isConcrete).stream();
    }
    
    @ParameterizedTest
    @MethodSource("toolPotentialClasses")
    void allPotentialsHaveAPanel(Class<? extends Potential> potentialClass) {
        org.junit.jupiter.api.Assertions.assertNotNull(PotentialPanelManager.getInstance()
                                                                            .getPotentialPanelClassOf(potentialClass), "There is no panel for editing " + potentialClass);
    }
    
}
