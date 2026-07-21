package org.openmarkov.safeCloning;

import java.io.*;

public interface AutoCloneable extends Serializable {
    
    
    static <T extends AutoCloneable> T safeClone(AutoCloneable autoCloneable) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Write the object to a byte array
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(autoCloneable);
            oos.flush();
            
            // Read a new object from the byte array
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
            
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
