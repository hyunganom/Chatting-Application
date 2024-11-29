package chat.messageserver.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@Configuration
public class KafkaConsumerConfig {

    private final String bootstrapServers;
    private final String groupId;
    private final String trustedPackages;

    /**
     * 생성자 주입을 통해 의존성과 설정 값을 주입받음.
     *
     * @param bootstrapServers Kafka 브로커 주소 (예: localhost:9092)
     * @param groupId          소비자 그룹 ID
     * @param trustedPackages  신뢰할 수 있는 패키지 목록 (JSON deserialization 시 보안 강화)
     */
    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.trusted-packages}") String trustedPackages) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.trustedPackages = trustedPackages;
    }

    /**
     * Kafka 소비자 팩토리를 빈으로 등록함.
     * 소비자 설정과 직렬화 방식을 정의함.
     *
     * @return 설정된 ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        // JSON deserializer 설정
        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(trustedPackages); // 신뢰할 수 있는 패키지 명시

        // 에러 핸들링 deserializer 설정
        ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        // 소비자 설정 프로퍼티 정의
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 브로커 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // 소비자 그룹 ID
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 오프셋 초기화 정책

        // 추가적인 소비자 설정을 필요에 따라 여기에 추가할 수 있음
        // 예: props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(), // 키 deserializer
                errorHandlingDeserializer // 값 deserializer
        );
    }

    /**
     * Kafka 리스너 컨테이너 팩토리를 빈으로 등록함.
     * Kafka 리스너의 병렬 처리와 소비자 팩토리를 설정함.
     *
     * @return 설정된 ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 필요에 따라 병렬 처리 수 등 추가 설정 가능
        // factory.setConcurrency(3);
        return factory;
    }
}
