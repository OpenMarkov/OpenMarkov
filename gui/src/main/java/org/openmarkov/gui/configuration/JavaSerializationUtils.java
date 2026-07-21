package org.openmarkov.gui.configuration;

import java.io.*;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

/**
 * Utility class for Java serialization/deserialization using Base64 encoding,
 * and for verifying the types of elements in collections and maps.
 */
public final class JavaSerializationUtils {

    /**
     * Deserializes an object from a Base64-encoded string using Java serialization.
     *
     * @param string the Base64-encoded serialized object
     * @param <T>    the expected type of the deserialized object
     * @return the deserialized object
     */
    public static <T> T javaDeserialize(String string) {
        byte[] bytes = Base64.getDecoder().decode(string);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Serializes an object to a Base64-encoded string using Java serialization.
     *
     * @param value the object to serialize
     * @param <T>   the type of the object
     * @return the Base64-encoded serialized representation
     */
    public static <T> String javaSerialize(T value) {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos)
        ) {
            out.writeObject(value);
            byte[] objectBytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(objectBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Verifies that all elements in a collection are instances of the specified class.
     *
     * @param collection the object to check (must be a {@link Collection})
     * @param clazz      the expected element type
     * @param <T>        the element type
     * @return {@code true} if the object is a collection and all elements are of type {@code T} or null
     */
    public static <T> boolean verifyCollection(Object collection, Class<T> clazz) {
        if (!(collection instanceof Collection<?> castedCollection)) {
            return false;
        }
        for (var element : castedCollection) {
            var isValid = element == null || clazz.isInstance(element);
            if (!isValid) return false;
        }
        return true;
    }
    
    /**
     * Verifies that all keys and values in a map are instances of the specified classes.
     *
     * @param map    the object to check (must be a {@link Map})
     * @param kClazz the expected key type
     * @param vClazz the expected value type
     * @param <K>    the key type
     * @param <V>    the value type
     * @return {@code true} if the object is a map and all entries match the expected types
     */
    public static <K, V> boolean verifyMap(Object map, Class<K> kClazz, Class<V> vClazz) {
        if (!(map instanceof Map<?, ?> castedMap)) {
            return false;
        }
        for (var entry : castedMap.entrySet()) {
            var isValidKey = entry.getKey() == null || kClazz.isInstance(entry.getKey());
            if (!isValidKey) return false;
            var isValidValue = entry.getValue() == null || vClazz.isInstance(entry.getValue());
            if (!isValidValue) return false;
        }
        return true;
    }
    
}
