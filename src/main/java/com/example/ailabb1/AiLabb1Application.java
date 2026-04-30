package com.example.ailabb1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@SpringBootApplication
public class AiLabb1Application {

    public static void main(String[] args) {
        SpringApplication.run(AiLabb1Application.class, args);
    }

}
