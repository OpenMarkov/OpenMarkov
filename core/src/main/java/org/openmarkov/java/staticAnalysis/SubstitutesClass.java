package org.openmarkov.java.staticAnalysis;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) public @interface SubstitutesClass {
    Class[] value();
    
    boolean includesInheritance();
}
