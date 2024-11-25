package chat.messageserver.config;

import chat.messageserver.event.MessageEvent;
import chat.messageserver.event.UserPresenceEvent;
import chat.messageserver.model.Message;
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

    // MessageEvent ProducerFactory 및 KafkaTemplate
    @Bean
    public ProducerFactory<String, MessageEvent> messageEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 타입 정보를 포함하지 않도록 설정
        JsonSerializer<MessageEvent> jsonSerializer = new JsonSerializer<>();
        jsonSerializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                jsonSerializer);
    }

    @Bean(name = "messageEventKafkaTemplate")
    public KafkaTemplate<String, MessageEvent> messageEventKafkaTemplate() {
        return new KafkaTemplate<>(messageEventProducerFactory());
    }

    // UserPresenceEvent ProducerFactory 및 KafkaTemplate (기존 설정 유지)
    @Bean
    public ProducerFactory<String, UserPresenceEvent> userPresenceEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        JsonSerializer<UserPresenceEvent> jsonSerializer = new JsonSerializer<>();
        jsonSerializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                jsonSerializer);
    }

    @Bean(name = "userPresenceEventKafkaTemplate")
    public KafkaTemplate<String, UserPresenceEvent> userPresenceEventKafkaTemplate() {
        return new KafkaTemplate<>(userPresenceEventProducerFactory());
    }
}
