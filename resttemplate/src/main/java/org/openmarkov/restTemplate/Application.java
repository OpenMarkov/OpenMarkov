package org.openmarkov.restTemplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        
        var res = Config.gson().fromJson("{\n" +
                                                 "    \"name\": \"Jorge\",\n" +
                                                 "    \"status\": \"Success\",\n" +
                                                 "    \"department\": {\n" +
                                                 "        \"name\": \"IT\",\n" +
                                                 "        \"number\": 15\n" +
                                                 "    }\n" +
                                                 "}", ExampleData.class);
        System.out.println(res);
        SpringApplication.run(Application.class, args);
    }
    
}