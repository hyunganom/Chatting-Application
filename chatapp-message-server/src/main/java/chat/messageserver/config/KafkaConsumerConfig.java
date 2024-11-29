package chat.messageserver.config;

import chat.messageserver.event.MessageEvent;
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

/**
 * Kafka 소비자 설정을 담당하는 구성 클래스.
 * Spring Kafka를 사용하여 Kafka 브로커와의 소비자 설정을 관리함.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private final String bootstrapServers;
    private final String groupId;

    /**
     * 생성자 주입을 통해 의존성과 설정 값을 주입받음.
     *
     * @param bootstrapServers Kafka 브로커 주소 (예: localhost:9092)
     * @param groupId          Kafka 소비자 그룹 ID
     */
    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
    }

    /**
     * Kafka 소비자 팩토리를 빈으로 등록함.
     * 소비자 설정과 역직렬화 방식을 정의함.
     *
     * @return 설정된 ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, MessageEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Kafka 브로커 주소 설정
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 소비자 그룹 ID 설정
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // 오프셋 초기화 설정 (earliest: 가장 처음부터 읽기 시작)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ErrorHandlingDeserializer 설정
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // 실제 사용될 Deserializer 클래스 지정
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // JsonDeserializer 설정
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "chat.messageserver.event");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "chat.messageserver.event.MessageEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // 타입 정보 헤더 사용하지 않음

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(MessageEvent.class))
        );
    }

    /**
     * Kafka 리스너 컨테이너 팩토리를 빈으로 등록함.
     * KafkaListener가 메시지를 수신할 때 사용됨.
     *
     * @return 설정된 ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MessageEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
