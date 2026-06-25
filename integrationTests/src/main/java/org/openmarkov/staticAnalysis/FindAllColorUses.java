package org.openmarkov.staticAnalysis;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.openmarkov.gui.configuration.GUIColor;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class FindAllColorUses {
    
    public static void main(String[] args) {
        ArrayList<Expression> expressions = new ArrayList<>();
        ParseUtils.baseOpenMarkovParsedClasses()
                  .filter(compiledClass -> compiledClass.originalClass() != GUIColor.class)
                  .filter(compiledClass -> compiledClass.originalClass() != GUIColors.class)
                  .filter(compiledClass -> compiledClass.originalClass() != org.openmarkov.stochasticPropagationOutput.XlsxWrite.class)
                  .forEach(compiledClass -> {
                      compiledClass.compilationUnit().findAll(FieldAccessExpr.class).stream()
                                   .filter(fieldDeclaration -> {
                                       try {
                                           fieldDeclaration.resolve();
                                           return true;
                                       } catch (RuntimeException ex) {
                                           return false;
                                       }
                                   })
                                   .filter(fieldDeclaration -> fieldDeclaration.resolve()
                                                                               .isField())
                                   .filter(fieldDeclaration -> fieldDeclaration.resolve()
                                                                               .asField()
                                                                               .getType()
                                                                               .isReferenceType())
                                   .filter(fieldDeclaration -> fieldDeclaration.resolve()
                                                                               .asField()
                                                                               .getType()
                                                                               .asReferenceType()
                                                                               .getTypeDeclaration()
                                                                               .get()
                                                                               .getQualifiedName()
                                                                               .equals(Color.class.getName()))
                                   .forEach(expressions::add);
                      compiledClass.compilationUnit().findAll(ObjectCreationExpr.class)
                                   .stream()
                                   .filter(objectCreationExpr -> {
                                       try {
                                           objectCreationExpr.resolve();
                                           return true;
                                       } catch (RuntimeException ex) {
                                           return false;
                                       }
                                   })
                                   .filter(objectCreationExpr -> objectCreationExpr.resolve()
                                                                                   .declaringType()
                                                                                   .getQualifiedName()
                                                                                   .equals(Color.class.getName()))
                                   .forEach(expressions::add);
                  });
        AtomicInteger messageIndex = new AtomicInteger();
        expressions.stream().sorted(Comparator.comparing(ParseUtils::getSourceLine))
                   .forEach(expression -> System.out.println(
                           (messageIndex.incrementAndGet())
                                   + " " +
                                   ParseUtils.getSourceLine(expression) + ": " + expression));
    }
    
}
