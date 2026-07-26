package com.hotmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. O ESCUDO QUE DERRUBOU SEU TESTE (Agora desativado)
            .csrf(AbstractHttpConfigurer::disable) 
            
            // 2. Avisa que não vamos usar sessão, apenas o Token JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 
            
            // 3. Nossas regras de rotas
            .authorizeHttpRequests(auth -> auth
                // Rotas Abertas (Cadastro e Login)
                .requestMatchers(HttpMethod.POST, "/usuarios").permitAll() 
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() 
                
                // Rotas do próprio usuário (Basta ter o token)
                .requestMatchers("/usuarios/meus-dados").authenticated()
                
                // Rotas EXCLUSIVAS para ADMIN
                .requestMatchers("/usuarios/**").hasRole("ADMIN") 
                
                // Qualquer outra coisa -> Exige token
                .anyRequest().authenticated()
            )
            // 4. Coloca o nosso Filtro de JWT na frente da porta
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}