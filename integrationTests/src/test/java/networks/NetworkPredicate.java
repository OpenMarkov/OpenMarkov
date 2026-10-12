package networks;

import org.openmarkov.core.model.network.type.NetworkType;

public sealed interface NetworkPredicate {
    
    record OfType(NetworkType networkType) implements NetworkPredicate {
    }
    
    record Named(String fileName) implements NetworkPredicate {
    }
    
    static NetworkPredicate.Named named(String fileName) {
        return new NetworkPredicate.Named(fileName);
    }
    
    static NetworkPredicate.OfType ofType(NetworkType networkType) {
        return new NetworkPredicate.OfType(networkType);
    }
    
}
