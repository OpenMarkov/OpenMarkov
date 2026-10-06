package org.openmarkov.staticAnalysis.utils;

import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserRecordDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionInterfaceDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionRecordDeclaration;
import org.jspecify.annotations.Nullable;
import org.openmarkov.java.initialization.Lazy;

import java.util.List;

public class ObjectCreations {
    
    public record ObjectCreationWithClass(ObjectCreationExpr expression, Class<?> constructedClass) {
    }
    
    public static final Lazy<List<ObjectCreationWithClass>> OBJECT_CREATIONS =
            Lazy.of(() ->
                            ParseUtils.baseOpenMarkovParsedClasses()
                                      .flatMap(parsedClass -> parsedClass.compilationUnit()
                                                                         .findAll(ObjectCreationExpr.class)
                                                                         .stream())
                                      .parallel()
                                      .map(creation -> {
                                          var resolvedType = creation.getType().resolve();
                                          Class<?> classToConstruct = switch (resolvedType) {
                                              case ReferenceTypeImpl referenceType ->
                                                      switch (referenceType.getTypeDeclaration().orElseGet(null)) {
                                                          case ReflectionClassDeclaration reflectionClassDeclaration ->
                                                                  ObjectCreations.findByQNameOrField(reflectionClassDeclaration);
                                                          case ReflectionInterfaceDeclaration reflectionInterfaceDeclaration ->
                                                                  ObjectCreations.findByQNameOrField(reflectionInterfaceDeclaration);
                                                          case ReflectionRecordDeclaration reflectionRecordDeclaration ->
                                                                  ObjectCreations.findByQNameOrField(reflectionRecordDeclaration);
                                                          case JavaParserClassDeclaration javaParserClassDeclaration ->
                                                                  ObjectCreations.findByQualifiedName(javaParserClassDeclaration.getWrappedNode()
                                                                                                                                .resolve()
                                                                                                                                .getQualifiedName());
                                                          case JavaParserInterfaceDeclaration interfaceDeclaration ->
                                                                  ObjectCreations.findByQualifiedName(interfaceDeclaration.getWrappedNode()
                                                                                                                          .getFullyQualifiedName()
                                                                                                                          .get());
                                                          case JavaParserRecordDeclaration recordDeclaration ->
                                                                  ObjectCreations.findByQualifiedName(recordDeclaration.getWrappedNode()
                                                                                                                       .getFullyQualifiedName()
                                                                                                                       .get());
                                                          case null, default -> null;
                                                      };
                                              default -> null;
                                          };
                                          
                                          return new ObjectCreationWithClass(creation, classToConstruct);
                                      })
                                      .filter(creation -> creation.constructedClass != null)
                                      .toList());
    
    private static Class<?> findByQNameOrField(ResolvedReferenceTypeDeclaration declaration) {
        var qName = declaration.getQualifiedName();
        var byQName = ObjectCreations.findByQualifiedName(qName);
        if (byQName != null) {
            return byQName;
        }
        try {
            var field = declaration.getClass().getDeclaredField("clazz");
            field.setAccessible(true);
            return (Class<?>) field.get(declaration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
    
    private static @Nullable Class<?> findByQualifiedName(String qualifiedName) {
        do {
            try {
                return ClassLoader.getSystemClassLoader().loadClass(qualifiedName);
            } catch (ClassNotFoundException e) {
            }
            qualifiedName = ObjectCreations.replaceLastLiteral(qualifiedName, ".", "$");
        } while (qualifiedName.contains("."));
        return null;
    }
    
    private static String replaceLastLiteral(String text, String target, String replacement) {
        int index = text.lastIndexOf(target);
        if (index == -1) {
            return text;
        }
        return text.substring(0, index) + replacement + text.substring(index + target.length());
    }
    
}
