package com.vsp.encodingservice.config;

import com.vsp.encodingservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Safe to disable in token-based microservices
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Strictly enforce that only Admins can add/edit/delete movies
                        // Note: .hasRole("ADMIN") automatically checks for "ROLE_ADMIN" in the authority
                        .requestMatchers(HttpMethod.POST, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/**").hasRole("ADMIN")

                        // Allow any authenticated user (standard or admin) to fetch/view movies
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").authenticated()

                        // Block everything else
                        .anyRequest().authenticated()
                )
                // Insert our custom JWT parsing filter BEFORE the standard Spring Auth filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}