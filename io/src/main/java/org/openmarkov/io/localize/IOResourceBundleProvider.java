package org.openmarkov.io.localize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

//@BindLocalizations(filePath = "io/localize/io_class_localizations_en.xml.xml", fileIsDirectoryChild = true)
public class IOResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public @NotNull String getRootOfResources() {
        return "/io";
    }
    
}
