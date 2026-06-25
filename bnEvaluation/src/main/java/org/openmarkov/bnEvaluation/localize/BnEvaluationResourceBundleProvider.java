package org.openmarkov.bnEvaluation.localize;

import org.jetbrains.annotations.NotNull;
//import org.openmarkov.annotation_processing.localization_bindings.BindLocalizations;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

/**
 * Provides the root resource path for BN Evaluation localization files.
 *
 * @author mvillar
 */
//@BindLocalizations(filePath = "bnevaluation/localize/bnevaluation_en.xml", fileIsDirectoryChild = true)
public class BnEvaluationResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public final @NotNull String getRootOfResources() {
        return "/bnevaluation";
    }
    
}
