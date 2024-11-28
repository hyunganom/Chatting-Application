package chat.userserver.config;

import chat.userserver.event.UserEvent;
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
 * Kafka 프로듀서 설정을 담당하는 클래스.
 * @Configuration: 스프링 설정 클래스임을 표시
 */
@Configuration
public class KafkaProducerConfig {

    // application.properties 또는 application.yml에서 설정 값을 주입받음
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka 프로듀서 팩토리를 생성하는 Bean.
     * 프로듀서 설정과 직렬화 방식을 정의.
     * @return ProducerFactory<String, UserEvent> 객체
     */
    @Bean
    public ProducerFactory<String, UserEvent> producerFactory() {
        // 프로듀서 설정 속성 정의
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 서버 주소
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 키 직렬화기 설정 (문자열)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // 값 직렬화기 설정 (JSON)
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); // 타입 정보 헤더 추가 여부 설정

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate을 생성하는 Bean.
     * KafkaTemplate은 메시지를 Kafka로 전송할 때 사용.
     * @return KafkaTemplate<String, UserEvent> 객체
     */
    @Bean
    public KafkaTemplate<String, UserEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
