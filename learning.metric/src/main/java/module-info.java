open module org.openmarkov.learning.metric {
    requires org.openmarkov.core;
    requires org.openmarkov.learning.core;
    requires org.apache.poi.poi;
    requires java.desktop;
    requires org.jetbrains.annotations;
    
    exports org.openmarkov.learning.metric.aic;
    exports org.openmarkov.learning.metric.bayesian;
    exports org.openmarkov.learning.metric.bde;
    exports org.openmarkov.learning.metric.entropy;
    exports org.openmarkov.learning.metric.k2;
    exports org.openmarkov.learning.metric.mdlm;
    exports org.openmarkov.learning.metric;
    exports org.openmarkov.learning.metric.annotation;
    exports org.openmarkov.learning.metric.util;
    exports org.openmarkov.learning.metric.cmi.mutualInformation;
    exports org.openmarkov.learning.metric.cmi.conditional;
    exports org.openmarkov.learning.metric.cmi.accuracy;
}
