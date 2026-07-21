open module org.openmarkov.io {
    requires org.openmarkov.core;
    
    requires org.jdom2;
    requires org.apache.commons.io;
    requires jeval;
    requires org.jetbrains.annotations;
    requires org.openmarkov.inference;
    requires org.apache.poi.poi;
    requires java.xml;
    requires antlr;
    requires org.apache.logging.log4j;
    requires org.apache.poi.ooxml;
    
    uses org.xml.sax.InputSource;
    
    exports org.openmarkov.io.probmodel.exception;
    exports org.openmarkov.io.probmodel.reader;
    exports org.openmarkov.io.probmodel.writer;
    exports org.openmarkov.io.probmodel.strings;
    
    exports org.openmarkov.io.xmlbif.strings;
    exports org.openmarkov.io.xmlbif;
    exports org.openmarkov.io.amua;
    exports org.openmarkov.core.io.format.annotation;
    exports org.openmarkov.core.io;
    exports org.openmarkov.core.io.database;
    exports org.openmarkov.core.io.database.plugin;
    exports org.openmarkov.core.io.exception;
    exports org.openmarkov.io.elvira;
    exports org.openmarkov.io.database.excel;
}
