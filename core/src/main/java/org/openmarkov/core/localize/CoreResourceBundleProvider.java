package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

//@BindLocalizations(filePath = "core/localize/CoreExceptions_en.xml", fileIsDirectoryChild = true)
public class CoreResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public @NotNull String getRootOfResources() {
        return "/core";
    }
    
}
