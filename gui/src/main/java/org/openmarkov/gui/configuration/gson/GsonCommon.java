package org.openmarkov.gui.configuration.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import org.openmarkov.gui.bindings.Input;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.io.File;

public class GsonCommon {
    
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setStrictness(Strictness.STRICT)
            .serializeNulls()
            .registerTypeAdapterFactory(new MandatoryFieldFactory())
            .registerTypeAdapter(File.class, new GsonAdapters.FileAdapter())
            .registerTypeHierarchyAdapter(Class.class, new GsonAdapters.ClassTypeAdapter())
            .registerTypeAdapterFactory(new SealedTypeAdapterFactory(Input.class, "type")
                                                .registerSubtype(Input.Key.class, "key")
                                                .registerSubtype(Input.Click.class, "click")
                                                .registerSubtype(Input.MouseWheel.class, "mouse_wheel"))
            .registerTypeAdapterFactory(new SealedTypeAdapterFactory(NetworkEditorPanel.NetworkSource.class, "type")
                                                .registerSubtype(NetworkEditorPanel.NetworkSource.SourceFile.class, "file")
                                                .registerSubtype(NetworkEditorPanel.NetworkSource.SourceURL.class, "url"))
            .enableComplexMapKeySerialization()
            .create();
}
