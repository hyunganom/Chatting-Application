package chat.messageserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 관련 설정을 담당하는 구성 클래스.
 * Spring Data Redis를 사용하여 RedisTemplate과 RedisCacheManager를 설정함.
 */
@Configuration
public class RedisConfig {

    private final RedisConnectionFactory connectionFactory;

    /**
     * 생성자 주입을 통해 RedisConnectionFactory를 주입받음.
     *
     * @param connectionFactory Redis 연결 팩토리
     */
    public RedisConfig(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * RedisTemplate을 빈으로 등록함.
     * 키와 값의 직렬화 방식을 설정하여 Redis와의 데이터 교환을 관리함.
     *
     * @return 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 키 직렬화기 설정
        template.setKeySerializer(new StringRedisSerializer());
        // 값 직렬화기 설정
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 해시 키 직렬화기 설정
        template.setHashKeySerializer(new StringRedisSerializer());
        // 해시 값 직렬화기 설정
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisCacheManager를 빈으로 등록함.
     * 캐시의 기본 설정을 정의하여 캐시 동작을 관리함.
     *
     * @return 설정된 RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 캐시 만료 시간 설정
                .disableCachingNullValues() // null 값 캐시 비활성화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
