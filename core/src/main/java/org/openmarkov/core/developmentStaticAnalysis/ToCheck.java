package org.openmarkov.core.developmentStaticAnalysis;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ToCheck {
    
    ReasonKind[] reasonKind();
    
    int[] relatesToIssues() default {};
    
    String reasonDescription();
    
    public enum ReasonKind {
        PROBABLE_BUG,
        BUG,
        CRITICAL_BUG,
        CODE_QUALITY,
        EXCEPTIONS_REWORK,
        USER_EXPERIENCE;
    }
    
}
