package com.grimni.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.grimni.util.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // Since we are using a Jwt token, sessions need to be turned off, since Jwt tokens don't rely on sessions like cookies
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // expose endpoint /auth/login - /auth/register - /auth/refresh - /health, as available to all, but any other requests to other endpoints require authentication
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/health",
                        "/swagger-ui/**", "/v3/api-docs/**").permitAll() // for this url endpoint, all requests are permitted
                .anyRequest().authenticated()); // to any other requests to endpoints, authentication is needed

        // since SecurityConfig is initially run before Server Dispatchlet, it blocks due to CORS restriction, therefore we have
        // to define here that our backend can take in requests from the frontend
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of(
                "http://localhost:80",
                "http://localhost:8080",
                "http://localhost:5173",
                "http://127.0.0.1:8080",
                "http://127.0.0.1:5173"
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
            config.setAllowedHeaders(List.of("*"));
            return config;
        }));

        // add jwtAuthFilter class before the UsernamePassword filter to authenticate the user and set it in the SecurityContextHolder
        // in order for the UsernamePassword filter to know the user is authenticated
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
}
