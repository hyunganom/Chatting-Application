package chat.userserver.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 캐싱을 위한 설정 클래스
 * @Configuration: 이 클래스가 Spring 설정 클래스를 나타
 * @EnableCaching: Spring의 캐싱 기능을 활성화
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis 캐시의 기본 설정을 정의하는 Bean
     * @return RedisCacheConfiguration 객체
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        // 기본 캐시 구성 가져오기
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 캐시 유효 시간 설정 (10분)
                .entryTtl(Duration.ofMinutes(10))
                // null 값을 캐시하지 않도록 설정
                .disableCachingNullValues()
                // 키 직렬화 방식 설정 (문자열 직렬화)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                // 값 직렬화 방식 설정 (JSON 직렬화)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return config;
    }

    /**
     * RedisTemplate을 정의하는 Bean입니다.
     * RedisTemplate은 Redis와의 상호작용을 돕는 템플릿 클래스
     * @param connectionFactory RedisConnectionFactory 객체
     * @return RedisTemplate<String, Object> 객체
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // RedisTemplate 인스턴스 생성
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Redis 연결 설정
        template.setConnectionFactory(connectionFactory);

        // 키 직렬화기 설정 (문자열 직렬화)
        template.setKeySerializer(new StringRedisSerializer());

        // 값 직렬화기 설정 (JSON 직렬화)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 해시 키 직렬화기 설정 (문자열 직렬화)
        template.setHashKeySerializer(new StringRedisSerializer());

        // 해시 값 직렬화기 설정 (JSON 직렬화)
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 템플릿 초기화
        template.afterPropertiesSet();

        return template;
    }
}
