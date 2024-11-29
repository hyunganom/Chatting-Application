package chat.messageserver.config;

import chat.messageserver.event.MessageEvent;
import chat.messageserver.event.UserPresenceEvent;
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

/**
 * Kafka 생산자 설정을 담당하는 구성 클래스.
 * Spring Kafka를 사용하여 Kafka 브로커와의 생산자 설정을 관리함.
 */
@Configuration
public class KafkaProducerConfig {

    private final String bootstrapServers;

    /**
     * 생성자 주입을 통해 의존성과 설정 값을 주입받음.
     *
     * @param bootstrapServers Kafka 브로커 주소 (예: localhost:9092)
     */
    public KafkaProducerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * MessageEvent용 Kafka 생산자 팩토리를 빈으로 등록함.
     *
     * @return 설정된 ProducerFactory
     */
    @Bean
    public ProducerFactory<String, MessageEvent> messageEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka 브로커 주소 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 키 직렬화기 설정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 값 직렬화기 설정
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // 타입 정보 헤더 추가하지 않음
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * MessageEvent용 KafkaTemplate을 빈으로 등록함.
     *
     * @return 설정된 KafkaTemplate
     */
    @Bean(name = "messageEventKafkaTemplate")
    public KafkaTemplate<String, MessageEvent> messageEventKafkaTemplate() {
        return new KafkaTemplate<>(messageEventProducerFactory());
    }

    /**
     * UserPresenceEvent용 Kafka 생산자 팩토리를 빈으로 등록함.
     *
     * @return 설정된 ProducerFactory
     */
    @Bean
    public ProducerFactory<String, UserPresenceEvent> userPresenceEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka 브로커 주소 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 키 직렬화기 설정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 값 직렬화기 설정
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // 타입 정보 헤더 추가하지 않음
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * UserPresenceEvent용 KafkaTemplate을 빈으로 등록함.
     *
     * @return 설정된 KafkaTemplate
     */
    @Bean(name = "userPresenceEventKafkaTemplate")
    public KafkaTemplate<String, UserPresenceEvent> userPresenceEventKafkaTemplate() {
        return new KafkaTemplate<>(userPresenceEventProducerFactory());
    }
}
