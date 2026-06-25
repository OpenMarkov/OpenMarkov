package org.openmarkov.gui.configuration.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;

public class GsonCommon {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setStrictness(Strictness.STRICT)
            .registerTypeAdapterFactory(new MandatoryFieldFactory())
            .registerTypeAdapter(java.io.File.class, new GsonAdapters.FileAdapter())
            .registerTypeHierarchyAdapter(Class.class, new GsonAdapters.ClassTypeAdapter())
            .create();
}
