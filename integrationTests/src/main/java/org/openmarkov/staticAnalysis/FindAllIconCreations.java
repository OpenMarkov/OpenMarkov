package org.openmarkov.staticAnalysis;

import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.openmarkov.java.reflectionUtils.ReflectionUtils;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;

public class FindAllIconCreations {
    public static void main(String[] args) {
        AtomicInteger messageIndex = new AtomicInteger(0);
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(parsedClass -> parsedClass.compilationUnit().findAll(ObjectCreationExpr.class).stream())
                  .filter(creation -> {
                      try {
                          var res = creation.resolve();
                          var constructor = ReflectionUtils.forceGetField(res, "constructor", Constructor.class);
                          var sourceClass = constructor.getDeclaringClass();
                          return Icon.class.isAssignableFrom(sourceClass);
                      } catch (RuntimeException | ReflectiveOperationException e) {
                          return false;
                      }
                  })
                  .map(iconCreation -> ParseUtils.getSourceLine(iconCreation) + ": "
                          + System.lineSeparator() + iconCreation.toString().trim())
                  .distinct()
                  .forEach(iconCreationString -> {
                      System.out.println((messageIndex.incrementAndGet()) + " " + iconCreationString);
                  });
        new Integer(5);
        new ImageIcon("");
    }
    
    
}
