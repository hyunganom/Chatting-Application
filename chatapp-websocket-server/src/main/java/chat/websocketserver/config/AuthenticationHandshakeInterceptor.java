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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationHandshakeInterceptor.class);

    @Autowired
    private JwtTokenProvider jwt;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String query = request.getURI().getQuery();          // token=…&roomId=…
        log.info("Handshake query={}", query);

        if (query == null) return false;

        Map<String, String> params =
                Arrays.stream(query.split("&"))
                        .map(s -> s.split("=", 2))
                        .filter(p -> p.length == 2)
                        .collect(Collectors.toMap(
                                p -> decode(p[0]),
                                p -> decode(p[1])));

        String token  = params.get("token");
        String roomId = params.get("roomId");

        if (token == null || roomId == null) {
            log.warn("Missing token or roomId");
            return false;
        }
        try {
            if (!jwt.validateToken(token)) {
                log.warn("Invalid token={}", token);
                return false;
            }
            Long userId = jwt.getUserId(token);
            attributes.put("userId", userId);
            attributes.put("roomId", Long.parseLong(roomId));

            log.info("Handshake OK userId={}, roomId={}", userId, roomId);
            return true;
        } catch (Exception e) {
            log.error("Handshake failed", e);
            return false;
        }
    }

    @Override public void afterHandshake(ServerHttpRequest req,
                                         ServerHttpResponse res,
                                         WebSocketHandler wsHandler,
                                         Exception ex) {}

    private String decode(String src) {
        return URLDecoder.decode(src, StandardCharsets.UTF_8);
    }
}
