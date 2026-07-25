package org.openmarkov.integrationTests.core.model.network;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.exception.CorruptNetworkFile;

import org.openmarkov.core.model.network.Variable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PotentialsCanBeClonedTest {
    
    record DeepCloneTestData(Potential potential, ProbNet probNet) {
    }
    
    Stream<DeepCloneTestData> deepCloneTestData() throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        var net = NetsIO.openNetworkURL(PotentialsCanBeClonedTest.DAN_WITH_EVERY_POTENTIAL_URL)
                        .probNet();
        return net.getNodes().stream()
                  .filter(node -> !node.getName().endsWith("Parent1"))
                  .filter(node -> !node.getName().endsWith("Parent2"))
                  .map(Node::getPotential).map(potential -> new DeepCloneTestData(potential, net));
    }
    
    @ParameterizedTest
    @MethodSource("deepCloneTestData")
    void testDeepCopy(DeepCloneTestData deepCloneTestData) {
        // Note what this does not test: it deep-copies the potential into the very network it came
        // from, so every variable it already holds is the right one and the re-pointing a deep copy
        // exists to do is never exercised. That is what
        // deepCopyUsesTheVariablesOfTheDestinationNetwork below is for.
        Potential sourcePotential = deepCloneTestData.potential;
        ProbNet probNet = deepCloneTestData.probNet;
        Potential clonedPotential = sourcePotential.deepCopy(probNet);
        assertThat(sourcePotential).usingRecursiveComparison().isEqualTo(clonedPotential);
    }
    
    
    Stream<Potential> cloneTestData() throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        return this.deepCloneTestData().map(DeepCloneTestData::potential);
    }
    
    @ParameterizedTest
    @MethodSource("cloneTestData")
    void testCopy(Potential sourcePotential) {
        Potential clonedPotential = sourcePotential.copy();
        assertThat(sourcePotential).usingRecursiveComparison().isEqualTo(clonedPotential);
    }
    

    // ---------------------------------------------------------------- the two checks by value miss

    /**
     * A deep copy exists to carry a potential into <em>another</em> network, re-pointing every
     * variable it holds at that network's own instances. The check above cannot see whether it does:
     * it copies the potential into the very network it came from, where the old variables are already
     * the right ones. This one copies into a different network and then demands identity, not
     * equality - two Variable objects with the same name and states are equal by value, and
     * everything else in the model looks variables up by identity.
     */
    @Disabled("Two families still keep variables of the source network when deepCopy(otherNet) is "
            + "called directly, and the paths say where: AugmentedProbTablePotential at "
            + ".augmentedProbTable.variables (the inner table is not carried over to the destination) "
            + "and UnivariateDistrPotential at .tableVariables. Switch on as each is fixed.")
    @ParameterizedTest
    @MethodSource("deepCloneTestData")
    void deepCopyUsesTheVariablesOfTheDestinationNetwork(DeepCloneTestData deepCloneTestData) {
        ProbNet destination = deepCloneTestData.probNet.deepCopy();

        Potential clone = deepCloneTestData.potential.deepCopy(destination);

        List<String> strangers = new ArrayList<>();
        for (Map.Entry<Variable, String> reached : variablesReachableFrom(clone).entrySet()) {
            Variable variable = reached.getKey();
            Variable ofDestination = destination.getVariable(variable.getName());
            if (ofDestination != null && ofDestination != variable) {
                // The path is the point: knowing WHICH field still holds the old instance is the
                // whole diagnosis; the name alone only says that one of them does.
                strangers.add(variable.getName() + " at " + reached.getValue());
            }
        }
        assertThat(strangers)
                .as("%s keeps variables of the source network: %s",
                    clone.getClass().getSimpleName(), strangers)
                .isEmpty();
    }

    /**
     * Sharing a mutable structure between a potential and its copy makes them the same object where
     * it matters, and the comparison by value cannot see it: sharing makes them <em>more</em> equal,
     * not less. Reported as the fields that are the same instance on both sides.
     */
    @Disabled("Finds one: UnivariateDistrPotential.copy() shares finiteStatesVariables and "
            + "parameterVariables with its original. Switch on when that is fixed.")
    @ParameterizedTest
    @MethodSource("cloneTestData")
    void copyDoesNotShareMutableStateWithItsOriginal(Potential sourcePotential) {
        Potential clone = sourcePotential.copy();

        List<String> shared = mutableFieldsSharedBetween(sourcePotential, clone);

        assertThat(shared)
                .as("%s shares mutable state with its copy: %s",
                    sourcePotential.getClass().getSimpleName(), shared)
                .isEmpty();
    }

    // ---------------------------------------------------------------- reflection helpers

    /**
     * Every variable the object graph hanging off {@code root} can reach, each mapped to the chain of
     * fields, list positions and map slots that leads to it. Keyed by identity, because two variables
     * with the same name are equal and this test exists precisely to tell them apart.
     */
    private Map<Variable, String> variablesReachableFrom(Object root) {
        Map<Variable, String> found = new IdentityHashMap<>();
        walk(root, java.util.Collections.newSetFromMap(new IdentityHashMap<>()), found, 0, "");
        return found;
    }

    private void walk(Object value, Set<Object> seen, Map<Variable, String> found, int depth, String path) {
        if (value == null || depth > 6 || !seen.add(value)) {
            return;
        }
        if (value instanceof Variable variable) {
            found.putIfAbsent(variable, path);
            return;
        }
        if (value instanceof Collection<?> collection) {
            int position = 0;
            for (Object element : collection) {
                walk(element, seen, found, depth + 1, path + "[" + position++ + "]");
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, mapped) -> {
                walk(key, seen, found, depth + 1, path + "{key}");
                walk(mapped, seen, found, depth + 1, path + "{value}");
            });
            return;
        }
        if (value.getClass().isArray()) {
            if (!value.getClass().getComponentType().isPrimitive()) {
                for (int i = 0; i < Array.getLength(value); i++) {
                    walk(Array.get(value, i), seen, found, depth + 1, path + "[" + i + "]");
                }
            }
            return;
        }
        if (!value.getClass().getName().startsWith("org.openmarkov")) {
            return;
        }
        for (Field field : fieldsOf(value.getClass())) {
            try {
                field.setAccessible(true);
                walk(field.get(value), seen, found, depth + 1, path + "." + field.getName());
            } catch (RuntimeException | ReflectiveOperationException ignored) {
                // a field this test cannot open is a field it cannot judge
            }
        }
    }

    private List<String> mutableFieldsSharedBetween(Object original, Object copy) {
        List<String> shared = new ArrayList<>();
        for (Field field : fieldsOf(original.getClass())) {
            try {
                field.setAccessible(true);
                Object fromOriginal = field.get(original);
                Object fromCopy = field.get(copy);
                if (fromOriginal != null && fromOriginal == fromCopy && isMutableStructure(fromOriginal)) {
                    shared.add(field.getName());
                }
            } catch (RuntimeException | ReflectiveOperationException ignored) {
                // idem
            }
        }
        return shared;
    }

    private boolean isMutableStructure(Object value) {
        return value.getClass().isArray() || value instanceof Collection<?> || value instanceof Map<?, ?>;
    }

    private List<Field> fieldsOf(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private static final URL DAN_WITH_EVERY_POTENTIAL_URL = Objects.requireNonNull(
            PotentialsCanBeClonedTest.class.getResource("/networks_for_every_potential/Dynamic-LIMID-every-potential.pgmx"));
}
