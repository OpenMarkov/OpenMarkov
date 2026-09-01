package org.openmarkov.java.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class InputStreamUtils {
    
    /// Reads an InputStream assuming the inputStream is not closed and not null. This is especially useful for loading
    ///  resources, as these conditions are always met.
    public static String read(@NotNull java.io.InputStream inputStream) {
        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}
