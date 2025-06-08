package chat.websocketserver.config;

import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.event.UserPresenceEvent;
import chat.websocketserver.model.Message;
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

    @Bean
    public ProducerFactory<String, MessageEvent> messageEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Configure the JsonSerializer
        JsonSerializer<MessageEvent> jsonSerializer = new JsonSerializer<>();
        jsonSerializer.setAddTypeInfo(true);

        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
    }

    @Bean(name = "messageEventKafkaTemplate")
    public KafkaTemplate<String, MessageEvent> messageEventKafkaTemplate() {
        return new KafkaTemplate<>(messageEventProducerFactory());
    }

    // ProducerFactory and KafkaTemplate for UserPresenceEvent
    @Bean
    public ProducerFactory<String, UserPresenceEvent> userPresenceEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Configure the JsonSerializer
        JsonSerializer<UserPresenceEvent> jsonSerializer = new JsonSerializer<>();
        jsonSerializer.setAddTypeInfo(true); // 타입 정보를 포함하도록 설정

        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
    }

    @Bean(name = "userPresenceEventKafkaTemplate")
    public KafkaTemplate<String, UserPresenceEvent> userPresenceEventKafkaTemplate() {
        return new KafkaTemplate<>(userPresenceEventProducerFactory());
    }
}
