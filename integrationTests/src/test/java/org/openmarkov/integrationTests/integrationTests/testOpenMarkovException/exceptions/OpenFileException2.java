package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IOpenMarkovException;


@SuppressWarnings("ALL")
public class OpenFileException2 extends Exception implements IOpenMarkovException {
    
    public final @NotNull String fileName;
    public final @Nullable String owner;
    public final @Nullable String permissions;
    
    public OpenFileException2(String fileName, String owner, String permissions) {
        this.fileName = fileName;
        this.owner = owner;
        this.permissions = permissions;
    }
    
    @Override @Nullable public String getExceptionTitle() {
        return "Cannot open file";
    }
    
    @Override @Nullable public String getExceptionMessage() {
        return "Cannot open file: "+this.fileName;
    }
    
    @Override public String toString() {
        return IOpenMarkovException.toString(this);
    }
}

