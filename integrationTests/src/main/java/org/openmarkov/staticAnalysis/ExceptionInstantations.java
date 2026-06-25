package org.openmarkov.staticAnalysis;

import org.openmarkov.core.exception.*;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.io.IOException;
import java.util.List;

public class ExceptionInstantations {
    
    
    private static final List<Class<? extends Throwable>> IGNORED_EXCEPTIONS = List.of(
            UnrecoverableException.class,
            UnreachableException.class
    );
    
    private static boolean shouldCheckException(Class<Throwable> throwableClass) {
        if (!IOpenMarkovException.class.isAssignableFrom(throwableClass)) {
            return false;
        }
        var isAnIgnoredException = ExceptionInstantations.IGNORED_EXCEPTIONS
                .stream()
                .anyMatch(ignoredException ->
                                  ignoredException.isAssignableFrom(throwableClass));
        if (isAnIgnoredException) {
            return false;
        }
        return true;
    }
    
    public static void main(String[] args) {
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(parsedClass -> parsedClass.compilationUnit()
                          .findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class)
                          .stream())
                  .filter(objectCreationExpr ->
                                  Throwable.class.isAssignableFrom(ParseUtils.classOf(objectCreationExpr.getType())))
                  .filter(objectCreationExpr ->
                                  ExceptionInstantations.shouldCheckException((Class<Throwable>) ParseUtils.classOf(objectCreationExpr.getType())))
                  .forEach(objectCreationExpr ->
                                   System.out.println(ParseUtils.classOf(objectCreationExpr.getType())
                                                                .getSimpleName() + " at " + ParseUtils.getSourceLine(objectCreationExpr)));
    }
    
    
}
