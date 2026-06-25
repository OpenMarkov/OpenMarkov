package org.openmarkov.integrationTests.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.model.network.potential.canonical.TuningPotential;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.probmodel.reader.PotentialParser;
import org.openmarkov.io.xmlbif.XMLBIFReader;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

class PGMXReadersTest {
    
    static final HashSet<Class<? extends PGMXReader_0_2>> READERS_THAT_CAN_MISS_POTENTIAL_READER_METHODS
            = new HashSet<>(List.of(PGMXReader_0_2.class, XMLBIFReader.class));
    
    /**
     * Potential classes that are computational artifacts and are never stored in PGMX files
     * as first-class elements, so they do not need a corresponding reader method.
     * <ul>
     *   <li>{@link GTablePotential} – runtime GLM artifact.</li>
     *   <li>{@link StrategicTablePotential} – serialised as {@code TablePotential} with embedded strategy.</li>
     *   <li>{@link UncertainTablePotential}, {@link AugmentedProbTable} – read via the {@code Table} tag
     *       inherited from {@code TablePotential}.</li>
     *   <li>{@link StrategyTree}, {@link SDAGStrategyTree} – read via the {@code TreeADD} parser.</li>
     *   <li>{@link MinPotential}, {@link MaxPotential}, {@link TuningPotential} – read via the
     *       {@code ICIPotential} parser with type discrimination.</li>
     * </ul>
     */
    static final HashSet<Class<? extends Potential>> POTENTIALS_WITHOUT_PGMX_REPRESENTATION;
    
    static {
        HashSet<Class<? extends Potential>> set = new HashSet<>(List.of(
                GTablePotential.class,
                StrategicTablePotential.class,
                UncertainTablePotential.class,
                AugmentedProbTable.class,
                StrategyTree.class,
                MinPotential.class,
                MaxPotential.class,
                TuningPotential.class
        ));
        try {
            // SDAGStrategyTree is in a non-exported package; reference via reflection
            @SuppressWarnings("unchecked")
            Class<? extends Potential> sdagClass =
                    (Class<? extends Potential>) Class.forName(
                            "org.openmarkov.core.model.network.potential.sdag.SDAGStrategyTree");
            set.add(sdagClass);
        } catch (ClassNotFoundException ignored) {
        }
        POTENTIALS_WITHOUT_PGMX_REPRESENTATION = set;
    }
    
    record TestData(PGMXReader_0_2 pgmxReader, Class<? extends Potential> potentialClass,
                    boolean requiresToReadAllPotentials) {
    }
    
    @ParameterizedTest
    @MethodSource("generateTestData")
    public void testAllReaders(TestData testData) {
        PotentialParser parser = testData.pgmxReader.potentialParsers.get(testData.potentialClass);
        if (parser == null) {
            if (testData.requiresToReadAllPotentials) {
                Assertions.fail("No potential parser found in " + testData.pgmxReader.getClass() + " for potential of class " + testData.potentialClass);
            }
        }
    }
    
    public static Stream<TestData> generateTestData() {
        var readers = PluginSearch.init()
                                  .extending(PGMXReader_0_2.class)
                                  .filter(ClassUtils::isConcrete)
                                  .filter(readerClass -> !readerClass.equals(Classificator.class))
                                  .stream()
                                  //.filter(reader -> reader
                                  //        .getAnnotation(FormatType.class).extension().equalsIgnoreCase("PGMX"))
                                  .toList();
        
        return readers.stream().flatMap(readerClass -> {
            PGMXReader_0_2 reader;
            try {
                reader = readerClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            boolean requiresToReadAllPotentials = !PGMXReadersTest.READERS_THAT_CAN_MISS_POTENTIAL_READER_METHODS.contains(readerClass);
            return PluginSearch.init()
                               .extending(Potential.class)
                               .filter(ClassUtils::isConcrete)
                               .stream()
                               .filter(potentialClass -> !POTENTIALS_WITHOUT_PGMX_REPRESENTATION.contains(potentialClass))
                               .map(potentialClass -> new TestData(reader, potentialClass, requiresToReadAllPotentials));
        });
    }
    
    
}