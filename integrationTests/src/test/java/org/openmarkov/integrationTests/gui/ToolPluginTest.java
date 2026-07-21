package org.openmarkov.integrationTests.gui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.plugin.PluginSearch;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests to verify classes extending {@link ToolPlugin} only have one constructor, which must receive no arguments.
 * <p>
 * It also checks the constructor can be accessed using reflections, just as
 * {@link org.openmarkov.gui.toolplugin.ToolPluginManager} does to get instances of said {@link ToolPlugin}s.
 *
 * @author jrico
 */
public class ToolPluginTest {
    
    public static Stream<Class<? extends ToolPlugin>> toolPluginClasses() {
        return PluginSearch.init().childrenOf(ToolPlugin.class).stream();
    }
    
    @ParameterizedTest
    @MethodSource("toolPluginClasses")
    public void verifyBounds(Class<ToolPlugin> toolPluginClass) {
        var constructors = Arrays.stream(toolPluginClass.getDeclaredConstructors()).toList();
        var hasNoArgsConstructors = constructors.stream().anyMatch(constructor -> constructor.getParameterCount() == 0);
        var hasConstructorsWithMultipleArgs = constructors.stream()
                                                          .anyMatch(constructor -> constructor.getParameterCount() > 0);
        if (!hasNoArgsConstructors) {
            fail("Class " + toolPluginClass.getName() + " should just have a single constructor with no arguments, but there is no constructor with no arguments");
        }
        if (hasConstructorsWithMultipleArgs) {
            fail("Class " + toolPluginClass.getName() + " should just have a single constructor with no arguments, but there is a constructor with multiple arguments");
        }
        try {
            toolPluginClass.getDeclaredConstructor().setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("The no arguments constructor of class " + toolPluginClass.getName() + " is not accessible, even when forcing accessibility via reflections");
        }
    }
}
