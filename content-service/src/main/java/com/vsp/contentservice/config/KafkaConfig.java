package com.vsp.contentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic sendMailTopic(){
        return TopicBuilder.name("send.mail")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
