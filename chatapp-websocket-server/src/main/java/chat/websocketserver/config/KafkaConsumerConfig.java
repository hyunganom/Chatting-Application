package chat.websocketserver.config;

import chat.websocketserver.event.ChatRoomEvent;
import chat.websocketserver.event.MessageEvent;
import chat.websocketserver.event.UserPresenceEvent;
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

    // ───────────────────────── 헬퍼 ───────────────────────── //
    private <T> ConsumerFactory<String, T> buildFactory(Class<T> clazz, String groupId) {

        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(clazz);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setUseTypeHeaders(false);      // __TypeId__ 헤더 무시 (value)
        valueDeserializer.setUseTypeMapperForKey(false); // key 쪽도 헤더 무시

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,           groupId);
        // Key 도 ErrorHandling 으로 감싸면 깨진 key 를 안전하게 건너뜁니다.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, valueDeserializer.getClass());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),  // 실제 Key Deserializer
                valueDeserializer          // 실제 Value Deserializer
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T>
    containerFactory(ConsumerFactory<String, T> cf) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
    // ─────────────────────────────────────────────────────── //

    // MessageEvent
    @Bean public ConsumerFactory<String, MessageEvent> messageEventConsumerFactory() {
        return buildFactory(MessageEvent.class, "message_event_group");
    }
    @Bean public ConcurrentKafkaListenerContainerFactory<String, MessageEvent>
    messageEventKafkaListenerContainerFactory() {
        return containerFactory(messageEventConsumerFactory());
    }

    // UserPresenceEvent
    @Bean public ConsumerFactory<String, UserPresenceEvent> userPresenceEventConsumerFactory() {
        return buildFactory(UserPresenceEvent.class, "user_presence_group");
    }
    @Bean public ConcurrentKafkaListenerContainerFactory<String, UserPresenceEvent>
    userPresenceKafkaListenerContainerFactory() {
        return containerFactory(userPresenceEventConsumerFactory());
    }

    // ChatRoomEvent
    @Bean public ConsumerFactory<String, ChatRoomEvent> chatRoomEventConsumerFactory() {
        return buildFactory(ChatRoomEvent.class, "chatroom_event_group");
    }
    @Bean public ConcurrentKafkaListenerContainerFactory<String, ChatRoomEvent>
    chatRoomEventKafkaListenerContainerFactory() {
        return containerFactory(chatRoomEventConsumerFactory());
    }
}
