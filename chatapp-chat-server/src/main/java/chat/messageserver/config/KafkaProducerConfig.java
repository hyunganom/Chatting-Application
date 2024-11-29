package chat.messageserver.config;

import chat.messageserver.event.ChatRoomEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
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
    private final boolean addTypeInfoHeaders;

    /**
     * 생성자 주입을 통해 의존성과 설정 값을 주입받음.
     *
     * @param bootstrapServers    Kafka 브로커 주소 (예: localhost:9092)
     * @param addTypeInfoHeaders  JSON 직렬화 시 타입 정보 헤더 추가 여부
     */
    public KafkaProducerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.producer.json.add-type-info-headers:false}") boolean addTypeInfoHeaders) {
        this.bootstrapServers = bootstrapServers;
        this.addTypeInfoHeaders = addTypeInfoHeaders;
    }

    /**
     * Kafka 생산자 팩토리를 빈으로 등록함.
     * 생산자 설정과 직렬화 방식을 정의함.
     *
     * @return 설정된 ProducerFactory
     */
    @Bean
    public ProducerFactory<String, ChatRoomEvent> producerFactory() {
        // 생산자 설정 프로퍼티 정의
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 브로커 주소
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 키 직렬화기 설정
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // 값 직렬화기 설정
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, addTypeInfoHeaders); // 타입 정보 헤더 추가 여부 설정

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate을 빈으로 등록함.
     * KafkaTemplate은 Kafka 메시지를 전송하는 데 사용됨.
     *
     * @return 설정된 KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, ChatRoomEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
