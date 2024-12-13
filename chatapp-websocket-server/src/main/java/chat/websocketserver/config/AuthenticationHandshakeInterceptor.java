package chat.websocketserver.config;

import chat.websocketserver.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationHandshakeInterceptor.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        // 요청 URI에서 쿼리 문자열을 가져옴.
        String query = request.getURI().getQuery();
        logger.info("Handshake query: {}", query);

        if (query != null) {
            // 쿼리 문자열을 파싱하여 매개변수 맵을 생성.
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(param -> param.split("=", 2))
                    .filter(keyVal -> keyVal.length == 2)
                    .collect(Collectors.toMap(
                            keyVal -> decodeURL(keyVal[0]),
                            keyVal -> decodeURL(keyVal[1])));

            // token과 roomId를 추출
            String token = params.get("token");
            String roomIdStr = params.get("roomId");
            logger.info("Extracted token: {}", token);
            logger.info("Extracted roomIdStr: {}", roomIdStr);

            if (token != null && roomIdStr != null) {
                try {
                    // 토큰 검증
                    if (jwtTokenProvider.validateToken(token)) {
                        String username = jwtTokenProvider.getUsername(token);
                        Long userId = jwtTokenProvider.getUserId(token);

                        // roomId를 Long 타입으로 변환
                        Long roomId = Long.parseLong(roomIdStr);

                        logger.info("Token is valid. Username: {}, UserId: {}", username, userId);

                        // 속성 맵에 userId와 roomId를 저장
                        attributes.put("userId", userId);
                        attributes.put("roomId", roomId);
                        logger.info("Attributes set: userId={}, roomId={}", userId, roomId);
                        return true;
                    } else {
                        logger.warn("Invalid token: {}", token);
                    }
                } catch (Exception e) {
                    logger.error("Error during token validation or parsing roomId: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("Missing token or roomId in query parameters.");
            }
        } else {
            logger.warn("No query parameters found in the handshake request.");
        }
        return false; // 인증 실패 시 연결을 거부
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }

    // URL 디코딩을 위한 헬퍼 메서드
    private String decodeURL(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}

