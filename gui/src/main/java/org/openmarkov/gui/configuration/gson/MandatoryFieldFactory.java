package org.openmarkov.gui.configuration.gson;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class MandatoryFieldFactory implements TypeAdapterFactory {
    
    @Override
    public <T> @NotNull TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Only apply validation to your specific package
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        TypeAdapter<JsonElement> jsonElementAdapter = gson.getAdapter(JsonElement.class);
        return new MandatoryFieldVerifier<>(delegate, jsonElementAdapter, type.getRawType());
    }
    
    private static final class MandatoryFieldVerifier<T> extends TypeAdapter<T> {
        
        private final TypeAdapter<T> delegate;
        private final TypeAdapter<? extends JsonElement> jsonElementAdapter;
        private final Class<? super T> rawClass;
        
        private MandatoryFieldVerifier(TypeAdapter<T> delegate, TypeAdapter<? extends JsonElement> jsonElementAdapter, Class<? super T> rawClass) {
            this.delegate = delegate;
            this.jsonElementAdapter = jsonElementAdapter;
            this.rawClass = rawClass;
        }
        
        @Override
        public void write(JsonWriter out, T value) throws IOException {
            this.delegate.write(out, value);
        }
        
        @Override
        public @Nullable T read(JsonReader in) throws IOException {
            JsonElement tree = this.jsonElementAdapter.read(in);
            if (tree.isJsonNull()) return null;
            if (!tree.isJsonObject()) {
                return this.delegate.read(new JsonReader(new StringReader(tree.toString())));
            }
            JsonObject jsonObject = tree.getAsJsonObject();
            // Validate all fields in the class hierarchy
            MandatoryFieldVerifier.validateJsonFields(jsonObject, this.rawClass);
            return this.delegate.fromJsonTree(tree);
        }
        
        private static HashMap<String, Field> nonTransientFieldsByNameOf(Class<?> visitingClass) {
            HashMap<String, Field> fieldsByName = new HashMap<>();
            for (Field field : visitingClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                String fieldName = field.getName();
                if (field.isAnnotationPresent(SerializedName.class)) {
                    fieldName = field.getAnnotation(SerializedName.class).value();
                }
                if (fieldsByName.containsKey(fieldName)) continue;
                fieldsByName.put(fieldName, field);
            }
            new ArrayList<>(fieldsByName.keySet()).forEach(fieldName -> {
                Field field = fieldsByName.get(fieldName);
                if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())) {
                    fieldsByName.remove(fieldName);
                }
            });
            return fieldsByName;
        }
        
        private static void validateJsonFields(JsonObject jsonObject, Class<?> visitingClass) {
            var fieldsByName = MandatoryFieldVerifier.nonTransientFieldsByNameOf(visitingClass);
            var missingFields = fieldsByName.entrySet()
                                            .stream()
                                            .filter(field -> !jsonObject.has(field.getKey()))
                                            .toList();
            if (!missingFields.isEmpty()) {
                var errors = missingFields.stream()
                                          .map(field -> "\t- " + field.getKey() + " of " + field.getValue()
                                                                                                .getDeclaringClass())
                                          .collect(Collectors.joining(System.lineSeparator()));
                throw new JsonParseException("Missing required fields when deserializing " + visitingClass + System.lineSeparator() + errors);
            }
            var wrongFields = jsonObject.asMap()
                                        .keySet()
                                        .stream()
                                        .filter(key -> !fieldsByName.containsKey(key))
                                        .toList();
            if (!wrongFields.isEmpty()) {
                throw new JsonParseException("The field(s) " + wrongFields + " are transient or do not match any existing field in " + visitingClass + " nor the classes it extends");
            }
        }
    }
}