package com.hotmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. ATIVA O CORS (O Passe Livre do React) e desativa o CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) 
            
            // 2. Avisa que não vamos usar sessão, apenas o Token JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 
            
            // 3. Nossas regras de rotas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/usuarios").permitAll() 
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() 
                
                .requestMatchers("/usuarios/meus-dados").authenticated()
                
                .requestMatchers("/usuarios/**").hasRole("ADMIN") 
                
                .anyRequest().authenticated()
            )
            // 4. Coloca o nosso Filtro de JWT na frente da porta
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 5. AS REGRAS DO PASSE LIVRE: Dizendo exatamente quem pode acessar o backend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Libera o endereço exato do seu React
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // Libera os métodos que o React vai poder usar
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Libera o envio de cabeçalhos (como o nosso Authorization: Bearer)
        configuration.setAllowedHeaders(List.of("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}