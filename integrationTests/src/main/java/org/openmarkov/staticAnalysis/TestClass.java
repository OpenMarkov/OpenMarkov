package org.openmarkov.staticAnalysis;

import com.github.javaparser.resolution.TypeSolver;
import org.openmarkov.core.model.network.ProbNet;
import org.xml.sax.SAXException;

public class TestClass {
    
    private static Integer returnJavaValue() {
        return null;
    }
    
    private static SAXException returnJavaExceptionValue() {
        return null;
    }
    
    private static TypeSolver returnDependencyValue() {
        return null;
    }
    
    private static ProbNet returnOMValue() {
        return null;
    }
}
