package org.openmarkov.restTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
public class Config {

    /**
     * The JSON (de)serializer of this service.
     *
     * <p>This used to be {@code GsonCommon.GSON}, borrowed from the {@code gui} module — which made
     * a web service without windows depend on the whole Swing module for one class. That instance
     * is tuned for the desktop settings file: it carries adapters for {@code File} and
     * {@code Class} fields, which this service does not have, and a factory that rejects any JSON
     * body with a missing or unknown field — which contradicted the controller, whose code handles
     * a missing {@code department} explicitly. A plain Gson is what the controller was written for.
     */
    @Bean
    public static Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setStrictness(Strictness.STRICT)
                .create();
    }

    @Bean
    public static GsonHttpMessageConverter gsonHttpMessageConverter(Gson gson) {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }
}
