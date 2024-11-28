package chat.userserver.service;

import chat.userserver.exception.SessionCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 세션 관리를 담당하는 서비스 클래스.
 * Redis를 사용하여 세션 데이터를 저장하고 관리함.
 */
@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    private final long sessionExpiry; // 세션 만료 시간 (분)

    /**
     * 생성자 주입을 통해 의존성과 설정 값을 주입받음.
     *
     * @param redisTemplate Redis 템플릿
     * @param sessionExpiry 세션 만료 시간 (분)
     */
    public SessionService(RedisTemplate<String, Object> redisTemplate,
                          @Value("${session.expiry.minutes:30}") long sessionExpiry) {
        this.redisTemplate = redisTemplate;
        this.sessionExpiry = sessionExpiry;
    }

    /**
     * 새로운 세션을 생성하고 Redis에 저장함.
     *
     * @param userId   사용자 ID
     * @param username 사용자 이름
     * @return 생성된 세션 ID
     */
    public String createSession(Long userId, String username) {
        String sessionId = UUID.randomUUID().toString();
        Map<String, Object> sessionData = Map.of(
                "userId", userId,
                "username", username
        );

        try {
            redisTemplate.opsForHash().putAll(sessionId, sessionData);
            redisTemplate.expire(sessionId, sessionExpiry, TimeUnit.MINUTES);
            logger.info("세션 생성됨: 사용자={}, 세션ID={}", username, sessionId);
        } catch (Exception e) {
            logger.error("세션 생성 실패: 사용자={}, 에러={}", username, e.getMessage());
            throw new SessionCreationException("세션 생성에 실패했어.");
        }

        return sessionId;
    }

    /**
     * 세션 ID로부터 사용자 이름을 조회함.
     *
     * @param sessionId 세션 ID
     * @return 사용자 이름 또는 null
     */
    public String getUsernameFromSession(String sessionId) {
        try {
            Object usernameObj = redisTemplate.opsForHash().get(sessionId, "username");
            if (usernameObj instanceof String) {
                String username = (String) usernameObj;
                // 세션 만료 시간 갱신
                redisTemplate.expire(sessionId, sessionExpiry, TimeUnit.MINUTES);
                logger.info("세션 조회됨: 세션ID={}, 사용자명={}", sessionId, username);
                return username;
            } else {
                logger.warn("세션을 찾을 수 없거나 만료됨: 세션ID={}", sessionId);
                return null;
            }
        } catch (Exception e) {
            logger.error("세션 조회 실패: 세션ID={}, 에러={}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * 세션 ID로부터 사용자 ID를 조회함.
     *
     * @param sessionId 세션 ID
     * @return 사용자 ID 또는 null
     */
    public Long getUserIdBySession(String sessionId) {
        try {
            Object userIdObj = redisTemplate.opsForHash().get(sessionId, "userId");
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                // 세션 만료 시간 갱신
                redisTemplate.expire(sessionId, sessionExpiry, TimeUnit.MINUTES);
                logger.info("세션 조회됨: 세션ID={}, 사용자ID={}", sessionId, userId);
                return userId;
            } else if (userIdObj instanceof Integer) { // Redis에서 숫자를 Integer로 반환할 경우 대비
                Long userId = ((Integer) userIdObj).longValue();
                // 세션 만료 시간 갱신
                redisTemplate.expire(sessionId, sessionExpiry, TimeUnit.MINUTES);
                logger.info("세션 조회됨: 세션ID={}, 사용자ID={}", sessionId, userId);
                return userId;
            } else {
                logger.warn("세션을 찾을 수 없거나 만료됨: 세션ID={}", sessionId);
                return null;
            }
        } catch (Exception e) {
            logger.error("세션 조회 실패: 세션ID={}, 에러={}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * 세션을 무효화하여 Redis에서 삭제함.
     *
     * @param sessionId 무효화할 세션 ID
     */
    public void invalidateSession(String sessionId) {
        try {
            Boolean deleted = redisTemplate.delete(sessionId);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("세션 무효화됨: 세션ID={}", sessionId);
            } else {
                logger.warn("세션을 찾을 수 없거나 이미 무효화됨: 세션ID={}", sessionId);
            }
        } catch (Exception e) {
            logger.error("세션 무효화 실패: 세션ID={}, 에러={}", sessionId, e.getMessage());
        }
    }
}
