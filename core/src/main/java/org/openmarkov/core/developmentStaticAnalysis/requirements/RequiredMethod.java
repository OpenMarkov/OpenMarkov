package org.openmarkov.core.developmentStaticAnalysis.requirements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiredMethod {
    String methodName();
    
    Class[] parameters() default {};
    
    Class returnType() default void.class;
    
    MethodKind methodKind() default MethodKind.Instance;
    
    enum MethodKind {
        Instance,
        Static,
        Abstract;
        
        public boolean is(Method method) {
            final boolean isStatic = Modifier.isStatic(method.getModifiers());
            final boolean isAbstract = Modifier.isAbstract(method.getModifiers());
            return switch (this) {
                case Instance -> !isStatic && !isAbstract;
                case Static -> isStatic && !isAbstract;
                case Abstract -> !isStatic && isAbstract;
            };
        }
        
        public String stringifyForMethodTitle() {
            return switch (this) {
                case Instance -> "";
                case Static -> "static ";
                case Abstract -> "abstract ";
            };
        }
    }
}
