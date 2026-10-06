package org.openmarkov.gui.configuration.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.util.HashMap;
import java.util.Map;

public class SealedTypeAdapterFactory implements TypeAdapterFactory {
    
    private final Class<?> baseClass;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new HashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new HashMap<>();
    
    public SealedTypeAdapterFactory(Class<?> baseClass, String typeFieldName) {
        this.baseClass = baseClass;
        this.typeFieldName = typeFieldName;
    }
    
    public <T> SealedTypeAdapterFactory registerSubtype(Class<? extends T> subtype, String label) {
        labelToSubtype.put(label, subtype);
        subtypeToLabel.put(subtype, label);
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!baseClass.isAssignableFrom(type.getRawType())) {
            return null; // Let Gson handle other types normally
        }
        
        Map<String, TypeAdapter<?>> labelToDelegate = new HashMap<>();
        Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new HashMap<>();
        
        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }
        
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) {
                Class<?> srcType = value.getClass();
                String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked")
                TypeAdapter<T> delegate = (TypeAdapter<T>) subtypeToDelegate.get(srcType);
                
                if (delegate == null) {
                    throw new JsonParseException("Cannot serialize unregistered subtype: " + srcType.getName());
                }
                
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                
                // Add the type property at the top level
                JsonObject clone = new JsonObject();
                clone.addProperty(typeFieldName, label);
                jsonObject.entrySet().forEach(e -> clone.add(e.getKey(), e.getValue()));
                
                gson.toJson(clone, out);
            }
            
            @Override
            public T read(JsonReader in) {
                // Public Gson API to parse JSON element safely from JsonReader
                JsonElement jsonElement = JsonParser.parseReader(in);
                
                if (!jsonElement.isJsonObject()) {
                    throw new JsonParseException("Expected JSON object for " + baseClass.getName());
                }
                
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement typeElement = jsonObject.remove(typeFieldName); // Strip type tag before delegate parsing
                
                if (typeElement == null) {
                    throw new JsonParseException("Missing field '" + typeFieldName + "' in polymorphic payload");
                }
                
                String label = typeElement.getAsString();
                @SuppressWarnings("unchecked")
                TypeAdapter<T> delegate = (TypeAdapter<T>) labelToDelegate.get(label);
                
                if (delegate == null) {
                    throw new JsonParseException("Unknown subtype label: " + label);
                }
                
                return delegate.fromJsonTree(jsonObject);
            }
        }.nullSafe();
    }
}