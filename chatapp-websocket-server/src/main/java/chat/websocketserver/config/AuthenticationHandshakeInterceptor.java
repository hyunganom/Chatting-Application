package chat.websocketserver.config;

import chat.websocketserver.service.SessionService;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    // Logger 인스턴스 생성
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationHandshakeInterceptor.class);

    @Autowired
    private SessionService sessionService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        // 요청 URI에서 쿼리 문자열을 가져옴.
        String query = request.getURI().getQuery();
        logger.info("Handshake query: " + query);

        if (query != null) {
            // 쿼리 문자열을 파싱하여 매개변수 맵을 생성.
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(param -> param.split("=", 2))
                    .filter(keyVal -> keyVal.length == 2)
                    .collect(Collectors.toMap(
                            keyVal -> decodeURL(keyVal[0]), // 키를 디코딩.
                            keyVal -> decodeURL(keyVal[1]))); // 값을 디코딩.

            // sessionId와 roomId를 추출
            String sessionId = params.get("sessionId");
            String roomIdStr = params.get("roomId");
            logger.info("Extracted sessionId: {}", sessionId);
            logger.info("Extracted roomIdStr: {}", roomIdStr);

            if (sessionId != null && roomIdStr != null) {
                try {
                    // roomId를 Long 타입으로 변환
                    Long roomId = Long.parseLong(roomIdStr);
                    // sessionId를 사용하여 userId를 가져
                    Long userId = sessionService.getUserIdBySession(sessionId);
                    logger.info("Retrieved userId: {}", userId);

                    if (userId != null) {
                        // 속성 맵에 userId와 roomId를 저장
                        attributes.put("userId", userId);
                        attributes.put("roomId", roomId);
                        logger.info("Attributes set: userId={}, roomId={}", userId, roomId);
                        return true;
                    } else {
                        logger.warn("Invalid sessionId: {}", sessionId);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid roomId format: {}", roomIdStr);
                }
            } else {
                logger.warn("Missing sessionId or roomId in query parameters.");
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
