package chat.userserver.config;

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
 * Kafka 소비자 설정을 담당하는 클래스.
 * @EnableKafka: Kafka 관련 기능을 활성화
 * @Configuration: 스프링 설정 클래스임을 표시
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // application.properties 또는 application.yml에서 설정 값을 주입받음
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Kafka 소비자 팩토리를 생성하는 Bean.
     * 소비자 설정과 직렬화 방식을 정의.
     * @return ConsumerFactory<String, Object> 객체
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        // JSON 디시리얼라이저 설정
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
        deserializer.addTrustedPackages("*"); // 모든 패키지 신뢰 (보안상 특정 패키지로 제한 가능)

        // 소비자 설정 속성 정의
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 서버 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // 소비자 그룹 ID
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음부터 메시지 읽기

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(), // 키를 문자열로 디시리얼라이즈
                new ErrorHandlingDeserializer<>(deserializer) // 값 디시리얼라이저에 에러 핸들링 추가
        );
    }

    /**
     * Kafka 리스너 컨테이너 팩토리를 생성하는 Bean.
     * 리스너가 Kafka 메시지를 처리할 때 사용하는 설정을 정의.
     * @return ConcurrentKafkaListenerContainerFactory<String, Object> 객체
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        // 리스너 컨테이너 팩토리 인스턴스 생성
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory()); // 앞서 정의한 소비자 팩토리 설정
        return factory;
    }
}
