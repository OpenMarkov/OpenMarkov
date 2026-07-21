open module org.openmarkov.full {
	requires org.openmarkov.core;
	requires org.openmarkov.gui;
	requires org.openmarkov.inference;
	requires org.openmarkov.io;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.apache.poi.poi;
	requires org.jdom2;
	requires org.apache.poi.ooxml;
    requires java.desktop;
    requires org.jetbrains.annotations;
    requires com.formdev.flatlaf;
    requires com.google.gson;
    requires org.apache.commons.io;
    requires org.jgrapht.core;
    
    exports org.openmarkov.full;
}
