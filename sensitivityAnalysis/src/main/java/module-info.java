import org.openmarkov.sensitivityanalysis.localize.SensitivityAnalysisResourceBundleProvider;

open module org.openmarkov.sensitivityanalysis {
	requires org.openmarkov.core;
	requires org.openmarkov.gui;
	requires org.jfree.jfreechart;
    requires org.apache.logging.log4j;
    requires org.jetbrains.annotations;
    requires java.desktop;
	requires org.openmarkov.inference;
    requires swingx;
    
    exports org.openmarkov.sensitivityanalysis.localize;
	exports org.openmarkov.sensitivityanalysis.model;
	exports org.openmarkov.sensitivityanalysis.dialog;
	exports org.openmarkov.sensitivityanalysis.exceptions;
	
	provides org.openmarkov.core.localize.spi.LocalizeResourcesProvider with SensitivityAnalysisResourceBundleProvider;
}
