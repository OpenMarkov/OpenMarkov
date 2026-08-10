package org.openmarkov.gui.configuration;

import org.openmarkov.java.io.InputStreamUtils;

import java.util.Objects;

public class CSS {
    
    public static final ThemedItem<String> F1_CSS = new ThemedItem<>(
            InputStreamUtils.read(Objects.requireNonNull(CSS.class.getResourceAsStream("/html/helpStyle.css")))
    ).inDark(
            InputStreamUtils.read(Objects.requireNonNull(CSS.class.getResourceAsStream("/html/helpStyleDark.css")))
    );
    
}
