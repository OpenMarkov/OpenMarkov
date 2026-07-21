package org.openmarkov.integrationTests.localize;

import org.jetbrains.annotations.NotNull;
//import org.openmarkov.annotation_processing.localization_bindings.BindLocalizations;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

//@BindLocalizations(filePath = "integrationTests/localize/IntegrationTestsExceptions_en.xml", fileIsDirectoryChild = true)
public class IntegrationTestResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public @NotNull String getRootOfResources() {
        return "/integrationTests";
    }
    
}
