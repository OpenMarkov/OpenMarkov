import org.openmarkov.gui.localize.GUIResourceBundleProvider;

open module org.openmarkov.gui {
    
    requires org.apache.commons.io;
    requires org.jdom2;
    requires org.openmarkov.core;
    requires org.openmarkov.io;
    requires org.openmarkov.inference;
    requires org.apache.logging.log4j;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.jfree.jfreechart;
    requires com.google.gson;
    
    //requires com.hexidec.ekit;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires java.prefs;
    requires jeval;
    requires org.apache.commons.lang3;
    requires org.apache.commons.compress;
    requires com.formdev.flatlaf;
    requires jdk.compiler;
    requires java.xml;
    requires colt;
    requires com.google.errorprone.annotations;
    requires inference.DES;
    requires com.jthemedetector;
    requires rust.essentials;
    
    exports org.openmarkov.gui.action;
    exports org.openmarkov.gui.layout.bayesian;
    exports org.openmarkov.gui.localize;
    exports org.openmarkov.gui.window;
    exports org.openmarkov.gui.dialog.inference.common;
    exports org.openmarkov.gui.loader.element;
    exports org.openmarkov.gui.toolplugin;
    exports org.openmarkov.gui.util;
    exports org.openmarkov.gui.dialog.common;
    exports org.openmarkov.gui.window.edition;
    exports org.openmarkov.gui.dialog.io;
    exports org.openmarkov.gui.dialog.costeffectiveness;
    exports org.openmarkov.gui.dialog.treeadd;
    exports org.openmarkov.gui.configuration;
    exports org.openmarkov.gui.configuration.gson;
    exports org.openmarkov.gui.window.decisiontree;
    exports org.openmarkov.gui.dialog;
    exports org.openmarkov.gui.exception;
    exports org.openmarkov.gui.component;
    exports org.openmarkov.gui.graphic;
    exports org.openmarkov.gui.componentBuilder;
    exports org.openmarkov.gui.commonComponents;
    exports org.openmarkov.gui.window.edition.networkEditorPanel;
    exports org.openmarkov.gui.dialog.node;
    exports org.openmarkov.java.swing;
    exports org.openmarkov.gui.window.decisiontree.elements;
    exports org.openmarkov.gui.graphics;
    exports org.openmarkov.gui.menutoolbar.toolbar;
    exports org.openmarkov.gui.menutoolbar.menu;
    exports org.openmarkov.gui.dialog.network;
    exports org.openmarkov.gui.productTour.tour;
    exports org.openmarkov.gui.productTour.tour.action;
    
    uses org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
    provides org.openmarkov.core.localize.spi.LocalizeResourcesProvider with GUIResourceBundleProvider;
    
}
