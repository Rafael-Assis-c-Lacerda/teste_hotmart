package com.hotmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        System.out.println("\n=====================================================");
        System.out.println("🔥 API da Hotmart rodando perfeitamente!");
        System.out.println("=====================================================\n");
    }
}