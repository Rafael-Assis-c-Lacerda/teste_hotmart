package com.hotmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        // Isso aqui liga o servidor Tomcat, conecta no PostgreSQL, 
        // lê todas as suas anotações e sobe a API. Tudo em uma linha.
        SpringApplication.run(App.class, args);
        System.out.println("API da Hotmart rodando com sucesso!");
    }
}