package org.openmarkov.gui.license;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class License {
    
    public final @NotNull String name;
    public final @NotNull String URL;
    public final @Nullable String distribution;
    public final @Nullable String resource;
    public final @Nullable String comments;
    private LicenseHolder holder;
    
    public License(@NotNull String name, @NotNull String url, @Nullable String distribution, @Nullable String resource, @Nullable String comments) {
        this.name = name;
        this.URL = url;
        this.distribution = distribution;
        this.resource = resource;
        this.comments = comments;
    }
    
    public @NotNull String name() {
        return this.name;
    }
    
    public @NotNull LicenseHolder holder() {
        return this.holder;
    }
    
    public void setHolder(@NotNull LicenseHolder holder) {
        this.holder = holder;
    }
    
    @Override
    public String toString() {
        String toString = "";
        if (this.holder != null) {
            toString += this.holder.descriptor() + " - ";
        }
        toString += this.name + " from " + this.URL;
        return toString;
    }
}
