package org.openmarkov.inference.DES.localize;

import org.jetbrains.annotations.NotNull;
//import org.openmarkov.annotation_processing.localization_bindings.BindLocalizations;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;


/**
 * Provides the root resource path for Inference DES localization files.
 */
//@BindLocalizations(filePath = "inference_des/localize/inference_des_class_localizations_en.xml", fileIsDirectoryChild = true)
public class InferenceDESResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public final @NotNull String getRootOfResources() {
        return "/inference_des";
    }
    
}
