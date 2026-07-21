package org.openmarkov.staticAnalysis.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.java.initialization.Lazy;
import org.openmarkov.plugin.PluginSearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseUtils {
    
    public static void prepareJavaParserConfiguration() {
        //This method does nothing on purpose, it only forces the static initializer to run.
    }
    
    private static final ParserConfiguration PARSER_CONFIGURATION;
    
    static {
        PARSER_CONFIGURATION = StaticJavaParser.getParserConfiguration();
        ParseUtils.PARSER_CONFIGURATION.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver(false)); //Adds parsing of JDK code
        ScanResult scan = new ClassGraph().scan();
        var classPaths = scan.getClasspathURLs().stream()
                             .map(URL::getFile)
                             .map(File::new)
                             .filter(File::exists)
                             .toList();
        scan.close();
        for (File classPath : classPaths) {
            //Adds external dependencies jar for resolving classes.
            String absolutePath = classPath.getAbsolutePath();
            if (absolutePath.endsWith(".jar")) {
                try {
                    typeSolver.add(new JarTypeSolver(classPath));
                } catch (IOException e) {
                }
            } else {
                if (absolutePath.contains("target\\classes")) {
                    absolutePath = absolutePath.replace("target\\classes", "src\\main\\java");
                }
                typeSolver.add(new JavaParserTypeSolver(new File(absolutePath)));
            }
        }
        ParseUtils.PARSER_CONFIGURATION.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        StaticJavaParser.setConfiguration(ParseUtils.PARSER_CONFIGURATION);
    }
    
    public record ParsedClass(Class<?> originalClass, CompilationUnit compilationUnit) {
    }
    
    private static final Lazy<Map<Class<?>, ParsedClass>> OPENMARKOV_PARSED_CLASSES =
            new Lazy<>(() -> PluginSearch
                    .init()
                    .stream()
                    .parallel()
                    .filter(openmarkovClass -> openmarkovClass.getModule() != IntegrationTest.class.getModule()) //Exclusion of integration tests
                    //.filter(openmarkovClass -> openmarkovClass.getModule() != AnnotationProcessing.class.getModule()) //Exclusion of annotation processing
                    .map(ParseUtils::parseClass)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            ParsedClass::originalClass,
                            v -> v,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new)));
    
    public static @Nullable ParsedClass parseClass(Class<?> openmarkovClass) {
        if (ParseUtils.OPENMARKOV_PARSED_CLASSES.isInitialized()) {
            ParsedClass preparsedClass = ParseUtils.OPENMARKOV_PARSED_CLASSES.get().get(openmarkovClass);
            if (preparsedClass != null) {
                return preparsedClass;
            }
        }
        try {
            if (StaticJavaParser.getParserConfiguration() != ParseUtils.PARSER_CONFIGURATION) {
                StaticJavaParser.setConfiguration(ParseUtils.PARSER_CONFIGURATION);
            }
            return new ParsedClass(openmarkovClass, StaticJavaParser.parse(ClassUtils.fileOfClass(openmarkovClass)));
        } catch (FileNotFoundException | IllegalArgumentException e) {
            return null;
        }
    }
    
    public static @NotNull Stream<ParsedClass> baseOpenMarkovParsedClasses() {
        return ParseUtils.OPENMARKOV_PARSED_CLASSES.get().values().stream();
    }
    
    private static final Lazy<Map<String, Class<? extends Object>>> CLASSES_BY_NAME = Lazy.of(() -> PluginSearch
            .full()
            .stream()
            .filter(aClass -> aClass.getCanonicalName() != null)
            .collect(Collectors.toMap(Class::getCanonicalName, value -> value)));
    
    public static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return ParseUtils.CLASSES_BY_NAME.get().get(className);
        }
    }
    
    public static Class<?> classOf(Type type) {
        return switch (type.resolve()) {
            case ResolvedReferenceType referenceType -> ParseUtils.classForName(referenceType.getQualifiedName());
            default -> throw new IllegalStateException("Unexpected value: " + type.resolve());
        };
    }
    
    public static CompilationUnit sourceOf(Node node) {
        return ParseUtils.superSearch(node, CompilationUnit.class).get();
    }
    
    public static <SearchingClass extends Node> Optional<SearchingClass> superSearch(Node node, Class<? extends SearchingClass> searchingClass) {
        while (node != null && !searchingClass.isAssignableFrom(node.getClass())) {
            node = node.getParentNode().orElse(null);
        }
        if (node == null) {
            return Optional.empty();
        }
        return Optional.of(searchingClass.cast(node));
    }
    
    public static @NotNull String getSourceLine(Node objectCreationExpr) {
        CompilationUnit origin = ParseUtils.sourceOf(objectCreationExpr);
        Optional<Range> range = objectCreationExpr.getRange();
        String packageName = origin.getPackageDeclaration().map(NodeWithName::getNameAsString)
                                   .orElse("");
        String className = origin.getPrimaryTypeName().orElse(null);
        String qualifiedName = packageName + "." + className;
        var methodName = ParseUtils.superSearch(objectCreationExpr, CallableDeclaration.class)
                                   .map(CallableDeclaration::getNameAsString)
                                   .orElse("somewhere");
        int line = range.get().begin.line;
        return String.format("%s.%s(%s.java:%d)", qualifiedName, methodName, className, line);
    }
    
}
