package chat.userserver.Util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration.minutes}")
    private long validityInMinutes;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final RedisTemplate<String, Object> redisTemplate;

    private SecretKey key;

    public JwtTokenProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void initializeKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("JWT Secret Key is not set!");
            throw new IllegalArgumentException("JWT Secret Key is not set!");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        logger.info("JWT Secret Key initialized successfully. Key length: {}", key.getEncoded().length);
    }

    // JWT 토큰 생성
    public String generateToken(String username, Long userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMinutes * 60 * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰 검증 (동기 방식)
    public boolean validateToken(String token) {
        try {
            logger.info("Validating token: {}", token);
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.info("Token parsed successfully.");
            // Redis에 토큰이 저장되어 있는지 확인 (동기 방식)
            Boolean hasKey = redisTemplate.hasKey(token);
            logger.info("Token exists in Redis: {}", hasKey);
            return hasKey != null && hasKey;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserId(String token) {
        Object userId = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    // Redis에 토큰 저장 (동기 방식)
    public void storeToken(String token, Long userId) {
        redisTemplate.opsForValue().set(token, userId, Duration.ofMinutes(validityInMinutes));
    }

    // 토큰 무효화 (동기 방식)
    public boolean invalidateToken(String token) {
        Boolean deleted = redisTemplate.delete(token);
        return deleted != null && deleted;
    }
}