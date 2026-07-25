package org.openmarkov.integrationTests.core.model.network;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.exception.CorruptNetworkFile;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What Java asks of any class that redefines equality, checked on one potential of every kind.
 * <p>
 * A class that says which of its instances are equal but does not say how they hash breaks every
 * hash-based collection it is put into: two potentials that are equal land in different buckets and
 * are both kept, so a Set of potentials behaves as a set of distinct objects that happens to answer
 * "yes" when asked whether it contains an equal one. OpenMarkov does keep potentials in such
 * collections - the constant potentials of a network, and the sets that order utility factors during
 * variable elimination - so this is not a theoretical complaint.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PotentialsObeyTheEqualityContractTest {

    Stream<Potential> potentialsOfEveryKind()
            throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        return NetsIO.openNetworkURL(NETWORK_WITH_EVERY_POTENTIAL_URL).probNet()
                     .getNodes().stream().map(Node::getPotential);
    }

    /**
     * Equality and hashing are one decision, not two: redefining the first and leaving the second as
     * Object's means the class says two objects are the same while every hash-based collection it
     * meets says they are not.
     * <p>
     * The rule is not "each class that declares equals declares hashCode". Inheriting a hash from a
     * superclass is right whenever the subclass only makes equality <em>stricter</em>, which is what
     * TablePotential does by also comparing its values: fewer things are equal, and the ones that
     * still are hash alike. What must not happen is hashing by identity while equating by value.
     */
    @ParameterizedTest
    @MethodSource("potentialsOfEveryKind")
    void noPotentialEquatesByValueWhileHashingByIdentity(Potential potential) throws NoSuchMethodException {
        Class<?> whereEqualsComesFrom = potential.getClass().getMethod("equals", Object.class).getDeclaringClass();
        Class<?> whereHashComesFrom = potential.getClass().getMethod("hashCode").getDeclaringClass();

        assertThat(whereEqualsComesFrom).isNotEqualTo(Object.class);
        assertThat(whereHashComesFrom)
                .as("%s redefines equals in %s but still hashes as Object does",
                    potential.getClass().getSimpleName(), whereEqualsComesFrom.getSimpleName())
                .isNotEqualTo(Object.class);
    }

    /**
     * Why the hash of a potential must not depend on its values, stated as a check rather than as a
     * comment someone can delete. The values of a table are edited in place all over the code - every
     * projection, every normalization writes into the array a potential already holds. A hash that
     * moved with them would leave the potential stranded in the bucket its old hash chose, still in
     * the collection but invisible to it.
     */
    @ParameterizedTest
    @MethodSource("potentialsOfEveryKind")
    void theHashOfATableDoesNotMoveWhenItsValuesAreEdited(Potential potential) {
        if (!(potential instanceof TablePotential table) || table.getValues().length == 0) {
            return;
        }
        int hashBefore = table.hashCode();

        table.getValues()[0] = table.getValues()[0] + 0.5;

        assertThat(table.hashCode())
                .as("editing the values of a %s moved its hash", potential.getClass().getSimpleName())
                .isEqualTo(hashBefore);
    }

    /** Nothing equals null, and asking is not an error - it is how collections probe for absence. */
    @ParameterizedTest
    @MethodSource("potentialsOfEveryKind")
    void aPotentialIsNotEqualToNull(Potential potential) {
        assertThat(potential.equals(null)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("potentialsOfEveryKind")
    void aPotentialIsEqualToItselfAndHashesTheSameEveryTime(Potential potential) {
        assertThat(potential).isEqualTo(potential);
        assertThat(potential.hashCode()).isEqualTo(potential.hashCode());
    }

    /**
     * The rule that matters: whatever a potential considers equal to itself must hash the same. A copy
     * is the one thing every potential can be compared against without building a network by hand.
     */
    @ParameterizedTest
    @MethodSource("potentialsOfEveryKind")
    void aPotentialAndItsCopyAgreeOnEqualityAndOnHashing(Potential potential) {
        Potential copy = potential.copy();

        assertThat(copy)
                .as("a copy of %s is not equal to the potential it was copied from",
                    potential.getClass().getSimpleName())
                .isEqualTo(potential);
        assertThat(copy.hashCode())
                .as("%s and its copy are equal but hash differently", potential.getClass().getSimpleName())
                .isEqualTo(potential.hashCode());
    }

    private static final URL NETWORK_WITH_EVERY_POTENTIAL_URL = Objects.requireNonNull(
            PotentialsObeyTheEqualityContractTest.class.getResource(
                    "/networks_for_every_potential/Dynamic-LIMID-every-potential.pgmx"));
}
