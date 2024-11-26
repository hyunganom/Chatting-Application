package chat.websocketserver.config;

import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.event.UserPresenceEvent;
import chat.websocketserver.model.Message;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // MessageEvent ConsumerFactory 및 ListenerContainerFactory
    @Bean
    public ConsumerFactory<String, MessageEvent> messageEventConsumerFactory() {
        JsonDeserializer<MessageEvent> deserializer = new JsonDeserializer<>(MessageEvent.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "message_event_group");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageEvent> messageEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MessageEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(messageEventConsumerFactory());
        return factory;
    }

    // UserPresenceEvent ConsumerFactory 및 ListenerContainerFactory
    @Bean
    public ConsumerFactory<String, UserPresenceEvent> userPresenceEventConsumerFactory() {
        JsonDeserializer<UserPresenceEvent> deserializer = new JsonDeserializer<>(UserPresenceEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false); // 헤더에 타입 정보가 없는 경우 기본 타입 사용

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user_presence_group");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserPresenceEvent> userPresenceKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserPresenceEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userPresenceEventConsumerFactory());
        return factory;
    }
}
