package org.openmarkov.staticAnalysis;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import org.jspecify.annotations.Nullable;
import org.openmarkov.java.reflectionUtils.ReflectionUtils;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class FindAllStringBuilderUses {
    
    public static void main(String[] args) {
        AtomicInteger messageIndex = new AtomicInteger();
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(parsedClass -> parsedClass.compilationUnit().findAll(MethodCallExpr.class).stream())
                  .forEach(methodCallExpr -> {
                      @Nullable ResolvedMethodDeclaration resolvedMethodDeclaration;
                      try {
                          resolvedMethodDeclaration = methodCallExpr.resolve();
                      } catch (RuntimeException ex) {
                          resolvedMethodDeclaration = null;
                      }
                      if (!(resolvedMethodDeclaration instanceof ReflectionMethodDeclaration reflectionMethodDeclaration)) {
                          return;
                      }
                      try {
                          var field = ReflectionUtils.forceGetField(reflectionMethodDeclaration, "method", Method.class);
                          if (!field.getDeclaringClass().equals(StringBuilder.class)) {
                              return;
                          }
                          System.out.println((messageIndex.incrementAndGet())
                                                     + " " +
                                                     ParseUtils.getSourceLine(methodCallExpr) + ": " + System.lineSeparator() + methodCallExpr.toString()
                                                                                                                                              .trim());
                          
                      } catch (ReflectiveOperationException ignored) {
                      
                      }
                  });
        
    }
    
}
