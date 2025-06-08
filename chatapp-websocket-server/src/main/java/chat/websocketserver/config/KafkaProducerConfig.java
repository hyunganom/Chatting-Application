// src/main/java/chat/websocketserver/config/KafkaProducerConfig.java
package chat.websocketserver.config;

import chat.websocketserver.event.ChatRoomEvent;
import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.event.UserPresenceEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private <T> ProducerFactory<String, T> buildFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // JSON 직렬화 시 __TypeId__ 헤더를 **포함하지 않도록** 설정
        JsonSerializer<T> serializer = new JsonSerializer<>();
        serializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), serializer);
    }

    @Bean(name = "messageEventKafkaTemplate")
    public KafkaTemplate<String, MessageEvent> messageEventKafkaTemplate() {
        return new KafkaTemplate<>(buildFactory());
    }

    @Bean(name = "userPresenceEventKafkaTemplate")
    public KafkaTemplate<String, UserPresenceEvent> userPresenceEventKafkaTemplate() {
        return new KafkaTemplate<>(buildFactory());
    }

    @Bean(name = "chatRoomEventKafkaTemplate")
    public KafkaTemplate<String, ChatRoomEvent> chatRoomEventKafkaTemplate() {
        return new KafkaTemplate<>(buildFactory());
    }
}
