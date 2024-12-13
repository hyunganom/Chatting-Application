package chat.apigatewayserver.filter;

import chat.apigatewayserver.util.JwtTokenProvider;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            logger.info("Incoming URI: {}", path);

            // 인증을 건너뛸 경로 목록
            String[] skipAuthPaths = {
                    "/users/login",
                    "/users/register",
                    "/users/kakao-login",
                    "/ws/**"
            };

            // 현재 요청 경로가 인증을 건너뛰어야 하는지 확인
            boolean skipAuth = false;
            for (String skipPath : skipAuthPaths) {
                if (pathMatcher.match(skipPath, path)) {
                    skipAuth = true;
                    break;
                }
            }

            if (skipAuth) {
                logger.info("Skipping authentication for path: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 로그: 요청 URI와 헤더 출력
            logger.info("Incoming request URI: {}", exchange.getRequest().getURI());
            logger.info("Authorization Header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                logger.info("Extracted Token: {}", token);

                return jwtTokenProvider.validateToken(token)
                        .flatMap(valid -> {
                            if (valid) {
                                String username = jwtTokenProvider.getUsername(token);
                                Long userId = jwtTokenProvider.getUserId(token);

                                // 로그: 토큰 검증 성공
                                logger.info("Token is valid. Username: {}, UserId: {}", username, userId);

                                exchange.getRequest().mutate()
                                        .header("X-User-Id", userId.toString())
                                        .header("X-Username", username)
                                        .build();

                                return chain.filter(exchange);
                            } else {
                                // 로그: 토큰 검증 실패
                                logger.warn("Token is invalid");
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }
                        })
                        .onErrorResume(e -> {
                            // 로그: 예외 발생
                            logger.error("Error validating token", e);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        });
            }

            // 로그: Authorization 헤더 없음
            logger.warn("Authorization header is missing or does not start with Bearer");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        };
    }

    public static class Config {
        // 필요시 구성 추가
    }
}
