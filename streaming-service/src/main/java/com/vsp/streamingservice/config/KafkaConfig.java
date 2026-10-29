package com.vsp.streamingservice.config;

import com.vsp.streamingservice.event.VideoEncodedEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, VideoEncodedEvent> consumerFactory(
            KafkaProperties properties) {

        JsonDeserializer<VideoEncodedEvent> deserializer =
                new JsonDeserializer<>(VideoEncodedEvent.class);

        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(null),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoEncodedEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, VideoEncodedEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, VideoEncodedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
