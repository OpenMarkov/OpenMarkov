import org.openmarkov.stochasticPropagationOutput.localize.StochasticPropagationOutputResourceBundleProvider;

open module org.openmarkov.stochasticpropagationoutput {
	requires org.openmarkov.core;
	requires org.openmarkov.gui;
	requires org.apache.logging.log4j;
	requires org.apache.poi.poi;
	requires org.apache.poi.ooxml;
	requires org.jetbrains.annotations;
    requires java.desktop;
    requires org.openmarkov.inference;
    
    exports org.openmarkov.stochasticPropagationOutput;
	exports org.openmarkov.stochasticPropagationOutput.localize;
	
	provides org.openmarkov.core.localize.spi.LocalizeResourcesProvider with StochasticPropagationOutputResourceBundleProvider;
	
	/*
	 * requires org.openmarkov.core; requires org.openmarkov.gui; requires
	 * org.openmarkov.inference.decompositionintosymmetricdans; requires
	 * org.apache.commons.io; requires swing.layout; requires org.jfree.jfreechart;
	 * requires org.openmarkov.inference.variableelimination;
	 */
	

}
