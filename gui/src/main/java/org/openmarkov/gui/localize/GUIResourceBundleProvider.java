package org.openmarkov.gui.localize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

/**
 * Service provider that exposes the GUI module's localization resources
 * (under {@code /gui}) to the {@link org.openmarkov.core.localize.StringDatabase}.
 */
//@BindLocalizations(filePath = "gui/localize/Menus_en.xml", fileIsDirectoryChild = true)
public class GUIResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public @NotNull String getRootOfResources() {
        return "/gui";
    }
    
}
