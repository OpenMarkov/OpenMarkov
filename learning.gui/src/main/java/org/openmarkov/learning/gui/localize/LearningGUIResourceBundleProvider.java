package org.openmarkov.learning.gui.localize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

/**
 * Provides the root resource path for Learning GUI localization files.
 */
//@BindLocalizations(filePath = "learning/gui/localize/Learning_en.xml", fileIsDirectoryChild = true)
public class LearningGUIResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public @NotNull String getRootOfResources() {
        return "/learning/gui";
    }
    
}
