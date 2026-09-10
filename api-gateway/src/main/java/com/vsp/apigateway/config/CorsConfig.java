package com.vsp.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow your Vite React frontend origin
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        
        // Allow all standard HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers (including Authorization for your JWT Bearer token)
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (headers, authorization tokens)
        corsConfig.setAllowCredentials(true);
        
        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}