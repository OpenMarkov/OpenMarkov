package org.openmarkov.integrationTests.gui;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.dialog.common.PotentialPanelManager;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import java.util.stream.Stream;

@Disabled("On hold until July 2026")
public class AllPotentialsHaveAPanel {
    
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
