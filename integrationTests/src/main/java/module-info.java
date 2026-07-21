open module org.openmarkov.integrationTests {
    requires org.jetbrains.annotations;
    requires org.json;
    requires org.openmarkov.core;
    requires org.openmarkov.costeffectiveness;
    requires org.openmarkov.dbgenerator;
    requires org.openmarkov.full;
    requires org.openmarkov.gui;
    requires org.openmarkov.inference;
    requires org.openmarkov.io;
    requires org.openmarkov.learning.algorithm;
    requires org.openmarkov.learning.core;
    requires org.openmarkov.learning.gui;
    requires org.openmarkov.learning.metric;
    requires org.openmarkov.sensitivityanalysis;
    requires org.openmarkov.stochasticpropagationoutput;
    requires java.xml;
    requires org.apache.logging.log4j;
    requires org.jfree.jfreechart;
    requires com.github.javaparser.core;
    requires com.github.javaparser.symbolsolver.core;
    requires com.google.common;
    requires io.github.classgraph;
    requires org.apache.commons.lang3;
    requires org.apache.poi.poi;
    requires com.google.gson;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.jspecify;
    
    exports org.openmarkov.integrationTests;
    exports org.openmarkov.integrationTests.localize;
}