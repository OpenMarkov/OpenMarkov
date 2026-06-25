package org.openmarkov.sensitivityanalysis.localize;

import org.jetbrains.annotations.NotNull;
//import org.openmarkov.annotation_processing.localization_bindings.BindLocalizations;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

/**
 * Provides the root resource path for Sensitivity Analysis localization files.
 */
//@BindLocalizations(filePath = "sensitivityanalysis/localize/sensitivityanalysis_en.xml", fileIsDirectoryChild = true)
public class SensitivityAnalysisResourceBundleProvider implements LocalizeResourcesProvider {
	
	@Override
	public @NotNull String getRootOfResources() {
		return "/sensitivityanalysis";
	}
	
	
}
