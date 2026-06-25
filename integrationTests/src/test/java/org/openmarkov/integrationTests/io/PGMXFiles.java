package org.openmarkov.integrationTests.io;

public class PGMXFiles implements PGMXFilter {

    private final String PGMX_EXTENSION = ".pgmx";

    @Override public boolean meetsCondition(PGMXCompound compound) {
        return compound.getFile().getName().toLowerCase().endsWith(PGMX_EXTENSION);
    }

}
