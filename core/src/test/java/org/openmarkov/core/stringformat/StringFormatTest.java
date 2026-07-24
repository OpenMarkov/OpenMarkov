package org.openmarkov.core.stringformat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author jrico
 */
@SuppressWarnings({"ConstantExpression", "DuplicateStringLiteralInspection"})
class StringFormatTest {
    
    private static final String TEST_PATTERN = "The {probNetWrapper.getNet.getName} net created at {CreationDate, date, short} is not a " +
            "{DesiredNetType} network, and this functionality is only available for {DesiredNetType}";
    
    private static final String EXPECTED = "The MyNet net created at 3/6/23 is not a " +
            "Bayesian network, and this functionality is only available for Bayesian";
    
    private static final Locale MESSAGE_LOCALE = new Locale("es");
    
    /**
     * Tests the generated String is the same as {@link StringFormatTest#EXPECTED}.
     */
    @Tag(TestConfig.DisabledInParallel)
    @Test final void testFields() {
        Map<String, Object> values = new HashMap<>();
        values.put("probNetWrapper", new ProbNetWrapper("MyNet"));
        values.put("CreationDate", java.util.Date.from(Instant.parse("2023-06-03T10:15:30.00Z")));
        values.put("DesiredNetType", BayesianNetworkType.getUniqueInstance());
        
        Locale.setDefault(MESSAGE_LOCALE);
        String formatedMessage = StringFormat.apply(StringFormatTest.TEST_PATTERN, values);
        Assertions.assertEquals(StringFormatTest.EXPECTED, formatedMessage);
    }
    
    /**
     * B1: the pattern tolerates spaces around the name of the placeholder on purpose, so a name with
     * them must resolve exactly like one without.
     */
    @Test final void aNameSurroundedBySpacesResolvesLikeOneWithout() {
        Map<String, Object> values = Map.of("NetName", "MyNet");

        Assertions.assertEquals("The MyNet net", StringFormat.apply("The { NetName } net", values));
        Assertions.assertEquals(StringFormat.apply("The {NetName} net", values),
                                StringFormat.apply("The {  NetName  } net", values));
    }

    /**
     * B1: the names extracted from a pattern must be the keys the arguments are looked up by, without
     * the surrounding spaces.
     */
    @Test final void extractedParameterNamesCarryNoSpaces() {
        Assertions.assertEquals(List.of("NetName", "Other"),
                                StringFormat.extractParameterNames("The { NetName } and the {Other}"));
    }

    /**
     * A method written with its empty parentheses, as a person naturally writes it, must resolve like
     * one written without them. Two placeholders of a real message —
     * {@code core_class_localizations_en.xml}, the potential that cannot be converted to a table —
     * were written that way and did not resolve at all: the user saw the braces.
     */
    @Test final void aMethodWrittenWithParenthesesResolvesLikeOneWithout() {
        Map<String, Object> values = Map.of("probNetWrapper", new ProbNetWrapper("MyNet"));

        Assertions.assertEquals("MyNet", StringFormat.apply("{probNetWrapper.getNet.getName()}", values));
        Assertions.assertEquals(StringFormat.apply("{probNetWrapper.getNet.getName}", values),
                                StringFormat.apply("{probNetWrapper.getNet.getName( )}", values));
    }

    /** B2: an array holding a null element must be rendered, not throw. */
    @Test final void anArrayWithANullElementIsRendered() {
        Map<String, Object> values = new HashMap<>();
        values.put("names", new String[] { "a", null, "b" });

        Assertions.assertEquals("The [a, null, b] nodes", StringFormat.apply("The {names} nodes", values));
    }

    /** B2: the same, for an array nested in another one. */
    @Test final void aNestedArrayWithANullElementIsRendered() {
        Map<String, Object> values = new HashMap<>();
        values.put("names", new String[][] { { "a", null }, { "b" } });

        Assertions.assertEquals("The [a, null, b] nodes", StringFormat.apply("The {names} nodes", values));
    }

    @Test final void aCollectionIsRenderedAsAnInlineList() {
        Map<String, Object> values = Map.of("names", List.of("a", "b", "c"));

        Assertions.assertEquals("The [a, b, c] nodes", StringFormat.apply("The {names} nodes", values));
    }

    @Test final void aMapIsRenderedAsAnInlineListOfPairs() {
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("a", 1);
        counts.put("b", 2);

        Assertions.assertEquals("The [a: 1, b: 2] counts",
                                StringFormat.apply("The {counts} counts", Map.of("counts", counts)));
    }

    @Test final void anArgumentThatWasNotGivenIsMarkedAsUnknown() {
        Assertions.assertEquals("The >>> NetName <<< net", StringFormat.apply("The {NetName} net", Map.of()));
    }

    @Test final void anArgumentThatIsNullSaysSo() {
        Map<String, Object> values = new HashMap<>();
        values.put("NetName", null);

        Assertions.assertEquals("The >>> NetName is null <<< net", StringFormat.apply("The {NetName} net", values));
    }

    @Test final void aMethodThatCannotBeResolvedIsReported() {
        Map<String, Object> values = Map.of("probNetWrapper", new ProbNetWrapper("MyNet"));

        Assertions.assertEquals(">>> Cannot resolve probNetWrapper.noSuchThing <<<",
                                StringFormat.apply("{probNetWrapper.noSuchThing}", values));
    }

    /** A format that cannot be applied to the argument must not abort the message: it falls back to its text. */
    @Test final void aFormatThatDoesNotFitTheArgumentFallsBackToItsText() {
        Assertions.assertEquals("abc", StringFormat.apply("{x, number}", Map.of("x", "abc")));
    }

    static class ProbNetWrapper{
        final ProbNet net;
        
        public ProbNetWrapper(String probNetName) {
            this.net = new ProbNet();
            this.net.setName(probNetName);
        }
        
        public ProbNet getNet() {
            return net;
        }
    }
    
}