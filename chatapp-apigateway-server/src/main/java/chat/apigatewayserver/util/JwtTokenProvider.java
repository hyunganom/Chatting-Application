package chat.apigatewayserver.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration.minutes}")
    private long jwtExpirationInMinutes;

    private Key key;

    /**
     * 프로퍼티가 설정된 후 서명 키를 초기화.
     */
    @PostConstruct
    public void init() {
        // 비밀 키를 적절히 인코딩하여 서명 키로 변환
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * JWT 토큰의 유효성을 검증.
     *
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false를 방출하는 Mono<Boolean>
     */
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
                    Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token);
                    return true;
                })
                .onErrorResume(e -> Mono.just(false))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * JWT 토큰에서 사용자명을 추출.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출한 사용자명
     */
    public String getUsername(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출한 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            return Long.parseLong((String) userId);
        } else {
            throw new IllegalArgumentException("토큰 내 userId 타입이 유효하지 않습니다.");
        }
    }

    /**
     * 주어진 사용자명과 사용자 ID로 JWT 토큰을 생성.
     *
     * @param username 토큰에 포함할 사용자명
     * @param userId   토큰에 포함할 사용자 ID
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMinutes * 60 * 1000); // 분을 밀리초로 변환

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰을 파싱하여 클레임을 반환.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출한 클레임
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
