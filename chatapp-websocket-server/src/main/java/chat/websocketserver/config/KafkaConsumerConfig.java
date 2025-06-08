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

    // ────────────── 공통 유틸 ────────────── //
    private <T> ConsumerFactory<String, T> buildFactory(Class<T> clazz, String groupId) {
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(clazz);
        jsonDeserializer.addTrustedPackages("*");      // 패키지 신뢰
        jsonDeserializer.setUseTypeMapperForKey(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildContainerFactory(ConsumerFactory<String, T> cf) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // ────────────── MessageEvent ────────────── //
    @Bean
    public ConsumerFactory<String, MessageEvent> messageEventConsumerFactory() {
        return buildFactory(MessageEvent.class, "message_event_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageEvent> messageEventKafkaListenerContainerFactory() {
        return buildContainerFactory(messageEventConsumerFactory());
    }

    // ────────────── UserPresenceEvent ────────────── //
    @Bean
    public ConsumerFactory<String, UserPresenceEvent> userPresenceEventConsumerFactory() {
        return buildFactory(UserPresenceEvent.class, "user_presence_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserPresenceEvent> userPresenceKafkaListenerContainerFactory() {
        return buildContainerFactory(userPresenceEventConsumerFactory());
    }

    // ────────────── ChatRoomEvent ────────────── //
    @Bean
    public ConsumerFactory<String, ChatRoomEvent> chatRoomEventConsumerFactory() {
        return buildFactory(ChatRoomEvent.class, "chatroom_event_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatRoomEvent> chatRoomEventKafkaListenerContainerFactory() {
        return buildContainerFactory(chatRoomEventConsumerFactory());
    }
}
