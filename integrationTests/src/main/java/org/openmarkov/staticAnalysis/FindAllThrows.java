package org.openmarkov.staticAnalysis;

import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class FindAllThrows {
    
    public static void main(String[] args) {
        AtomicInteger messageIndex = new AtomicInteger();
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(c -> c.compilationUnit()
                                 .findAll(com.github.javaparser.ast.body.CallableDeclaration.class)
                                 .stream())
                  //.filter(call -> !call.getThrownExceptions().isEmpty())
                  .forEach(call -> System.out.println(
                          (messageIndex.incrementAndGet())
                                  + " " +
                                  ParseUtils.getSourceLine(call) + ": " + System.lineSeparator() + call.toString()
                                                                                                       .trim()));
    }
    
}
