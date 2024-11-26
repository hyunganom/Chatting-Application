package chat.websocketserver.config;

import chat.websocketserver.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private SessionService sessionService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        System.out.println("Handshake query: " + query);

        if (query != null) {
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(param -> param.split("="))
                    .filter(keyVal -> keyVal.length == 2)
                    .collect(Collectors.toMap(
                            keyVal -> keyVal[0], keyVal -> keyVal[1]));

            String sessionId = params.get("sessionId");
            String roomIdStr = params.get("roomId");
            System.out.println("Extracted sessionId: " + sessionId);
            System.out.println("Extracted roomIdStr: " + roomIdStr);

            if (sessionId != null && roomIdStr != null) {
                Long roomId = Long.parseLong(roomIdStr);
                Long userId = sessionService.getUserIdBySession(sessionId);
                System.out.println("Retrieved userId: " + userId);

                if (userId != null) {
                    attributes.put("userId", userId);
                    attributes.put("roomId", roomId);
                    System.out.println("Attributes set: userId=" + userId + ", roomId=" + roomId);
                    return true;
                } else {
                    System.out.println("Invalid sessionId: " + sessionId);
                }
            } else {
                System.out.println("Missing sessionId or roomId in query parameters.");
            }
        } else {
            System.out.println("No query parameters found in the handshake request.");
        }
        return false; // 인증 실패 시 연결 거부
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // 필요 시 후처리 로직 추가
    }
}
