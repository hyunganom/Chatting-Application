package chat.messageserver.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

/**
 * 캐시 설정을 담당하는 구성 클래스.
 * Redis를 사용하여 애플리케이션의 캐시를 관리함.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final Duration cacheTtl;

    /**
     * 생성자 주입을 통해 의존성과 설정 값을 주입받음.
     *
     * @param redisConnectionFactory Redis 연결 팩토리
     */
    public CacheConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.cacheTtl = Duration.ofHours(1); // 기본 캐시 만료 시간을 1시간으로 설정
    }

    /**
     * RedisTemplate을 빈으로 등록함.
     * 키와 값을 직렬화하는 방식을 설정함.
     *
     * @return 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 키 직렬화기 설정: String 형태로 직렬화
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 값 직렬화기 설정: JSON 형태로 직렬화
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 템플릿 초기화
        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisCacheManager를 빈으로 등록함.
     * 캐시의 기본 설정을 정의하고, Redis와 연동함.
     *
     * @return 설정된 RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager() {
        // 캐시 구성 설정
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheTtl) // 캐시 만료 시간 설정
                .disableCachingNullValues() // null 값을 캐시에 저장하지 않음
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // 키 직렬화 방식 설정
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())); // 값 직렬화 방식 설정

        // 캐시 매니저 빌더 설정
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig) // 기본 캐시 설정 적용
                .build();
    }
}
