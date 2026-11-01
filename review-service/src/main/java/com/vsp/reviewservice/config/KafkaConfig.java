package com.vsp.reviewservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic ratingUpdateEvent() {
        return TopicBuilder.name("rating.update")
                .partitions(3)
                .replicas(1)
                .build();
    }

}