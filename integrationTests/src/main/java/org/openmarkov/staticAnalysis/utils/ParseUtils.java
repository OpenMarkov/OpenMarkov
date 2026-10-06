package org.openmarkov.staticAnalysis.utils;

import com.github.javaparser.ParseProblemException;
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
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.java.initialization.Lazy;
import org.openmarkov.plugin.PluginSearch;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseUtils {
    
    public static void prepareJavaParserConfiguration() {
        //This method does nothing on purpose, it only forces the static initializer to run.
    }
    
    private static final ParserConfiguration PARSER_CONFIGURATION;
    
    static {
        PARSER_CONFIGURATION = StaticJavaParser.getParserConfiguration();
        ParseUtils.PARSER_CONFIGURATION.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_25);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver(false)); //Adds parsing of JDK code
        typeSolver.add(new ClassLoaderTypeSolver(Thread.currentThread().getContextClassLoader()));
        
        // This code is to load external dependencies such as code from .jars or target/class, but it is mostly
        // unrequired on testing.
        //
        // I left this code here just in case necessities change.
//        try (ScanResult scan = new ClassGraph().scan()) {
//            List<URL> classpathUrls = scan.getClasspathURLs();
//            for (URL url : classpathUrls) {
//                if (url.getPath().endsWith(".jar")) {
//                    try {
//                        typeSolver.add(new JarTypeSolver(new File(url.toURI())));
//                    } catch (Exception ignored) {
//                    }
//                }
//            }
//            URL[] urlArray = classpathUrls.toArray(new URL[0]);
//            ClassLoader projectClassLoader = new URLClassLoader(
//                    urlArray,
//                    Thread.currentThread().getContextClassLoader()
//            );
//            typeSolver.add(new ClassLoaderTypeSolver(projectClassLoader));
//        }
        
        ParseUtils.PARSER_CONFIGURATION.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        StaticJavaParser.setConfiguration(ParseUtils.PARSER_CONFIGURATION);
    }
    

    /**
     * Whether the class belongs to the integrationTests module, which holds the
     * analysis tools themselves and is not part of the analyzed application.
     * <p>
     * This used to compare {@code getModule()}, which only tells modules apart
     * on the module path; the build runs on the classpath, where every class
     * shares the unnamed module, so the comparison excluded everything and the
     * tools analyzed zero classes. The code source — the jar or classes
     * directory a class was loaded from — tells them apart in both launch modes.
     */
    private static boolean isInIntegrationTestsModule(Class<?> clazz) {
        var integrationTestsSource = IntegrationTest.class.getProtectionDomain().getCodeSource();
        var classSource = clazz.getProtectionDomain().getCodeSource();
        if (integrationTestsSource == null || classSource == null) {
            return false;
        }
        return Objects.equals(integrationTestsSource.getLocation(), classSource.getLocation());
    }

    public record ParsedClass(Class<?> originalClass, CompilationUnit compilationUnit) {
    }
    
    private static final Lazy<Map<Class<?>, ParsedClass>> OPENMARKOV_PARSED_CLASSES =
            new Lazy<>(() -> PluginSearch
                    .init()
                    .stream()
                    .parallel()
                    .filter(openmarkovClass -> !ParseUtils.isInIntegrationTestsModule(openmarkovClass)) //Exclusion of integration tests
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
        } catch (ParseProblemException e) {
            // One unparseable source must not kill every analysis tool: the parse
            // used to escape the lazy initializer and break them all at once, and
            // the exception did not even name the file.
            System.err.println("ParseUtils: could not parse " + openmarkovClass.getName() + ": " + e.getMessage());
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
