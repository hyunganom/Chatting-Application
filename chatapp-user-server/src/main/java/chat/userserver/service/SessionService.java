package chat.userserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long SESSION_EXPIRY = 30; // 세션 만료 시간 (분)

    public String createSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(sessionId, username, SESSION_EXPIRY, TimeUnit.MINUTES);
        logger.info("Session created for user: {}, sessionId: {}", username, sessionId);
        return sessionId;
    }

    public String getUsernameFromSession(String sessionId) {
        String username = (String) redisTemplate.opsForValue().get(sessionId);
        if (username != null) {
            logger.info("Session found: sessionId: {}, username: {}", sessionId, username);
        } else {
            logger.warn("Session not found or expired: sessionId: {}", sessionId);
        }
        return username;
    }

    public void invalidateSession(String sessionId) {
        redisTemplate.delete(sessionId);
        logger.info("Session invalidated: sessionId: {}", sessionId);
    }
}
