package org.openmarkov.costEffectiveness.localize;

import org.jetbrains.annotations.NotNull;
//import org.openmarkov.annotation_processing.localization_bindings.BindLocalizations;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;


/**
 * Provides the root resource path for cost-effectiveness localization files.
 */
//@BindLocalizations(filePath = "costeffectiveness/localize/costeffectiveness_en.xml", fileIsDirectoryChild = true)
public class CostEffectivenessResourceBundleProvider implements LocalizeResourcesProvider {
	
	@Override
	public final @NotNull String getRootOfResources() {
		return "/costeffectiveness";
	}
	
}
