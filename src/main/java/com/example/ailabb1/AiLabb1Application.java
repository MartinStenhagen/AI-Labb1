package com.example.ailabb1;

import com.example.ailabb1.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@EnableConfigurationProperties(AiProperties.class)
@SpringBootApplication
public class AiLabb1Application {

    public static void main(String[] args) {
        SpringApplication.run(AiLabb1Application.class, args);
    }

}
