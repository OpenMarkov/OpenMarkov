import org.openmarkov.costEffectiveness.localize.CostEffectivenessResourceBundleProvider;

open module org.openmarkov.costeffectiveness {
    requires java.desktop;
    requires org.apache.commons.io;
    requires org.jetbrains.annotations;
    requires org.jfree.jfreechart;
    requires org.openmarkov.core;
    requires org.openmarkov.gui;
    requires org.openmarkov.inference;
    requires swing.layout;

    exports org.openmarkov.costEffectiveness.localize;
    exports org.openmarkov.costEffectiveness;

    provides org.openmarkov.core.localize.spi.LocalizeResourcesProvider with CostEffectivenessResourceBundleProvider;
}
