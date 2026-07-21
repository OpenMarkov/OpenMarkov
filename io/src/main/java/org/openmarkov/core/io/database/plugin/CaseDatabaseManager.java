/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.io.database.plugin;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.core.io.exception.NoReaderForExtension;
import org.openmarkov.core.io.exception.NoWriterForExtensionException;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is the manager of the case database formats. Detects the class anotated as CaseDatabaseFormat
 * annotations.
 *
 * @author ibermejo
 * @see org.openmarkov.core.io.format.annotation.FormatType
 */
public class CaseDatabaseManager {
    /**
     * The list of case database reader plugins detected in the project
     */
    private final HashMap<String, Class<? extends CaseDatabaseReader>> readerPlugins;
    /**
     * The list of case database writer plugins detected in the project
     */
    private final HashMap<String, Class<? extends CaseDatabaseWriter>> writerPlugins;
    
    public static CaseDatabaseFormat info(Class<?> readerPlugin) {
        return readerPlugin.getAnnotation(CaseDatabaseFormat.class);
    }
    
    private static CaseDatabaseFormat info(CaseDatabaseWriter readerPlugin) {
        return CaseDatabaseManager.info(readerPlugin.getClass());
    }
    
    private static CaseDatabaseFormat info(CaseDatabaseReader writerPlugin) {
        return CaseDatabaseManager.info(writerPlugin.getClass());
    }
    
    /**
     * Gets a FormatManager instance
     */
    public CaseDatabaseManager() {
        this.readerPlugins = new LinkedHashMap<>();
        this.writerPlugins = new LinkedHashMap<>();
        CaseDatabaseManager.findAllReaderPlugins().forEach(readerPlugin -> {
            CaseDatabaseFormat lAnnotation = readerPlugin.getAnnotation(CaseDatabaseFormat.class);
            readerPlugins.put(lAnnotation.extension(), readerPlugin);
        });
        CaseDatabaseManager.findAllWriterPlugins().forEach(writerPlugin -> {
            CaseDatabaseFormat lAnnotation = writerPlugin.getAnnotation(CaseDatabaseFormat.class);
            writerPlugins.put(lAnnotation.extension(), writerPlugin);
        });
    }
    
    private static @NotNull Stream<Class<? extends CaseDatabaseReader>> findAllReaderPlugins() {
        return PluginSearch.init().annotatedWith(CaseDatabaseFormat.class)
                           .childrenOf(CaseDatabaseReader.class)
                           .stream();
    }
    
    private static @NotNull Stream<Class<? extends CaseDatabaseWriter>> findAllWriterPlugins() {
        return PluginSearch.init().annotatedWith(CaseDatabaseFormat.class)
                           .childrenOf(CaseDatabaseWriter.class)
                           .stream();
    }
    
    /**
     * Gets the writer with the extension
     *
     * @param extension the extension required
     *
     * @return a CaseDatabaseWriter object
     */
    public CaseDatabaseWriter getWriter(String extension) throws NoReaderForExtension {
        try {
            Class<? extends CaseDatabaseWriter> writerClass = this.writerPlugins.get(extension);
            if (writerClass == null) {
                throw new NoReaderForExtension(extension);
            }
            return writerClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * @param extension the extension required
     *
     * @return a CaseDatabaseReader object
     */
    public CaseDatabaseReader getReader(String extension) throws NoWriterForExtensionException {
        try {
            Class<? extends CaseDatabaseReader> readerClass = readerPlugins.get(extension);
            if (readerClass == null) {
                throw new NoWriterForExtensionException(extension);
            }
            return readerClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new UnreachableException(e);
        }
    }
    
    public HashMap<String, String> getAllReaders() {
        HashMap<String, String> readersInfo = new HashMap<>();
        for (String extension : readerPlugins.keySet()) {
            String description = readerPlugins.get(extension).getAnnotation(CaseDatabaseFormat.class).name();
            readersInfo.put(extension, description);
        }
        return readersInfo;
    }
    
    public static List<Class<? extends CaseDatabaseReader>> listReaders() {
        return CaseDatabaseManager.findAllReaderPlugins().toList();
    }
    
    /**
     * Returns a HashMap whose keys are extensions accepted by the writers and
     * whose values are descriptions of the file format written by the writer
     *
     * @return a HashMap whose keys are extensions accepted by the writers and
     * whose values are descriptions of the file format written by the writer
     */
    public HashMap<String, String> getAllWriters() {
        HashMap<String, String> writersInfo = new HashMap<>();
        for (String extension : writerPlugins.keySet()) {
            String description = writerPlugins.get(extension).getAnnotation(CaseDatabaseFormat.class).name();
            writersInfo.put(extension, description);
        }
        return writersInfo;
    }
    
    public static List<Class<? extends CaseDatabaseWriter>> listWriters() {
        return CaseDatabaseManager.findAllWriterPlugins().toList();
    }
    
}
