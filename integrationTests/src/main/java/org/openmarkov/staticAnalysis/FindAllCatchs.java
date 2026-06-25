package org.openmarkov.staticAnalysis;

import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class FindAllCatchs {
    
    public static void main(String[] args) {
        AtomicInteger messageIndex = new AtomicInteger();
        ParseUtils.baseOpenMarkovParsedClasses()
                  .flatMap(c -> c.compilationUnit().findAll(com.github.javaparser.ast.stmt.CatchClause.class).stream())
                  .forEach(call -> System.out.println(
                          (messageIndex.incrementAndGet())
                                  + " " +
                                  ParseUtils.getSourceLine(call) + ": " + System.lineSeparator() + call.toString()
                                                                                                       .trim()));
    }
    
}
