package chat.userserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long SESSION_EXPIRY = 30; // 세션 만료 시간 (분)

    /**
     * 새로운 세션 생성
     *
     * @param userId   사용자 ID
     * @param username 사용자 이름
     * @return 생성된 세션 ID
     */
    public String createSession(Long userId, String username) {
        String sessionId = UUID.randomUUID().toString();
        // 세션 ID에 사용자 정보 저장 (Map 형태)
        Map<String, Object> sessionData = Map.of(
                "userId", userId,
                "username", username
        );
        redisTemplate.opsForHash().putAll(sessionId, sessionData);
        redisTemplate.expire(sessionId, SESSION_EXPIRY, TimeUnit.MINUTES);
        logger.info("Session created for user: {}, sessionId: {}", username, sessionId);
        return sessionId;
    }

    /**
     * 세션 ID로부터 사용자 이름 조회
     *
     * @param sessionId 세션 ID
     * @return 사용자 이름 또는 null
     */
    public String getUsernameFromSession(String sessionId) {
        String username = (String) redisTemplate.opsForHash().get(sessionId, "username");
        if (username != null) {
            logger.info("Session found: sessionId: {}, username: {}", sessionId, username);
        } else {
            logger.warn("Session not found or expired: sessionId: {}", sessionId);
        }
        return username;
    }

    /**
     * 세션 ID로부터 사용자 ID 조회
     *
     * @param sessionId 세션 ID
     * @return 사용자 ID 또는 null
     */
    public Long getUserIdBySession(String sessionId) {
        Object userIdObj = redisTemplate.opsForHash().get(sessionId, "userId");
        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            logger.info("Session found: sessionId: {}, userId: {}", sessionId, userId);
            return userId;
        } else {
            logger.warn("Session not found or expired: sessionId: {}", sessionId);
            return null;
        }
    }

    /**
     * 세션 무효화
     *
     * @param sessionId 세션 ID
     */
    public void invalidateSession(String sessionId) {
        redisTemplate.delete(sessionId);
        logger.info("Session invalidated: sessionId: {}", sessionId);
    }
}
