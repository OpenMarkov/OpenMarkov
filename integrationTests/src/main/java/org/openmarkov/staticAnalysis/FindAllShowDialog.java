package org.openmarkov.staticAnalysis;

import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicInteger;

public class FindAllShowDialog {
    
    public static void main(String[] args) {
        AtomicInteger messageIndex = new AtomicInteger();
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(c -> c.compilationUnit()
                                 .findAll(com.github.javaparser.ast.expr.MethodCallExpr.class)
                                 .stream())
                  .filter(methodCallExpr -> {
                      try {
                          return "javax.swing.JOptionPane.showMessageDialog".equals(methodCallExpr.resolve()
                                                                                                  .getQualifiedName());
                      } catch (RuntimeException e) {
                          return false;
                      }
                  })
                  /*
                .filter(methodCallExpr ->
                                methodCallExpr.getArguments().stream().noneMatch(arg->arg.toString().startsWith("JOptionPane")))
                   */
                  .forEach(call -> System.out.println(
                          (messageIndex.incrementAndGet())
                                  + " " +
                                  ParseUtils.getSourceLine(call) + ": " + call));
    }
    
}
