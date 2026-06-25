package org.openmarkov.restTemplate;

import com.google.gson.Gson;
import org.openmarkov.gui.configuration.gson.GsonCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
public class Config {
    
    @Bean
    public static Gson gson() {
        return GsonCommon.GSON;
    }
    
    @Bean
    public static GsonHttpMessageConverter gsonHttpMessageConverter(Gson gson) {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }
}