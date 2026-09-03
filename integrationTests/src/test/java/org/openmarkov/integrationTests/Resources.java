package org.openmarkov.integrationTests;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.integrationTests.staticAnalysis.NoConfusingExceptionsTest;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class Resources {
    
    public static final File RAW_DIR;
    
    static {
        try {
            RAW_DIR = Path.of(Path.of(NoConfusingExceptionsTest.class.getResource("/do_not_delete.txt").toURI())
                                  .toFile()
                                  .getParentFile()
                                  .getPath()
                                  .replace("target\\test-classes", "src\\test\\resources")
                                  .replace("target/test-classes", "src/test/resources"))
                          .toFile();
        } catch (URISyntaxException e) {
            throw new UnreachableException(e);
        }
    }
    
}
