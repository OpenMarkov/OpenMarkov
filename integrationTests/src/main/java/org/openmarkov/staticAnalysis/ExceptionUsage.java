package org.openmarkov.staticAnalysis;

import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.UnionType;
import org.openmarkov.plugin.PluginSearch;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExceptionUsage {
    
    private static final Map<String, Class<?>> PLUGINS_BY_CANONICAL_NAMES = PluginSearch
            .full()
            .filter(aClass -> aClass.getCanonicalName() != null)
            .stream()
            .collect(Collectors.toMap(
                    Class::getCanonicalName,
                    value -> value));
    
    static class ExceptionUse {
        Class<? extends Exception> exceptionClass;
        int instantiations = 0;
        int catches = 0;
        
        ExceptionUse(Class<? extends Exception> exceptionClass) {
            this.exceptionClass = exceptionClass;
        }
        
        final float caughtRatio() {
            if (this.instantiations == 0) {
                return this.catches;
            }
            return ((float) this.catches) / this.instantiations;
        }
    }
    
    public static void main(String[] args) {
        ParseUtils.prepareJavaParserConfiguration();
        var omExceptionToUses = PluginSearch
                .init()
                .childrenOf(Exception.class)
                .stream()
                .collect(Collectors.toMap(
                        exceptionClass -> exceptionClass,
                        ExceptionUse::new));
        
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(parsedClass -> parsedClass.compilationUnit()
                                                     .findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class)
                                                     .stream())
                  .forEach(objectCreationExpr -> {
                      var exceptionName = objectCreationExpr.getType().resolve().asReferenceType().getQualifiedName();
                      var exceptionClass = ExceptionUsage.PLUGINS_BY_CANONICAL_NAMES.get(exceptionName);
                      if (!omExceptionToUses.containsKey(exceptionClass)) {
                          return;
                      }
                      while (exceptionClass != null) {
                          ExceptionUse uses = omExceptionToUses.get(exceptionClass);
                          if (uses != null) {
                              uses.instantiations += 1;
                          }
                          exceptionClass = exceptionClass.getSuperclass();
                      }
                  });
        
        ParseUtils
                .baseOpenMarkovParsedClasses()
                .flatMap(parsedClass -> parsedClass.compilationUnit().findAll(CatchClause.class)
                                                   .stream())
                .flatMap(catchClause -> {
                             var parameter = catchClause.getParameter().getType();
                             if (parameter instanceof UnionType unionType) {
                                 return unionType
                                         .getElements().stream()
                                         .map(type -> {
                                             String className = type.resolve().asReferenceType().getQualifiedName();
                                             return ExceptionUsage.PLUGINS_BY_CANONICAL_NAMES.get(className);
                                         });
                             }
                             String className = parameter.resolve().asReferenceType().getQualifiedName();
                             return Stream.of(ExceptionUsage.PLUGINS_BY_CANONICAL_NAMES.get(className));
                         }
                )
                .forEach(exceptionName -> {
                    if (!omExceptionToUses.containsKey(exceptionName)) {
                        return;
                    }
                    omExceptionToUses.get(exceptionName).catches += 1;
                });
        omExceptionToUses
                .keySet()
                .stream()
                .sorted(Comparator.comparing(Class::getName))
                .map(omExceptionToUses::get)
                .map(usesOfException ->
                             usesOfException.caughtRatio() + " " +
                                     usesOfException.exceptionClass +
                                     " is caught " + usesOfException.catches +
                                     " times and instantiated "
                                     + usesOfException.instantiations + " times.")
                .forEach(System.out::println);
    }
    
    
}
