open module org.openmarkov.core {
    requires commons.math3;
    requires jeval;
    requires colt;
    requires org.apache.logging.log4j;
    requires org.jetbrains.annotations;
    // requires transitive java.desktop;

    requires java.xml;

    requires java.instrument;
    requires io.github.classgraph;
    requires org.jdom2;
    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;
    requires org.apache.commons.io;
    requires antlr;
    requires org.apache.poi.poi;
    requires rust.essentials;
    requires java.desktop;
    
    exports org.openmarkov.core.action.core;
    exports org.openmarkov.core.model.decisiontree;
    exports org.openmarkov.core.model.decisiontree.operation;
    exports org.openmarkov.core.exception;
    exports org.openmarkov.core.inference;
    exports org.openmarkov.core.inference.heuristic;
    exports org.openmarkov.core.inference.tasks;
    exports org.openmarkov.core.model.graph;
    exports org.openmarkov.core.model.network;
    exports org.openmarkov.core.model.network.constraint;
    exports org.openmarkov.core.model.network.constraint.annotation;
    exports org.openmarkov.core.model.network.factory;
    exports org.openmarkov.core.model.network.modelUncertainty;
    exports org.openmarkov.core.model.network.potential;
    exports org.openmarkov.core.model.network.potential.canonical;
    exports org.openmarkov.core.model.network.potential.operation;
    exports org.openmarkov.core.model.network.potential.plugin;
    exports org.openmarkov.core.model.network.potential.treeadd;
    exports org.openmarkov.core.model.network.type;
    exports org.openmarkov.core.model.network.type.plugin;
    exports org.openmarkov.core.inference.annotation;
    exports org.openmarkov.core.stringformat;
    exports org.openmarkov.core.testTags;
    exports org.openmarkov.plugin;
    exports org.openmarkov.core.localize.spi;
    exports org.openmarkov.core.localize;
    exports org.openmarkov.core.logging;
    exports org.openmarkov.core.developmentStaticAnalysis;
    exports org.openmarkov.java.enumUtils;
    exports org.openmarkov.java.exceptionUtils;
    exports org.openmarkov.java.cloneUtils;
    exports org.openmarkov.java.nullUtils;
    exports org.openmarkov.core.action.base;
    exports org.openmarkov.core.action.base.linkEdits;
    exports org.openmarkov.core.developmentStaticAnalysis.requirements;
    exports org.openmarkov.java.classUtils;
    exports org.openmarkov.core.developmentStaticAnalysis.mutability;
    exports org.openmarkov.core.expression;
    exports org.openmarkov.java.initialization;
    exports org.openmarkov.java.function;
    exports org.openmarkov.java.collectionsUtils.arrayUtils;
    exports org.openmarkov.java.reflectionUtils;
    exports org.openmarkov.java.collectionsUtils.streamUtils;
    exports org.openmarkov.java.collectionsUtils;
    exports org.openmarkov.core.model.database;
    exports org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;
    
}
