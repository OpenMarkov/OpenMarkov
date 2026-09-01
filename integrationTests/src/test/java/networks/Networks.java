package networks;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.integrationTests.Resources;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Networks {
    
    private static Path NETWORKS_DIR = Resources.RAW_DIR.toPath()
                                                        .getParent()
                                                        .getParent()
                                                        .getParent()
                                                        .getParent()
                                                        .resolve("0_miscellaneous")
                                                        .resolve("networks");
    
    private static final List<File> NETWORK_FILES;
    
    static {
        try {
            NETWORK_FILES = Files.walk(NETWORKS_DIR)
                                 .map(Path::toFile)
                                 .filter(File::isFile)
                                 .filter(file -> file.getName().endsWith(".pgmx")
                                         || file.getName().endsWith(".xml")
                                         || file.getName().endsWith(".elv"))
                                 .toList();
        } catch (IOException e) {
            throw new UnreachableException(e);
        }
    }
    
    static void main() {
        getNetworks();
    }
    
    public static Stream<URL> getNetworks() {
        return Networks.getNetworks((String) null);
    }
    
    public static Stream<URL> getNetworks(@NotNull NetworkType networkType) {
        return Networks.getNetworks(networkType.codeName());
    }
    
    /**
     * Method to obtain filtered networks in the repository by it network type
     *
     * @param networkFilterType constant to define the filter. Use the static constants defined in this class
     *
     * @return List of filtered url networks
     */
    private static Stream<URL> getNetworks(String networkFilterType) {
        var networks = Networks.NETWORK_FILES.stream();
        if (networkFilterType != null) {
            networks = networks.filter(networkFile -> networkFile.getParentFile()
                                                                 .getName()
                                                                 .equals(networkFilterType));
        }
        return networks.map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new UnreachableException(e);
            }
        });
    }
    
}
