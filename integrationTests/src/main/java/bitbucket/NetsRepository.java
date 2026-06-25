/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package bitbucket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.LIMIDType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.model.network.type.POMDPType;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class gets the networks available on the bitbucket repository
 *
 * @author Jorge Pérez Martín
 */
public class NetsRepository {
    
    private static final List<NetsCache.BitbucketFile> NETWORK_FILES = NetsCache.resolveCache().toList();
    
    /**
     * Method to obtain the complete list of URL of all networks in the repository
     *
     * @return URL of the networks
     */
    public static List<URL> getNetworks() {
        return NetsRepository.getNetworks((String) null);
    }
    
    /**
     * Method to obtain filtered networks in the repository by it network type
     *
     * @param networkType NetWorkType of the net
     *
     * @return List of filtered url networks
     */
    public static List<URL> getNetworks(NetworkType networkType) {
        String networksDirName = switch (networkType) {
            case BayesianNetworkType ignored -> "bn";
            case DecisionAnalysisNetworkType ignored -> "dan";
            case InfluenceDiagramType ignored -> "id";
            case LIMIDType ignored -> "limids";
            case MIDType ignored -> "mid";
            case POMDPType ignored -> "pomdp";
            default -> null;
        };
        if (networksDirName == null) {
            return null;
        }
        return getNetworks(networksDirName);
    }
    
    /**
     * Method to obtain filtered networks in the repository by it network type
     *
     * @param networkFilterType constant to define the filter. Use the static constants defined in this class
     *
     * @return List of filtered url networks
     */
    private static List<URL> getNetworks(String networkFilterType) {
        var networks = NETWORK_FILES.stream().filter(jsonFileRead
                                                             -> jsonFileRead.bitbucketFileRef()
                                                                            .relativePath()
                                                                            .getLast()
                                                                            .endsWith(".pgmx"));

        if (networkFilterType != null) {
            networks = networks.filter(jsonFileRead -> jsonFileRead.bitbucketFileRef().relativePath()
                                                                   .getFirst()
                                                                   .equals(networkFilterType));
        }
        return new ArrayList<>(networks.map(NetsCache.BitbucketFile::resolveURL).toList());
        
        /*
        try {
            var networks = BitbucketApi.streamOfBitbucketFiles("cisiad/org.probmodelxml.networks", "master")
                        .filter(jsonFileRead -> jsonFileRead.relativePath()
                                                            .getLast()
                                                            .endsWith(".pgmx"));
            if (networkFilterType != null) {
                networks = networks.filter(jsonFileRead -> jsonFileRead.relativePath()
                                                                       .getFirst()
                                                                       .equals(networkFilterType));
            }
            return new ArrayList<>(networks.map(BitbucketApi.BitbucketFileRef::href).toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
        
    }
    
    
}
