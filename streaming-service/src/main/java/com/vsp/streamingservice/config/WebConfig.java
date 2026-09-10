package com.vsp.streamingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestClient contentServiceRestClient(
            @Value("${content-service.base-url}") String baseUrl) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}