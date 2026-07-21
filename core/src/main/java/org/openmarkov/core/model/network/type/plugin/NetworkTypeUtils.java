/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Iñigo
 */
public class NetworkTypeUtils {
    
    public static final List<Class<? extends NetworkType>> NETWORK_TYPE_CLASSES
            = NetworkTypeUtils.findAllNetworkTypes().toList();
    
    public static NetworkTypeInfo getInfo(Class<? extends NetworkType> networkClass) {
        return networkClass.getAnnotation(NetworkTypeInfo.class);
    }
    
    public static NetworkType safeInstanciate(Class<? extends NetworkType> networkClass) {
        try {
            return (NetworkType) networkClass.getMethod("getUniqueInstance").invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 NoSuchMethodException | SecurityException e) {
            throw new UnreachableException(e);
        }
    }
    
    public static @Nullable Class<? extends NetworkType> getNetworkClassByName(String name) {
        Class<? extends NetworkType> networkClass = NetworkTypeUtils.NETWORK_TYPE_CLASSES
                .stream()
                .filter(networkType -> NetworkTypeUtils.getInfo(networkType).name().equals(name))
                .findFirst()
                .orElse(null);
        if (networkClass == null) {
            networkClass = NetworkTypeUtils.NETWORK_TYPE_CLASSES
                    .stream()
                    .filter(networkType -> Arrays.stream(
                                                         NetworkTypeUtils.getInfo(networkType).alternativeNames())
                                                 .anyMatch(altName -> altName.equals(name)))
                    .findFirst()
                    .orElse(null);
        }
        return networkClass;
    }
    
    /**
     * This method gets all the plugins with NetworkTypeInfo annotations
     *
     * @return a list with the plugins detected with NetworkTypeInfo annotations.
     */
    private static @NotNull Stream<Class<? extends NetworkType>> findAllNetworkTypes() {
        return PluginSearch.init()
                           .annotatedWith(NetworkTypeInfo.class)
                           .childrenOf(NetworkType.class)
                           .stream();
    }
}