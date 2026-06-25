import org.openmarkov.dbgenerator.localize.DBGeneratorResourceBundleProvider;

open module org.openmarkov.dbgenerator {
	requires org.openmarkov.core;
	requires org.openmarkov.gui;
	requires org.apache.commons.io;
	requires swing.layout;
	requires org.jetbrains.annotations;
    requires java.desktop;
    requires org.openmarkov.io;
    
    exports org.openmarkov.dbgenerator;
	exports org.openmarkov.dbgenerator.gui;
	
	exports org.openmarkov.dbgenerator.localize;
	
	/*
	 * requires org.openmarkov.gui; requires org.jfree.jfreechart; requires
	 * org.openmarkov.inference.variableelimination; requires java.desktop; requires
	 * org.apache.logging.log4j;
	 */
	
	provides org.openmarkov.core.localize.spi.LocalizeResourcesProvider with DBGeneratorResourceBundleProvider;

}
